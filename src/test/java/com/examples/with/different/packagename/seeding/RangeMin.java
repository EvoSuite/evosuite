/*
 * Exact copy of class org.apache.commons.lang3.Range
 */
package com.examples.with.different.packagename.seeding;

import java.io.Serializable;
import java.util.Comparator;


/**
 * <p>An immutable range of objects from a minimum to maximum point inclusive.</p>
 * 
 * <p>The objects need to either be implementations of {@code Comparable}
 * or you need to supply a {@code Comparator}. </p>
 *
 * <p>#ThreadSafe# if the objects and comparator are thread-safe</p>
 * 
 * @since 3.0
 * @version $Id: Range.java 1199894 2011-11-09 17:53:59Z ggregory $
 */
public final class RangeMin<T> implements Serializable {

    /**
     * Serialization version.
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ordering scheme used in this range.
     */
    private final Comparator<T> comparator;
    /**
     * The minimum value in this range (inclusive).
     */
    private final T minimum;
    /**
     * The maximum value in this range (inclusive).
     */
    private final T maximum;
    /**
     * Cached output hashCode (class is immutable).
     */
    private transient int hashCode;
    /**
     * Cached output toString (class is immutable).
     */
    private transient String toString;

    /**
     * <p>Obtains a range using the specified element as both the minimum
     * and maximum in this range.</p>
     * 
     * <p>The range uses the natural ordering of the elements to determine where
     * values lie in the range.</p>
     *
     * @param <T> the type of the elements in this range
     * @param element  the value to use for this range, not null
     * @return the range object, not null
     * @throws IllegalArgumentException if the element is null
     * @throws ClassCastException if the element is not {@code Comparable}
     */
    public static <T extends Comparable<T>> RangeMin<T> is(T element) {
        return between(element, element, null);
    }

    /**
     * <p>Obtains a range using the specified element as both the minimum
     * and maximum in this range.</p>
     * 
     * <p>The range uses the specified {@code Comparator} to determine where
     * values lie in the range.</p>
     *
     * @param <T> the type of the elements in this range
     * @param element  the value to use for this range, must not be {@code null}
     * @param comparator  the comparator to be used, null for natural ordering
     * @return the range object, not null
     * @throws IllegalArgumentException if the element is null
     * @throws ClassCastException if using natural ordering and the elements are not {@code Comparable}
     */
    public static <T> RangeMin<T> is(T element, Comparator<T> comparator) {
        return between(element, element, comparator);
    }

    /**
     * <p>Obtains a range with the specified minimum and maximum values (both inclusive).</p>
     * 
     * <p>The range uses the natural ordering of the elements to determine where
     * values lie in the range.</p>
     *
     * <p>The arguments may be passed in the order (min,max) or (max,min).
     * The getMinimum and getMaximum methods will return the correct values.</p>
     *
     * @param <T> the type of the elements in this range
     * @param fromInclusive  the first value that defines the edge of the range, inclusive
     * @param toInclusive  the second value that defines the edge of the range, inclusive
     * @return the range object, not null
     * @throws IllegalArgumentException if either element is null
     * @throws ClassCastException if the elements are not {@code Comparable}
     */
    public static <T extends Comparable<T>> RangeMin<T> between(T fromInclusive, T toInclusive) {
        return between(fromInclusive, toInclusive, null);
    }
    /**
     * <p>Obtains a range with the specified minimum and maximum values (both inclusive).</p>
     * 
     * <p>The range uses the specified {@code Comparator} to determine where
     * values lie in the range.</p>
     *
     * <p>The arguments may be passed in the order (min,max) or (max,min).
     * The getMinimum and getMaximum methods will return the correct values.</p>
     *
     * @param <T> the type of the elements in this range
     * @param fromInclusive  the first value that defines the edge of the range, inclusive
     * @param toInclusive  the second value that defines the edge of the range, inclusive
     * @param comparator  the comparator to be used, null for natural ordering
     * @return the range object, not null
     * @throws IllegalArgumentException if either element is null
     * @throws ClassCastException if using natural ordering and the elements are not {@code Comparable}
     */
    public static <T> RangeMin<T> between(T fromInclusive, T toInclusive, Comparator<T> comparator) {
        return new RangeMin<T>(fromInclusive, toInclusive, comparator);
    }

    /**
     * Creates an instance.
     *
     * @param element1  the first element, not null
     * @param element2  the second element, not null
     * @param comparator  the comparator to be used, null for natural ordering
     */
    @SuppressWarnings("unchecked")
    private RangeMin(T element1, T element2, Comparator<T> comparator) {
        if (element1 == null || element2 == null) {
            throw new IllegalArgumentException("Elements in a range must not be null: element1=" +
                                               element1 + ", element2=" + element2);
        }
        if (comparator == null) {
            comparator = ComparableComparator.INSTANCE;
        }
        if (comparator.compare(element1, element2) < 1) {
            this.minimum = element1;
            this.maximum = element2;
        } else {
            this.minimum = element2;
            this.maximum = element1;
        }
        this.comparator = comparator;
    }

    /**
     * <p>Compares this range to another object to test if they are equal.</p>.
     *
     * <p>To be equal, the minimum and maximum values must be equal, which
     * ignores any differences in the comparator.</p>
     *
     * @param obj the reference object with which to compare
     * @return true if this object is equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        } else {
            @SuppressWarnings("unchecked") // OK because we checked the class above
            RangeMin<T> range = (RangeMin<T>) obj;
            return minimum.equals(range.minimum) &&
                   maximum.equals(range.maximum);
        }
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings({"rawtypes", "unchecked"})
    private enum ComparableComparator implements Comparator {
        INSTANCE;
        /**
         * Comparable based compare implementation. 
         *
         * @param obj1 left hand side of comparison
         * @param obj2 right hand side of comparison
         * @return negative, 0, positive comparison value
         */
        public int compare(Object obj1, Object obj2) {
            return ((Comparable) obj1).compareTo(obj2);
        }
    }
}
