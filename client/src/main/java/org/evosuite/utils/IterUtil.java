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

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class IterUtil {
    /**
     * Constant <code>DEFAULT_JOIN_SEPARATOR=", "</code>
     */
    public static final String DEFAULT_JOIN_SEPARATOR = ", ";

    /**
     * <p>join</p>
     *
     * @param iter      a {@link java.lang.Iterable} object.
     * @param separator a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(Iterable<?> iter, String separator) {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;

        for (Object item : iter) {
            if (!isFirst) {
                result.append(separator);
            }

            result.append(item);
            isFirst = false;
        }

        return result.toString();
    }

    /**
     * <p>join</p>
     *
     * @param iter a {@link java.lang.Iterable} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(Iterable<?> iter) {
        return join(iter, DEFAULT_JOIN_SEPARATOR);
    }

    public static <T> List<T> minList(Iterable<T> collection, Comparator<? super T> comparator) {
        List<T> minima = new LinkedList<>();

        if (collection == null) {
            return minima;
        }

        Iterator<T> it = collection.iterator();

        T currentMin = it.next();
        minima.add(currentMin);

        while (it.hasNext()) {
            T element = it.next();
            int comparison = comparator.compare(element, currentMin);

            if (comparison < 0) {
                minima.clear();
                currentMin = element;
                minima.add(element);
            } else if (comparison == 0) {
                minima.add(element);
            }
        }

        return minima;
    }
}
