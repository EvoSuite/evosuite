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
package org.evosuite.symbolic;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.vm.ArithmeticVM;
import org.evosuite.symbolic.vm.CallVM;
import org.evosuite.symbolic.vm.FunctionVM;
import org.evosuite.symbolic.vm.HeapVM;
import org.evosuite.symbolic.vm.JumpVM;
import org.evosuite.symbolic.vm.LocalsVM;
import org.evosuite.symbolic.vm.OtherVM;
import org.evosuite.symbolic.vm.PathConstraint;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uta.cse.dsc.IVM;
import edu.uta.cse.dsc.MainConfig;
import edu.uta.cse.dsc.VM;
import edu.uta.cse.dsc.instrument.DscInstrumentingClassLoader;

/**
 * <p>
 * ConcolicExecution class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class ConcolicExecution {

	private static Logger logger = LoggerFactory.getLogger(ConcolicExecution.class);

	/**
	 * Retrieve the path condition for a given test case
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<BranchCondition> getSymbolicPath(TestChromosome test) {
		TestChromosome dscCopy = (TestChromosome) test.clone();
		DefaultTestCase defaultTestCase = (DefaultTestCase) dscCopy.getTestCase();

		return executeConcolic(defaultTestCase);
	}

	protected static List<BranchCondition> executeConcolic(DefaultTestCase defaultTestCase) {

		logger.debug("Preparing concolic execution");

		/**
		 * Prepare DSC configuration
		 */
		MainConfig.setInstance();
		/**
		 * Instrumenting class loader
		 */
		DscInstrumentingClassLoader classLoader = new DscInstrumentingClassLoader();

		/**
		 * Path constraint and symbolic environment
		 */
		SymbolicEnvironment env = new SymbolicEnvironment(classLoader);
		PathConstraint pc = new PathConstraint();

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
		listeners.add(new FunctionVM(env));
		VM.vm.setListeners(listeners);
		VM.vm.startupConcolicExecution();

		defaultTestCase.changeClassLoader(classLoader);
		SymbolicObserver symbolicExecObserver = new SymbolicObserver(env);

		TestCaseExecutor.getInstance().addObserver(symbolicExecObserver);

		logger.info("Starting concolic execution");
		ExecutionResult result = TestCaseExecutor.runTest(defaultTestCase);
		List<BranchCondition> branches = pc.getBranchConditions();
		logger.info("Concolic execution ended with " + branches.size()
		        + " branches collected");
		if (!result.noThrownExceptions()) {
			int idx = result.getFirstPositionOfThrownException();
			logger.info("Exception thrown: " + result.getExceptionThrownAtPosition(idx));
		}
		logNrOfConstraints(branches);

		logger.debug("Cleaning concolic execution");
		TestCaseExecutor.getInstance().removeObserver(symbolicExecObserver);
		VM.vm.cleanupConcolicExecution();

		return branches;
	}

	private static void logNrOfConstraints(List<BranchCondition> branches) {
		int nrOfConstraints = 0;
		for (BranchCondition branchCondition : branches) {

			Constraint<?> constraint = branchCondition.getLocalConstraint();
			Object leftVal = constraint.getLeftOperand().execute();
			Object rightVal = constraint.getRightOperand().execute();
			nrOfConstraints++;
		}

		logger.debug("nrOfConstraints=" + nrOfConstraints);
	}
}
