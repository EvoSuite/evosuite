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
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.graphs.cfg.BasicBlock;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Annibale Panichella
 */
public class BranchFitnessGraph<T extends Chromosome, V extends FitnessFunction<T>>{
	
	private static final Logger logger = LoggerFactory.getLogger(BranchFitnessGraph.class);

	protected DefaultDirectedGraph<FitnessFunction<T>, DependencyEdge> graph = new DefaultDirectedGraph<FitnessFunction<T>, DependencyEdge>(DependencyEdge.class);

	protected Set<FitnessFunction<T>> rootBranches = new HashSet<FitnessFunction<T>>();

	@SuppressWarnings("unchecked")
	public BranchFitnessGraph(Set<FitnessFunction<T>> goals){
		for (FitnessFunction<T> fitness : goals){
			graph.addVertex(fitness);
		}

		// derive dependencies among branches
		for (FitnessFunction<T> fitness : goals){
			Branch branch = ((BranchCoverageTestFitness) fitness).getBranch();
			if (branch==null){
				this.rootBranches.add(fitness); 
				continue;
			}

			if (branch.getInstruction().isRootBranchDependent())
					//|| branch.getInstruction().getControlDependentBranchIds().contains(-1))
				this.rootBranches.add(fitness); 
			// see dependencies for all true/false branches
			ActualControlFlowGraph rcfg = branch.getInstruction().getActualCFG();
			Set<BasicBlock> visitedBlock = new HashSet<BasicBlock>();
			Set<BasicBlock> parents = lookForParent(branch.getInstruction().getBasicBlock(), rcfg, visitedBlock);
			for (BasicBlock bb : parents){
				Branch newB = extractBranch(bb);
				if (newB == null){
					this.rootBranches.add(fitness);
					continue;
				}
				
				BranchCoverageGoal goal = new BranchCoverageGoal(newB, true, newB.getClassName(), newB.getMethodName());
				BranchCoverageTestFitness newFitness = new BranchCoverageTestFitness(goal);
				graph.addEdge((FitnessFunction<T>) newFitness, fitness);

				BranchCoverageGoal goal2 = new BranchCoverageGoal(newB, false, newB.getClassName(), newB.getMethodName());
				BranchCoverageTestFitness newfitness2 = new BranchCoverageTestFitness(goal2);
				graph.addEdge((FitnessFunction<T>) newfitness2, fitness);
			}
		}
	}
	
	
	public Set<BasicBlock> lookForParent(BasicBlock block, ActualControlFlowGraph acfg, Set<BasicBlock> visitedBlock){
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
	public boolean containsBranches(BasicBlock block){
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
	public Branch extractBranch(BasicBlock block){
		for (BytecodeInstruction inst : block)
			if (inst.isBranch() || inst.isActualBranch())
				return inst.toBranch();
		return null;
	}
	
	public Set<FitnessFunction<T>> getRootBranches(){
		return this.rootBranches;
	}
	
	@SuppressWarnings("unchecked")
	public Set<FitnessFunction<T>> getStructuralChildren(FitnessFunction<T> parent){
		Set<DependencyEdge> outgoingEdges = this.graph.outgoingEdgesOf(parent);
		Set<FitnessFunction<T>> children = new HashSet<FitnessFunction<T>>();
		for (DependencyEdge edge : outgoingEdges){
			children.add((FitnessFunction<T>) edge.getTarget());
		}
		return children;
	}
	
	@SuppressWarnings("unchecked")
	public Set<FitnessFunction<T>> getStructuralParents(FitnessFunction<T> parent){
		Set<DependencyEdge> incomingEdges = this.graph.incomingEdgesOf(parent);
		Set<FitnessFunction<T>> parents = new HashSet<FitnessFunction<T>>();
		for (DependencyEdge edge : incomingEdges){
			parents.add((FitnessFunction<T>) edge.getSource());
		}
		return parents;
	}
}
