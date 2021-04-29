package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation;

import java.util.Arrays;

@SuppressWarnings("unused")
public class BooleanToIntUtil {

    private static final int DOUBLE_CMP_NAN_RESULT = 1;
    private static final int FLOAT_CMP_NAN_RESULT = 1;
    private static final boolean OBJECT_CMP_CERTAINTY_MAX = true;
    private static final int OBJECT_CMP_RESULT_TRUE;
    private static final int OBJECT_CMP_RESULT_FALSE;
    static{
        if(OBJECT_CMP_CERTAINTY_MAX) {
            OBJECT_CMP_RESULT_TRUE = Integer.MAX_VALUE; // 1;
            OBJECT_CMP_RESULT_FALSE = Integer.MIN_VALUE; // 0;
        } else {
            OBJECT_CMP_RESULT_TRUE = 1;
            OBJECT_CMP_RESULT_FALSE = 0;
        }
    }


    private BooleanToIntUtil() {

    }


    public static boolean fromInt(int value) {
        return value > 0;
    }

    public static int toInt(boolean b) {
        return b ? 1 : 0;
    }

    public static int lAnd(int[] ints) {
        return Arrays.stream(ints).reduce(Math::min).orElse(Integer.MAX_VALUE);
    }

    private static int binaryLAnd(int a, int b) {
        return Math.min(a, b);
    }


    public static int binaryLxor(int a, int b) {
        /*if (Math.signum(a) == Math.signum(b)) {
            return -Math.abs(a - b);
        } else {
            return Math.max(a, b);
        }*/
        return Math.signum(a) == Math.signum(b) ? -Math.abs(a - b) : Math.max(a, b);
    }

    public static int binaryLor(int a, int b) {
        if(a > 0 || b > 0){
            return Math.max(a,b);
        } else {
            return Math.min(a,b);
        }
        //return Math.max(a, b);
    }

    public static int lOr(int[] ints) {
        return Arrays.stream(ints).reduce(BooleanToIntUtil::binaryLor).orElse(Integer.MIN_VALUE);
    }

    public static int lXor(int[] ints) {
        return Arrays.stream(ints).reduce(BooleanToIntUtil::binaryLxor).
                orElse(0);
    }

    public static int neg(int i) {
        if (i == Integer.MIN_VALUE || i -1 == Integer.MIN_VALUE)
            return Integer.MAX_VALUE;
        return -(i - 1);
    }

    public static int intCmpEqBoolean(int int1, int int2) {
        return -Math.abs(int1 - int2) + 1;
    }

    public static int intCmpNeBoolean(int int1, int int2) {
        return Math.abs(int1 - int2);
    }

    public static int intCmpLeBoolean(int int1, int int2) {
        return intCmpGeBoolean(int2, int1);
    }

    public static int intCmpLtBoolean(int int1, int int2) {
        return intCmpGtBoolean(int2, int1);
    }

    public static int intCmpGtBoolean(int int1, int int2) {
        return int1 - int2;
    }

    public static int intCmpGeBoolean(int int1, int int2) {
        if(int1 - int2 == Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return int1 - int2 + 1;
    }

    public static int ifEqBoolean(int i) {
        return -Math.abs(i) + 1;
    }

    public static int ifNeBoolean(int i) {
        return Math.abs(i);
    }

    public static int ifLeBoolean(int i) {
        if(i == Integer.MIN_VALUE){
            return Integer.MAX_VALUE;
        }
        if(-i == Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return -i + 1;
    }

    public static int ifLtBoolean(int i) {
        if(Integer.MIN_VALUE == i)
            return Integer.MAX_VALUE;
        return -i;
    }

    public static int ifGeBoolean(int i) {
        if(i == Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return i + 1;
    }

    public static int ifGtBoolean(int i) {
        return i;
    }

    public static int aCmpEq(Object o1, Object o2) {
        return o1 == o2 ? OBJECT_CMP_RESULT_TRUE : OBJECT_CMP_RESULT_FALSE;
    }

    public static int aCmpNe(Object o1, Object o2) {
        return o1 != o2 ? OBJECT_CMP_RESULT_TRUE: OBJECT_CMP_RESULT_FALSE;
    }

    public static int ifNull(Object o) {
        return o == null ? OBJECT_CMP_RESULT_TRUE : OBJECT_CMP_RESULT_FALSE;
    }

    public static int ifNonNull(Object o) {
        return o != null ? OBJECT_CMP_RESULT_TRUE : OBJECT_CMP_RESULT_FALSE;
    }

    public static int cmpBooleanEq(int i1, int i2) {
        if (i1 > 0 && i2 > 0) {
            return Math.min(i1, i2);
        }
        if (i1 <= 0 && i2 <= 0) {
            return neg(Math.max(i1, i2));
        }
        return neg(Math.max(abs(i1), abs(i2)));
    }

    public static int cmpBooleanNe(int i1, int i2) {
        int min = Math.min(abs(i1), abs(i2));
        if (i1 > 0 && i2 <= 0)
            return min;
        if (i1 <= 0 && i2 > 0)
            return min;
        return neg(min);
    }

    public static int abs(int i) {
        if (i <= 0)
            return neg(i);
        return i;
    }

    /**
     * Replaces the instruction dcmpg (Double compare greater).
     * This method returns an integer >= 1 (instead of ==1) if {@param d1} > {@param d2}.
     * The case of {@param d1} == {@param d2} is unchanged and will result in 0. The remaining times,
     * this method will return an integer <= -1 (instead of == -1). If either double is {@value} NaN this method will
     * return a positive integer (The instruction dcmpg returns 1).
     *
     * @param d1 The first double
     * @param d2 The second double
     * @return The comparison between {@param d1} and {@param d2}
     */
    public static int dCmpG(double d1, double d2) {
        if (Double.isNaN(d1) || Double.isNaN(d2)) {
            return DOUBLE_CMP_NAN_RESULT;
        }
        if (d1 == d2) {
            return 0;
        }
        int diff = (int) (d1 - d2);
        if (d1 > d2) {
            if(diff == Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return 1 + diff;
        }
        if(diff == Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        return -1 + diff;
    }

    /**
     * Replaces the instruction dcmpl (Double compare less).
     * This method returns an integer >= 1 (instead of ==1) if {@param d1} > {@param d2}.
     * The case of {@param d1} == {@param d2} is unchanged and will result in 0. The remaining times,
     * this method will return an integer <= -1 (instead of == -1). If either double is {@value} NaN this method will
     * return a negative integer (The instruction dcmpl returns -1).
     *
     * @param d1 The first double
     * @param d2 The second double
     * @return The comparison between {@param d1} and {@param d2}
     */
    public static int dCmpL(double d1, double d2) {
        if (Double.isNaN(d1) || Double.isNaN(d2)) {
            return -DOUBLE_CMP_NAN_RESULT;
        }
        if (d1 == d2) {
            return 0;
        }
        int diff = (int) (d1 - d2);
        if (d1 > d2) {
            if(diff == Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return 1 + diff;
        }
        if(diff == Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        return -1 + diff;
        // return d1 > d2 ? 1 + diff : -1 + diff;
    }

    /**
     * Replaces the instruction fcmpg (Float compare less).
     * This method returns an integer >= 1 (instead of ==1) if {@param f1} > {@param f2}.
     * The case of {@param f1} == {@param f2} is unchanged and will result in 0. The remaining times,
     * this method will return an integer <= -1 (instead of == -1). If either double is {@value} NaN this method will
     * return a positive integer (The instruction fcmpg returns -1).
     *
     * @param f1 The first double
     * @param f2 The second double
     * @return The comparison between {@param f1} and {@param f2}
     */
    public static int fCmpG(float f1, float f2) {
        if (Float.isNaN(f1) || Float.isNaN(f2))
            return FLOAT_CMP_NAN_RESULT;
        if (f1 == f2)
            return 0;
        int diff = (int) (f1 - f2);
        if (f1 > f2) {
            if(diff == Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return 1 + diff;
        }
        if(diff == Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        return -1 + diff;
    }

    /**
     * Replaces the instruction fcmpg (Float compare less).
     * This method returns an integer >= 1 (instead of ==1) if {@param f1} > {@param f2}.
     * The case of {@param f1} == {@param f2} is unchanged and will result in 0. The remaining times,
     * this method will return an integer <= -1 (instead of == -1). If either double is {@value} NaN this method will
     * return a negative integer (The instruction fcmpl returns -1).
     *
     * @param f1 The first double
     * @param f2 The second double
     * @return The comparison between {@param f1} and {@param f2}
     */
    public static int fCmpL(float f1, float f2) {
        if (Float.isNaN(f1) || Float.isNaN(f2))
            return FLOAT_CMP_NAN_RESULT * -1;
        if (f1 == f2)
            return 0;
        int diff = (int) (f1 - f2);
        if (f1 > f2) {
            if(diff == Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return 1 + diff;
        }
        if(diff == Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        return -1 + diff;
    }

    /**
     * Updates an certainty boolean, that is not written in one branch of a conditional jump, but the other.
     * An example, when this function is used is, whenever a boolean variable is written in the true branch of an if
     * condition but not in the false branch, this method will be called in the false branch. This method only
     * changes how true or false the value is:
     * <p>
     * x > 0 <=> update(x,_,_) > 0
     * x <= 0 <=> update(x,_,_) <= 0
     *
     * @param value        is the current value
     * @param distance     how true/false the distance was
     * @param reassignment the value, that is assigned in the other branch
     */
    public static int update(int value, int distance, int reassignment) {
        if (fromInt(value) == fromInt(reassignment)) {
            return value;
        }
        if (!fromInt(distance)) {
            distance = neg(distance);
        }
        if (fromInt(value)) {
            return updateTrue(value, distance, reassignment);
        } else {
            return updateFalse(value, distance, reassignment);
        }
    }

    /**
     * Computes the certainty after an dependent update, in which the reassigned value is unknown.
     *
     * @param value    current value of the variable.
     * @param distance the dependents update branching condition distance to change.
     * @return the new value of the variable.
     */
    public static int update(int value, int distance) {
        return update(value, distance, neg(value));
    }

    public static int updateFalse(int value, int distance, int reassignment) {
        if (fromInt(value))
            throw new IllegalArgumentException("Expected a false value to update");
        if (!fromInt(reassignment)) {
            // false would be assigned to a already false value.
            // => the comparison has no impact on the final value.
            return value;
        } else {
            // true would be assigned.
            // neg(value) > 0 , distance > 0
            // => binaryLand(...) > 0
            // => neg(...) <= 0

            return neg(binaryLAnd(neg(value), distance));
        }
    }

    public static int updateTrue(int value, int distance, int reassignment) {
        if (!fromInt(value))
            throw new IllegalArgumentException("Expected a true value to update");
        if (fromInt(reassignment)) {
            // true would be assigned to a already true value.
            // the comparison has no impact on the final value.
            return value;
        } else {
            // false would be assigned to the value.
            return binaryLAnd(value, distance);
        }
    }

    public static int enforcePositiveConstraint(int i) {
        return Math.max(i,1);
    }

    public static int enforceNegativeConstraint(int i) {
        return Math.min(i, 0);
    }
}
