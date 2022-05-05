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
package org.evosuite.testsuite;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.localsearch.TestSuiteLocalSearch;

import java.util.*;

import static java.util.stream.Collectors.toCollection;

/**
 * <p>
 * TestSuiteChromosome class.
 * </p>
 *
 * @author Gordon Fraser
 * <p>
 * Final in order to prevent breaking of self type.
 */
public final class TestSuiteChromosome
        extends AbstractTestSuiteChromosome<TestSuiteChromosome, TestChromosome> {

    /**
     * Secondary objectives used during ranking
     */
    private static final List<SecondaryObjective<TestSuiteChromosome>>
            secondaryObjectives = new ArrayList<>();
    private static int secondaryObjIndex = 0;
    private static final long serialVersionUID = 88380759969800800L;

    /**
     * Add an additional secondary objective to the end of the list of
     * objectives
     *
     * @param objective a {@link org.evosuite.ga.SecondaryObjective} object.
     */
    public static void addSecondaryObjective(SecondaryObjective<TestSuiteChromosome> objective) {
        secondaryObjectives.add(objective);
    }

    public static int getSecondaryObjectivesSize() {
        return secondaryObjectives.size();
    }

    public static boolean isFirstSecondaryObjectiveEnabled() {
        return secondaryObjIndex == 0;
    }

    public static void disableFirstSecondaryObjective() {
        if (secondaryObjIndex != 1)
            secondaryObjIndex = 1;
    }

    public static void enableFirstSecondaryObjective() {
        if (secondaryObjIndex != 0)
            secondaryObjIndex = 0;
    }

    public static void reverseSecondaryObjective() {
        Collections.reverse(secondaryObjectives);
    }

    /**
     * Remove secondary objective from list, if it is there
     *
     * @param objective a {@link org.evosuite.ga.SecondaryObjective} object.
     */
    public static void removeSecondaryObjective(SecondaryObjective<TestSuiteChromosome> objective) {
        secondaryObjectives.remove(objective);
    }

    public static void removeAllSecondaryObjectives() {
        secondaryObjectives.clear();
    }

    @Override
    public TestSuiteChromosome self() {
        return this;
    }

    /**
     * <p>
     * Constructor for TestSuiteChromosome.
     * </p>
     */
    public TestSuiteChromosome() {
        super();
    }


    /**
     * <p>
     * Constructor for TestSuiteChromosome.
     * </p>
     *
     * @param testChromosomeFactory a {@link org.evosuite.ga.ChromosomeFactory} object.
     */
    public TestSuiteChromosome(ChromosomeFactory<TestChromosome> testChromosomeFactory) {
        super(testChromosomeFactory);
    }

    /**
     * <p>
     * Constructor for TestSuiteChromosome.
     * </p>
     *
     * @param source a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
     */
    protected TestSuiteChromosome(TestSuiteChromosome source) {
        super(source);
    }

    /**
     * Add a test to a test suite
     *
     * @param test a {@link org.evosuite.testcase.TestCase} object.
     */
    public TestChromosome addTest(TestCase test) {
        TestChromosome c = new TestChromosome();
        c.setTestCase(test);
        addTest(c);

        return c;
    }

    @Override
    public void addTestChromosome(TestChromosome testChromosome) {
        this.addTest(testChromosome);
    }


    public void clearMutationHistory() {
        tests.forEach(t -> t.getMutationHistory().clear());
    }


    /**
     * {@inheritDoc}
     * <p>
     * Create a deep copy of this test suite
     *
     * @return
     */
    @Override
    public TestSuiteChromosome clone() {
        return new TestSuiteChromosome(this);
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.Chromosome#compareSecondaryObjective(org.evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareSecondaryObjective(TestSuiteChromosome o) {
        double compare = 0;
        for(SecondaryObjective<TestSuiteChromosome> so : secondaryObjectives) {
            compare += so.compareChromosomes(this.self(), o);
        }

        return (int) Math.signum(compare);
    }


    /**
     * For manual algorithm
     *
     * @param testCase to remove
     */
    public void deleteTest(TestCase testCase) {
        if (testCase != null) {
            tests.removeIf(t -> t.getTestCase().equals(testCase));
        }
    }

    /**
     * <p>
     * getCoveredGoals
     * </p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<TestFitnessFunction> getCoveredGoals() {
        Set<TestFitnessFunction> goals = new LinkedHashSet<>();
        for (TestChromosome t : tests) {
            Set<TestFitnessFunction> coveredGoals = t.getTestCase().getCoveredGoals();
            goals.addAll(coveredGoals);
        }
        return goals;
    }

    public void removeCoveredGoal(TestFitnessFunction f) {
        tests.forEach(t -> t.getTestCase().removeCoveredGoal(f));
    }

    /**
     * <p>
     * getTests
     * </p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<TestCase> getTests() {
        return tests.stream()
                .map(TestChromosome::getTestCase)
                .collect(toCollection(ArrayList::new));
    }


    @Override
    public boolean localSearch(LocalSearchObjective<TestSuiteChromosome> objective) {
        TestSuiteLocalSearch localSearch = TestSuiteLocalSearch.selectTestSuiteLocalSearch();
        return localSearch.doSearch(this, objective);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Apply mutation on test suite level
     */
    @Override
    public void mutate() {
        for (int i = 0; i < Properties.NUMBER_OF_MUTATIONS; i++) {
            super.mutate();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Determine relative ordering of this chromosome to another chromosome If
     * fitness is equal, the shorter chromosome comes first
     */
    /*
     * public int compareTo(Chromosome o) { if(RANK_LENGTH && getFitness() ==
     * o.getFitness()) { return (int) Math.signum((length() -
     * ((TestSuiteChromosome)o).length())); } else return (int)
     * Math.signum(getFitness() - o.getFitness()); }
     */
    @Override
    public String toString() {
        String result = "TestSuite: " + tests.size() + "\n";
        int i = 0;
        for (TestChromosome test : tests) {
            result += "Test " + i + ": \n";
            i++;
            if (test.getLastExecutionResult() != null) {
                result += test.getTestCase().toCode(test.getLastExecutionResult().exposeExceptionMapping());
            } else {
                result += test.getTestCase().toCode() + "\n";
            }
        }
        return result;
    }

    /**
     * Optimize a test smell metric
     * @param testSmell Test smell metric to be optimized
     * @return double with the test smell score
     */
    public double calculateSmellValuesTestSuite (AbstractTestSmell testSmell){

        if(!this.smellValues.containsKey(testSmell.getName())){

            double specificSmellScore = testSmell.computeTestSmellMetric(this);
            if (!Double.isNaN(specificSmellScore)){
                this.smellValues.put(testSmell.getName(), specificSmellScore);
                return specificSmellScore;
            } else {
                return 0;
            }
        }

        return this.smellValues.get(testSmell.getName());
    }

    /**
     * Optimize test smell metrics
     * @param listOfTestSmells A list with the test smell metrics that will be optimized
     * @return double with the total test smell score
     */
	public double calculateSmellValuesTestSuiteUnrestricted (List<AbstractTestSmell> listOfTestSmells){

	    double smellScore = 0;

        for (AbstractTestSmell testSmell : listOfTestSmells){
            double specificSmellScore = testSmell.computeTestSmellMetric(this);
            if (!Double.isNaN(specificSmellScore)){
                smellScore += specificSmellScore;
            }
        }

        return smellScore;
    }

	/**
	 * Get the score for each test smell metric
	 * @return LinkedHashMap containing the metrics and the respective scores
	 */
	public LinkedHashMap<String, Double> getSmellValuesTestSuite (){
		return this.smellValues;
	}

}
