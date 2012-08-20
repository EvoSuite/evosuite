package org.evosuite.symbolic.vm;

/**
 * 
 * @author galeotti
 *
 */
public final class ExceptionReference extends NonNullReference {

	private ExceptionReference() {
		super("java.lang.Exception", -1);
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
