package edu.uta.cse.dsc.vm2;

import edu.uta.cse.dsc.AbstractVM;

/**
 * Remaining Jvm bytecode instruction
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class OtherVM extends AbstractVM {

	public OtherVM() {

	}

	@Override
	public void UNUSED() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void MONITORENTER() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void MONITOREXIT() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void WIDE() {
		throw new UnsupportedOperationException();
	}

}
