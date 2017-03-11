package org.evosuite.ga.metaheuristics.mosa.structural;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.mutation.StrongMutationTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.graphs.cfg.BasicBlock;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrongMutationGraph<T extends Chromosome, V extends FitnessFunction<T>>{

	private static final Logger logger = LoggerFactory.getLogger(StrongMutationGraph.class);

	protected DefaultDirectedGraph<FitnessFunction<T>, DependencyEdge> graph = new DefaultDirectedGraph<FitnessFunction<T>, DependencyEdge>(DependencyEdge.class);

	protected Set<FitnessFunction<T>> rootMutants = new HashSet<FitnessFunction<T>>();

	protected BranchFitnessGraph<T, FitnessFunction<T>> branchGraph;

	protected Map<BranchCoverageTestFitness, Set<StrongMutationTestFitness>> infectedBranches = new HashMap<BranchCoverageTestFitness, Set<StrongMutationTestFitness>>();

	@SuppressWarnings("unchecked")
	public StrongMutationGraph(Set<FitnessFunction<T>> goals){
		for (FitnessFunction<T> fitness : goals){
			graph.addVertex(fitness);
		}

		this.initializeBranchGraph();
		this.deriveInfectedBranches(goals);

		// derive structural dependencies among mutations
		for (FitnessFunction<T> fitness : goals){
			StrongMutationTestFitness mutant = (StrongMutationTestFitness) fitness;
			List<FitnessFunction<T>> visitedBranches = new ArrayList<FitnessFunction<T>>();

			LinkedList<FitnessFunction<T>> branches = new LinkedList<FitnessFunction<T>>();
			for(BranchCoverageGoal branchGoal:  mutant.getMutation().getControlDependencies()){
				BranchCoverageTestFitness branch = new BranchCoverageTestFitness(branchGoal);
				for (FitnessFunction<T> child : branchGraph.getStructuralChildren((FitnessFunction<T>) branch))
					branches.addLast(child);
			}
	
			while (branches.size()>0){
				FitnessFunction<T> branch = branches.poll();
				if (visitedBranches.contains(branch))
					continue;

				visitedBranches.add(branch);
				if (this.infectedBranches.containsKey(branch)){
					for (StrongMutationTestFitness strong : this.infectedBranches.get(branch))
						this.graph.addEdge(fitness, (FitnessFunction<T>) strong);
				} else {
					for (FitnessFunction<T> child : branchGraph.getStructuralChildren(branch))
						branches.addLast(child);
				}
			}
		}// end for each mutation
		
		this.deriveRootMutants();
	}

	protected void initializeBranchGraph(){
		List<BranchCoverageTestFitness> branches = new BranchCoverageFactory().getCoverageGoals();
		Set<FitnessFunction<T>> set = new HashSet<FitnessFunction<T>>();
		for (BranchCoverageTestFitness branch : branches)
			set.add((FitnessFunction<T>) branch);
		this.branchGraph =  new BranchFitnessGraph<T, FitnessFunction<T>>(set);

	}

	protected void deriveInfectedBranches(Set<FitnessFunction<T>> goals){
		for (FitnessFunction<T> fitness : goals){
			StrongMutationTestFitness mutant = (StrongMutationTestFitness) fitness;
			// first we derive mutations affecting  root branches
			Set<BranchCoverageGoal> controlDependencies = mutant.getMutation().getControlDependencies();
			for(BranchCoverageGoal branch: controlDependencies){
				BranchCoverageTestFitness branchFitness = new BranchCoverageTestFitness(branch);
				// let's fill the map
				if (infectedBranches.containsKey(branchFitness))
					this.infectedBranches.get(branchFitness).add(mutant);
				else {
					Set<StrongMutationTestFitness> mutations = new HashSet<StrongMutationTestFitness>();
					mutations.add(mutant);
					this.infectedBranches.put(branchFitness, mutations);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void deriveRootMutants(){
		List<FitnessFunction<T>> visitedBranches = new ArrayList<FitnessFunction<T>>();
		LinkedList<FitnessFunction<T>> branches = new LinkedList<FitnessFunction<T>>();
		branches.addAll(branchGraph.rootBranches);

		while (branches.size()>0){
			FitnessFunction<T> branch = branches.poll();
			if (visitedBranches.contains(branch))
				continue;

			visitedBranches.add(branch);
			if (this.infectedBranches.containsKey(branch)){
				for (StrongMutationTestFitness strong : this.infectedBranches.get(branch))
					this.rootMutants.add((FitnessFunction<T>) strong);
			} else {
				for (FitnessFunction<T> child : branchGraph.getStructuralChildren(branch))
					branches.addLast(child);
			}
		}
		for (FitnessFunction<T> fitnessFunction : graph.vertexSet()){
			StrongMutationTestFitness strong = (StrongMutationTestFitness) fitnessFunction;
			if (strong.getMutation().getControlDependencies().size() == 0 || graph.inDegreeOf(fitnessFunction) == 0)
				this.rootMutants.add(fitnessFunction);
		}
		//logger.error("Number of Root {}", rootMutants.size());
	}

	public Set<FitnessFunction<T>> getRootTargets(){
		return this.rootMutants;
	}

	public Set<FitnessFunction<T>> getStructuralChildren(FitnessFunction<T> parent){
		Set<DependencyEdge> outgoingEdges = this.graph.outgoingEdgesOf(parent);
		Set<FitnessFunction<T>> children = new HashSet<FitnessFunction<T>>();
		for (DependencyEdge edge : outgoingEdges){
			children.add((FitnessFunction<T>) edge.getTarget());
		}
		return children;
	}

	public Set<FitnessFunction<T>> getStructuralParents(FitnessFunction<T> parent){
		Set<DependencyEdge> incomingEdges = this.graph.incomingEdgesOf(parent);
		Set<FitnessFunction<T>> parents = new HashSet<FitnessFunction<T>>();
		for (DependencyEdge edge : incomingEdges){
			parents.add((FitnessFunction<T>) edge.getSource());
		}
		return parents;
	}
}
