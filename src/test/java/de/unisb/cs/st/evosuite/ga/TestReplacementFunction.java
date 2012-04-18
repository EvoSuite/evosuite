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
