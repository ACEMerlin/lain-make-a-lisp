package lain;

import java.util.*;

/**
 * Created by merlin on 16/7/27.
 */
class Core {
    private static Types.LainFunction plus = new Types.LainFunction("+") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            if (args.size() == 0) {
                return new Types.LainInteger(0);
            } else {
                if (args.getValue().stream()
                        .allMatch(p -> Types.LainInteger.class.isAssignableFrom(p.getClass()))) {
                    return args.getValue().stream()
                            .map(e -> (Types.LainInteger) e)
                            .reduce(new Types.LainInteger(0), Types.LainInteger::add);
                } else {
                    return new Types.LainDecimal(args.getValue().stream()
                            .map(e -> ((Types.LainNumber) e).doubleValue())
                            .reduce(0.0, Double::sum));
                }
            }
        }
    };

    private static Types.LainFunction minus = new Types.LainFunction("-") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            if (args.size() == 0) {
                throw new Types.LainException("wrong number of parameters to '-' function");
            } else if (args.size() == 1) {
                if (args.getValue().stream()
                        .allMatch(p -> Types.LainInteger.class.isAssignableFrom(p.getClass()))) {
                    int first = ((Types.LainInteger) args.get(0)).intValue();
                    return new Types.LainInteger(-first);
                } else {
                    double first = ((Types.LainDecimal) args.get(0)).doubleValue();
                    return new Types.LainDecimal(first);
                }
            } else {
                if (args.getValue().stream()
                        .allMatch(p -> Types.LainInteger.class.isAssignableFrom(p.getClass()))) {
                    Types.LainInteger first = ((Types.LainInteger) args.get(0));
                    return ((Types.LainList) args.sub(1)).getValue().stream()
                            .map(e -> (Types.LainInteger) e)
                            .reduce(first, Types.LainInteger::minus);
                } else {
                    double first = ((Types.LainDecimal) args.get(0)).doubleValue();
                    return new Types.LainDecimal(first - ((Types.LainList) args.sub(1)).getValue().stream()
                            .map(e -> ((Types.LainNumber) e).doubleValue())
                            .reduce(0.0, Double::sum));
                }
            }
        }
    };

    private static Types.LainFunction times = new Types.LainFunction("*") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            if (args.size() == 0) {
                return new Types.LainInteger(1);
            } else {
                if (args.getValue().stream()
                        .allMatch(p -> Types.LainInteger.class.isAssignableFrom(p.getClass()))) {
                    return args.getValue().stream()
                            .map(e -> (Types.LainInteger) e)
                            .reduce(new Types.LainInteger(1), Types.LainInteger::times);
                } else {
                    return new Types.LainDecimal(args.getValue().stream()
                            .map(e -> ((Types.LainNumber) e).doubleValue())
                            .reduce(1.0, Double::sum));
                }
            }
        }
    };

    private static Types.LainFunction divides = new Types.LainFunction("/") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            if (args.size() == 0) {
                throw new Types.LainException("wrong number of parameters to '/' function");
            } else if (args.size() == 1) {
                double first = ((Types.LainNumber) args.get(0)).doubleValue();
                return new Types.LainDecimal(1 / first);
            } else {
                if (args.getValue().stream()
                        .allMatch(p -> Types.LainInteger.class.isAssignableFrom(p.getClass()))) {
                    Types.LainInteger first = ((Types.LainInteger) args.get(0));
                    int ret = first.intValue();
                    List<Types.LainObj> list = ((Types.LainList) args.sub(1)).getValue();
                    for (Types.LainObj aList : list) {
                        ret = ret / ((Types.LainInteger) aList).intValue();
                    }
                    return new Types.LainInteger(ret);
                } else {
                    Types.LainNumber first = ((Types.LainNumber) args.get(0));
                    double ret = first.doubleValue();
                    List<Types.LainObj> list = ((Types.LainList) args.sub(1)).getValue();
                    for (Types.LainObj aList : list) {
                        ret = ret / ((Types.LainNumber) aList).doubleValue();
                    }
                    return new Types.LainDecimal(ret);
                }
            }
        }
    };

    private static Types.LainFunction equal = new Types.LainFunction("=") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                Types.LainList rest = ((Types.LainList) args.sub(i + 1));
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (Types.equal(args.get(i), rest.get(j)).equals(Types.False)) {
                        return Types.False;
                    }
                }
            }
            return Types.True;
        }
    };

    private static Types.LainFunction lt = new Types.LainFunction("<") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                Types.LainList rest = ((Types.LainList) args.sub(i + 1));
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (Types.LainNumber.lt(
                            (Types.LainNumber) args.get(i),
                            (Types.LainNumber) rest.get(j)).equals(Types.False)) {
                        return Types.False;
                    }
                }
            }
            return Types.True;
        }
    };

    private static Types.LainFunction lte = new Types.LainFunction("<=") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                Types.LainList rest = ((Types.LainList) args.sub(i + 1));
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (Types.LainNumber.lte(
                            (Types.LainNumber) args.get(i),
                            (Types.LainNumber) rest.get(j)).equals(Types.False)) {
                        return Types.False;
                    }
                }
            }
            return Types.True;
        }
    };

    private static Types.LainFunction gt = new Types.LainFunction(">") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                Types.LainList rest = ((Types.LainList) args.sub(i + 1));
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (Types.LainNumber.gt(
                            (Types.LainNumber) args.get(i),
                            (Types.LainNumber) rest.get(j)).equals(Types.False)) {
                        return Types.False;
                    }
                }
            }
            return Types.True;
        }
    };

    private static Types.LainFunction gte = new Types.LainFunction(">=") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                Types.LainList rest = ((Types.LainList) args.sub(i + 1));
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (Types.LainNumber.gte(
                            (Types.LainNumber) args.get(i),
                            (Types.LainNumber) rest.get(j)).equals(Types.False)) {
                        return Types.False;
                    }
                }
            }
            return Types.True;
        }
    };

    private static Types.LainFunction prn = new Types.LainFunction("prn") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            StringJoiner sj = new StringJoiner(" ", "", "");
            for (Types.LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, true));
            }
            System.out.print(sj.toString() + "\n");
            return Types.Nil;
        }
    };

    private static Types.LainFunction prStr = new Types.LainFunction("pr-str") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            StringJoiner sj = new StringJoiner(" ", "", "");
            for (Types.LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, true));
            }
            return new Types.LainString(sj.toString());
        }
    };

    private static Types.LainFunction str = new Types.LainFunction("str") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            StringJoiner sj = new StringJoiner("", "", "");
            for (Types.LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, false));
            }
            return new Types.LainString(sj.toString());
        }
    };

    private static Types.LainFunction println = new Types.LainFunction("println") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            StringJoiner sj = new StringJoiner(" ", "", "");
            for (Types.LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, false));
            }
            System.out.print(sj.toString() + "\n");
            return Types.Nil;
        }
    };

    private static Types.LainFunction list = new Types.LainFunction("list") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            return new Types.LainList(args.getValue());
        }
    };

    private static Types.LainFunction isList = new Types.LainFunction("list?") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            for (Types.LainObj arg : args.getValue()) {
                if (arg instanceof Types.LainVector) {
                    return Types.False;
                }
                if (!(arg instanceof Types.LainList)) {
                    return Types.False;
                }
            }
            return Types.True;
        }
    };

    private static Types.LainFunction isEmpty = new Types.LainFunction("empty?") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            for (Types.LainObj arg : args.getValue()) {
                if (!(arg instanceof Types.LainList)) {
                    return Types.False;
                } else if (((Types.LainList) arg).size() != 0) {
                    return Types.False;
                }
            }
            return Types.True;
        }
    };

    private static Types.LainFunction count = new Types.LainFunction("count") {
        @Override
        public Types.LainObj apply(Types.LainList args) throws Types.LainException {
            if (!(args.get(0) instanceof Types.LainList)) {
                return new Types.LainInteger(0);
            } else {
                return new Types.LainInteger(((Types.LainList) args.get(0)).size());
            }
        }
    };

    private static Map<String, Types.LainObj> core = new HashMap<>();

    static {
        core.put(plus.name, plus);
        core.put(minus.name, minus);
        core.put(times.name, times);
        core.put(divides.name, divides);
        core.put(Types.True.getValue(), Types.True);
        core.put(Types.False.getValue(), Types.False);
        core.put(Types.Nil.getValue(), Types.Nil);
        core.put(equal.name, equal);
        core.put(lt.name, lt);
        core.put(lte.name, lte);
        core.put(gt.name, gt);
        core.put(gte.name, gte);
        core.put(prn.name, prn);
        core.put(prStr.name, prStr);
        core.put(str.name, str);
        core.put(println.name, println);
        core.put(list.name, list);
        core.put(isList.name, isList);
        core.put(isEmpty.name, isEmpty);
        core.put(count.name, count);
    }

    static Map<String, Types.LainObj> ns = Collections.unmodifiableMap(core);
}
