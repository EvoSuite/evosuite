package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * An object of this class corresponds to a Definition inside the class under test.
 * 
 * Definitions are created by the DefUseFactory via the DefUsePool.
 * 
 * @author Andre Mis
 */

public class Definition extends DefUse {

	Definition(BytecodeInstruction wrap, int defuseId, int defId, int useId,
			boolean isParameterUse) {
	
		super(wrap, defuseId, defId, useId, isParameterUse);
		if (!isDefinition())
			throw new IllegalArgumentException(
					"Vertex of a definition expected");
	}
}
