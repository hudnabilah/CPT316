package org.example;

import java.util.List;

public class Parser {
    // Member variables to store the list of tokens and the current index during parsing
    private List<Lexer.Token> tokens;
    private int currentTokenIndex;

    // Constructor to initialize the Parser with a list of tokens
    public Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    // Class representing a node in the Abstract Syntax Tree (AST)
    public static class ASTNode {
        private String value;
        private String type;
        private List<ASTNode> children;

        // Constructor to create an ASTNode with a value, type, and children
        public ASTNode(String value, String type, List<ASTNode> children) {
            this.value = value;
            this.type = type;
            this.children = children;
        }

        // Getter methods for retrieving the value, type, and children of the node
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

    // Move to the next token in the token list
    private void consume() {
        currentTokenIndex++;
    }

    // Get the current token without moving to the next one
    private Lexer.Token getCurrentToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        }
        return null;
    }

    // Parse the input and build the abstract syntax tree (AST)
    public ASTNode parse() {
        // Start the parsing process with the first child
        ASTNode leftNode = parseChild();
        // Parse the entire expression and return the resulting AST
        ASTNode result = parseNode(leftNode);
        return result;
    }

    // Parse a node in the AST
    private ASTNode parseNode(ASTNode leftNode) {
        // Get the current token
        Lexer.Token currentToken = getCurrentToken();

        // Skip over whitespace tokens
        while (currentToken != null && currentToken.c.equals(" ")) {
            consume();
            currentToken = getCurrentToken();
        }

        // Check if the current token is of a valid type for a node
        if (currentToken != null && (currentToken.t == Lexer.Type.CONSTANT || currentToken.t == Lexer.Type.IDENTIFIER
                || currentToken.t == Lexer.Type.KEYWORD || currentToken.t == Lexer.Type.LITERAL
                || currentToken.t == Lexer.Type.SYMBOL || currentToken.t == Lexer.Type.OPERATOR
                || (currentToken.t == Lexer.Type.SEPARATOR && !currentToken.c.equals("{")))) {
            // Consume the current token and parse the next child
            consume();
            ASTNode factorNode = parseChild();
            // Create a new node with the operator, its type, and the left and right children
            ASTNode newNode = new ASTNode(currentToken.c, currentToken.t.toString(), List.of(leftNode, factorNode));
            // Recursively parse the next node
            return parseNode(newNode);
        }

        // Return the leftNode if no valid current token is found
        return leftNode;
    }

    // Parse a child node in the AST
    private ASTNode parseChild() {
        // Get the current token
        Lexer.Token currentToken = getCurrentToken();

        // Skip over whitespace tokens
        while (currentToken != null && currentToken.c.equals(" ")) {
            consume();
            currentToken = getCurrentToken();
        }

        // Check if there is a valid current token for a child node
        if (currentToken != null) {
            // Consume the current token and create a node with its value and type
            consume();
            return new ASTNode(currentToken.c, currentToken.t.toString(), null);
        }

        // Return an empty node if no valid current token is found
        else {
            return new ASTNode("", "", null);
        }
    }

    // Print the AST in a readable format
    public static void printAST(ASTNode node, int depth) {
        if (node != null) {
            StringBuilder indent = new StringBuilder();
            // Indentation based on the depth of the node in the tree
            for (int i = 0; i < depth; ++i) {
                indent.append("  ");
            }
            // Print the value and type of the node
            System.out.printf("%s%-15s%s\n", indent.toString(), node.getType(), node.getValue());
            if (node.getChildren() != null) {
                for (ASTNode child : node.getChildren()) {
                    // Recursively print the children of the nodes
                    printAST(child, depth + 1);
                }
            }
        }
    }
}