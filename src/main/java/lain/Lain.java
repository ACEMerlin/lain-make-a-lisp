package lain;

import lain.Types.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static lain.Types.*;

/**
 * Created by merlin on 16/7/27.
 */
public class Lain {

    private static LainObj evalAst(LainObj ast, Env env) throws LainException {
        if (ast instanceof LainSymbol) {
            return env.get((LainSymbol) ast);
        } else if (ast instanceof LainVector) {
            LainVector newVector = new LainVector();
            LainVector oldVector = (LainVector) ast;
            for (LainObj item : oldVector.getValue()) {
                newVector.accumulate(EVAL(item, env));
            }
            return newVector;
        } else if (ast instanceof LainList) {
            LainList newList = new LainList();
            LainList oldList = (LainList) ast;
            for (LainObj item : oldList.getValue()) {
                newList.accumulate(EVAL(item, env));
            }
            return newList;
        } else if (ast instanceof LainHashMap) {
            LainHashMap newHashMap = new LainHashMap();
            LainHashMap oldHashMap = (LainHashMap) ast;
            for (Object o : oldHashMap.entries()) {
                Map.Entry entry = (Map.Entry) o;
                newHashMap.put((LainAtom) entry.getKey(), EVAL(
                        (LainObj) entry.getValue(), env));
            }
            return newHashMap;
        }
        return ast;
    }

    private static LainObj READ(String str) throws LainException {
        return Reader.readStr(str);
    }

    private static LainObj EVAL(LainObj ast, Env env) throws LainException {
        LainObj a0, a1, a2, a3;
        while (true) {
            if (!(ast instanceof LainList) || (ast instanceof LainVector)) {
                return evalAst(ast, env);
            }
            LainObj expanded = macroexpand(ast, env);
            if (!(expanded instanceof LainList)) {
                return evalAst(expanded, env);
            }
            LainList list = (LainList) expanded;
            if (list.size() == 0) {
                return ast;
            }
            a0 = list.get(0);
            a1 = list.get(1);
            a2 = list.get(2);
            a3 = list.get(3);
            if (a0 instanceof LainList) {
                LainObj obj = EVAL(a0, env);
                if (obj instanceof LainFunction) {
                    LainObj args = evalAst(list.rest(), env);
                    return ((LainFunction) obj).apply((LainList) args);
                }
            }
            if (!(a0 instanceof LainSymbol))
                throw new LainException("can't apply non-symbol: " + a0.toString());
            LainSymbol symbol = (LainSymbol) a0;
            switch (symbol.getValue()) {
                case "def!":
                    LainObj result = EVAL(a2, env);
                    env.set((LainSymbol) a1, result);
                    return result;
                case "let":
                    Env newEnv = new Env(env);
                    LainList pairs = (LainList) a1;
                    for (int i = 0; i < pairs.size(); i = i + 2) {
                        newEnv.set((LainSymbol) pairs.get(i),
                                EVAL(pairs.get(i + 1), newEnv));
                    }
                    ast = a2;
                    env = newEnv;
                    break;
                case "do":
                    LainList doArgs = (list.rest());
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
                    LainObj predicate = EVAL(a1, env);
                    if (!predicate.equals(Nil) && !predicate.equals(False)) {
                        ast = a2;
                        break;
                    } else if (a3 == null) {
                        return Nil;
                    } else {
                        ast = a3;
                        break;
                    }
                case "lambda":
                case "λ":
                    //((lambda (a b) (+ a b)) 2 3)
                    //(a b): a1
                    //(+ a b) : a2
                    //a -> 2, b -> 3 : new env (a1 -> args)
                    final LainObj a2f = a2;
                    final LainObj a1f = a1;
                    return new LainFunction("lambda", a2, env, (LainList) a1) {
                        @Override
                        public LainObj apply(LainList args) throws LainException {
                            return EVAL(a2f, new Env(getEnv(), (LainList) a1f, args));
                        }
                    };
                case "quote":
                    return list.get(1);
                case "quasiquote":
                    ast = quasiquote(a1);
                    break;
                case "defmacro!":
                    LainFunction marcoResult = (LainFunction) EVAL(a2, env);
                    marcoResult.setMacro(true);
                    env.set((LainSymbol) a1, marcoResult);
                    return marcoResult;
                case "macroexpand":
                    return macroexpand(a1, env);
                case "try":
                    try {
                        return EVAL(a1, env);
                    } catch (Throwable t) {
                        if (list.size() > 2) {
                            LainObj exc;
                            LainObj a20 = ((LainList) a2).get(0);
                            if (((LainSymbol) a20).getValue().equals("catch")) {
                                if (t instanceof LainException) {
                                    exc = ((LainException) t).getValue();
                                } else {
                                    StringWriter sw = new StringWriter();
                                    t.printStackTrace(new PrintWriter(sw));
                                    String tstr = sw.toString();
                                    exc = new LainString(t.getMessage() + ": " + tstr);
                                }
                                return EVAL(((LainList) a2).get(2),
                                        new Env(env, ((LainList) a2).sub(1, 2),
                                                new LainList(exc)));
                            }
                        }
                        throw t;
                    }
                default:
                    LainList evalResult = (LainList) evalAst(list, env);
                    LainFunction caller = (LainFunction) evalResult.get(0);
                    if (caller.name.equals("lambda") || caller.name.equals("λ")) {
                        ast = caller.getAst();
                        env = new Env(caller.getEnv(), caller.getPrams(), evalResult.sub(1));
                        break;
                    } else {
                        return caller.apply(evalResult.rest());
                    }
            }
        }
    }

    private static boolean isMacroCall(LainObj ast, Env env) throws LainException {
        if (ast instanceof LainList) {
            LainObj a0 = ((LainList) ast).get(0);
            if (a0 instanceof LainSymbol &&
                    env.find((LainSymbol) a0) != null) {
                LainObj macro = env.get((LainSymbol) a0);
                if (macro instanceof LainFunction &&
                        ((LainFunction) macro).isMacro()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static LainObj macroexpand(LainObj ast, Env env) throws LainException {
        while (isMacroCall(ast, env)) {
            ast = ((LainFunction) env.get((LainSymbol) ((LainList) ast).get(0)))
                    .apply(((LainList) ast).rest());
        }
        return ast;
    }

    private static boolean isPair(LainObj obj) {
        return obj instanceof LainList && ((LainList) obj).size() > 0;
    }

    /*
     * (def! lst (quote (2 3))) -> (2 3)
     * (quasiquote (1 (unquote lst))) -> (1 (2 3))
     * (quasiquote (1 (splice-unquote lst))) -> (1 2 3)
     */
    private static LainObj quasiquote(LainObj ast) {
        boolean isVec = false;
        if (LainVector.class.isAssignableFrom(ast.getClass())) {
            isVec = true;
        }
        if (!isPair(ast)) {
            return new LainList(new LainSymbol("quote"), ast);
        } else {
            LainObj a0 = ((LainList) ast).get(0);
            if ((a0 instanceof LainSymbol) &&
                    (((LainSymbol) a0).getValue().equals("unquote"))) {
                return ((LainList) ast).get(1);
            } else if (isPair(a0)) {
                LainObj a00 = ((LainList) a0).get(0);
                if ((a00 instanceof LainSymbol) &&
                        (((LainSymbol) a00).getValue().equals("splice-unquote"))) {
                    if (!isVec) {
                        return new LainList(new LainSymbol("concat"),
                                ((LainList) a0).get(1),
                                quasiquote(((LainList) ast).rest()));
                    } else {
                        return new LainList(new LainSymbol("concat-vec"),
                                ((LainList) a0).get(1),
                                quasiquote(((LainList) ast).rest()));
                    }
                }
            }
            if (!isVec) {
                return new LainList(new LainSymbol("cons"),
                        quasiquote(a0),
                        quasiquote(((LainList) ast).rest()));
            } else {
                return new LainList(new LainSymbol("cons-vec"),
                        quasiquote(a0),
                        quasiquote(((LainList) ast).rest()));
            }
        }
    }

    private static String PRINT(LainObj exp) {
        return Printer.printStr(exp, true);
    }

    private static LainObj RE(Env env, String str) throws LainException {
        return EVAL(READ(str), env);
    }

    public static void main(String[] args) throws LainException {
        String prompt = "user> ";
        final Env env = new Env(Core.ns);
        env.set(new LainSymbol("eval"), new LainFunction("eval") {
            @Override
            public LainObj apply(LainList args) throws LainException {
                return EVAL(args.get(0), env);
            }
        });
        LainList lainArgs = new LainList();
        for (String arg : args) {
            lainArgs.accumulate(new LainString(arg));
        }
        env.set(new LainSymbol("*ARGV*"), lainArgs);
        RE(env, "(def! load-file (lambda (f) (eval (read-string (str \"(do \" (slurp f) \")\")))))");
        if (args.length > 0) {
            RE(env, "(load-file \"" + args[0] + "\")");
            return;
        }
        RE(env, "(def! *host-language* \"java\")");
        RE(env, "(println (str \"Mal [\" *host-language* \"]\"))");
        while (true) {
            String line;
            try {
                line = ReadLine.jnaReadLine(prompt);
                if (line == null || line.equals(""))
                    continue;
                LainObj lainObj = RE(env, line);
                if (lainObj != null)
                    System.out.println(PRINT(lainObj));
            } catch (IOException | ReadLine.EOFException e) {
                break;
            } catch (LainException e) {
                System.out.println("Error: " + Printer.printStr(e.getValue(), false));
            }
        }
    }
}
