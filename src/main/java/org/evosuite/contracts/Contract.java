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
package org.evosuite.contracts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.FieldStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Based on ObjectContract / Randoop
 * 
 * @author Gordon Fraser
 */
public abstract class Contract {

	protected static Logger logger = LoggerFactory.getLogger(Contract.class);

	protected class Pair {
		Object object1;
		Object object2;

		public Pair(Object o1, Object o2) {
			object1 = o1;
			object2 = o2;
		}
	}

	protected Collection<Object> getAllObjects(Scope scope) {
		// TODO: Assignable classes and subclasses?
		return scope.getObjects(Properties.getTargetClass());
	}

	protected Collection<Pair> getAllObjectPairs(Scope scope) {
		Set<Pair> pairs = new HashSet<Pair>();
		for (Object o1 : scope.getObjects(Properties.getTargetClass())) {
			for (Object o2 : scope.getObjects(o1.getClass())) {
				pairs.add(new Pair(o1, o2));
			}
		}
		return pairs;
	}

	protected Collection<Object> getAffectedObjects(StatementInterface statement,
	        Scope scope) {
		try {
			Set<Object> objects = new HashSet<Object>();
			if (statement instanceof ConstructorStatement
			        || statement instanceof FieldStatement) {
				objects.add(statement.getReturnValue().getObject(scope));
			} else if (statement instanceof MethodStatement) {
				MethodStatement ms = (MethodStatement) statement;
				Object o = statement.getReturnValue().getObject(scope);
				if (o != null)
					objects.add(o);
				if (!ms.isStatic())
					objects.add(ms.getCallee().getObject(scope));
			}
			return objects;
		} catch (CodeUnderTestException e) {
			throw new UnsupportedOperationException();
		}

	}

	protected Collection<Pair> getAffectedObjectPairs(StatementInterface statement,
	        Scope scope) {
		try {
			Set<Pair> pairs = new HashSet<Pair>();

			if (statement instanceof ConstructorStatement
			        || statement instanceof FieldStatement) {
				Object o = statement.getReturnValue().getObject(scope);
				if (o != null) {
					for (Object o1 : scope.getObjects(o.getClass())) {
						for (Object o2 : scope.getObjects(o.getClass())) {
							pairs.add(new Pair(o1, o2));
						}
					}
				}
			} else if (statement instanceof MethodStatement) {
				MethodStatement ms = (MethodStatement) statement;
				Object o = statement.getReturnValue().getObject(scope);
				if (o != null) {
					for (Object o1 : scope.getObjects(o.getClass())) {
						for (Object o2 : scope.getObjects(o.getClass())) {
							pairs.add(new Pair(o1, o2));
						}
					}
				}
				if (!ms.isStatic()) {
					o = ms.getCallee().getObject(scope);
					if (o != null) {
						for (Object o1 : scope.getObjects(o.getClass())) {
							for (Object o2 : scope.getObjects(o.getClass())) {
								pairs.add(new Pair(o1, o2));
							}
						}
					}
				}
			}
			return pairs;
		} catch (CodeUnderTestException e) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Check if this statement is related to the unit under test
	 * 
	 * @param statement
	 * @return
	 */
	protected boolean isTargetStatement(StatementInterface statement) {
		//if (statement.getReturnClass().equals(Properties.getTargetClass()))
		//	return true;

		if (statement instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) statement;
			if (Properties.getTargetClass().equals(ms.getMethod().getDeclaringClass()))
				return true;
		} else if (statement instanceof ConstructorStatement) {
			ConstructorStatement cs = (ConstructorStatement) statement;
			if (Properties.getTargetClass().equals(cs.getConstructor().getDeclaringClass()))
				return true;
		} else if (statement instanceof FieldStatement) {
			FieldStatement fs = (FieldStatement) statement;
			if (Properties.getTargetClass().equals(fs.getField().getDeclaringClass()))
				return true;
		}

		return false;
	}

	/**
	 * Run the test against this contract and determine whether it reports a
	 * failure
	 * 
	 * @param test
	 * @return
	 */
	public boolean fails(TestCase test) {
		ContractChecker.setActive(false);
		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		SingleContractChecker checker = new SingleContractChecker(this);
		executor.addObserver(checker);
		TestCaseExecutor.runTest(test);
		executor.removeObserver(checker);
		//ContractChecker.setActive(true);
		return !checker.isValid();
	}

	public abstract boolean check(StatementInterface statement, Scope scope,
	        Throwable exception);

}
