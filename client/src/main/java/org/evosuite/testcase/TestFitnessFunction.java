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
package org.evosuite.testcase;

import org.evosuite.TestGenerationContext;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.ccg.ClassCallGraph;
import org.evosuite.graphs.ccg.ClassCallNode;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract base class for fitness functions for test case chromosomes
 * 
 * @author Gordon Fraser
 */
public abstract class TestFitnessFunction extends FitnessFunction<TestChromosome>
        implements Comparable<TestFitnessFunction> {

	private static final long serialVersionUID = 5602125855207061901L;
	private int cyclomaticComplexity; // initialized when the getter is called

	static boolean warnedAboutIsSimilarTo = false;

	/**
	 * <p>
	 * getFitness
	 * </p>
	 * 
	 * @param individual
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @param result
	 *            a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 * @return a double.
	 */
	public abstract double getFitness(TestChromosome individual, ExecutionResult result);

	/** {@inheritDoc} */
	@Override
	public double getFitness(TestChromosome individual) {
		logger.trace("Executing test case on original");
		ExecutionResult origResult = individual.getLastExecutionResult();
		if (origResult == null || individual.isChanged()) {
			origResult = runTest(individual.test);
			individual.setLastExecutionResult(origResult);
			individual.setChanged(false);
		}

		double fitness = getFitness(individual, origResult);
		updateIndividual(individual, fitness);

		return fitness;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Used to preorder goals by difficulty
	 */
	@Override
	public abstract int compareTo(TestFitnessFunction other);

	protected final int compareClassName(TestFitnessFunction other){
		return this.getClass().getName().compareTo(other.getClass().getName());
	}

	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals(Object other);

	/** {@inheritDoc} */
	public ExecutionResult runTest(TestCase test) {
		return TestCaseExecutor.runTest(test);
	}

	/**
	 * Determine if there is an existing test case covering this goal
	 * 
	 * @param tests
	 *            a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean isCovered(List<TestCase> tests) {
		return tests.stream().anyMatch(this::isCovered);
	}

	/**
	 * Determine if there is an existing test case covering this goal
	 * 
	 * @param tests
	 *            a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean isCoveredByResults(List<ExecutionResult> tests) {
		return tests.stream().anyMatch(this::isCovered);
	}

	public boolean isCoveredBy(TestSuiteChromosome testSuite) {
		int num = 1;
		for (TestChromosome test : testSuite.getTestChromosomes()) {
			logger.debug("Checking goal against test "+num+"/"+testSuite.size());
			num++;
			if (isCovered(test))
				return true;
		}
		return false;
		// return testSuite.getTestChromosomes().stream().anyMatch(this::isCovered);
	}

	/**
	 * <p>
	 * isCovered
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a boolean.
	 */
	public boolean isCovered(TestCase test) {
		TestChromosome c = new TestChromosome();
		c.test = test;
		return isCovered(c);
	}

	/**
	 * <p>
	 * isCovered
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @return a boolean.
	 */
	public boolean isCovered(TestChromosome tc) {
		if(tc.getTestCase().isGoalCovered(this)){
			return true;
		}

		ExecutionResult result = tc.getLastExecutionResult();
		if (result == null || tc.isChanged()) {
			result = runTest(tc.test);
			tc.setLastExecutionResult(result);
			tc.setChanged(false);
		}

		return isCovered(tc, result);
	}

	/**
	 * <p>
	 * isCovered
	 * </p>
	 * 
	 * @param individual
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @param result
	 *            a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 * @return a boolean.
	 */
	public boolean isCovered(TestChromosome individual, ExecutionResult result) {
		boolean covered = getFitness(individual, result) == 0.0;
		if (covered) {
			individual.test.addCoveredGoal(this);
		}
		return covered;
	}

	/**
	 * Helper function if this is used without a chromosome
	 * 
	 * @param result
	 * @return
	 */
	public boolean isCovered(ExecutionResult result) {
		TestChromosome chromosome = new TestChromosome();
		chromosome.setTestCase(result.test);
		chromosome.setLastExecutionResult(result);
		chromosome.setChanged(false);
		return isCovered(chromosome, result);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#isMaximizationFunction()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isMaximizationFunction() {
		return false;
	}

	/**
	 * Returns the fully qualified name of the target class. For example, a class named {@code Bar}
	 * in a package {@code com.example.foo} has the fully qualified name
	 * {@code com.example.foo.Bar}. For more thorough information about this topic please refer to
	 * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-6.html#jls-6.7">The Java® Language Specification</a>.
	 *
	 * @return the fully qualified name of the target class
	 */
	public abstract String getTargetClass();

	/**
	 * <p>
	 * Returns the method name and method descriptor of the target method concatenated as string.
	 * For instance, consider the method
	 * <blockquote><pre>
	 * Object someMethod(int i, double d, Thread t) {...}
	 * </pre></blockquote>
	 * The method name is <code>someMethod</code> and the method descriptor is
	 * <blockquote><pre>
	 * (IDLjava/lang/Thread;)Ljava/lang/Object;
	 * </pre></blockquote>
	 * The concatenation of method name and descriptor therefore is
	 * <blockquote><pre>
	 * someMethod(IDLjava/lang/Thread;)Ljava/lang/Object;
	 * </pre></blockquote>
	 * Any constructor of a given class has the special name <code>&lt;init&gt;</code>.
	 * </p>
	 * <p>
	 * For more thorough information about method descriptors refer to the
	 * <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3.3">The Java® Virtual Machine Specification</a>.
	 * </p>
	 *
	 * @return the method name and descriptor of the target method
	 */
	public abstract String getTargetMethod();

	/**
	 * Returns the cyclomatic complexity of the target method (as given by
	 * {@link TestFitnessFunction#getTargetMethod()}).
	 *
	 * @return the cyclomatic complexity of the target method
	 */
	public int getCyclomaticComplexity() {
		// This method is thread-safe: the cyclomaticComplexity field is effectively final as long
		// as no setter exists. Then, race conditions cannot occur. The worst thing that can happen
		// is that two threads initialize cyclomaticComplexity to the same value at the same time.

		if (cyclomaticComplexity < 1) { // Lazy initialization of the cyclomaticComplexity field
			final InstrumentingClassLoader cl = TestGenerationContext.getInstance().getClassLoaderForSUT();
			final GraphPool gp = GraphPool.getInstance(cl);
			final RawControlFlowGraph cfg = gp.getRawCFG(getTargetClass(), getTargetMethod());
			cyclomaticComplexity = cfg.getCyclomaticComplexity();
		}

		assert cyclomaticComplexity > 0 : "cyclomatic complexity must be positive number";

		return cyclomaticComplexity;
	}

	private int getCyclomaticComplexityWithCallees() {
		// This method is thread-safe: the cyclomaticComplexity field is effectively final as long
		// as no setter exists. Then, race conditions cannot occur. The worst thing that can happen
		// is that two threads initialize cyclomaticComplexity to the same value at the same time.

		if (cyclomaticComplexity < 1) { // Lazy initialization of the cyclomaticComplexity field
			final InstrumentingClassLoader cl = TestGenerationContext.getInstance().getClassLoaderForSUT();
			final GraphPool gp = GraphPool.getInstance(cl);

			// Class name and method name that contain the target.
			final String targetClass = getTargetClass();
			final String targetMethod = getTargetMethod();

			final RawControlFlowGraph cfg = gp.getRawCFG(targetClass, targetMethod);
			final int ownComplexity = cfg.getCyclomaticComplexity();

			// Constructs the class call graph for the target class.
			final ClassCallGraph ccg = gp.getCCFG(targetClass).getCcg();

			// Node in the class call graph representing the method containing the current target.
			final ClassCallNode method = ccg.getNodeByMethodName(targetMethod);

			// Entry nodes of the methods called by the current target method.
			// Only considers methods that are declared in the same class as the target method.
			ccg.outgoingEdgesOf(method);
			final Set<ClassCallNode> callees = ccg.getChildren(method);
//			final Set<ClassCallNode> callees = ccg.getChildrenRecursively(method);
			callees.remove(method); // don't consider recursive invocations of the target method

			// Computes the sum of the cyclomatic complexities of the callee methods, as well as
			// the total number of callee methods.
			final IntSummaryStatistics calleeComplexities = callees.stream()
					.map(callee -> gp.getRawCFG(targetClass, callee.getMethod()))
					.collect(Collectors.summarizingInt(RawControlFlowGraph::getCyclomaticComplexity));
			final int totalCalleeComplexity = (int) calleeComplexities.getSum();
			final int numberOfCallees = (int) calleeComplexities.getCount();

			/*
			 * Conceptually, if we want to compute the cyclomatic complexity of the target method
			 * recursively, i.e., by also considering the cyclomatic complexities of the callee
			 * methods, we have to embed the entire CFG of every callee method into the CFG of
			 * the target method. This is done by replacing the vertex that calls another method
			 * with the corresponding CFG of that method. The incoming edge of the vertex we just
			 * replaced is now connected to the method entry node of the called method. In analogue,
			 * the outgoing edge of the vertex we replaced is now connected to the method exit point
			 * of the called method. The computation uses raw CFGs, i.e., it does not summarize
			 * sequentially composed statements to basic blocks. Therefore, we can compute the
			 * recursive cyclomatic complexity by computing the cyclomatic complexities of all
			 * involved methods individually, then summing it all up, and finally subtracting the
			 * number of individual callees to account for the fact that we replaced some nodes
			 * with entire CFGs as explained earlier.
			 */
			cyclomaticComplexity = ownComplexity + totalCalleeComplexity - numberOfCallees;
		}

		// sanity check that field was properly initialized and no impossible value was computed
		assert cyclomaticComplexity > 0 : "cyclomatic complexity must be positive number";

		return cyclomaticComplexity;
	}
}
