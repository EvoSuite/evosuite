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

package org.evosuite.eclipse.replace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseMinimizer;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * This class is the fitness function, which calculates for a given test
 * chromosome how different it is to the test case we are trying to replace
 * 
 */
public class DifferenceFitnessFunction extends TestFitnessFunction {

	private static final long serialVersionUID = -7316773344809958156L;

	/** The test we want to replace */
	private final TestCase originalTest;

	/** Set of coverage objectives satisfied by the test we want to replace */
	private final Set<TestFitnessFunction> originalCoveredGoals = new HashSet<TestFitnessFunction>();

	/** Set of coverage objectives not satisfied by the test we want to replace */
	private final Set<TestFitnessFunction> originalUncoveredGoals = new HashSet<TestFitnessFunction>();

	//private Set<Integer> distanceRecord=new HashSet<Integer>();
	//private Set<String> stringRecord=new HashSet<String>();
	private final HashMap distanceRecord = new HashMap();
	private final HashMap stringRecord = new HashMap();
	private final int count = 0;
	private HashMap replaceTokenRecord = new HashMap(); //.get(key)
	private HashMap replaceTypeRecord = new HashMap();
	private HashMap originalTokenRecord = new HashMap(); //.get(key)
	private HashMap originalTypeRecord = new HashMap();
	private int loopNo = 0;
	private NWAlgo nw;

	/**
	 * Constructor to initialize the test we want to replace
	 * 
	 * @param originalTest
	 */
	public DifferenceFitnessFunction(TestCase originalTest, List<TestCase> otherTests,
	        TestFitnessFactory factory) { //step 1
		this.originalTest = originalTest;
		determineOriginalGoals(factory, otherTests);
		System.out.println("Original test covers " + originalCoveredGoals.size()
		        + " goals");
	}

	/**
	 * @return the originalTest
	 */
	public TestCase getOriginalTest() {
		return originalTest;
	}

	/**
	 * Iterate over all coverage goals (e.g. branches) and determine which of
	 * these are covered only by the original test case
	 * 
	 * @param factory
	 */
	private void determineOriginalGoals(TestFitnessFactory factory,
	        List<TestCase> otherTests) { //step 2
		List<TestFitnessFunction> goals = factory.getCoverageGoals();
		for (TestFitnessFunction goal : goals) {
			if (goal.isCovered(originalTest)) {
				if (!goal.isCovered(otherTests)) {
					// This is a goal that is uniquely covered only by the target test
					originalCoveredGoals.add(goal);
					System.out.println("Uniquely covered goal: " + goal);
				}
			} else {
				originalUncoveredGoals.add(goal);
			}
		}
	}

	/**
	 * Return the set of goals the original test covered uniquely
	 * 
	 * @return
	 */
	public Set<TestFitnessFunction> getOriginalGoals() { //enter by TestCaseReplacer
		return originalCoveredGoals;
	}

	/**
	 * Determine how close we are in terms of covering the same goals as the
	 * original test case
	 * 
	 * @param individual
	 * @return
	 */
	private double getCoverage(TestChromosome individual) {
		double similarity = 0.0;

		for (TestFitnessFunction coverageGoal : originalCoveredGoals) {
			similarity += coverageGoal.getFitness(individual);
			// similarity += 1 - normalize(coverageGoal.getFitness(individual));
		}

		return similarity;
	}

	/**
	 * Determine the syntactic distance between the string representation of two
	 * test cases.
	 * 
	 * 
	 * @param individual
	 * @return
	 */
	private double getSyntacticSimilarity(TestChromosome individual) {

		// The method toCode converts a test case to its JUnit representation
		String originalString = originalTest.toCode();
		//System.out.println("\n orgString: "+ originalString);
		// The length of test cases varies during the search
		// therefore the comparison needs to be on a test case
		// where we have removed all the irrelevant statements
		TestCaseMinimizer minimizer = new TestCaseMinimizer(originalCoveredGoals);
		TestChromosome copy = (TestChromosome) individual.clone();
		minimizer.minimize(copy);
		TestCodeVisitor visitor = new TestCodeVisitor();
		copy.getTestCase().accept(visitor);

		TestCodeVisitor workaroundVisitor = new TestCodeVisitor();
		originalTest.accept(workaroundVisitor); //		<-- new add, replace = original

		Set<String> varNames = new HashSet<String>(visitor.getVariableNames());
		varNames.addAll(workaroundVisitor.getVariableNames());

		Set<String> classNames = new HashSet<String>(visitor.getClassNames());
		classNames.addAll(workaroundVisitor.getClassNames());

		String newString = visitor.getCode(); //System.out.println(newString);
		//System.out.println(classNames +"var: " +varNames );
		//String newString = copy.getTestCase().toCode();

		// TODO: Now we need to calculate the syntactic difference
		//       by comparing originalString and newString
		//start

		//getCollection(individual,copy,minimizer );   //mine
		//end

		if (!newString.isEmpty()) {
			if (loopNo == 0) {
				TokenSlicer ts1 = new TokenSlicer(originalString, varNames, classNames);
				originalTokenRecord = ts1.getTokenRecord();
				originalTypeRecord = ts1.getTypeRecord();
				loopNo++;
			}

			TokenSlicer ts2 = new TokenSlicer(newString, varNames, classNames);
			replaceTokenRecord = ts2.getTokenRecord();
			replaceTypeRecord = ts2.getTypeRecord();

			/*
			System.out.println("\noriginal:");
			for(int i=0; i<=originalTokenRecord.size();i++){
				System.out.print(originalTokenRecord.get(i)+ " ");
			}
			System.out.println("\n"+originalTokenRecord +"\n replace: ");
			for(int i=0; i<=replaceTypeRecord.size();i++){
				System.out.print(replaceTypeRecord.get(i)+ " ");
			}*/
			//System.out.println("\nreplaced test token:\n "+replaceTokenRecord);
			//System.out.println("replaced test type: \n"+replaceTypeRecord);
			//System.out.println("replaced test case: "+newString);

			nw = new NWAlgo(originalTokenRecord, replaceTokenRecord, originalTypeRecord,
			        replaceTypeRecord);
			double nwDistance = nw.getDistance();
			return nwDistance;

		}//notEmpty

		return originalString.length() + 10;
	}

	/**
	 * This method calculates the actual fitness value for a given individual
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;

		// TODO: Calculate the fitness value
		// The following are just examples

		// We will want to make sure that the new test case executes 
		// the same code as the original test case. We can achieve this 
		// by checking if it satisfies the same coverage goals.
		double coverage = getCoverage(individual);

		// The test case should look as different as possible to the 
		// last test case, therefore we want to maximize syntactic 
		// distance		
		double similarity = getSyntacticSimilarity(individual); //hv to find the biggest un-similar

		// Now we need to combine the measures somehow to a fitness value
		fitness = coverage + (1.0 - normalize(Math.abs(similarity)));
		// fitness = coverage > 0 ? 1/coverage : 1 + similarity;
		// fitness = 1000 * (1/(coverage+1)) + similarity;
		// ...

		//System.out.println("Fitness: " + fitness);
		//System.out.println("\n\n---------------");
		updateIndividual(this, individual, fitness);
		return fitness;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#isMaximizationFunction()
	 */
	@Override
	public boolean isMaximizationFunction() {
		// If the optimal fitness value is 0, then this needs to be set to true
		return false;
	}

	@Override
	public int compareTo(TestFitnessFunction arg0) {
		return 0;
	}

	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		// TODO Auto-generated method stub
		return null;
	}

}
