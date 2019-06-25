package org.evosuite.coverage.aes.method;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Map;

import org.evosuite.coverage.aes.AbstractAESCoverageSuiteFitness;
import org.evosuite.coverage.aes.Spectrum;
import org.evosuite.coverage.aes.branch.BranchDetails;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.testcase.execution.ExecutionResult;

public class AESMethodCoverageSuiteFitness extends AbstractAESCoverageSuiteFitness {

	private static final long serialVersionUID = 6334385024571769982L;
	
	private List<String> methods;
	
	public AESMethodCoverageSuiteFitness(Metric metric) {
		super(metric);
	}

	public AESMethodCoverageSuiteFitness() {
		this(Metric.AES);
	}
	
	private List<String> determineMethods() {
		if (this.methods == null) {
			this.methods = new ArrayList<String>();
			for (MethodCoverageTestFitness goal : getFactory().getCoverageGoals()) {
				if (!(goal instanceof UnreachableMethodCoverageTestFitness)) {
					this.methods.add(goal.toString());
				}
			}
		}
		return this.methods;
	}

	protected AESMethodCoverageFactory getFactory() {
		return new AESMethodCoverageFactory();
	}

	@Override
	protected Spectrum getSpectrum(List<ExecutionResult> results) {
		List<String> methods = determineMethods();
		Spectrum spectrum = new Spectrum(results.size(), methods.size());
		
		for (int t = 0; t < results.size(); t++) {			
			for (String coveredMethod : results.get(t).getTrace().getCoveredMethods()) {
				coveredMethod = "[METHOD] " + coveredMethod;                                      //most important change
				spectrum.setInvolved(t, methods.indexOf(coveredMethod));
				
			}
		}
		
		return spectrum;
	}

    protected Map<Integer,Double> getWeights()
    {
        return null;
    }
}
