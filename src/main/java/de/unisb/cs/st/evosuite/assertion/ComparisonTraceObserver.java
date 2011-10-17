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

package de.unisb.cs.st.evosuite.assertion;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.testcase.CodeUnderTestException;
import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class ComparisonTraceObserver extends ExecutionObserver {

	private final static Logger logger = LoggerFactory.getLogger(ComparisonTraceObserver.class);

	private final ComparisonTrace trace = new ComparisonTrace();

	public ComparisonTraceObserver() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		try {
			VariableReference retval = statement.getReturnValue();
			if (retval == null || retval.isEnum() || retval.isPrimitive()
			        || exception != null)
				return;
			Object object = retval.getObject(scope);
			if (object == null) {
				logger.debug("Statement adds null value");
				return; // TODO: Add different check?
			}
			if (isWrapperType(object.getClass()))
				return;
			Map<Integer, Boolean> eqmap = new HashMap<Integer, Boolean>();
			Map<Integer, Integer> cmpmap = new HashMap<Integer, Integer>();

			logger.debug("Comparing to other objects of type " + retval.getClassName());
			//scope.printScope();
			//if(scope.hasObjects(retval.type)) {
			for (VariableReference other : scope.getElements(retval.getType())) {
				//logger.info("Found other object of type "+retval.type.getName()+" in scope");
				Object other_object = other.getObject(scope);
				// TODO: Create a matrix of object comparisons?
				if (other_object == null)
					continue; // TODO: Don't do this?

				try {
					logger.debug("Comparison of " + retval + " with " + other + " is: "
					        + object.equals(other_object));
					eqmap.put(other.getStPosition(), object.equals(other_object));
				} catch (Throwable t) {
					logger.debug("Exception during equals: " + t);
					/*
					logger.info("Type of retval: "+retval.getType());
					logger.info(object);
					logger.info(other_object);
					logger.info(object.getClass()+": "+object);
					logger.info(other_object.getClass()+": "+other_object);
					 */
					// ignore?
				}
				if (object instanceof Comparable<?>) {
					Comparable<Object> c = (Comparable<Object>) object;
					try {
						cmpmap.put(other.getStPosition(), c.compareTo(other_object));
					} catch (Throwable t) {
						logger.debug("Exception during compareto: " + t);
						/*
						logger.info("Type of retval: "+retval.getType());
						logger.info(object.getClass()+": "+object);
						logger.info(other_object.getClass()+": "+other_object);
						 */
						// ignore?
					}
				}
			}

			int position = statement.getPosition();
			trace.equals_map.put(position, eqmap);
			if (object instanceof Comparable<?>) {
				trace.compare_map.put(position, cmpmap);
			}
			// trace.return_values.put(position, retval.getStPosition());
			//} else {
			//	logger.info("No other objects of type "+retval.type.getName()+" in scope");
			//}
		} catch (CodeUnderTestException e) {
			throw new UnsupportedOperationException();
		}
	}

	public ComparisonTrace getTrace() {
		return trace.clone();
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		trace.clear();
	}

}
