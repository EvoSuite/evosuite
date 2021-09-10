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
package com.examples.with.different.packagename;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.Test;

import com.examples.with.different.packagename.ClassHierarchyIncludingInterfaces.Interfaces;

/**
 * Snippet from Lang project
 * (org.apache.commons.lang3.ClassUtilsTest)
 */
public class ClassHierarchyIncludingInterfacesTest {

    public interface GenericConsumer<T> {
        void consume(T t);
    }

    public class GenericParent<T> implements GenericConsumer<T> {
        @Override
        public void consume(final T t) {
            // empty
        }
    }

    public class StringParameterizedChild extends GenericParent<String> {
        @Override
        public void consume(final String t) {
            super.consume(t);
        }
    }

    @Test
    public void testHierarchyIncludingInterfaces() {
        final Iterator<Class<?>> iter = ClassHierarchyIncludingInterfaces.hierarchy(StringParameterizedChild.class, Interfaces.INCLUDE).iterator();
        assertEquals(StringParameterizedChild.class, iter.next());
        assertEquals(GenericParent.class, iter.next());
        assertEquals(GenericConsumer.class, iter.next());
        assertEquals(Object.class, iter.next());
        assertFalse(iter.hasNext());
    }
}
