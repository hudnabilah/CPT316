package org.example;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    public enum Type {
        KEYWORD,
        CONSTANT,
        SYMBOL,
        LITERAL,
        IDENTIFIER,
        OPERATOR,
        SEPARATOR
    }

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

    public static List<Token> lex(String input) {
        String localKeywordPattern = "\\b(?<!\\w)(define|if|else|while|for|return|\\+|lambda|let|cond|and|or|not|begin|quote|set!)(?!\\w)\\b";
        String localConstantPattern = "\\b\\d+\\b";
        String localidentifierPattern = "\\b(?!define\\b|if\\b|else\\b|while\\b|for\\b|return\\b|\\+\\b|lambda\\b|let\\b|cond\\b|and\\b|or\\b|not\\b|begin\\b|quote\\b|set!\\b)[a-zA-Z]\\w*\\b";
        String localliteralPattern = "\"[^\"]*\"";
        String localsymbolPattern = "[#&$@]";
        String localoperatorPattern = "\\+|-|\\*|/|%|==|!=|<|>|<=|>=|\\=";
        String localseparatorPattern = "\\(|\\)|\\{|\\}|;|,|:|_";

        // Combine patterns into a single regex for each language construct
        String combinedPattern = String.format("(%s)|(%s)|(%s)|(%s)|(%s)|(%s)|(%s)",
                localKeywordPattern, localConstantPattern, localidentifierPattern,
                localliteralPattern, localsymbolPattern, localoperatorPattern, localseparatorPattern);

        Pattern combinedPatternCompiled = Pattern.compile(combinedPattern);
        Matcher matcher = combinedPatternCompiled.matcher(input);

        List<Token> tokens = new ArrayList<>();

        while (matcher.find()) {
            String matchedGroup = matcher.group();
            Type type;

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

            tokens.add(new Token(type, matchedGroup));
        }

        return tokens;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter source code (Your code needs to start and end with curly brackets):");
            String input = scanner.nextLine();

            if (input.trim().startsWith("{") && input.trim().endsWith("}")) {
                List<Token> tokens = lex(input);
                System.out.println("Tokens:");
                for (Token t : tokens) {
                    System.out.println(t);
                }

                // Create an instance of the Parser
                Parser parser = new Parser(tokens);

                // Call the program method to start parsing
                try {
                    parser.program();
                    System.out.println("Parsing successful!");
                } catch (Error e) {
                    System.out.println("Parsing failed: " + e.getMessage());
                }

                System.out.println("Do you want to enter another source code? (yes/no)");
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("yes")) {
                    break;  // Exit the loop if the user doesn't want to enter another source code
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