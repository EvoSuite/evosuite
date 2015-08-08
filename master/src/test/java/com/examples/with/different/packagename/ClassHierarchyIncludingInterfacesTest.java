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
