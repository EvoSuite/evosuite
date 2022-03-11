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
package com.examples.with.different.packagename.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GenericStaticMethod4 {

    public static <E> List<E> synchronizedList(final List<E> list) {
        return Collections.synchronizedList(list);
    }

    public static <E> List<E> select(final Collection<? extends E> inputCollection,
                                     final Predicate<? super E> predicate) {
        List<E> result = new ArrayList<>();
        for (E elem : inputCollection) {
            if (predicate.evaluate(elem)) {
                result.add(elem);
            }
        }
        return result;
    }

    public static <E> List<E> selectRejected(final Collection<? extends E> inputCollection,
                                             final Predicate<? super E> predicate) {
        List<E> result = new ArrayList<>();
        for (E elem : inputCollection) {
            if (!predicate.evaluate(elem)) {
                result.add(elem);
            }
        }
        return result;
    }
}
