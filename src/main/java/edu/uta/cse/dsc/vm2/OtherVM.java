package edu.uta.cse.dsc.vm2;

import edu.uta.cse.dsc.AbstractVM;

/**
 * Remaining Jvm bytecode instruction
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class OtherVM extends AbstractVM {

	private final SymbolicEnvironment env;

	public OtherVM(SymbolicEnvironment env) {
		this.env = env;
	}

	@Override
	public void UNUSED() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Enter synchronized region of code
	 */
	@Override
	public void MONITORENTER() {
		// discard symbolic argument
		this.env.topFrame().operandStack.popRef();
		// ignore this instruction
		return;
	}

	@Override
	public void MONITOREXIT() {
		// discard symbolic argument
		this.env.topFrame().operandStack.popRef();
		// ignore this instruction
		return;
	}

	@Override
	public void WIDE() {
		throw new UnsupportedOperationException();
	}

}
