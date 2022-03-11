/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testcase.variable;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.utils.PassiveChangeListener;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

public class VariableReferenceImpl implements VariableReference {

    private static final long serialVersionUID = -2621368452798208805L;

    private int distance = 0;

    private static final Logger logger = LoggerFactory.getLogger(VariableReferenceImpl.class);

    /**
     * Type (class) of the variable
     */
    protected GenericClass<?> type;

    /**
     * The testCase in which this VariableReference is valid
     */
    protected TestCase testCase;
    protected final PassiveChangeListener<Void> changeListener = new PassiveChangeListener<>();
    protected Integer stPosition;
    private String originalCode;

    /**
     * Constructor
     *
     * @param testCase The TestCase which defines the statement which defines this
     * @param type     The type (class) of the variable
     */
    public VariableReferenceImpl(TestCase testCase, GenericClass<?> type) {
        this.testCase = testCase;
        this.type = type;
        testCase.addListener(changeListener);
    }

    /**
     * <p>
     * Constructor for VariableReferenceImpl.
     * </p>
     *
     * @param testCase a {@link org.evosuite.testcase.TestCase} object.
     * @param type     a {@link java.lang.reflect.Type} object.
     */
    public VariableReferenceImpl(TestCase testCase, Type type) {
        this(testCase, GenericClassFactory.get(type));
    }

    /**
     * {@inheritDoc}
     * <p>
     * The position of the statement, defining this VariableReference, in the
     * testcase.
     * <p>
     * TODO: Notify change listener also when return value changes
     */
    @Override
    public synchronized int getStPosition() {
        if (stPosition == null || changeListener.hasChanged()) {
            stPosition = null;
            for (int i = 0; i < testCase.size(); i++) {
                Statement stmt = testCase.getStatement(i);
                if (stmt.getReturnValue().equals(this)) {
                    stPosition = i;
                    break;
                }
            }
            if (stPosition == null) {
                String msg = "Bloody annoying bug \n";
                msg += "Test case has " + testCase.size() + " function calls \n";
                for (int i = 0; i < testCase.size(); i++) {
                    msg += testCase.getStatement(i).getCode(null) + "\n";
                }
                msg += "failed to find type " + this.type.getTypeName() + "\n";

                throw new AssertionError(
                        msg + "A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");
            }
        } else {
            int position = stPosition; //somehow this could be null, leading to NPE. Synchronization issue?
            stPosition = null;
            stPosition = getStPosition();
            assert (stPosition == position);
        }
        return stPosition;
    }

    @Override
    public TestCase getTestCase() {
        return testCase;
    }

    /**
     * {@inheritDoc}
     * <p>
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
     * {@inheritDoc}
     * <p>
     * Create a copy of the current variable
     */
    @Override
    public VariableReference clone(TestCase newTestCase) {
        return newTestCase.getStatement(getStPosition()).getReturnValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference copy(TestCase newTestCase, int offset) {
        return newTestCase.getStatement(getStPosition() + offset).getReturnValue();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return simple class name
     */
    @Override
    public String getSimpleClassName() {
        // TODO: Workaround for bug in commons lang
        if (type.isPrimitive()
                || (type.isArray() && GenericClassFactory.get(type.getComponentType()).isPrimitive()))
            return type.getRawClass().getSimpleName();

        return type.getSimpleName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return class name
     */
    @Override
    public String getClassName() {
        return type.getClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentName() {
        return type.getComponentName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getComponentType() {
        return type.getComponentType();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return true if variable is an enumeration
     */
    @Override
    public boolean isEnum() {
        return type.isEnum();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.VariableReference#isArray()
     */
    @Override
    public boolean isArray() {
        return type.isArray();
    }

    @Override
    public boolean isArrayIndex() {
        return false;
    }

    @Override
    public boolean isFieldReference() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return true if variable is a primitive type
     */
    @Override
    public boolean isPrimitive() {
        return type.isPrimitive();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return true if variable is void
     */
    @Override
    public boolean isVoid() {
        return type.isVoid();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return true if variable is a string
     */
    @Override
    public boolean isString() {
        return type.isString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return true if type of variable is a primitive wrapper
     */
    @Override
    public boolean isWrapperType() {
        return type.isWrapperType();
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return true if other type can be assigned to this variable
     */
    @Override
    public boolean isAssignableFrom(Type other) {
        return type.isAssignableFrom(other);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return true if this variable can by assigned to a variable of other type
     */
    @Override
    public boolean isAssignableTo(Type other) {
        //if (type.hasWildcardTypes()) {
        //	GenericClass rawClass = new GenericClass(other);
        //	logger.warn("Getting raw assignables for: "+other +" and "+type);
        //	logger.warn(testCase.toCode());
        //	TypeUtils.isAssignable(other, getType());
        //	return GenericClass.isAssignable(rawClass.getRawClass(), type.getRawClass());
        //} else {
        return type.isAssignableTo(other);
        //}
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return true if other type can be assigned to this variable
     */
    @Override
    public boolean isAssignableFrom(VariableReference other) {
        return type.isAssignableFrom(other.getType());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return true if this variable can by assigned to a variable of other type
     */
    @Override
    public boolean isAssignableTo(VariableReference other) {
        return type.isAssignableTo(other.getType());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return type of this variable
     */
    @Override
    public Type getType() {
        return type.getType();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set type of this variable
     */
    @Override
    public void setType(Type type) {
        this.type = GenericClassFactory.get(type);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return raw class of this variable
     */
    @Override
    public Class<?> getVariableClass() {
        return type.getRawClass();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return raw class of this variable's component
     */
    @Override
    public Class<?> getComponentClass() {
        return type.getRawClass().getComponentType();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return the actual object represented by this variable for a given scope
     */
    @Override
    public Object getObject(Scope scope) throws CodeUnderTestException {
        return scope.getObject(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOriginalCode() {
        return originalCode;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the actual object represented by this variable in a given scope
     */
    @Override
    public void setObject(Scope scope, Object value) throws CodeUnderTestException {
        scope.setObject(this, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOriginalCode(String code) {
        if (originalCode != null) {
            logger.debug("Original code already set to '{}', skip setting it to '{}'.",
                    originalCode, code);
            return;
        }
        if (code != null) {
            this.originalCode = code.trim();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
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
     * {@inheritDoc}
     * <p>
     * Return name for source code representation
     */
    @Override
    public String getName() {
        return "var" + getStPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {

        logger.debug("Loading variable in bytecode: " + getStPosition());
        if (getStPosition() < 0) {
            mg.visitInsn(Opcodes.ACONST_NULL);
        } else
            mg.loadLocal(locals.get(getStPosition()),
                    org.objectweb.asm.Type.getType(type.getRawClass()));
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(VariableReference other) {
        return getStPosition() - other.getStPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean same(VariableReference r) {
        if (r == null)
            return false;

        if (this.getStPosition() != r.getStPosition())
            return false;

        return this.type.equals(r.getGenericClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericClass<?> getGenericClass() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.VariableReference#getAdditionalVariableReference()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference getAdditionalVariableReference() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.VariableReference#setAdditionalVariableReference(org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAdditionalVariableReference(VariableReference var) {
        // Do nothing by default
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.VariableReference#replaceAdditionalVariableReference(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceAdditionalVariableReference(VariableReference var1,
                                                   VariableReference var2) {
        // no op

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.VariableReference#getDistance()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDistance() {
        return distance;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.VariableReference#setDistance(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.VariableReference#changeClassLoader(java.lang.ClassLoader)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeClassLoader(ClassLoader loader) {
        type.changeClassLoader(loader);
    }
}
