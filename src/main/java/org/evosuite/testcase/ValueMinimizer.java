/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.testcase;

import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ValueMinimizer class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class ValueMinimizer implements TestVisitor {

	private static Logger logger = LoggerFactory.getLogger(ValueMinimizer.class);

	private static interface Minimization {
		public boolean isNotWorse();
	}

	private static class TestMinimization implements Minimization {

		private final TestFitnessFunction fitness;

		private final TestChromosome individual;

		private double lastFitness;

		public TestMinimization(TestFitnessFunction fitness, TestChromosome test) {
			this.fitness = fitness;
			this.individual = test;
			this.lastFitness = test.getFitness();
		}

		/* (non-Javadoc)
		 * @see org.evosuite.ga.LocalSearchObjective#hasImproved(org.evosuite.ga.Chromosome)
		 */
		@Override
		public boolean isNotWorse() {
			individual.setChanged(true);
			double newFitness = fitness.getFitness(individual);
			if (newFitness <= lastFitness) { // TODO: Maximize
				lastFitness = newFitness;
				individual.setFitness(lastFitness);
				return true;
			} else {
				individual.setFitness(lastFitness);
				return false;
			}
		}
	}

	private static class SuiteMinimization implements Minimization {

		private final TestSuiteFitnessFunction fitness;

		private final TestSuiteChromosome suite;

		private final TestChromosome individual;

		private final int testIndex;

		private double lastFitness;

		private double lastCoverage;

		public SuiteMinimization(TestSuiteFitnessFunction fitness,
		        TestSuiteChromosome suite, int index) {
			this.fitness = fitness;
			this.suite = suite;
			this.individual = suite.getTestChromosome(index);
			this.testIndex = index;
			this.lastFitness = suite.getFitness();
			this.lastCoverage = suite.getCoverage();
		}

		/* (non-Javadoc)
		 * @see org.evosuite.ga.LocalSearchObjective#hasImproved(org.evosuite.ga.Chromosome)
		 */
		@Override
		public boolean isNotWorse() {
			ExecutionResult lastResult = individual.getLastExecutionResult().clone();
			individual.setChanged(true);
			suite.setTestChromosome(testIndex, individual);
			double newFitness = fitness.getFitness(suite);
			// individual.setChanged(true);
			if (newFitness <= lastFitness) { // TODO: Maximize
				logger.debug("Fitness changed from " + lastFitness + " to " + newFitness);
				lastFitness = newFitness;
				lastCoverage = suite.getCoverage();
				suite.setFitness(lastFitness);
				return true;
			} else {
				individual.setLastExecutionResult(lastResult);
				suite.setFitness(lastFitness);
				suite.setCoverage(lastCoverage);
				return false;
			}
		}
	}

	private Minimization objective;

	/**
	 * <p>
	 * minimize
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @param objective
	 *            a {@link org.evosuite.testcase.TestFitnessFunction} object.
	 */
	public void minimize(TestChromosome test, TestFitnessFunction objective) {
		this.objective = new TestMinimization(objective, test);
		test.test.accept(this);
	}

	/**
	 * <p>
	 * minimize
	 * </p>
	 * 
	 * @param suite
	 *            a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 * @param objective
	 *            a {@link org.evosuite.testsuite.TestSuiteFitnessFunction}
	 *            object.
	 */
	public void minimize(TestSuiteChromosome suite, TestSuiteFitnessFunction objective) {
		int i = 0;
		for (TestChromosome test : suite.getTestChromosomes()) {
			this.objective = new SuiteMinimization(objective, suite, i);
			test.test.accept(this);
			i++;
		}

	}

	private <T> void binarySearch(NumericalPrimitiveStatement<T> statement) {
		@SuppressWarnings("unchecked")
		PrimitiveStatement<T> zero = (PrimitiveStatement<T>) PrimitiveStatement.getPrimitiveStatement(statement.tc,
		                                                                                              statement.getReturnClass());
		T max = statement.getValue();
		T min = zero.getValue();
		boolean positive = statement.isPositive();
		T lastValue = null;
		boolean done = false;
		while (!done) {
			T oldValue = statement.getValue();
			statement.setMid(min, max);
			T newValue = statement.getValue();
			if (oldValue.equals(newValue)) {
				break;
			}
			if (lastValue != null && lastValue.equals(newValue)) {
				break;
			}
			lastValue = newValue;
			logger.info("Trying " + statement.getValue() + " " + min + "/" + max + " - "
			        + statement.getClass());

			if (min.equals(max) || statement.getValue().equals(min)
			        || statement.getValue().equals(max)) {
				done = true;
				logger.info("Fixpoint.");
				//assert (objective.isNotWorse());
			}
			if (objective.isNotWorse()) {
				logger.info("Fitness hasn't decreased");
				// If fitness has not decreased, new max is new value
				max = statement.getValue();
			} else {
				logger.info("Fitness has decreased!");
				// Else has to be larger
				if (positive)
					statement.increment();
				else
					statement.decrement();
				min = statement.getValue();
				statement.setValue(max);
			}
		}
	}

	private void removeCharacters(StringPrimitiveStatement p) {

		String oldString = p.getValue();

		for (int i = oldString.length() - 1; i >= 0; i--) {
			String newString = oldString.substring(0, i) + oldString.substring(i + 1);
			p.setValue(newString);
			//logger.info(" " + i + " " + oldValue + "/" + oldValue.length() + " -> "
			//        + newString + "/" + newString.length());
			if (objective.isNotWorse()) {
				oldString = p.getValue();
			} else {
				p.setValue(oldString);
			}
		}
	}

	/**
	 * Try to remove non-ASCII characters
	 * 
	 * @param statement
	 */
	private void cleanString(StringPrimitiveStatement statement) {
		String oldString = statement.getValue();
		String newString = oldString.replaceAll("[^\\p{ASCII}]", "").replaceAll("\\p{Cntrl}",
		                                                                        "");
		statement.setValue(newString);
		if (!objective.isNotWorse()) {
			statement.setValue(oldString);
			newString = oldString;
		}

		oldString = newString;
		newString = newString.replaceAll("[^\\p{L}\\p{N}]", "");
		statement.setValue(newString);
		if (!objective.isNotWorse()) {
			statement.setValue(oldString);
		}

		removeCharacters(statement);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestVisitor#visitTestCase(org.evosuite.testcase.TestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitTestCase(TestCase test) {
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestVisitor#visitPrimitiveStatement(org.evosuite.testcase.PrimitiveStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
		if (statement instanceof NumericalPrimitiveStatement<?>) {
			if (statement instanceof BooleanPrimitiveStatement)
				return;
			logger.info("Statement before minimization: " + statement.getCode());
			binarySearch((NumericalPrimitiveStatement<?>) statement);
			logger.info("Statement after minimization: " + statement.getCode());
		} else if (statement instanceof StringPrimitiveStatement) {
			logger.info("Statement before minimization: " + statement.getCode());
			cleanString((StringPrimitiveStatement) statement);
			logger.info("Statement after minimization: " + statement.getCode());
			// TODO: Try to delete characters, or at least replace non-ascii characters with ascii characters
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestVisitor#visitFieldStatement(org.evosuite.testcase.FieldStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitFieldStatement(FieldStatement statement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestVisitor#visitMethodStatement(org.evosuite.testcase.MethodStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMethodStatement(MethodStatement statement) {
		//if (true)
		//	return;
		/*
		try {
			TestCluster cluster = TestCluster.getInstance();
			DefaultTestFactory factory = DefaultTestFactory.getInstance();

			StatementInterface copy = statement;
			int position = copy.getPosition();

			Set<AccessibleObject> generators = cluster.getGenerators(statement.getReturnType());
			logger.info("Trying replacement of " + statement.getCode());
			//logger.info(test.toCode());
			for (AccessibleObject generator : generators) {
				try {
					logger.info("Trying replacement with " + generator);
					factory.changeCall(test, statement, generator);
					if (objective.isNotWorse()) {
						//logger.info(test.toCode());
						copy = statement;
						logger.info("Success replacement with " + generator);
					} else {
						logger.info("Failed replacement with " + generator);
						test.setStatement(copy, position);
						//logger.info(test.toCode());
					}
				} catch (ConstructionFailedException e) {
					logger.info("Failed replacement with " + generator);
					test.setStatement(copy, position);
					// logger.info(test.toCode());
				}
			}

		} catch (ConstructionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestVisitor#visitConstructorStatement(org.evosuite.testcase.ConstructorStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitConstructorStatement(ConstructorStatement statement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestVisitor#visitArrayStatement(org.evosuite.testcase.ArrayStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitArrayStatement(ArrayStatement statement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestVisitor#visitAssignmentStatement(org.evosuite.testcase.AssignmentStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitAssignmentStatement(AssignmentStatement statement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestVisitor#visitNullStatement(org.evosuite.testcase.NullStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitNullStatement(NullStatement statement) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void visitPrimitiveExpression(PrimitiveExpression primitiveExpression) {
		// TODO-JRO Implement method visitPrimitiveExpression
		logger.warn("Method visitPrimitiveExpression not implemented!");

	}

}
