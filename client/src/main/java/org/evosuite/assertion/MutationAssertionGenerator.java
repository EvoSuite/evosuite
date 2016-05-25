/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.assertion;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationObserver;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.execution.reset.ClassReInitializer;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.MethodStatement;
//import org.evosuite.testsuite.SearchStatistics;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class executes a test case on a unit and all mutants and infers
 * assertions from the resulting traces.
 * 
 * TODO: This class is a mess.
 * 
 * @author Gordon Fraser
 */
public abstract class MutationAssertionGenerator extends AssertionGenerator {

	private final static Logger logger = LoggerFactory.getLogger(MutationAssertionGenerator.class);

	protected final Map<Integer, Mutation> mutants = new HashMap<>();

	protected final static PrimitiveTraceObserver primitiveObserver = new PrimitiveTraceObserver();
	protected final static ComparisonTraceObserver comparisonObserver = new ComparisonTraceObserver();
	protected final static SameTraceObserver sameObserver = new SameTraceObserver();
	protected final static InspectorTraceObserver inspectorObserver = new InspectorTraceObserver();
	protected final static PrimitiveFieldTraceObserver fieldObserver = new PrimitiveFieldTraceObserver();
	protected final static NullTraceObserver nullObserver = new NullTraceObserver();
	protected final static ArrayTraceObserver arrayObserver = new ArrayTraceObserver();

	protected final static Map<Mutation, Integer> timedOutMutations = new HashMap<Mutation, Integer>();

	protected final static Map<Mutation, Integer> exceptionMutations = new HashMap<Mutation, Integer>();

	/** Constant <code>observerClasses</code> */
	protected static Class<?>[] observerClasses = { PrimitiveTraceEntry.class, ComparisonTraceEntry.class,
			SameTraceEntry.class, InspectorTraceEntry.class, PrimitiveFieldTraceEntry.class, NullTraceEntry.class,
			ArrayTraceEntry.class };

	/**
	 * Default constructor
	 */
	public MutationAssertionGenerator() {
		for (Mutation m : MutationPool.getMutants()) {
			mutants.put(m.getId(), m);
		}
		TestCaseExecutor.getInstance().newObservers();
		TestCaseExecutor.getInstance().addObserver(primitiveObserver);
		TestCaseExecutor.getInstance().addObserver(comparisonObserver);
		TestCaseExecutor.getInstance().addObserver(sameObserver);
		TestCaseExecutor.getInstance().addObserver(inspectorObserver);
		TestCaseExecutor.getInstance().addObserver(fieldObserver);
		TestCaseExecutor.getInstance().addObserver(nullObserver);
		TestCaseExecutor.getInstance().addObserver(arrayObserver);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Execute a test case on the original unit
	 */
	@Override
	protected ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	/**
	 * Execute a test case on a mutant
	 * 
	 * @param test
	 *            The test case that should be executed
	 * @param mutant
	 *            The mutant on which the test case shall be executed
	 */
	protected ExecutionResult runTest(TestCase test, Mutation mutant) {
		ExecutionResult result = new ExecutionResult(test, mutant);
		// resetObservers();
		comparisonObserver.clear();
		sameObserver.clear();
		primitiveObserver.clear();
		inspectorObserver.clear();
		fieldObserver.clear();
		nullObserver.clear();
		arrayObserver.clear();
		try {
			logger.debug("Executing test");
			if (mutant == null) {
				MutationObserver.deactivateMutation();
			} else {
				MutationObserver.activateMutation(mutant);
			}
			result = TestCaseExecutor.getInstance().execute(test);
			MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);

			result.setTrace(comparisonObserver.getTrace(), ComparisonTraceEntry.class);
			result.setTrace(sameObserver.getTrace(), SameTraceEntry.class);
			result.setTrace(primitiveObserver.getTrace(), PrimitiveTraceEntry.class);
			result.setTrace(inspectorObserver.getTrace(), InspectorTraceEntry.class);
			result.setTrace(fieldObserver.getTrace(), PrimitiveFieldTraceEntry.class);
			result.setTrace(nullObserver.getTrace(), NullTraceEntry.class);
			result.setTrace(arrayObserver.getTrace(), ArrayTraceEntry.class);

		} catch (Exception e) {
			throw new Error(e);
		}

		return result;
	}

	protected Criterion[] oldCriterion = Properties.CRITERION;

	/**
	 * If we are not doing mutation testing anyway, then we need to reinstrument
	 * the code to get the mutants now
	 * 
	 * @param suite
	 */
	@Override
	public void setupClassLoader(TestSuiteChromosome suite) {
		oldCriterion = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
		if (!ArrayUtil.contains(oldCriterion, Criterion.MUTATION)
				&& !ArrayUtil.contains(oldCriterion, Criterion.WEAKMUTATION)
				&& !ArrayUtil.contains(oldCriterion, Criterion.ONLYMUTATION)
				&& !ArrayUtil.contains(oldCriterion, Criterion.STRONGMUTATION)) {
			Properties.CRITERION = new Criterion[] { Criterion.MUTATION };
		}
		if (Properties.RESET_STATIC_FIELDS) {
			final boolean reset_all_classes = Properties.RESET_ALL_CLASSES_DURING_ASSERTION_GENERATION;
			ClassReInitializer.getInstance().setReInitializeAllClasses(reset_all_classes);
		}
		changeClassLoader(suite);
		for (Mutation m : MutationPool.getMutants()) {
			mutants.put(m.getId(), m);
		}
	}

	/**
	 * Set the criterion to whatever it was before
	 * 
	 * @param suite
	 */
	protected void restoreCriterion(TestSuiteChromosome suite) {
		Properties.CRITERION = oldCriterion;
	}

	/**
	 * We send status about the mutation score when we're done, because we know
	 * it
	 * 
	 * @param tkilled
	 */
	protected void calculateMutationScore(Set<Integer> tkilled) {
		if (MutationPool.getMutantCounter() == 0) {
			Properties.CRITERION = oldCriterion;
			// SearchStatistics.getInstance().mutationScore(1.0);
			LoggingUtils.getEvoLogger()
					.info("* Resulting test suite's mutation score: " + NumberFormat.getPercentInstance().format(1.0));
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.MutationScore, 1.0);

		} else {
			double score = (double) tkilled.size() / (double) MutationPool.getMutantCounter();
			// SearchStatistics.getInstance().mutationScore(score);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.MutationScore, score);
			LoggingUtils.getEvoLogger().info(
					"* Resulting test suite's mutation score: " + NumberFormat.getPercentInstance().format(score));
		}
	}

	/**
	 * @param test
	 * @param mutation_traces
	 * @param executedMutants
	 * @return
	 */
	protected int getNumKilledMutants(TestCase test, Map<Mutation, List<OutputTrace<?>>> mutation_traces,
			List<Mutation> executedMutants) {
		List<Assertion> assertions;
		Set<Integer> killed = new HashSet<Integer>();
		assertions = test.getAssertions();
		for (Assertion assertion : assertions) {
			for (Mutation m : executedMutants) {

				boolean isKilled = false;
				if (mutation_traces.containsKey(m)) {
					int i = 0;
					for (OutputTrace<?> trace : mutation_traces.get(m)) {
						isKilled = trace.isDetectedBy(assertion);
						if (isKilled) {
							logger.debug("Mutation killed: " + m.getId() + " by trace " + i++);
							killed.add(m.getId());
							break;
						}
						i++;
					}
				} else {
					isKilled = true;
				}
			}
		}
		logger.debug("Killed mutants: " + killed);
		return killed.size();
	}

	/**
	 * Returns true if the statement has nothing but null assertions
	 * 
	 * @param statement
	 * @return
	 */
	protected boolean justNullAssertion(Statement statement) {
		Set<Assertion> assertions = statement.getAssertions();
		if (assertions.isEmpty())
			return false;
		else {
			Iterator<Assertion> iterator = assertions.iterator();
			VariableReference ret = statement.getReturnValue();
			VariableReference callee = null;
			if (statement instanceof MethodStatement) {
				callee = ((MethodStatement) statement).getCallee();
			}
			boolean just = true;
			while (iterator.hasNext()) {
				Assertion ass = iterator.next();
				if (!(ass instanceof NullAssertion)) {
					if (ass.getReferencedVariables().contains(ret) || ass.getReferencedVariables().contains(callee)) {
						just = false;
						break;
					}
				}
			}

			return just;
		}
	}

	protected boolean primitiveWithoutAssertion(Statement statement) {
		if (!statement.getReturnValue().isPrimitive())
			return false;

		Set<Assertion> assertions = statement.getAssertions();
		if (assertions.isEmpty())
			return true;
		else {
			Iterator<Assertion> iterator = assertions.iterator();
			VariableReference ret = statement.getReturnValue();
			while (iterator.hasNext()) {
				Assertion ass = iterator.next();
				if (ass instanceof PrimitiveAssertion) {
					if (ass.getReferencedVariables().contains(ret)) {
						return false;
					}
				}
			}

			return true;
		}
	}

	/**
	 * Returns true if the variable var is used as callee later on in the test
	 * 
	 * @param test
	 * @param var
	 * @return
	 */
	protected boolean isUsedAsCallee(TestCase test, VariableReference var) {
		for (int pos = var.getStPosition() + 1; pos < test.size(); pos++) {
			Statement statement = test.getStatement(pos);
			if (statement instanceof MethodStatement) {
				if (((MethodStatement) statement).getCallee() == var)
					return true;
			} else if (statement instanceof FieldStatement) {
				if (((FieldStatement) statement).getSource() == var)
					return true;
			}

		}

		return false;
	}

	/**
	 * Remove assertNonNull assertions for all cases where we have further
	 * assertions
	 * 
	 * @param test
	 */
	protected void filterRedundantNonnullAssertions(TestCase test) {
		Set<Assertion> redundantAssertions = new HashSet<Assertion>();
		for (Statement statement : test) {
			if (statement instanceof ConstructorStatement) {
				ConstructorStatement cs = (ConstructorStatement) statement;
				for (Assertion a : cs.getAssertions()) {
					if (a instanceof NullAssertion) {
						if (cs.getAssertions().size() > 0) {
							for (Assertion a2 : cs.getAssertions()) {
								if (a2.getSource() == cs.getReturnValue())
									redundantAssertions.add(a);
							}
						} else if (isUsedAsCallee(test, cs.getReturnValue())) {
							redundantAssertions.add(a);
						}
					}
				}
			}
		}

		for (Assertion a : redundantAssertions) {
			test.removeAssertion(a);
		}
	}

	/**
	 * Remove inspector assertions that follow method calls of the same method
	 * 
	 * @param statement
	 */
	protected void filterInspectorPrimitiveDuplication(Statement statement) {
		Set<Assertion> assertions = new HashSet<Assertion>(statement.getAssertions());
		if (assertions.size() < 2)
			return;

		if (!(statement instanceof MethodStatement))
			return;

		MethodStatement methodStatement = (MethodStatement) statement;

		boolean hasPrimitive = false;
		for (Assertion assertion : assertions) {
			if (assertion instanceof PrimitiveAssertion) {
				if (assertion.getStatement().equals(statement)) {
					hasPrimitive = true;
				}
			}
		}

		if (hasPrimitive) {
			for (Assertion assertion : assertions) {
				if (assertion instanceof InspectorAssertion) {
					InspectorAssertion ia = (InspectorAssertion) assertion;
					if (ia.getInspector().getMethod().equals(methodStatement.getMethod().getMethod())) {
						statement.removeAssertion(assertion);
						return;
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.assertion.AssertionGenerator#addAssertions(org.evosuite.
	 * testcase.TestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public void addAssertions(TestCase test) {
		// TODO Auto-generated method stub

	}

}
