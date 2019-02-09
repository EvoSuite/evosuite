package org.evosuite.coverage.aes.branch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.coverage.aes.AbstractAESCoverageSuiteFitness;
import org.evosuite.coverage.aes.Spectrum;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class AESBranchCoverageSuiteFitness extends AbstractAESCoverageSuiteFitness {

	private static final long serialVersionUID = 7409239464436681146L;

	private Map<Integer, Integer> trueMap;
	private Map<Integer, Integer> falseMap;
	private Map<String, Integer> branchlessMethodsMap;
	private int numberOfGoals = 0;

	public AESBranchCoverageSuiteFitness(Metric metric) {
		super(metric);
	}

	public AESBranchCoverageSuiteFitness() {
		this(Metric.AES);
	}

	private void determineCoverageGoals() {
		if(this.branchlessMethodsMap == null || this.trueMap == null || this.falseMap == null) {
			this.branchlessMethodsMap = new HashMap<String, Integer>();
			this.trueMap = new HashMap<Integer, Integer>();
			this.falseMap = new HashMap<Integer, Integer>();

			List<TestFitnessFunction> goals = new AESBranchCoverageFactory().getCoverageGoals();
			this.numberOfGoals = goals.size() - 1;

			for(int g = 0; g < this.numberOfGoals; g++) {
				TestFitnessFunction ff = goals.get(g);

				if (ff instanceof BranchCoverageTestFitness) {
					BranchCoverageTestFitness goal =  (BranchCoverageTestFitness) ff;

					if (goal.getBranch() == null) { // branchless method
						branchlessMethodsMap.put(goal.getClassName() + "." + goal.getMethod(), g);
					} else if (goal.getBranchExpressionValue()) { // true branch
						trueMap.put(goal.getBranch().getActualBranchId(), g);
					} else { // false branch
						falseMap.put(goal.getBranch().getActualBranchId(), g);
					}
				}
			}
		}
	}

	@Override
	protected Spectrum getSpectrum(List<ExecutionResult> results) {
		determineCoverageGoals();
		Spectrum spectrum = new Spectrum(results.size(), this.numberOfGoals);

		for (int t = 0; t < results.size(); t++) {
			ExecutionResult result = results.get(t);

			for(String method : result.getTrace().getCoveredMethods()) {
				if (branchlessMethodsMap.containsKey(method)) {
					spectrum.setInvolved(t, branchlessMethodsMap.get(method));
				}
			}

			for (int trueBranchId : result.getTrace().getCoveredTrueBranches()) {
				if (trueMap.containsKey(trueBranchId)) {
					spectrum.setInvolved(t, trueMap.get(trueBranchId));
				}
			}

			for (int falseBranchId : result.getTrace().getCoveredFalseBranches()) {
				if (falseMap.containsKey(falseBranchId)) {
					spectrum.setInvolved(t, falseMap.get(falseBranchId));
				}
			}

		}

		return spectrum;
	}
}
