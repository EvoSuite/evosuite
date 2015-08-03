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
