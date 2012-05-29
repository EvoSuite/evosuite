/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.util.List;

import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * This adapters allows the use of a TestSuiteFitnessFunction as a TestFitnessFactory for the purpose of TestSuite minimization.
 * @author Sebastian Steenbuck
 *
 */
public class TestSuiteFitnessFunc_to_TestFitnessFactory_Adapter implements TestFitnessFactory{

	private final TestSuiteFitnessFunction testSuiteFitness;
	public TestSuiteFitnessFunc_to_TestFitnessFactory_Adapter(TestSuiteFitnessFunction testSuiteFitness){
		this.testSuiteFitness=testSuiteFitness;
	}
	
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getFitness(TestSuiteChromosome suite) {
		return testSuiteFitness.getFitness(suite);
	}

}
