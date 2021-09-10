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
package org.evosuite.testcase.statements;

import org.apache.commons.lang3.ArrayUtils;
import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClassFactory;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * An array statement creates a new array. For example, {@code Object[] var = new Object[10]}.
 * Technically, an array definition implicitly defines a set of values of the component type of the
 * array, according to the length of the array.
 *
 * @author Gordon Fraser
 */
/*
 *  TODO: The length is currently stored in ArrayReference and the ArrayStatement.
 *  This is bound to lead to inconsistencies.
 */
public class ArrayStatement extends AbstractStatement {

    private static final long serialVersionUID = -2858236370873914156L;

    private static int[] createRandom(int dimensions) {
        int[] result = new int[dimensions];
        for (int idx = 0; idx < dimensions; idx++) {
            result[idx] = Randomness.nextInt(Properties.MAX_ARRAY);
        }
        return result;
    }

    /**
     * <p>
     * determineDimensions
     * </p>
     *
     * @param type a {@link java.lang.reflect.Type} object.
     * @return a int.
     */
    public static int determineDimensions(java.lang.reflect.Type type) {
        String name = type.toString().replace("class", "").trim();
        int count = 0;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '[') {
                count++;
            }
        }
        return count;
    }

    private int[] lengths;

    /**
     * <p>
     * Constructor for ArrayStatement.
     * </p>
     *
     * @param tc             a {@link org.evosuite.testcase.TestCase} object.
     * @param arrayReference a {@link org.evosuite.testcase.variable.ArrayReference} object.
     */
    public ArrayStatement(TestCase tc, ArrayReference arrayReference) {
        this(tc, arrayReference,
                createRandom(determineDimensions(arrayReference.getType())));
    }

    /**
     * <p>
     * Constructor for ArrayStatement.
     * </p>
     *
     * @param tc             a {@link org.evosuite.testcase.TestCase} object.
     * @param arrayReference a {@link org.evosuite.testcase.variable.ArrayReference} object.
     * @param length         an array of int.
     */
    public ArrayStatement(TestCase tc, ArrayReference arrayReference, int[] length) {
        super(tc, arrayReference);
        setLengths(length);
        arrayReference.setLengths(lengths);
    }

    /**
     * <p>
     * Constructor for ArrayStatement.
     * </p>
     *
     * @param tc   a {@link org.evosuite.testcase.TestCase} object.
     * @param type a {@link java.lang.reflect.Type} object.
     */
    public ArrayStatement(TestCase tc, java.lang.reflect.Type type) {
        this(tc, type, createRandom(determineDimensions(type)));
    }

    /**
     * <p>
     * Constructor for ArrayStatement.
     * </p>
     *
     * @param tc     a {@link org.evosuite.testcase.TestCase} object.
     * @param type   a {@link java.lang.reflect.Type} object.
     * @param length a int.
     */
    public ArrayStatement(TestCase tc, java.lang.reflect.Type type, int length) {
        this(tc, type, new int[]{length});
    }

    /**
     * <p>
     * Constructor for ArrayStatement.
     * </p>
     *
     * @param tc     a {@link org.evosuite.testcase.TestCase} object.
     * @param type   a {@link java.lang.reflect.Type} object.
     * @param length an array of int.
     */
    public ArrayStatement(TestCase tc, java.lang.reflect.Type type, int[] length) {
        this(tc, new ArrayReference(tc, GenericClassFactory.get(type), length), length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        ArrayStatement copy = new ArrayStatement(newTestCase, retval.getType(), lengths);
        // copy.assertions = copyAssertions(newTestCase, offset);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object s) {
        if (this == s)
            return true;
        if (s == null)
            return false;
        if (getClass() != s.getClass())
            return false;

        ArrayStatement as = (ArrayStatement) s;
        if (!Arrays.equals(lengths, as.lengths))
            return false;
        return retval.equals(as.retval);

        // if (!Arrays.equals(variables, other.variables))
        // return false;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable execute(Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        Throwable exceptionThrown = null;

        // Add array variable to pool
        try {
            Class<?> componentType = retval.getComponentClass();
            while (componentType.isArray())
                componentType = componentType.getComponentType();
            retval.setObject(scope, Array.newInstance(componentType, lengths));

        } catch (CodeUnderTestException e) {
            exceptionThrown = e.getCause();
        }
        return exceptionThrown;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericAccessibleObject<?> getAccessibleObject() {
        return null;
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.testcase.Statement#getUniqueVariableReferences()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VariableReference> getUniqueVariableReferences() {
        return new ArrayList<>(getVariableReferences());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<VariableReference> getVariableReferences() {
        Set<VariableReference> references = new LinkedHashSet<>();
        references.add(retval);
        return references;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = retval.hashCode();
        result = prime * result + Arrays.hashCode(lengths);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignmentStatement() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#isValid()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        int maxAssignment = 0;
        for (Statement statement : this.tc) {
            for (VariableReference var : statement.getVariableReferences()) {
                if (var.getAdditionalVariableReference() == this.retval) {
                    VariableReference currentVar = var;
                    while (currentVar instanceof FieldReference) {
                        currentVar = ((FieldReference) currentVar).getSource();
                    }
                    ArrayIndex index = (ArrayIndex) currentVar;
                    maxAssignment = Math.max(maxAssignment, index.getArrayIndex());
                }
            }
        }
        if (maxAssignment > lengths[0]) {
            logger.warn("Max assignment = " + maxAssignment + ", length = " + lengths[0]);
            return false;
        }
        return super.isValid();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.AbstractStatement#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.AbstractTestFactory)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        int maxAssignment = 0;
        for (Statement statement : test) {
            for (VariableReference var : statement.getVariableReferences()) {
                if (var.getAdditionalVariableReference() == this.retval) {
                    VariableReference currentVar = var;
                    while (currentVar instanceof FieldReference) {
                        currentVar = ((FieldReference) currentVar).getSource();
                    }
                    if (!(currentVar instanceof ArrayIndex)) {
                        LoggingUtils.getEvoLogger().error("Found assignment to array without ArrayIndex:");
                        LoggingUtils.getEvoLogger().error(test.toCode());
                        LoggingUtils.getEvoLogger().error(statement.getPosition() + ", "
                                + statement.getCode());
                    }
                    ArrayIndex index = (ArrayIndex) currentVar;
                    maxAssignment = Math.max(maxAssignment, index.getArrayIndex());
                }
            }
        }

        int dim = 0;
        if (lengths.length > 1) {
            dim = Randomness.nextInt(lengths.length - 1);
        }
        int newLength = lengths[dim];
        while (newLength == lengths[dim]) {
            if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
                newLength = Randomness.nextInt(maxAssignment,
                        Math.max(maxAssignment + 1,
                                Properties.MAX_ARRAY)) + 1;
            } else {
                int max = Math.min(Math.abs(lengths[dim] - maxAssignment - 1),
                        Properties.MAX_DELTA);
                if (max > 0)
                    newLength = lengths[dim] + Randomness.nextInt(2 * max) - max;
                else
                    newLength = lengths[dim] + Randomness.nextInt(Properties.MAX_DELTA);
            }
        }

        // TODO: Need to make sure this doesn't happen by construction
        if (newLength <= 0)
            newLength = 1;

        lengths[dim] = newLength;
        ((ArrayReference) retval).setLengths(lengths);
        return true;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(VariableReference var1, VariableReference var2) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean same(Statement s) {
        if (this == s)
            return true;
        if (s == null)
            return false;
        if (getClass() != s.getClass())
            return false;

        ArrayStatement as = (ArrayStatement) s;
        if (!Arrays.equals(lengths, as.lengths))
            return false;
        return retval.same(as.retval);
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
        System.arraycopy(lengths, 0, this.lengths, 0, lengths.length);
        ((ArrayReference) retval).setLengths(lengths);
    }

    /**
     * <p>
     * Setter for the field <code>lengths</code>.
     * </p>
     *
     * @param length an array of int.
     * @param index  an int.
     */
    public void setLength(int length, int index) {
        lengths[index] = length;
        ((ArrayReference) retval).setLength(length, index);
    }


    /**
     * <p>
     * setSize
     * </p>
     *
     * @param size a int.
     */
    public void setSize(int size) {
        /// assert lengths.length == 1;
        this.lengths[0] = size;
        ((ArrayReference) retval).setArrayLength(size);
    }

    /**
     * <p>
     * size
     * </p>
     *
     * @return a int.
     */
    public int size() {
        // assert lengths.length == 1;
        return lengths[0];
    }

    public ArrayReference getArrayReference() {
        return (ArrayReference) getReturnValue();
    }
}
