package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LexerTests {

    @ParameterizedTest
    @MethodSource
    void testIdentifier(String test, String input, boolean success) {
        test(input, Token.Type.IDENTIFIER, success);
    }

    private static Stream<Arguments> testIdentifier() {
        return Stream.of(
                // Example Matching Test Cases
                Arguments.of("Alphabetic", "getName", true),
                Arguments.of("Alphanumeric", "thelegend27", true),
                // Matching Test Cases
                Arguments.of("Starts With Capital", "ABS", true),
                Arguments.of("Starts With Underscore", "_layout", true),
                Arguments.of("Single Char", "a", true),
                Arguments.of("Hyphens", "a-b-c", true),
                Arguments.of("Underscores", "___", true),
                Arguments.of("Single Char 2", "_", true),
                Arguments.of("Includes Underscore", "theLegend_27", true),
                Arguments.of("Includes Hyphen", "theLegend-27", true),
                // Example Non-matching Test Cases
                Arguments.of("Leading Hyphen", "-five", false),
                Arguments.of("Leading Digit", "1fish2fish3fishbluefish", false),
                // Non-matching Test Cases
                Arguments.of("Parentheses", "(invalid)", false),
                Arguments.of("Symbol", "theLegend#27", false),
                Arguments.of("Operator", "theLegend<27>", false),
                Arguments.of("Brackets", "array[i]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testInteger(String test, String input, boolean success) {
        test(input, Token.Type.INTEGER, success);
    }

    private static Stream<Arguments> testInteger() {
        return Stream.of(
                // Example Matching Test Cases
                Arguments.of("Single Digit", "1", true),
                // Matching Test Cases
                Arguments.of("Zero", "0", true),
                Arguments.of("Multiple Digits 1", "10", true),
                Arguments.of("Multiple Digits 2", "1234", true),
                Arguments.of("Signed Positive", "+134", true),
                Arguments.of("Signed Positive 2", "+123", true),
                Arguments.of("Signed Negeative", "-792", true),
                // Example Non-matching Test Cases
                Arguments.of("Decimal", "123.456", false),
                Arguments.of("Signed Decimal", "-1.0", false),
                Arguments.of("Trailing Decimal", "1.", false),
                Arguments.of("Leading Decimal", ".5", false),
                // Non-matching Test Cases
                Arguments.of("Non-numeric", "number", false),
                Arguments.of("Expression", "1+2", false),
                Arguments.of("Parentheses", "(34)", false),
                Arguments.of("Binary", "0101", false),
                Arguments.of("Multiple Zeros", "00000", false),//// here is the problem case? should be true? todo
                Arguments.of("2 signs", "+-120000", false),
                Arguments.of("Leading Space", " 34", false),
                Arguments.of("Trailing Space", "76 ", false),
                Arguments.of("Leading zeroes", "007", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDecimal(String test, String input, boolean success) {
        test(input, Token.Type.DECIMAL, success);
    }

    private static Stream<Arguments> testDecimal() {
        return Stream.of(
                // Example Matching Cases
                Arguments.of("Multiple Digits", "123.456", true),
                Arguments.of("Negative Decimal", "-1.0", true),
                // Matching Test Cases
                Arguments.of("Positive Sign", "+0.375", true),
                Arguments.of("Trailing Zeros 1", "3.000001", true),
                Arguments.of("Trailing Zeros 2", "3.000000", true),
                Arguments.of("Everything", "+354.7760000", true),
                // Example Non-matching Test Cases
                Arguments.of("Integer", "1", false),
                Arguments.of("Trailing Decimal", "1.", false),
                Arguments.of("Leading Decimal", ".5", false),
                // Non-matching Test Cases
                Arguments.of("Non-numeric", "5k", false),
                Arguments.of("Comma", "50,000", false),
                Arguments.of("Multiple Decimals", "123.456.789", false),
                Arguments.of("Dollars", "$1.99", false),
                Arguments.of("leading zeros", "007.0", false), // problem case? should be true? todo
                Arguments.of("Two decimals", "1..0", false),
                Arguments.of("Two Decimals", "1.2.3",false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testCharacter(String test, String input, boolean success) {
        test(input, Token.Type.CHARACTER, success);
    }

    private static Stream<Arguments> testCharacter() {
        return Stream.of(
                // Example Matching Test Cases
                Arguments.of("Alphabetic", "\'c\'", true),
                Arguments.of("Newline Escape", "\'\\n\'", true),
                // Matching Test Cases
                Arguments.of("Number", "\'2\'", true),
                Arguments.of("Symbol", "\'@\'", true),
                Arguments.of("Bracket", "\'[\'", true),
                Arguments.of("Escape B", "\'\\b\'", true),
                Arguments.of("Escape R", "\'\\r\'", true),
                Arguments.of("Escape T", "\'\\t\'", true),
                Arguments.of("Escape Single", "\'\'\'", true),
                Arguments.of("Escape Double", "\'\"\'", true),
                //Example Non-matching Test Cases
                Arguments.of("Empty", "\'\'", false),
                Arguments.of("Multiple", "\'abc\'", false),
                // Non-matching Test Cases
                Arguments.of("Double Quotes", "\"a\"", false),
                Arguments.of("No Quotes", "b", false),
                Arguments.of("End With Newline", "\'Q\\n\'", false),
                Arguments.of("End With Return", "\'Z\\r\'", false),
                Arguments.of("Unclosed", "\'c", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testString(String test, String input, boolean success) {
        test(input, Token.Type.STRING, success);
    }

    private static Stream<Arguments> testString() {
        return Stream.of(
                // Example Matching Test Cases
                Arguments.of("Empty", "\"\"", true),
                Arguments.of("Alphabetic", "\"abc\"", true),
                Arguments.of("Newline Escape", "\"Hello,\\nWorld\"", true),
                // Matching Test Cases
                Arguments.of("Binary", "\"011001001\"", true),
                Arguments.of("Hex", "\"0xAc9\"", true),
                Arguments.of("Symbols", "\"!\"", true), //@#$%^&*
                Arguments.of("Hashtag", "\"#sasquatch\"", true),
                Arguments.of("Capital", "\"Nessie\"", true),
                Arguments.of("Curly Braces", "\"{}\"", true),
                Arguments.of("Commas", "\"1,000,000\"", true),
                Arguments.of("Happy", "\":D\"", true),
                Arguments.of("All LowerCase", "\"lowercase\"", true),
                Arguments.of("Multiple Words", "\"The Dead South\"", true),
                Arguments.of("Long String", "\"acetylmethylcarbinol\"", true),
                Arguments.of("Embeded Single Quotes", "\"Potatoes! Boil ’em, mash ’em, stick ’em in a stew.\"", true),
                Arguments.of("Special Characters", "\"âlemşümullük\"", true),
                // Example Non-mathcing Test Cases
                Arguments.of("Unterminated", "\"unterminated", false),
                Arguments.of("Invalid Escape", "\"invalid\\escape\"", false),
                // Non-matching Test Cases
                Arguments.of("Unquoted", "unquoted", false),
                Arguments.of("Single Quotes", "\'Single Quotes\'", false),
                Arguments.of("Invalid Escape 2", "\"<h1>Heading<\\h>\"", false),
                Arguments.of("Invalid Escape 3", "\"\\\\\\xyz\"", false),
                Arguments.of("Literal Newline", "\"Written \n on two lines\"", false),
                Arguments.of("Trailing Text", "\"My precious!\" — Gollum", false),
                Arguments.of("Weird quotes", "\'\"\'string\"\'\"", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOperator(String test, String input, boolean success) {
        //this test requires our lex() method, since that's where whitespace is handled.
        test(input, Arrays.asList(new Token(Token.Type.OPERATOR, input, 0)), success);
    }

    private static Stream<Arguments> testOperator() {
        return Stream.of(
                // Example Matching Test Cases
                Arguments.of("Character", "(", true),
                Arguments.of("Comparison", "<=", true),
                // Matching Test Cases
                Arguments.of("Plus", "+", true),
                Arguments.of("Minus", "-", true),
                Arguments.of("Equal", "==", true),
                Arguments.of("Multiply", "*", true),
                Arguments.of("Greater Than Equal", ">=", true),
                Arguments.of("Not Equal 1", "!=", true),
                Arguments.of("Parentheses O", "(", true),
                Arguments.of("Parentheses C", ")", true),
                // Example Non-matching Test Cases
                Arguments.of("Space", " ", false),
                Arguments.of("Tab", "\t", false),
                // Non-matching Test Cases
                Arguments.of("Not Equal", "<>", false),
                Arguments.of("Logical And", "&&", false),
                Arguments.of("Logical Or", "||", false),
                Arguments.of("Whitespace", "\\s", false),
                Arguments.of("Newline" , "\\n", false),
                Arguments.of("$", "$", false),
                Arguments.of("NE", "!===", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testExamples(String test, String input, List<Token> expected) {
        test(input, expected, true);
    }

    private static Stream<Arguments> testExamples() {
        return Stream.of(
                Arguments.of("Example 1", "LET x = 5;", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "LET", 0),
                        new Token(Token.Type.IDENTIFIER, "x", 4),
                        new Token(Token.Type.OPERATOR, "=", 6),
                        new Token(Token.Type.INTEGER, "5", 8),
                        new Token(Token.Type.OPERATOR, ";", 9)
                )),
                Arguments.of("Example 2", "print(\"Hello, World!\");", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "print", 0),
                        new Token(Token.Type.OPERATOR, "(", 5),
                        new Token(Token.Type.STRING, "\"Hello, World!\"", 6),
                        new Token(Token.Type.OPERATOR, ")", 21),
                        new Token(Token.Type.OPERATOR, ";", 22)
                ))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testFoo(String test, String input, List<Token> expected) {
        test(input, expected, true);
    }

    private static Stream<Arguments> testFoo() {
        return Stream.of(
                Arguments.of("Foo", "LET i = -1;\\n" +
                        "LET inc = 2;\\n" +
                        "DEF foo() DO\\n" +
                        "    WHILE i <= 1 DO\\n" +
                        "        IF i > 0 DO\\n" +
                        "            print(\"bar\");\\n" +
                        "        END\\n" +
                        "        i = i + inc;\\n" +
                        "    END\\n" +
                        "END\\n", Arrays.asList(
                        //LET i = -1;
                        new Token(Token.Type.IDENTIFIER, "LET", 0),
                        new Token(Token.Type.IDENTIFIER, "i", 4),
                        new Token(Token.Type.OPERATOR, "=", 6),
                        new Token(Token.Type.INTEGER, "-1", 8),
                        new Token(Token.Type.OPERATOR, ";", 10),
                        //LET inc = 2;
                        new Token(Token.Type.IDENTIFIER, "LET", 12),
                        new Token(Token.Type.IDENTIFIER, "inc", 16),
                        new Token(Token.Type.OPERATOR, "=", 20),
                        new Token(Token.Type.INTEGER, "2", 22),
                        new Token(Token.Type.OPERATOR, ";", 23),
                        //DEF foo() DO
                        new Token(Token.Type.IDENTIFIER, "DEF", 25),
                        new Token(Token.Type.IDENTIFIER, "foo", 29),
                        new Token(Token.Type.OPERATOR, "(", 32),
                        new Token(Token.Type.OPERATOR, ")", 33),
                        new Token(Token.Type.IDENTIFIER, "DO", 35),
                        //    WHILE i <= 1 DO
                        new Token(Token.Type.IDENTIFIER, "WHILE", 42),
                        new Token(Token.Type.IDENTIFIER, "i", 48),
                        new Token(Token.Type.OPERATOR, "<=", 50),
                        new Token(Token.Type.INTEGER, "1", 53),
                        new Token(Token.Type.IDENTIFIER, "DO", 55),

                        //        IF i > 0 DO
                        new Token(Token.Type.IDENTIFIER, "IF", 66),
                        new Token(Token.Type.IDENTIFIER, "i", 69),
                        new Token(Token.Type.OPERATOR, ">", 71),
                        new Token(Token.Type.INTEGER, "0", 73),
                        new Token(Token.Type.IDENTIFIER, "DO", 75),
                        //            print(\"bar\");
                        new Token(Token.Type.IDENTIFIER, "print", 90),
                        new Token(Token.Type.OPERATOR, "(", 95),
                        new Token(Token.Type.STRING, "\"bar\"", 96),
                        new Token(Token.Type.OPERATOR, ")", 101),
                        new Token(Token.Type.OPERATOR, ";", 102),
                        //        END
                        new Token(Token.Type.IDENTIFIER, "END", 112),
                        //        i = i + inc;
                        new Token(Token.Type.IDENTIFIER, "i", 124),
                        new Token(Token.Type.OPERATOR, "=", 126),
                        new Token(Token.Type.IDENTIFIER, "i", 128),
                        new Token(Token.Type.OPERATOR, "+", 130),
                        new Token(Token.Type.IDENTIFIER, "inc", 132),
                        new Token(Token.Type.OPERATOR, ";", 135),
                        //    END
                        new Token(Token.Type.IDENTIFIER, "END", 141),
                        //END
                        new Token(Token.Type.IDENTIFIER, "END", 145)
                ))

        );
    }

    @ParameterizedTest
    @MethodSource
    void testWhitespace(String test, String input, List<Token> expected) {
        test(input, expected, true);
    }

    private static Stream<Arguments> testWhitespace() {
        return Stream.of(
                Arguments.of("Test1", "one = two", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "one", 0),
                        new Token(Token.Type.OPERATOR, "=", 4),
                        new Token(Token.Type.IDENTIFIER, "two", 6)
                ))
        );
    }

    @Test
    void testException() {
        ParseException exception = Assertions.assertThrows(ParseException.class,
                () -> new Lexer("\"unterminated").lex());
        Assertions.assertEquals(13, exception.getIndex());
    }

    /**
     * Tests that lexing the input through {@link Lexer#lexToken()} produces a
     * single token with the expected type and literal matching the input.
     */
    private static void test(String input, Token.Type expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(new Token(expected, input, 0), new Lexer(input).lexToken());
            } else {
                Assertions.assertNotEquals(new Token(expected, input, 0), new Lexer(input).lexToken());
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
        Lexer.reset();
    }

    /**
     * Tests that lexing the input through {@link Lexer#lex()} matches the
     * expected token list.
     */
    private static void test(String input, List<Token> expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(expected, new Lexer(input).lex());
            } else {
                Assertions.assertNotEquals(expected, new Lexer(input).lex());
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
    }

}
