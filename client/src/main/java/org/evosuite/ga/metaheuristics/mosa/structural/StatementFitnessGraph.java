package org.evosuite.ga.metaheuristics.mosa.structural;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.statement.StatementCoverageTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.graphs.cfg.BasicBlock;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatementFitnessGraph<T extends Chromosome, V extends FitnessFunction<T>>{

	private static final Logger logger = LoggerFactory.getLogger(StatementFitnessGraph.class);

	protected DefaultDirectedGraph<FitnessFunction<T>, DependencyEdge> graph = new DefaultDirectedGraph<FitnessFunction<T>, DependencyEdge>(DependencyEdge.class);

	protected Set<FitnessFunction<T>> rootStatements = new HashSet<FitnessFunction<T>>();

	protected BranchFitnessGraph<T, FitnessFunction<T>> branchGraph;

	@SuppressWarnings("unchecked")
	public StatementFitnessGraph(Set<FitnessFunction<T>> goals){
		for (FitnessFunction<T> fitness : goals){
			graph.addVertex(fitness);
		}
		
		this.initializeBranchGraph();

		// derive root statements
		for (FitnessFunction<T> fitness : branchGraph.rootBranches){
			BranchCoverageTestFitness branchFitness = (BranchCoverageTestFitness) fitness;
			if (branchFitness.getBranch() == null)
				continue;
			BasicBlock bb = branchFitness.getBranch().getInstruction().getBasicBlock();
			for (BytecodeInstruction instr : bb){
				StatementCoverageTestFitness newStmt = new StatementCoverageTestFitness(instr.getClassName(), instr.getMethodName(), instr.getInstructionId());
				if (this.graph.containsVertex((FitnessFunction<T>) newStmt))
					this.rootStatements.add((FitnessFunction<T>) newStmt);
			}
		}

		// derive dependencies among branches
		for (FitnessFunction<T> fitness : goals){
			StatementCoverageTestFitness stmt = (StatementCoverageTestFitness) fitness;
			for (BranchCoverageTestFitness branchFitness : stmt.getBranchFitnesses()){
				if (branchFitness.getBranch() == null){
					this.rootStatements.add(fitness); 
					continue;
				}
				if (branchFitness.getBranch().getInstruction().isRootBranchDependent() 
						|| branchFitness.getBranch().getInstruction().isDirectlyControlDependentOn(null))
					this.rootStatements.add(fitness); 

				// derive from ActualControlFlowGraph
				ActualControlFlowGraph acfg = branchFitness.getBranch().getInstruction().getActualCFG();
				Set<BasicBlock> bbs = acfg.getParents(stmt.getGoalInstruction().getBasicBlock());
				for (BasicBlock bb : bbs){
					if (bb.equals(stmt.getGoalInstruction().getBasicBlock()))
						continue;
					for (BytecodeInstruction instr : bb){
						StatementCoverageTestFitness newStmt = new StatementCoverageTestFitness(instr.getClassName(), instr.getMethodName(), instr.getInstructionId());
						if (this.graph.containsVertex((FitnessFunction<T>) newStmt))
							graph.addEdge((FitnessFunction<T>) newStmt, fitness);
					}
				}
				
				List<FitnessFunction<T>> visitedBranches = new ArrayList<FitnessFunction<T>>();
				// iterate over the current targets 
				LinkedList<FitnessFunction<T>> parents = new LinkedList<FitnessFunction<T>>();
				parents.addAll(branchGraph.getStructuralParents((FitnessFunction<T>) branchFitness));

				while(parents.size()>0){
					FitnessFunction<T> fitnessFunction = parents.poll();
					if (visitedBranches.contains(fitnessFunction))
						continue;

					visitedBranches.add(fitnessFunction);
					BranchCoverageTestFitness branchFf = (BranchCoverageTestFitness) fitnessFunction;
					BasicBlock bb = branchFf.getBranch().getInstruction().getBasicBlock();
					if (bb.equals(stmt.getGoalInstruction().getBasicBlock()))
						continue;
					for (BytecodeInstruction instr : bb){
						StatementCoverageTestFitness newStmt = new StatementCoverageTestFitness(instr.getClassName(), instr.getMethodName(), instr.getInstructionId());
						if (this.graph.containsVertex((FitnessFunction<T>) newStmt))
							graph.addEdge((FitnessFunction<T>) newStmt, fitness);
					}
				} // end visiting the Graph

			} // end for each infected branch

		}
	}

	@SuppressWarnings("unchecked")
	public Set<FitnessFunction<T>> getStructuralChildren(FitnessFunction<T> parent){
		if (!graph.containsVertex(parent))
			logger.error("{}",parent);
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

	@SuppressWarnings("unchecked")
	protected void initializeBranchGraph(){
		List<BranchCoverageTestFitness> branches = new BranchCoverageFactory().getCoverageGoals();
		Set<FitnessFunction<T>> set = new HashSet<FitnessFunction<T>>();
		for (BranchCoverageTestFitness branch : branches)
			set.add((FitnessFunction<T>) branch);
		this.branchGraph =  new BranchFitnessGraph<T, FitnessFunction<T>>(set);
	}
}
