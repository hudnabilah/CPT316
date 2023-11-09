package org.example;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * Lexical analyzer for Scheme-like minilanguage:
 * (define (foo x) (bar (baz x)))
 */
public class Lexer {
public enum Type {
    KEYWORD,
    CONSTANT,
    SYMBOL,
    LITERAL,
    IDENTIFIER,
    OPERATOR
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

        /*
         * Given a String, and an index, get the atom starting at that index
         */
        public static String getAtom(String s, int i) {
            int j = i;
            for (; j < s.length(); ) {
                if (Character.isLetter(s.charAt(j))) {
                    j++;
                } else {
                    return s.substring(i, j);
                }
            }
            return s.substring(i, j);
        }

    public static List<Token> lex(String input) {
        String localKeywordPattern = "\\b(?<!\\w)(define|if|else|while|for|return|\\+)(?!\\w)\\b";
        String localConstantPattern = "\\b\\d+\\b";
        String localidentifierPattern = "\\b(?!define\\b)[a-zA-Z][a-zA-Z0-9]*\\b";
        String localliteralPattern = "\"[^\"]*\"";
        String localsymbolPattern = "\\(|\\)|\\{|\\}|;"; // Add more symbols as needed
        String localoperatorPattern = "\\+|-|\\*|/|%|==|!=|<|>|<=|>="; // Add more operators as needed


        // Combine patterns into a separate regex for each language construct
        String keywordRegex = String.format("(%s)", localKeywordPattern);
        String constantRegex = String.format("(%s)", localConstantPattern);
        String identifierRegex = String.format("(%s)", localidentifierPattern);
        String literalRegex = String.format("(%s)", localliteralPattern);
        String symbolRegex = String.format("(%s)", localsymbolPattern);
        String operatorRegex = String.format("(%s)", localoperatorPattern);


        // Compile the regular expressions
        Pattern keywordPattern = Pattern.compile(keywordRegex);
        Pattern constantPattern = Pattern.compile(constantRegex);
        Pattern identifierPattern = Pattern.compile(identifierRegex);
        Pattern literalPattern = Pattern.compile(literalRegex);
        Pattern symbolPattern = Pattern.compile(symbolRegex);
        Pattern operatorPattern = Pattern.compile(operatorRegex);


        // Match the input against each regular expression
        Matcher keywordMatcher = keywordPattern.matcher(input);
        Matcher constantMatcher = constantPattern.matcher(input);
        Matcher identifierMatcher = identifierPattern.matcher(input);
        Matcher literalMatcher = literalPattern.matcher(input);
        Matcher symbolMatcher = symbolPattern.matcher(input);
        Matcher operatorMatcher = operatorPattern.matcher(input);


        // Create a list to store the tokens
        List<Token> tokens = new ArrayList<>();

        // Iterate over the input and add tokens to the list as they are found
        while (true) {
            if (keywordMatcher.find()) {
                tokens.add(new Token(Type.KEYWORD, keywordMatcher.group()));
            } else if (constantMatcher.find()) {
                tokens.add(new Token(Type.CONSTANT, constantMatcher.group()));
            } else if (identifierMatcher.find()) {
                tokens.add(new Token(Type.IDENTIFIER, identifierMatcher.group()));
            } else if (literalMatcher.find()) {
                tokens.add(new Token(Type.LITERAL, literalMatcher.group()));
            } else if (symbolMatcher.find()) {
                tokens.add(new Token(Type.SYMBOL, symbolMatcher.group()));
            } else if (operatorMatcher.find()) {
                tokens.add(new Token(Type.OPERATOR, operatorMatcher.group()));
            } else {
                break; // No more matches, exit the loop
            }
        }

        // Return the list of tokens
        return tokens;
    }

        private static boolean isNumber(String s) {
            // Implement logic to check if the token is a number
            return s.matches("\\d+");
        }

        private static boolean isSymbol(String s) {
            // Implement logic to check if the token is a symbol
            return s.matches("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>\\/?]+");
        }

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);


            System.out.println("Enter source code:");
            String input = scanner.nextLine();

            List<Token> tokens = lex(input);
            System.out.println("Tokens:");
            for (Token t : tokens) {
                System.out.println(t);
            }

            scanner.close();
        }
    }


