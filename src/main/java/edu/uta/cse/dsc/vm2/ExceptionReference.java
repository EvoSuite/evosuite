package edu.uta.cse.dsc.vm2;

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
