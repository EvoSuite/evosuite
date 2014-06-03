package org.evosuite.result;

import java.io.Serializable;
import java.util.Set;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;

public interface TestGenerationResult extends Serializable {

	public enum Status { SUCCESS, TIMEOUT, ERROR };
	
	/** Did test generation succeed? */
	public Status getTestGenerationStatus();
	
	/** If there was an error, this contains the error message */
	public String getErrorMessage();
	
	/** The entire GA in its final state */
	public GeneticAlgorithm<?> getGeneticAlgorithm();
	
	/** Map from test method to ContractViolation */
	public Set<Failure> getContractViolations(String name);
	
	/** Class that was tested */
	public String getClassUnderTest();
	
	/** Target coverage criterion used to create this test suite */
	public String getTargetCriterion();
	
	/** Coverage level of the target criterion */
	public double getTargetCoverage();
	
	/** Map from test method to EvoSuite test case */
	public TestCase getTestCase(String name);

	/** Map from test method to EvoSuite test case */
	public String getTestCode(String name);
	
	/** JUnit test suite source code */
	public String getTestSuiteCode();
	
	/** Lines covered by test */ 
	public Set<Integer> getCoveredLines(String name);
	
	public Set<BranchInfo> getCoveredBranches(String name);

	public Set<MutationInfo> getCoveredMutants(String name);

	public Set<MutationInfo> getExceptionMutants();

	/** Lines covered by final test suite */ 
	public Set<Integer> getCoveredLines();

	/** Branches covered by final test suite */ 
	public Set<BranchInfo> getCoveredBranches();

	/** Mutants detected by final test suite */ 
	public Set<MutationInfo> getCoveredMutants();

	/** Lines not covered by final test suite */ 
	public Set<Integer> getUncoveredLines();

	/** Branches not covered by final test suite */ 
	public Set<BranchInfo> getUncoveredBranches();

	/** Mutants not detected by final test suite */ 
	public Set<MutationInfo> getUncoveredMutants();

	/** Comment for that test */
	public String getComment(String name);

}
