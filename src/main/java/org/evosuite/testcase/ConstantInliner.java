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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.evosuite.testsuite.TestSuiteChromosome;


/**
 * Inline all primitive values and null references in the test case
 * 
 * @author Gordon Fraser
 * 
 */
public class ConstantInliner extends ExecutionObserver {

	private TestCase test = null;

	public void inline(TestCase test) {
		this.test = test;
		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		executor.addObserver(this);
		executor.execute(test);
		executor.removeObserver(this);
		removeUnusedVariables(test);
		assert (test.isValid());

	}

	public void inline(TestChromosome test) {
		inline(test.test);
	}

	public void inline(TestSuiteChromosome suite) {
		for (TestCase test : suite.getTests())
			inline(test);
	}

	/**
	 * Remove all unreferenced variables
	 * 
	 * @param t
	 *            The test case
	 * @return True if something was deleted
	 */
	public boolean removeUnusedVariables(TestCase t) {
		List<Integer> to_delete = new ArrayList<Integer>();
		boolean has_deleted = false;

		int num = 0;
		for (StatementInterface s : t) {
			if (s instanceof PrimitiveStatement) {

				VariableReference var = s.getReturnValue();
				if (!t.hasReferences(var)) {
					to_delete.add(num);
					has_deleted = true;
				}
			}
			num++;
		}
		Collections.sort(to_delete, Collections.reverseOrder());
		for (Integer position : to_delete) {
			t.remove(position);
		}

		return has_deleted;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
	 */
	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#statement(de.unisb.cs.st.evosuite.testcase.StatementInterface, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		try {
			for (VariableReference var : statement.getVariableReferences()) {
				if (var.equals(statement.getReturnValue())
				        || var.equals(statement.getReturnValue().getAdditionalVariableReference()))
					continue;
				if (var.isPrimitive() || var.isString()) {
					ConstantValue value = new ConstantValue(test, var.getGenericClass());
					value.setValue(var.getObject(scope));
					// logger.info("Statement before inlining: " + statement.getCode());
					statement.replace(var, value);
					// logger.info("Statement after inlining: " + statement.getCode());
				} else {
					// TODO: Ignoring exceptions during getObject, but keeping the assertion for now
					try {
						Object object = var.getObject(scope);
						if (object == null) {
							ConstantValue value = new ConstantValue(test,
							        var.getGenericClass());
							value.setValue(var.getObject(scope));
							// logger.info("Statement before inlining: " + statement.getCode());
							statement.replace(var, value);
							// logger.info("Statement after inlining: " + statement.getCode());
						}
					} catch (CodeUnderTestException e) {
						// ignore
					}
				}
			}
		} catch (CodeUnderTestException e) {

			throw new AssertionError("This case isn't handled yet: " + e.getCause()
			        + ", " + Arrays.asList(e.getStackTrace()));
		}

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}
}
