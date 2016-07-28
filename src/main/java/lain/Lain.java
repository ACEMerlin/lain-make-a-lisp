package lain;

import java.io.IOException;
import java.util.Map;

/**
 * Created by merlin on 16/7/27.
 */
public class Lain {

    private static Types.LainObj evalAst(Types.LainObj ast, Env env) throws Types.LainException {
        if (ast instanceof Types.LainSymbol) {
            return env.get((Types.LainSymbol) ast);
        } else if (ast instanceof Types.LainVector) {
            Types.LainVector newVector = new Types.LainVector();
            Types.LainVector oldVector = (Types.LainVector) ast;
            for (Types.LainObj item : oldVector.getValue()) {
                newVector.accumulate(EVAL(item, env));
            }
            return newVector;
        } else if (ast instanceof Types.LainList) {
            Types.LainList newList = new Types.LainList();
            Types.LainList oldList = (Types.LainList) ast;
            for (Types.LainObj item : oldList.getValue()) {
                newList.accumulate(EVAL(item, env));
            }
            return newList;
        } else if (ast instanceof Types.LainHashMap) {
            Types.LainHashMap newHashMap = new Types.LainHashMap();
            Types.LainHashMap oldHashMap = (Types.LainHashMap) ast;
            for (Object o : oldHashMap.entries()) {
                Map.Entry entry = (Map.Entry) o;
                newHashMap.put((Types.LainAtom) entry.getKey(), EVAL(
                        (Types.LainObj) entry.getValue(), env));
            }
            return newHashMap;
        }
        return ast;
    }

    private static Types.LainObj READ(String str) throws Reader.ParseException {
        return Reader.readStr(str);
    }

    private static Types.LainObj EVAL(Types.LainObj ast, Env env) throws Types.LainException {
        if (!(ast instanceof Types.LainList) || (ast instanceof Types.LainVector)) {
            return evalAst(ast, env);
        }
        Types.LainList list = (Types.LainList) ast;
        if (list.size() == 0) {
            return ast;
        }
        if (list.get(0) instanceof Types.LainList) {
            Types.LainObj obj = EVAL(list.get(0), env);
            if (obj instanceof Types.LainFunction) {
                Types.LainObj args = evalAst(list.rest(), env);
                return ((Types.LainFunction) obj).apply((Types.LainList) args);
            }
        }
        if (!(list.get(0) instanceof Types.LainSymbol))
            throw new Reader.ParseException("can't apply non-symbol: " + list.get(0).toString());
        Types.LainSymbol symbol = (Types.LainSymbol) list.get(0);
        switch (symbol.getValue()) {
            case "def!":
                Types.LainObj result = EVAL(list.get(2), env);
                env.set((Types.LainSymbol) list.get(1), result);
                return result;
            case "let*":
                Env newEnv = new Env(env);
                Types.LainList pairs = (Types.LainList) list.get(1);
                for (int i = 0; i < pairs.size(); i = i + 2) {
                    newEnv.set((Types.LainSymbol) pairs.get(i),
                            EVAL(pairs.get(i + 1), newEnv));
                }
                return EVAL(list.get(2), newEnv);
            case "do":
                Types.LainList rest = ((Types.LainList) list.rest());
                for (int i = 0; i < rest.getValue().size(); i++) {
                    if (i != rest.getValue().size() - 1) {
                        EVAL(rest.get(i), env);
                    } else {
                        return EVAL(rest.get(i), env);
                    }
                }
            case "if":
                Types.LainObj predicate = EVAL(list.get(1), env);
                if (!predicate.equals(Types.Nil) && !predicate.equals(Types.False)) {
                    return EVAL(list.get(2), env);
                } else if (list.get(3) == null) {
                    return Types.Nil;
                } else {
                    return EVAL(list.get(3), env);
                }
            case "lambda":
            case "λ":
                return new Types.LainFunction("lambda") {
                    @Override
                    public Types.LainObj apply(Types.LainList args) throws Types.LainException {
                        return EVAL(list.get(2), new Env(env, (Types.LainList) list.get(1), args));
                    }
                };
            default:
                Types.LainObj args = evalAst(list.rest(), env);
                Types.Lambda caller = (Types.Lambda) env.get(symbol);
                return caller.apply((Types.LainList) args);
        }
    }

    private static String PRINT(Types.LainObj exp) {
        return Printer.printStr(exp, true);
    }

    private static Types.LainObj RE(Env env, String str) throws Types.LainException {
        return EVAL(READ(str), env);
    }

    public static void main(String[] args) {
        String prompt = "user> ";
        Env env = new Env(Core.ns);
        while (true) {
            String line;
            try {
                line = ReadLine.readLine(prompt);
                if (line == null || line.equals(""))
                    continue;
                Types.LainObj lainObj = RE(env, line);
                if (lainObj != null)
                    System.out.println(PRINT(lainObj));
            } catch (IOException e) {
                break;
            } catch (Types.LainException e) {
                e.printStackTrace();
            }
        }
    }
}
