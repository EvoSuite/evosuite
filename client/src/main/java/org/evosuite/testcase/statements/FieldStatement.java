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

import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericField;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A statement that can access public member variables of objects. For example, the Java statement
 * <pre><code>
 * int var2 = var1.size;
 * </code></pre>
 * fits into this category, since the statement accesses the public member {@code size} of the
 * object referenced by {@code var1}. Value and type of a field statement are defined by the
 * member variable. In the given example, the type of the member variable {@code size} is {@code
 * int}, hence {@code var2} must also be an {@code int}.
 *
 * @author Gordon Fraser
 */
public class FieldStatement extends AbstractStatement {

    private static final long serialVersionUID = -4944610139232763790L;

    protected GenericField field;
    protected VariableReference source;

    /**
     * <p>
     * Constructor for FieldStatement.
     * </p>
     *
     * @param tc     a {@link org.evosuite.testcase.TestCase} object.
     * @param field  a {@link java.lang.reflect.Field} object.
     * @param source a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param type   a {@link java.lang.reflect.Type} object.
     */
    public FieldStatement(TestCase tc, GenericField field, VariableReference source) {
        super(tc, new VariableReferenceImpl(tc, field.getFieldType()));
        this.field = field;
        this.source = source;
        if (retval.getComponentType() != null) {
            retval = new ArrayReference(tc, retval.getGenericClass(), 0);
        }
    }

    /**
     * This constructor allows you to use an already existing VariableReference
     * as retvar. This should only be done, iff an old statement is replaced
     * with this statement. And already existing objects should in the future
     * reference this object.
     *
     * @param tc      a {@link org.evosuite.testcase.TestCase} object.
     * @param field   a {@link java.lang.reflect.Field} object.
     * @param source  a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param ret_var a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public FieldStatement(TestCase tc, GenericField field, VariableReference source,
                          VariableReference ret_var) {
        super(tc, ret_var);
        assert (tc.size() > ret_var.getStPosition()); //as an old statement should be replaced by this statement
        this.field = field;
        this.source = source;
    }

    /**
     * <p>
     * Getter for the field <code>source</code>.
     * </p>
     *
     * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public VariableReference getSource() {
        return source;
    }

    /**
     * Try to replace source of field with all possible choices
     *
     * @param test
     * @param statement
     * @param objective
     */
    /* (non-Javadoc)
     * @see org.evosuite.testcase.AbstractStatement#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.TestFactory)
     */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {

        if (Randomness.nextDouble() >= Properties.P_CHANGE_PARAMETER)
            return false;

        if (!isStatic()) {
            VariableReference source = getSource();
            List<VariableReference> objects = test.getObjects(source.getType(),
                    getPosition());
            objects.remove(source);

            if (!objects.isEmpty()) {
                setSource(Randomness.choice(objects));
                return true;
            }
        }

        return false;
    }

    /**
     * <p>
     * Setter for the field <code>source</code>.
     * </p>
     *
     * @param source a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public void setSource(VariableReference source) {
        this.source = source;
    }

    @Override
    public boolean isAccessible() {
        if (!field.isAccessible())
            return false;

        return super.isAccessible();
    }

    /**
     * <p>
     * isStatic
     * </p>
     *
     * @return a boolean.
     */
    public boolean isStatic() {
        return field.isStatic();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#getNumParameters()
     */
    @Override
    public int getNumParameters() {
        if (isStatic())
            return 0;
        else
            return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        if (field.isStatic()) {
            FieldStatement s = new FieldStatement(newTestCase, field.copy(), null);
            s.getReturnValue().setType(retval.getType()); // Actual type may have changed, e.g. subtype
            // s.assertions = copyAssertions(newTestCase, offset);
            return s;
        } else {
            VariableReference newSource = source.copy(newTestCase, offset);
            FieldStatement s = new FieldStatement(newTestCase, field.copy(), newSource);
            s.getReturnValue().setType(retval.getType()); // Actual type may have changed, e.g. subtype
            // s.assertions = copyAssertions(newTestCase, offset);
            return s;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable execute(final Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        Throwable exceptionThrown = null;

        try {
            return super.exceptionHandler(new Executer() {

                @Override
                public void execute() throws InvocationTargetException,
                        IllegalArgumentException, IllegalAccessException,
                        InstantiationException, CodeUnderTestException {
                    Object source_object;
                    try {
                        source_object = (field.isStatic()) ? null
                                : source.getObject(scope);

                        if (!field.isStatic() && source_object == null) {
                            retval.setObject(scope, null);
                            throw new CodeUnderTestException(new NullPointerException());
                        }
                        //} catch (CodeUnderTestException e) {
                        //	throw CodeUnderTestException.throwException(e.getCause());
                    } catch (CodeUnderTestException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new EvosuiteError(e);
                    }

                    Object ret = field.getField().get(source_object);
                    if (ret != null && !retval.isAssignableFrom(ret.getClass())) {
                        throw new CodeUnderTestException(new ClassCastException());
                    }
                    try {
                        // FIXXME: isAssignableFrom int <- Integer does not return true
                        //assert(ret==null || retval.getVariableClass().isAssignableFrom(ret.getClass())) : "we want an " + retval.getVariableClass() + " but got an " + ret.getClass();
                        retval.setObject(scope, ret);
                    } catch (CodeUnderTestException e) {
                        throw e;
                        // throw CodeUnderTestException.throwException(e);
                    } catch (Throwable e) {
                        throw new EvosuiteError(e);
                    }
                }

                @Override
                public Set<Class<? extends Throwable>> throwableExceptions() {
                    Set<Class<? extends Throwable>> t = new LinkedHashSet<>();
                    t.add(InvocationTargetException.class);
                    return t;
                }
            });

        } catch (InvocationTargetException e) {
            exceptionThrown = e.getCause();
        }
        return exceptionThrown;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<VariableReference> getVariableReferences() {
        Set<VariableReference> references = new LinkedHashSet<>();
        references.add(retval);
        if (!isStatic()) {
            references.add(source);
            if (source.getAdditionalVariableReference() != null)
                references.add(source.getAdditionalVariableReference());
        }
        return references;

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(VariableReference var1, VariableReference var2) {
        if (!field.isStatic()) {
            if (source.equals(var1))
                source = var2;
            else
                source.replaceAdditionalVariableReference(var1, var2);
        }
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

        FieldStatement fs = (FieldStatement) s;
        if (!field.isStatic())
            return source.equals(fs.source) && retval.equals(fs.retval)
                    && field.equals(fs.field);
        else
            return retval.equals(fs.retval) && field.equals(fs.field);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 51;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    /**
     * <p>
     * Getter for the field <code>field</code>.
     * </p>
     *
     * @return a {@link java.lang.reflect.Field} object.
     */
    public GenericField getField() {
        return field;
    }

    /**
     * <p>
     * Setter for the field <code>field</code>.
     * </p>
     *
     * @param field a {@link java.lang.reflect.Field} object.
     */
    public void setField(GenericField field) {
        // assert (this.field.getType().equals(field.getType()));
        this.field = field;
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
    public boolean same(Statement s) {
        if (this == s)
            return true;
        if (s == null)
            return false;
        if (getClass() != s.getClass())
            return false;

        FieldStatement fs = (FieldStatement) s;
        if (!field.isStatic())
            return source.same(fs.source) && retval.same(fs.retval)
                    && field.equals(fs.field);
        else
            return retval.same(fs.retval) && field.equals(fs.field);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericField getAccessibleObject() {
        return field;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignmentStatement() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#changeClassLoader(java.lang.ClassLoader)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeClassLoader(ClassLoader loader) {
        field.changeClassLoader(loader);
        super.changeClassLoader(loader);
    }
}
