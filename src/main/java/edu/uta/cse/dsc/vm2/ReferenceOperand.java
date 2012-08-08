package edu.uta.cse.dsc.vm2;

public class ReferenceOperand implements SingleWordOperand {

	private final Object reference;
	
	public ReferenceOperand(Object ref) {
		this.reference=ref;
	}

	public Object getReference() {
		return reference;
	}
}