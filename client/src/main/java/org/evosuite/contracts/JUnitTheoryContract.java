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
package org.evosuite.contracts;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;

public class JUnitTheoryContract extends Contract {

	private GenericMethod theoryMethod;
	
	private Object theoryReceiver;
	
	public JUnitTheoryContract(GenericMethod theoryMethod) throws InstantiationException, IllegalAccessException {
		this.theoryMethod = theoryMethod;
		this.theoryReceiver = theoryMethod.getDeclaringClass().newInstance();
		if(theoryMethod.getParameterTypes().length != 1)
			throw new IllegalArgumentException("Number of arguments needs to be one");
	}
	
	@Override
	public ContractViolation check(Statement statement, Scope scope,
			Throwable exception) {
		for(VariableReference var : getAllVariables(scope)) {
			logger.debug("Current variable: "+var);
			Object object = scope.getObject(var);

			if (object == null) {
				logger.debug("Current object is null");
				continue;
			}
			
			try {
				theoryMethod.getMethod().invoke(theoryReceiver, object);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				logger.warn("Error while checking contract: "+e);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				logger.warn("Error while checking contract: "+e);
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				return new ContractViolation(this, statement, e.getCause(), var);
			} catch(Throwable t) {
				logger.warn("New contract violation found: "+t);
				return new ContractViolation(this, statement, t, var);
			}
		}
		
		return null;
	}

	@Override
	public void addAssertionAndComments(Statement statement,
			List<VariableReference> variables, Throwable exception) {
		TestCase test = statement.getTestCase();
		int position = statement.getPosition();
		VariableReference a = variables.get(0);
		int pos = a.getStPosition();

		try {
			Constructor<?> defaultConstructor = theoryReceiver.getClass().getConstructor();
			GenericConstructor constructor = new GenericConstructor(defaultConstructor, theoryReceiver.getClass());
			Statement st1 = new ConstructorStatement(test, constructor, new ArrayList<VariableReference>());
			VariableReference receiver = test.addStatement(st1, position + 1);
			
			Statement st2 = new MethodStatement(test, theoryMethod, receiver, Arrays.asList(new VariableReference[] {test.getStatement(pos).getReturnValue()}));
			test.addStatement(st2, position + 2);
			st2.addComment("Violates theory: "+theoryMethod.getName());
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			logger.warn("Error while creating contract violation: "+e);

			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			logger.warn("Error while creating contract violation: "+e);
			e.printStackTrace();
		} 
		
	}
	
	@Override
	public void changeClassLoader(ClassLoader classLoader) {
		theoryMethod.changeClassLoader(classLoader);
		
		try {
			theoryReceiver = theoryMethod.getDeclaringClass().newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "JUnit Theory contract: "+theoryMethod.getName();
	}
}
