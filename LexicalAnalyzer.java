import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
public class LexicalAnalyzer {

    private static final String[] keywords = {"int", "float", "double", "if", "else", "while", "for", "return"};

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
        String line;

        while ((line = reader.readLine()) != null) {
            tokenizeLine(line);
        }

        reader.close();
    }

    private static void tokenizeLine(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, " \t\n\r\f;()[]{}+-*/=<>!%^&|~", true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (isKeyword(token)) {
                System.out.println(token + " - KEYWORD");
            } else if (isIdentifier(token)) {
                System.out.println(token + " - IDENTIFIER");
            } else if (isOperator(token)) {
                System.out.println(token + " - OPERATOR");
            } else {
                System.out.println(token + " - UNKNOWN");
            }
        }
    }

    private static boolean isKeyword(String token) {
        for (String keyword : keywords) {
            if (keyword.equals(token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIdentifier(String token) {
        if (Character.isLetter(token.charAt(0))) {
            for (int i = 1; i < token.length(); i++) {
                if (!Character.isLetterOrDigit(token.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isOperator(String token) {
        String[] operators = {"+", "-", "*", "/", "=", "<", ">", "!", "&&", "||", "^", "&", "|", "~"};
        for (String operator : operators) {
            if (operator.equals(token)) {
                return true;
            }
        }
        return false;
    }
}
