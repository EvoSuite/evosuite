/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.AbstractStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * A test case is a list of statements
 * If replaceConst is set to true (default) all occurrences of a constructor, of the class for which tests are generated,
 * are replaced with a pseudo statement. Which refers to the object which is shared between the threads.
 * Statements from this testCase should only be executed with a ConcurrentScope
 * 
 * @author Sebastian Steenbuck
 * 
 * #TODO steenbuck BasicTestCase should have a more descriptive name
 * 
 */
public class BasicTestCase extends DefaultTestCase {

	private static Logger logger = Logger.getLogger(BasicTestCase.class);


	/**
	 * Equals BasicTestCase(true)
	 */
	public BasicTestCase() {
		super();
	}





	public String getThreadCode(Map<Integer, Throwable> exceptions, int id){
		throw new AssertionError("we should execute the one in concurrentTestCase");
	}



	/**
	 * Set new statement at position
	 * 
	 * @param statement
	 *            New statement
	 * @param position
	 *            Position at which to add
	 * @return Return value of statement
	 */
	@Override
	public VariableReference setStatement(StatementInterface statement, int position) {
		assert(position>=0);
		return super.setStatement(statement, position);
	}

	/**
	 * Add new statement at position and fix following variable references
	 * 
	 * @param statement
	 *            New statement
	 * @param position
	 *            Position at which to add
	 * @return Return value of statement
	 */
	@Override
	public VariableReference addStatement(StatementInterface statement, int position) {
		assert(position>=0);
		assert(statement!=null);
		assert(statement.getReturnValue()!=null);
		VariableReference ret = super.addStatement(statement, position);
		assert(statement.getReturnValue().getStPosition()==position);
		return ret;
	}

	/**
	 * Append new statement at end of test case
	 * 
	 * @param statement
	 *            New statement
	 * @return VariableReference of return value
	 */
	public void addStatement(AbstractStatement statement) {
		this.addStatement(statement, this.size());
	}



	/**
	 * Create a copy of the test case
	 */
	@Override
	public BasicTestCase clone() {
		BasicTestCase newTestCase = new BasicTestCase();
		for (StatementInterface s : this) {
			newTestCase.statements.add(s.clone(newTestCase));
		}
		newTestCase.getCoveredGoals().addAll(super.getCoveredGoals());

		return newTestCase;
	}
}
