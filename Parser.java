package org.example;

import java.util.List;

public class Parser {

    private final List<Lexer.Token> tokens;
    private int pos;

    public Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public void error(String msg) {
        throw new Error("Syntax error at position {pos}: {msg}");
    }

    public boolean consume(Lexer.Type expectedType) {
        if (tokens.get(pos).t == expectedType) {
            pos++;
            return true;
        } else {
            return false;
        }
    }

    public boolean consume(Lexer.Type t, String expectedValue) {
        if (tokens.get(pos).t == t && tokens.get(pos).c.equals(expectedValue)) {
            pos++;
            return true;
        } else {
            return false;
        }
    }

    public void expect(Lexer.Type expectedType) {
        if (!consume(expectedType)) {
            error("Expected " + expectedType + ", got " + tokens.get(pos).t);
        }
    }

    public void expect(Lexer.Type t, String expectedValue) {
        if (!consume(t, expectedValue)) {
            error("Expected " + t + " with value '" + expectedValue + "', got " + tokens.get(pos).t + " with value '" + tokens.get(pos).c + "'");
        }
    }

    public void program() {
        while (pos < tokens.size()) {
            statement();
        }
    }

    public void statement() {
        if (consume(Lexer.Type.KEYWORD, "define")) {
            define();
        } else {
            expr();
        }
    }

    public void define() {
        expect(Lexer.Type.IDENTIFIER);
        expect(Lexer.Type.OPERATOR, "=");
        expr();
        expect(Lexer.Type.SEPARATOR, ";");
    }

    public void expr() {
        term();
        while (consume(Lexer.Type.OPERATOR, "+")) {
            term();
        }
    }

    public void term() {
        factor();
        while (consume(Lexer.Type.OPERATOR, "*")) {
            factor();
        }
    }

    public void factor() {
        if (consume(Lexer.Type.IDENTIFIER) || consume(Lexer.Type.CONSTANT)) {
            // do nothing, as we have successfully consumed an identifier or constant
        } else if (consume(Lexer.Type.SEPARATOR, "(")) {
            expr();
            expect(Lexer.Type.SEPARATOR, ")");
        } else {
            error("Expected an identifier, constant, or '(' expression ')', got " + tokens.get(pos).t);
        }
    }


    public void id() {
        expect(Lexer.Type.IDENTIFIER);
    }

    public void num() {
        expect(Lexer.Type.CONSTANT);
    }

    public void str() {
        expect(Lexer.Type.LITERAL);
    }
}