package edu.uta.cse.dsc.vm2.string;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class SpecialFunction extends StringFunction {

	public SpecialFunction(SymbolicEnvironment env, String owner, String name,
			String desc) {
		super(env, owner, name, desc);
	}

	public abstract void INVOKESPECIAL();

}
