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
package org.evosuite.symbolic.expr;

import org.evosuite.symbolic.expr.bv.*;
import org.evosuite.symbolic.expr.constraint.ConstraintVisitor;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.constraint.RealConstraint;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.evosuite.symbolic.expr.reader.StringReaderExpr;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.expr.token.TokenizerExpr;
import org.evosuite.utils.RegexDistanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;
import java.util.Vector;

public class DistanceCalculator implements ConstraintVisitor<Object, Void> {

    static Logger log = LoggerFactory.getLogger(DistanceCalculator.class);

    @Override
    public Object visit(IntegerConstraint n, Void arg) {

        ExpressionEvaluator visitor = new ExpressionEvaluator();
        long leftVal = (Long) n.getLeftOperand().accept(visitor, null);
        long rightVal = (Long) n.getRightOperand().accept(visitor, null);

        // special integer constraint: string indexOf char != -1
        long distance = getDistanceIndexOfCFound(n, leftVal, rightVal);
        if (distance != -1)
            return distance;

        // special integer constraint: string indexOfCI char index != -1
        distance = getDistanceIndexOfCIFound(n, leftVal, rightVal);
        if (distance != -1)
            return distance;

        // special integer constraint: string indexOf char == k (k>-1)
        distance = getDistanceIndexOfCEqualsK(n, leftVal, rightVal);
        if (distance != -1)
            return distance;

        // special integer constraint: string indexOf char int == k (k>-1)
        distance = getDistanceIndexOfCIEqualsK(n, leftVal, rightVal);
        if (distance != -1)
            return distance;

        // special case: regex
        distance = getDistanceRegex(n, leftVal, rightVal);
        if (distance != -1)
            return distance;

        // special cases: reader.read()==-1
        // special cases: reader.read()!=-1
        distance = getDistanceStringReaderLength(n, leftVal, rightVal);
        if (distance != -1)
            return distance;

        distance = getDistanceStringIsInteger(n, leftVal, rightVal);
        if (distance != -1)
            return distance;

        Comparator cmpr = n.getComparator();
        log.debug("Calculating distance for " + leftVal + " " + cmpr + " " + rightVal);

        distance = leftVal - rightVal;


        switch (cmpr) {
            case EQ:
                return Math.abs(distance);
            case NE:
                return distance != 0 ? (long) 0 : (long) 1;
            case LT:
                return distance < 0 ? 0 : distance + 1;
            case LE:
                return distance <= 0 ? 0 : distance;
            case GT:
                return distance > 0 ? 0 : Math.abs(distance) + 1;
            case GE:
                return distance >= 0 ? 0 : Math.abs(distance);
            default:
                log.warn("getIntegerDist: unimplemented comparator");
                return Long.MAX_VALUE;
        }
    }

    private static long getDistanceStringIsInteger(IntegerConstraint n, long leftVal, long rightVal) {

        if (n.getLeftOperand() instanceof StringUnaryToIntegerExpression && n.getComparator() == Comparator.NE
                && n.getRightOperand() instanceof IntegerConstant) {
            IntegerConstant right_constant = (IntegerConstant) n.getRightOperand();
            StringUnaryToIntegerExpression left_string_expr = (StringUnaryToIntegerExpression) n.getLeftOperand();

            if (right_constant.getConcreteValue() != 0L) {
                return -1;
            }

            if (left_string_expr.getOperator() != Operator.IS_INTEGER) {
                return -1;
            }

            String string = left_string_expr.getOperand().getConcreteValue();
            if (string.length() > 0) {
                char[] charArray = string.toCharArray();
                int maxDistance = 0;
                for (char c : charArray) {
                    int distance;
                    if (!Character.isDigit(c)) {
                        if (c < '0') {
                            distance = '0' - c;
                        } else if (c > '9') {
                            distance = c - '9';
                        } else {
                            throw new RuntimeException("This branch is unreachable!");
                        }
                        if (maxDistance < distance) {
                            maxDistance = distance;
                        }
                    }
                }
                return maxDistance;
            } else {
                return Long.MAX_VALUE;
            }

        }
        return -1;
    }

    @Override
    public Object visit(RealConstraint n, Void arg) {
        ExpressionEvaluator visitor = new ExpressionEvaluator();
        double left = (Double) n.getLeftOperand().accept(visitor, null);
        double right = (Double) n.getRightOperand().accept(visitor, null);

        Comparator cmpr = n.getComparator();

        switch (cmpr) {

            case EQ:

                return Math.abs(left - right);
            case NE:

                return (left - right) != 0 ? (double) 0 : (double) 1;
            case LT:

                return left - right < 0 ? 0 : left - right + 1;
            case LE:

                return left - right <= 0 ? 0 : left - right;
            case GT:

                return left - right > 0 ? 0 : right - left + 1;
            case GE:

                return left - right >= 0 ? 0 : right - left;

            default:
                log.warn("getIntegerDist: unimplemented comparator");
                return Double.MAX_VALUE;
        }

    }

    @Override
    public Object visit(StringConstraint n, Void arg) {
        Expression<?> exprLeft = n.getLeftOperand();
        Comparator cmpr = n.getComparator();
        double distance = 0.0;

        if (exprLeft instanceof StringBinaryComparison) {
            StringBinaryComparison scTarget = (StringBinaryComparison) exprLeft;
            distance = getStringDistance(scTarget);
            log.debug("Calculating distance of constraint " + n);
        } else if (exprLeft instanceof StringMultipleComparison) {
            StringMultipleComparison scTarget = (StringMultipleComparison) exprLeft;
            distance = getStringDistance(scTarget);
            log.debug("Calculating distance of constraint " + n);
        } else if (exprLeft instanceof HasMoreTokensExpr) {
            HasMoreTokensExpr hasMoreTokensExpr = (HasMoreTokensExpr) exprLeft;
            distance = getStringDistance(hasMoreTokensExpr);
            log.debug("Calculating distance of constraint " + n);
        } else {
            assert (false) : "Invalid string comparison";
        }
        assert (((Long) n.getRightOperand().getConcreteValue()).intValue() == 0);
        if (cmpr == Comparator.NE) {
            return distance;
        } else {
            // if we don't want to satisfy return 0
            // if not satisfied Long.MAX_VALUE else
            return distance > 0 ? 0.0 : Double.MAX_VALUE;
        }

    }

    private static long getDistanceIndexOfCEqualsK(IntegerConstraint n, long leftVal, long rightVal) {
        if (n.getLeftOperand() instanceof StringBinaryToIntegerExpression && n.getComparator() == Comparator.EQ
                && n.getRightOperand() instanceof IntegerConstant) {
            IntegerConstant right_constant = (IntegerConstant) n.getRightOperand();
            StringBinaryToIntegerExpression left_string_expr = (StringBinaryToIntegerExpression) n.getLeftOperand();

            if (left_string_expr.getOperator() == Operator.INDEXOFC) {

                Expression<?> theSymbolicString = left_string_expr.getLeftOperand();
                Expression<?> theSymbolicChar = left_string_expr.getRightOperand();
                Expression<?> theSymbolicIndex = right_constant;

                // check theString.lenght>0
                ExpressionEvaluator exprExecutor = new ExpressionEvaluator();
                String theConcreteString = (String) theSymbolicString.accept(exprExecutor, null);
                Long theConcreteIndex = (Long) theSymbolicIndex.accept(exprExecutor, null);
                if (theConcreteIndex > theConcreteString.length() - 1) {
                    // there is no char at the index to modify
                    return Long.MAX_VALUE;
                } else if (theConcreteIndex != -1) {
                    int theIndex = theConcreteIndex.intValue();
                    char theConcreteChar = (char) ((Long) theSymbolicChar.accept(exprExecutor, null)).longValue();
                    char theCurrentChar = theConcreteString.charAt(theIndex);
                    return Math.abs(theCurrentChar - theConcreteChar);
                }
            }
        }

        return -1;
    }

    private static long getDistanceRegex(IntegerConstraint n, long leftVal, long rightVal) {
        ExpressionEvaluator exprExecutor = new ExpressionEvaluator();

        if (n.getLeftOperand() instanceof IntegerUnaryExpression) {
            if (((IntegerUnaryExpression) n.getLeftOperand()).getOperator() == Operator.ISDIGIT) {
                Long leftObject = (Long) ((IntegerUnaryExpression) n.getLeftOperand()).getOperand().accept(exprExecutor,
                        null);
                long left_operand = leftObject;
                char theChar = (char) left_operand;
                if ((n.getComparator() == Comparator.EQ && rightVal == 1L)
                        || (n.getComparator() == Comparator.NE && rightVal == 0L)) {
                    if (theChar < '0')
                        return '0' - theChar;
                    else if (theChar > '9')
                        return theChar - '9';
                    else
                        return 0;
                } else if ((n.getComparator() == Comparator.EQ && rightVal == 0L)
                        || (n.getComparator() == Comparator.NE && rightVal == 1L)) {
                    if (theChar < '0' || theChar > '9')
                        return 0;
                    else
                        return Math.min(Math.abs('9' - theChar), Math.abs(theChar - '0'));
                }

            } else if (((IntegerUnaryExpression) n.getLeftOperand()).getOperator() == Operator.ISLETTER) {
                Long leftObject = (Long) ((IntegerUnaryExpression) n.getLeftOperand()).getOperand().accept(exprExecutor,
                        null);
                long left_operand = leftObject;
                char theChar = (char) left_operand;
                if ((n.getComparator() == Comparator.EQ && rightVal == 1L)
                        || (n.getComparator() == Comparator.NE && rightVal == 0L)) {
                    if (theChar < 'A')
                        return 'A' - theChar;
                    else if (theChar > 'z')
                        return theChar - 'z';
                    else
                        return 0;
                } else if ((n.getComparator() == Comparator.EQ && rightVal == 0L)
                        || (n.getComparator() == Comparator.NE && rightVal == 1L)) {
                    if (theChar < 'A' || theChar > 'z')
                        return 0;
                    else
                        return Math.min(Math.abs('z' - theChar), Math.abs(theChar - 'A'));
                }
            }
        }

        return -1;
    }

    private static long getDistanceIndexOfCFound(IntegerConstraint n, long leftVal, long rightVal) {

        ExpressionEvaluator exprExecutor = new ExpressionEvaluator();

        if (n.getLeftOperand() instanceof StringBinaryToIntegerExpression && n.getComparator() == Comparator.NE
                && n.getRightOperand() instanceof IntegerConstant) {
            IntegerConstant right_constant = (IntegerConstant) n.getRightOperand();
            StringBinaryToIntegerExpression left_string_expr = (StringBinaryToIntegerExpression) n.getLeftOperand();

            if (left_string_expr.getOperator() == Operator.INDEXOFC && right_constant.getConcreteValue() == -1L) {

                Expression<?> theSymbolicString = left_string_expr.getLeftOperand();
                Expression<?> theSymbolicChar = left_string_expr.getRightOperand();

                // check theString.lenght>0
                String theConcreteString = (String) theSymbolicString.accept(exprExecutor, null);
                if (theConcreteString.length() == 0) {
                    // if the string is empty, then the branch distance is
                    // maximum since
                    // no char can be modified to satisfy the constraint
                    return Long.MAX_VALUE;
                } else {
                    char theConcreteChar = (char) ((Long) theSymbolicChar.accept(exprExecutor, null)).longValue();
                    char[] charArray = theConcreteString.toCharArray();
                    int min_distance_to_char = Integer.MAX_VALUE;
                    for (char c : charArray) {
                        if (Math.abs(c - theConcreteChar) < min_distance_to_char) {
                            min_distance_to_char = Math.abs(c - theConcreteChar);
                        }

                    }
                    return min_distance_to_char;
                }
            }
        }

        return -1;
    }

    private static long getDistanceIndexOfCIEqualsK(IntegerConstraint n, long leftVal, long rightVal) {

        ExpressionEvaluator exprExecutor = new ExpressionEvaluator();

        if (n.getLeftOperand() instanceof StringMultipleToIntegerExpression && n.getComparator() == Comparator.EQ
                && n.getRightOperand() instanceof IntegerConstant) {
            IntegerConstant right_constant = (IntegerConstant) n.getRightOperand();
            StringMultipleToIntegerExpression left_string_expr = (StringMultipleToIntegerExpression) n.getLeftOperand();

            if (left_string_expr.getOperator() == Operator.INDEXOFCI) {

                Expression<?> theSymbolicString = left_string_expr.getLeftOperand();
                Expression<?> theSymbolicChar = left_string_expr.getRightOperand();
                Expression<?> theSymbolicIndex = right_constant;

                Expression<?> theOffset = left_string_expr.getOther().get(0);
                Long theConcreteOffset = (Long) theOffset.accept(exprExecutor, null);

                // check theString.lenght>0
                String theConcreteString = (String) theSymbolicString.accept(exprExecutor, null);
                Long theConcreteIndex = (Long) theSymbolicIndex.accept(exprExecutor, null);
                if (theConcreteIndex > theConcreteString.length() - theConcreteOffset - 1) {
                    // there is no char at the index to modify
                    return Long.MAX_VALUE;
                } else if (theConcreteIndex != -1) {
                    int theIndex = theConcreteIndex.intValue();
                    char theConcreteChar = (char) ((Long) theSymbolicChar.accept(exprExecutor, null)).longValue();
                    char theCurrentChar = theConcreteString.charAt(theIndex);
                    return Math.abs(theCurrentChar - theConcreteChar);
                }
            }
        }

        return -1;
    }

    private static long getDistanceStringReaderLength(IntegerConstraint n, long leftVal, long rightVal) {

        ExpressionEvaluator exprExecutor = new ExpressionEvaluator();
        Expression<?> left = n.getLeftOperand();
        Expression<?> right = n.getRightOperand();
        if (left instanceof StringReaderExpr && right instanceof IntegerConstant) {
            StringReaderExpr stringReaderExpr = (StringReaderExpr) left;
            IntegerConstant intValue = (IntegerConstant) right;

            String conc_string = (String) stringReaderExpr.getString().accept(exprExecutor, null);
            int new_length = stringReaderExpr.getReaderPosition();
            int conc_string_length = conc_string.length();

            if ((intValue.getConcreteValue() == 0L) && n.getComparator().equals(Comparator.LT)) {

                if (conc_string_length <= new_length)
                    return 0L;
                else {
                    // return distance to length(string)<=new_length
                    return conc_string_length - new_length;
                }
            }

            if ((intValue.getConcreteValue() == 0L) && n.getComparator().equals(Comparator.GE)) {

                if (conc_string_length > new_length)
                    return 0L;
                else {
                    // return distance to length(string)>new_length
                    return new_length - conc_string_length + 1;
                }
            }

            if ((intValue.getConcreteValue() == -1L)
                    && (n.getComparator().equals(Comparator.EQ) || n.getComparator().equals(Comparator.NE))) {

                if (n.getComparator().equals(Comparator.EQ)) {
                    if (conc_string_length <= new_length)
                        return 0L;
                    else {
                        // return distance to length(string)<=new_length
                        return conc_string_length - new_length;
                    }

                } else if (n.getComparator().equals(Comparator.NE)) {
                    if (conc_string_length > new_length)
                        return 0L;
                    else {
                        // return distance to length(string)>new_length
                        return new_length - conc_string_length + 1;
                    }
                }
            }

        }
        // TODO Auto-generated method stub
        return -1L;
    }

    private static long getDistanceIndexOfCIFound(IntegerConstraint n, long leftVal, long rightVal) {
        ExpressionEvaluator exprExecutor = new ExpressionEvaluator();

        if (n.getLeftOperand() instanceof StringMultipleToIntegerExpression && n.getComparator() == Comparator.NE
                && n.getRightOperand() instanceof IntegerConstant) {
            IntegerConstant right_constant = (IntegerConstant) n.getRightOperand();
            StringMultipleToIntegerExpression left_string_expr = (StringMultipleToIntegerExpression) n.getLeftOperand();

            if (left_string_expr.getOperator() == Operator.INDEXOFCI && right_constant.getConcreteValue() == -1L) {

                Expression<?> theSymbolicString = left_string_expr.getLeftOperand();
                Expression<?> theSymbolicChar = left_string_expr.getRightOperand();
                Expression<?> theOffset = left_string_expr.getOther().get(0);

                // check theString.lenght>0
                String theConcreteString = (String) theSymbolicString.accept(exprExecutor, null);
                Long theConcreteOffset = (Long) theOffset.accept(exprExecutor, null);

                if (theConcreteOffset > theConcreteString.length() - 1) {
                    // if the remaining string is empty, then the branch
                    // distance is maximum since no char can be modified to
                    // satisfy the constraint
                    return Long.MAX_VALUE;
                } else {
                    char theConcreteChar = (char) ((Long) theSymbolicChar.accept(exprExecutor, null)).longValue();
                    char[] charArray = theConcreteString
                            .substring(theConcreteOffset.intValue()).toCharArray();
                    int min_distance_to_char = Integer.MAX_VALUE;
                    for (char c : charArray) {
                        if (Math.abs(c - theConcreteChar) < min_distance_to_char) {
                            min_distance_to_char = Math.abs(c - theConcreteChar);
                        }

                    }
                    return min_distance_to_char;
                }
            }
        }

        return -1;
    }

    private static double getStringDistance(HasMoreTokensExpr hasMoreTokensExpr) {
        TokenizerExpr tokenizerExpr = hasMoreTokensExpr.getTokenizerExpr();

        StringValue string = tokenizerExpr.getString();
        StringValue delimiter = tokenizerExpr.getDelimiter();
        int nextTokenCount = tokenizerExpr.getNextTokenCount();

        ExpressionEvaluator exprExecutor = new ExpressionEvaluator();
        String concreteString = (String) string.accept(exprExecutor, null);
        String concreteDelimiter = (String) delimiter.accept(exprExecutor, null);

        if (concreteString.length() < concreteDelimiter.length() * nextTokenCount) {
            // not enough characters in original string to perform so many
            // nextToken operations
            return Double.MAX_VALUE;
        }

        StringTokenizer tokenizer = new StringTokenizer(concreteString, concreteDelimiter);
        Vector<String> tokens = new Vector<>();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }

        if (tokens.size() > nextTokenCount) {
            // we already have enough tokens to make n true
            return 0;
        } else {
            return StrEquals("", concreteDelimiter);
        }
    }

    private static double getStringDistance(StringBinaryComparison comparison) {
        try {
            ExpressionEvaluator exprExecutor = new ExpressionEvaluator();
            String first = (String) comparison.getLeftOperand().accept(exprExecutor, null);
            String second = (String) comparison.getRightOperand().accept(exprExecutor, null);

            switch (comparison.getOperator()) {
                case EQUALSIGNORECASE:
                    return StrEqualsIgnoreCase(first, second);
                case EQUALS:
                    log.debug("Edit distance between " + first + " and " + second + " is: " + StrEquals(first, second));
                    return StrEquals(first, second);
                case ENDSWITH:
                    return StrEndsWith(first, second);
                case CONTAINS:
                    return StrContains(first, second);
                case PATTERNMATCHES:
                    return RegexMatches(second, first);

                case APACHE_ORO_PATTERN_MATCHES:
                    return RegexMatches(second, first);

                default:
                    log.warn("StringComparison: unimplemented operator!" + comparison.getOperator());
                    return Double.MAX_VALUE;
            }
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    private static double StrContains(String val, CharSequence subStr) {
        int val_length = val.length();
        int subStr_length = subStr.length();
        double min_dist = Double.MAX_VALUE;
        String sub = subStr.toString();

        if (subStr_length > val_length) {
            return avmDistance(val, sub);
            // return editDistance(val, sub);
        } else {
            int diff = val_length - subStr_length;
            for (int i = 0; i < diff + 1; i++) {
                double res = StrEquals(val.substring(i, subStr_length + i), sub);
                if (res < min_dist) {
                    min_dist = res;
                }
            }
        }
        return min_dist;
    }

    private static double StrEndsWith(String value, String suffix) {
        int len = Math.min(suffix.length(), value.length());
        String val1 = value.substring(value.length() - len);
        return StrEquals(val1, suffix);
    }

    private static double avmDistance(String s, String t) {
        double distance = Math.abs(s.length() - t.length());
        int max = Math.min(s.length(), t.length());
        for (int i = 0; i < max; i++) {
            distance += Constraint.normalize(Math.abs(s.charAt(i) - t.charAt(i)));
        }
        return distance;
    }

    private static double getStringDistance(StringMultipleComparison comparison) {
        try {
            ExpressionEvaluator exprExecutor = new ExpressionEvaluator();

            String first = (String) comparison.getLeftOperand().accept(exprExecutor, null);
            String second = (String) comparison.getRightOperand().accept(exprExecutor, null);

            switch (comparison.getOperator()) {
                case STARTSWITH:
                    long start = (Long) comparison.getOther().get(0).accept(exprExecutor, null);
                    return StrStartsWith(first, second, (int) start);
                case REGIONMATCHES:
                    long frstStart = (Long) comparison.getOther().get(0).accept(exprExecutor, null);
                    long secStart = (Long) comparison.getOther().get(1).accept(exprExecutor, null);
                    long length = (Long) comparison.getOther().get(2).accept(exprExecutor, null);
                    long ignoreCase = (Long) comparison.getOther().get(3).accept(exprExecutor, null);

                    return StrRegionMatches(first, (int) frstStart, second, (int) secStart, (int) length, ignoreCase != 0);
                default:
                    log.warn("StringComparison: unimplemented operator!" + comparison.getOperator());
                    return Double.MAX_VALUE;
            }
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    private static double StrRegionMatches(String value, int thisStart, String string, int start, int length,
                                           boolean ignoreCase) {
        if (value == null || string == null)
            throw new NullPointerException();

        if (start < 0 || string.length() - start < length) {
            return length - string.length() + start;
        }

        if (thisStart < 0 || value.length() - thisStart < length) {
            return length - value.length() + thisStart;
        }
        if (length <= 0) {
            return 0;
        }

        String s1 = value;
        String s2 = string;
        if (ignoreCase) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
        }

        String substring1 = s1.substring(thisStart, thisStart + length);
        String substring2 = s2.substring(start, start + length);
        return StrEquals(substring1, substring2);
    }

    private static double StrEqualsIgnoreCase(String first, String second) {
        return StrEquals(first.toLowerCase(), second.toLowerCase());
    }

    private static double StrEquals(String first, Object second) {
        if (first.equals(second))
            return 0; // Identical
        else {
            return avmDistance(first, second.toString());
            // return editDistance(first, second.toString());
        }
    }

    private static double StrStartsWith(String value, String prefix, int start) {
        int len = Math.min(prefix.length(), value.length());
        int end = (start + len > value.length()) ? value.length() : start + len;
        return StrEquals(value.substring(start, end), prefix);
    }

    private static double RegexMatches(String val, String regex) {
        return RegexDistanceUtils.getDistanceTailoredForStringAVM(val, regex);
    }

}
