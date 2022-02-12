package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).

 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.

 * This type of parser is called recursive descent. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */

public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    //Parses the {@code source} rule.
    public Ast.Source parseSource() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException {
        Ast.Expr name = parseExpression();
        // name = value;
        if (peek("=") || peek(";")) {
            if (peek("=")) {
                match("=");
                Ast.Expr value = parseExpression();
                return new Ast.Stmt.Assignment(name, value);
            } else {
                // name()
                match(";");
                return new Ast.Stmt.Expression(name);
            }
        } else {
            if (tokens.has(0))
                throw new ParseException("Index: ", tokens.get(0).getIndex());
            else
                throw new ParseException("Index: ", parseExIndex());
        }
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        Ast.Expr left = parseEqualityExpression();

        if(tokens.tokens.size() <= 3){
            while (peek("AND")||peek("OR")) {
                String operator = tokens.get(0).getLiteral();
                match(Token.Type.IDENTIFIER);
                Ast.Expr right = parseLogicalExpression();
                if (!peek(operator)) return new Ast.Expr.Binary(operator, left, right);
                else left = new Ast.Expr.Binary(operator, left, right);
            }
            return left;
        }

        while (peek("AND") || peek("OR")) {
            left = expressionHelper(left);
        }
        return left;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        Ast.Expr left = parseAdditiveExpression();
        while (peek("<") ||
                peek("<=") ||
                peek(">") ||
                peek(">=") ||
                peek("==") ||
                peek("!=")) {
            left = expressionHelper(left);
        }
        return left;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        Ast.Expr left = parseMultiplicativeExpression();
        while (peek("+") || peek("-")) {
            left = expressionHelper(left);
        }
        return left;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        Ast.Expr left = parseSecondaryExpression();
        while (peek("*") || peek("/")) {
            left = expressionHelper(left);
        }
        return left;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        String name;
        List<Ast.Expr> arguments = new ArrayList<Ast.Expr>();
        Ast.Expr left = parsePrimaryExpression();
        if (peek(".")) {
            while (peek(".")) {
                match(".");
                if (peek(Token.Type.IDENTIFIER)) {
                    name = tokens.get(0).getLiteral();
                    match(Token.Type.IDENTIFIER);
                } else {
                    if (tokens.has(0)) throw new ParseException("Index: ", tokens.get(0).getIndex());
                    else throw new ParseException("Index: ", parseExIndex());
                }
                if (peek("(")) {
                    match("(");
                    while (!peek(")")) {
                        arguments.add(parseExpression());
                        if (peek(",")) match(",");
                    }
                    if (peek(")")) match(")");
                    if (!peek(")")) return new Ast.Expr.Function(Optional.of(left), name, arguments);
                    else left = new Ast.Expr.Function(Optional.of(left), name, arguments);
                } else {
                    if (!peek(")")) {
                        return new Ast.Expr.Access(Optional.of(left), name);
                    } else left = new Ast.Expr.Access(Optional.of(left), name);
                }
            }
        } else return left;
        return null;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        List<Ast.Expr> arguments = new ArrayList<Ast.Expr>();
        // nil
        if (match("NIL"))
            return new Ast.Expr.Literal(null);
            // true
        else if (match("TRUE"))
            return new Ast.Expr.Literal(true);
            // false
        else if (match("FALSE"))
            return new Ast.Expr.Literal(false);
            // integer
        else if (match(Token.Type.INTEGER))
            return new Ast.Expr.Literal(new BigInteger(tokens.get(-1).getLiteral()));
            // decimal
        else if (match(Token.Type.DECIMAL))
            return new Ast.Expr.Literal(new BigDecimal(tokens.get(-1).getLiteral()));
            // character
        else if (match(Token.Type.CHARACTER)) {
            // without escapes
            if (tokens.get(-1).getLiteral().length() <= 3) {
                return new Ast.Expr.Literal(tokens.get(-1).getLiteral().charAt(1));
            }
            // with escape
            else {
                String line = escape(0);
                return new Ast.Expr.Literal(line.charAt(1));
            }
            // string
        } else if (match(Token.Type.STRING)) {
            String line = escape(-1);
            return new Ast.Expr.Literal(line.substring(1, line.length() - 1));
            //  '(' expression ')'
        } else if (peek("(")) {
            match("(");
            Ast.Expr.Group group = new Ast.Expr.Group(parseExpression());
            if (peek(")")) {
                match(")");
                return group;
            } else {
                if (tokens.has(0)) {
                    throw new ParseException("Index: ", tokens.get(0).getIndex());
                } else throw new ParseException("Index:", parseExIndex());
            }
            //  identifier ('(' (expression (',' expression)*)? ')')?
        } else if (match(Token.Type.IDENTIFIER)) {
            String name = tokens.get(-1).getLiteral();
            //match(Token.Type.IDENTIFIER);
            if (peek("(")) {
                match("(");
                while (!peek(")")) {
                    arguments.add(parseExpression());
                    if (peek(",")) {
                        match(",");
                        if (peek(")"))
                            throw new ParseException("Index", tokens.get(0).getIndex());
                    }
                }
                match(")");
                return new Ast.Expr.Function(Optional.empty(), name, arguments);
            } else {
                return new Ast.Expr.Access(Optional.empty(), name);
            }
        } else {
            if (tokens.has(0)) {
                throw new ParseException("Index: ", tokens.get(0).getIndex());
            }
            else{
                throw new ParseException("Index: ", parseExIndex());
            }
        }
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     * <p>
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i)) return false;
            else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType()) return false;
            } else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) return false;
            } else throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++)
                tokens.advance();
        }
        return peek;
    }

    private static final class TokenStream {
        private final List<Token> tokens;
        private int index = 0;

        public int showIndex (){
            return index;
        }

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }
    }

    // Helper functions
    private int parseExIndex() {
        return tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length();
    }

    private Ast.Expr expressionHelper(Ast.Expr left) {
        String operator = tokens.get(0).getLiteral();
        match(Token.Type.OPERATOR);
        Ast.Expr right = parseLogicalExpression();
        if (!peek(operator)) return new Ast.Expr.Binary(operator, left, right);
        else left = new Ast.Expr.Binary(operator, left, right);
        return left;
    }

    private String escape(int offset) {
        String value = tokens.get(offset).getLiteral();
        value = value.replace("\\b", "\b");
        value = value.replace("\\n", "\n");
        value = value.replace("\\r", "\r");
        value = value.replace("\\t", "\t");
        value = value.replace("\\\"", "\"");
        value = value.replace("\\\\", "\\");
        value = value.replace("\\\'", "\'");
        return value;
    }
}