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

package org.evosuite.instrumentation.testability;

import org.evosuite.Properties;
import org.evosuite.seeding.ConstantPoolManager;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * <p>
 * BooleanHelper class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class BooleanHelper {

    /**
     * Constant <code>K=Integer.MAX_VALUE - 2</code>
     */
    public static final int K = Integer.MAX_VALUE - 2;
    //public static final int K = 1000;

    public static final int TRUE = K;

    public static final int FALSE = -K;

    static Map<Integer, Integer> lastDistance = new HashMap<>();

    /**
     * <p>
     * clearStack
     * </p>
     */
    public static void clearStack() {
        lastDistance.clear();
    }

    /**
     * Helper function that is called instead of Object.equals
     *
     * @param obj1 a {@link java.lang.Object} object.
     * @param obj2 a {@link java.lang.Object} object.
     * @return a int.
     */
    public static int objectEquals(Object obj1, Object obj2) {
        return obj1.equals(obj2) ? TRUE : FALSE;
    }


    /**
     * Keep track of the distance for this predicate
     *
     * @param branchId a int.
     * @param distance a int.
     */
    public static void pushPredicate(int distance, int branchId) {
        lastDistance.put(branchId, Math.abs(distance));
    }

    /**
     * Retrieve the distance of a predicate with its given approximation level
     *
     * @param branchId           a int.
     * @param approximationLevel a int.
     * @param value              a int.
     * @return a int.
     */
    public static int getDistance(int branchId, int approximationLevel, int value) {
        int distance = Integer.MAX_VALUE;
        if (branchId > 0) {
            if (lastDistance.containsKey(branchId)) {
                distance = lastDistance.get(branchId);
            }
        }
        double val = (1.0 + normalize(distance)) / Math.pow(2.0, approximationLevel);

        int d = (int) Math.ceil(K * val);
        if (d == 0)
            d = 1;
        if (value <= 0)
            d = -d;

        return d;
    }

    /**
     * FIXME: the use of this function needs to be clarified
     *
     * @param distance
     * @return
     */
    public static double normalize(int distance) {
        //		double k = K;
        double k = Properties.MAX_INT;
        double d = distance;
        return d / (d + 0.5 * k);
        //return distance / (distance + 1.0);
    }

    /**
     * Replacement function for double comparison
     *
     * @param d1 a double.
     * @param d2 a double.
     * @return a int.
     */
    public static int doubleSubG(double d1, double d2) {
        if (d1 == d2) {
            ConstantPoolManager.getInstance().addDynamicConstant(d1);
            return 0;
        } else {
            // Bytecode spec: If either number is NaN, the integer 1 is pushed onto the stack
            if (Double.isNaN(d1) || Double.isNaN(d2)) {
                return 1;
            }

            return doubleSubHelper(d1, d2);
        }
    }

    public static int doubleSubL(double d1, double d2) {
        if (d1 == d2) {
            ConstantPoolManager.getInstance().addDynamicConstant(d1);
            return 0;
        } else {
            // Bytecode spec: If either number is NaN, the integer -1 is pushed onto the stack
            if (Double.isNaN(d1) || Double.isNaN(d2)) {
                return -1;
            }

            return doubleSubHelper(d1, d2);
        }
    }


    private static int doubleSubHelper(double d1, double d2) {
        if (Double.isInfinite(d1) || Double.isInfinite(d2)) {
            return Double.compare(d1, d2);
        }

        double diff = d1 - d2;
        double diff2 = diff / (1.0 + Math.abs(diff));
        //			int d3 = (int) Math.round(Integer.MAX_VALUE * diff2);
        int d3 = (int) (diff2 < 0 ? Math.floor(Integer.MAX_VALUE * diff2)
                : Math.ceil(Integer.MAX_VALUE * diff2));
        if (d3 == 0)
            d3 = (int) Math.signum(diff);

        ConstantPoolManager.getInstance().addDynamicConstant(d1);
        ConstantPoolManager.getInstance().addDynamicConstant(d2);
        return d3;
    }

    /**
     * Replacement function for float comparison
     *
     * @param f1 a float.
     * @param f2 a float.
     * @return a int.
     */
    public static int floatSubG(float f1, float f2) {
        if (f1 == f2) {
            ConstantPoolManager.getInstance().addDynamicConstant(f1);
            return 0;
        } else {
            // Bytecode spec: If either number is NaN, the integer 1 is pushed onto the stack
            if (Float.isNaN(f1) || Float.isNaN(f2)) {
                return 1;
            }

            return floatSubHelper(f1, f2);
        }
    }

    public static int floatSubL(float f1, float f2) {
        if (f1 == f2) {
            ConstantPoolManager.getInstance().addDynamicConstant(f1);
            return 0;
        } else {
            // Bytecode spec: If either number is NaN, the integer -1 is pushed onto the stack
            if (Float.isNaN(f1) || Float.isNaN(f2)) {
                return -1;
            }
            return floatSubHelper(f1, f2);
        }
    }

    private static int floatSubHelper(float f1, float f2) {

        if (Float.isInfinite(f1) || Float.isInfinite(f2)) {
            return Float.compare(f1, f2);
        }
        double diff = (double) f1 - (double) f2;
        double diff2 = Math.signum(diff) * Math.abs(diff) / (1.0F + Math.abs(diff));
        int d3 = (int) Math.ceil(Integer.MAX_VALUE * diff2);
        if (d3 == 0)
            d3 = (int) Math.signum(diff);
        ConstantPoolManager.getInstance().addDynamicConstant(f1);
        ConstantPoolManager.getInstance().addDynamicConstant(f2);
        return d3;
    }


    /**
     * <p>
     * intSub
     * </p>
     *
     * @param a a int.
     * @param b a int.
     * @return a int.
     */
    public static int intSub(int a, int b) {
        long sub = (long) a - (long) b;
        if (sub < -K)
            return -K;
        else if (sub > K)
            return K;
        return (int) sub;
    }

    /**
     * Replacement function for long comparison
     *
     * @param l1 a long.
     * @param l2 a long.
     * @return a int.
     */
    public static int longSub(long l1, long l2) {
        if (l1 == l2) {
            ConstantPoolManager.getInstance().addDynamicConstant(l1);
            return 0;
        } else {
            double diff = (double) l1 - (double) l2;
            double diff2 = Math.signum(diff) * Math.abs(diff) / (1.0 + Math.abs(diff));
            int d3 = (int) Math.ceil(Integer.MAX_VALUE * diff2);
            ConstantPoolManager.getInstance().addDynamicConstant(l1);
            ConstantPoolManager.getInstance().addDynamicConstant(l2);
            return d3;
        }
    }

    /**
     * <p>
     * fromDouble
     * </p>
     *
     * @param d a double.
     * @return a int.
     */
    @Deprecated
    public static int fromDouble(double d) {
        //logger.info("Converting double " + d);
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else 
		*/
        if (d == 0.0)
            return 0;
        else {
            double d2 = Math.signum(d) * Math.abs(d) / (1.0 + Math.abs(d));
            //logger.info(" -> " + d2);
            int d3 = (int) Math.round(Integer.MAX_VALUE * d2);
            //logger.info(" -> " + d3);
            return d3;
        }
    }

    /**
     * <p>
     * fromFloat
     * </p>
     *
     * @param d a float.
     * @return a int.
     */
    @Deprecated
    public static int fromFloat(float d) {
        //logger.info("Converting float " + d);
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else */
        if (d == 0.0f)
            return 0;
        else {
            float d2 = Math.signum(d) * Math.abs(d) / (1f + Math.abs(d));
            //logger.info(" ->" + d2);
            int d3 = Math.round(Integer.MAX_VALUE * d2);
            //logger.info(" -> " + d3);
            return d3;
        }
    }

    /**
     * <p>
     * fromLong
     * </p>
     *
     * @param d a long.
     * @return a int.
     */
    @Deprecated
    public static int fromLong(long d) {
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
			*/
        //else
        //	return (int) d;
        if (d == 0L)
            return 0;
        double d2 = Math.signum(d) * Math.abs(d) / (1L + Math.abs(d));
        int d3 = (int) Math.round(Integer.MAX_VALUE * d2);
        return d3;
    }

    /**
     * <p>
     * booleanToInt
     * </p>
     *
     * @param b a boolean.
     * @return a int.
     */
    public static int booleanToInt(boolean b) {
        if (b)
            return TRUE;
        else
            return FALSE;
    }

    /**
     * <p>
     * intToBoolean
     * </p>
     *
     * @param x a int.
     * @return a boolean.
     */
    public static boolean intToBoolean(int x) {
        return x > 0;
    }

    /**
     * <p>
     * min
     * </p>
     *
     * @param a a int.
     * @param b a int.
     * @param c a int.
     * @return a int.
     */
    public static int min(int a, int b, int c) {
        if (a < b)
            return Math.min(a, c);
        else
            return Math.min(b, c);
    }

    /**
     * <p>
     * compareBoolean
     * </p>
     *
     * @param a a int.
     * @param b a int.
     * @return a int.
     */
    public static int compareBoolean(int a, int b) {
        if ((a > 0 && b > 0) || (a <= 0 && b <= 0))
            return Math.abs(a - b);
        else
            return -1 * Math.abs(a - b);
    }

    /**
     * <p>
     * editDistance_old
     * </p>
     *
     * @param s a {@link java.lang.String} object.
     * @param t a {@link java.lang.String} object.
     * @return a int.
     */
    public static int editDistance_old(String s, String t) {
        int[][] d; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        int k = 127;

        // Step 1

        n = s.length();
        m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2

        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3

        for (i = 1; i <= n; i++) {

            s_i = s.charAt(i - 1);

            // Step 4

            for (j = 1; j <= m; j++) {

                t_j = t.charAt(j - 1);

                // Step 5

                if (s_i == t_j) {
                    cost = 0;
                } else {
                    //					cost = 127/4 + 3 * Math.abs(s_i - t_j)/4;
                    cost = 127;
                }

                // Step 6

                d[i][j] = min(d[i - 1][j] + k, d[i][j - 1] + k, d[i - 1][j - 1] + cost);

            }

        }

        // Step 7

        return d[n][m];
    }


    /**
     * Replacement function for the Java instanceof instruction, which returns a
     * distance integer
     *
     * @param o a {@link java.lang.Object} object.
     * @param c a {@link java.lang.Class} object.
     * @return a int.
     */
    public static int instanceOf(Object o, Class<?> c) {
        if (o == null)
            return FALSE;
        return c.isAssignableFrom(o.getClass()) ? TRUE : FALSE;
    }

    /**
     * Replacement function for the Java IFNULL instruction, returning a
     * distance integer
     *
     * @param o
     * @param opcode
     * @param opcode a int.
     * @return a int.
     */
    public static int isNull(Object o, int opcode) {
        if (opcode == Opcodes.IFNULL)
            return o == null ? TRUE : FALSE;
        else
            return o != null ? TRUE : FALSE;
    }

    /**
     * <p>
     * IOR
     * </p>
     *
     * @param a a int.
     * @param b a int.
     * @return a int.
     */
    public static int IOR(int a, int b) {
        int ret = 0;
        if (a > 0 || b > 0) {
            // True

            ret = a;
            if (b > 0 && b < a)
                ret = b;
        } else {
            // False

            ret = a;
            if (b > a)
                ret = b;
        }

        return ret;
    }

    /**
     * <p>
     * IAND
     * </p>
     *
     * @param a a int.
     * @param b a int.
     * @return a int.
     */
    public static int IAND(int a, int b) {
        return Math.min(a, b);
    }

    /**
     * <p>
     * IXOR
     * </p>
     *
     * @param a a int.
     * @param b a int.
     * @return a int.
     */
    public static int IXOR(int a, int b) {
        int ret = 0;
        if (a > 0 && b <= 0) {
            // True
            ret = a;
        } else if (b > 0 && a <= 0) {
            ret = b;
        } else {
            // False
            ret = -Math.abs(a - b);
        }

        return ret;
    }

    /**
     * <p>
     * isEqual
     * </p>
     *
     * @param o1     a {@link java.lang.Object} object.
     * @param o2     a {@link java.lang.Object} object.
     * @param opcode a int.
     * @return a int.
     */
    public static int isEqual(Object o1, Object o2, int opcode) {
        if (opcode == Opcodes.IF_ACMPEQ)
            return o1 == o2 ? K : -K;
        else
            return o1 != o2 ? K : -K;
    }

    private static final Stack<Object> parametersObject = new Stack<>();
    private static final Stack<Boolean> parametersBoolean = new Stack<>();
    private static final Stack<Character> parametersChar = new Stack<>();
    private static final Stack<Byte> parametersByte = new Stack<>();
    private static final Stack<Short> parametersShort = new Stack<>();
    private static final Stack<Integer> parametersInteger = new Stack<>();
    private static final Stack<Float> parametersFloat = new Stack<>();
    private static final Stack<Long> parametersLong = new Stack<>();
    private static final Stack<Double> parametersDouble = new Stack<>();

    /**
     * <p>
     * popParameterBooleanFromInt
     * </p>
     *
     * @return a boolean.
     */
    public static boolean popParameterBooleanFromInt() {
        int i = parametersInteger.pop();
        boolean result = i > 0;
        return result;
    }

    /**
     * <p>
     * popParameterIntFromBoolean
     * </p>
     *
     * @return a int.
     */
    public static int popParameterIntFromBoolean() {
        boolean i = parametersBoolean.pop();
        if (i)
            return K;
        else
            return -K;
    }

    /**
     * <p>
     * popParameterBoolean
     * </p>
     *
     * @return a boolean.
     */
    public static boolean popParameterBoolean() {
        return parametersBoolean.pop();
    }

    /**
     * <p>
     * popParameterChar
     * </p>
     *
     * @return a char.
     */
    public static char popParameterChar() {
        return parametersChar.pop();
    }

    /**
     * <p>
     * popParameterByte
     * </p>
     *
     * @return a byte.
     */
    public static byte popParameterByte() {
        return parametersByte.pop();
    }

    /**
     * <p>
     * popParameterShort
     * </p>
     *
     * @return a short.
     */
    public static short popParameterShort() {
        return parametersShort.pop();
    }

    /**
     * <p>
     * popParameterInt
     * </p>
     *
     * @return a int.
     */
    public static int popParameterInt() {
        return parametersInteger.pop();
    }

    /**
     * <p>
     * popParameterFloat
     * </p>
     *
     * @return a float.
     */
    public static float popParameterFloat() {
        return parametersFloat.pop();
    }

    /**
     * <p>
     * popParameterLong
     * </p>
     *
     * @return a long.
     */
    public static long popParameterLong() {
        return parametersLong.pop();
    }

    /**
     * <p>
     * popParameterDouble
     * </p>
     *
     * @return a double.
     */
    public static double popParameterDouble() {
        return parametersDouble.pop();
    }

    /**
     * <p>
     * popParameterObject
     * </p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public static Object popParameterObject() {
        return parametersObject.pop();
    }

    /**
     * <p>
     * popParameter
     * </p>
     *
     * @param o a {@link java.lang.Object} object.
     * @return a {@link java.lang.Object} object.
     */
    public static Object popParameter(Object o) {
        return parametersObject.pop();
    }

    /**
     * <p>
     * pushParameter
     * </p>
     *
     * @param o a boolean.
     */
    public static void pushParameter(boolean o) {
        parametersBoolean.push(o);
    }

    /**
     * <p>
     * pushParameter
     * </p>
     *
     * @param o a char.
     */
    public static void pushParameter(char o) {
        parametersChar.push(o);
    }

    /**
     * <p>
     * pushParameter
     * </p>
     *
     * @param o a byte.
     */
    public static void pushParameter(byte o) {
        parametersByte.push(o);
    }

    /**
     * <p>
     * pushParameter
     * </p>
     *
     * @param o a short.
     */
    public static void pushParameter(short o) {
        parametersShort.push(o);
    }

    /**
     * <p>
     * pushParameter
     * </p>
     *
     * @param o a int.
     */
    public static void pushParameter(int o) {
        parametersInteger.push(o);
    }

    /**
     * <p>
     * pushParameter
     * </p>
     *
     * @param o a float.
     */
    public static void pushParameter(float o) {
        parametersFloat.push(o);
    }

    /**
     * <p>
     * pushParameter
     * </p>
     *
     * @param o a long.
     */
    public static void pushParameter(long o) {
        parametersLong.push(o);
    }

    /**
     * <p>
     * pushParameter
     * </p>
     *
     * @param o a double.
     */
    public static void pushParameter(double o) {
        parametersDouble.push(o);
    }

    /**
     * <p>
     * pushParameter
     * </p>
     *
     * @param o a {@link java.lang.Object} object.
     */
    public static void pushParameter(Object o) {
        parametersObject.push(o);
    }
}
