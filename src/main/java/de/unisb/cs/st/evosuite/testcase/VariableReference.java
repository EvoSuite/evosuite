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

package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.lowagie.text.pdf.ArabicLigaturizer;

/**
 * This class represents a variable in a test case
 * 
 * TODO: Store generic types in this variable - we know at creation what it is
 * (from method calls)
 * 
 * @author Gordon Fraser
 * 
 */
public class VariableReference implements Comparable<VariableReference> {

	private static Logger logger = Logger.getLogger(VariableReference.class);
	
	/**
	 * Type (class) of the variable
	 */
	protected GenericClass type;

	/**
	 * If this variable is contained in an array, this is the reference to the
	 * array
	 */
	protected VariableReference array = null;

	/**
	 * Index in the array
	 */
	protected int array_index = 0;
	
	/**
	 * Index in the array
	 */
	protected int array_length = 0;

	/**
	 * The testCase in which this VariableReference is valid
	 */
	protected final TestCase testCase;
	
	/**
	 * Constructor
	 * 
	 * @param testCase 
	 * 			  The TestCase which defines the statement which defines this 
	 * @param type
	 *            The type (class) of the variable
	 * @param position
	 *            The statement in the test case that declares this variable
	 */
	public VariableReference(TestCase testCase, GenericClass type) {
		this.testCase=testCase;
		this.type = type;
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 *            The type (class) of the variable
	 * @param position
	 *            The statement in the test case that declares this variable
	 */
	public VariableReference(TestCase testCase, VariableReference array, int index, int length) {
		this.testCase=testCase;
		this.type = new GenericClass(array.getComponentType());
		this.array = array;
		this.array_index = index;
		this.array_length = length;
	}

	public VariableReference(TestCase testCase, Type type) {
		this(testCase, new GenericClass(type));
	}
	
	public int getArrayLength(){
		return array_length;
	}
	
	public void setArrayLength(int l){
		assert(l>=0);
		array_length=l;
	}
	
	/**
	 * The position of the statement, defining this VariableReference, in the testcase.
	 * @return
	 */
	public int getStPosition(){
		for(int i=0 ; i<testCase.size() ; i++){
			if(testCase.getStatement(i).getReturnValue().equals(this)){
				return i;
			}
		}
		
		if(isArrayIndex()){
			//notice that this case is only reached if no AssignmentStatement was used to assign to the array index (as in that case the for loop would have found something)
			//Therefore the array must have been assigned in some method and we can return the method call
			return array.getStPosition();
		}
		
		throw new AssertionError("A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");
	}

	/**
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference clone() {
		throw new UnsupportedOperationException("This method SHOULD not be used, as only the original reference is keeped up to date");
		/*VariableReference copy = new VariableReference(type, statement);
		if (array != null) {
			copy.array = array.clone();
			copy.array_index = array_index;
			copy.array_length = array_length;
		}
		return copy;*/
	}

	/**
	 * Return simple class name
	 */
	public String getSimpleClassName() {
		return type.getSimpleName();
	}

	/**
	 * Return class name
	 */
	public String getClassName() {
		return type.getClassName();
	}

	public String getComponentName() {
		return type.getComponentName();
	}

	public Type getComponentType() {
		return type.getComponentType();
	}

	public VariableReference getArray() {
		return array;
	}

	/**
	 * Return true if variable is an enumeration
	 */
	public boolean isEnum() {
		return type.isEnum();
	}

	/**
	 * Return true if variable is a primitive type
	 */
	public boolean isPrimitive() {
		return type.isPrimitive();
	}

	/**
	 * Return true if variable is void
	 */
	public boolean isVoid() {
		return type.isVoid();
	}

	/**
	 * Return true if variable is a string
	 */
	public boolean isString() {
		return type.isString();
	}

	/**
	 * Return true if variable is an array
	 */
	public boolean isArray() {
		return type.isArray();
	}
	
	public void setArray(VariableReference r){
		array=r;
	}

	/**
	 * Return true if variable is an array
	 */
	public boolean isArrayIndex() {
		return array != null;
	}

	/**
	 * Return true if type of variable is a primitive wrapper
	 */
	public boolean isWrapperType() {
		return type.isWrapperType();
	}

	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 */
	public boolean isAssignableFrom(Type other) {
		return type.isAssignableFrom(other);
	}

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 */
	public boolean isAssignableTo(Type other) {
		return type.isAssignableTo(other);
	}

	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 */
	public boolean isAssignableFrom(VariableReference other) {
		return type.isAssignableFrom(other.type);
	}

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 */
	public boolean isAssignableTo(VariableReference other) {
		return type.isAssignableTo(other.type);
	}

	/**
	 * Return type of this variable
	 */
	public Type getType() {
		return type.getType();
	}

	/**
	 * Set type of this variable
	 */
	public void setType(Type type) {
		this.type = new GenericClass(type);
	}

	/**
	 * Return raw class of this variable
	 */
	public Class<?> getVariableClass() {
		return type.getRawClass();
	}

	/**
	 * Return raw class of this variable's component
	 */
	public Class<?> getComponentClass() {
		return type.getRawClass().getComponentType();
	}

	/**
	 * Return the actual object represented by this variable for a given scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 */
	public Object getObject(Scope scope) {
		return scope.get(this);
	}

	/**
	 * Comparison
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj); //We can use the object equals as each VariableReference is only defined once
	}

	/**
	 * Hash function
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getStPosition();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((array == null) ? 0 : array.hashCode() + array_index);
		return result;
	}

	/**
	 * Return string representation of the variable
	 */
	@Override
	public String toString() {
		return "VariableReference: Statement " + getStPosition() + ", type "
		        + type.getTypeName();
	}

	/**
	 * Return name for source code representation
	 * 
	 * @return
	 */
	public String getName() {
		if (array != null)
			return array.getName() + "[" + array_index + "]";
		else
			return "var" + getStPosition();
	}

	public void loadBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
		if (array == null) {
			logger.debug("Loading variable in bytecode: " + getStPosition());
			if (getStPosition() < 0) {
				mg.visitInsn(Opcodes.ACONST_NULL);
			} else
				mg.loadLocal(locals.get(getStPosition()),
				             org.objectweb.asm.Type.getType(type.getRawClass()));
		} else {
			array.loadBytecode(mg, locals);
			mg.push(array_index);
			mg.arrayLoad(org.objectweb.asm.Type.getType(type.getRawClass()));
		}
	}

	public void storeBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
		if (array == null) {
			logger.debug("Storing variable in bytecode: " + getStPosition() + " of type "
			        + org.objectweb.asm.Type.getType(type.getRawClass()));
			if (!locals.containsKey(getStPosition()))
				locals.put(getStPosition(),
				           mg.newLocal(org.objectweb.asm.Type.getType(type.getRawClass())));
			mg.storeLocal(locals.get(getStPosition()),
			              org.objectweb.asm.Type.getType(type.getRawClass()));
		} else {
			array.loadBytecode(mg, locals);
			mg.push(array_index);
			mg.arrayStore(org.objectweb.asm.Type.getType(type.getRawClass()));
		}

	}

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
}
