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

import com.googlecode.gentyref.GenericTypeReflector;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.generic.GenericClassFactory;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class defines an reference to an array element. E.g. foo[3]
 *
 * @author Sebastian Steenbuck
 */
public class ArrayIndex extends VariableReferenceImpl {

    private static final long serialVersionUID = -4492869536935582711L;

    /**
     * Index in the array
     */
    private List<Integer> indices;

    /**
     * If this variable is contained in an array, this is the reference to the
     * array
     */
    protected ArrayReference array = null;

    /**
     * Constructor
     *
     * @param testCase a {@link org.evosuite.testcase.TestCase} object.
     * @param array    a {@link ArrayReference} object.
     * @param index    a int.
     */
    public ArrayIndex(TestCase testCase, ArrayReference array, int index) {
        this(testCase, array, Collections.singletonList(index));
    }

    /**
     * <p>
     * Constructor for ArrayIndex.
     * </p>
     *
     * @param testCase a {@link org.evosuite.testcase.TestCase} object.
     * @param array    a {@link ArrayReference} object.
     * @param indices  a {@link java.util.List} object.
     */
    public ArrayIndex(TestCase testCase, ArrayReference array, List<Integer> indices) {
        super(testCase, GenericClassFactory.get(getReturnType(array, indices.size())));
        this.array = array;
        setArrayIndices(indices);
    }

    private static Type getReturnType(ArrayReference array, int indicesCnt) {
        assert indicesCnt >= 1;
        Type result = array.getComponentType();
        // Class<?> result = (Class<?>) array.getComponentType();
        for (int idx = 1; idx < indicesCnt; idx++) {
            result = GenericTypeReflector.getArrayComponentType(result);
            //result = result.getComponentType();
        }
        return result;
    }

    /**
     * <p>
     * Getter for the field <code>array</code>.
     * </p>
     *
     * @return a {@link ArrayReference} object.
     */
    public ArrayReference getArray() {
        return array;
    }

    /**
     * <p>
     * Setter for the field <code>array</code>.
     * </p>
     *
     * @param r a {@link ArrayReference} object.
     */
    public void setArray(ArrayReference r) {
        array = r;
    }

    /**
     * Return true if variable is an array
     *
     * @return a boolean.
     */
    public boolean isArrayIndex() {
        return true;
    }

    /**
     * <p>
     * getArrayIndex
     * </p>
     *
     * @return a int.
     */
    public int getArrayIndex() {
        assert indices.size() == 1;
        return indices.get(0);
    }

    /**
     * <p>
     * setArrayIndex
     * </p>
     *
     * @param index a int.
     */
    public void setArrayIndex(int index) {
        assert indices.size() == 1;
        indices.set(0, index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStPosition() {
        assert (array != null);
        for (int i = 0; i < testCase.size(); i++) {
            if (testCase.getStatement(i).getReturnValue().equals(this)) {
                return i;
            }
        }

        //notice that this case is only reached if no AssignmentStatement was used to assign to the array index (as in that case the for loop would have found something)
        //Therefore the array must have been assigned in some method and we can return the method call

        //throw new AssertionError(
        //        "A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");

        return array.getStPosition();

        //throw new AssertionError("A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return name for source code representation
     */
    @Override
    public String getName() {
        return ArrayUtil.buildArrayIndexName(array.getName(), indices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
        if (indices.size() > 1) {
            throw new RuntimeException("Not yet implemented for multidimensional arrays!");
        }
        array.loadBytecode(mg, locals);
        mg.push(indices.get(0));
        mg.arrayLoad(org.objectweb.asm.Type.getType(type.getRawClass()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
        if (indices.size() > 1) {
            throw new RuntimeException("Not yet implemented for multidimensional arrays!");
        }

        int localVar = mg.newLocal(org.objectweb.asm.Type.getType(getVariableClass()));
        mg.storeLocal(localVar);

        array.loadBytecode(mg, locals);

        mg.push(indices.get(0));
        mg.loadLocal(localVar);

        mg.arrayStore(org.objectweb.asm.Type.getType(type.getRawClass()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean same(VariableReference r) {
        if (r == null)
            return false;

        if (!(r instanceof ArrayIndex))
            return false;

        ArrayIndex other = (ArrayIndex) r;
        if (this.getStPosition() != r.getStPosition())
            return false;

        if (!this.array.same(other.getArray()))
            return false;

        if (!indices.equals(other.indices))
            return false;

        if (this.type.equals(r.getGenericClass()))
            ;

        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return the actual object represented by this variable for a given scope
     */
    @Override
    public Object getObject(Scope scope) throws CodeUnderTestException {
        Object arrayObject = array.getObject(scope);
        try {
            for (int idx = 0; idx < indices.size() - 1; idx++) {
                if (arrayObject == null) {
                    throw new CodeUnderTestException(new NullPointerException());
                }
                arrayObject = Array.get(arrayObject, indices.get(idx));
            }
            if (arrayObject == null) {
                throw new CodeUnderTestException(new NullPointerException());
            }
            return Array.get(arrayObject, indices.get(indices.size() - 1));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CodeUnderTestException(e);
        }
    }

    private Integer getIntValue(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).intValue();
        } else if (object instanceof Character) {
            return (int) (Character) object;
        } else
            return 0;
    }

    private Short getShortValue(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return (short) ((Number) object).intValue();
        } else if (object instanceof Character) {
            return (short) ((Character) object).charValue();
        } else
            return 0;
    }

    private Byte getByteValue(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return (byte) ((Number) object).intValue();
        } else if (object instanceof Character) {
            return (byte) ((Character) object).charValue();
        } else
            return 0;
    }

    private Long getLongValue(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).longValue();
        } else if (object instanceof Character) {
            return (long) (Character) object;
        } else
            return 0L;
    }

    private Float getFloatValue(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).floatValue();
        } else if (object instanceof Character) {
            return (float) (Character) object;
        } else
            return 0F;
    }

    private Double getDoubleValue(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else if (object instanceof Character) {
            return (double) (Character) object;
        } else
            return 0.0;
    }

    private Character getCharValue(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Character) {
            return (Character) object;
        } else if (object instanceof Number) {
            return (char) ((Number) object).intValue();
        } else
            return '0';
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the actual object represented by this variable in a given scope
     */
    @Override
    public void setObject(Scope scope, Object value) throws CodeUnderTestException {
        Object arrayObject = array.getObject(scope);
        try {
            for (int idx = 0; idx < indices.size() - 1; idx++) {
                if (arrayObject == null) {
                    throw new CodeUnderTestException(new NullPointerException());
                }
                arrayObject = Array.get(arrayObject, indices.get(idx));
            }
            if (arrayObject == null) {
                throw new CodeUnderTestException(new NullPointerException());
            }
            if (value == null && arrayObject.getClass().getComponentType().isPrimitive()) {
                throw new CodeUnderTestException(new NullPointerException());
            }
            if (arrayObject.getClass().getComponentType().equals(int.class))
                Array.setInt(arrayObject, indices.get(indices.size() - 1),
                        getIntValue(value));
            else if (arrayObject.getClass().getComponentType().equals(boolean.class))
                Array.setBoolean(arrayObject, indices.get(indices.size() - 1),
                        (Boolean) value);
            else if (arrayObject.getClass().getComponentType().equals(char.class)) {
                Array.setChar(arrayObject, indices.get(indices.size() - 1),
                        getCharValue(value));
            } else if (arrayObject.getClass().getComponentType().equals(double.class))
                Array.setDouble(arrayObject, indices.get(indices.size() - 1),
                        getDoubleValue(value));
            else if (arrayObject.getClass().getComponentType().equals(float.class))
                Array.setFloat(arrayObject, indices.get(indices.size() - 1),
                        getFloatValue(value));
            else if (arrayObject.getClass().getComponentType().equals(long.class))
                Array.setLong(arrayObject, indices.get(indices.size() - 1),
                        getLongValue(value));
            else if (arrayObject.getClass().getComponentType().equals(short.class))
                Array.setShort(arrayObject, indices.get(indices.size() - 1),
                        getShortValue(value));
            else if (arrayObject.getClass().getComponentType().equals(byte.class))
                Array.setByte(arrayObject, indices.get(indices.size() - 1),
                        getByteValue(value));
                // We also need to check if we are assigning to a wrapper type, because autoboxing
                // only seems to work from int -> Integer, but e.g. not from byte -> Integer
            else if (arrayObject.getClass().getComponentType().equals(Integer.class))
                Array.set(arrayObject, indices.get(indices.size() - 1),
                        getIntValue(value));
            else if (arrayObject.getClass().getComponentType().equals(Boolean.class))
                Array.set(arrayObject, indices.get(indices.size() - 1), value);
            else if (arrayObject.getClass().getComponentType().equals(Character.class)) {
                Array.set(arrayObject, indices.get(indices.size() - 1),
                        getCharValue(value));
            } else if (arrayObject.getClass().getComponentType().equals(Double.class))
                Array.set(arrayObject, indices.get(indices.size() - 1),
                        getDoubleValue(value));
            else if (arrayObject.getClass().getComponentType().equals(Float.class))
                Array.set(arrayObject, indices.get(indices.size() - 1),
                        getFloatValue(value));
            else if (arrayObject.getClass().getComponentType().equals(Long.class))
                Array.set(arrayObject, indices.get(indices.size() - 1),
                        getLongValue(value));
            else if (arrayObject.getClass().getComponentType().equals(Short.class))
                Array.set(arrayObject, indices.get(indices.size() - 1),
                        getShortValue(value));
            else if (arrayObject.getClass().getComponentType().equals(Byte.class))
                Array.set(arrayObject, indices.get(indices.size() - 1),
                        getByteValue(value));
            else {
                Array.set(arrayObject, indices.get(indices.size() - 1), value);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CodeUnderTestException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Create a copy of the current variable
     */
    @Override
    public VariableReference copy(TestCase newTestCase, int offset) {
        ArrayReference otherArray = (ArrayReference) newTestCase.getStatement(array.getStPosition()
                + offset).getReturnValue();
        //must be set as we only use this to clone whole testcases
        return new ArrayIndex(newTestCase, otherArray, indices);
    }

    @Override
    public VariableReference clone(TestCase newTestCase) {
        return new ArrayIndex(newTestCase, (ArrayReference) array.clone(newTestCase), indices);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.VariableReference#getAdditionalVariableReference()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference getAdditionalVariableReference() {
        if (array.getAdditionalVariableReference() == null)
            return array;
        else
            return array.getAdditionalVariableReference();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.VariableReference#setAdditionalVariableReference(org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAdditionalVariableReference(VariableReference var) {
        assert (var instanceof ArrayReference);
        array = (ArrayReference) var;
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
        if (array.equals(var1)) {
            if (var2 instanceof ArrayReference) {
                array = (ArrayReference) var2;
            }
            // EvoSuite might try to replace this with a field reference
            // but for this we have FieldStatements, which would give us
            // ArrayReferences.
            // Such a replacement should only happen as part of a graceful delete
        } else
            array.replaceAdditionalVariableReference(var1, var2);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((array == null) ? 0 : array.hashCode());
        // TODO: indices shouldn't normally be null
        //       but it sometimes happens for StrongMutation...
        result = prime * result + ((indices == null) ? 0 : indices.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ArrayIndex other = (ArrayIndex) obj;
        if (array == null) {
            if (other.array != null)
                return false;
        } else if (!array.equals(other.array))
            return false;
        return indices.equals(other.indices);
    }

    /**
     * <p>
     * setArrayIndices
     * </p>
     *
     * @param indices a {@link java.util.List} object.
     */
    public void setArrayIndices(List<Integer> indices) {
        this.indices = new ArrayList<>();
        for (Integer i : indices)
            this.indices.add(i);
    }

    /**
     * <p>
     * getArrayIndices
     * </p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Integer> getArrayIndices() {
        return indices;
    }
}
