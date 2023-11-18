package org.example;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class Lexer {
    // Enumeration for different token types
    public enum Type {
        KEYWORD,
        CONSTANT,
        SYMBOL,
        LITERAL,
        IDENTIFIER,
        OPERATOR,
        SEPARATOR
    }

    // Token class to represent a token with type and content
    public static class Token {
        public final Type t;
        public final String c;

        public Token(Type t, String c) {
            this.t = t;
            this.c = c;
        }

        public String toString() {
            return t.toString() + "<" + c + ">";
        }
    }

    // Custom exception to handle rule violations during lexical analysis
    public static class RuleViolationException extends RuntimeException {
        public RuleViolationException(String message) {
            super(message);
        }
    }

    // Lexical analysis method
    public static List<Token> lex(String input) {
        Scanner scanner = new Scanner(System.in);

        // Define regex patterns for different language constructs
        String localKeywordPattern = "\\b(return)\\b";
        String localConstantPattern = "\\b\\d+\\b";
        String localIdentifierPattern = "\\b(?!return\\b)[a-zA-Z]\\w*\\b";
        String localLiteralPattern = "\"[^\"]*\"";
        String localSymbolPattern = "[#@]";
        String localOperatorPattern = "\\+|-|\\*|/|%|==|!=|<|>|<=|>=|=";
        String localSeparatorPattern = "\\(|\\)|\\{|\\}|;";
        String localIllegalCharacterPattern = "[^\\s]";

        // Combine patterns into a single regex for each language construct
        String combinedPattern = String.format("(%s)|(%s)|(%s)|(%s)|(%s)|(%s)|(%s)|(%s)",
                localKeywordPattern, localConstantPattern, localIdentifierPattern, localLiteralPattern,
                localSymbolPattern, localOperatorPattern, localSeparatorPattern, localIllegalCharacterPattern);

        // Compile the combined regex pattern
        Pattern combinedPatternCompiled = Pattern.compile(combinedPattern);
        Matcher matcher = combinedPatternCompiled.matcher(input);

        // List to store tokens
        List<Token> tokens = new ArrayList<>();
        String lastTokenType = null;
        boolean semicolonEncountered = false;

        // Loop through the input and identify tokens
        while (matcher.find()) {
            String matchedGroup = matcher.group();
            Type type = null;

            // Determine the type of the matched group based on the patterns
            if (matchedGroup.matches(localKeywordPattern)) {
                type = Type.KEYWORD;
            } else if (matchedGroup.matches(localConstantPattern)) {
                type = Type.CONSTANT;
            } else if (matchedGroup.matches(localIdentifierPattern)) {
                type = Type.IDENTIFIER;
            } else if (matchedGroup.matches(localLiteralPattern)) {
                type = Type.LITERAL;
            } else if (matchedGroup.matches(localSymbolPattern)) {
                type = Type.SYMBOL;
            } else if (matchedGroup.matches(localOperatorPattern)) {
                type = Type.OPERATOR;
            } else if (matchedGroup.matches(localSeparatorPattern)) {
                type = Type.SEPARATOR;
            } else if (matchedGroup.matches(localIllegalCharacterPattern)) {
                checkIllegalCharacter(scanner, matchedGroup);
                continue;  // Skip the rest of the loop for illegal characters
            }

            // Check for rule violations
            checkOperator(type, matcher, input);
            checkConsecutiveTokens(type, lastTokenType);
            checkLiteralsAndConstants(type, lastTokenType, matchedGroup);

            // Add the identified token to the list
            tokens.add(new Token(type, matchedGroup));
            lastTokenType = type.toString();

            if (matchedGroup.equals(";")) {
                semicolonEncountered = true;
            }
        }

        // Check for additional rule violations
        checkSemicolon(input, semicolonEncountered);

        // Return the list of tokens
        return tokens;
    }

    //Rule 1: Check if the code starts and ends with curly brackets
    private static boolean checkCurlyBracket(String code) {
        code = code.trim();

        if (!code.startsWith("{") || !code.endsWith("}")) {
            System.out.println("\nRule 1 Violation: Source code must start with '{' and end with '}'");
            return false;
        }

        return true;
    }

    // Rule 2: Check for illegal characters in the source code
    private static void checkIllegalCharacter(Scanner scanner, String character) {
        throw new RuleViolationException("\nRule 2 Violation: Source code cannot have illegal character: " + character);
    }

    // Rule 3: Check if the operator is used correctly between two identifiers
    private static void checkOperator(Type type, Matcher matcher, String input) {
        if (type == Type.OPERATOR) {
            int nextTokenIndex = matcher.end();
            while (nextTokenIndex < input.length() && Character.isWhitespace(input.charAt(nextTokenIndex))) {
                nextTokenIndex++;
            }

            if (nextTokenIndex < input.length()) {
                String nextToken = input.substring(nextTokenIndex, nextTokenIndex + 1);
                Type nextTokenType = null;

                if (nextToken.matches("\\b\\d+\\b")) {
                    nextTokenType = Type.CONSTANT;
                } else if (nextToken.matches("\\b(?!return\\b)[a-zA-Z]\\w*\\b")) {
                    nextTokenType = Type.IDENTIFIER;
                }

                if (nextTokenType == null || (!nextTokenType.equals(Type.IDENTIFIER) && !nextTokenType.equals(Type.CONSTANT))) {
                    throw new RuleViolationException("\nRule 3 violation: Operator must be used between two identifiers");
                }
            }
        }
    }

    // Rule 4: Check for consecutive tokens of the same type
    private static void checkConsecutiveTokens(Type type, String lastTokenType) {
        if (type.toString().equals(lastTokenType) && type != Type.SEPARATOR) {
            throw new RuleViolationException("\nRule 4 Violation: Two consecutive tokens of the same type are not allowed");
        }
    }

    // Rule 5: Check if literals and constants are used in the correct context
    private static void checkLiteralsAndConstants(Type type, String lastTokenType, String matchedGroup) {
        if ((type == Type.LITERAL || type == Type.CONSTANT)
                && !(lastTokenType != null && (lastTokenType.equals(Type.OPERATOR.toString())
                || lastTokenType.equals(Type.KEYWORD.toString())
                || lastTokenType.equals(Type.KEYWORD.toString() + "<return>")) && !matchedGroup.equals("return"))) {
            throw new RuleViolationException("\nRule 5 Violation: Literals/Constants can only be used in assignment and return operations");
        }
    }

    // Rule 6: Check if a semicolon is present before the closing curly brace
    private static void checkSemicolon(String input, boolean semicolonEncountered) {
        if (!semicolonEncountered) {
            int lastSemicolonIndex = input.lastIndexOf(';');
            int lastCurlyBraceIndex = input.lastIndexOf('}');

            if (lastSemicolonIndex < lastCurlyBraceIndex) {
                throw new RuleViolationException("\nRule 6 Violation: Semicolon is required at the end before the closing curly brace '}'");
            }
        }
    }

    // Rule 7: Check if brackets, parentheses, braces, and brackets are in pairs
    private static boolean checkMatchingPairs(String code) {
        Map<Character, Character> bracketPairs = Map.of('(', ')', '{', '}', '[', ']');
        java.util.Stack<Character> stack = new java.util.Stack<>();

        for (char c : code.toCharArray()) {
            if (bracketPairs.containsKey(c)) {
                stack.push(c);
            } else if (bracketPairs.containsValue(c) && (stack.isEmpty() || bracketPairs.get(stack.pop()) != c)) {
                System.out.println("\nRule 7 Violation: Curly brackets, parentheses, braces, and brackets must be in pairs");
                return false;
            }
        }

        if (!stack.isEmpty()) {
            System.out.println("\nRule 7 Violation: Curly brackets, parentheses, braces, and brackets must be in pairs");
            return false;
        }
        return true;
    }

    // To ask the user for another input
    private static boolean askForAnotherInput(Scanner scanner) {
        System.out.println("\n----------------------------\nDo you want to enter another source code? (yes/no)");
        String response = scanner.nextLine().trim().toLowerCase();
        if (!response.equals("yes")) {
            return false;
        } else {
            for (int i = 0; i < 50; i++) {
                System.out.println(); // Print empty lines to "clear" the console
            }
            return true;
        }
    }

    // Main function to take user input, perform lexical analysis, and display tokens and AST
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter source code (Your code needs to start and end with curly brackets):");
            String input = scanner.nextLine();
            if (checkCurlyBracket(input) && checkMatchingPairs(input)) {
                try {
                    // Lexical analysis
                    List<Lexer.Token> tokens = Lexer.lex(input);

                    // Display tokens
                    System.out.println("----------------------------\nTokens:");
                    for (Lexer.Token t : tokens) {
                        System.out.printf("%-15s%s\n", t.t, t.c);
                    }

                    // Parsing
                    Parser parser = new Parser(tokens);

                    try {
                        Parser.ASTNode root = parser.parse();
                        System.out.println("----------------------------\nAbstract Syntax Tree (AST):");
                        Parser.printAST(root, 0);
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                    }

                } catch (RuleViolationException e) {
                    System.out.println(e.getMessage());
                }

                if (!askForAnotherInput(scanner)) {
                    break;  // Exit the loop if the user doesn't want to enter another source code
                }

            } else {
                if (!askForAnotherInput(scanner)) {
                    break;  // Exit the loop if the user doesn't want to re-enter the source code
                }
            }
        }

        scanner.close();
    }
}
