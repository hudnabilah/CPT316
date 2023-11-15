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
        if (currentTokenIndex < tokens.size()) {
            currentTokenIndex++;
        }
    }

    private Lexer.Token getCurrentToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        }
        return null;
    }

    public ASTNode parse() {
        return parseExpression();
    }

    private ASTNode parseExpression() {
        ASTNode termNode = parseTerm();
        return parseExpressionPrime(termNode);
    }

    private ASTNode parseExpressionPrime(ASTNode leftNode) {
        Lexer.Token currentToken = getCurrentToken();

        if (currentToken != null && (currentToken.t == Lexer.Type.OPERATOR)) {
            consume();
            ASTNode termNode = parseTerm();
            ASTNode newNode = new ASTNode(currentToken.c, "OPERATOR", List.of(leftNode, termNode));
            return parseExpressionPrime(newNode);
        }

        return leftNode;
    }

    private ASTNode parseTerm() {
        ASTNode factorNode = parseFactor();
        return parseTermPrime(factorNode);
    }

    private ASTNode parseTermPrime(ASTNode leftNode) {
        Lexer.Token currentToken = getCurrentToken();

        if (currentToken != null && (currentToken.t == Lexer.Type.OPERATOR)) {
            consume();
            ASTNode factorNode = parseFactor();
            ASTNode newNode = new ASTNode(currentToken.c, "OPERATOR", List.of(leftNode, factorNode));
            return parseTermPrime(newNode);
        }

        return leftNode;
    }

    private ASTNode parseFactor() {
        Lexer.Token currentToken = getCurrentToken();

        if (currentToken != null) {
            System.out.println("1");
            System.out.println(currentToken.t);
            System.out.println(currentToken.c);
            if (currentToken.t == Lexer.Type.CONSTANT || currentToken.t == Lexer.Type.IDENTIFIER) {
                consume();
                System.out.println("2");
                return new ASTNode(currentToken.c, currentToken.t.toString(), null);
            } else if (currentToken.t == Lexer.Type.SEPARATOR && currentToken.c.equals("{")) {
                System.out.println("3");
                consume();
                ASTNode expressionNode = parseExpression();
                currentToken = getCurrentToken();  // Update the current token after parsing the expression
                if (currentToken != null && currentToken.t == Lexer.Type.SEPARATOR && currentToken.c.equals("}")) {
                    System.out.println("4");
                    consume();
                    return expressionNode;
                } else {
                    System.out.println("5");
                    throw new RuntimeException("Expected closing parenthesis ')'");
                }
            }
        }

        throw new RuntimeException("Invalid factor");
    }

    public static void printAST(ASTNode node, int depth) {
        if (node != null) {
            for (int i = 0; i < depth; ++i) {
                System.out.print(" ");
            }
            System.out.println(node.getValue() + ":" + node.getType());
            if (node.getChildren() != null) {
                for (ASTNode child : node.getChildren()) {
                    printAST(child, depth + 1);
                }
            }
        }
    }
}