/**
 * 
 */
package de.unisb.cs.st.evosuite.utils;

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

	final List<Object> stack = new ArrayList<Object>();

	/**
	 * Indicates whether or not OOS has tried to write an IOException
	 * (presumably as the result of a serialization error) to the stream.
	 */
	boolean broken = false;

	public DebuggingObjectOutputStream(OutputStream out) throws IOException {
		super(out);
		enableReplaceObject(true);
	}

	/**
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
	 */
	public List<Object> getStack() {
		return stack;
	}
}
