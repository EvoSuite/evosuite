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

import org.apache.commons.lang3.ArrayUtils;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Gordon Fraser
 */

/*
 *  TODO: The length is currently stored in ArrayReference and the ArrayStatement.
 *  This is bound to lead to inconsistencies.
 */
public class ArrayReference extends VariableReferenceImpl {

    private static final long serialVersionUID = 3309591356542131910L;

    private int[] lengths;

    /**
     * <p>
     * Constructor for ArrayReference.
     * </p>
     *
     * @param tc    a {@link org.evosuite.testcase.TestCase} object.
     * @param clazz a {@link java.lang.Class} object.
     */
    public ArrayReference(TestCase tc, Class<?> clazz) {
        this(tc, GenericClassFactory.get(clazz),
                new int[ArrayStatement.determineDimensions(clazz)]);
    }

    /**
     * <p>
     * Constructor for ArrayReference.
     * </p>
     *
     * @param tc      a {@link org.evosuite.testcase.TestCase} object.
     * @param clazz   a {@link GenericClass} object.
     * @param lengths an array of int.
     */
    public ArrayReference(TestCase tc, GenericClass<?> clazz, int[] lengths) {
        super(tc, clazz);
        assert (lengths.length > 0);
        // this.lengths = lengths;
        setLengths(lengths);
    }

    /**
     * <p>
     * Constructor for ArrayReference.
     * </p>
     *
     * @param tc           a {@link org.evosuite.testcase.TestCase} object.
     * @param clazz        a {@link GenericClass} object.
     * @param array_length a int.
     */
    public ArrayReference(TestCase tc, GenericClass<?> clazz, int array_length) {
        this(tc, clazz, new int[]{array_length});
    }

    /**
     * <p>
     * getArrayLength
     * </p>
     *
     * @return a int.
     */
    public int getArrayLength() {
        // assert lengths.length == 1;
        return lengths[0];
    }

    /**
     * <p>
     * setArrayLength
     * </p>
     *
     * @param l a int.
     */
    public void setArrayLength(int l) {
        assert (l >= 0);
        // assert lengths.length == 1;
        lengths[0] = l;
    }

    /**
     * <p>
     * Getter for the field <code>lengths</code>.
     * </p>
     *
     * @return an array of int.
     */
    public List<Integer> getLengths() {
        return Arrays.asList(ArrayUtils.toObject(lengths));
    }

    /**
     * <p>
     * Setter for the field <code>lengths</code>.
     * </p>
     *
     * @param lengths an array of int.
     */
    public void setLengths(int[] lengths) {
        this.lengths = new int[lengths.length];
        for (int i = 0; i < lengths.length; i++)
            this.lengths[i] = lengths[i];
    }

    /**
     * <p>
     * Setter for an element of the field <code>lengths</code>.
     * </p>
     *
     * @param length an int
     * @param index  an int
     */
    public void setLength(int length, int index) {
        this.lengths[index] = length;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Create a copy of the current variable
     */
    @Override
    public VariableReference copy(TestCase newTestCase, int offset) {
        VariableReference newRef = newTestCase.getStatement(getStPosition() + offset).getReturnValue();
        if (newRef instanceof ArrayReference) {
            ArrayReference otherArray = (ArrayReference) newRef;
            otherArray.setLengths(lengths);
            return otherArray;
        } else {

            // FIXXME: This part should be redundant

            if (newRef.getComponentType() != null) {
                ArrayReference otherArray = new ArrayReference(newTestCase, type, lengths);
                newTestCase.getStatement(getStPosition() + offset).setRetval(otherArray);
                return otherArray;
            } else {
                // This may happen when cloning a method statement which returns an Object that in fact is an array
                // We'll just create a new ArrayReference in this case.
                ArrayReference otherArray = new ArrayReference(newTestCase, type, lengths);
                newTestCase.getStatement(getStPosition() + offset).setRetval(otherArray);
                return otherArray;
                //				throw new RuntimeException("After cloning the array disappeared: "
                //				        + getName() + "/" + newRef.getName() + " in test "
                //				        + newTestCase.toCode() + " / old test: " + testCase.toCode());
            }
        }
    }

    /**
     * <p>
     * getArrayDimensions
     * </p>
     *
     * @return a int.
     */
    public int getArrayDimensions() {
        return lengths.length;
    }

    /**
     * <p>
     * Setter for the field <code>lengths</code>.
     * </p>
     *
     * @param lengths a {@link java.util.List} object.
     */
    public void setLengths(List<Integer> lengths) {
        this.lengths = new int[lengths.size()];
        int idx = 0;
        for (Integer length : lengths) {
            this.lengths[idx] = length;
            idx++;
        }
    }

    public int getMaximumIndex() {
        int max = 0;

        for (Statement s : testCase) {
            for (VariableReference var : s.getVariableReferences()) {
                if (var instanceof ArrayIndex) {
                    ArrayIndex index = (ArrayIndex) var;
                    if (index.getArray().equals(this)) {
                        max = Math.max(max, index.getArrayIndex());
                    }
                }
            }
        }
        return max;
    }

    public boolean isInitialized(int index) {
        return isInitialized(index, testCase.size());
    }

    public boolean isInitialized(int index, int position) {
        int pos = 0;
        for (Statement s : testCase) {
            if (pos++ >= position)
                return false;

            if (s instanceof AssignmentStatement) {
                VariableReference ret = s.getReturnValue();
                if (ret instanceof ArrayIndex) {
                    ArrayIndex ai = (ArrayIndex) ret;
                    if (ai.getArray().equals(this) && ai.getArrayIndex() == index)
                        return true;
                }
            }
        }
        return false;
    }
}
