package plc.project;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 * <p>
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and
 * {@link #match(Object...)} are helpers to make the implementation easier.
 * <p>
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        try {
            List<Ast.Field> field = new ArrayList<Ast.Field>();
            List<Ast.Method> method = new ArrayList<Ast.Method>();

            if (tokens.has(0) && peek(Token.Type.IDENTIFIER)) {
                while (peek(Token.Type.IDENTIFIER)) {

                    if (peek("LET")) {
                        while (peek("LET")) {
                            field.add(parseField());
                            if (tokens.has(0) && (!peek("LET") && !peek("DEF")))
                                throw new ParseException("Expected DEF", tokens.get(0).getIndex());
                        }
                    }

                    if (peek("DEF")) {
                        while (peek("DEF")) {
                            method.add(parseMethod());
                            if (tokens.has(0) && !peek("DEF"))
                                throw new ParseException("Expected DEF", tokens.get(0).getIndex());
                        }
                    }
                }
            }

            if (!tokens.has(0))
                return new Ast.Source(field, method);
            else
                throw new ParseException("Expected IDENTIFIER", tokens.get(0).getIndex());
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the next
     * tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        try {
            match("LET");
            String name = "";
            String typeName = "";

            if (peek(Token.Type.IDENTIFIER)) {
                name = tokens.get(0).getLiteral();
                match(Token.Type.IDENTIFIER);
            } else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Expected IDENTIFIER", index);
            }

            if (peek(":")) {
                match(":");
            } else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Missing colon", index);
            }

            if (peek(Token.Type.IDENTIFIER)) {
                typeName = tokens.get(0).getLiteral();
                match(Token.Type.IDENTIFIER);
            } else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Expected TYPE", index);
            }

            if (peek("=")) {
                match("=");
                Ast.Expr value = parseExpression();
                if (peek(";")) {
                    match(";");
                    return new Ast.Field(name, typeName, Optional.of(value));
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("No semicolon", index);
                }
            } else {
                if (peek(";")) {
                    match(";");
                    return new Ast.Field(name, typeName, Optional.empty());
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("No semicolon", index);
                }
            }
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the next
     * tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        try {
            match("DEF");

            List<String> parameter = new ArrayList<String>();
            List<Ast.Stmt> statement = new ArrayList<Ast.Stmt>();

            String funcName = "";
            String returnTypeName = "";
            List<String> parameterTypeNames = new ArrayList<String>();

            // Get identifier
            if (peek(Token.Type.IDENTIFIER)) {
                funcName = tokens.get(0).getLiteral();
                match(Token.Type.IDENTIFIER);
            } else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("No IDENTIFIER", index);
            }

            if (peek("("))
                match("(");
            else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("No PARENTHESIS", index);
            }

            while (peek(Token.Type.IDENTIFIER)) {
                // Need to catch NON-IDENTIFIERS in here
                parameter.add(tokens.get(0).getLiteral());
                match(Token.Type.IDENTIFIER);

                if (peek(":")) {
                    match(":");
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Expected colon", index);
                }

                if (peek(Token.Type.IDENTIFIER)) {
                    parameterTypeNames.add(tokens.get(0).getLiteral());
                    match(Token.Type.IDENTIFIER);
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Expected TYPE", index);
                }

                if (peek(",")) {
                    match(",");
                    if (peek(")"))
                        throw new ParseException("Unexpected comma", tokens.get(0).getIndex());
                } else {
                    if (!peek(")")) {
                        int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                        throw new ParseException("Missing comma before parenthesis", index);
                    }
                }
            }

            if (peek(")"))
                match(")");
            else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("No closing parenthesis", index);
            }

            if (peek(":")) {
                match(":");

                if (peek(Token.Type.IDENTIFIER)) {
                    returnTypeName = tokens.get(0).getLiteral();
                    match(Token.Type.IDENTIFIER);
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Expected TYPE", index);
                }
            }

            if (peek("DO"))
                match("DO");
            else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Expected DO", index);
            }


            // Need some way to check that END is actually there
/*
        int i = tokens.showIndex();
        boolean invalid = true;
        while (i < tokens.tokens.size()){
            if(tokens.tokens.get(i).toString().equals("END")){
                invalid = false;
                break;
            }
            i++;
        }

        if (invalid){
            int index = (tokens.has(0))? tokens.get(0).getIndex(): indexHelper(false);
            throw new ParseException("Expected END", index);
        }// end
*/

            while (!peek("END")) {
                statement.add(parseStatement());
            }

            if (peek("END")) {
                match("END");
                if (returnTypeName.equals("")) {
                    return new Ast.Method(funcName, parameter, parameterTypeNames, Optional.empty(), statement);
                } else {
                    return new Ast.Method(funcName, parameter, parameterTypeNames, Optional.of(returnTypeName), statement);
                }
            } else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Expected END", index);
            }
            // Got rid of the helper function since the compiler does not like it
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method. If
     * the next tokens do not start a declaration, if, while, or return statement,
     * then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException {
        try {
            if (peek("LET"))
                return parseDeclarationStatement();
            else if (peek("IF"))
                return parseIfStatement();
            else if (peek("FOR"))
                return parseForStatement();
            else if (peek("WHILE"))
                return parseWhileStatement();
            else if (peek("RETURN"))
                return parseReturnStatement();
            else {
                Ast.Stmt.Expr left = parseExpression();
                if (!match("=")) {
                    if (!match(";")) {
                        throw new ParseException("No semicolon", tokens.get(-1).getIndex());
                    }
                    return new Ast.Stmt.Expression(left);
                }

                Ast.Stmt.Expr value = parseExpression();

                if (!match(";")) {
                    throw new ParseException("No semicolon", tokens.get(-1).getIndex());
                }
                return new Ast.Stmt.Assignment(left, value);
            }

        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a declaration statement, aka
     * {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        try {
            match("LET");
            String identifier = "";
            String typeName = "";

            if (peek(Token.Type.IDENTIFIER)) {
                identifier = tokens.get(0).getLiteral();
                match(Token.Type.IDENTIFIER);
            } else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Expected IDENTIFIER", index);
            }

            if (peek(":")) {
                match(":");

                if (peek(Token.Type.IDENTIFIER)) {
                    typeName = tokens.get(0).getLiteral();
                    match(Token.Type.IDENTIFIER);
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Expected TYPE", index);
                }
            }

            if (peek("=")) {
                match("=");
                Ast.Expr value = parseExpression();
                if (peek(";")) {
                    match(";");
                    if (typeName.equals(""))
                        return new Ast.Stmt.Declaration(identifier, Optional.empty(), Optional.of(value));
                    else
                        return new Ast.Stmt.Declaration(identifier, Optional.of(typeName), Optional.of(value));
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Missing semicolon", index);
                }
            } else {
                if (peek(";")) {
                    match(";");
                    if (typeName.equals(""))
                        return new Ast.Stmt.Declaration(identifier, Optional.empty(), Optional.empty());
                    else
                        return new Ast.Stmt.Declaration(identifier, Optional.of(typeName), Optional.empty());
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Missing semicolon", index);
                }
            }
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method should
     * only be called if the next tokens start an if statement, aka {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        try {
            List<Ast.Stmt> DO = new ArrayList<>(); // then
            List<Ast.Stmt> ELSE = new ArrayList<>();// else

            match("IF");

            Ast.Expr expression = parseExpression();

            if (peek("DO")) {
                match("DO");

                while (!peek("ELSE") && !peek("END"))
                    DO.add(parseStatement());

                if (peek("ELSE")) {
                    match("ELSE");

                    while (!peek("END"))
                        ELSE.add(parseStatement());
                }
                if (peek("END")) {
                    match("END");
                    return new Ast.Stmt.If(expression, DO, ELSE);
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Expected END", index);
                }
            } else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Expected DO", index);
            }

        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method should
     * only be called if the next tokens start a for statement, aka {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        try {
            String identifier = ""; // was null
            List<Ast.Stmt> statements = new ArrayList<Ast.Stmt>();
            if (match("FOR")) {

                // identifier
                if (peek(Token.Type.IDENTIFIER)) {
                    identifier = tokens.get(0).getLiteral();
                    match(Token.Type.IDENTIFIER);
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Expected IDENTIFIER", index);
                }

                // IN
                if (peek("IN"))
                    match("IN");
                else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Expected IN", index);
                }
                // DO
                Ast.Expr expression = parseExpression();
                if (peek("DO"))
                    match("DO");
                else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Expected DO", index);
                }

                while (!peek("END")) {
                    statements.add(parseStatement());
                }

                if (peek("END")) {
                    match("END");
                    return new Ast.Stmt.For(identifier, expression, statements);
                } else {
                    int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                    throw new ParseException("Expected END", index);
                }
            } else {
                throw new ParseException("No For", (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false));
            }

        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method should
     * only be called if the next tokens start a while statement, aka {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        try {
            match("WHILE");

            Ast.Expr expression = parseExpression();

            if (peek("DO"))
                match("DO");
            else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Expected DO", index);
            }

            List<Ast.Stmt> statements = new ArrayList<Ast.Stmt>();

            // Again, need a way to check that END is actually in the tokens list

            while (!match("END") && tokens.has(0)) {
                statements.add(parseStatement());
            }
            if (!tokens.get(-1).getLiteral().equals("END")) {
                throw new ParseException("Expected END", tokens.get(-1).getIndex());
            }
            return new Ast.Stmt.While(expression, statements);

        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method should
     * only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        try {
            match("RETURN");

            if (tokens.get(0).getLiteral().equals(";")) {
                // Added this, my thoughts are that if the current token is a semicolon, then
                // there cannot be a return value, which is an exception
                throw new ParseException("Missing value", tokens.get(0).getIndex());
            }

            Ast.Expr expression = parseExpression();

            if (peek(";")) {
                match(";");
                return new Ast.Stmt.Return(expression);
            } else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Missing Semicolon", index);
            }
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        try {
            return parseLogicalExpression();
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        Ast.Expr left = parseEqualityExpression();
        try {
            while (peek("AND") || peek("OR")) {
                String operator = tokens.get(0).getLiteral();

                match(Token.Type.IDENTIFIER);

                Ast.Expr right = parseEqualityExpression();

                if (!peek("AND") && !peek("OR"))
                    return new Ast.Expr.Binary(operator, left, right);
                else
                    left = new Ast.Expr.Binary(operator, left, right);
            }
            return left;
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        try {
            Ast.Expr left = parseAdditiveExpression();

            // Break these peeks into a modular format later
            while (peek("<") ||
                    peek("<=") ||
                    peek(">") ||
                    peek(">=") ||
                    peek("==") ||
                    peek("!=")) {
                String operator = tokens.get(0).getLiteral();
                match(Token.Type.OPERATOR);
                Ast.Expr right = parseAdditiveExpression();

                if (!peek("<") ||
                        !peek("<=") ||
                        !peek(">") ||
                        !peek(">=") ||
                        !peek("==") ||
                        !peek("!="))
                    return new Ast.Expr.Binary(operator, left, right);
                else
                    left = new Ast.Expr.Binary(operator, left, right);
            }
            return left;
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        try {
            Ast.Expr left = parseMultiplicativeExpression();
            while (peek("+") || peek("-")) {
                String operator = tokens.get(0).getLiteral();
                match(Token.Type.OPERATOR);
                Ast.Expr right = parseAdditiveExpression();

                if (!peek("+") && !peek("-"))
                    return new Ast.Expr.Binary(operator, left, right);
                else
                    left = new Ast.Expr.Binary(operator, left, right);
            }
            return left;
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        try {
            Ast.Expr left = parseSecondaryExpression();
            while (peek("*") || peek("/")) {
                String operator = tokens.get(0).getLiteral();
                match(Token.Type.OPERATOR);
                Ast.Expr right = parseAdditiveExpression();

                if (!peek("*") && !peek("/"))
                    return new Ast.Expr.Binary(operator, left, right);
                else
                    left = new Ast.Expr.Binary(operator, left, right);
            }

            return left;
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        try {
            String name = ""; // was null
            List<Ast.Expr> arguments = new ArrayList<Ast.Expr>();
            Ast.Expr left = parsePrimaryExpression();

            if (!peek(".")) {
                return left;
            } else {
                while (peek(".")) {
                    match(".");
                    if (peek(Token.Type.IDENTIFIER)) {
                        name = tokens.get(0).getLiteral();
                        match(Token.Type.IDENTIFIER);
                    } else {
                        int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                        throw new ParseException("Invalid IDENTIFIER", index);
                    }

                    if (peek("(")) {
                        match("(");

                        while (!peek(")")) {
                            arguments.add(parseExpression());
                            if (peek(","))
                                match(",");
                        }
                        match(")");
                        if (!peek("."))
                            return new Ast.Expr.Function(Optional.of(left), name, arguments);
                        else
                            left = new Ast.Expr.Function(Optional.of(left), name, arguments);
                    } else {
                        if (!peek(".")) {
                            return new Ast.Expr.Access(Optional.of(left), name);
                        } else
                            left = new Ast.Expr.Access(Optional.of(left), name);
                    }
                }
            }
            return null;
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule for
     * expressions and includes literal values, grouping, variables, and functions.
     * It may be helpful to break these up into other methods but is not strictly
     * necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        try {
            //nil
            if (match("NIL")) {
                return new Ast.Expr.Literal(null);
            }
            // true
            else if (match("TRUE")) {
                return new Ast.Expr.Literal(true);
            }
            // false
            else if (peek("FALSE")) {
                return new Ast.Expr.Literal(false);
            }
            // integer
            else if (match(Token.Type.INTEGER)) {
                return new Ast.Expr.Literal(new BigInteger(tokens.get(-1).getLiteral()));
            }
            // decimal
            else if (match(Token.Type.DECIMAL)) {
                return new Ast.Expr.Literal(new BigDecimal(tokens.get(-1).getLiteral()));
            }
            // character
            else if (peek(Token.Type.CHARACTER)) {//match?
                // without escapes
                if (tokens.get(0).getLiteral().length() <= 3) {
                    return new Ast.Expr.Literal(tokens.get(0).getLiteral().charAt(1));
                }
                // with escapes
                else {
                    String line = escape();
                    Character c = line.charAt(1);
                    match(Token.Type.CHARACTER);
                    return new Ast.Expr.Literal(c);
                }
            }
            // string
            else if (match(Token.Type.STRING)) {
                String line = escape();
                return new Ast.Expr.Literal(line.substring(1, line.length() - 1));
            }
            // '(' expression ')'
            else if (peek("(")) {
                match("(");
                Ast.Expr.Group group = new Ast.Expr.Group(parseExpression());
                if (match(")")) {
                    return group;
                } else {
                    throw new ParseException("No closing parenthesis", tokens.get(-1).getIndex());
                }
            }

            // identifier ('(' (expression (',' expression)*)? ')')?
            else if (peek(Token.Type.IDENTIFIER)) {
                String name = tokens.get(0).getLiteral();
                match(Token.Type.IDENTIFIER);

                if (peek("(")) {
                    match("(");

                    List<Ast.Expr> arguments = new ArrayList<Ast.Expr>();

                    while (!peek(")")) {
                        arguments.add(parseExpression());
                        if (peek(",")) {
                            match(",");
                            if (peek(")"))
                                throw new ParseException("Extra semicolon", tokens.get(0).getIndex());
                        }
                    }

                    match(")");
                    return new Ast.Expr.Function(Optional.empty(), name, arguments);
                } else
                    return new Ast.Expr.Access(Optional.empty(), name);
            } else {
                int index = (tokens.has(0)) ? tokens.get(0).getIndex() : indexHelper(false);
                throw new ParseException("Invalid PRIMARY expression", index);
            }
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage(), pe.getIndex());
        }
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's type
     * is the same, or a {@link String}, which matches if the token's literal is the
     * same.
     * <p>
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i))
                return false;
            else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType())
                    return false;
            } else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral()))
                    return false;
            } else
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true and
     * advances the token stream.
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

        public int showIndex() {
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
    private Ast.Expr expressionHelper(Ast.Expr left) {
        String operator = tokens.get(0).getLiteral();
        match(Token.Type.OPERATOR);
        Ast.Expr right = parseLogicalExpression();
        return new Ast.Expr.Binary(operator, left, right);
    }

    private ParseException error(String msg) {
        if (tokens.has(0)) {
            return new ParseException(msg, tokens.get(0).getIndex());
        } else {
            return new ParseException(msg, (tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length()));
        }
    }

    private String escape() {
        String value = tokens.get(-1).getLiteral(); // was -1
        value = value.replace("\\b", "\b");
        value = value.replace("\\n ", "\n");
        value = value.replace("\\n", "\n");
        value = value.replace("\\r", "\r");
        value = value.replace("\\t", "\t");
        value = value.replace("\\\"", "\"");
        value = value.replace("\\\\", "\\");
        value = value.replace("\\\'", "\'");
        return value;
    }

    private void exHelper(String msg) {
        try {
            int b = tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length();

            if (tokens.has(0))
                throw new ParseException(msg, tokens.get(0).getIndex());
            else
                throw new ParseException(msg, b);
        } catch (ParseException pe) {
            // do nothing
        }
    }

    private int indexHelper(boolean has) {
        if (has)
            return tokens.get(0).getIndex();
        return tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length();
    }
}