package kd.prtotype;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEvaluator {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("'[^']*'|\\w+|\\(|\\)|!=|<=|>=|<|>|=|\\S");

    public boolean evaluate(Map<String, Object> variables, String expression) {
        List<String> tokens = tokenize(expression);
        return parseExpression(new TokenIterator(tokens), variables);
    }

    private List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(expr);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private boolean parseExpression(TokenIterator tokens, Map<String, Object> vars) {
        return parseOr(tokens, vars);
    }

    private boolean parseOr(TokenIterator tokens, Map<String, Object> vars) {
        boolean result = parseAnd(tokens, vars);
        while (tokens.match("OR")) {
            result = result || parseAnd(tokens, vars);
        }
        return result;
    }

    private boolean parseAnd(TokenIterator tokens, Map<String, Object> vars) {
        boolean result = parseNot(tokens, vars);
        while (tokens.match("AND")) {
            result = result && parseNot(tokens, vars);
        }
        return result;
    }

    private boolean parseNot(TokenIterator tokens, Map<String, Object> vars) {
        if (tokens.match("NOT")) {
            return !parseComparison(tokens, vars);
        }
        return parseComparison(tokens, vars);
    }

    private boolean parseComparison(TokenIterator tokens, Map<String, Object> vars) {
        if (tokens.match("(")) {
            boolean val = parseExpression(tokens, vars);
            tokens.expect(")");
            return val;
        }

        String key = tokens.next();

        // Check for direct boolean use
        if (!tokens.hasNext() || isLogicalToken(tokens.peek())) {
            return getBooleanValue(vars, key);
        }

        // Existing logic
        if (tokens.match("NOT")) {
            tokens.expect("NULL");
            return vars.get(key) != null;
        } else if (tokens.match("NULL")) {
            return vars.get(key) == null;
        }

        String operator = tokens.next();
        String valueToken = tokens.next();
        Object actual = vars.get(key);
        Object expected = parseLiteral(valueToken);
        return compare(actual, expected, operator);
    }

    private boolean isLogicalToken(String token) {
        return token != null &&
                (token.equalsIgnoreCase("AND") ||
                        token.equalsIgnoreCase("OR") ||
                        token.equals(")"));
    }

    private boolean getBooleanValue(Map<String, Object> vars, String key) {
        Object val = vars.get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        throw new RuntimeException("Boolean variable '" + key + "' used without comparison");
    }
    private Object parseLiteral(String token) {
        // Fast path for booleans
        if (token.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (token.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }

        // String literal
        if (token.startsWith("'") && token.endsWith("'")) {
            return token.substring(1, token.length() - 1);
        }

        // Number fast path
        char firstChar = token.charAt(0);
        if (Character.isDigit(firstChar) ||
                (token.length() > 1 && firstChar == '-' && Character.isDigit(token.charAt(1)))) {

            // Only check for decimal point once
            if (token.indexOf('.') != -1) {
                try {
                    return Double.parseDouble(token);
                } catch (NumberFormatException e) {
                    return token;
                }
            } else {
                try {
                    return Integer.parseInt(token);
                } catch (NumberFormatException e) {
                    return token;
                }
            }
        }

        return token;
    }

    private boolean compare(Object actual, Object expected, String op) {
        if (actual == null || expected == null) {
            return false;
        }

        if (actual instanceof Boolean && expected instanceof Boolean) {
            boolean a = (Boolean) actual;
            boolean b = (Boolean) expected;
            if (op.equals("=")) {
                return a == b;
            }
            if (op.equals("!=")) {
                return a != b;
            }
            return false;
        }

        if (actual instanceof Number && expected instanceof Number) {
            double a = ((Number) actual).doubleValue();
            double b = ((Number) expected).doubleValue();
            return switch (op) {
                case "=" -> a == b;
                case "!=" -> a != b;
                case ">" -> a > b;
                case "<" -> a < b;
                case ">=" -> a >= b;
                case "<=" -> a <= b;
                default -> false;
            };
        }

        String a = String.valueOf(actual);
        String b = String.valueOf(expected);
        return switch (op) {
            case "=" -> a.equals(b);
            case "!=" -> !a.equals(b);
            default -> false;
        };
    }

    private static class TokenIterator {
        private final List<String> tokens;
        private int pos = 0;

        TokenIterator(List<String> tokens) {
            this.tokens = tokens;
        }

        boolean hasNext() {
            return pos < tokens.size();
        }

        String next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Unexpected end of tokens");
            }
            return tokens.get(pos++);
        }

        boolean match(String expected) {
            if (hasNext() && tokens.get(pos).equalsIgnoreCase(expected)) {
                pos++;
                return true;
            }
            return false;
        }

        void expect(String expected) {
            if (!match(expected)) {
                throw new RuntimeException("Expected token: " + expected);
            }
        }

        String peek() {
            return hasNext() ? tokens.get(pos) : null;
        }

    }

}