package org.example;

import java.util.List;

public class Parser {
    private List<Lexer.Token> tokens;
    private int currentTokenIndex;

    public Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    public static class ASTNode {
        private String value;
        private String type;
        private List<ASTNode> children;

        public ASTNode(String value, String type, List<ASTNode> children) {
            this.value = value;
            this.type = type;
            this.children = children;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public List<ASTNode> getChildren() {
            return children;
        }
    }

    private void consume() {
        currentTokenIndex++;
    }

    private Lexer.Token getCurrentToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        }
        return null;
    }

    public ASTNode parse() {
        ASTNode leftNode = parseFactor();
        ASTNode result = parseExpressionPrime(leftNode);
        return result;
    }

    private ASTNode parseExpressionPrime(ASTNode leftNode) {
        Lexer.Token currentToken = getCurrentToken();

        // Skip over whitespace
        while (currentToken != null && currentToken.c.equals(" ")) {
            consume();
            currentToken = getCurrentToken();
        }

        if (currentToken != null && (currentToken.t == Lexer.Type.CONSTANT || currentToken.t == Lexer.Type.IDENTIFIER
                || currentToken.t == Lexer.Type.KEYWORD || currentToken.t == Lexer.Type.LITERAL
                || currentToken.t == Lexer.Type.SYMBOL || currentToken.t == Lexer.Type.OPERATOR
                || (currentToken.t == Lexer.Type.SEPARATOR && !currentToken.c.equals("{")))) {
            consume();
            ASTNode factorNode = parseFactor();
            ASTNode newNode = new ASTNode(currentToken.c, currentToken.t.toString(), List.of(leftNode, factorNode));
            return parseExpressionPrime(newNode);
        }

        return leftNode;
    }

    private ASTNode parseFactor() {
        Lexer.Token currentToken = getCurrentToken();

        // Skip over whitespace
        while (currentToken != null && currentToken.c.equals(" ")) {
            consume();
            currentToken = getCurrentToken();
        }

        if (currentToken != null) {
            consume();
            return new ASTNode(currentToken.c, currentToken.t.toString(), null);
        }

        else{
            return new ASTNode("","",null);
        }

    }

    public static void printAST(ASTNode node, int depth) {
        if (node != null) {
            for (int i = 0; i < depth; ++i) {
                System.out.print("\t");
            }
            if(node.getType()!="") {
                System.out.println(node.getValue() + ":" + node.getType());
                if (node.getChildren() != null) {
                    for (ASTNode child : node.getChildren()) {
                        printAST(child, depth + 1);
                    }
                }
            }
            else
                System.out.print("\n");
        }
    }
}