package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class PrimitiveFieldTraceObserver extends ExecutionObserver {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(PrimitiveFieldTraceObserver.class);
	
	private PrimitiveFieldTrace trace = new PrimitiveFieldTrace();
	
	@Override
	public void clear() {
		trace.trace.clear();
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void statement(int position, Scope scope, VariableReference retval) {
		if(retval == null)
			return;

		Object object = scope.get(retval);
		if(object != null && !object.getClass().isPrimitive() && !object.getClass().isEnum() && !isWrapperType(object.getClass())) {
			List<Object> fields = new ArrayList<Object>();
			List<Field> valid_fields = new ArrayList<Field>();
			for(Field field : retval.getVariableClass().getFields()) {
				// TODO Check for wrapper types
				if((!Modifier.isProtected(field.getModifiers()) && !Modifier.isPrivate(field.getModifiers())) && !field.getType().equals(void.class) && field.getType().isPrimitive()) {
					try {
						fields.add(field.get(object)); // TODO: Create copy
						valid_fields.add(field);
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
				}
			}
			if(!trace.fields.containsKey(position))
				trace.fields.put(retval.getType(), valid_fields);
			trace.trace.put(position, fields);
		}
	}
	public PrimitiveFieldTrace getTrace() {
		return trace.clone();
	}

}
