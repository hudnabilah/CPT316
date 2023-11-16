package org.example;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

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

    // Lexical analysis function
    public static List<Token> lex(String input) {
        // Define regex patterns for different language constructs
        String localKeywordPattern = "\\b(return)\\b";
        String localConstantPattern = "\\b\\d+\\b";
        String localidentifierPattern = "\\b(?!return\\b)[a-zA-Z]\\w*\\b";
        String localliteralPattern = "\"[^\"]*\"";
        String localsymbolPattern = "[#@]";
        String localoperatorPattern = "\\+|-|\\*|/|%|==|!=|<|>|<=|>=|\\=";
        String localseparatorPattern = "\\(|\\)|\\{|\\}|;";

        // Combine patterns into a single regex for each language construct
        String combinedPattern = String.format("(%s)|(%s)|(%s)|(%s)|(%s)|(%s)|(%s)",
                localKeywordPattern, localConstantPattern, localidentifierPattern,
                localliteralPattern, localsymbolPattern, localoperatorPattern, localseparatorPattern);

        // Compile the combined regex pattern
        Pattern combinedPatternCompiled = Pattern.compile(combinedPattern);
        Matcher matcher = combinedPatternCompiled.matcher(input);

        List<Token> tokens = new ArrayList<>();
        String lastTokenType = null;

        boolean semicolonEncountered = false;
        while (matcher.find()) {
            String matchedGroup = matcher.group();
            Type type = null;

            // Determine the type of the matched group based on the patterns
            if (matchedGroup.matches(localKeywordPattern)) {
                type = Type.KEYWORD;
            } else if (matchedGroup.matches(localConstantPattern)) {
                type = Type.CONSTANT;
            } else if (matchedGroup.matches(localidentifierPattern)) {
                type = Type.IDENTIFIER;
            } else if (matchedGroup.matches(localliteralPattern)) {
                type = Type.LITERAL;
            } else if (matchedGroup.matches(localsymbolPattern)) {
                type = Type.SYMBOL;
            } else if (matchedGroup.matches(localoperatorPattern)) {
                type = Type.OPERATOR;
            } else if (matchedGroup.matches(localseparatorPattern)) {
                type = Type.SEPARATOR;
            } else {
                throw new RuntimeException("Invalid input at position " + matcher.start());
            }

            if (matchedGroup.equals(";")) {
                semicolonEncountered = true;
            }

            // Rule 1: Check if operators are used correctly between two identifiers
            if (type == Type.OPERATOR) {
                if (lastTokenType == null || (!lastTokenType.equals(Type.IDENTIFIER.toString())
                        && !lastTokenType.equals(Type.CONSTANT.toString()))) {
                    throw new RuleViolationException("Rule 1 violation: Operator must be used between two identifiers");
                }

                // Check the token after the operator, skipping whitespaces
                int nextTokenIndex = matcher.end();
                while (nextTokenIndex < input.length() && Character.isWhitespace(input.charAt(nextTokenIndex))) {
                    nextTokenIndex++;
                }

                if (nextTokenIndex < input.length()) {
                    String nextToken = input.substring(nextTokenIndex, nextTokenIndex + 1);
                    Type nextTokenType = null;

                    if (nextToken.matches(localidentifierPattern)) {
                        nextTokenType = Type.IDENTIFIER;
                    } else if (nextToken.matches(localConstantPattern)) {
                        nextTokenType = Type.CONSTANT;
                    }

                    if (nextTokenType == null || (!nextTokenType.equals(Type.IDENTIFIER)
                            && !nextTokenType.equals(Type.CONSTANT))) {
                        throw new RuleViolationException("Rule 1 violation: Operator must be used between two identifiers");
                    }
                }
            }

            // Rule 2: Check if consecutive tokens of the same type are not allowed
            if (type.toString().equals(lastTokenType) && type != Type.SEPARATOR) {
                throw new RuleViolationException("Rule 2 violation: Two consecutive tokens of the same type are not allowed");
            }

            // Rule 3: Check if literals/constants are used only in assignment and return operations
            if ((type == Type.LITERAL || type == Type.CONSTANT)
                    && !(lastTokenType != null && (lastTokenType.equals(Type.OPERATOR.toString())
                    || lastTokenType.equals(Type.KEYWORD.toString())
                    || lastTokenType.equals(Type.KEYWORD.toString() + "<return>")) && !matchedGroup.equals("return"))) {
                throw new RuleViolationException("Rule 3 violation: Literals/Constants can only be used in assignment and return operations");
            }

            tokens.add(new Token(type, matchedGroup));
            lastTokenType = type.toString();
        }

        return tokens;
    }

    // Function to check if the source code is valid
    private static boolean isValidSourceCode(String code) {
        code = code.trim();

        // Check for matching curly brackets
        if (!code.matches("\\{\\s*.*\\s*\\}")) {
            return false;
        }

        // Check for balanced parentheses, braces, and brackets
        Map<Character, Character> bracketPairs = new HashMap<>();
        bracketPairs.put('(', ')');
        bracketPairs.put('{', '}');
        bracketPairs.put('[', ']');

        // Using a stack to check for balanced brackets
        java.util.Stack<Character> stack = new java.util.Stack<>();

        for (char c : code.toCharArray()) {
            if (bracketPairs.containsKey(c)) {
                stack.push(c);
            } else if (bracketPairs.containsValue(c)) {
                if (stack.isEmpty() || bracketPairs.get(stack.pop()) != c) {
                    return false;
                }
            }
            // Ignore other characters
        }

        // Ensure the stack is empty (all opening brackets were closed)
        return stack.isEmpty();
    }

    // Main function to take user input, perform lexical analysis, and display tokens and AST
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter source code (Your code needs to start and end with curly brackets):");
            String input = scanner.nextLine();
            if (isValidSourceCode(input)) {
                try {
                    // Lexical analysis
                    List<Lexer.Token> tokens = Lexer.lex(input);

                    // Display tokens
                    System.out.println("----------------------------\nTokens:");
                    for (Lexer.Token t : tokens) {
                        System.out.println(t);
                    }

                    // Parsing
                    Parser parser = new Parser(tokens);

                    try {
                        Parser.ASTNode root = parser.parse();
                        System.out.println("----------------------------\nAST:");
                        Parser.printAST(root, 0);
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                    }

                    System.out.println("Do you want to enter another source code? (yes/no)");
                    String response = scanner.nextLine().trim().toLowerCase();
                    if (!response.equals("yes")) {
                        break;  // Exit the loop if the user doesn't want to enter another source code
                    }
                } catch (RuleViolationException e) {
                    System.out.println("Rule Violation: " + e.getMessage());
                    System.out.println("Do you want to re-enter the source code? (yes/no)");

                    String response = scanner.nextLine().trim().toLowerCase();
                    if (!response.equals("yes")) {
                        break;  // Exit the loop if the user doesn't want to re-enter the source code
                    }
                }
            } else {
                System.out.println("Error: Source code must start and end with curly brackets (})");
                System.out.println("Do you want to re-enter the source code? (yes/no)");

                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("yes")) {
                    break;  // Exit the loop if the user doesn't want to re-enter the source code
                }
            }
        }

        scanner.close();
    }
}