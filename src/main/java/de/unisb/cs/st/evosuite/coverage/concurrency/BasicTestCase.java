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
import de.unisb.cs.st.evosuite.testcase.Statement;
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



	private final boolean replaceConst;

	/**
	 * Equals BasicTestCase(true)
	 */
	public BasicTestCase() {
		this(true);
	}

	/**
	 * @param replaceConst
	 */
	public BasicTestCase(boolean replaceConst){
		super();
		this.replaceConst=replaceConst;
	}

	/**
	 * The statements returned by this method can only be executed with a concurrentScope
	 * @param clazz
	 * @param pos
	 * @return
	 */
	@SuppressWarnings("unchecked") //we loose the type information during the call to new VariableReference. 
	private Statement getPseudoStatement(final Class clazz, int pos){
		Statement st= new Statement() {

			@Override
			public void replaceUnique(VariableReference oldVar, VariableReference newVar) {
			}

			@Override
			public void replace(VariableReference oldVar, VariableReference newVar) {
			}

			@Override
			public int hashCode() {
				return 0;
			}

			@Override
			public Set<VariableReference> getVariableReferences() {
				Set<VariableReference> s = new HashSet<VariableReference>();
				s.add(retval);
				return s;
			}

			@Override
			public List<VariableReference> getUniqueVariableReferences() {
				List<VariableReference> s = new ArrayList<VariableReference>();
				s.add(retval);
				return s;
			}

			@Override
			public String getCode(Throwable exception) {
				//#TODO steenbuck param0 should not be hardcoded
				return retval.getSimpleClassName() + " " + retval.getName() + " = param0;";
			}

			@Override
			public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
					Throwable exception) {
			}

			@Override
			public Throwable execute(Scope scope, PrintStream out)
			throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
				if(scope instanceof ConcurrentScope){
					//Object o = scope.get(new VariableReference(retval.getType(), -1));
					Object o = ((ConcurrentScope)scope).getSharedObject();
					assert(retval.getVariableClass().isAssignableFrom(o.getClass())) : "we want an " + retval.getVariableClass() + " but got an " + o.getClass();
					scope.set(retval, o);
				}else{
					throw new AssertionError("Statements from " + BasicTestCase.class.getName() + " should only be executed with a concurrent scope");
				}
				return null;
			}

			@Override
			public boolean equals(Statement s) {
				return s==this;
			}

			@Override
			public Statement clone() {
				return getPseudoStatement(clazz, retval.statement);
			}

			@Override
			public void adjustVariableReferences(int position, int delta) {
				retval.adjust(delta, position);
			}

		};

		st.SetRetval(new VariableReference(clazz, pos));

		return st;
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
	public VariableReference setStatement(Statement statement, int position) {
		assert(position>=0);
		statement = replaceConstructorStatement(statement, position);
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
	public void addStatement(Statement statement, int position) {
		assert(position>=0);
		assert(statement.getReturnValue().statement==position);
		statement = replaceConstructorStatement(statement, position);
		super.addStatement(statement, position);
	}

	/**
	 * Checks if a constructor call should reference a param
	 * @param statement
	 * @param position
	 * @return
	 */
	private Statement replaceConstructorStatement(Statement statement, int position){
		if(replaceConst && statement instanceof ConstructorStatement){
			ConstructorStatement c = (ConstructorStatement)statement;
			//#TODO steenbuck we should check if the constructor uses the object we supplied as param (if yes maybe we should let the object be created)
			assert(Properties.getTargetClass()!=null);
			if(Properties.getTargetClass().isAssignableFrom(c.getConstructor().getDeclaringClass())){
				logger.debug("Replaced a constructor call for " + c.getClass().getSimpleName() + " with a pseudo statement. Representing the object shared between the test threads");
				statement = getPseudoStatement(Properties.getTargetClass(), position);
			}
		}

		return statement;
	}

	/**
	 * Append new statement at end of test case
	 * 
	 * @param statement
	 *            New statement
	 * @return VariableReference of return value
	 */
	public void addStatement(Statement statement) {
		this.addStatement(statement, super.size());
	}



	/**
	 * Create a copy of the test case
	 */
	@Override
	public BasicTestCase clone() {
		BasicTestCase t = new BasicTestCase(replaceConst);
		List<Statement> newStatements = t.getStatements();
		for (Statement s : this) {
			newStatements.add(s.clone());
		}
		t.getCoveredGoals().addAll(super.getCoveredGoals());

		return t;
	}
}
