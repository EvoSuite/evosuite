package org.evosuite.symbolic.vm;

/**
 * 
 * @author galeotti
 *
 */
public class ReferenceOperand implements SingleWordOperand {

	private final Reference ref;

	public ReferenceOperand(Reference o) {
		this.ref = o;
	}

	public Reference getReference() {
		return this.ref;
	}

	@Override
	public String toString() {
		return this.ref.toString();
	}

}