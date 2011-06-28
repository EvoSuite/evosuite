/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author fraser
 * 
 */
public class ValueMinimizer implements TestVisitor {

	private static Logger logger = Logger.getLogger(ValueMinimizer.class);

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

		public SuiteMinimization(TestSuiteFitnessFunction fitness,
		        TestSuiteChromosome suite, int index) {
			this.fitness = fitness;
			this.suite = suite;
			this.individual = suite.getTestChromosome(index);
			this.testIndex = index;
			this.lastFitness = suite.getFitness();
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasImproved(de.unisb.cs.st.evosuite.ga.Chromosome)
		 */
		@Override
		public boolean isNotWorse() {
			individual.setChanged(true);
			suite.setTestChromosome(testIndex, individual);
			double newFitness = fitness.getFitness(suite);
			// individual.setChanged(true);
			if (newFitness <= lastFitness) { // TODO: Maximize
				logger.info("Fitness changed from " + lastFitness + " to " + newFitness);
				lastFitness = newFitness;
				suite.setFitness(lastFitness);
				return true;
			} else {
				suite.setFitness(lastFitness);
				return false;
			}
		}
	}

	private Minimization objective;

	private TestCase test;

	public void minimize(TestChromosome test, TestFitnessFunction objective) {
		this.objective = new TestMinimization(objective, test);
		this.test = test.test;
		test.test.accept(this);
	}

	public void minimize(TestSuiteChromosome suite, TestSuiteFitnessFunction objective) {
		int i = 0;
		for (TestChromosome test : suite.getTestChromosomes()) {
			this.objective = new SuiteMinimization(objective, suite, i);
			this.test = test.test;
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

		boolean done = false;
		while (!done) {
			statement.setMid(min, max);
			logger.info("Trying " + statement.getValue() + " " + min + "/" + max);

			if (min.equals(max)) {
				done = true;
				assert (objective.isNotWorse());
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
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitFieldStatement(de.unisb.cs.st.evosuite.testcase.FieldStatement)
	 */
	@Override
	public void visitFieldStatement(FieldStatement statement) {
		// TODO Auto-generated method stub

	}

	private int getNumParameters(AccessibleObject o) {
		int num = 0;
		if (o instanceof Method) {
			Method m = (Method) o;
			if (Modifier.isStatic(m.getModifiers()))
				num++;
			num += m.getParameterTypes().length;
		} else if (o instanceof Constructor<?>) {
			Constructor<?> c = (Constructor<?>) o;
			num = c.getParameterTypes().length;
		} else if (o instanceof Field) {
			Field f = (Field) o;
			if (Modifier.isStatic(f.getModifiers()))
				num++;

		}
		return num;
	}

	private boolean isPrimitive(AccessibleObject o) {
		if (o instanceof Method) {
			Method m = (Method) o;
			return m.getReturnType().isPrimitive();
		} else if (o instanceof Constructor<?>) {
			return false;
		} else if (o instanceof Field) {
			Field f = (Field) o;
			return f.getType().isPrimitive();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitMethodStatement(de.unisb.cs.st.evosuite.testcase.MethodStatement)
	 */
	@Override
	public void visitMethodStatement(MethodStatement statement) {

		try {
			TestCluster cluster = TestCluster.getInstance();
			DefaultTestFactory factory = DefaultTestFactory.getInstance();

			int numParameters = statement.parameters.size();
			StatementInterface copy = statement;
			int position = copy.getPosition();

			List<AccessibleObject> generators = cluster.getGenerators(statement.getReturnType());
			logger.info("Trying replacement of " + statement.getCode());
			//logger.info(test.toCode());
			for (AccessibleObject generator : generators) {
				try {
					logger.info("Trying replacement with " + generator);
					factory.changeCall(test, statement, generator);
					if (objective.isNotWorse()) {
						//logger.info(test.toCode());
						numParameters = getNumParameters(generator);
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

}
