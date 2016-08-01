package lain;

import lain.Types.*;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static lain.Types.*;


/**
 * Created by merlin on 16/7/26.
 */
class Reader {
    private int position;
    private List<String> tokens;

    static class ParseException extends LainException {
        ParseException(String message) {
            super(message);
        }
    }

    private Reader(List<String> tokens) {
        this.position = 0;
        this.tokens = tokens;
    }

    private String next() {
        return tokens.get(position++);
    }

    private String peek() {
        if (position < tokens.size())
            return tokens.get(position);
        else
            return null;
    }

    static LainObj readStr(String str) throws ParseException {
        return readForm(new Reader(tokenize(str)));
    }

    private static LainObj readForm(Reader reader) throws ParseException {
        String firstToken = reader.peek();
        LainObj ret = null;
        if (firstToken != null) {
            switch (firstToken.charAt(0)) {
                case '(':
                    ret = readList(reader, new LainList(), '(', ')');
                    break;
                case '[':
                    ret = readList(reader, new LainVector(), '[', ']');
                    break;
                case '{':
                    ret = readHashMap(reader);
                    break;
                case ')':
                    throw new ParseException("unexpected ')'");
                case ']':
                    throw new ParseException("unexpected ']'");
                case '}':
                    throw new ParseException("unexpected '}'");
                case '@':
                    reader.next();
                    return new LainList(new LainSymbol("deref"), readForm(reader));
                default:
                    ret = readAtom(reader);
            }
            if (ret == null)
                throw new ParseException("parse error");
        }
        return ret;
    }

    private static LainObj readAtom(Reader reader) throws ParseException {
        String token = reader.next();
        Pattern pattern = Pattern.compile("(^-?[0-9]+$)|(^-?[0-9][0-9.]*$)|(^nil$)|(^true$)|(^false$)|^\"(.*)\"$|:(.*)|(^[^\"]*$)");
        Matcher matcher = pattern.matcher(token);
        if (!matcher.find()) {
            throw new ParseException("unrecognized token '" + token + "'");
        }
        if (matcher.group(1) != null) {
            return new LainInteger(Integer.parseInt(matcher.group(1)));
        } else {
            if (matcher.group(2) != null) {
                return new LainDecimal(Double.parseDouble(matcher.group(2)));
            } else if (matcher.group(3) != null) {
                return Nil;
            } else if (matcher.group(4) != null) {
                return True;
            } else if (matcher.group(5) != null) {
                return False;
            } else if (matcher.group(6) != null) {
                return new LainString(StringEscapeUtils.unescapeJson(matcher.group(6)));
            } else if (matcher.group(7) != null) {
                return new LainKeyword("\u029e" + matcher.group(7));
            } else if (matcher.group(8) != null) {
                return new LainSymbol(matcher.group(8));
            } else {
                throw new ParseException("unrecognized '" + matcher.group(0) + "'");
            }
        }
    }

    private static LainObj readList(Reader reader, LainList list, char start, char end) throws ParseException {
        String token = reader.next();
        if (token.charAt(0) != start) {
            throw new ParseException("parse error");
        } else {
            while ((token = reader.peek()) != null && token.charAt(0) != end) {
                list.accumulate(readForm(reader));
            }
            if (token == null) {
                throw new ParseException("expected: '" + end + "'!");
            }
            reader.next();
            return list;
        }
    }

    private static LainObj readHashMap(Reader reader) throws ParseException {
        LainList list = (LainList) readList(reader, new LainList(), '{', '}');
        return new LainHashMap(list);
    }

    /*
     * 1. [\s ,]*:
     *    排除空格和逗号
     *    Matches any number of whitespaces or commas.
     *    This is not captured so it will be ignored and not tokenized.
     * 2. ~@:
     *    捕获~@
     *    Captures the special two-characters ~@ (tokenized).
     * 3. [\[\]{}()'`~^@]:
     *    捕获[]{}'`~^@中的一个
     *    Captures any special single character,
     *    one of []{}'`~^@ (tokenized).
     * 4. "(?:[\\].|[^\\"])*":
     *    捕获从"开始并从"结束的字符串(忽略中间的转义,如\n,\")
     *    会捕获空字符串:""
     *    Starts capturing at a double-quote and stops at the next double-quote
     *    unless it was proceeded by a backslash in which case
     *    it includes it until the next double-quote (tokenized).
     * 5. ;.*:
     *    捕获注释(以;开头的句子)
     *    Captures any sequence of characters starting with ; (tokenized).
     * 6. [^\s\[\]{}('"`,;)]*:
     *    不捕获以空白、[、]、{、}、(、'、"、`、,、;、)特殊字符开头的字符串
     *    也就是说捕获符、数字、true、false、nil等需要的字符串
     *    Captures a sequence of zero or more non special characters
     *    (e.g. symbols, numbers, "true", "false", and "nil")
     *    and is sort of the inverse of the one above that captures special characters (tokenized).
     */
    private static List<String> tokenize(String str) {
        String patternStr = "[\\s ,]*(~@|[\\[\\]{}()'`~^@]|\"(?:[\\\\].|[^\\\\\"])*\"?|;.*|[^\\s \\[\\]{}()'\"`~@,;]*)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(str);
        List<String> ret = new ArrayList<>();
        while (matcher.find()) {
            String token = matcher.group(1);
            if (token != null && !token.equals("")
                    && !(token.charAt(0) == ';'))
                ret.add(token);
        }
        return ret;
    }
}
