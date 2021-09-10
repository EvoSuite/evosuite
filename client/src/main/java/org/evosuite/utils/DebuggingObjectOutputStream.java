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
/**
 * <p>DebuggingObjectOutputStream class.</p>
 *
 * @author Gordon Fraser
 */
package org.evosuite.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DebuggingObjectOutputStream extends ObjectOutputStream {

    private static final Field DEPTH_FIELD;

    static {
        try {
            DEPTH_FIELD = ObjectOutputStream.class.getDeclaredField("depth");
            DEPTH_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    final List<Object> stack = new ArrayList<>();

    /**
     * Indicates whether or not OOS has tried to write an IOException
     * (presumably as the result of a serialization error) to the stream.
     */
    boolean broken = false;

    /**
     * <p>Constructor for DebuggingObjectOutputStream.</p>
     *
     * @param out a {@link java.io.OutputStream} object.
     * @throws java.io.IOException if any.
     */
    public DebuggingObjectOutputStream(OutputStream out) throws IOException {
        super(out);
        enableReplaceObject(true);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Abuse {@code replaceObject()} as a hook to maintain our stack.
     */
    @Override
    protected Object replaceObject(Object o) {
        // ObjectOutputStream writes serialization
        // exceptions to the stream. Ignore
        // everything after that so we don't lose
        // the path to a non-serializable object. So
        // long as the user doesn't write an
        // IOException as the root object, we're OK.
        int currentDepth = currentDepth();
        if (o instanceof IOException && currentDepth == 0) {
            broken = true;
        }
        if (!broken) {
            truncate(currentDepth);
            //System.out.println("Current object: " + o.getClass().getName());
            stack.add(o);
        }
        return o;
    }

    private void truncate(int depth) {
        while (stack.size() > depth) {
            pop();
        }
    }

    private Object pop() {
        return stack.remove(stack.size() - 1);
    }

    /**
     * Returns a 0-based depth within the object graph of the current object
     * being serialized.
     */
    private int currentDepth() {
        try {
            Integer oneBased = ((Integer) DEPTH_FIELD.get(this));
            return oneBased - 1;
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the path to the last object serialized. If an exception occurred,
     * this should be the path to the non-serializable object.
     *
     * @return a {@link java.util.List} object.
     */
    public List<Object> getStack() {
        return stack;
    }
}
