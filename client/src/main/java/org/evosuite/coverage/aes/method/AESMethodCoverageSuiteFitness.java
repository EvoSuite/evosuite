/*
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
package org.evosuite.coverage.aes.method;

import java.util.ArrayList;
import java.util.List;
import org.evosuite.coverage.aes.AbstractAESCoverageSuiteFitness;
import org.evosuite.coverage.aes.Spectrum;
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

}
