package de.unisb.cs.st.evosuite.symbolic;

public class ConstraintTooLongException extends RuntimeException {

	private static final long serialVersionUID = -7625075607830941781L;
	
	public ConstraintTooLongException(){
		super();
	}

	public ConstraintTooLongException(String msg){
		super(msg);
	}

}
