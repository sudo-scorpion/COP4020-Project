package plc.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The lexer works through three main functions:
 * <p>
 * - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 * - {@link #lexToken()}, which lexes the next token
 * - {@link CharStream}, which manages the state of the lexer and literals
 * <p>
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid or missing.
 * <p>
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        ArrayList<Token> token = new ArrayList<>();
        do {
            if (peek("[\\s]")) {
                chars.advance();
                chars.skip();
            } else
                token.add(lexToken());
        } while (peek("."));

        return token;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     * <p>
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        if (peek("^[a-zA-Z_]") && peek("[a-zA-z0-9_-]?"))
            return lexIdentifier();
        else if (peek("[+-]", "[\\d]") || peek("[\\d]"))
            return lexNumber();
        else if (peek("^'") && peek("'$"))
            return lexCharacter();
        else if (peek("^\"") && peek("\"$"))
            return lexString();
        else
            return lexOperator();
    }

    public Token lexIdentifier() {
        while (peek("[a-zA-Z0-9_-]"))
            match("[a-zA-Z0-9_-]");
        return chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber() { //if (peek("[+-]?", "[1-9]+","[\\d*]") || peek("[\\d]"))
        if (peek("[+-]"))
            match("[+-]");
        if (peek("0", "[^.]")){
            match("0");
            return chars.emit(Token.Type.INTEGER);
        }
        while (peek("\\d+"))
            match("\\d+");
        if (peek("\\.{1}", "\\d+")) {
            match("\\.{1}");
            while (peek("\\d+"))
                match("\\d+");
            return chars.emit(Token.Type.DECIMAL);
        }
        return chars.emit(Token.Type.INTEGER);
    }

    public Token lexCharacter() {
        if (peek("\''\'"))
            throw new ParseException("invalid char", chars.index);
        if (peek("'"))
            match("'");
        if (peek("'$"))
            match("'$");
        if (peek("\\\\"))
            lexEscape();
        else if (peek("[^'\\n\\'$]"))
            match("[^'\\n\\'$]");
        if (peek("^'") && peek("'$")) {
            match("'");
            return chars.emit(Token.Type.CHARACTER);
        }else
            throw new ParseException("invalid char", chars.index);
    }

    public Token lexString() {
        if (peek("^\""))
            match("^\"");
        if (peek("\"&"))
            match("\"$");
        while (peek("[^\"\"$]")) {
            if (peek("\\n"))
                throw new ParseException("unterminated string", chars.index);
            if (peek("\\\\"))
                lexEscape();
            else
                match(".");
        }
        if (peek("^\"") && peek("\"$"))
            match("\"");
        else
            throw new ParseException("unterminated string", chars.index);
        return chars.emit(Token.Type.STRING);
    }

    public void lexEscape() {
        if (peek("\\\\"))
            match("\\\\");
        if(peek("['\"bnrt]"))
            match("['\"bnrt]");
        else
            throw new ParseException("invalid escape", chars.index);
    }

    public Token lexOperator() {
        if (peek(".", "="))
            match(".", "=");
        else
            match(".");
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if tshe next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        for (int i = 0; i < patterns.length; i++)
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i]))
                return false;
        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        boolean peek = peek(patterns);
        if (peek)
            for (int i = 0; i < patterns.length; i++)
                chars.advance();
        return true;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     * <p>
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;


        public CharStream(String input) {this.input = input; }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            //System.out.println(type + " " + input.substring(start, index));
            return new Token(type, input.substring(start, index), start);
        }

    }

}
