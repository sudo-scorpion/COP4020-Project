package plc.homework;

import java.util.regex.Pattern;

/**
 * Contains {@link Pattern} constants, which are compiled regular expressions.
 * See the assignment page for resources on regexes as needed.
 */
public class Regex {

    public static final Pattern
            EMAIL = Pattern.compile("[A-Za-z0-9._\\-]+@[A-Za-z0-9-]*\\.[a-z]{2,3}"),
            // 1. Find any even length of string between 10 and 20
            EVEN_STRINGS = Pattern.compile("(..){5,10}"),
            // 2. A list of positive number comma separated with square brackets
            INTEGER_LIST = Pattern.compile("\\[((\\d,[ ]*)*\\d)*[ ]*]"),
            // Integer or decimal number with + or - (optional)
            NUMBER = Pattern.compile("[+|-]?0*[\\d]+(\\.\\d+)?"),
            // Double-quoted literal string
            STRING = Pattern.compile("\"(\\\\[bnrt'\"\\\\]|[^\\\\])*\"");

}
