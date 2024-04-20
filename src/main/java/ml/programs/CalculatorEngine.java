package ml.programs;/*
 * Copyright (c) 2024 by Max Lemberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static ml.programs.NumberHelper.PI;
import static ml.programs.NumberHelper.e;
import static ml.programs.ParenthesesBalancer.balanceParentheses;

/**
 * CalculatorActivity
 *
 * @author Max Lemberg
 * @version 3.2.0
 * @date 20.04.2023
 */

public class CalculatorEngine {
    // Declaration of a constant of type MathContext with a precision of 35. This is used for division to ensure a precision of 10 decimal places.
    private static final MathContext MC = new MathContext(35, RoundingMode.HALF_UP);

    // Declaration of a constant for the root operation.
    public static final String ROOT = "√";
    public static final String THIRD_ROOT = "³√";
    public static String MODE = "Deg";

    /**
     * This method calculates the result of a mathematical expression. The expression is passed as a string parameter.
     * <p>
     * It first replaces all the special characters in the expression with their corresponding mathematical symbols.
     * <p>
     * If the expression is in scientific notation, it converts it to decimal notation.
     * <p>
     * It then tokenizes the expression and evaluates it.
     * <p>
     * If the result is too large, it returns "Wert zu groß" (Value too large).
     * If the result is in scientific notation, it formats it to decimal notation.
     * <p>
     * It handles various exceptions such as ArithmeticException, IllegalArgumentException, and other exceptions.
     *
     * @param calc The mathematical expression as a string to be calculated.
     * @return The result of the calculation as a string.
     * @throws ArithmeticException      If there is an arithmetic error in the calculation.
     * @throws IllegalArgumentException If there is an illegal argument in the calculation.
     */
    public static String calculate(String calc) {
        try {
            String trim;
            if (String.valueOf(calc.charAt(0)).equals("+")) {
                calc = calc.substring(1);
            } else if (String.valueOf(calc.charAt(0)).equals("-")) {
                calc = "0" + calc;
            }

            // Replace all the special characters in the expression with their corresponding mathematical symbols
            // important: "е" (German: 'Eulersche-Zahl') and "e" (used for notation) are different characters

            calc = fixExpression(calc);
            String commonReplacements = calc.replace('×', '*')
                    .replace('÷', '/')
                    .replace("=", "")
                    .replace("E", "e")
                    .replace("π", PI)
                    .replaceAll("е", e)
                    .replaceAll(" ", "")
                    .replace("½", "0,5")
                    .replace("⅓", "0,33333333333")
                    .replace("¼", "0,25");

            trim = commonReplacements.replace(".", "").replace(",", ".").trim();
            trim = balanceParentheses(trim);

            System.out.println("Trim:" + trim);

            // If the expression is in scientific notation, convert it to decimal notation
            if (isScientificNotation(trim)) {
                String result = convertScientificToDecimal(trim);
                return removeNonNumeric(result);
            }

            final List<String> tokens = tokenize(trim);

            for (int i = 0; i < tokens.size() - 1; i++) {
                try {
                    if (tokens.get(i).equals("/") && tokens.get(i + 1).equals("-")) {
                        // Handle negative exponent in division
                        tokens.remove(i + 1);
                        tokens.add(i + 1, "NEG_EXPONENT");
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }

            // Evaluate the expression and handle exceptions
            final BigDecimal result = evaluate(tokens);

            double resultDouble = result.doubleValue();
            // If the result is too large, return "Wert zu groß"
            if (Double.isInfinite(resultDouble)) {
                return "Wert zu groß";
            }
            // return the result in decimal notation
            return result.stripTrailingZeros().toPlainString().replace('.', ',');
        } catch (ArithmeticException e) {
            // Handle exceptions related to arithmetic errors
            if (Objects.equals(e.getMessage(), "Wert zu groß")) {
                return "Wert zu groß";
            } else {
                return e.getMessage();
            }
        } catch (IllegalArgumentException e) {
            // Handle exceptions related to illegal arguments
            return e.getMessage();
        } catch (Exception e) {
            return "Syntax Fehler";
        }
    }

    public static boolean isSymbol(final String character) {
        return (String.valueOf(character).equals("¼") || String.valueOf(character).equals("⅓") || String.valueOf(character).equals("½") ||
                String.valueOf(character).equals("е") || String.valueOf(character).equals("e") || String.valueOf(character).equals("π"));
    }

    public static String fixExpression(String input) {
        StringBuilder sb = new StringBuilder();

        if (input.length() >= 2) {
            for (int i = 0; i < input.length(); i++) {
                char currentChar = input.charAt(i);
                sb.append(currentChar);

                if (i + 1 < input.length()) {
                    char nextChar = input.charAt(i + 1);

                    boolean charFound = checkForChar(currentChar);
                    if (charFound) {
                        continue;
                    }

                    if (isOperator(String.valueOf(currentChar)) && isSymbol(String.valueOf(nextChar))) {
                        continue;
                    }

                    if (Character.isDigit(currentChar) && isOperator(String.valueOf(nextChar))) {
                        continue;
                    }

                    if (String.valueOf(currentChar).equals("(") && String.valueOf(nextChar).equals("³")) {
                        continue;
                    }

                    if (Character.isDigit(currentChar) && String.valueOf(nextChar).equals("(")) {
                        sb.append('×');
                        continue;
                    }

                    if ((Character.isDigit(currentChar) && isSymbol(String.valueOf(nextChar))) || (isSymbol(String.valueOf(currentChar)) && Character.isDigit(nextChar))) {
                        sb.append('×');
                        continue;
                    }

                    if ((isSymbol(String.valueOf(currentChar)) && String.valueOf(nextChar).equals("(")) || (String.valueOf(currentChar).equals(")") && isSymbol(String.valueOf(nextChar)))) {
                        sb.append('×');
                        continue;
                    }

                    if (shouldInsertMultiplication(currentChar, nextChar) && (!Character.isDigit(currentChar) && !Character.isDigit(nextChar)) && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×');
                        continue;
                    }

                    if (Character.isDigit(currentChar) && String.valueOf(currentChar).equals("(") && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×');
                        continue;
                    }

                    if (Character.isDigit(currentChar) && Character.isLetter(nextChar) && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×');
                        continue;
                    }

                    if ((currentChar == 'π' && Character.isDigit(nextChar)) ||
                            (nextChar == 'π' && Character.isDigit(currentChar)) && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×'); // Insert '×' between 'π' and digit
                        continue;
                    }

                    if ((Character.isDigit(nextChar) && String.valueOf(nextChar).equals("(") && !isOperator(String.valueOf(nextChar))) ||
                            (nextChar == '³')) {
                        sb.append('×');
                        continue;
                    }

                    if (String.valueOf(currentChar).equals("!") && Character.isDigit(nextChar) && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×');
                    }
                }
            }
            if (sb.length() > 0 && sb.substring(sb.length() - 2, sb.length()).equals("×=")) {
                sb.delete(sb.length() - 2, sb.length());
            }
            return sb.toString();
        }
        return input;
    }

    public static boolean checkForChar(char currentChar) {
        String[] errorMessages = {
                "Wert zu groß",
                "Syntax Fehler",
                "Kein Teilen durch 0",
                "Nur reelle Zahlen",
                "Unbekannter Operator",
                "Domainfehler",
                "Ungültiges Zahlenformat",
                "Nicht definiert",
                "Ungültiger Wert",
                "Ungültige Eingabe",
                "Unendlich",
                "For input string",
                "Kein Teilen",
                "Unbekannte Funktion",
                "Ungültiges Argument",
                "Ungültige Basis",
                "Ungültige Basis oder Argument"
        };

        for (String errorMessage : errorMessages) {
            if (errorMessage.indexOf(currentChar) != -1) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldInsertMultiplication(char currentChar, char nextChar) {
        Set<Character> validChars = createValidCharsSet();
        List<String> places = Arrays.asList("s", "i", "n", "c", "o", "t", "a", "l", "h", "g", "⁻", "¹", "³", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉", "");

        if (places.contains(String.valueOf(currentChar)) && places.contains(String.valueOf(nextChar))) {
            return false; // Don't insert '*' between 'sin' and next character
        }

        return (!validChars.contains(currentChar) && !validChars.contains(nextChar)) ||
                (Character.isDigit(currentChar) && (Character.isLetter(nextChar) || nextChar == '√')) ||
                (currentChar == ')' && (Character.isDigit(nextChar) || nextChar == '(' || nextChar == '√')) ||
                (currentChar == ')' && isMathFunction("", 0)) ||
                (isMathFunction("", 0) && (Character.isDigit(nextChar) || nextChar == '(' || nextChar == '√')) ||
                (isMathFunction("", 0) && isMathFunction("", 1));
    }

    public static Set<Character> createValidCharsSet() {
        Set<Character> validChars = new HashSet<>();
        validChars.add('+');
        validChars.add('-');
        validChars.add('*');
        validChars.add('×');
        validChars.add('/');
        validChars.add('÷');
        validChars.add('.');
        validChars.add(',');
        validChars.add('(');
        validChars.add(')');
        validChars.add('√');
        validChars.add('^');
        validChars.add('!');
        // Add more valid characters here if needed
        return validChars;
    }

    public static boolean isMathFunction(String input, int startIndex) {
        Set<String> mathFunctions = createMathFunctionsSet();
        for (String mathFunction : mathFunctions) {
            if (input.regionMatches(startIndex, mathFunction, 0, mathFunction.length())) {
                // Check if the entire function is present
                if (startIndex + mathFunction.length() >= input.length() || !Character.isLetter(input.charAt(startIndex + mathFunction.length()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Set<String> createMathFunctionsSet() {
        Set<String> mathFunctions = new HashSet<>();
        mathFunctions.add("^");
        mathFunctions.add("√");
        mathFunctions.add("³√");
        mathFunctions.add("ln");
        mathFunctions.add("sin");
        mathFunctions.add("cos");
        mathFunctions.add("tan");
        mathFunctions.add("log");
        mathFunctions.add("sinh");
        mathFunctions.add("cosh");
        mathFunctions.add("tanh");
        mathFunctions.add("log₂");
        mathFunctions.add("log₃");
        mathFunctions.add("log₄");
        mathFunctions.add("log₅");
        mathFunctions.add("log₆");
        mathFunctions.add("log₇");
        mathFunctions.add("log₈");
        mathFunctions.add("log₉");
        mathFunctions.add("sin⁻¹");
        mathFunctions.add("cos⁻¹");
        mathFunctions.add("tan⁻¹");
        mathFunctions.add("sinh⁻¹");
        mathFunctions.add("cosh⁻¹");
        mathFunctions.add("tanh⁻¹");
        // Add more math functions here if needed
        return mathFunctions;
    }

    /**
     * isScientificNotation method checks if a given string is in scientific notation.
     *
     * @param str The input string to be checked.
     * @return True if the string is in scientific notation, otherwise false.
     */
    public static boolean isScientificNotation(final String str) {
        // The input string is formatted by replacing all commas with dots. This is because in some locales, a comma is used as the decimal separator.
        final String formattedInput = str.replace(",", ".");

        // A regular expression pattern is defined to match the scientific notation. The pattern is as follows:
        // "^([-+]?\\d+(\\.\\d+)?)([eE][-+]?\\d+)$"
        // Explanation of the pattern:
        // "^" - start of the line
        // "([-+]?\\d+(\\.\\d+)?)"" - matches a number which may be negative or positive, and may have a decimal part
        // "([eE][-+]?\\d+)" - matches 'e' or 'E' followed by an optional '+' or '-' sign, followed by one or more digits
        // "$" - end of the line
        final Pattern pattern = Pattern.compile("^([-+]?\\d+(\\.\\d+)?)([eE][-+]?\\d+)$");

        // The pattern is used to create a matcher for the formatted input string
        final Matcher matcher = pattern.matcher(formattedInput);

        // The method returns true if the matcher finds a match in the input string, indicating that the string is in scientific notation
        return matcher.matches();
    }

    /**
     * convertScientificToDecimal method converts a number in scientific notation to decimal representation.
     *
     * @param str The input string in scientific notation.
     * @return The decimal representation of the input string.
     */
    public static String convertScientificToDecimal(final String str) {
        // Define the pattern for scientific notation
        final Pattern pattern = Pattern.compile("([-+]?\\d+(\\.\\d+)?)([eE][-+]?\\d+)");
        final Matcher matcher = pattern.matcher(str);
        final StringBuffer sb = new StringBuffer();

        // Process all matches found in the input string
        while (matcher.find()) {
            // Extract number and exponent parts from the match
            final String numberPart = matcher.group(1);
            String exponentPart = matcher.group(3);

            // Remove the 'e' or 'E' from the exponent part
            if (exponentPart != null) {
                exponentPart = exponentPart.substring(1);
            }

            // Check and handle the case where the exponent is too large
            if (exponentPart != null) {
                final int exponent = Integer.parseInt(exponentPart);

                // Determine the sign of the number and create a BigDecimal object
                assert numberPart != null;
                final String sign = numberPart.startsWith("-") ? "-" : "";
                BigDecimal number = new BigDecimal(numberPart);

                // Negate the number if the input starts with a minus sign
                if (numberPart.startsWith("-")) {
                    number = number.negate();
                }

                // Scale the number by the power of ten specified by the exponent
                BigDecimal scaledNumber;
                if (exponent >= 0) {
                    scaledNumber = number.scaleByPowerOfTen(exponent);
                } else {
                    scaledNumber = number.divide(BigDecimal.TEN.pow(-exponent), MC);
                }

                // Remove trailing zeros and append the scaled number to the result buffer
                String result = sign + scaledNumber.stripTrailingZeros().toPlainString();
                if (result.startsWith(".")) {
                    result = "0" + result;
                }
                matcher.appendReplacement(sb, result);
            }
        }

        // Append the remaining part of the input string to the result buffer
        matcher.appendTail(sb);

        // Check if the result buffer contains two consecutive minus signs and remove one if necessary
        if (sb.indexOf("--") != -1) {
            sb.replace(sb.indexOf("--"), sb.indexOf("--") + 2, "-");
        }

        // Return the final result as a string
        System.out.println("sb:" + sb);
        return sb.toString();
    }

    /**
     * This method removes all non-numeric characters from a string, except for the decimal point and comma.
     * It uses a regular expression to match all characters that are not digits, decimal points, or commas, and replaces them with an empty string.
     *
     * @param str The string to be processed.
     * @return The processed string with all non-numeric characters removed.
     */
    public static String removeNonNumeric(final String str) {
        // Replace all non-numeric and non-decimal point characters in the string with an empty string
        return str.replaceAll("[^0-9.,\\-]", "");
    }

    /**
     * Tokenizes a mathematical expression, breaking it into individual components such as numbers, operators, and functions.
     *
     * @param expression The input mathematical expression to be tokenized.
     * @return A list of tokens extracted from the expression.
     */
    public static List<String> tokenize(final String expression) {
        // Debugging: Print input expression
        System.out.println("Input Expression: " + expression);

        // Remove all spaces from the expression
        String expressionWithoutSpaces = expression.replaceAll("\\s+", "");

        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < expressionWithoutSpaces.length(); i++) {
            char c = expressionWithoutSpaces.charAt(i);

            // If the character is a digit, period, or minus sign (if it's at the beginning, after an opening parenthesis, or after an operator),
            // add it to the current token
            if (Character.isDigit(c) || c == '.' || (c == '-' && (i == 0 || expressionWithoutSpaces.charAt(i - 1) == '('
                    || isOperator(String.valueOf(expressionWithoutSpaces.charAt(i - 1)))
                    || expressionWithoutSpaces.charAt(i - 1) == ','))) {
                currentToken.append(c);
            } else if (i + 3 < expressionWithoutSpaces.length() && expressionWithoutSpaces.startsWith("³√", i)) {
                // If "³√(" is found, handle the cubic root operation
                tokens.add(expressionWithoutSpaces.substring(i, i + 2));
                i += 1;
            } else {
                // If the character is an operator or a parenthesis, add the current token to the list and reset the current token
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                if (i + 3 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 3);
                    if (function.equals("ln(")) {
                        tokens.add(function); // Add the full function name
                        i += 2; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 4 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 4);
                    if (function.equals("sin(") || function.equals("cos(") || function.equals("tan(")) {
                        tokens.add(function); // Add the full function name
                        i += 3; // Skip the next characters (already processed)
                        continue;
                    }
                    if (function.equals("log(")) {
                        tokens.add(function); // Add the full function name
                        i += 3; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 5 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 5);
                    if (function.equals("sinh(") || function.equals("cosh(") || function.equals("tanh(")) {
                        tokens.add(function); // Add the full function name
                        i += 4; // Skip the next characters (already processed)
                        continue;
                    }
                    if (function.equals("log₂(") || function.equals("log₃(") || function.equals("log₄(") ||
                            function.equals("log₅(") || function.equals("log₆(") || function.equals("log₇(") ||
                            function.equals("log₈(") || function.equals("log₉(")) {
                        tokens.add(function); // Add the full function name
                        i += 4; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 6 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 6);
                    if (function.equals("sin⁻¹(") || function.equals("cos⁻¹(") || function.equals("tan⁻¹(")) {
                        tokens.add(function); // Add the full function name
                        i += 5; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 7 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 7);
                    if (function.equals("sinh⁻¹(") || function.equals("cosh⁻¹(") || function.equals("tanh⁻¹(")) {
                        tokens.add(function); // Add the full function name
                        i += 6; // Skip the next characters (already processed)
                        continue;
                    }
                }

                tokens.add(Character.toString(c));
            }
        }

        // Add the last token if it exists
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        // Debugging: Print tokens
        System.out.println("Tokens: " + tokens);

        return tokens;
    }

    /**
     * Evaluates a mathematical expression represented as a list of tokens.
     * Converts the expression from infix notation to postfix notation, then evaluates the postfix expression.
     *
     * @param tokens The mathematical expression in infix notation.
     * @return The result of the expression.
     */
    public static BigDecimal evaluate(final List<String> tokens) {
        // Convert the infix expression to postfix
        final List<String> postfixTokens = infixToPostfix(tokens);
        System.out.println("Postfix Tokens: " + postfixTokens);

        // Evaluate the postfix expression and return the result
        return evaluatePostfix(postfixTokens);
    }

    /**
     * Applies an operator to two operands. Supports addition, subtraction, multiplication, division, square root, factorial, and power operations ... .
     * Checks the operator and performs the corresponding operation.
     *
     * @param operand1 The first operand for the operation.
     * @param operand2 The second operand for the operation.
     * @param operator The operator for the operation.
     * @return The result of the operation.
     * @throws IllegalArgumentException If the operator is not recognized or if the second operand for the square root operation is negative.
     */
    public static BigDecimal applyOperator(final BigDecimal operand1, final BigDecimal operand2, final String operator) {
        switch (operator) {
            case "+":
                return operand1.add(operand2, MC);
            case "-":
                return operand1.subtract(operand2, MC);
            case "*":
                return operand1.multiply(operand2, MC);
            case "/":
                if (operand2.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Kein Teilen durch 0");
                } else {
                    return operand1.divide(operand2, MC);
                }
            case ROOT:
                if (operand2.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Nur reelle Zahlen");
                } else {
                    return BigDecimal.valueOf(Math.sqrt(operand2.doubleValue()));
                }
            case THIRD_ROOT:
                return BigDecimal.valueOf(Math.pow(operand2.doubleValue(), 1.0 / 3.0));
            case "!":
                return factorial(operand1);
            case "^":
                return operand1.pow(operand2.intValue(), MathContext.DECIMAL128);
            case "log(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(10)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₂(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(2)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₃(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(3)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₄(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(4)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₅(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(5)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₆(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(6)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₇(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(7)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₈(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(8)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₉(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(9)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "ln(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "sin(":
                if (MODE.equals("Rad")) {
                    return BigDecimal.valueOf(Math.sin(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.sin(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "sinh(":
                if (MODE.equals("Rad")) {
                    return BigDecimal.valueOf(Math.sinh(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.sinh(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "sin⁻¹(":
                if (MODE.equals("Rad")) {
                    return BigDecimal.valueOf(Math.asin(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.toDegrees(Math.asin(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "sinh⁻¹(":
                return asinh(operand2);
            case "cos(":
                if (MODE.equals("Rad")) {
                    return BigDecimal.valueOf(Math.cos(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.cos(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "cosh(":
                if (MODE.equals("Rad")) {
                    return BigDecimal.valueOf(Math.cosh(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.cosh(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "cos⁻¹(":
                if (MODE.equals("Rad")) {
                    return BigDecimal.valueOf(Math.acos(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.toDegrees(Math.acos(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "cosh⁻¹(":
                return acosh(operand2);
            case "tan(":
                if (MODE.equals("Rad")) {
                    return BigDecimal.valueOf(Math.tan(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.tan(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "tanh(":
                if (MODE.equals("Rad")) {
                    return BigDecimal.valueOf(Math.tanh(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.tanh(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "tan⁻¹(":
                if (MODE.equals("Rad")) {
                    return BigDecimal.valueOf(Math.atan(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.toDegrees(Math.atan(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "tanh⁻¹(":
                return atanh(operand2);
            default:
                throw new IllegalArgumentException("Unbekannter Operator");
        }
    }

    /**
     * Calculates the factorial of a number.
     * <p>
     * The factorial of a number is the product of all positive integers less than or equal to the number.
     * For example, the factorial of 5 (denoted as 5!) is 1*2*3*4*5 = 120.
     * <p>
     * The method takes a BigDecimal number as input. It first checks if the number is negative. If it is,
     * the number is made positive for the calculation. Then it checks if the number is a whole number because
     * factorial is only defined for whole numbers.
     * <p>
     * It initializes the result to 1. This will hold the calculated factorial.
     * It calculates the factorial by multiplying the number with the result and then decrementing the number,
     * until the number is greater than 1.
     * <p>
     * If the original number was negative, the result is negated. Otherwise, the result is returned as is.
     *
     * @param number The number for which the factorial is to be calculated.
     * @return The factorial of the number.
     * @throws IllegalArgumentException If the number is not a whole number or if it's greater than 170.
     */
    public static BigDecimal factorial(BigDecimal number) {
        // Check if the number is greater than 170
        if (number.compareTo(new BigDecimal("170")) > 0) {
            throw new IllegalArgumentException("Wert zu groß");
        }

        // Check if the number is negative
        boolean isNegative = number.compareTo(BigDecimal.ZERO) < 0;
        // If the number is negative, convert it to positive
        if (isNegative) {
            number = number.negate();
        }

        // Check if the number is an integer. If not, throw an exception
        if (number.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Domainfehler");
        }

        // Initialize the result as 1
        BigDecimal result = BigDecimal.ONE;

        // Calculate the factorial of the number
        while (number.compareTo(BigDecimal.ONE) > 0) {
            result = result.multiply(number);
            number = number.subtract(BigDecimal.ONE);
        }

        // If the original number was negative, return the negative of the result. Otherwise, return the result.
        return isNegative ? result.negate() : result;
    }

    /**
     * This method calculates the power of a base number to an exponent.
     * It first converts the base and exponent to double values, then uses the Math.pow method to calculate the power.
     * If the result is infinite (which can happen if the base and exponent are too large), it throws an ArithmeticException.
     * If the result is not a valid number format, it throws a NumberFormatException.
     *
     * @param base     The base number.
     * @param exponent The exponent.
     * @return The result of raising the base to the power of the exponent.
     * @throws ArithmeticException   If the result is too large to be represented as a double.
     * @throws NumberFormatException If the result is not a valid number format.
     */
    public static BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        // Convert the base and exponent to double values
        double baseDouble = base.doubleValue();
        double exponentDouble = exponent.doubleValue();

        // Check if the base is zero and the exponent is negative
        if (baseDouble == 0 && exponentDouble < 0) {
            throw new ArithmeticException("Kein Teilen durch 0");
        }

        // Check if the base is negative and the exponent is an integer
        double resultDouble;
        if (baseDouble < 0 && exponentDouble == (int) exponentDouble) {
            baseDouble = -baseDouble;
            resultDouble = -Math.pow(baseDouble, exponentDouble);
        } else {
            resultDouble = Math.pow(baseDouble, exponentDouble);
        }

        // If the result is too large to be represented as a double, throw an exception
        if (Double.isInfinite(resultDouble)) {
            throw new ArithmeticException("Wert zu groß");
        }

        // Convert the result back to a BigDecimal and return it
        try {
            return new BigDecimal(resultDouble, MC).stripTrailingZeros();
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Ungültiges Zahlenformat");
        }
    }

    /**
     * Evaluates a mathematical expression represented in postfix notation.
     *
     * @param postfixTokens The list of tokens in postfix notation.
     * @return The result of the expression.
     * @throws IllegalArgumentException If there is a syntax error in the expression or the stack size is not 1 at the end.
     */
    public static BigDecimal evaluatePostfix(final List<String> postfixTokens) {
        // Create a stack to store numbers
        final List<BigDecimal> stack = new ArrayList<>();

        // Iterate through each token in the postfix list
        for (final String token : postfixTokens) {
            // Debugging: Print current token
            System.out.println("Token: " + token);

            // If the token is a number, add it to the stack
            if (isNumber(token)) {
                stack.add(new BigDecimal(token));
            } else if (isOperator(token)) {
                // If the token is an operator, apply the operator to the numbers in the stack
                applyOperatorToStack(token, stack);
            } else if (isFunction(token)) {
                // If the token is a function, evaluate the function and add the result to the stack
                evaluateFunction(token, stack);
            } else {
                // If the token is neither a number, operator, nor function, throw an exception
                System.out.println("Token is neither a number nor an operator");
                throw new IllegalArgumentException("Syntax Fehler");
            }

            // Debugging: Print current stack
            System.out.println("Stack: " + stack);
        }

        // If there is more than one number in the stack at the end, throw an exception
        if (stack.size() != 1) {
            System.out.println("Stacksize != 1");
            throw new IllegalArgumentException("Syntax Fehler");
        }

        // Return the result
        return stack.get(0);
    }

    /**
     * Applies an operator to numbers in the stack based on the given operator.
     *
     * @param operator The operator to be applied.
     * @param stack    The stack containing numbers.
     */
    private static void applyOperatorToStack(String operator, List<BigDecimal> stack) {
        // If the operator is "!", apply the operator to only one number
        if (operator.equals("!")) {
            final BigDecimal operand1 = stack.remove(stack.size() - 1);
            final BigDecimal result = applyOperator(operand1, BigDecimal.ZERO, operator);
            stack.add(result);
        }
        // If the operator is not "!", apply the operator to two numbers
        else {
            final BigDecimal operand2 = stack.remove(stack.size() - 1);
            // If the operator is not ROOT and THIRDROOT, apply the operator to two numbers
            if (!operator.equals(ROOT) && !operator.startsWith(THIRD_ROOT)) {
                final BigDecimal operand1 = stack.remove(stack.size() - 1);
                final BigDecimal result = applyOperator(operand1, operand2, operator);
                stack.add(result);
            }
            // If the operator is ROOT, apply the operator to only one number
            else {
                BigDecimal result;
                switch (operator) {
                    case ROOT:
                        if (operand2.compareTo(BigDecimal.ZERO) < 0) {
                            // If the operand is negative, throw an exception or handle it as needed
                            throw new IllegalArgumentException("Nur reelle Zahlen");
                        } else {
                            result = BigDecimal.valueOf(Math.sqrt(operand2.doubleValue()));
                        }
                        break;
                    case THIRD_ROOT:
                        result = BigDecimal.valueOf(Math.pow(operand2.doubleValue(), 1.0 / 3.0));
                        break;
                    default:
                        // Handle other operators if needed
                        throw new IllegalArgumentException("Syntax Fehler");
                }
                stack.add(result);
            }
        }
    }

    /**
     * Evaluates a mathematical function and adds the result to the stack.
     *
     * @param function The function to be evaluated.
     * @param stack    The stack containing numbers.
     */
    private static void evaluateFunction(String function, List<BigDecimal> stack) {
        // Implement the evaluation of functions like sin, cos, tan.
        // You can use BigDecimalMath library or Java Math class for standard functions
        // Add the result of the function evaluation to the stack
        BigDecimal operand;

        switch (function) {
            case "log(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log10(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₂(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(2)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₃(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(3)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₄(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(4)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₅(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(5)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₆(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(6)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₇(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(7)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₈(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(8)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₉(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(9)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "ln(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Nicht definiert");
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "sin(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (MODE.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.sin(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.sin(Math.toRadians(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "sinh(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (MODE.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.sinh(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.sinh(Math.toRadians(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "sin⁻¹(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (operand.doubleValue() < -1 || operand.doubleValue() > 1) {
                    throw new ArithmeticException("Ungültiger Wert");
                }
                if (MODE.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.asin(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.toDegrees(Math.asin(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "sinh⁻¹(":
                operand = stack.remove(stack.size() - 1);
                stack.add(asinh(operand));
                break;
            case "cos(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (MODE.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.cos(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.cos(Math.toRadians(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "cosh(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (MODE.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.cosh(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.cosh(Math.toRadians(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "cos⁻¹(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (operand.doubleValue() < -1 || operand.doubleValue() > 1) {
                    throw new ArithmeticException("Ungültiger Wert");
                }
                if (MODE.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.acos(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.toDegrees(Math.acos(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "cosh⁻¹(":
                operand = stack.remove(stack.size() - 1);
                stack.add(acosh(operand));
                break;
            case "tan(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (MODE.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.tan(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    double degrees = operand.doubleValue();
                    if (isMultipleOf90(degrees)) {
                        // Check if the tangent of multiples of 90 degrees is being calculated
                        throw new ArithmeticException("Nicht definiert");
                    }
                    result = BigDecimal.valueOf(Math.tan(Math.toRadians(degrees))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "tanh(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (MODE.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.tanh(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    double degrees = operand.doubleValue();
                    result = BigDecimal.valueOf(Math.tanh(Math.toRadians(degrees))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "tan⁻¹(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (MODE.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.atan(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.toDegrees(Math.atan(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "tanh⁻¹(":
                operand = stack.remove(stack.size() - 1);
                stack.add(atanh(operand));
                break;
        }
    }

    /**
     * Checks if a given angle in degrees is a multiple of 90.
     *
     * @param degrees The angle in degrees to be checked.
     * @return true if the angle is a multiple of 90, false otherwise.
     */
    private static boolean isMultipleOf90(double degrees) {
        // Check if degrees is a multiple of 90
        return Math.abs(degrees % 90) == 0;
    }

    /**
     * Converts a mathematical expression from infix notation to postfix notation.
     *
     * @param infixTokens The list of tokens in infix notation.
     * @return The list of tokens in postfix notation.
     */
    public static List<String> infixToPostfix(final List<String> infixTokens) {
        final List<String> postfixTokens = new ArrayList<>();
        final Stack<String> stack = new Stack<>();

        for (int i = 0; i < infixTokens.size(); i++) {
            final String token = infixTokens.get(i);
            // Debugging: Print current token and stack
            System.out.println("Current Token: " + token);
            System.out.println("Stack: " + stack);

            if (isNumber(token)) {
                postfixTokens.add(token);
            } else if (isFunction(token)) {
                stack.push(token);
            } else if (isOperator(token) && token.equals("-")) {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(token) && !isFunction(stack.peek())) {
                    postfixTokens.add(stack.pop());
                }
                stack.push(token);
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(token) && !isFunction(stack.peek())) {
                    postfixTokens.add(stack.pop());
                }
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    postfixTokens.add(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek().equals("(")) {
                    stack.pop(); // Remove the opening parenthesis
                    if (!stack.isEmpty() && isFunction(stack.peek())) {
                        postfixTokens.add(stack.pop());
                    }
                }
            }

            // Debugging: Print postfixTokens and stack after processing current token
            System.out.println("Postfix Tokens: " + postfixTokens);
            System.out.println("Stack after Token Processing: " + stack);
        }

        while (!stack.isEmpty()) {
            postfixTokens.add(stack.pop());
        }

        // Debugging: Print final postfixTokens
        System.out.println("Final Postfix Tokens: " + postfixTokens);

        return postfixTokens;
    }

    /**
     * Checks if the given token represents a recognized trigonometric function.
     *
     * @param token The token to be checked.
     * @return true if the token represents a trigonometric function, false otherwise.
     */
    public static boolean isFunction(final String token) {
        // Check if the token is one of the recognized trigonometric functions
        return token.equals("sin(") || token.equals("cos(") || token.equals("tan(") ||
                token.equals("sinh(") || token.equals("cosh(") || token.equals("tanh(") ||
                token.equals("log(") || token.equals("log₂(") || token.equals("log₃(") ||
                token.equals("log₄(") || token.equals("log₅(") || token.equals("log₆(") ||
                token.equals("log₇(") || token.equals("log₈(") || token.equals("log₉(") ||
                token.equals("ln(") || token.equals("sin⁻¹(") || token.equals("cos⁻¹(") ||
                token.equals("tan⁻¹(") || token.equals("sinh⁻¹(") || token.equals("cosh⁻¹(") ||
                token.equals("tanh⁻¹(");
    }

    // Inverse hyperbolic sine
    public static BigDecimal asinh(BigDecimal x) {
        BigDecimal term1 = x.pow(2).add(BigDecimal.ONE, MathContext.DECIMAL128);
        BigDecimal term2 = x.add(new BigDecimal(Math.sqrt(term1.doubleValue()), MathContext.DECIMAL128));

        return new BigDecimal(Math.log(term2.doubleValue()), MathContext.DECIMAL128);
    }

    // Inverse hyperbolic cosine
    public static BigDecimal acosh(BigDecimal x) {
        BigDecimal term1 = x.pow(2).subtract(BigDecimal.ONE, MathContext.DECIMAL128);
        BigDecimal term2 = x.add(new BigDecimal(Math.sqrt(term1.doubleValue()), MathContext.DECIMAL128));

        return new BigDecimal(Math.log(term2.doubleValue()), MathContext.DECIMAL128);
    }

    // Inverse hyperbolic tangent
    public static BigDecimal atanh(BigDecimal x) {
        BigDecimal term1 = BigDecimal.ONE.add(x, MathContext.DECIMAL128);
        BigDecimal term2 = BigDecimal.ONE.subtract(x, MathContext.DECIMAL128);

        if (x.compareTo(BigDecimal.valueOf(-1)) <= 0 || x.compareTo(BigDecimal.valueOf(1)) >= 0) {
            throw new ArithmeticException("Ungültiger Wert");
        }

        BigDecimal quotient = term1.divide(term2, MathContext.DECIMAL128);
        return new BigDecimal(0.5 * Math.log(quotient.doubleValue()), MathContext.DECIMAL128);
    }

    /**
     * Checks if a token is a number.
     * It attempts to create a BigDecimal from the token. If successful, the token is considered a number; otherwise, it is not.
     *
     * @param token The token to be checked.
     * @return True if the token is a number, false otherwise.
     */
    public static boolean isNumber(final String token) {
        // Try to create a new BigDecimal from the token
        try {
            new BigDecimal(token);
            // If successful, the token is a number
            return true;
        }
        // If a NumberFormatException is thrown, the token is not a number
        catch (final NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the given token represents a recognized non-functional operator.
     *
     * @param token The token to be checked.
     * @return true if the token represents a non-functional operator, false otherwise.
     */
    public static boolean isOperator(final String token) {
        // Check if the token is one of the recognized non-functional operators
        return token.contains("+") || token.contains("-") || token.contains("*") || token.contains("/") ||
                token.contains("×") || token.contains("÷") ||
                token.contains("^") || token.contains("√") || token.contains("!") || token.contains("³√");
    }

    public static boolean isStandardOperator(final String token) {
        // Check if the token is one of the recognized non-functional operators
        return token.contains("+") || token.contains("-") || token.contains("*") || token.contains("/")
                || token.contains("×") || token.contains("÷");
    }

    /**
     * Determines the precedence of an operator.
     * Precedence rules determine the order in which expressions involving both unary and binary operators are evaluated.
     *
     * @param operator The operator to be checked.
     * @return The precedence of the operator.
     * @throws IllegalArgumentException If the operator is not recognized.
     */
    public static int precedence(final String operator) {
        // If the operator is an opening parenthesis, return 0
        switch (operator) {
            case "(":
                return 0;

            // If the operator is addition or subtraction, return 1
            case "+":
            case "-":
                return 1;

            // If the operator is multiplication or division, return 2
            case "*":
            case "/":
                return 2;

            // If the operator is exponentiation, return 3
            case "^":
                return 3;

            // If the operator is root, return 4
            case "√":
            case "³√":
                return 4;

            // If the operator is factorial, return 5
            case "!":
                return 5;

            // If the operator is sine, cosine, or tangent ..., return 6
            case "log(":
            case "log₂(":
            case "log₃(":
            case "log₄(":
            case "log₅(":
            case "log₆(":
            case "log₇(":
            case "log₈(":
            case "log₉(":
            case "ln(":
            case "sin(":
            case "cos(":
            case "tan(":
            case "sinh(":
            case "cosh(":
            case "tanh(":
            case "sinh⁻¹(":
            case "cosh⁻¹(":
            case "tanh⁻¹(":
            case "sin⁻¹(":
            case "cos⁻¹(":
            case "tan⁻¹(":
                return 6;

            // If the operator is not recognized, throw an exception
            default:
                throw new IllegalArgumentException("Syntax Fehler");
        }
    }
}