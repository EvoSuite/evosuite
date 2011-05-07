package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

public class DefUseFactory {

	// convenience methods for now
	
	/**
	 *  Returns a Definition instance
	 * given a BytecodeInstruction for which isDefinition() is true
	 *  Otherwise returns a Use instance
	 * given a BytecodeInstruction for which isUse() is true
	 * 
	 * WARNING: when given the wrap for an IINC this method will return a Definition
	 */
	public static DefUse makeInstance(BytecodeInstruction wrap) {
		if(!wrap.isDefUse())
			throw new IllegalArgumentException("expect wrap of a defuse to create one");
		if(wrap.isDefinition())
			return makeDefinition(wrap);
		if(wrap.isUse())
			return makeUse(wrap);

		throw new IllegalStateException("either isUse() or isDefinition() must return true on a defuse");
	}
	
	/**
	 * Returns a Use instance given a BytecodeInstruction for which isUse() is
	 * true
	 * 
	 */
	public static Use makeUse(BytecodeInstruction wrap) {
		if (!wrap.isUse())
			throw new IllegalArgumentException(
					"expect wrap of a use to create one");
		if (!DefUsePool.isKnown(wrap))
			throw new IllegalArgumentException(
					"expect DefUsePool to know the given BytecodeInstruction");

		int defuseId = DefUsePool.getRegisteredDefUseId(wrap);
		int defId = DefUsePool.getRegisteredDefId(wrap);
		int useId = DefUsePool.getRegisteredUseId(wrap);
		boolean isParameterUse = DefUsePool.isRegisteredParameterUse(wrap);

		return new Use(wrap, defuseId, defId, useId, isParameterUse);
	}

	/**
	 * Returns a Definition instance given a BytecodeInstruction for which
	 * isDefinition() is true
	 * 
	 */
	public static Definition makeDefinition(BytecodeInstruction wrap) {
		if (!wrap.isDefinition())
			throw new IllegalArgumentException(
					"expect wrap of a definition to create one");
		if (!DefUsePool.isKnown(wrap))
			throw new IllegalArgumentException(
					"expect DefUsePool to know the given BytecodeInstruction");

		int defuseId = DefUsePool.getRegisteredDefUseId(wrap);
		int defId = DefUsePool.getRegisteredDefId(wrap);
		int useId = DefUsePool.getRegisteredUseId(wrap);
		boolean isParameterUse = DefUsePool.isRegisteredParameterUse(wrap);

		return new Definition(wrap, defuseId, defId, useId, isParameterUse);
	}
	
}
