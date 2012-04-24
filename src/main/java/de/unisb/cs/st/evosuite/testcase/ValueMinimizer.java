/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
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
		 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasImproved(de.unisb.cs.st.evosuite.ga.Chromosome)
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

		private final double lastCoverage;

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
		 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasImproved(de.unisb.cs.st.evosuite.ga.Chromosome)
		 */
		@Override
		public boolean isNotWorse() {
			ExecutionResult lastResult = individual.getLastExecutionResult();
			individual.setChanged(true);
			suite.setTestChromosome(testIndex, individual);
			double newFitness = fitness.getFitness(suite);
			// individual.setChanged(true);
			if (newFitness <= lastFitness) { // TODO: Maximize
				logger.debug("Fitness changed from " + lastFitness + " to " + newFitness);
				lastFitness = newFitness;
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

	public void minimize(TestChromosome test, TestFitnessFunction objective) {
		this.objective = new TestMinimization(objective, test);
		test.test.accept(this);
	}

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
				//assert (objective.isNotWorse());
			} else if (objective.isNotWorse()) {
				// If fitness has not decreased, new max is new value
				max = statement.getValue();
			} else {
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
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitTestCase(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public void visitTestCase(TestCase test) {
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitPrimitiveStatement(de.unisb.cs.st.evosuite.testcase.PrimitiveStatement)
	 */
	@Override
	public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
		if (statement instanceof NumericalPrimitiveStatement<?>) {
			if (statement instanceof BooleanPrimitiveStatement)
				return;
			logger.info("Statement before minimization: " + statement.getCode());
			binarySearch((NumericalPrimitiveStatement<?>) statement);
			logger.info("Statement after minimization: " + statement.getCode());
		} else if (statement instanceof StringPrimitiveStatement) {
			cleanString((StringPrimitiveStatement) statement);
			// TODO: Try to delete characters, or at least replace non-ascii characters with ascii characters
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitFieldStatement(de.unisb.cs.st.evosuite.testcase.FieldStatement)
	 */
	@Override
	public void visitFieldStatement(FieldStatement statement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitMethodStatement(de.unisb.cs.st.evosuite.testcase.MethodStatement)
	 */
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
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitConstructorStatement(de.unisb.cs.st.evosuite.testcase.ConstructorStatement)
	 */
	@Override
	public void visitConstructorStatement(ConstructorStatement statement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitArrayStatement(de.unisb.cs.st.evosuite.testcase.ArrayStatement)
	 */
	@Override
	public void visitArrayStatement(ArrayStatement statement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitAssignmentStatement(de.unisb.cs.st.evosuite.testcase.AssignmentStatement)
	 */
	@Override
	public void visitAssignmentStatement(AssignmentStatement statement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitNullStatement(de.unisb.cs.st.evosuite.testcase.NullStatement)
	 */
	@Override
	public void visitNullStatement(NullStatement statement) {
		// TODO Auto-generated method stub

	}

}
