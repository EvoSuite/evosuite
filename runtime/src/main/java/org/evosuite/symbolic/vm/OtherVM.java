package org.evosuite.symbolic.vm;

import edu.uta.cse.dsc.AbstractVM;

/**
 * 
 * @author galeotti
 *
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
