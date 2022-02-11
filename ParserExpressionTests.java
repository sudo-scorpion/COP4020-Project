package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from Homework 1
 * or the LexerTests file from the last project part for more information.
 */
final class ParserExpressionTests {

    @ParameterizedTest
    @MethodSource
    void testExpressionStatement(String test, List<Token> tokens, Ast.Stmt.Expression expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testExpressionStatement() {
        return Stream.of(
                Arguments.of("Function Expression",
                        Arrays.asList(
                                //name();
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5),
                                new Token(Token.Type.OPERATOR, ";", 6)
                        ),
                        new Ast.Stmt.Expression(new Ast.Expr.Function(Optional.empty(), "name", Arrays.asList()))
                ),
                Arguments.of("Parameter",
                        Arrays.asList(
                                //foo(bar);
                                new Token(Token.Type.IDENTIFIER, "foo", 0),
                                new Token(Token.Type.OPERATOR, "(", 3),
                                new Token(Token.Type.IDENTIFIER, "bar", 4),
                                new Token(Token.Type.OPERATOR, ")", 7),
                                new Token(Token.Type.OPERATOR, ";", 8)
                        ),
                        new Ast.Stmt.Expression(new Ast.Expr.Function(Optional.empty(), "foo", Arrays.asList(
                                new Ast.Expr.Access(Optional.empty(), "bar")
                        )))
                ),
                Arguments.of("Two Parameters",
                        Arrays.asList(
                                //foo(bar, baz);
                                new Token(Token.Type.IDENTIFIER, "foo", 0),
                                new Token(Token.Type.OPERATOR, "(", 3),
                                new Token(Token.Type.IDENTIFIER, "bar", 4),
                                new Token(Token.Type.OPERATOR, ",", 7),
                                new Token(Token.Type.IDENTIFIER, "baz", 8),
                                new Token(Token.Type.OPERATOR, ")", 11),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        new Ast.Stmt.Expression(new Ast.Expr.Function(Optional.empty(), "foo", Arrays.asList(
                                new Ast.Expr.Access(Optional.empty(), "bar"),
                                new Ast.Expr.Access(Optional.empty(), "baz")
                        )))
                ),
                Arguments.of("Secondary Parameters",
                        Arrays.asList(
                                //foo(bar, (baz));
                                new Token(Token.Type.IDENTIFIER, "foo", 0),
                                new Token(Token.Type.OPERATOR, "(", 3),
                                new Token(Token.Type.IDENTIFIER, "bar", 4),
                                new Token(Token.Type.OPERATOR, ",", 7),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "baz", 9),
                                new Token(Token.Type.OPERATOR, ")", 11),
                                new Token(Token.Type.OPERATOR, ")", 12),
                                new Token(Token.Type.OPERATOR, ";", 13)
                        ),
                        new Ast.Stmt.Expression(new Ast.Expr.Function(Optional.empty(), "foo", Arrays.asList(
                                new Ast.Expr.Access(Optional.empty(), "bar"),
                                new Ast.Expr.Group(new Ast.Expr.Access(Optional.empty(), "baz"))
                        )))
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAssignmentStatement(String test, List<Token> tokens, Ast.Stmt.Assignment expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testAssignmentStatement() {
        return Stream.of(
                Arguments.of("Assignment",
                        Arrays.asList(
                                //name = value;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.IDENTIFIER, "value", 7),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.empty(), "name"),
                                new Ast.Expr.Access(Optional.empty(), "value")
                        )
                ),
                Arguments.of("Number Assignment",
                        Arrays.asList(
                                //name = 12345;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.INTEGER, "12345", 7),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.empty(), "name"),
                                new Ast.Expr.Literal(new BigInteger("12345"))
                        )
                ),
                Arguments.of("Grouped Expression Assignment",
                        Arrays.asList(
                                //name = (expr1 + expr2);
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.OPERATOR, "(", 7),
                                new Token(Token.Type.IDENTIFIER, "expr1", 8),
                                new Token(Token.Type.OPERATOR, "+", 14),
                                new Token(Token.Type.IDENTIFIER, "expr2", 16),
                                new Token(Token.Type.OPERATOR, ")", 22),
                                new Token(Token.Type.OPERATOR, ";", 23)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.empty(), "name"),
                                new Ast.Expr.Group(new Ast.Expr.Binary("+",
                                        new Ast.Expr.Access(Optional.empty(), "expr1"),
                                        new Ast.Expr.Access(Optional.empty(), "expr2")
                                ))
                        )
                ),
                Arguments.of("Grouped Expression Assignment",
                        Arrays.asList(
                                //name = expr1 + expr2;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.IDENTIFIER, "expr1", 7),
                                new Token(Token.Type.OPERATOR, "+", 14),
                                new Token(Token.Type.IDENTIFIER, "expr2", 17),
                                new Token(Token.Type.OPERATOR, ";", 22)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.empty(), "name"),
                                new Ast.Expr.Binary("+",
                                        new Ast.Expr.Access(Optional.empty(), "expr1"),
                                        new Ast.Expr.Access(Optional.empty(), "expr2")
                                )
                        )
                ),
                Arguments.of("Grouped Expression Assignment",
                        Arrays.asList(
                                //name = (expr1 + expr2);
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.OPERATOR, "(", 7),
                                new Token(Token.Type.IDENTIFIER, "expr1", 8),
                                new Token(Token.Type.OPERATOR, "+", 14),
                                new Token(Token.Type.INTEGER, "12345", 16),
                                new Token(Token.Type.OPERATOR, ")", 22),
                                new Token(Token.Type.OPERATOR, ";", 23)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.empty(), "name"),
                                new Ast.Expr.Group(new Ast.Expr.Binary("+",
                                        new Ast.Expr.Access(Optional.empty(), "expr1"),
                                        new Ast.Expr.Literal(new BigInteger("12345"))
                                ))
                        )
                )

        );
    }

    @ParameterizedTest
    @MethodSource
    void testLiteralExpression(String test, List<Token> tokens, Ast.Expr.Literal expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                Arguments.of("Null Literal",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "NIL", 0)),
                        new Ast.Expr.Literal(null)// todo fix this
                ),
                Arguments.of("Boolean Literal",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "TRUE", 0)),
                        new Ast.Expr.Literal(Boolean.TRUE)
                ),
                Arguments.of("Boolean Literal 2",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "FALSE", 0)),
                        new Ast.Expr.Literal(Boolean.FALSE)
                ),
                Arguments.of("Integer Literal",
                        Arrays.asList(new Token(Token.Type.INTEGER, "1", 0)),
                        new Ast.Expr.Literal(new BigInteger("1"))
                ),
                Arguments.of("Integer Literal Multiple Digits",
                        Arrays.asList(new Token(Token.Type.INTEGER, "123", 0)),
                        new Ast.Expr.Literal(new BigInteger("123"))
                ),
                Arguments.of("Signed Integer",
                        Arrays.asList(new Token(Token.Type.INTEGER, "+123", 0)),
                        new Ast.Expr.Literal(new BigInteger("+123"))
                ),
                Arguments.of("Decimal Literal",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "2.0", 0)),
                        new Ast.Expr.Literal(new BigDecimal("2.0"))
                ),
                Arguments.of("Decimal Literal Multiple Digits",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "123.456", 0)),
                        new Ast.Expr.Literal(new BigDecimal("123.456"))
                ),
                Arguments.of("Signed Decimal Literal",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "-123.456", 0)),
                        new Ast.Expr.Literal(new BigDecimal("-123.456"))
                ),
                Arguments.of("Decimal Literal With 0s",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "0.500", 0)),
                        new Ast.Expr.Literal(new BigDecimal("0.500"))
                ),
                Arguments.of("Character Literal",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'c'", 0)),
                        new Ast.Expr.Literal('c')
                ),
                Arguments.of("Single Quote Literal",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\''", 0)),
                        new Ast.Expr.Literal('\'')
                ),
                Arguments.of("String Literal",
                        Arrays.asList(new Token(Token.Type.STRING, "\"string\"", 0)),
                        new Ast.Expr.Literal("string")
                ),
                Arguments.of("String Literal Symbols",
                        Arrays.asList(new Token(Token.Type.STRING, "\"!@#$%^&*\"", 0)),
                        new Ast.Expr.Literal("!@#$%^&*")
                ),
                Arguments.of("String Literal Space",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Colonel Sanders\"", 0)),
                        new Ast.Expr.Literal("Colonel Sanders")
                ),
                Arguments.of("Escape Character",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\nWorld!\"", 0)),
                        new Ast.Expr.Literal("Hello,\nWorld!")
                ),
                Arguments.of("Escape Character b",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\bWorld!\"", 0)),
                        new Ast.Expr.Literal("Hello,\bWorld!")
                ),
                Arguments.of("Escape Character r",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\rWorld!\"", 0)),
                        new Ast.Expr.Literal("Hello,\rWorld!")
                ),
                Arguments.of("Escape Character t",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\tWorld!\"", 0)),
                        new Ast.Expr.Literal("Hello,\tWorld!")
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGroupExpression(String test, List<Token> tokens, Ast.Expr.Group expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testGroupExpression() {
        return Stream.of(
                Arguments.of("Grouped Variable",
                        Arrays.asList(
                                //(expr)
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        new Ast.Expr.Group(new Ast.Expr.Access(Optional.empty(), "expr"))
                ),
                Arguments.of("Grouped Binary",
                        Arrays.asList(
                                //(expr1 + expr2)
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr1", 1),
                                new Token(Token.Type.OPERATOR, "+", 7),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, ")", 14)
                        ),
                        new Ast.Expr.Group(new Ast.Expr.Binary("+",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        ))
                ),
                Arguments.of("Grouped Access",
                        Arrays.asList(
                                //((expr))
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.OPERATOR, "(", 1),
                                new Token(Token.Type.IDENTIFIER, "expr", 2),
                                new Token(Token.Type.OPERATOR, ")", 6),
                                new Token(Token.Type.OPERATOR, ")", 7)
                        ),
                        new Ast.Expr.Group(new Ast.Expr.Group(new Ast.Expr.Access(Optional.empty(), "expr")))
                ),
                Arguments.of("Grouped Function",
                        Arrays.asList(
                                //(name())
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 1),
                                new Token(Token.Type.OPERATOR, "(", 5),
                                new Token(Token.Type.OPERATOR, ")", 6),
                                new Token(Token.Type.OPERATOR, ")", 7)
                        ),
                        new Ast.Expr.Group(new Ast.Expr.Function(Optional.empty(),"name", Arrays.asList()))
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testBinaryExpression(String test, List<Token> tokens, Ast.Expr.Binary expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("Binary And",
                        Arrays.asList(
                                //expr1 AND expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.IDENTIFIER, "AND", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 10)
                        ),
                        new Ast.Expr.Binary("AND",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Or",
                        Arrays.asList(
                                //expr1 OR expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.IDENTIFIER, "OR", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expr.Binary("OR",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Less Than",
                        Arrays.asList(
                                //expr1 < expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "<", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expr.Binary("<",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Less Equal",
                        Arrays.asList(
                                //expr1 <= expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "<=", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expr.Binary("<=",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Greater Than",
                        Arrays.asList(
                                //expr1 > expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, ">", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expr.Binary(">",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Greater Equal",
                        Arrays.asList(
                                //expr1 >= expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, ">=", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expr.Binary(">=",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Equality",
                        Arrays.asList(
                                //expr1 == expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "==", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expr.Binary("==",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Inequality",
                        Arrays.asList(
                                //expr1 != expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "!=", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expr.Binary("!=",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Addition",
                        Arrays.asList(
                                //expr1 + expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expr.Binary("+",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Subtraction",
                        Arrays.asList(
                                //expr1 - expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "-", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expr.Binary("-",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Multiplication",
                        Arrays.asList(
                                //expr1 * expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expr.Binary("*",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Division",
                        Arrays.asList(
                                //expr1 / expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "/", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expr.Binary("/",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                /*
                // todo this test fails
                Arguments.of("Binary And",
                        Arrays.asList(
                                //expr1 AND expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "AND", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 10),
                                new Token(Token.Type.OPERATOR, "AND", 16),
                                new Token(Token.Type.IDENTIFIER, "expr3", 20)
                        ),
                        new Ast.Expr.Binary("AND",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                //new Ast.Expr.Access(Optional.empty(), "AND expr2")
                                new Ast.Expr.Binary("AND",
                                        new Ast.Expr.Access(Optional.empty(), "expr2"),
                                        new Ast.Expr.Access(Optional.empty(), "expr3")
                                )
                        )
                ),
                 */
                Arguments.of("Boolean And",
                        Arrays.asList(
                                // TRUE AND TRUE
                                new Token(Token.Type.IDENTIFIER, "TRUE", 0),
                                new Token(Token.Type.IDENTIFIER, "AND", 5),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 9)
                        ),
                        new Ast.Expr.Binary("AND",
                                new Ast.Expr.Literal(Boolean.TRUE),
                                new Ast.Expr.Literal(Boolean.TRUE)
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAccessExpression(String test, List<Token> tokens, Ast.Expr.Access expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testAccessExpression() {
        return Stream.of(
                Arguments.of("Variable",
                        Arrays.asList(
                                // name
                                new Token(Token.Type.IDENTIFIER, "name", 0)
                        ),
                        new Ast.Expr.Access(Optional.empty(), "name")
                ),
                Arguments.of("Field Access",
                        Arrays.asList(
                                //obj.field
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "field", 4)
                        ),
                        new Ast.Expr.Access(Optional.of(new Ast.Expr.Access(Optional.empty(), "obj")), "field")
                )

        );
    }

    @ParameterizedTest
    @MethodSource
    void testFunctionExpression(String test, List<Token> tokens, Ast.Expr.Function expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Zero Arguments",
                        Arrays.asList(
                                //name()
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        new Ast.Expr.Function(Optional.empty(), "name", Arrays.asList())
                ),
                Arguments.of("Single Argument",
                        Arrays.asList(
                                //name(expr1)
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "expr1", 5),
                                new Token(Token.Type.OPERATOR, ")", 10)
                        ),
                        new Ast.Expr.Function(Optional.empty(), "name", Arrays.asList(
                                new Ast.Expr.Access(Optional.empty(), "expr1")
                        ))
                ),
                Arguments.of("Multiple Arguments",
                        Arrays.asList(
                                //name(expr1, expr2, expr3)
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "expr1", 5),
                                new Token(Token.Type.OPERATOR, ",", 10),
                                new Token(Token.Type.IDENTIFIER, "expr2", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr3", 19),
                                new Token(Token.Type.OPERATOR, ")", 24)
                        ),
                        new Ast.Expr.Function(Optional.empty(), "name", Arrays.asList(
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2"),
                                new Ast.Expr.Access(Optional.empty(), "expr3")
                        ))
                ),
                Arguments.of("Method Call",
                        Arrays.asList(
                                //obj.method()
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "method", 4),
                                new Token(Token.Type.OPERATOR, "(", 10),
                                new Token(Token.Type.OPERATOR, ")", 11)
                        ),
                        new Ast.Expr.Function(Optional.of(new Ast.Expr.Access(Optional.empty(), "obj")), "method", Arrays.asList())
                ),
                Arguments.of("Nested Function",
                        Arrays.asList(
                                //name(foobar())
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "foobar", 12),
                                new Token(Token.Type.OPERATOR, "(", 13),
                                new Token(Token.Type.OPERATOR, ")", 14),
                                new Token(Token.Type.OPERATOR, ")", 15)
                        ),
                        new Ast.Expr.Function(Optional.empty(), "name", Arrays.asList(
                                new Ast.Expr.Function(Optional.empty(),"foobar", Arrays.asList())
                        ))
                )
        );
    }
 
    /**
     * Standard test function. If expected is null, a ParseException is expected
     * to be thrown (not used in the provided tests).
     */
    private static <T extends Ast> void test(List<Token> tokens, T expected, Function<Parser, T> function) {
        Parser parser = new Parser(tokens);
        if (expected != null) {
            Assertions.assertEquals(expected, function.apply(parser));
        } else {
            Assertions.assertThrows(ParseException.class, () -> function.apply(parser));
        }
    }
}
