package edu.uta.cse.dsc.vm2;

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
