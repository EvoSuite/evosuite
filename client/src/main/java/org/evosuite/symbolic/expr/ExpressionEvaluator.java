/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with EvoSuite. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.expr;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.evosuite.symbolic.expr.bv.*;
import org.evosuite.symbolic.expr.fp.*;
import org.evosuite.symbolic.expr.reader.StringReaderExpr;
import org.evosuite.symbolic.expr.ref.GetFieldExpression;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.ref.array.ArrayConstant;
import org.evosuite.symbolic.expr.ref.array.ArraySelect;
import org.evosuite.symbolic.expr.ref.array.ArrayStore;
import org.evosuite.symbolic.expr.ref.array.ArrayVariable;
import org.evosuite.symbolic.expr.reftype.LambdaSyntheticType;
import org.evosuite.symbolic.expr.reftype.LiteralClassType;
import org.evosuite.symbolic.expr.reftype.LiteralNullType;
import org.evosuite.symbolic.expr.str.*;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.expr.token.NextTokenizerExpr;
import org.evosuite.symbolic.expr.token.StringNextTokenExpr;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * returns the concrete evaluation for a given expression.
 * <p>
 * NOTE (ilebrero): The arrays implementation may be a bit expensive.
 *
 * @author galeotti
 */
public class ExpressionEvaluator implements ExpressionVisitor<Object, Void> {

    private static final long TRUE_VALUE = 1L;
    private static final long FALSE_VALUE = 0L;
    protected static final Logger log = LoggerFactory.getLogger(ExpressionEvaluator.class);
    public static final String IMPLEMENT_ME = "Implement me.";

    @Override
    public Object visit(IntegerBinaryExpression n, Void arg) {
        Long leftLong = (Long) n.getLeftOperand().accept(this, null);
        Long rightLong = (Long) n.getRightOperand().accept(this, null);

        long leftVal = leftLong;
        long rightVal = rightLong;

        Operator op = n.getOperator();
        switch (op) {

            case SHL:
                return leftVal << rightVal;
            case SHR:
                return leftVal >> rightVal;
            case USHR:
                return leftVal >>> rightVal;
            case AND:
            case IAND:
                return leftVal & rightVal;
            case OR:
            case IOR:
                return leftVal | rightVal;
            case XOR:
            case IXOR:
                return leftVal ^ rightVal;
            case DIV:
                return leftVal / rightVal;
            case MUL:
                return leftVal * rightVal;
            case MINUS:
                return leftVal - rightVal;
            case PLUS:
                return leftVal + rightVal;
            case REM:
                return leftVal % rightVal;
            case MAX:
                return Math.max(leftVal, rightVal);
            case MIN:
                return Math.min(leftVal, rightVal);

            default:
                log.warn("IntegerBinaryExpression: unimplemented operator: " + op);
                return null;
        }

    }

    @Override
    public Object visit(IntegerComparison n, Void arg) {
        log.warn("IntegerComparison.execute() invokation");
        throw new IllegalStateException("This method should not be invoked");
    }

    @Override
    public Object visit(IntegerConstant n, Void arg) {
        return n.getConcreteValue();
    }

    @Override
    public Object visit(IntegerUnaryExpression n, Void arg) {

        Long longObject = (Long) n.getOperand().accept(this, null);

        long leftVal = longObject;

        Operator op = n.getOperator();
        switch (op) {

            case NEG:
                return -leftVal;
            case ABS:
                return Math.abs(leftVal);
            case GETNUMERICVALUE:
                return (long) Character.getNumericValue((char) leftVal);
            case ISLETTER:
                return Character.isLetter((char) leftVal) ? TRUE_VALUE : FALSE_VALUE;
            case ISDIGIT:
                return Character.isDigit((char) leftVal) ? TRUE_VALUE : FALSE_VALUE;

            default:
                log.warn("IntegerUnaryExpression: unimplemented operator: " + op);
                return null;
        }
    }

    @Override
    public Object visit(IntegerVariable n, Void arg) {
        return n.getConcreteValue();
    }

    @Override
    public Object visit(RealComparison n, Void arg) {
        log.warn("RealComparison.execute() invokation");
        throw new IllegalStateException("This method should not be invoked");
    }

    @Override
    public Object visit(RealToIntegerCast n, Void arg) {
        Double doubleObject = (Double) n.getArgument().accept(this, null);
        return doubleObject.longValue();
    }

    @Override
    public Object visit(RealUnaryToIntegerExpression n, Void arg) {
        Double doubleObject = (Double) n.getOperand().accept(this, null);
        double leftVal = doubleObject;

        Operator op = n.getOperator();
        switch (op) {

            case ROUND:
                return Math.round(leftVal);
            case GETEXPONENT:
                return (long) Math.getExponent(leftVal);

            default:
                log.warn("IntegerUnaryExpression: unimplemented operator: " + op);
                return null;
        }
    }

    @Override
    public Object visit(StringBinaryComparison n, Void arg) {
        String first = (String) n.getLeftOperand().accept(this, null);
        String second = (String) n.getRightOperand().accept(this, null);

        Operator op = n.getOperator();
        switch (op) {
            case STARTSWITH:
                return first.startsWith(second) ? TRUE_VALUE : FALSE_VALUE;
            case EQUALSIGNORECASE:
                return first.equalsIgnoreCase(second) ? TRUE_VALUE : FALSE_VALUE;
            case EQUALS:
                return first.equals(second) ? TRUE_VALUE : FALSE_VALUE;
            case ENDSWITH:
                return first.endsWith(second) ? TRUE_VALUE : FALSE_VALUE;
            case CONTAINS:
                return first.contains(second) ? TRUE_VALUE : FALSE_VALUE;
            case PATTERNMATCHES:
                return second.matches(first) ? TRUE_VALUE : FALSE_VALUE;
            case APACHE_ORO_PATTERN_MATCHES: {
                Perl5Matcher matcher = new Perl5Matcher();
                Perl5Compiler compiler = new Perl5Compiler();
                Pattern pattern;
                try {
                    pattern = compiler.compile(first);
                } catch (MalformedPatternException e) {
                    throw new RuntimeException(e);
                }
                return matcher.matches(second, pattern) ? TRUE_VALUE : FALSE_VALUE;

            }
            default:
                log.warn("StringComparison: unimplemented operator!" + op);
                return null;
        }

    }

    @Override
    public Object visit(StringBinaryToIntegerExpression n, Void arg) {
        String first = (String) n.getLeftOperand().accept(this, null);
        Object second = n.getRightOperand().accept(this, null);

        Operator op = n.getOperator();
        switch (op) {

            // returns Int
            case COMPARETO: {
                String string = (String) second;
                return (long) first.compareTo(string);
            }
            case COMPARETOIGNORECASE: {
                String string = (String) second;
                return (long) first.compareToIgnoreCase(string);
            }
            case INDEXOFC: {
                long ch = (Long) second;
                return (long) first.indexOf((char) ch);
            }
            case INDEXOFS: {
                String string = (String) second;
                return (long) first.indexOf(string);
            }
            case LASTINDEXOFC: {
                long ch = (Long) second;
                return (long) first.lastIndexOf((char) ch);
            }
            case LASTINDEXOFS: {
                String string = (String) second;
                return (long) first.lastIndexOf(string);
            }
            case CHARAT: {
                int indx = ((Long) second).intValue();
                return (long) first.charAt(indx);
            }
            default:
                log.warn(
                        "StringBinaryToIntegerExpression: unimplemented operator! Operator" + op);
                return null;
        }

    }

    @Override
    public Object visit(StringMultipleComparison n, Void arg) {
        String first = (String) n.getLeftOperand().accept(this, null);
        String second = (String) n.getRightOperand().accept(this, null);
        ArrayList<Expression<?>> other_v = n.getOther();

        Operator op = n.getOperator();
        switch (op) {
            case STARTSWITH:
                long start = (Long) other_v.get(0).accept(this, null);
                return first.startsWith(second, (int) start) ? TRUE_VALUE : FALSE_VALUE;

            case REGIONMATCHES:
                long frstStart = (Long) other_v.get(0).accept(this, null);
                long secStart = (Long) other_v.get(1).accept(this, null);
                long length = (Long) other_v.get(2).accept(this, null);
                long ignoreCase = (Long) other_v.get(3).accept(this, null);

                return first.regionMatches(ignoreCase != 0, (int) frstStart, second, (int) secStart,
                        (int) length) ? TRUE_VALUE : FALSE_VALUE;
            default:
                log.warn("StringMultipleComparison: unimplemented operator!");
                return null;
        }
    }

    @Override
    public Object visit(StringMultipleToIntegerExpression n, Void arg) {
        String first = (String) n.getLeftOperand().accept(this, null);
        Object second = n.getRightOperand().accept(this, null);
        ArrayList<Expression<?>> other_v = n.getOther();

        long secLong, thrdLong;
        String secStr;

        Operator op = n.getOperator();
        switch (op) {

            // returns int
            case INDEXOFCI:
                secLong = (Long) second;
                thrdLong = (Long) other_v.get(0).accept(this, null);
                return (long) first.indexOf((int) secLong, (int) thrdLong);
            case INDEXOFSI:
                secStr = (String) second;
                thrdLong = (Long) other_v.get(0).accept(this, null);
                return (long) first.indexOf(secStr, (int) thrdLong);
            case LASTINDEXOFCI:
                secLong = (Long) second;
                thrdLong = (Long) other_v.get(0).accept(this, null);
                return (long) first.lastIndexOf((int) secLong, (int) thrdLong);
            case LASTINDEXOFSI:
                secStr = (String) second;
                thrdLong = (Long) other_v.get(0).accept(this, null);
                return (long) first.lastIndexOf(secStr, (int) thrdLong);

            default:
                log.warn("StringMultipleToIntegerExpression: unimplemented operator: " + op);
                return null;
        }
    }

    @Override
    public Object visit(StringToIntegerCast n, Void arg) {
        String str = (String) n.getArgument().accept(this, null);
        return Long.parseLong(str);

    }

    @Override
    public Object visit(StringUnaryToIntegerExpression n, Void arg) {
        String exOn = (String) n.getOperand().accept(this, null);

        Operator op = n.getOperator();
        switch (op) {

            case LENGTH:
                return (long) exOn.length();

            case IS_INTEGER: {
                try {
                    Integer.parseInt(exOn);
                    return TRUE_VALUE;
                } catch (NumberFormatException ex) {
                    return FALSE_VALUE;
                }
            }

            default:
                log.warn("StringUnaryExpression: unimplemented operator!");
                return null;
        }
    }

    @Override
    public Object visit(IntegerToRealCast n, Void arg) {
        Long exprVal = (Long) n.getArgument().accept(this, null);
        return exprVal.doubleValue();
    }

    @Override
    public Object visit(RealBinaryExpression n, Void arg) {

        Double leftDouble = (Double) n.getLeftOperand().accept(this, null);
        Double rightDouble = (Double) n.getRightOperand().accept(this, null);

        double leftVal = leftDouble;
        double rightVal = rightDouble;

        Operator op = n.getOperator();
        switch (op) {

            case DIV:
                return leftVal / rightVal;
            case MUL:
                return leftVal * rightVal;
            case MINUS:
                return leftVal - rightVal;
            case PLUS:
                return leftVal + rightVal;
            case REM:
                return leftVal % rightVal;
            case ATAN2:
                return Math.atan2(leftVal, rightVal);
            case COPYSIGN:
                return Math.copySign(leftVal, rightVal);
            case HYPOT:
                return Math.hypot(leftVal, rightVal);
            case IEEEREMAINDER:
                return Math.IEEEremainder(leftVal, rightVal);
            case MAX:
                return Math.max(leftVal, rightVal);
            case MIN:
                return Math.min(leftVal, rightVal);
            case NEXTAFTER:
                return Math.nextAfter(leftVal, rightVal);
            case POW:
                return Math.pow(leftVal, rightVal);
            case SCALB:
                return Math.scalb(leftVal, (int) rightVal);

            default:
                log.warn("IntegerBinaryExpression: unimplemented operator: " + op);
                return null;
        }

    }

    @Override
    public Object visit(RealConstant n, Void arg) {
        return n.getConcreteValue();
    }

    @Override
    public Object visit(RealUnaryExpression n, Void arg) {
        Double doubleObject = (Double) n.getOperand().accept(this, null);
        double doubleVal = doubleObject;

        Operator op = n.getOperator();
        switch (op) {

            case ABS:
                return Math.abs(doubleVal);
            case ACOS:
                return Math.acos(doubleVal);
            case ASIN:
                return Math.asin(doubleVal);
            case ATAN:
                return Math.atan(doubleVal);
            case CBRT:
                return Math.cbrt(doubleVal);
            case CEIL:
                return Math.ceil(doubleVal);
            case COS:
                return Math.cos(doubleVal);
            case COSH:
                return Math.cosh(doubleVal);
            case EXP:
                return Math.exp(doubleVal);
            case EXPM1:
                return Math.expm1(doubleVal);
            case FLOOR:
                return Math.floor(doubleVal);
            case LOG:
                return Math.log(doubleVal);
            case LOG10:
                return Math.log10(doubleVal);
            case LOG1P:
                return Math.log1p(doubleVal);
            case NEG:
                return -doubleVal;
            case NEXTUP:
                return Math.nextUp(doubleVal);
            case RINT:
                return Math.rint(doubleVal);
            case SIGNUM:
                return Math.signum(doubleVal);
            case SIN:
                return Math.sin(doubleVal);
            case SINH:
                return Math.sinh(doubleVal);
            case SQRT:
                return Math.sqrt(doubleVal);
            case TAN:
                return Math.tan(doubleVal);
            case TANH:
                return Math.tanh(doubleVal);
            case TODEGREES:
                return Math.toDegrees(doubleVal);
            case TORADIANS:
                return Math.toRadians(doubleVal);
            case ULP:
                return Math.ulp(doubleVal);

            default:
                log.warn("RealUnaryExpression: unimplemented operator: " + op);
                return null;
        }

    }

    @Override
    public Object visit(RealVariable n, Void arg) {
        return n.getConcreteValue();
    }

    @Override
    public Object visit(StringReaderExpr n, Void arg) {

        String conc_string = (String) n.getString().accept(this, null);
        if (n.getReaderPosition() >= conc_string.length()) {
            return -TRUE_VALUE;
        } else {
            return (long) conc_string.charAt(n.getReaderPosition());
        }
    }

    @Override
    public Object visit(IntegerToStringCast n, Void arg) {
        Long exprVal = (Long) n.getArgument().accept(this, null);
        return Long.toString(exprVal);
    }

    @Override
    public Object visit(RealToStringCast n, Void arg) {
        Double doubleObject = (Double) n.getArgument().accept(this, null);
        return Double.toString(doubleObject);
    }

    @Override
    public Object visit(StringBinaryExpression n, Void arg) {
        String first = (String) n.getLeftOperand().accept(this, null);
        Object second = n.getRightOperand().accept(this, null);

        Operator op = n.getOperator();
        switch (op) {

            // returns String
            case CONCAT: {
                String string = (String) second;
                return first.concat(string);
            }
            case APPEND_BOOLEAN: {
                Long sndLong = (Long) second;
                boolean booleabValue = sndLong != 0;
                return first + booleabValue;
            }
            case APPEND_CHAR: {
                Long sndLong = (Long) second;
                char charValue = (char) sndLong.longValue();
                return first + charValue;
            }
            case APPEND_INTEGER: {
                Long sndLong = (Long) second;
                return first + sndLong;
            }
            case APPEND_REAL: {
                Double sndLong = (Double) second;
                return first + sndLong;
            }
            case APPEND_STRING: {
                String string = (String) second;
                return first + (string);
            }

            default:
                log.warn("StringBinaryExpression: unimplemented operator! Operator" + op);
                return null;
        }
    }

    @Override
    public Object visit(StringConstant n, Void arg) {
        return n.getConcreteValue();
    }

    @Override
    public Object visit(StringMultipleExpression n, Void arg) {
        String first = (String) n.getLeftOperand().accept(this, null);
        Object right = n.getRightOperand().accept(this, null);
        ArrayList<Expression<?>> other_v = n.getOther();
        long secLong, thrdLong;
        String secStr, thrdStr;

        Operator op = n.getOperator();
        switch (op) {

            // returns string
            case SUBSTRING: {
                secLong = (Long) right;
                thrdLong = (Long) other_v.get(0).accept(this, null);
                return first.substring((int) secLong, (int) thrdLong);
            }
            case REPLACEC:
                secLong = (Long) right;
                thrdLong = (Long) other_v.get(0).accept(this, null);
                return first.replace((char) secLong, (char) thrdLong);
            case REPLACECS:
                secStr = (String) right;
                thrdStr = (String) other_v.get(0).accept(this, null);
                return first.replace(secStr, thrdStr);
            case REPLACEALL:
                secStr = (String) right;
                thrdStr = (String) other_v.get(0).accept(this, null);
                return first.replaceAll(secStr, thrdStr);
            case REPLACEFIRST:
                secStr = (String) right;
                thrdStr = (String) other_v.get(0).accept(this, null);
                return first.replaceFirst(secStr, thrdStr);
            default:
                log.warn("StringMultipleExpression: unimplemented operator: " + op);
                return null;
        }

    }

    @Override
    public Object visit(StringUnaryExpression n, Void arg) {
        String exOn = (String) n.getOperand().accept(this, null);

        Operator op = n.getOperator();
        switch (op) {

            case TOLOWERCASE:
                return exOn.toLowerCase();
            case TOUPPERCASE:
                return exOn.toUpperCase();
            case TRIM:
                return exOn.trim();

            default:
                log.warn("StringUnaryExpression: unimplemented operator!" + op);
                return null;
        }
    }

    @Override
    public Object visit(StringVariable n, Void arg) {
        return n.getConcreteValue();
    }

    @Override
    public Object visit(HasMoreTokensExpr n, Void arg) {
        StringTokenizer tokenizer = (StringTokenizer) n.getTokenizerExpr().accept(this, null);
        return tokenizer.hasMoreTokens() ? TRUE_VALUE : FALSE_VALUE;
    }

    @Override
    public Object visit(NewTokenizerExpr n, Void arg) {
        String stringVal = (String) n.getString().accept(this, null);
        String delimVal = (String) n.getDelimiter().accept(this, null);
        StringTokenizer tokenizer = new StringTokenizer(stringVal, delimVal);
        return tokenizer;
    }

    @Override
    public Object visit(NextTokenizerExpr n, Void arg) {
        StringTokenizer tokenizer = (StringTokenizer) n.getTokenizerExpr().accept(this, null);
        tokenizer.nextToken();
        return tokenizer;
    }

    @Override
    public Object visit(StringNextTokenExpr n, Void arg) {
        StringTokenizer tokenizer = (StringTokenizer) n.getTokenizerExpr().accept(this, null);
        return tokenizer.nextToken();
    }

    @Override
    public Object visit(ReferenceConstant r, Void arg) {
        return r.getConcreteValue();
    }

    @Override
    public Object visit(ReferenceVariable r, Void arg) {
        return r.getConcreteValue();
    }

    @Override
    public Object visit(GetFieldExpression r, Void arg) {
        final Object conc_receiver = r.getReceiverExpr().accept(this, arg);
        final String field_name = r.getFieldName();
        if (conc_receiver == null) {
            // TODO
            throw new UnsupportedOperationException("How the null case should be handled?");
        }
        try {
            Field field = conc_receiver.getClass().getField(field_name);
            final boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            Object ret_value = field.get(conc_receiver);
            field.setAccessible(isAccessible);
            return ret_value;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object visit(ArrayStore.IntegerArrayStore r, Void arg) {
        Object array = r.getSymbolicArray().accept(this, null);
        Long index = (Long) r.getSymbolicIndex().accept(this, null);
        Long value = (Long) r.getSymbolicValue().accept(this, null);

        int intIndex = Math.toIntExact(index);

        // We don't work on the returned array to not update the concrete internal array
        Object newArray = ArrayUtil.createArrayCopy(array);
        Array.set(
                newArray,
                intIndex,
                TypeUtil.convertIntegerTo(value, newArray.getClass().getComponentType().getName())
        );

        return newArray;
    }

    @Override
    public Object visit(ArraySelect.IntegerArraySelect r, Void arg) {
        Object array = r.getSymbolicArray().accept(this, null);
        Long index = (Long) r.getSymbolicIndex().accept(this, null);

        int intIndex = Math.toIntExact(index);

        return TypeUtil.unboxPrimitiveValue(Array.get(array, intIndex));
    }

    @Override
    public Object visit(ArraySelect.RealArraySelect r, Void arg) {
        Object array = r.getSymbolicArray().accept(this, null);
        Long index = (Long) r.getSymbolicIndex().accept(this, null);

        int intIndex = Math.toIntExact(index);

        return TypeUtil.unboxPrimitiveValue(Array.get(array, intIndex));
    }

    @Override
    public Object visit(ArraySelect.StringArraySelect r, Void arg) {
        return null;
    }

    @Override
    public Object visit(ArrayStore.RealArrayStore r, Void arg) {
        Object array = r.getSymbolicArray().accept(this, null);
        Long index = (Long) r.getSymbolicIndex().accept(this, null);
        Double value = (Double) r.getSymbolicValue().accept(this, null);

        int intIndex = Math.toIntExact(index);

        // We don't work on the returned array to not update the concrete internal array
        Object newArray = ArrayUtil.createArrayCopy(array);
        Array.set(newArray, intIndex, TypeUtil.convertRealTo(value, newArray.getClass().getComponentType().getName()));

        return newArray;
    }

    @Override
    public Object visit(ArrayStore.StringArrayStore r, Void arg) {
        throw new UnsupportedOperationException(IMPLEMENT_ME);
    }

    @Override
    public Object visit(ArrayConstant.IntegerArrayConstant r, Void arg) {
        return r.getConcreteValue();
    }

    @Override
    public Object visit(ArrayConstant.RealArrayConstant r, Void arg) {
        return r.getConcreteValue();
    }

    @Override
    public Object visit(ArrayConstant.StringArrayConstant r, Void arg) {
        throw new UnsupportedOperationException(IMPLEMENT_ME);
    }

    @Override
    public Object visit(ArrayConstant.ReferenceArrayConstant r, Void arg) {
        throw new UnsupportedOperationException(IMPLEMENT_ME);
    }

    @Override
    public Object visit(ArrayVariable.IntegerArrayVariable r, Void arg) {
        return r.getConcreteValue();
    }

    @Override
    public Object visit(ArrayVariable.RealArrayVariable r, Void arg) {
        return r.getConcreteValue();
    }

    @Override
    public Object visit(ArrayVariable.StringArrayVariable r, Void arg) {
        throw new UnsupportedOperationException(IMPLEMENT_ME);
    }

    @Override
    public Object visit(ArrayVariable.ReferenceArrayVariable r, Void arg) {
        throw new UnsupportedOperationException(IMPLEMENT_ME);
    }

    @Override
    public Object visit(LambdaSyntheticType r, Void arg) {
        throw new UnsupportedOperationException(IMPLEMENT_ME);
    }

    @Override
    public Object visit(LiteralNullType r, Void arg) {
        throw new UnsupportedOperationException(IMPLEMENT_ME);
    }

    @Override
    public Object visit(LiteralClassType r, Void arg) {
        throw new UnsupportedOperationException(IMPLEMENT_ME);
    }
}
