package org.evosuite.symbolic.vm;

/**
 * 
 * @author galeotti
 *
 */
public final class NullReference implements Reference {

	private final static NullReference singleton = new NullReference();

	private NullReference() {
	}

	public static NullReference getInstance() {
		return singleton;
	}

	@Override
	public String toString() {
		return "NULL";
	}
}
