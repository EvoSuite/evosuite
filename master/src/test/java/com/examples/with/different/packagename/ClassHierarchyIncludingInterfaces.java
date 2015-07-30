package com.examples.with.different.packagename;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableObject;

/**
 * Snippet from Lang project
 * (org.apache.commons.lang3.ClassUtils)
 */
public class ClassHierarchyIncludingInterfaces {

	public enum Interfaces {
        INCLUDE, EXCLUDE
    }

	public static Iterable<Class<?>> hierarchy(final Class<?> type) {
        return hierarchy(type, Interfaces.EXCLUDE);
    }

	public static Iterable<Class<?>> hierarchy(final Class<?> type, final Interfaces interfacesBehavior) {
        final Iterable<Class<?>> classes = new Iterable<Class<?>>() {
    
            @Override
            public Iterator<Class<?>> iterator() {
                final MutableObject<Class<?>> next = new MutableObject<Class<?>>(type);
                return new Iterator<Class<?>>() {
    
                    @Override
                    public boolean hasNext() {
                        return next.getValue() != null;
                    }
    
                    @Override
                    public Class<?> next() {
                        final Class<?> result = next.getValue();
                        next.setValue(result.getSuperclass());
                        return result;
                    }
    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
    
                };
            }
    
        };
        if (interfacesBehavior != Interfaces.INCLUDE) {
            return classes;
        }
        return new Iterable<Class<?>>() {
    
            @Override
            public Iterator<Class<?>> iterator() {
                final Set<Class<?>> seenInterfaces = new HashSet<Class<?>>();
                final Iterator<Class<?>> wrapped = classes.iterator();
    
                return new Iterator<Class<?>>() {
                    Iterator<Class<?>> interfaces = Collections.<Class<?>> emptySet().iterator();
    
                    @Override
                    public boolean hasNext() {
                        return interfaces.hasNext() || wrapped.hasNext();
                    }
    
                    @Override
                    public Class<?> next() {
                        if (interfaces.hasNext()) {
                            final Class<?> nextInterface = interfaces.next();
                            seenInterfaces.add(nextInterface);
                            return nextInterface;
                        }
                        final Class<?> nextSuperclass = wrapped.next();
                        final Set<Class<?>> currentInterfaces = new LinkedHashSet<Class<?>>();
                        walkInterfaces(currentInterfaces, nextSuperclass);
                        interfaces = currentInterfaces.iterator();
                        return nextSuperclass;
                    }
    
                    private void walkInterfaces(final Set<Class<?>> addTo, final Class<?> c) {
                        for (final Class<?> iface : c.getInterfaces()) {
                            if (!seenInterfaces.contains(iface)) {
                                addTo.add(iface);
                            }
                            walkInterfaces(addTo, iface);
                        }
                    }
    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
    
                };
            }
        };
    }
}
