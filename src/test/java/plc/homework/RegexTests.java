package plc.homework;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. A framework of the test structure
 * is provided, you will fill in the remaining pieces.
 * <p>
 * To run tests, either click the run icon on the left margin, which can be used
 * to run all tests or only a specific test. You should make sure your tests are
 * run through IntelliJ (File > Settings > Build, Execution, Deployment > Build
 * Tools > Gradle > Run tests using <em>IntelliJ IDEA</em>). This ensures the
 * name and inputs for the tests are displayed correctly in the run window.
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     * <p>
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests (see above note if not working).
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above.
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),
                // Added test cases
                Arguments.of("Underscore", "mohammad_islam@ufl.edu", true),
                Arguments.of("Missing @", "mohammad.islamufl.edu", false),
                Arguments.of("Period in User Name", "mohammad.islam@ufl.edu", true),
                Arguments.of("Numeric in Middle", "mohammad123islam@gmail.com", true),
                Arguments.of("Numeric Beginning", "123Mohammad.islam@gmail.com", true),
                Arguments.of("Empty", " ", false),
                Arguments.of("Wrong Domain", "mohammad.islam@gmail.comm", false),
                Arguments.of("Missing Domain", "mohammad.islam@gmaail.", false),
                Arguments.of("Two Char Domain", "mohammad.islam@gmail.io", true),
                Arguments.of("Extra Periods", "mohammad.islam.saiful@gmail.com", true),
                Arguments.of("Space in Between User Name", "mohammad islam@ufl.edu", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                //what has ten letters and starts with gas?
                Arguments.of("10 Characters", "automobile", true),
                Arguments.of("14 Characters", "i<3pancakes10!", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("13 Characters", "i<3pancakes9!", false),
                // Added test cases
                Arguments.of("Empty String", " ", false),
                Arguments.of("single Char", "a", false),
                Arguments.of("21 Char", "dsajkflasfjljflFJ48@#", false),
                Arguments.of("22 Char", "dsajkflasfjljflFJ48@#G", false),
                Arguments.of("9 Char", "automobil", false),
                Arguments.of("12 Char", "abcde12!@$%*", true),
                Arguments.of("16 Char", "abcde12!@$%*$^()", true),
                Arguments.of("20 Char", "()ABCDEFG12!@$%*$^()", true)

        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                // Added test cases
                Arguments.of("Comma Tailing", "[1,2,3,]", false),
                Arguments.of("Not Digits", "[a, B, 12]", false),
                Arguments.of("Missing Bracket [", "1,2,3]", false),
                Arguments.of("Missing Bracket ]", "[1,2,3", false),
                Arguments.of("Extra comma", "[1,,2,3", false),
                Arguments.of("Symbol", "[%, *, 2", false),
                Arguments.of("Decimal Numbers", "[3.2, 5.7, 0.5]", false),
                Arguments.of("Empty", "[]", true),
                Arguments.of("Extra Space", "[11, 2, 388    ]", true),
                Arguments.of("Big Numbers", "[100000,20000000,300000000,4000000000]", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        test(input, Regex.NUMBER, success);
    }
    public static Stream<Arguments> testNumberRegex() {
        return Stream.of(

                Arguments.of("Missing Left 0", ".123", false),
                Arguments.of("Missing Right 0", "123.", false),
                Arguments.of("Alphabet in Digit", "10a", false),
                Arguments.of("Symbol in Digit", "10#", false),
                Arguments.of("Alphabet Numeric", "Ten", false),
                Arguments.of("Empty Input", " ", false),
                Arguments.of("Single Digit", "1", true),
                Arguments.of("Positive Integer w/o +", "89246636", true),
                Arguments.of("Negative Integer", "-89246636", true),
                Arguments.of("Positive Integer w/ +", "+89246636", true),
                Arguments.of("Positive decimal w/o +", "0.123", true),
                Arguments.of("Positive decimal w/ +", "+0.123", true),
                Arguments.of("Negative decimal", "-0.123", true),
                Arguments.of("Integer Leading Zeros", "00000011111", true),
                Arguments.of("Decimal Leading Zeros", "000000.11111", true),
                Arguments.of("Decimal Leading Zeros", "11111.0000000000", true)

        );
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testStringRegex() {
        return Stream.of(
                Arguments.of("Correct String", "\" Hello! World\"", true),
                Arguments.of("Incorrect String", "Hello! World", false),
                Arguments.of("Missing Left Escape,", "Hello! World\"", false),
                Arguments.of("Missing Right Escape,", "\"Hello! World", false),
                Arguments.of("Extra backslash,", "\"Hello! \\ World", false),
                Arguments.of("Mixed char,", "\"He%%##! \" 1218W55rld\"", true),
                Arguments.of("New Line,", "\"He%%##!\n1218W55rld\"", true),
                Arguments.of("Empty String,", "\"\"", true),
                Arguments.of("With b", "\"Mount \\b ain\"", true),
                Arguments.of("With n", "\"Mount \\n ain\"", true),
                Arguments.of("With r", "\"Mount \\r ain\"", true),
                Arguments.of("With t", "\"Mount \\t ain\"", true),
                Arguments.of("With random Char", "\"Mount \\s ain\"", false),
                Arguments.of("With random number", "\"Mount \\12 ain\"", false)
        );
    }

    /**
     * Asserts that the input matches the given pattern. This method doesn't do
     * much now, but you will see this concept in future assignments.
     */
    private static void test(String input, Pattern pattern, boolean success) {
        Assertions.assertEquals(success, pattern.matcher(input).matches());
    }

}
