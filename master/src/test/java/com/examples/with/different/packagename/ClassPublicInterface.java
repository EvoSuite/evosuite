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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * Snippet from Lang project
 * (org.apache.commons.lang3.event.EventUtils)
 */
public class ClassPublicInterface {

	public static <L> void addEventListener(final Object eventSource, final Class<L> listenerType, final L listener) {
        try {
            MethodUtils.invokeMethod(eventSource, "add" + listenerType.getSimpleName(), listener);
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + eventSource.getClass().getName()
                    + " does not have a public add" + listenerType.getSimpleName()
                    + " method which takes a parameter of type " + listenerType.getName() + ".");
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException("Class " + eventSource.getClass().getName()
                    + " does not have an accessible add" + listenerType.getSimpleName ()
                    + " method which takes a parameter of type " + listenerType.getName() + ".");
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("Unable to add listener.", e.getCause());
        }
    }

	public static <L> void bindEventsToMethod(final Object target, final String methodName, final Object eventSource,
            final Class<L> listenerType, final String... eventTypes) {
		final L listener = listenerType.cast(Proxy.newProxyInstance(target.getClass().getClassLoader(),
                new Class[] { listenerType }, new EventBindingInvocationHandler(target, methodName, eventTypes)));
        addEventListener(eventSource, listenerType, listener);
	}

	private static class EventBindingInvocationHandler implements InvocationHandler {
        private final Object target;
        private final String methodName;
        private final Set<String> eventTypes;

        /**
         * Creates a new instance of {@code EventBindingInvocationHandler}.
         *
         * @param target the target object for method invocations
         * @param methodName the name of the method to be invoked
         * @param eventTypes the names of the supported event types
         */
        EventBindingInvocationHandler(final Object target, final String methodName, final String[] eventTypes) {
            this.target = target;
            this.methodName = methodName;
            this.eventTypes = new HashSet<String>(Arrays.asList(eventTypes));
        }

        /**
         * Handles a method invocation on the proxy object.
         *
         * @param proxy the proxy instance
         * @param method the method to be invoked
         * @param parameters the parameters for the method invocation
         * @return the result of the method call
         * @throws Throwable if an error occurs
         */
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] parameters) throws Throwable {
            if (eventTypes.isEmpty() || eventTypes.contains(method.getName())) {
                if (hasMatchingParametersMethod(method)) {
                    return MethodUtils.invokeMethod(target, methodName, parameters);
                }
                return MethodUtils.invokeMethod(target, methodName);
            }
            return null;
        }

        /**
         * Checks whether a method for the passed in parameters can be found.
         *
         * @param method the listener method invoked
         * @return a flag whether the parameters could be matched
         */
        private boolean hasMatchingParametersMethod(final Method method) {
            return MethodUtils.getAccessibleMethod(target.getClass(), methodName, method.getParameterTypes()) != null;
        }
    }
}
