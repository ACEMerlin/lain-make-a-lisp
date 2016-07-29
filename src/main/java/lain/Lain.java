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
        Types.LainObj a0, a1, a2, a3;
        while (true) {
            if (!(ast instanceof Types.LainList) || (ast instanceof Types.LainVector)) {
                return evalAst(ast, env);
            }
            Types.LainList list = (Types.LainList) ast;
            if (list.size() == 0) {
                return ast;
            }
            a0 = ((Types.LainList) ast).get(0);
            a1 = ((Types.LainList) ast).get(1);
            a2 = ((Types.LainList) ast).get(2);
            a3 = ((Types.LainList) ast).get(3);
            if (a0 instanceof Types.LainList) {
                Types.LainObj obj = EVAL(a0, env);
                if (obj instanceof Types.LainFunction) {
                    Types.LainObj args = evalAst(list.rest(), env);
                    return ((Types.LainFunction) obj).apply((Types.LainList) args);
                }
            }
            if (!(a0 instanceof Types.LainSymbol))
                throw new Reader.ParseException("can't apply non-symbol: " + a0.toString());
            Types.LainSymbol symbol = (Types.LainSymbol) a0;
            switch (symbol.getValue()) {
                case "def!":
                    Types.LainObj result = EVAL(a2, env);
                    env.set((Types.LainSymbol) a1, result);
                    return result;
                case "let*":
                    Env newEnv = new Env(env);
                    Types.LainList pairs = (Types.LainList) a1;
                    for (int i = 0; i < pairs.size(); i = i + 2) {
                        newEnv.set((Types.LainSymbol) pairs.get(i),
                                EVAL(pairs.get(i + 1), newEnv));
                    }
                    ast = a2;
                    env = newEnv;
                    break;
                case "do":
                    Types.LainList doArgs = (list.rest());
                    for (int i = 0; i < doArgs.getValue().size(); i++) {
                        if (i != doArgs.getValue().size() - 1) {
                            EVAL(doArgs.get(i), env);
                        } else {
                            return EVAL(doArgs.get(i), env);
                        }
                    }
                    evalAst(list.sub(1, list.size() - 1), env);
                    ast = list.get(list.size() - 1);
                    break;
                case "if":
                    Types.LainObj predicate = EVAL(a1, env);
                    if (!predicate.equals(Types.Nil) && !predicate.equals(Types.False)) {
                        ast = a2;
                        break;
                    } else if (a3 == null) {
                        return Types.Nil;
                    } else {
                        ast = a3;
                        break;
                    }
                case "lambda":
                case "λ":
                    //((lambda (a b) (+ a b)) 2 3)
                    //(a b): a1
                    //(+ a b) : a2
                    //a -> 2, b -> 3 : new env (args)
                    final Types.LainObj a2f = a2;
                    final Types.LainObj a1f = a1;
                    return new Types.LainFunction("lambda", a2, env, (Types.LainList) a1) {
                        @Override
                        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
                            return EVAL(a2f, new Env(env, (Types.LainList) a1f, args));
                        }
                    };
                default:
                    Types.LainList evalResult = (Types.LainList) evalAst(list, env);
                    Types.LainFunction caller = (Types.LainFunction) evalResult.get(0);
                    if (caller.name.equals("lambda") || caller.name.equals("λ")) {
                        ast = caller.ast;
                        env = new Env(caller.env, caller.prams, evalResult.sub(1));
                        break;
                    } else {
                        return caller.apply(evalResult.rest());
                    }
            }
        }
    }

    private static String PRINT(Types.LainObj exp) {
        return Printer.printStr(exp, true);
    }

    private static Types.LainObj RE(Env env, String str) throws Types.LainException {
        return EVAL(READ(str), env);
    }

    public static void main(String[] args) throws Types.LainException {
        String prompt = "user> ";
        Env env = new Env(Core.ns);
        RE(env, "(def! not (lambda (a) (if a false true)))");
        while (true) {
            String line;
            try {
                line = ReadLine.jnaReadLine(prompt);
                if (line == null || line.equals(""))
                    continue;
                Types.LainObj lainObj = RE(env, line);
                if (lainObj != null)
                    System.out.println(PRINT(lainObj));
            } catch (IOException | ReadLine.EOFException e) {
                break;
            } catch (Types.LainException e) {
                e.printStackTrace();
            }
        }
    }
}
