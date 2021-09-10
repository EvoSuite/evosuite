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
package org.evosuite.utils;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.testcase.TestCaseUpdater;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ArrayUtil {

    public static final char OPEN_SQUARE_BRACKET = '[';
    public static final String INPUT_OBJECT_MUST_BE_AN_ARRAY_EXCEPTION_MESSAGE = "Input object must be an array.";

    /**
     * <p>asSet</p>
     *
     * @param values a T object.
     * @param <T>    a T object.
     * @return a {@link java.util.Set} object.
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> asSet(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    /**
     * Constant <code>DEFAULT_JOIN_SEPARATOR="IterUtil.DEFAULT_JOIN_SEPARATOR"</code>
     */
    public static final String DEFAULT_JOIN_SEPARATOR = IterUtil.DEFAULT_JOIN_SEPARATOR;

    /**
     * <p>box</p>
     *
     * @param array an array of int.
     * @return an array of {@link java.lang.Integer} objects.
     */
    public static Integer[] box(int[] array) {
        Integer[] result = new Integer[array.length];

        /* Can't use System.arraycopy() -- it doesn't do boxing */
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }

        return result;
    }

    /**
     * <p>box</p>
     *
     * @param array an array of long.
     * @return an array of {@link java.lang.Long} objects.
     */
    public static Long[] box(long[] array) {
        Long[] result = new Long[array.length];

        /* Can't use System.arraycopy() -- it doesn't do boxing */
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }

        return result;
    }

    /**
     * <p>box</p>
     *
     * @param array an array of byte.
     * @return an array of {@link java.lang.Byte} objects.
     */
    public static Byte[] box(byte[] array) {
        Byte[] result = new Byte[array.length];

        /* Can't use System.arraycopy() -- it doesn't do boxing */
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }

        return result;
    }

    /**
     * <p>join</p>
     *
     * @param array     an array of {@link java.lang.Object} objects.
     * @param separator a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(Object[] array, String separator) {
        return StringUtils.join(array, separator);
    }

    /**
     * <p>join</p>
     *
     * @param array an array of {@link java.lang.Object} objects.
     * @return a {@link java.lang.String} object.
     */
    public static String join(Object[] array) {
        return StringUtils.join(array, DEFAULT_JOIN_SEPARATOR);
    }

    /**
     * <p>join</p>
     *
     * @param array     an array of long.
     * @param separator a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(long[] array, String separator) {
        return join(box(array), separator);
    }

    /**
     * <p>join</p>
     *
     * @param array an array of long.
     * @return a {@link java.lang.String} object.
     */
    public static String join(long[] array) {
        return join(array, DEFAULT_JOIN_SEPARATOR);
    }

    /**
     * <p>join</p>
     *
     * @param array     an array of byte.
     * @param separator a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(byte[] array, String separator) {
        return join(box(array), separator);
    }

    /**
     * <p>join</p>
     *
     * @param array an array of byte.
     * @return a {@link java.lang.String} object.
     */
    public static String join(byte[] array) {
        return join(array, DEFAULT_JOIN_SEPARATOR);
    }

    /**
     * <p>contains</p>
     *
     * @param array
     * @param object
     * @return true if array contains an instance equals to object
     */
    public static boolean contains(Object[] array, Object object) {
        for (Object obj : array) {
            if (obj == object)
                return true;
            else if (obj != null && obj.equals(object))
                return true;
            else if (object instanceof String && obj.toString().equals(object))
                // TODO: Does this check really make sense?
                return true;
        }
        return false;
    }

    /**
     * @param arr
     * @param obj
     * @return
     */
    public static Object[] append(Object[] arr, Object obj) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = obj;
        return arr;
    }

    /**
     * Calculates the amount of dimensions the array contains.
     * <p>
     * ClassName for arrays contains one '[' for each dimension as a prefix of the class name,
     * we just count them. (i.e. int[][] arr contains "[[I" as a class name).
     *
     * @param arr
     * @return
     */
    public static int getDimensions(Object arr) {
        if (!arr.getClass().isArray()) {
            throw new IllegalArgumentException(INPUT_OBJECT_MUST_BE_AN_ARRAY_EXCEPTION_MESSAGE);
        }

        String className = arr.getClass().getName();
        return 1 + className.lastIndexOf(OPEN_SQUARE_BRACKET);
    }

    /**
     * Recovers the array lengths for each dimension.
     *
     * @param arr
     * @return
     */
    public static int[] getArrayLengths(Object arr) {
        if (!arr.getClass().isArray()) {
            throw new IllegalArgumentException(INPUT_OBJECT_MUST_BE_AN_ARRAY_EXCEPTION_MESSAGE);
        }

        int dimensions = ArrayUtil.getDimensions(arr);
        int[] lengths = new int[dimensions];

        Object array = arr;
        for (int dimension = 0; dimension < dimensions; dimension++) {
            lengths[dimension] = Array.getLength(array);

            // We don't want to access the last one as it's not an array element
            if (dimension < dimensions - 1) array = Array.get(array, 0);
        }

        return lengths;
    }

    public static String buildArrayIndexName(String arrayName, List<Integer> indices) {
        String result = arrayName;
        for (int index : indices) {
            result += "[" + index + "]";
        }
        return result;
    }

    /**
     * Creates the lengths array required to create the array referenes
     *
     * @param argumentType
     * @return
     */
    public static int[] buildDimensionsArray(Type argumentType) {
        int dimensions = argumentType.getDimensions();
        int[] lengths = new int[dimensions];

        for (int dimension = 0; dimension < dimensions; ++dimension) {
            lengths[dimension] = 0;
        }

        return lengths;
    }

    /**
     * Creates the random lengths array required to create the array referenes
     *
     * @param argumentType
     * @return
     */
    public static int[] buildRandomDimensionsArray(Type argumentType) {
        int dimensions = argumentType.getDimensions();
        int[] lengths = new int[dimensions];

        for (int dimension = 0; dimension < dimensions; ++dimension) {
            lengths[dimension] = Randomness.nextInt(TestCaseUpdater.ARRAY_DIMENSION_LOWER_BOUND, TestCaseUpdater.DEFAULT_ARRAY_LENGTH_UPPER_BOUND);
        }

        return lengths;
    }


    public static Object createArrayCopy(Object originalArray) {
        int length = Array.getLength(originalArray);
        Object copyArr = Array.newInstance(originalArray.getClass().getComponentType(), length);

        System.arraycopy(originalArray, 0, copyArr, 0, length);

        return copyArr;
    }

    public static class MultiDimensionalArrayIterator {
        private final Object array;

        private final int[] lengths;
        private final int[] currentPositions;
        private boolean hasNext;

        public MultiDimensionalArrayIterator(Object array) {
            this.hasNext = true;
            this.lengths = ArrayUtil.getArrayLengths(array);
            this.currentPositions = new int[lengths.length];
            this.array = array;
        }

        /**
         * Obtains the next element and iterates over the array updating the internal state of the indexes.
         * <p>
         * Precondition: hasNext has been checked before calling (access is inbounds).
         *
         * @return
         */
        public Object getNextElement() {
            Object element = array;

            //In the last access the array becomes the element itself.
            for (int index : currentPositions) {
                element = Array.get(element, index);
            }

            // iterates
            iterateIndexes();

            return element;
        }

        /**
         * Returns whether the iterator has a next value.
         *
         * @return
         */
        public boolean hasNext() {
            return hasNext;
        }

        /**
         * Returns the current indexes
         *
         * @return
         */
        public int[] getCurrentIndex() {
            return currentPositions.clone();
        }

        /**
         * Iterates to the next position in the array
         * <p>
         * *** General Algorithm **
         * if current dimension has maxed out its value
         * if last dimension has been reached
         * Then we checked all the elements -> IndexOutOfBounds
         * else we can reset the value of this dimension and move on to the next one.
         * else we can update the current dimension and finish
         */
        private void iterateIndexes() {
            boolean changeableIndexFound = false;

            int index = 0;
            while (!changeableIndexFound) {

                // Last this dimension is in the last position
                if (currentPositions[index] == lengths[index] - 1) {

                    // We are in the last dimension
                    if (index == lengths.length - 1) {
                        currentPositions[index] = currentPositions[index] + 1;
                        changeableIndexFound = true;
                    } else {
                        currentPositions[index] = 0;
                        index++;
                    }

                    // We are in an overflow
                } else if (currentPositions[index] == lengths[index]) {
                    throw new IndexOutOfBoundsException();

                    // We are in an updateable position
                } else {
                    currentPositions[index] = currentPositions[index] + 1;
                    changeableIndexFound = true;
                }
            }

            checkHasNext(index);
        }

        /**
         * Checks whether the iterator has more elements ot check
         *
         * @param lastIndexUpdated
         */
        private void checkHasNext(int lastIndexUpdated) {
            if (lastIndexUpdated == currentPositions.length - 1
                    && currentPositions[lastIndexUpdated] == lengths[lastIndexUpdated]) {
                hasNext = false;
            }
        }
    }
}
