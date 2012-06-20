package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.utils.PassiveChangeListener;

public class VariableReferenceImpl implements VariableReference {

	private static final long serialVersionUID = -2621368452798208805L;

	private int distance = 0;

	private static Logger logger = LoggerFactory.getLogger(VariableReferenceImpl.class);

	/**
	 * Type (class) of the variable
	 */
	protected GenericClass type;

	/**
	 * The testCase in which this VariableReference is valid
	 */
	protected TestCase testCase;
	protected final PassiveChangeListener<Void> changeListener = new PassiveChangeListener<Void>();
	protected Integer stPosition;
	private String originalCode;

	/**
	 * Constructor
	 * 
	 * @param testCase
	 *            The TestCase which defines the statement which defines this
	 * @param type
	 *            The type (class) of the variable
	 * @param position
	 *            The statement in the test case that declares this variable
	 */
	public VariableReferenceImpl(TestCase testCase, GenericClass type) {
		this.testCase = testCase;
		this.type = type;
		testCase.addListener(changeListener);
	}

	public VariableReferenceImpl(TestCase testCase, Type type) {
		this(testCase, new GenericClass(type));
	}

	/**
	 * The position of the statement, defining this VariableReference, in the
	 * testcase.
	 * 
	 * @return
	 */
	@Override
	public int getStPosition() {
		if ((stPosition == null) || changeListener.hasChanged()) {
			stPosition = null;
			for (int i = 0; i < testCase.size(); i++) {
				StatementInterface stmt = testCase.getStatement(i); 
				if (stmt.getReturnValue().equals(this)) {
					stPosition = i;
					break;
				}
			}
			if (stPosition == null) {
				if (originalCode != null) {
					throw new AssertionError("Error transforming '" + originalCode + 
							"': no statement in the test defined the variable reference!");
				}
				throw new AssertionError(
				        "A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");
			}
		}
		return stPosition;
	}

	/**
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference clone() {
		throw new UnsupportedOperationException(
		        "This method SHOULD not be used, as only the original reference is keeped up to date");
		/*VariableReference copy = new VariableReference(type, statement);
		if (array != null) {
			copy.array = array.clone();
			copy.array_index = array_index;
			copy.array_length = array_length;
		}
		return copy;*/
	}

	/**
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference clone(TestCase newTestCase) {
		return newTestCase.getStatement(getStPosition()).getReturnValue();
	}

	@Override
	public VariableReference copy(TestCase newTestCase, int offset) {
		return newTestCase.getStatement(getStPosition() + offset).getReturnValue();
	}

	/**
	 * Return simple class name
	 */
	@Override
	public String getSimpleClassName() {
		// TODO: Workaround for bug in commons lang
		if (type.isPrimitive()
		        || (type.isArray() && new GenericClass(type.getComponentType()).isPrimitive()))
			return type.getRawClass().getSimpleName();

		return type.getSimpleName();
	}

	/**
	 * Return class name
	 */
	@Override
	public String getClassName() {
		return type.getClassName();
	}

	@Override
	public String getComponentName() {
		return type.getComponentName();
	}

	@Override
	public Type getComponentType() {
		return type.getComponentType();
	}

	/**
	 * Return true if variable is an enumeration
	 */
	@Override
	public boolean isEnum() {
		return type.isEnum();
	}

	/**
	 * Return true if variable is a primitive type
	 */
	@Override
	public boolean isPrimitive() {
		return type.isPrimitive();
	}

	/**
	 * Return true if variable is void
	 */
	@Override
	public boolean isVoid() {
		return type.isVoid();
	}

	/**
	 * Return true if variable is a string
	 */
	@Override
	public boolean isString() {
		return type.isString();
	}

	/**
	 * Return true if type of variable is a primitive wrapper
	 */
	@Override
	public boolean isWrapperType() {
		return type.isWrapperType();
	}

	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 */
	@Override
	public boolean isAssignableFrom(Type other) {
		return type.isAssignableFrom(other);
	}

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 */
	@Override
	public boolean isAssignableTo(Type other) {
		return type.isAssignableTo(other);
	}

	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 */
	@Override
	public boolean isAssignableFrom(VariableReference other) {
		return type.isAssignableFrom(other.getType());
	}

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 */
	@Override
	public boolean isAssignableTo(VariableReference other) {
		return type.isAssignableTo(other.getType());
	}

	/**
	 * Return type of this variable
	 */
	@Override
	public Type getType() {
		return type.getType();
	}

	/**
	 * Set type of this variable
	 */
	@Override
	public void setType(Type type) {
		this.type = new GenericClass(type);
	}

	/**
	 * Return raw class of this variable
	 */
	@Override
	public Class<?> getVariableClass() {
		return type.getRawClass();
	}

	/**
	 * Return raw class of this variable's component
	 */
	@Override
	public Class<?> getComponentClass() {
		return type.getRawClass().getComponentType();
	}

	/**
	 * Return the actual object represented by this variable for a given scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 */
	@Override
	public Object getObject(Scope scope) throws CodeUnderTestException {
		return scope.getObject(this);
	}
	
	@Override
	public String getOriginalCode(){
		return originalCode;
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
		scope.setObject(this, value);
	}

	public void setOriginalCode(String code){
		if (originalCode != null) {
			logger.debug("Original code already set to '{}', skip setting it to '{}'.", originalCode, code);
			return;
		}
		if (code != null) {
			this.originalCode = code.trim();
		}
	}
	
	/**
	 * Comparison
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj); //We can use the object equals as each VariableReference is only defined once
	}

	/**
	 * Return string representation of the variable
	 */
	@Override
	public String toString() {
		if (originalCode != null) {
			return originalCode;
		}
		return "VariableReference: Statement " + getStPosition() + ", type "
		        + type.getTypeName();
	}

	/**
	 * Return name for source code representation
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return "var" + getStPosition();
	}

	@Override
	public void loadBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {

		logger.debug("Loading variable in bytecode: " + getStPosition());
		if (getStPosition() < 0) {
			mg.visitInsn(Opcodes.ACONST_NULL);
		} else
			mg.loadLocal(locals.get(getStPosition()),
			             org.objectweb.asm.Type.getType(type.getRawClass()));
	}

	@Override
	public void storeBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {

		logger.debug("Storing variable in bytecode: " + getStPosition() + " of type "
		        + org.objectweb.asm.Type.getType(type.getRawClass()));
		if (!locals.containsKey(getStPosition()))
			locals.put(getStPosition(),
			           mg.newLocal(org.objectweb.asm.Type.getType(type.getRawClass())));
		mg.storeLocal(locals.get(getStPosition()),
		              org.objectweb.asm.Type.getType(type.getRawClass()));
	}

	@Override
	public Object getDefaultValue() {
		if (isVoid())
			return null;
		else if (type.isString())
			return "";
		else if (isPrimitive()) {
			if (type.getRawClass().equals(float.class))
				return 0.0F;
			else if (type.getRawClass().equals(long.class))
				return 0L;
			else if (type.getRawClass().equals(boolean.class))
				return false;
			else
				return 0;
		} else
			return null;
	}

	@Override
	public String getDefaultValueString() {
		if (isVoid())
			return "";
		else if (type.isString())
			return "\"\"";
		else if (isPrimitive()) {
			if (type.getRawClass().equals(float.class))
				return "0.0F";
			else if (type.getRawClass().equals(long.class))
				return "0L";
			else if (type.getRawClass().equals(boolean.class))
				return "false";
			else
				return "0";
		} else
			return "null";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(VariableReference other) {
		return getStPosition() - other.getStPosition();
	}

	@Override
	public boolean same(VariableReference r) {
		if (r == null)
			return false;

		if (this.getStPosition() != r.getStPosition())
			return false;

		if (this.type.equals(r.getGenericClass()))
			return true;

		return false;
	}

	@Override
	public GenericClass getGenericClass() {
		return type;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getAdditionalVariableReference()
	 */
	@Override
	public VariableReference getAdditionalVariableReference() {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#setAdditionalVariableReference(de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void setAdditionalVariableReference(VariableReference var) {
		// Do nothing by default
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#replaceAdditionalVariableReference(de.unisb.cs.st.evosuite.testcase.VariableReference, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replaceAdditionalVariableReference(VariableReference var1,
	        VariableReference var2) {
		// no op

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getDistance()
	 */
	@Override
	public int getDistance() {
		return distance;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#setDistance(int)
	 */
	@Override
	public void setDistance(int distance) {
		this.distance = distance;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#changeClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		type.changeClassLoader(loader);
	}
}
