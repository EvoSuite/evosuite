package edu.uta.cse.dsc.vm2.string;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class StringStaticFunction extends StringFunction {

	public StringStaticFunction(SymbolicEnvironment env, String owner,
			String name, String desc) {
		super(env, owner, name, desc);
	}

}
