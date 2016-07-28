package lain;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.*;

/**
 * Created by merlin on 16/7/26.
 */
public class Types {
    public static LainSymbol equal(LainObj a, LainObj b) {
        Class ac = a.getClass(), bc = b.getClass();
        if (!(ac == bc ||
                (a instanceof LainList && b instanceof LainList))) {
            return False;
        }
        return a.equals(b) ? True : False;
    }

    public static class LainObj {
        public String toString(boolean readable) {
            return this.toString();
        }
    }

    public static abstract class LainAtom extends LainObj {
    }

    public static abstract class LainNumber extends LainAtom {
        public abstract Integer intValue();

        public abstract Double doubleValue();

        public static LainSymbol lt(LainNumber a, LainNumber b) {
            if (LainDecimal.class.isAssignableFrom(a.getClass())
                    && LainDecimal.class.isAssignableFrom(b.getClass())) {
                return a.doubleValue() < b.doubleValue() ? True : False;
            } else if (LainInteger.class.isAssignableFrom(a.getClass())
                    && LainInteger.class.isAssignableFrom(b.getClass())) {
                return a.intValue() < b.intValue() ? True : False;
            } else if (LainInteger.class.isAssignableFrom(a.getClass())
                    && LainDecimal.class.isAssignableFrom(b.getClass())) {
                return a.intValue() < b.doubleValue() ? True : False;
            } else if (LainDecimal.class.isAssignableFrom(a.getClass())
                    && LainInteger.class.isAssignableFrom(b.getClass())) {
                return a.doubleValue() < b.intValue() ? True : False;
            } else {
                return False;
            }
        }

        public static LainSymbol lte(LainNumber a, LainNumber b) {
            if (LainDecimal.class.isAssignableFrom(a.getClass())
                    && LainDecimal.class.isAssignableFrom(b.getClass())) {
                return a.doubleValue() <= b.doubleValue() ? True : False;
            } else if (LainInteger.class.isAssignableFrom(a.getClass())
                    && LainInteger.class.isAssignableFrom(b.getClass())) {
                return a.intValue() <= b.intValue() ? True : False;
            } else if (LainInteger.class.isAssignableFrom(a.getClass())
                    && LainDecimal.class.isAssignableFrom(b.getClass())) {
                return a.intValue() <= b.doubleValue() ? True : False;
            } else if (LainDecimal.class.isAssignableFrom(a.getClass())
                    && LainInteger.class.isAssignableFrom(b.getClass())) {
                return a.doubleValue() <= b.intValue() ? True : False;
            } else {
                return False;
            }
        }

        public static LainSymbol gt(LainNumber a, LainNumber b) {
            if (LainDecimal.class.isAssignableFrom(a.getClass())
                    && LainDecimal.class.isAssignableFrom(b.getClass())) {
                return a.doubleValue() > b.doubleValue() ? True : False;
            } else if (LainInteger.class.isAssignableFrom(a.getClass())
                    && LainInteger.class.isAssignableFrom(b.getClass())) {
                return a.intValue() > b.intValue() ? True : False;
            } else if (LainInteger.class.isAssignableFrom(a.getClass())
                    && LainDecimal.class.isAssignableFrom(b.getClass())) {
                return a.intValue() > b.doubleValue() ? True : False;
            } else if (LainDecimal.class.isAssignableFrom(a.getClass())
                    && LainInteger.class.isAssignableFrom(b.getClass())) {
                return a.doubleValue() > b.intValue() ? True : False;
            } else {
                return False;
            }
        }

        public static LainSymbol gte(LainNumber a, LainNumber b) {
            if (LainDecimal.class.isAssignableFrom(a.getClass())
                    && LainDecimal.class.isAssignableFrom(b.getClass())) {
                return a.doubleValue() >= b.doubleValue() ? True : False;
            } else if (LainInteger.class.isAssignableFrom(a.getClass())
                    && LainInteger.class.isAssignableFrom(b.getClass())) {
                return a.intValue() >= b.intValue() ? True : False;
            } else if (LainInteger.class.isAssignableFrom(a.getClass())
                    && LainDecimal.class.isAssignableFrom(b.getClass())) {
                return a.intValue() >= b.doubleValue() ? True : False;
            } else if (LainDecimal.class.isAssignableFrom(a.getClass())
                    && LainInteger.class.isAssignableFrom(b.getClass())) {
                return a.doubleValue() >= b.intValue() ? True : False;
            } else {
                return False;
            }
        }
    }

    public static class LainInteger extends LainNumber {
        private Integer value;

        public Integer getValue() {
            return value;
        }

        public LainInteger(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public Integer intValue() {
            return value;
        }

        @Override
        public Double doubleValue() {
            return value.doubleValue();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null
                    && obj instanceof LainInteger
                    && this.value.equals(((LainInteger) obj).value);
        }

        public static LainInteger add(LainInteger a, LainInteger b) {
            return new LainInteger(a.intValue() + b.intValue());
        }

        public static LainInteger minus(LainInteger a, LainInteger b) {
            return new LainInteger(a.intValue() - b.intValue());
        }

        public static LainInteger times(LainInteger a, LainInteger b) {
            return new LainInteger(a.intValue() * b.intValue());
        }

        public static LainInteger divides(LainInteger a, LainInteger b) {
            return new LainInteger(a.intValue() / b.intValue());
        }
    }

    public static class LainDecimal extends LainNumber {
        private Double value;

        public LainDecimal(Double value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public Double doubleValue() {
            return value;
        }

        @Override
        public Integer intValue() {
            return value.intValue();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null
                    && obj instanceof LainDecimal
                    && this.value.equals(((LainDecimal) obj).value);
        }

        public static LainDecimal add(LainDecimal a, LainDecimal b) {
            return new LainDecimal(a.doubleValue() + b.doubleValue());
        }

        public static LainDecimal minus(LainDecimal a, LainDecimal b) {
            return new LainDecimal(a.doubleValue() - b.doubleValue());
        }

        public static LainDecimal times(LainDecimal a, LainDecimal b) {
            return new LainDecimal(a.doubleValue() * b.doubleValue());
        }

        public static LainDecimal divides(LainDecimal a, LainDecimal b) {
            return new LainDecimal(a.doubleValue() / b.doubleValue());
        }
    }

    public static class LainString extends LainAtom {
        private String value;

        public LainString(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "\"" + value + "\"";
        }

        public String toString(boolean readable) {
            if (readable) {
                return "\"" + StringEscapeUtils.escapeJson(value) + "\"";
            } else {
                return value;
            }
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null
                    && obj instanceof LainString
                    && this.value.equals(((LainString) obj).value);
        }
    }

    public static class LainSymbol extends LainAtom {
        private String value;

        public String getValue() {
            return value;
        }

        public LainSymbol(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null
                    && obj instanceof LainSymbol
                    && this.value.equals(((LainSymbol) obj).value);
        }
    }

    public static class LainKeyword extends LainAtom {
        private String value;

        public LainKeyword(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return ":" + value.substring(1);
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null
                    && obj instanceof LainKeyword
                    && this.value.equals(((LainKeyword) obj).value);
        }
    }

    public static LainSymbol Nil = new LainSymbol("nil");
    public static LainSymbol True = new LainSymbol("true");
    public static LainSymbol False = new LainSymbol("false");

    public static class LainList extends LainObj {
        private List<LainObj> value;

        public List<LainObj> getValue() {
            return value;
        }

        char start = '(';
        char end = ')';

        public LainList(LainObj... list) {
            this.value = new ArrayList<LainObj>();
            accumulate(list);
        }

        public LainList(List<LainObj> list) {
            this.value = new ArrayList<>(list);
            accumulate(list);
        }

        public void accumulate(LainObj... lainObj) {
            if (lainObj != null)
                Collections.addAll(value, lainObj);
        }

        public void accumulate(List<LainObj> list) {
            if (list != null)
                Collections.addAll(list);
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ",
                    String.valueOf(start),
                    String.valueOf(end));
            for (LainObj name : value) {
                sj.add(name.toString());
            }
            return sj.toString();
        }

        @Override
        public String toString(boolean readable) {
            if (readable) {
                StringJoiner sj = new StringJoiner(" ",
                        String.valueOf(start),
                        String.valueOf(end));
                for (LainObj name : value) {
                    sj.add(name.toString(readable));
                }
                return sj.toString();
            } else {
                StringJoiner sj = new StringJoiner(" ",
                        String.valueOf(start),
                        String.valueOf(end));
                for (LainObj name : value) {
                    sj.add(StringEscapeUtils.unescapeJson(name.toString(readable)));
                }
                return sj.toString();
            }
        }

        public int size() {
            return value.size();
        }

        public LainObj get(int position) {
            if (position >= size()) {
                return null;
            } else {
                return value.get(position);
            }
        }

        public LainObj rest() {
            if (size() > 0) {
                return new LainList(value.subList(1, value.size()));
            } else {
                return new LainList();
            }
        }

        public LainObj sub(int start, int end) {
            return new LainList(value.subList(start, end));
        }

        public LainObj sub(int start) {
            return sub(start, value.size());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null
                    || !(obj instanceof LainList)
                    || size() != ((LainList) obj).size()) {
                return false;
            }
            for (int i = 0; i < size(); i++) {
                if (!get(i).equals(((LainList) obj).get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class LainVector extends LainList {
        public LainVector(LainObj... list) {
            super(list);
            start = '[';
            end = ']';
        }
    }

    public static class LainHashMap extends LainObj {
        private HashMap<LainAtom, LainObj> value;

        public Set<Map.Entry<LainAtom, LainObj>> entries() {
            return value.entrySet();
        }

        public void put(LainAtom atom, LainObj obj) {
            value.put(atom, obj);
        }

        public LainHashMap(LainObj... obj) throws Reader.ParseException {
            this(new LainList(obj));
        }

        public LainHashMap(LainList list) throws Reader.ParseException {
            value = new HashMap<LainAtom, LainObj>();
            if ((list.value.size() & 1) == 0) {
                for (int i = 0; i < list.value.size(); i = i + 2) {
                    LainObj key = list.value.get(i);
                    LainObj value = list.value.get(i + 1);
                    if ((LainString.class.isAssignableFrom(key.getClass())) |
                            (LainKeyword.class.isAssignableFrom(key.getClass()))) {
                        this.value.put((LainAtom) key, value);
                    } else {
                        throw new Reader.ParseException("wrong type of key in HashMap!");
                    }
                }
            } else {
                throw new Reader.ParseException("wrong size of HashMap!");
            }
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ", "{", "}");
            value.forEach((k, v) -> {
                sj.add(k.toString());
                sj.add(v.toString());
            });
            return sj.toString();
        }

        @Override
        public String toString(boolean readable) {
            if (readable) {
                StringJoiner sj = new StringJoiner(" ",
                        String.valueOf("{"),
                        String.valueOf("}"));
                value.forEach((k, v) -> {
                    sj.add(k.toString(readable));
                    sj.add(v.toString(readable));
                });
                return sj.toString();
            } else {
                StringJoiner sj = new StringJoiner(" ",
                        String.valueOf("{"),
                        String.valueOf("}"));
                value.forEach((k, v) -> {
                    sj.add(StringEscapeUtils.unescapeJson(k.toString(readable)));
                    sj.add(StringEscapeUtils.unescapeJson(v.toString(readable)));
                });
                return sj.toString();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null
                    || !(obj instanceof LainHashMap)
                    || value.size() != ((LainHashMap) obj).value.size()) {
                return false;
            }
            for (LainAtom key : value.keySet()) {
                if (!value.get(key).equals(((LainHashMap) obj).value.get(key))) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class LainException extends Exception {
        public LainException(String message) {
            super(message);
        }
    }

    public interface Lambda {
        LainObj apply(LainList args) throws LainException;
    }

    public static abstract class LainFunction extends LainObj implements Lambda {
        String name;

        public LainFunction(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "#<function>" + name;
        }
    }
}
