/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga.metaheuristics.mosa.structural;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.coverage.branch.Branch;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.graphs.cfg.BasicBlock;
import org.evosuite.graphs.cfg.BytecodeInstruction;

public class CFGUtils {

	public static Set<BasicBlock> lookForParent(BasicBlock block, ActualControlFlowGraph acfg, Set<BasicBlock> visitedBlock){
		Set<BasicBlock> realParent = new HashSet<BasicBlock>();
		Set<BasicBlock> parents = acfg.getParents(block);
		if (parents.size() == 0){
			realParent.add(block);
			return realParent;
		}
		for (BasicBlock bb : parents){
			if (visitedBlock.contains(bb))
				continue;
			visitedBlock.add(bb);
			if (containsBranches(bb))
				realParent.add(bb);
			else 
				realParent.addAll(lookForParent(bb, acfg, visitedBlock));
		}
		return realParent;
	}

	/**
	 * Utility method that verifies whether a basic block (@link {@link BasicBlock})
	 * contains a branch.
	 * @param block object of {@link BasicBlock}
	 * @return true or false depending on whether a branch is found
	 */
	public static boolean containsBranches(BasicBlock block){
		for (BytecodeInstruction inst : block)
			if (inst.toBranch()!=null)
				return true;
		return false;
	}

	/**
	 * Utility method that extracts a branch ({@link Branch}) from a basic block 
	 * (@link {@link BasicBlock}).
	 * @param block object of {@link BasicBlock}
	 * @return an object of {@link Branch} representing the branch in the block
	 */
	public static Branch extractBranch(BasicBlock block){
		for (BytecodeInstruction inst : block)
			if (inst.isBranch() || inst.isActualBranch())
				return inst.toBranch();
		return null;
	}
}
