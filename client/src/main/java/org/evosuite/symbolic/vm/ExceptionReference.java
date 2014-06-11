package org.evosuite.symbolic.vm;

import org.objectweb.asm.Type;

/**
 * 
 * @author galeotti
 * 
 */
public final class ExceptionReference extends NonNullReference {

	private ExceptionReference() {
		super(Type.getType(Exception.class), -1);
	}

	private static final ExceptionReference instance = new ExceptionReference();

	public static ExceptionReference getInstance() {
		return instance;
	}

	@Override
	public String toString() {
		return "EXCEPTION";
	}

}
