package edu.uta.cse.dsc.vm2.string;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class StaticFunction extends StringFunction {

	public StaticFunction(SymbolicEnvironment env, String owner,
			String name, String desc) {
		super(env, owner, name, desc);
	}

	public abstract void INVOKESTATIC();

}
