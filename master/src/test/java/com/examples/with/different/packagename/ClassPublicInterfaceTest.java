/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.apache.commons.lang3.event.EventListenerSupport;
import org.junit.Test;

/**
 * Snippet from Lang project
 * (org.apache.commons.lang3.event.EventUtilsTest)
 */
public class ClassPublicInterfaceTest {

	public static interface MultipleEventListener {
        public void event1(Object e);
    }

	public static class EventCounter
    {
        private int count;

        public void eventOccurred()
        {
            count++;
        }

        public int getCount()
        {
            return count;
        }
    }

	public static class MultipleEventSource
    {
        private final EventListenerSupport<MultipleEventListener> listeners = EventListenerSupport.create(MultipleEventListener.class);

        public void addMultipleEventListener(final MultipleEventListener listener)
        {
            listeners.addListener(listener);
        }
    }

	@Test
	public void testBindFilteredEventsToMethod() {
		final MultipleEventSource src = new MultipleEventSource();
		final EventCounter counter = new EventCounter();
		ClassPublicInterface.bindEventsToMethod(counter, "eventOccurred", src, MultipleEventListener.class, "event1");
        assertEquals(0, counter.getCount());
	}
}
