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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class PrimitiveFieldTraceObserver extends ExecutionObserver {

	private final PrimitiveFieldTrace trace = new PrimitiveFieldTrace();

	@Override
	public void clear() {
		trace.trace.clear();
	}

	public PrimitiveFieldTrace getTrace() {
		return trace.clone();
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		VariableReference retval = statement.getReturnValue();

		if (retval == null) {
			return;
		}

		Object object = scope.get(retval);
		if ((object != null) && !object.getClass().isPrimitive() && !object.getClass().isEnum()
				&& !isWrapperType(object.getClass())) {
			// List<Object> fields = new ArrayList<Object>();
			// List<Field> valid_fields = new ArrayList<Field>();
			Map<Field, Object> fieldMap = new HashMap<Field, Object>();

			for (Field field : retval.getVariableClass().getFields()) {
				// TODO Check for wrapper types
				if ((!Modifier.isProtected(field.getModifiers()) && !Modifier.isPrivate(field.getModifiers()))
						&& !field.getType().equals(void.class) && field.getType().isPrimitive()) {
					try {
						fieldMap.put(field, field.get(object));
						// fields.add(field.get(object)); // TODO: Create copy
						// valid_fields.add(field);
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
				}
			}
			// if (!trace.fields.containsKey(statement.getPosition()))
			// trace.fields.put(retval.getType(), valid_fields);
			// trace.trace.put(statement.getPosition(), fields);
			trace.fieldMap.put(statement.getPosition(), fieldMap);
		}
	}

}
