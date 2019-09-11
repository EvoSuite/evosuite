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
import org.evosuite.setup.TestCluster;
import org.evosuite.symbolic.instrument.ClassLoaderUtils;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericExecutable;
import org.evosuite.utils.generic.GenericMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Objects;
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
	protected final String className;
	protected final String methodName;
	private final boolean publicTargetMethod;
	private final boolean constructor;
	private final boolean staticTargetMethod;
	private final Class<?> clazz;
	private int cyclomaticComplexity; // initialized when the getter is called

	protected TestFitnessFunction(final String className,
								  final String methodNameDesc) {
		this.className = Objects.requireNonNull(className, "class name cannot be null");
		this.methodName = Objects.requireNonNull(methodNameDesc, "method name + descriptor cannot be null");
		this.clazz = Objects.requireNonNull(getTargetClass(className));
		final GenericExecutable<?, ?> executable =
				Objects.requireNonNull(getTargetExecutable(methodNameDesc, clazz));
		this.publicTargetMethod = executable.isPublic();
		this.staticTargetMethod = executable.isStatic();
		this.constructor = executable.isConstructor();
	}

	private static Class<?> getTargetClass(final String className) {
		try {
			return TestCluster.getInstance().getClass(className);
		} catch (ClassNotFoundException e) {
			logger.error("Unable to reflect unknown class {}", className);
			return null;
		}
	}

	private static GenericExecutable<?, ?> getTargetExecutable(final String methodNameDesc,
															   final Class<?> clazz) {
		// methodNameDesc = name + descriptor, we have to split it into two parts to work with it
		final int descriptorStartIndex = methodNameDesc.indexOf('(');
		assert descriptorStartIndex > 0 : "malformed method name or descriptor";
		final String name = methodNameDesc.substring(0, descriptorStartIndex);
		final String descriptor = methodNameDesc.substring(descriptorStartIndex);

		// Tries to reflect the argument types.
		final Class<?>[] argumentTypes;
		final ClassLoader classLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();
		try {
			argumentTypes = ClassLoaderUtils.getArgumentClasses(classLoader, descriptor);
		} catch (Throwable t) {
			logger.error("Unable to reflect argument types of method {}", methodNameDesc);
			logger.error("\tCause: {}", t.getMessage());
			return null;
		}

		final boolean isConstructor = name.equals("<init>");
		if (isConstructor) {
			return new GenericConstructor(getConstructor(clazz, argumentTypes), clazz);
		} else {
			return new GenericMethod(getMethod(clazz, name, argumentTypes), clazz);
		}
	}

	private static Constructor<?> getConstructor(final Class<?> clazz,
												 final Class<?>[] argumentTypes) {
		final Constructor<?> constructor;
		try {
			constructor = clazz.getConstructor(argumentTypes);
		} catch (NoSuchMethodException e) {
			logger.error("No constructor of {} with argument types {}", clazz.getName(),
					argumentTypes);
			return null;
		}
		return constructor;
	}

	private static Method getMethod(final Class<?> clazz,
									final String name,
									final Class<?>[] argumentTypes) {
		final Method method;
		try {
			method = clazz.getMethod(name, argumentTypes);
		} catch (NoSuchMethodException e) {
			logger.error("No method with name {} and arguments {} in {}", name, argumentTypes,
					clazz.getName());
			return null;
		}
		return method;
	}

	/**
	 * <p>
	 * getFitness
	 * </p>
	 *
	 * @param individual a {@link org.evosuite.testcase.TestChromosome} object.
	 * @param result     a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 * @return a double.
	 */
	public abstract double getFitness(TestChromosome individual, ExecutionResult result);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getFitness(TestChromosome individual) {
		logger.trace("Executing test case on original");
		ExecutionResult lastResult = individual.getLastExecutionResult();
		if (lastResult == null || individual.isChanged()) {
			lastResult = runTest(individual.test);
			individual.setLastExecutionResult(lastResult);
			individual.setChanged(false);
		}

		double fitness = getFitness(individual, lastResult);
		updateIndividual(individual, fitness);

		return fitness;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Used to preorder goals by difficulty
	 */
	@Override
	public abstract int compareTo(TestFitnessFunction other);

	protected final int compareClassName(TestFitnessFunction other) {
		return this.getClass().getName().compareTo(other.getClass().getName());
	}

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object other);

	/**
	 * {@inheritDoc}
	 */
	public ExecutionResult runTest(TestCase test) {
		return TestCaseExecutor.runTest(test);
	}

	/**
	 * Determine if there is an existing test case covering this goal
	 *
	 * @param tests a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean isCovered(List<TestCase> tests) {
		return tests.stream().anyMatch(this::isCovered);
	}

	/**
	 * Determine if there is an existing test case covering this goal
	 *
	 * @param tests a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean isCoveredByResults(List<ExecutionResult> tests) {
		return tests.stream().anyMatch(this::isCovered);
	}

	public boolean isCoveredBy(TestSuiteChromosome testSuite) {
		int num = 1;
		for (TestChromosome test : testSuite.getTestChromosomes()) {
			logger.debug("Checking goal against test " + num + "/" + testSuite.size());
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
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
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
	 * @param tc a {@link org.evosuite.testcase.TestChromosome} object.
	 * @return a boolean.
	 */
	public boolean isCovered(TestChromosome tc) {
		if (tc.getTestCase().isGoalCovered(this)) {
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
	 * @param individual a {@link org.evosuite.testcase.TestChromosome} object.
	 * @param result     a {@link org.evosuite.testcase.execution.ExecutionResult} object.
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

	/**
	 * {@inheritDoc}
	 */
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
	public final String getTargetClassName() {
		return className;
	}

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
	public final String getTargetMethodName() {
		return methodName;
	}

	/**
	 * Returns the cyclomatic complexity of the target method (as given by
	 * {@link TestFitnessFunction#getTargetMethodName()}).
	 *
	 * @return the cyclomatic complexity of the target method
	 * @see RawControlFlowGraph#getCyclomaticComplexity()
	 */
	public int getCyclomaticComplexity() {
		// This method is thread-safe: the cyclomaticComplexity field is effectively final as long
		// as no setter exists. Then, race conditions cannot occur. The worst thing that can happen
		// is that two threads initialize cyclomaticComplexity to the same value at the same time.

		if (cyclomaticComplexity < 1) { // Lazy initialization of the cyclomaticComplexity field
			final InstrumentingClassLoader cl = TestGenerationContext.getInstance().getClassLoaderForSUT();
			final GraphPool gp = GraphPool.getInstance(cl);
			final RawControlFlowGraph cfg = gp.getRawCFG(getTargetClassName(), getTargetMethodName());
			cyclomaticComplexity = cfg.getCyclomaticComplexity();

			assert cyclomaticComplexity > 0 : "cyclomatic complexity must be positive number";
		}

		return cyclomaticComplexity;
	}

	/**
	 * Returns the cyclomatic complexity of the target method, including the cyclomatic complexities
	 * of all methods <i>directly</i> called by the target method.
	 * <p>
	 * The rationale is to handle pathetic cases where very complicated methods are called by very
	 * simple ones, such as this one:
	 * <pre><code>
	 * void foo() {
	 *     veryComplicatedMethod(); // cyclomatic complexity = 42
	 * }
	 * </code></pre>
	 * Using the traditional definition of the cyclomatic complexity as implemented in
	 * {@link TestFitnessFunction#getCyclomaticComplexity()}, <code>foo()</code> would have a
	 * cyclomatic complexity of just 1, despite the fact that it's calling a method
	 * with a much higher complexity. In the case of test generation, this would make covering
	 * <code>foo()</code> much more appealing, when in fact it's just as appealing as covering the
	 * <code>veryComplicatedMethod()</code>. For this reason, this method treats <code>foo</code>
	 * and <code>veryComplicatedMethod()</code> the same way by assigning them the same cyclomatic
	 * complexity.
	 * <p>
	 * Conceptually, if we want to compute the cyclomatic complexity of a method while also
	 * considering the cyclomatic complexities of its callee methods, we have to embed the entire
	 * CFG of every callee method into the CFG of the target method. This is done by replacing the
	 * vertex that calls another method with the corresponding CFG of that method. The incoming
	 * edge of the vertex we just replaced is now connected to the method entry node of the called
	 * method. In analogue, the outgoing edge of the vertex we replaced is now connected to the
	 * method exit point of the called method.
	 * <p>
	 * This notion only works if the callee method is called only once. Otherwise, the exit node
	 * of the embedded CFG would have an out-degree of more than 1, despite not being a decision
	 * node. It also means that the results computed by this method will be slightly flawed in
	 * case the callee gets called more than once. However, this method is not meant to produce
	 * exact results, it's rather only intended to serve the purpose of returning a rough estimate
	 * of the complexity of a method.
	 * <p>
	 * The computation uses raw CFGs, i.e., it does not summarize sequentially composed statements
	 * to basic blocks. Therefore, we can compute the "recursive" cyclomatic complexity by
	 * computing the cyclomatic complexities of all involved methods individually, then summing
	 * it all up, and finally subtracting the number of individual callees to account for the
	 * fact that we replaced some nodes with entire CFGs as explained earlier. Note that the
	 * cyclomatic complexities of the callee methods are not computed recursively using this same
	 * method. That is, callees of callees are not accounted for. Instead, for the sake of
	 * efficiency and simplicity, the cyclomatic complexity of calles is computed using the
	 * "traditional way" as implemented in {@code getCyclomaticComplexity()} and
	 * {@link RawControlFlowGraph#getCyclomaticComplexity()}.
	 *
	 * @return the cyclomatic complexity
	 */
	public int getCyclomaticComplexityInclCallees() {
		// This method is thread-safe: the cyclomaticComplexity field is effectively final as long
		// as no setter exists. Then, race conditions cannot occur. The worst thing that can happen
		// is that two threads initialize cyclomaticComplexity to the same value at the same time.

		if (cyclomaticComplexity < 1) { // Lazy initialization of the cyclomaticComplexity field
			final InstrumentingClassLoader cl = TestGenerationContext.getInstance().getClassLoaderForSUT();
			final GraphPool gp = GraphPool.getInstance(cl);

			// Class name and method name that contain the target.
			final String targetClass = getTargetClassName();
			final String targetMethod = getTargetMethodName();

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
			// the total number of callee methods. A method, even if being called multiple times,
			// is accounted for only once.
			final IntSummaryStatistics calleeComplexities = callees.stream()
					.map(callee -> gp.getRawCFG(targetClass, callee.getMethod()))
					.collect(Collectors.summarizingInt(RawControlFlowGraph::getCyclomaticComplexity));
			final int totalCalleeComplexity = (int) calleeComplexities.getSum();
			final int numberOfCallees = (int) calleeComplexities.getCount(); // Individual callees!

			// Using the formula explained in the JavaDoc.
			cyclomaticComplexity = ownComplexity + totalCalleeComplexity - numberOfCallees;

			// sanity check that field was properly initialized and no impossible value was computed
			assert cyclomaticComplexity > 0 : "cyclomatic complexity must be positive number";
		}

		return cyclomaticComplexity;
	}

	public boolean isPublic() {
		return publicTargetMethod;
	}

	public boolean isStatic() {
		return staticTargetMethod;
	}

	public boolean isConstructor() {
		return constructor;
	}

	public Class<?> getClazz() {
		return clazz;
	}
}
