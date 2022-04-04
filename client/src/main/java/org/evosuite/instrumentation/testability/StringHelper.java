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
import org.evosuite.instrumentation.RegexDistance;
import org.evosuite.seeding.ConstantPoolManager;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andrea Arcuri on 26/03/15.
 */
public class StringHelper {

    /**
     * <p>
     * editDistance
     * </p>
     *
     * @param s a {@link java.lang.String} object.
     * @param t a {@link java.lang.String} object.
     * @return a int.
     */
    public static int editDistance(String s, String t) {
        //if (s == null || t == null) {
        //	throw new IllegalArgumentException("Strings must not be null");
        //}

		/*
		    The difference between this impl. and the previous is that, rather
		     than creating and retaining a matrix of size s.length()+1 by t.length()+1,
		     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		     is the 'current working' distance array that maintains the newest distance cost
		     counts as we iterate through the characters of String s.  Each time we increment
		     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		     allows us to retain the previous cost counts as required by the algorithm (taking
		     the minimum of the cost count to the left, up one, and diagonally up and to the left
		     of the current cost count being calculated).  (Note that the arrays aren't really
		     copied anymore, just switched...this is clearly much better than cloning an array
		     or doing a System.arraycopy() each time  through the outer loop.)

		     Effectively, the difference between the two implementations is this one does not
		     cause an out of memory condition when calculating the LD over two very large strings.
		 */

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int[] p = new int[n + 1]; //'previous' cost array, horizontally
        int[] d = new int[n + 1]; // cost array, horizontally
        int[] _d; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }

    /*
     * Return a positive number if the 2 strings are equal, or a <=0 value representing
     * how different they are
     */

    /**
     * <p>
     * StringEquals
     * </p>
     *
     * @param first  a {@link java.lang.String} object.
     * @param second a {@link java.lang.Object} object.
     * @return a int.
     */
    public static int StringEquals(String first, Object second) {
        if (first == null) {
            throw new NullPointerException(
                    "StringEquals is not supposed to work on a null caller");
        }
        // Comparison with null is always false
        if (second == null) {
            return -BooleanHelper.K;
        }

        if (first.equals(second)) {
            return BooleanHelper.K; // Identical
        } else {
            ConstantPoolManager.getInstance().addDynamicConstant(first);
            ConstantPoolManager.getInstance().addDynamicConstant(second);
            // return -getDistanceBasedOnLeftAlignment(first, second.toString());
            double distance = -getDistanceBasedOnLeftAlignmentCharacterDistance(first, second.toString());
            double d2 = distance / (1.0 + Math.abs(distance));

            return (int) Math.round(BooleanHelper.K * d2);
        }
    }

    public static double StringEqualsCharacterDistance(String first, Object second) {
        if (first == null) {
            throw new IllegalArgumentException(
                    "StringEquals is not supposed to work on a null caller");
        }
        // Comparison with null is always false
        if (second == null) {
            return -BooleanHelper.K;
        }

        if (first.equals(second)) {
            return BooleanHelper.K; // Identical
        } else {
            //System.out.println("Edit distance between " + first + " and " + second
            //       + " is " + -editDistance(first, second.toString()) + " / "
            //      + getLevenshteinDistance(first, (String) second));
            //return -editDistance(first, second.toString());
            //return -getLevenshteinDistance(first, (String) second);
            return -getDistanceBasedOnLeftAlignmentCharacterDistance(first,
                    second.toString());
        }
    }

    public static int StringMatches(String str, String regex) {
        int distance = RegexDistance.getDistance(str, regex);

        if (Properties.DYNAMIC_POOL > 0.0) {
            if (distance > 0) {
                String instance = RegexDistance.getRegexInstance(regex);
                ConstantPoolManager.getInstance().addDynamicConstant(instance);
            } else {
                String instance = RegexDistance.getNonMatchingRegexInstance(regex);
                ConstantPoolManager.getInstance().addDynamicConstant(instance);
            }
        }

        if (distance > 0)
            return -distance;
        else
            return BooleanHelper.K;
    }

    public static int StringMatchRegex(String regex, CharSequence input) {
        int distance = RegexDistance.getDistance(input.toString(), regex);

        if (Properties.DYNAMIC_POOL > 0.0) {
            if (distance > 0) {
                String instance = RegexDistance.getRegexInstance(regex);
                ConstantPoolManager.getInstance().addDynamicConstant(instance);
            } else {
                String instance = RegexDistance.getNonMatchingRegexInstance(regex);
                ConstantPoolManager.getInstance().addDynamicConstant(instance);
            }
        }

        if (distance > 0)
            return -distance;
        else
            return BooleanHelper.K;
    }

    public static int StringMatchRegex(Matcher matcher) {
        Pattern pattern = matcher.pattern();
        String regex = pattern.pattern();
        CharSequence input;
        try {
            Field textField = Matcher.class.getDeclaredField("text");
            textField.setAccessible(true);
            input = (CharSequence) textField.get(matcher);
            int distance = RegexDistance.getDistance(input.toString(), regex);

            if (Properties.DYNAMIC_POOL > 0.0) {
                if (distance > 0) {
                    String instance = RegexDistance.getRegexInstance(regex);
                    ConstantPoolManager.getInstance().addDynamicConstant(instance);
                } else {
                    String instance = RegexDistance.getNonMatchingRegexInstance(regex);
                    ConstantPoolManager.getInstance().addDynamicConstant(instance);
                }
            }

            if (distance > 0)
                return -distance;
            else
                return BooleanHelper.K;
        } catch (Throwable t) {
            t.printStackTrace();
            return matcher.matches() ? 1 : -1;
        }
    }

    /**
     * <p>
     * getDistanceBasedOnLeftAlignment
     * </p>
     *
     * @param a a {@link java.lang.String} object.
     * @param b a {@link java.lang.String} object.
     * @return a int.
     */
    protected static int getDistanceBasedOnLeftAlignment(String a, String b) {
        int differences = 0;
        int min = Math.min(a.length(), b.length());
        int max = Math.max(a.length(), b.length());
        differences += (max - min);
        for (int i = 0; i < min; i++) {
            /*
             * Note: instead of just checking for mismatches, we could use something more sophisticated.
             * Eg, "a" is closer to "e" than "!". But maybe, considering the type of local search
             * we do, we don't need to do it
             */
            if (a.charAt(i) != b.charAt(i)) {
                differences++;
            }
        }
        return differences;

    }

    public static double getDistanceBasedOnLeftAlignmentCharacterDistance(String a,
                                                                          String b) {
        if (a == b) {
            return BooleanHelper.K;
        } else if (a == null && b != null) {
            return b.length() + 1; // +1 is important to handle the empty string ""
        } else if (a != null && b == null) {
            return a.length() + 1;
        } else {
            double differences = 0.0;
            int min = Math.min(a.length(), b.length());
            int max = Math.max(a.length(), b.length());
            differences += (max - min);
            for (int i = 0; i < min; i++) {
                /*
                 * Note: instead of just checking for mismatches, we could use something more sophisticated.
                 * Eg, "a" is closer to "e" than "!". But maybe, considering the type of local search
                 * we do, we don't need to do it
                 */
                if (a.charAt(i) != b.charAt(i)) {
                    differences += BooleanHelper.normalize(Math.abs(a.charAt(i) - b.charAt(i)));
                }
            }
            //LoggingUtils.getEvoLogger().info("Distance between " + a + " and " + b  + " is: " + differences);
            return differences;
        }
    }

    /**
     * <p>
     * StringEqualsIgnoreCase
     * </p>
     *
     * @param first  a {@link java.lang.String} object.
     * @param second a {@link java.lang.String} object.
     * @return a int.
     */
    public static int StringEqualsIgnoreCase(String first, String second) {
        if (first == null) {
            throw new NullPointerException(
                    "StringEquals is not supposed to work on a null caller");
        }
        // Comparison with null is always false
        if (second == null) {
            return -BooleanHelper.K;
        }
        // We may miss locale specific cases of equivalence, so
        // first we check for equivalence using java.lang.String
        if (first.equalsIgnoreCase(second)) {
            return BooleanHelper.K;
        }
        return StringEquals(first.toLowerCase(), second.toLowerCase());
    }

    /**
     * <p>
     * StringStartsWith
     * </p>
     *
     * @param value  a {@link java.lang.String} object.
     * @param prefix a {@link java.lang.String} object.
     * @param start  a int.
     * @return a int.
     */
    public static int StringStartsWith(String value, String prefix, int start) {
        int len = Math.min(prefix.length(), value.length());
        ConstantPoolManager.getInstance().addDynamicConstant(prefix + value);
        return StringEquals(value.substring(start, Math.min(start + len, value.length())), prefix);
    }

    /**
     * <p>
     * StringEndsWith
     * </p>
     *
     * @param value  a {@link java.lang.String} object.
     * @param suffix a {@link java.lang.String} object.
     * @return a int.
     */
    public static int StringEndsWith(String value, String suffix) {
        int len = Math.min(suffix.length(), value.length());
        String val1 = value.substring(value.length() - len);
        ConstantPoolManager.getInstance().addDynamicConstant(value + suffix);
        return StringEquals(val1, suffix);
    }

    /**
     * <p>
     * StringIsEmpty
     * </p>
     *
     * @param value a {@link java.lang.String} object.
     * @return a int.
     */
    public static int StringIsEmpty(String value) {
        int len = value.length();
        if (len == 0) {
            return BooleanHelper.K;
        } else {
            return -len;
        }
    }

    /**
     * <p>
     * StringRegionMatches
     * </p>
     *
     * @param value      a {@link java.lang.String} object.
     * @param thisStart  a int.
     * @param string     a {@link java.lang.String} object.
     * @param start      a int.
     * @param length     a int.
     * @param ignoreCase a boolean.
     * @return a int.
     */
    public static int StringRegionMatches(String value, boolean ignoreCase,
                                          int thisStart, String string, int start, int length) {

        if (value == null || string == null)
            throw new NullPointerException();

        if (start < 0 || string.length() - start < length) {
            return -BooleanHelper.K;
        }

        if (thisStart < 0 || value.length() - thisStart < length) {
            return -BooleanHelper.K;
        }
        if (length <= 0) {
            return BooleanHelper.K;
        }
        // We may miss locale specific cases of equivalence, so
        // first we check for equivalence using java.lang.String
        if (ignoreCase && value.regionMatches(ignoreCase, thisStart, string, start, length)) {
            return BooleanHelper.K;
        }

        String s1 = value;
        String s2 = string;
        if (ignoreCase) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
        }
        if (Properties.DYNAMIC_POOL > 0.0) {
            String sub1 = s1.substring(thisStart, length + thisStart);
            String sub2 = s2.substring(start, length + start);
            String sn1 = s1.substring(0, thisStart) + sub2
                    + s1.substring(thisStart + length);
            String sn2 = s2.substring(0, start) + sub1 + s2.substring(start + length);
            ConstantPoolManager.getInstance().addDynamicConstant(sn1);
            ConstantPoolManager.getInstance().addDynamicConstant(sn2);
        }

        return StringEquals(s1.substring(thisStart, Math.min(length + thisStart, s1.length())),
                s2.substring(start, Math.min(length + start, s2.length())));
    }

    public static int StringRegionMatches(String value, int thisStart, String string,
                                          int start, int length) {
        return StringRegionMatches(value, false, thisStart, string, start, length);
    }
}
