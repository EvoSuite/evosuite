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
package org.evosuite.result;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.assertion.Assertion;
import org.evosuite.contracts.ContractViolation;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.result.TestGenerationResult.Status;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.utils.LoggingUtils;

public class TestGenerationResultBuilder {

	public static TestGenerationResult buildErrorResult(String errorMessage) {
		TestGenerationResultImpl result = new TestGenerationResultImpl();
		result.setStatus(Status.ERROR);
		result.setErrorMessage(errorMessage);
		getInstance().fillInformationFromConfiguration(result);
		getInstance().fillInformationFromTestData(result);
		getInstance().resetTestData();
		return result;
	}

	public static TestGenerationResult buildTimeoutResult() {
		TestGenerationResultImpl result = new TestGenerationResultImpl();
		result.setStatus(Status.TIMEOUT);
		getInstance().fillInformationFromConfiguration(result);
		getInstance().fillInformationFromTestData(result);
		getInstance().resetTestData();
		return result;
	}

	public static TestGenerationResult buildSuccessResult() {
		TestGenerationResultImpl result = new TestGenerationResultImpl();
		result.setStatus(Status.SUCCESS);
		getInstance().fillInformationFromConfiguration(result);
		getInstance().fillInformationFromTestData(result);
		getInstance().resetTestData();
		return result;
	}
	
	private static TestGenerationResultBuilder instance = null;
	
	private TestGenerationResultBuilder() {
		resetTestData();
	}
	
	public static TestGenerationResultBuilder getInstance() {
		if(instance == null)
			instance = new TestGenerationResultBuilder();
		
		return instance;
	}
	
	private void resetTestData() {
		code = "";
		ga = null;
		testCode.clear();
		testCases.clear();
		contractViolations.clear();
		uncoveredLines = LinePool.getAllLines();
		for(Branch b : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllBranches()) {
			uncoveredBranches.add(new BranchInfo(b, true));
			uncoveredBranches.add(new BranchInfo(b, false));
		}
		for(Mutation m : MutationPool.getMutants()) {
			uncoveredMutants.add(new MutationInfo(m));
		}
	}
	
	private void fillInformationFromConfiguration(TestGenerationResultImpl result) {
		result.setClassUnderTest(Properties.TARGET_CLASS);
		String[] criteria = new String[Properties.CRITERION.length];
		for (int i = 0; i < Properties.CRITERION.length; i++)
		    criteria[i] = Properties.CRITERION[i].name();
		result.setTargetCriterion(criteria);
	}
	
	private void fillInformationFromTestData(TestGenerationResultImpl result) {
		
		Set<MutationInfo> exceptionMutants = new LinkedHashSet<MutationInfo>();
		for(Mutation m : MutationPool.getMutants()) {
			if(MutationTimeoutStoppingCondition.isDisabled(m)) {
				MutationInfo info = new MutationInfo(m);
				exceptionMutants.add(info);
				uncoveredMutants.remove(info);
			}
		}
		
		for(String test : testCode.keySet()) {
			result.setTestCode(test, testCode.get(test));
			result.setTestCase(test, testCases.get(test));
			result.setContractViolations(test, contractViolations.get(test));
			result.setCoveredLines(test, testLineCoverage.get(test));
			result.setCoveredBranches(test, testBranchCoverage.get(test));
			result.setCoveredMutants(test, testMutantCoverage.get(test));
			result.setComment(test, testComments.get(test));
		}
		
		result.setUncoveredLines(uncoveredLines);
		result.setUncoveredBranches(uncoveredBranches);
		result.setUncoveredMutants(uncoveredMutants);
		result.setExceptionMutants(exceptionMutants);
		result.setTestSuiteCode(code);
		result.setGeneticAlgorithm(ga);
        for (Map.Entry<FitnessFunction<?>, Double> e : targetCoverages.entrySet()) {
            result.setTargetCoverage(e.getKey(), e.getValue());
        }

	}
	
	private String code = "";
	
	private GeneticAlgorithm<?> ga = null;
	
	private Map<String, String> testCode = new LinkedHashMap<String, String>();

	private Map<String, TestCase> testCases = new LinkedHashMap<String, TestCase>();
	
	private Map<String, String> testComments = new LinkedHashMap<String, String>();

	private Map<String, Set<Integer>> testLineCoverage = new LinkedHashMap<String, Set<Integer>>();

	private Map<String, Set<BranchInfo>> testBranchCoverage = new LinkedHashMap<String, Set<BranchInfo>>();

	private Map<String, Set<MutationInfo>> testMutantCoverage = new LinkedHashMap<String, Set<MutationInfo>>();

	private Map<String, Set<Failure>> contractViolations = new LinkedHashMap<String, Set<Failure>>();
	
	private Set<Integer> uncoveredLines = LinePool.getAllLines();
	
	private Set<BranchInfo> uncoveredBranches = new LinkedHashSet<BranchInfo>();

	private Set<MutationInfo> uncoveredMutants = new LinkedHashSet<MutationInfo>();

    private LinkedHashMap<FitnessFunction<?>, Double> targetCoverages = new LinkedHashMap<FitnessFunction<?>, Double>();
	
	public void setTestCase(String name, String code, TestCase testCase, String comment, ExecutionResult result) {
		testCode.put(name, code);
		testCases.put(name, testCase);
		Set<Failure> failures = new LinkedHashSet<Failure>();
		for(ContractViolation violation : testCase.getContractViolations()) {
			failures.add(new Failure(violation));
		}
		if(!Properties.CHECK_CONTRACTS && result.hasUndeclaredException()) {
			int position = result.getFirstPositionOfThrownException();
			Throwable exception = result.getExceptionThrownAtPosition(position);			
			failures.add(new Failure(exception, position, testCase));
		}
		contractViolations.put(name, failures);
		testComments.put(name, comment);
		testLineCoverage.put(name, result.getTrace().getCoveredLines());
		
		uncoveredLines.removeAll(result.getTrace().getCoveredLines());
		
		Set<BranchInfo> branchCoverage = new LinkedHashSet<BranchInfo>();
		for(int branchId : result.getTrace().getCoveredFalseBranches()) {
			Branch branch = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranch(branchId);
			if(branch == null) {
				LoggingUtils.getEvoLogger().warn("Branch is null: "+branchId);
				continue;
			}
			BranchInfo info = new BranchInfo(branch.getClassName(), branch.getMethodName(), branch.getInstruction().getLineNumber(), false);
			branchCoverage.add(info);
		}
		for(int branchId : result.getTrace().getCoveredTrueBranches()) {
			Branch branch = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranch(branchId);
			if(branch == null) {
				LoggingUtils.getEvoLogger().warn("Branch is null: "+branchId);
				continue;
			}
			BranchInfo info = new BranchInfo(branch.getClassName(), branch.getMethodName(), branch.getInstruction().getLineNumber(), true);
			branchCoverage.add(info);
		}
		testBranchCoverage.put(name, branchCoverage);
		uncoveredBranches.removeAll(branchCoverage);
		
		Set<MutationInfo> mutationCoverage = new LinkedHashSet<MutationInfo>();
		for(Assertion assertion : testCase.getAssertions()) {
			for(Mutation m : assertion.getKilledMutations()) {
				mutationCoverage.add(new MutationInfo(m));
			}
		}
		testMutantCoverage.put(name, mutationCoverage);
		uncoveredMutants.removeAll(mutationCoverage);
	}
	
	public void setTestSuiteCode(String code) {
		this.code = code;
	}
	
	public void setGeneticAlgorithm(GeneticAlgorithm<?> ga) {
		this.ga = ga;
        for (Map.Entry<FitnessFunction<?>, Double> e : ga.getBestIndividual().getCoverageValues().entrySet()) {
            targetCoverages.put(e.getKey(), e.getValue());
        }
	}
}
