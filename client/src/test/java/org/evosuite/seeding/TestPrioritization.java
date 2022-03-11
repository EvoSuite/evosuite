/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.seeding;

import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


public class TestPrioritization {

    @Test
    public void test() {
        Comparator<Integer> integerComparator = Comparator.comparingInt(x -> x % 10);
        Prioritization<Integer> pc = new Prioritization<>(integerComparator);
        pc.add(1, 1);
        pc.add(11, 3);
        pc.add(21, 2);
        pc.add(10, 10);
        List<Integer> integers = pc.toSortedList();
        assertThat(integers, equalTo(Arrays.asList(10, 1, 21, 11)));
    }


    @Test
    public void test1() {
        Prioritization<GenericClass<?>> prioritization =
                new Prioritization<>(comparingInt(GenericClass::getNumParameters));
        prioritization.add(new GenericClassImpl(NoParams1.class), 1);
        prioritization.add(new GenericClassImpl(NoParams2.class), 2);
        prioritization.add(new GenericClassImpl(OneParam1.class), 1);
        prioritization.add(new GenericClassImpl(OneParam2.class), 2);
        prioritization.add(new GenericClassImpl(TwoParams1.class), 1);
        prioritization.add(new GenericClassImpl(TwoParams2.class), 2);

        List<GenericClass<?>> result = prioritization.toSortedList();
        List<Class<?>> expected = Arrays.asList(NoParams1.class, NoParams2.class, OneParam1.class, OneParam2.class, TwoParams1.class, TwoParams2.class);
        assertThat("Resulting size of Prioritizing collection is not correct", result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            GenericClass<?> genericClass = result.get(i);
            GenericClass<?> expectedGC = new GenericClassImpl(expected.get(i));
            assertThat("", genericClass.equals(expectedGC));
        }
    }

    private static class NoParams2 {
    }

    private static class NoParams1 {
    }

    private static class OneParam1<T> {
    }

    private static class OneParam2<T> {
    }

    private static class TwoParams1<A, B> {
    }

    private static class TwoParams2<A, B> {
    }
}