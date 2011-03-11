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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class StringTraceExecutionObserver extends ExecutionObserver {

	private static Logger logger = Logger.getLogger(StringTraceExecutionObserver.class);

	Map<Integer, String> trace = new HashMap<Integer, String>();

	@Override
	public void output(int position, String output) {
		//logger.info("Received output: "+output);
		//trace.put(position, output.trim());
		// TODO: can't use this for oracles
	}

	@Override
	public void statement(Statement statement, Scope scope, Throwable exception) {
		VariableReference retval = statement.getReturnValue();

		Object object = scope.get(retval);

		//System.out.println("TG: Adding value "+object.toString());
		// Only add string if this is not Object.toString()
		try {
			if (object == null) {
				//logger.info("Received return value null");
				trace.put(statement.getPosition(), "null");
			} else {
				Set<String> unusable = new HashSet<String>();
				unusable.add("java.lang.Object");
				//unusable.add("java.util.AbstractCollection");
				//unusable.add("org.jaxen.pattern.UnionPattern");
				//unusable.add("org.jaxen.pattern.LocationPathPattern");
				String declared_class = object.getClass().getMethod("toString").getDeclaringClass().getName();

				//				if(!object.getClass().getMethod("toString").getDeclaringClass().equals(java.lang.Object.class)) {
				if (!unusable.contains(declared_class)) {
					String value = object.toString();
					if (value == null) {
						//logger.info("Received return value that converts to null string");
						trace.put(statement.getPosition(), "null");
					} else {
						if (!value.matches("@[abcdef\\d]+")) {
							value = value.replaceAll("@[abcdef\\d]+", "");
							//logger.info(object.getClass().getMethod("toString").getDeclaringClass()+" says: "+value);
							trace.put(statement.getPosition(), value);
						}
					}
				}
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			logger.debug("Failed to add object of class " + object.getClass()
			        + " to string trace: " + e.getMessage());
		}
	}

	public StringOutputTrace getTrace() {
		return new StringOutputTrace(trace);
	}

	@Override
	public void clear() {
		trace.clear();
	}

}
