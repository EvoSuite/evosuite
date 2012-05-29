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
package de.unisb.cs.st.evosuite.ga;

import org.junit.*;
import java.util.*;

import de.unisb.cs.st.evosuite.testcase.*;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteReplacementFunction;
import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Andrea Arcuri
 * 
 */

public class TestReplacementFunction {
	
	public static final boolean defaultCheckParentLength = Properties.CHECK_PARENTS_LENGTH;
	
	
	@After
	public void reset(){
		Properties.CHECK_PARENTS_LENGTH = defaultCheckParentLength;
	}
	
	@Test
	public void testParentReplacement(){
		
		FakeTestSuiteChromosome offspring1 = new FakeTestSuiteChromosome(2,10);
		FakeTestSuiteChromosome offspring2 = new FakeTestSuiteChromosome(1,10);
		FakeTestSuiteChromosome parent1 = new FakeTestSuiteChromosome(2,5);
		FakeTestSuiteChromosome parent2 = new FakeTestSuiteChromosome(2,5);
		
		ReplacementFunction SUT = new TestSuiteReplacementFunction();
		
		Properties.CHECK_PARENTS_LENGTH = false;
		boolean result = SUT.keepOffspring(parent1, parent2, offspring1, offspring2);
		Assert.assertTrue(result);

		Properties.CHECK_PARENTS_LENGTH = true;
		result = SUT.keepOffspring(parent1, parent2, offspring1, offspring2);
		Assert.assertTrue(!result);
	}
	
	
	
	private class FakeTestSuiteChromosome extends TestSuiteChromosome{
	
		private int code;
		private int length;
		
		public FakeTestSuiteChromosome(int c,int l){
			code = c;
			length = l;
		}
		
		@Override
		public int totalLengthOfTestCases(){
			return length;
		}
		
		@Override
		public int compareTo(Chromosome other){
			int k = ((FakeTestSuiteChromosome)other).code;
			return code-k;
		}
	}
}
