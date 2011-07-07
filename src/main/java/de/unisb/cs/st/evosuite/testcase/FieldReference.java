/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import de.unisb.cs.st.evosuite.contracts.NullPointerExceptionContract;

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
		assert(field!=null);
		assert(source!=null || Modifier.isStatic(field.getModifiers())) : "No source object was supplied, therefore we assumed the field to be static. However asking the field if it was static, returned false";
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
	public Object getObject(Scope scope) throws CodeUnderTestException {

		Object s;
		if(Modifier.isStatic(field.getModifiers())){
			s = null;
		}else{
			s = source.getObject(scope);
		}

		try{
			return field.get(s);
		} catch (IllegalArgumentException e) {
			logger.error("Error accessing field " + field + " of object " + source + ": "
					+ e, e);
			throw e;
		} catch (IllegalAccessException e) {
			logger.error("Error accessing field " + field + " of object " + source + ": "
					+ e, e);
			throw new EvosuiteError(e);
		} catch (NullPointerException e){
			throw new CodeUnderTestException(e);
		}catch (ExceptionInInitializerError e){
			throw new CodeUnderTestException(e);
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
	public void setObject(Scope scope, Object value) throws CodeUnderTestException {
		Object sourceObject = null;
		try {

			if (source != null) {
				sourceObject = source.getObject(scope);
				if (sourceObject == null){
					/*
					 * #FIXME this is dangerously far away from the java semantics
					 *	That means we can have a testcase
					 *  SomeObject var1 = null;
					 *  var1.someAttribute = test;
					 *  and the testcase will execute in evosuite, executing it with junit will however lead to a nullpointer exception
					 */
					return;
				}
			}
			if (field.getType().equals(int.class))
				field.setInt(sourceObject, (Integer) value);
			else if (field.getType().equals(boolean.class))
				field.setBoolean(sourceObject, (Boolean) value);
			else if (field.getType().equals(byte.class))
				field.setByte(sourceObject, (Byte) value);
			else if (field.getType().equals(char.class))
				field.setChar(sourceObject, (Character) value);
			else if (field.getType().equals(double.class))
				field.setDouble(sourceObject, (Double) value);
			else if (field.getType().equals(float.class))
				field.setFloat(sourceObject, (Float) value);
			else if (field.getType().equals(long.class))
				field.setLong(sourceObject, (Long) value);
			else if (field.getType().equals(short.class))
				field.setShort(sourceObject, (Short) value);
			else {
				if (value!=null && !field.getType().isAssignableFrom(value.getClass())) {
					logger.error("Not assignable: " + value + " of class "
							+ value.getClass() + " to field " + field + " of variable "
							+ source);
				}
				assert (value==null || field.getType().isAssignableFrom(value.getClass()));
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
			logger.error("Error while assigning field: " + getName() + " with value "
					+ value + " on object " + sourceObject + ": " + e, e);
			e.printStackTrace();
			throw e;
		} catch (IllegalAccessException e) {
			logger.error("Error while assigning field: " + e, e);
			throw new EvosuiteError(e);
		} catch (NullPointerException e) {
			throw new CodeUnderTestException(e);
		} catch (ExceptionInInitializerError e){
			throw new CodeUnderTestException(e);
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
		if (source != null) {
			if (source.equals(var1))
				source = var2;
			else
				source.replaceAdditionalVariableReference(var1, var2);
		}
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
		else {
			for (int i = 0; i < testCase.size(); i++) {
				if (testCase.getStatement(i).references(this)) {
					return i;
				}
			}
			throw new AssertionError(
			"A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase.");
		}

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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldReference other = (FieldReference) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

}
