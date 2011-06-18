/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Field;

/**
 * @author fraser
 * 
 */
public class FieldReference extends VariableReferenceImpl {

	private final Field field;

	private VariableReference source;

	/**
	 * @param testCase
	 * @param type
	 */
	public FieldReference(TestCase testCase, Field field, VariableReference source) {
		super(testCase, field.getGenericType());
		this.field = field;
		this.source = source;
		//		logger.info("Creating new field assignment for field " + field + " of object "
		//		        + source);

	}

	/**
	 * @param testCase
	 * @param type
	 */
	public FieldReference(TestCase testCase, Field field) {
		super(testCase, field.getGenericType());
		this.field = field;
		this.source = null;
	}

	/**
	 * Access the field
	 * 
	 * @return
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Access the source object
	 * 
	 * @return
	 */
	public VariableReference getSource() {
		return source;
	}

	/**
	 * Return the actual object represented by this variable for a given scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 */
	@Override
	public Object getObject(Scope scope) {
		try {
			if (source != null) {
				Object s = source.getObject(scope);
				return field.get(s);
			} else {
				return field.get(null);
			}
		} catch (IllegalArgumentException e) {
			logger.error("Error accessing field " + field + " of object " + source + ": "
			        + e);
			return null;
		} catch (IllegalAccessException e) {
			logger.info("Error accessing field " + field + " of object " + source + ": "
			        + e);
			return null;
		}
	}

	/**
	 * Set the actual object represented by this variable in a given scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 * @param value
	 *            The value to be assigned
	 */
	@Override
	public void setObject(Scope scope, Object value) {
		Object sourceObject = null;
		try {

			if (source != null) {
				sourceObject = source.getObject(scope);
				if (sourceObject == null)
					return;
			}
			if (value.getClass().equals(Integer.class)
			        || value.getClass().equals(int.class))
				field.setInt(sourceObject, (Integer) value);
			else if (value.getClass().equals(Boolean.class)
			        || value.getClass().equals(boolean.class))
				field.setBoolean(sourceObject, (Boolean) value);
			else if (value.getClass().equals(Byte.class)
			        || value.getClass().equals(byte.class))
				field.setByte(sourceObject, (Byte) value);
			else if (value.getClass().equals(Character.class)
			        || value.getClass().equals(char.class))
				field.setChar(sourceObject, (Character) value);
			else if (value.getClass().equals(Double.class)
			        || value.getClass().equals(double.class))
				field.setDouble(sourceObject, (Double) value);
			else if (value.getClass().equals(Float.class)
			        || value.getClass().equals(float.class))
				field.setFloat(sourceObject, (Float) value);
			else if (value.getClass().equals(Long.class)
			        || value.getClass().equals(long.class))
				field.setLong(sourceObject, (Long) value);
			else if (value.getClass().equals(Short.class)
			        || value.getClass().equals(short.class))
				field.setShort(sourceObject, (Short) value);
			else {
				if (!field.getType().isAssignableFrom(value.getClass())) {
					logger.error("Not assignable: " + value + " to field " + field
					        + " of variable " + source);
				}
				assert (field.getType().isAssignableFrom(value.getClass()));
				if (!field.getDeclaringClass().isAssignableFrom(sourceObject.getClass())) {
					logger.error("Field " + field + " defined in class "
					        + field.getDeclaringClass());
					logger.error("Source object " + sourceObject + " has class "
					        + sourceObject.getClass());
					logger.error("Value object " + value + " has class "
					        + value.getClass());
				}
				assert (field.getDeclaringClass().isAssignableFrom(sourceObject.getClass()));
				field.set(sourceObject, value);
			}
		} catch (IllegalArgumentException e) {
			logger.info("Error while assigning field: " + getName() + " with value "
			        + value + " on object " + sourceObject + ": " + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.info("Error while assigning field: " + e);
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getAdditionalVariableReference()
	 */
	@Override
	public VariableReference getAdditionalVariableReference() {
		if (source != null && source.getAdditionalVariableReference() != null)
			return source.getAdditionalVariableReference();
		else
			return source;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#setAdditionalVariableReference(de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void setAdditionalVariableReference(VariableReference var) {
		if (source != null
		        && !field.getDeclaringClass().isAssignableFrom(var.getVariableClass())) {
			logger.info("Not assignable: " + field.getDeclaringClass() + " and " + var);
			assert (false);
		}
		source = var;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#replaceAdditionalVariableReference(de.unisb.cs.st.evosuite.testcase.VariableReference, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replaceAdditionalVariableReference(VariableReference var1,
	        VariableReference var2) {
		if (source.equals(var1))
			source = var2;
		else
			source.replaceAdditionalVariableReference(var1, var2);
	}

	@Override
	public int getStPosition() {
		for (int i = 0; i < testCase.size(); i++) {
			if (testCase.getStatement(i).getReturnValue().equals(this)) {
				return i;
			}
		}
		if (source != null)
			return source.getStPosition();
		else
			throw new AssertionError(
			        "A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");

		//			return 0;
	}

	/**
	 * Return name for source code representation
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		if (source != null)
			return source.getName() + "." + field.getName();
		else
			return field.getDeclaringClass().getSimpleName() + "." + field.getName();
	}

	/**
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference clone(TestCase newTestCase) {
		if (source != null) {
			//			VariableReference otherSource = newTestCase.getStatement(source.getStPosition()).getReturnValue();
			VariableReference otherSource = source.clone(newTestCase);
			return new FieldReference(newTestCase, field, otherSource);
		} else {
			return new FieldReference(newTestCase, field);
		}
	}

	public int getDepth() {
		int depth = 1;
		if (source instanceof FieldReference) {
			depth += ((FieldReference) source).getDepth();
		}
		return depth;
	}
}
