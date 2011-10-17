/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation.mutation;

import java.util.List;

import org.objectweb.asm.tree.MethodNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;

/**
 * @author Gordon Fraser
 * 
 */
public interface MutationOperator {

	/**
	 * Insert the mutation into the bytecode
	 * 
	 * @param mn
	 * @param className
	 * @param methodName
	 * @param instruction
	 */
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction);

	/**
	 * Check if the mutation operator is applicable to the instruction
	 * 
	 * @param instruction
	 * @return
	 */
	public boolean isApplicable(BytecodeInstruction instruction);

}
