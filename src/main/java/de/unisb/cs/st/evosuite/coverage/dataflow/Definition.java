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
	
	/**
	 * Determines whether this Definition can be an active definition
	 * for the given instruction.
	 * 
	 *  This is the case if instruction constitutes a Use for the 
	 *  same variable as this Definition
	 *  
	 *  Not to be confused with DefUse.canBecomeActiveDefinitionFor,
	 *  which is sort of the dual to this method
	 */
	public boolean canBeActiveFor(BytecodeInstruction instruction) {
		if(!instruction.isUse())
			return false;
		
		Use use = DefUseFactory.makeUse(instruction);
		return sharesVariableWith(use);
	}
	
//	@Override
//	public boolean equals(Object o) {
//		if(o==null)
//			return false;
//		if(o==this)
//			return true;
//		if(!(o instanceof Definition))
//			return super.equals(o);
//		
//		Definition other = (Definition)o;
//		
//		return defId == other.defId;
//	}
}
