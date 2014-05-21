package org.evosuite.result;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;

class TestGenerationResultImpl implements TestGenerationResult {

	private static final long serialVersionUID = 1306033906557741929L;

	private Status status = Status.ERROR;
	
	private String errorMessage = "";
	
	private Map<String, Set<Failure>> contractViolations = new LinkedHashMap<String, Set<Failure>>();
	
	private Map<String, TestCase> testCases = new LinkedHashMap<String, TestCase>();
	
	private Map<String, String> testCode = new LinkedHashMap<String, String>();
	
	private Map<String, Set<Integer>> testLineCoverage = new LinkedHashMap<String, Set<Integer>>();

	private Map<String, Set<BranchInfo>> testBranchCoverage = new LinkedHashMap<String, Set<BranchInfo>>();

	private Map<String, Set<MutationInfo>> testMutantCoverage = new LinkedHashMap<String, Set<MutationInfo>>();

	private Set<Integer> coveredLines = new LinkedHashSet<Integer>();

	private Set<Integer> uncoveredLines = new LinkedHashSet<Integer>();

	private Set<BranchInfo> coveredBranches = new LinkedHashSet<BranchInfo>();

	private Set<BranchInfo> uncoveredBranches = new LinkedHashSet<BranchInfo>();

	private Set<MutationInfo> coveredMutants = new LinkedHashSet<MutationInfo>();

	private Set<MutationInfo> uncoveredMutants = new LinkedHashSet<MutationInfo>();
	
	private Set<MutationInfo> exceptionMutants = new LinkedHashSet<MutationInfo>();

	private Map<String, String> testComments = new LinkedHashMap<String, String>();
	
	private String testSuiteCode = "";
	
	private String targetClass = "";
	
	private String targetCriterion = "";
	
	private double targetCoverage = 0.0;
	
	private GeneticAlgorithm<?> ga = null;
	
	/** Did test generation succeed? */
	public Status getTestGenerationStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	/** If there was an error, this contains the error message */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		status = Status.ERROR;
		this.errorMessage = errorMessage;
	}
	
	/** The entire GA in its final state */
	public GeneticAlgorithm<?> getGeneticAlgorithm() {
		return ga;
	}
	
	public void setGeneticAlgorithm(GeneticAlgorithm<?> ga) {
		this.ga = ga;
	}
	
	/** Map from test method to ContractViolation */
	public Set<Failure> getContractViolations(String name)  {
		return contractViolations.get(name);
	}
	
	public void setContractViolations(String name, Set<Failure> violations) {
		contractViolations.put(name, violations);
	}
	
	public void setClassUnderTest(String targetClass) {
		this.targetClass = targetClass;
	}
	
	@Override
	public String getClassUnderTest() {
		return targetClass;
	}
	
	public void setTargetCoverage(double coverage) {
		this.targetCoverage = coverage;
	}
	
	@Override
	public double getTargetCoverage() {
		return targetCoverage;
	}
	
	@Override
	public String getTargetCriterion() {
		return targetCriterion;
	}
	
	public void setTargetCriterion(String criterion) {
		this.targetCriterion = criterion;
	}
	
	/** Map from test method to EvoSuite test case */
	public TestCase getTestCase(String name) {
		return testCases.get(name);
	}
	
	public void setTestCase(String name, TestCase test) {
		testCases.put(name,  test);
	}

	/** Map from test method to EvoSuite test case */
	public String getTestCode(String name) {
		return testCode.get(name);
	}
	
	public void setTestCode(String name, String code) {
		testCode.put(name, code);
	}

	/** JUnit test suite source code */
	public String getTestSuiteCode() {
		return testSuiteCode;
	}
	
	public void setTestSuiteCode(String code) {
		this.testSuiteCode = code;
	}

	/** Lines covered by final test suite */ 
	public Set<Integer> getCoveredLines() {
		return coveredLines;
	}
	
	public void setCoveredLines(String name, Set<Integer> covered) {
		testLineCoverage.put(name, covered);
		coveredLines.addAll(covered);
	}

	public void setCoveredBranches(String name, Set<BranchInfo> covered) {
		testBranchCoverage.put(name, covered);
		coveredBranches.addAll(covered);
	}

	public void setCoveredMutants(String name, Set<MutationInfo> covered) {
		testMutantCoverage.put(name, covered);
		coveredMutants.addAll(covered);
	}

	@Override
	public String getComment(String name) {
		return testComments.get(name);
	}
	
	public void setComment(String name, String comment) {
		testComments.put(name, comment);
	}

	@Override
	public Set<Integer> getCoveredLines(String name) {
		return testLineCoverage.get(name);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(String testName : testCases.keySet()) {
			builder.append("Test "+testName+": \n");
			builder.append(" "+testLineCoverage.get(testName));
			builder.append("\n");
			builder.append(" "+testBranchCoverage.get(testName));
			builder.append("\n");
			builder.append(" "+testMutantCoverage.get(testName));
			builder.append("\n");
		}
		builder.append("Uncovered lines: ");
		builder.append(uncoveredLines.toString());
		builder.append("\n");
		builder.append("Uncovered branches: ");
		builder.append(uncoveredBranches.toString());
		builder.append("\n");
		builder.append("Uncovered mutants: "+uncoveredMutants.size());
		builder.append("\n");
		builder.append("Covered mutants: "+coveredMutants.size());
		builder.append("\n");
		builder.append("Timeout mutants: "+exceptionMutants.size());
		builder.append("\n");
		builder.append("Failures: "+contractViolations);
		builder.append("\n");
		return builder.toString();
	}

	@Override
	public Set<BranchInfo> getCoveredBranches(String name) {
		return testBranchCoverage.get(name);
	}

	@Override
	public Set<MutationInfo> getCoveredMutants(String name) {
		return testMutantCoverage.get(name);
	}

	@Override
	public Set<BranchInfo> getCoveredBranches() {
		return coveredBranches;
	}

	@Override
	public Set<MutationInfo> getCoveredMutants() {
		return coveredMutants;
	}

	@Override
	public Set<Integer> getUncoveredLines() {
		return uncoveredLines;
	}
	
	public void setUncoveredLines(Set<Integer> lines) {
		uncoveredLines.addAll(lines);
	}

	@Override
	public Set<BranchInfo> getUncoveredBranches() {
		return uncoveredBranches;
	}

	public void setUncoveredBranches(Set<BranchInfo> branches) {
		uncoveredBranches.addAll(branches);
	}

	@Override
	public Set<MutationInfo> getUncoveredMutants() {
		return uncoveredMutants;
	}
	
	@Override
	public Set<MutationInfo> getExceptionMutants() {
		return exceptionMutants;
	}

	public void setExceptionMutants(Set<MutationInfo> mutants) {
		exceptionMutants.addAll(mutants);
	}

	public void setUncoveredMutants(Set<MutationInfo> mutants) {
		uncoveredMutants.addAll(mutants);
	}

}
