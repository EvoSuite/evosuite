package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

/**
 * Can be used to create instances of Definition and Use
 * 
 *  When given an instruction this factory asks the DefUsePool
 * whether it knows this instruction. If it does the pool
 * reveals the initially assigned defUseIDs which are then
 * put into the respective constructors.
 * 
 * @author Andre Mis
 */
public class DefUseFactory {
	
	/**
	 * Returns a Use instance given a BytecodeInstruction for which isUse() is
	 * true
	 * 
	 */
	public static Use makeUse(BytecodeInstruction instruction) {
		if (!instruction.isUse())
			throw new IllegalArgumentException(
					"expect wrap of a use to create one");
		if (!DefUsePool.isKnown(instruction))
			throw new IllegalArgumentException(
					"expect DefUsePool to know the given BytecodeInstruction: "+instruction.toString());

		int defuseId = DefUsePool.getRegisteredDefUseId(instruction);
		int defId = DefUsePool.getRegisteredDefId(instruction);
		int useId = DefUsePool.getRegisteredUseId(instruction);
		boolean isParameterUse = DefUsePool.isRegisteredParameterUse(instruction);

		return new Use(instruction, defuseId, defId, useId, isParameterUse);
	}

	/**
	 * Returns a Definition instance given a BytecodeInstruction for which
	 * isDefinition() is true
	 * 
	 */
	public static Definition makeDefinition(BytecodeInstruction instruction) {
		if (!instruction.isDefinition())
			throw new IllegalArgumentException(
					"expect wrap of a definition to create one");
		if (!DefUsePool.isKnown(instruction))
			throw new IllegalArgumentException(
					"expect DefUsePool to know the given BytecodeInstruction");

		int defuseId = DefUsePool.getRegisteredDefUseId(instruction);
		int defId = DefUsePool.getRegisteredDefId(instruction);
		int useId = DefUsePool.getRegisteredUseId(instruction);
		boolean isParameterUse = DefUsePool.isRegisteredParameterUse(instruction);

		return new Definition(instruction, defuseId, defId, useId, isParameterUse);
	}

	/**
	 *  Convenience method to offer DefUse-Functionality for when 
	 * it doesn't matter whether a Definition or Use is returned
	 * 
	 *  Returns a Definition instance
	 * given a BytecodeInstruction for which isDefinition() is true
	 *  Otherwise returns a Use instance
	 * given a BytecodeInstruction for which isUse() is true
	 * 
	 * WARNING: when given the wrap for an IINC this method will return a Definition
	 */
	public static DefUse makeInstance(BytecodeInstruction instruction) {
		if(!instruction.isDefUse())
			throw new IllegalArgumentException("expect wrap of a defuse to create one");
		if(instruction.isDefinition())
			return makeDefinition(instruction);
		if(instruction.isUse())
			return makeUse(instruction);

		throw new IllegalStateException("either isUse() or isDefinition() must return true on a defuse");
	}
}
