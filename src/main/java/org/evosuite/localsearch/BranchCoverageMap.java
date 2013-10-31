package org.evosuite.localsearch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.SearchListener;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

public class BranchCoverageMap implements SearchListener {

	public static BranchCoverageMap instance = null;
	
	private Map<Integer, TestCase> coveredTrueBranches;

	private Map<Integer, TestCase> coveredFalseBranches;

	private BranchCoverageMap() {
		
	}
	
	public static BranchCoverageMap getInstance() {
		if(instance == null)
			instance = new BranchCoverageMap();
		
		return instance;
	}
	
	public boolean isCoveredTrue(int branchId) {
		return coveredTrueBranches.containsKey(branchId);
	}

	public boolean isCoveredFalse(int branchId) {
		return coveredFalseBranches.containsKey(branchId);
	}
	
	public TestCase getTestCoveringTrue(int branchId) {
		return coveredTrueBranches.get(branchId);
	}

	public TestCase getTestCoveringFalse(int branchId) {
		return coveredFalseBranches.get(branchId);
	}
	
	public Set<Integer> getCoveredTrueBranches() {
		return coveredTrueBranches.keySet();
	}

	public Set<Integer> getCoveredFalseBranches() {
		return coveredFalseBranches.keySet();
	}

	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {
		coveredTrueBranches  = new LinkedHashMap<Integer, TestCase>();
		coveredFalseBranches = new LinkedHashMap<Integer, TestCase>();		
	}

	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		
	}

	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		coveredTrueBranches  = null;
		coveredFalseBranches = null;
	}

	@Override
	public void fitnessEvaluation(Chromosome individual) {
		if(individual instanceof TestSuiteChromosome) {
			TestSuiteChromosome suite = (TestSuiteChromosome)individual;
			for(TestChromosome testChromosome : suite.getTestChromosomes()) {
				ExecutionResult lastResult = testChromosome.getLastExecutionResult();
				if(lastResult != null) {
					for(Integer branchId : lastResult.getTrace().getCoveredTrueBranches()) {
						if(!coveredTrueBranches.containsKey(branchId)) {
							coveredTrueBranches.put(branchId, testChromosome.getTestCase());
						}
					}
					for(Integer branchId : lastResult.getTrace().getCoveredFalseBranches()) {
						if(!coveredFalseBranches.containsKey(branchId)) {
							coveredFalseBranches.put(branchId, testChromosome.getTestCase());
						}
					}
				}
			}
		}
		
	}

	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub
		
	}

	
	
}
