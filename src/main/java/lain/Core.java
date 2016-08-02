package lain;

import lain.Types.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static lain.Types.*;

/**
 * Created by merlin on 16/7/27.
 */
class Core {
    private static LainFunction plus = new LainFunction("+") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.size() == 0) {
                return new LainInteger(0);
            } else {
                if (args.getValue().stream()
                        .allMatch(p -> LainInteger.class.isAssignableFrom(p.getClass()))) {
                    return args.getValue().stream()
                            .map(e -> (LainInteger) e)
                            .reduce(new LainInteger(0), LainInteger::add);
                } else {
                    return new LainDecimal(args.getValue().stream()
                            .map(e -> ((LainNumber) e).doubleValue())
                            .reduce(0.0, Double::sum));
                }
            }
        }
    };

    private static LainFunction minus = new LainFunction("-") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.size() == 0) {
                throw new LainException("wrong number of parameters to '-' function");
            } else if (args.size() == 1) {
                if (args.getValue().stream()
                        .allMatch(p -> LainInteger.class.isAssignableFrom(p.getClass()))) {
                    int first = ((LainInteger) args.get(0)).intValue();
                    return new LainInteger(-first);
                } else {
                    double first = ((LainDecimal) args.get(0)).doubleValue();
                    return new LainDecimal(first);
                }
            } else {
                if (args.getValue().stream()
                        .allMatch(p -> LainInteger.class.isAssignableFrom(p.getClass()))) {
                    LainInteger first = ((LainInteger) args.get(0));
                    return (args.sub(1)).getValue().stream()
                            .map(e -> (LainInteger) e)
                            .reduce(first, LainInteger::minus);
                } else {
                    double first = ((LainDecimal) args.get(0)).doubleValue();
                    return new LainDecimal(first - (args.sub(1)).getValue().stream()
                            .map(e -> ((LainNumber) e).doubleValue())
                            .reduce(0.0, Double::sum));
                }
            }
        }
    };

    private static LainFunction times = new LainFunction("*") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.size() == 0) {
                return new LainInteger(1);
            } else {
                if (args.getValue().stream()
                        .allMatch(p -> LainInteger.class.isAssignableFrom(p.getClass()))) {
                    return args.getValue().stream()
                            .map(e -> (LainInteger) e)
                            .reduce(new LainInteger(1), LainInteger::times);
                } else {
                    return new LainDecimal(args.getValue().stream()
                            .map(e -> ((LainNumber) e).doubleValue())
                            .reduce(1.0, LainNumber::times));
                }
            }
        }
    };

    private static LainFunction divides = new LainFunction("/") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.size() == 0) {
                throw new LainException("wrong number of parameters to '/' function");
            } else if (args.size() == 1) {
                double first = ((LainNumber) args.get(0)).doubleValue();
                return new LainDecimal(1 / first);
            } else {
                if (args.getValue().stream()
                        .allMatch(p -> LainInteger.class.isAssignableFrom(p.getClass()))) {
                    LainInteger first = ((LainInteger) args.get(0));
                    int ret = first.intValue();
                    List<LainObj> list = args.sub(1).getValue();
                    for (LainObj aList : list) {
                        ret = ret / ((LainInteger) aList).intValue();
                    }
                    return new LainInteger(ret);
                } else {
                    LainNumber first = ((LainNumber) args.get(0));
                    double ret = first.doubleValue();
                    List<LainObj> list = (args.sub(1)).getValue();
                    for (LainObj aList : list) {
                        ret = ret / ((LainNumber) aList).doubleValue();
                    }
                    return new LainDecimal(ret);
                }
            }
        }
    };

    private static LainFunction equal = new LainFunction("=") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = (args.sub(i + 1));
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (equal(args.get(i), rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction lt = new LainFunction("<") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = args.sub(i + 1);
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (LainNumber.lt(
                            (LainNumber) args.get(i),
                            (LainNumber) rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction lte = new LainFunction("<=") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = args.sub(i + 1);
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (LainNumber.lte(
                            (LainNumber) args.get(i),
                            (LainNumber) rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction gt = new LainFunction(">") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = args.sub(i + 1);
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (LainNumber.gt(
                            (LainNumber) args.get(i),
                            (LainNumber) rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction gte = new LainFunction(">=") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = args.sub(i + 1);
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (LainNumber.gte(
                            (LainNumber) args.get(i),
                            (LainNumber) rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction prn = new LainFunction("prn") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            StringJoiner sj = new StringJoiner(" ", "", "");
            for (LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, true));
            }
            System.out.print(sj.toString() + "\n");
            return Nil;
        }
    };

    private static LainFunction prStr = new LainFunction("pr-str") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            StringJoiner sj = new StringJoiner(" ", "", "");
            for (LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, true));
            }
            return new LainString(sj.toString());
        }
    };

    private static LainFunction str = new LainFunction("str") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            StringJoiner sj = new StringJoiner("", "", "");
            for (LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, false));
            }
            return new LainString(sj.toString());
        }
    };

    private static LainFunction println = new LainFunction("println") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            StringJoiner sj = new StringJoiner(" ", "", "");
            for (LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, false));
            }
            System.out.print(sj.toString() + "\n");
            return Nil;
        }
    };

    private static LainFunction list = new LainFunction("list") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return new LainList(args.getValue());
        }
    };

    private static LainFunction isList = new LainFunction("list?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            for (LainObj arg : args.getValue()) {
                if (arg instanceof LainVector) {
                    return False;
                }
                if (!(arg instanceof LainList)) {
                    return False;
                }
            }
            return True;
        }
    };

    private static LainFunction isEmpty = new LainFunction("empty?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            for (LainObj arg : args.getValue()) {
                if (!(arg instanceof LainList)) {
                    return False;
                } else if (((LainList) arg).size() != 0) {
                    return False;
                }
            }
            return True;
        }
    };

    private static LainFunction count = new LainFunction("count") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (!(args.get(0) instanceof LainList)) {
                return new LainInteger(0);
            } else {
                return new LainInteger(((LainList) args.get(0)).size());
            }
        }
    };

    private static LainFunction readString = new LainFunction("read-string") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.get(0) instanceof LainString)
                return Reader.readStr(((LainString) args.get(0)).getValue());
            else
                throw new LainException("wrong type of argument to function 'read-string'");
        }
    };

    private static LainFunction slurp = new LainFunction("slurp") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            try {
                return new LainString(
                        new String(Files.readAllBytes(
                                Paths.get(((LainString) args.get(0)).getValue()))));

            } catch (IOException e) {
                throw new LainException("can't find file: " + args.get(0));
            }
        }
    };

    private static LainFunction atom = new LainFunction("atom") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return new LainAtom(args.get(0));
        }
    };

    private static LainFunction isAtom = new LainFunction("atom?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) instanceof LainAtom ? True : False;
        }
    };

    private static LainFunction deRef = new LainFunction("deref") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return ((LainAtom) args.get(0)).getAtom();
        }
    };

    private static LainFunction resetAtom = new LainFunction("reset!") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            ((LainAtom) args.get(0)).setAtom(args.get(1));
            return args.get(1);
        }
    };

    private static LainFunction swapAtom = new LainFunction("swap!") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            LainFunction func = (LainFunction) args.get(1);
            LainList restArgs = args.sub(2);
            LainAtom atom = (LainAtom) args.get(0);
            LainList newArgs = new LainList();
            newArgs.getValue().addAll(restArgs.getValue());
            newArgs.getValue().add(0, atom.getAtom());
            atom.setAtom(func.apply(newArgs));
            return atom.getAtom();
        }
    };

    private static LainFunction cons = new LainFunction("cons") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<LainObj> list = new ArrayList<>();
            list.add(args.get(0));
            list.addAll(((LainList) args.get(1)).getValue());
            return new LainList(list);
        }
    };

    private static LainFunction consVec = new LainFunction("cons-vec") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<LainObj> list = new ArrayList<>();
            list.add(args.get(0));
            list.addAll(((LainList) args.get(1)).getValue());
            return new LainVector(list);
        }
    };

    private static LainFunction concat = new LainFunction("concat") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<LainObj> list = new ArrayList<>();
            for (LainObj arg : args.getValue()) {
                list.addAll(((LainList) arg).getValue());
            }
            return new LainList(list);
        }
    };

    private static LainFunction concatVec = new LainFunction("concat-vec") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<LainObj> list = new ArrayList<>();
            for (LainObj arg : args.getValue()) {
                list.addAll(((LainList) arg).getValue());
            }
            return new LainVector(list);
        }
    };

    private static Map<String, LainObj> core = new HashMap<>();

    static {
        put(plus);
        put(minus);
        put(times);
        put(divides);
        put(True);
        put(False);
        put(Nil);
        put(equal);
        put(lt);
        put(lte);
        put(gt);
        put(gte);
        put(prn);
        put(prStr);
        put(str);
        put(println);
        put(list);
        put(isList);
        put(isEmpty);
        put(count);
        put(readString);
        put(slurp);
        put(atom);
        put(isAtom);
        put(deRef);
        put(resetAtom);
        put(swapAtom);
        put(cons);
        put(concat);
        put(concatVec);
        put(consVec);
    }

    private static void put(LainObj func) {
        core.put(func.toString(), func);
    }

    static Map<String, LainObj> ns = Collections.unmodifiableMap(core);
}
