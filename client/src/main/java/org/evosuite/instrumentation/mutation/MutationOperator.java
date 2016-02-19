/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.instrumentation.mutation;

import java.util.List;

import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;


/**
 * <p>MutationOperator interface.</p>
 *
 * @author Gordon Fraser
 */
public interface MutationOperator {

	/**
	 * Insert the mutation into the bytecode
	 *
	 * @param mn a {@link org.objectweb.asm.tree.MethodNode} object.
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @param frame a {@link org.objectweb.asm.tree.analysis.Frame} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction, Frame frame);

	/**
	 * Check if the mutation operator is applicable to the instruction
	 *
	 * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public boolean isApplicable(BytecodeInstruction instruction);

}
