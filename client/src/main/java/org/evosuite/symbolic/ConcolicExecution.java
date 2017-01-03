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
package org.evosuite.symbolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.ExpressionExecutor;
import org.evosuite.symbolic.instrument.ConcolicInstrumentingClassLoader;
import org.evosuite.symbolic.vm.ArithmeticVM;
import org.evosuite.symbolic.vm.CallVM;
import org.evosuite.symbolic.vm.HeapVM;
import org.evosuite.symbolic.vm.JumpVM;
import org.evosuite.symbolic.vm.LocalsVM;
import org.evosuite.symbolic.vm.OtherVM;
import org.evosuite.symbolic.vm.PathConditionCollector;
import org.evosuite.symbolic.vm.SymbolicFunctionVM;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.evosuite.dse.IVM;
import org.evosuite.dse.MainConfig;
import org.evosuite.dse.VM;

/**
 * <p>
 * ConcolicExecution class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class ConcolicExecution {

	private static Logger logger = LoggerFactory
			.getLogger(ConcolicExecution.class);

	/** Instrumenting class loader */
	private static final ConcolicInstrumentingClassLoader classLoader = new ConcolicInstrumentingClassLoader();

	/**
	 * Retrieve the path condition for a given test case
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<BranchCondition> getSymbolicPath(TestChromosome test) {
		TestChromosome dscCopy = (TestChromosome) test.clone();
		DefaultTestCase defaultTestCase = (DefaultTestCase) dscCopy
				.getTestCase();

		return executeConcolic(defaultTestCase);
	}

	public static List<BranchCondition> executeConcolic(
			DefaultTestCase defaultTestCase) {

		logger.debug("Preparing concolic execution");

		/**
		 * Prepare DSC configuration
		 */
		MainConfig.setInstance();

		/**
		 * Path constraint and symbolic environment
		 */
		SymbolicEnvironment env = new SymbolicEnvironment(classLoader);
		PathConditionCollector pc = new PathConditionCollector();

		/**
		 * VM listeners
		 */
		List<IVM> listeners = new ArrayList<IVM>();
		listeners.add(new CallVM(env, classLoader));
		listeners.add(new JumpVM(env, pc));
		listeners.add(new HeapVM(env, pc, classLoader));
		listeners.add(new LocalsVM(env));
		listeners.add(new ArithmeticVM(env, pc));
		listeners.add(new OtherVM(env));
		listeners.add(new SymbolicFunctionVM(env, pc));
		VM.getInstance().setListeners(listeners);
		VM.getInstance().prepareConcolicExecution();

		defaultTestCase.changeClassLoader(classLoader);
		SymbolicObserver symbolicExecObserver = new SymbolicObserver(env);

		Set<ExecutionObserver> originalExecutionObservers = TestCaseExecutor.getInstance().getExecutionObservers();
		TestCaseExecutor.getInstance().newObservers();
		TestCaseExecutor.getInstance().addObserver(symbolicExecObserver);

		logger.info("Starting concolic execution");
		ExecutionResult result = new ExecutionResult(defaultTestCase, null);

		try {
			logger.debug("Executing test");

			long startConcolicExecutionTime = System.currentTimeMillis();
			result = TestCaseExecutor.getInstance().execute(defaultTestCase,
					Properties.CONCOLIC_TIMEOUT);
			long estimatedConcolicExecutionTime = System.currentTimeMillis()
					- startConcolicExecutionTime;
			DSEStats.getInstance().reportNewConcolicExecutionTime(estimatedConcolicExecutionTime);

			MaxStatementsStoppingCondition.statementsExecuted(result
					.getExecutedStatements());

		} catch (Exception e) {
			logger.error("Exception during concolic execution {}", e);
			return new ArrayList<BranchCondition>();
		} finally {
			logger.debug("Cleaning concolic execution");
			TestCaseExecutor.getInstance().setExecutionObservers(originalExecutionObservers);
		}
		VM.disableCallBacks(); // ignore all callbacks from now on
		
		List<BranchCondition> branches = pc.getPathCondition();
		logger.info("Concolic execution ended with " + branches.size()
				+ " branches collected");
		if (!result.noThrownExceptions()) {
			int idx = result.getFirstPositionOfThrownException();
			logger.info("Exception thrown: "
					+ result.getExceptionThrownAtPosition(idx));
		}
		logNrOfConstraints(branches);

		logger.debug("Cleaning concolic execution");
		TestCaseExecutor.getInstance().setExecutionObservers(originalExecutionObservers);

		return branches;
	}

	private static void logNrOfConstraints(List<BranchCondition> branches) {
		int nrOfConstraints = 0;

		ExpressionExecutor exprExecutor = new ExpressionExecutor();
		for (BranchCondition branchCondition : branches) {

			for (Constraint<?> supporting_constraint : branchCondition
					.getSupportingConstraints()) {
				supporting_constraint.getLeftOperand().accept(exprExecutor,null);
				supporting_constraint.getRightOperand().accept(exprExecutor, null);
				nrOfConstraints++;
			}

			Constraint<?> constraint = branchCondition.getConstraint();
			constraint.getLeftOperand().accept(exprExecutor,null);
			constraint.getRightOperand().accept(exprExecutor,null);
			nrOfConstraints++;

		}
		logger.debug("nrOfConstraints=" + nrOfConstraints);
	}
}
