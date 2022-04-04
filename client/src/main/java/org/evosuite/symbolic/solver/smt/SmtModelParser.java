/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver.smt;

import org.evosuite.symbolic.solver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;

public final class SmtModelParser extends ResultParser {

    public static final String AS_TOKEN = "as";
    public static final String SAT_TOKEN = "sat";
    public static final String INT_TOKEN = SmtSort.INT.getName();
    public static final String REAL_TOKEN = SmtSort.REAL.getName();
    public static final String SLASH_TOKEN = "/";
    public static final String MINUS_TOKEN = "-";
    public static final String QUOTE_TOKEN = "\"";
    public static final String MODEL_TOKEN = "model";
    public static final String CONST_TOKEN = "const";
    public static final String STORE_TOKEN = SmtOperation.Operator.STORE.toString();
    public static final String ARRAY_TOKEN = SmtSort.ARRAY.getName();
    public static final String STRING_TOKEN = SmtSort.STRING.getName();
    public static final String NEW_LINE_TOKEN = "\n";
    public static final String DEFINE_FUN_TOKEN = "define-fun";
    public static final String BLANK_SPACE_TOKEN = " ";
    public static final String LEFT_PARENTHESIS_TOKEN = "(";
    public static final String RIGHT_PARENTHESIS_TOKEN = ")";

    private final Map<String, Object> initialValues;
    static Logger logger = LoggerFactory.getLogger(SmtModelParser.class);

    public SmtModelParser(Map<String, Object> initialValues) {
        this.initialValues = initialValues;
    }

    public SmtModelParser() {
        this.initialValues = null;
    }

    public SolverResult parse(String solverResultStr)
            throws SolverParseException, SolverErrorException, SolverTimeoutException {
        if (solverResultStr.startsWith(SAT_TOKEN)) {
            logger.debug("Solver outcome was SAT");
            SolverResult satResult = parseModel(solverResultStr);
            return satResult;
        } else if (solverResultStr.startsWith("unsat")) {
            logger.debug("Solver outcome was UNSAT");
            SolverResult unsatResult = SolverResult.newUNSAT();
            return unsatResult;
        } else if (solverResultStr.startsWith("unknown")) {
            logger.debug("Solver outcome was UNKNOWN (probably due to timeout)");
            throw new SolverTimeoutException();
        } else if (solverResultStr.startsWith("(error")) {
            logger.debug("Solver output was the following " + solverResultStr);
            throw new SolverErrorException("An error (probably an invalid input) occurred while executing the solver");
        } else {
            logger.debug("The following solver output could not be parsed " + solverResultStr);
            throw new SolverParseException("Solver output is unknown. We are unable to parse it to a proper solution!",
                    solverResultStr);
        }

    }

    private SolverResult parseModel(String solverResultStr) {
        Map<String, Object> solution = new HashMap<>();

        String token;
        StringTokenizer tokenizer = new StringTokenizer(solverResultStr, "() \n\t", true);
        token = tokenizer.nextToken();
        checkExpectedToken(SAT_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

        token = tokenizer.nextToken();
        checkExpectedToken(MODEL_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

        while (token != null) {
            if (token.equals(RIGHT_PARENTHESIS_TOKEN)) {
                break;
            }

            checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

            token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

            if (token.equals(DEFINE_FUN_TOKEN)) {
                token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

                String fun_name = token;

                token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

                token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

                token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

                Object value;
                if (token.equals(INT_TOKEN)) {
                    value = parseIntegerValue(tokenizer);

                } else if (token.equals(REAL_TOKEN)) {
                    value = parseRealValue(tokenizer);

                } else if (token.equals(STRING_TOKEN)) {
                    value = parseStringValue(tokenizer);

                } else if (token.equals(LEFT_PARENTHESIS_TOKEN)) {
                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                    checkExpectedToken(ARRAY_TOKEN, token);
                    value = parseArrayValue(tokenizer);

                } else {
                    throw new IllegalArgumentException("Unknown data type " + token);
                }

                token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

                solution.put(fun_name, value);
                token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
            }
        }

        if (solution.isEmpty()) {
            logger.warn("The solver model has no variables");
            return null;
        } else {
            logger.debug("Parsed values from solver output");
            for (String varName : solution.keySet()) {
                String valueOf = String.valueOf(solution.get(varName));
                logger.debug(varName + ":" + valueOf);
            }
        }

        if (initialValues != null) {
            if (!solution.keySet().equals(initialValues.keySet())) {
                logger.debug("Adding missing values to Solver solution");
                addMissingValues(initialValues, solution);
            }
        }

        SolverResult satResult = SolverResult.newSAT(solution);
        return satResult;
    }

    private Object parseArrayValue(StringTokenizer tokenizer) {
        Object arrayContents;
        String contentType;
        String token;

        // index type
        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(INT_TOKEN, token);

        contentType = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

        // Store expressions
        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

        int storeOperationsAmount = 0;
        while (token.equals(STORE_TOKEN)) {
            storeOperationsAmount++;

            token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
            checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

            token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        }

        checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(AS_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(CONST_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(ARRAY_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(INT_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(contentType, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

        // This is the array default value, not checking it
        consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

        arrayContents = doParseArrayContent(tokenizer, contentType, token, storeOperationsAmount);

        return arrayContents;
    }

    private Object doParseArrayContent(StringTokenizer tokenizer, String contentType, String token, int elementsAmount) {
        Map<Integer, Object> arrayContents = new HashMap();
        int maxIndex = 0;

        while (elementsAmount > 0) {
            int index = Math.toIntExact(parseIntegerValue(tokenizer));
            Object content;

            if (contentType.equals(INT_TOKEN)) {
                content = parseIntegerValue(tokenizer);
            } else if (contentType.equals(REAL_TOKEN)) {
                content = parseRealValue(tokenizer);
            } else if (contentType.equals(STRING_TOKEN)) {
                //TODO: TestMe when objects support are implemented!
                content = parseStringValue(tokenizer);
            } else {
                throw new IllegalArgumentException("Unknown array content type data " + token);
            }

            arrayContents.put(index, content);
            if (index > maxIndex) maxIndex = index;

            token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
            checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
            elementsAmount--;
        }

        // TODO (ilebrero): There's an incoherence between the result of SMT (arrays are modeled as undefined functions)
        //  		 and the check we do after parsing this result to check if the solution is valid. We have to recreate the
        //			 hole concrete array then and we don't have info at this point about the length variable (it may be bigger).
        //
        // We create the concrete array
        Object array = buildNewArray(contentType, maxIndex + 1);

        // We fill the array with the new values
        for (Integer index : arrayContents.keySet()) {
            if (contentType.equals(INT_TOKEN)) {
                ((long[]) array)[index] = (Long) arrayContents.get(index);

            } else if (contentType.equals(REAL_TOKEN)) {
                ((double[]) array)[index] = (Double) arrayContents.get(index);

            } else if (contentType.equals(STRING_TOKEN)) {
                ((String[]) array)[index] = (String) arrayContents.get(index);

            } else {
                throw new IllegalArgumentException("Unknown array content type data " + contentType);
            }
        }

        return array;
    }

    /**
     * Creates a new array instance based on a content type and length
     *
     * @param contentType
     * @param length
     * @return
     */
    private Object buildNewArray(String contentType, int length) {
        Class componentTypeClass;

        if (INT_TOKEN.equals(contentType)) {
            componentTypeClass = long.class;
        } else if (REAL_TOKEN.equals(contentType)) {
            componentTypeClass = double.class;
        } else if (STRING_TOKEN.equals(contentType)) {
            componentTypeClass = String.class;
        } else {
            throw new IllegalStateException("Unexpected array content type: " + contentType);
        }

        return Array.newInstance(
                componentTypeClass,
                length
        );
    }

    private static String consumeTokens(StringTokenizer tokenizer, String... tokensToConsume) {
        List<String> tokenList = Arrays.asList(tokensToConsume);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!tokenList.contains(token)) {
                return token;
            }
        }
        // reached end of string
        return null;
    }

    private String parseStringValue(StringTokenizer tokenizer) {
        String token;
        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        StringBuilder strBuilder = new StringBuilder();

        checkExpectedToken(QUOTE_TOKEN, String.valueOf(token.charAt(0)));

        strBuilder.append(token);
        if (!token.substring(1).endsWith(QUOTE_TOKEN)) {
            String stringToken;
            do {
                if (!tokenizer.hasMoreTokens()) {
                    System.out.println("Error!");
                }
                stringToken = tokenizer.nextToken();
                strBuilder.append(stringToken);
            } while (!stringToken.endsWith(QUOTE_TOKEN)); // append until
            // \" is found
        }
        String stringWithNoQuotes = removeQuotes(strBuilder.toString());
        String string = decode(stringWithNoQuotes);

        return string;
    }

    private static String decode(String encodedString) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < encodedString.length(); i++) {
            char c = encodedString.charAt(i);
            if (c == '\\') {
                if (i < encodedString.length() - 1) {
                    switch (encodedString.charAt(i + 1)) {
                        case 'b': {
                            builder.append('\b');
                            i++;
                            break;
                        }
                        case 't': {
                            builder.append('\t');
                            i++;
                            break;
                        }
                        case 'n': {
                            builder.append('\n');
                            i++;
                            break;
                        }
                        case '\\': {
                            builder.append('\\');
                            i++;
                            break;
                        }
                        case 'x': {
                            String hexString = encodedString.substring(i + 2, i + 4);
                            int decimal = Integer.parseInt(hexString, 16);
                            builder.append((char) decimal);
                            i = i + 3;
                            break;
                        }
                        default: {
                            builder.append(c);
                        }
                    }
                }

            } else {
                builder.append(c);
            }
        }
        return builder.toString();

    }

    private String removeQuotes(String stringWithQuotes) {
        return stringWithQuotes.substring(1, stringWithQuotes.length() - 1);
    }

    private static Double parseRealValue(StringTokenizer tokenizer) {
        String token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

        Double value;
        if (!token.equals(LEFT_PARENTHESIS_TOKEN)) {
            value = Double.parseDouble(token);

        } else {
            token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
            if (token.equals(MINUS_TOKEN)) {

                token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                if (token.equals(LEFT_PARENTHESIS_TOKEN)) {
                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                    checkExpectedToken(SLASH_TOKEN, token);

                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                    String numeratorStr = token;

                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                    String denominatorStr = token;

                    value = parseRational(true, numeratorStr, denominatorStr);

                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                    checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                    checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
                } else {
                    String absoluteValueStr = token;
                    value = Double.parseDouble(MINUS_TOKEN + absoluteValueStr);

                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                    checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
                }
            } else {

                if (token.equals(SLASH_TOKEN)) {

                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

                    String numeratorStr;
                    boolean neg;
                    if (token.equals(LEFT_PARENTHESIS_TOKEN)) {

                        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                        checkExpectedToken(MINUS_TOKEN, token);

                        neg = true;

                        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                        numeratorStr = token;

                        token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                        checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
                    } else {
                        neg = false;
                        numeratorStr = token;
                    }

                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

                    String denominatorStr = token;
                    value = parseRational(neg, numeratorStr, denominatorStr);

                    token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
                    checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
                } else {

                    value = Double.parseDouble(token);
                }
            }
        }

        return value;
    }

    private static Long parseIntegerValue(StringTokenizer tokenizer) {
        String token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
        boolean neg = false;
        String integerValueStr;
        if (token.equals(LEFT_PARENTHESIS_TOKEN)) {
            neg = true;
            token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

            checkExpectedToken(MINUS_TOKEN, token);
            token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

            integerValueStr = token;
        } else {
            integerValueStr = token;
        }
        Long value;
        if (neg) {
            String absoluteIntegerValue = integerValueStr;
            value = Long.parseLong(MINUS_TOKEN + absoluteIntegerValue);
        } else {
            value = Long.parseLong(integerValueStr);
        }
        if (neg) {
            token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
            checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
        }

        return value;
    }

    private static void checkExpectedToken(String expectedToken, String actualToken) {
        if (!actualToken.equals(expectedToken)) {
            throw new IllegalArgumentException(
                    "Malformed solver solution. Expected \"" + expectedToken + "\" but found \"" + actualToken + "\"");
        }
    }

    private static void addMissingValues(Map<String, Object> initialValues, Map<String, Object> solution) {
        for (String otherVarName : initialValues.keySet()) {
            if (!solution.containsKey(otherVarName)) {
                solution.put(otherVarName, initialValues.get(otherVarName));
            }
        }
    }
}
