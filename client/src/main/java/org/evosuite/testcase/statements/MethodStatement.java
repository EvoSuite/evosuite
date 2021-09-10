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

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.execution.UncompilableCodeException;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericMethod;
import org.objectweb.asm.Type;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Method statements invoke methods on objects or call static methods. Value and type of a
 * method statement is defined by its return value.
 */
public class MethodStatement extends EntityWithParametersStatement {

    private static final long serialVersionUID = 6134126797102983073L;

    /**
     * The method that is being called.
     */
    protected GenericMethod method;

    /**
     * The object the method is invoked on. Set to {@code null} if the method to call is static.
     */
    protected VariableReference callee;

    /**
     * Creates a new method statement for the given test case {@code tc}, calling the supplied
     * {@code method} on the object represented by {@code callee} with the parameter list given by
     * {@code parameters}. Setting {@code callee} to {@code null} is only allowed if {@code method}
     * represents a static method.
     *
     * @param tc         the tes case for which to create the method statement
     * @param method     the method to call
     * @param callee     the object on which to call the method
     * @param parameters a list of references to the parameters to be used for the method call
     * @throws IllegalArgumentException
     */
    public MethodStatement(TestCase tc, GenericMethod method, VariableReference callee,
                           List<VariableReference> parameters) throws IllegalArgumentException {
        super(tc, method.getReturnType(), parameters,
                method.getMethod().getAnnotations(), method.getMethod().getParameterAnnotations());

        init(method, callee);
    }

    public MethodStatement(TestCase tc, GenericMethod method, VariableReference callee,
                           List<VariableReference> parameters, VariableReference retVal)
            throws IllegalArgumentException {
        this(tc, method, callee, parameters);
        this.retval = retVal;
    }

    /**
     * This constructor allows you to use an already existing VariableReference
     * as retvar. This should only be done, iff an old statement is replaced
     * with this statement. And already existing objects should in the future
     * reference this object.
     *
     * @param tc         a {@link org.evosuite.testcase.TestCase} object.
     * @param method     a {@link java.lang.reflect.Method} object.
     * @param callee     a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param retvar     a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param parameters a {@link java.util.List} object.
     */
    public MethodStatement(TestCase tc, GenericMethod method, VariableReference callee,
                           VariableReference retvar, List<VariableReference> parameters) {
        super(tc, retvar, parameters,
                method.getMethod().getAnnotations(), method.getMethod().getParameterAnnotations());

        if (retvar.getStPosition() >= tc.size()) {
            //as an old statement should be replaced by this statement
            throw new IllegalArgumentException("Cannot replace in position "
                    + retvar.getStPosition() + " when the test case has only "
                    + tc.size() + " elements");
        }

        init(method, callee);
    }

    private void init(GenericMethod method, VariableReference callee) throws IllegalArgumentException {
        if (callee == null && !method.isStatic()) {
            throw new IllegalArgumentException(
                    "A null callee cannot call a non-static method: " +
                            method.getDeclaringClass().getCanonicalName() +
                            "." + method.getName());
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter list cannot be null for method " +
                    method.getDeclaringClass().getCanonicalName() +
                    "." + method.getName());
        }
        for (VariableReference var : parameters) {
            if (var == null) {
                //recall that 'null' would be mapped to a NullReference
                throw new IllegalArgumentException(
                        "Parameter list cannot have null parameters (this is different from a NullReference)");
            }
        }
        if (method.getParameterTypes().length != parameters.size()) {
            throw new IllegalArgumentException(
                    "Parameters list mismatch from the types declared in the method: "
                            + method.getParameterTypes().length + " != "
                            + parameters.size());
        }

        this.method = method;
        if (isStatic())
            this.callee = null;
        else
            this.callee = callee;
    }

    /**
     * <p>
     * Getter for the field <code>method</code>.
     * </p>
     *
     * @return a {@link java.lang.reflect.Method} object.
     */
    public GenericMethod getMethod() {
        return method;
    }

    /**
     * <p>
     * Setter for the field <code>method</code>.
     * </p>
     *
     * @param method a {@link java.lang.reflect.Method} object.
     */
    public void setMethod(GenericMethod method) {
        this.method = method;
    }

    /**
     * <p>
     * Getter for the field <code>callee</code>.
     * </p>
     *
     * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public VariableReference getCallee() {
        return callee;
    }

    /**
     * <p>
     * Setter for the field <code>callee</code>.
     * </p>
     *
     * @param callee a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public void setCallee(VariableReference callee) {
        if (!isStatic())
            this.callee = callee;
    }

    /**
     * <p>
     * isStatic
     * </p>
     *
     * @return a boolean.
     */
    public boolean isStatic() {
        return method.isStatic();
    }

    private boolean isInstanceMethod() {
        return !method.isStatic();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable execute(final Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        logger.trace("Executing method " + method.getName());
        final Object[] inputs = new Object[parameters.size()];
        Throwable exceptionThrown = null;

        try {
            return super.exceptionHandler(new Executer() {

                @Override
                public void execute() throws InvocationTargetException,
                        IllegalArgumentException, IllegalAccessException,
                        InstantiationException, CodeUnderTestException {
                    Object callee_object;
                    try {
                        java.lang.reflect.Type[] parameterTypes = method.getParameterTypes();
                        for (int i = 0; i < parameters.size(); i++) {
                            VariableReference parameterVar = parameters.get(i);
                            inputs[i] = parameterVar.getObject(scope);
                            if (inputs[i] == null && method.getMethod().getParameterTypes()[i].isPrimitive()) {
                                throw new CodeUnderTestException(new NullPointerException());
                            }
                            if (inputs[i] != null && !TypeUtils.isAssignable(inputs[i].getClass(), parameterTypes[i])) {
                                // TODO: This used to be a check of the declared type, but the problem is that
                                //       Generic types are not updated during execution, so this may fail:
                                //!parameterVar.isAssignableTo(parameterTypes[i])) {
                                throw new CodeUnderTestException(
                                        new UncompilableCodeException("Cannot assign " + parameterVar.getVariableClass().getName() + " to " + parameterTypes[i]));
                            }
                        }

                        callee_object = method.isStatic() ? null
                                : callee.getObject(scope);
                        if (!method.isStatic() && callee_object == null) {
                            throw new CodeUnderTestException(new NullPointerException());
                        }
                    } catch (CodeUnderTestException e) {
                        throw e;
                        // throw CodeUnderTestException.throwException(e.getCause());
                    } catch (Throwable e) {
                        e.printStackTrace();
                        throw new EvosuiteError(e);
                    }

                    Object ret = method.getMethod().invoke(callee_object, inputs);
                    // Try exact return type
                    /*
                     * TODO: Sometimes we do want to cast an Object to String etc...
                     */
                    if (method.getReturnType() instanceof Class<?>) {
                        Class<?> returnClass = (Class<?>) method.getReturnType();

                        if (!returnClass.isPrimitive()
                                && ret != null
                                && !returnClass.isAssignableFrom(ret.getClass())) {
                            throw new CodeUnderTestException(new ClassCastException(
                                    "Cannot assign " + method.getReturnType()
                                            + " to variable of type " + retval.getType()));
                        }
                    }


                    try {
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
            logger.debug("Exception thrown in method {}: {}", method.getName(),
                    exceptionThrown);
        }
        return exceptionThrown;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeclaredException(Throwable t) {
        if (t == null)
            return false;

        for (Class<?> declaredException : method.getMethod().getExceptionTypes()) {
            if (declaredException.isAssignableFrom(t.getClass()))
                return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        ArrayList<VariableReference> newParams = new ArrayList<>();
        for (VariableReference r : parameters) {
            newParams.add(r.copy(newTestCase, offset));
        }

        MethodStatement m;
        if (isStatic()) {
            // FIXXME: If callee is an array index, this will return an invalid
            // copy of the cloned variable!
            m = new MethodStatement(newTestCase, method.copy(), null, newParams);
        } else {
            VariableReference newCallee = callee.copy(newTestCase, offset);
            m = new MethodStatement(newTestCase, method.copy(), newCallee, newParams);

        }
        if (retval instanceof ArrayReference
                && !(m.getReturnValue() instanceof ArrayReference)) {
            // logger.info("Copying array retval: " + retval.getGenericClass());
            //	assert (retval.getGenericClass() != null);
            //	assert (retval.getGenericClass().isArray()) : method.toString();
            ArrayReference newRetVal = new ArrayReference(newTestCase,
                    retval.getGenericClass(), ((ArrayReference) retval).getArrayLength());
            m.setRetval(newRetVal);

        }
        m.getReturnValue().setType(retval.getType()); // Actual type may have changed, e.g. subtype

        // m.assertions = copyAssertions(newTestCase, offset);

        return m;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<VariableReference> getVariableReferences() {
        Set<VariableReference> references = super.getVariableReferences();

        if (isInstanceMethod()) {
            references.add(callee);
            if (callee.getAdditionalVariableReference() != null)
                references.add(callee.getAdditionalVariableReference());
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
    public void replace(VariableReference oldVar, VariableReference newVar) {
        super.replace(oldVar, newVar);

        if (isInstanceMethod()) {
            if (callee.equals(oldVar))
                callee = newVar;
            else
                callee.replaceAdditionalVariableReference(oldVar, newVar);
        }
    }


    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#getNumParameters()
     */
    @Override
    public int getNumParameters() {
        return parameters.size() + (isStatic() ? 0 : 1);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return method.getName() + Type.getMethodDescriptor(method.getMethod());
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

        MethodStatement ms = (MethodStatement) s;
        if (ms.parameters.size() != parameters.size())
            return false;

        if (!this.method.equals(ms.method))
            return false;

        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).equals(ms.parameters.get(i)))
                return false;
        }

        if (!retval.equals(ms.retval))
            return false;

        if ((callee == null && ms.callee != null)
                || (callee != null && ms.callee == null)) {
            return false;
        } else {
            if (callee == null)
                return true;
            else
                return (callee.equals(ms.callee));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callee == null) ? 0 : callee.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.Statement#getDeclaredExceptions()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<?>> getDeclaredExceptions() {
        Set<Class<?>> ex = super.getDeclaredExceptions();
        ex.addAll(Arrays.asList(method.getMethod().getExceptionTypes()));
        return ex;
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
        List<VariableReference> references = super.getUniqueVariableReferences();

        if (isInstanceMethod()) {
            references.add(callee);
            if (callee instanceof ArrayIndex)
                references.add(((ArrayIndex) callee).getArray());
        }

        return references;
    }

    @Override
    public boolean isAccessible() {
        if (!method.isAccessible())
            return false;

        return super.isAccessible();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#isValid()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        assert (super.isValid());
        for (VariableReference v : parameters) {
            v.getStPosition();
        }
        if (!isStatic()) {
            callee.getStPosition();
        }
        return true;
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

        MethodStatement ms = (MethodStatement) s;
        if (ms.parameters.size() != parameters.size())
            return false;

        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).same(ms.parameters.get(i)))
                return false;
        }

        if (!this.method.equals(ms.method))
            return false;

        if (!retval.same(ms.retval))
            return false;

        if ((callee == null && ms.callee != null)
                || (callee != null && ms.callee == null)) {
            return false;
        } else {
            if (callee == null)
                return true;
            else
                return (callee.same(ms.callee));
        }
    }

    /**
     * Go through parameters of method call and apply local search
     *
     * @param test
     * @param factory
     */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {

        if (Randomness.nextDouble() >= Properties.P_CHANGE_PARAMETER)
            return false;

        List<VariableReference> parameters = getParameterReferences();

        boolean changed = false;
        int max = parameters.size();
        if (!isStatic()) {
            max++;
        }

        if (max == 0)
            return false; // Static method with no parameters...

        double pParam = 1.0 / max;
        if (!isStatic() && Randomness.nextDouble() < pParam) {
            // replace callee
            VariableReference callee = getCallee();
            List<VariableReference> objects = test.getObjects(callee.getType(),
                    getPosition());
            objects.remove(callee);
            objects = objects.stream().filter(var -> !(test.getStatement(var.getStPosition()) instanceof FunctionalMockStatement))
                    .collect(Collectors.toList());

            if (!objects.isEmpty()) {
                VariableReference replacement = Randomness.choice(objects);
                setCallee(replacement);
                changed = true;
            }
        }

        for (int numParameter = 0; numParameter < parameters.size(); numParameter++) {
            if (Randomness.nextDouble() < pParam) {
                if (mutateParameter(test, numParameter))
                    changed = true;
            }
        }
        return changed;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public GenericMethod getAccessibleObject() {
        return method;
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
        method.changeClassLoader(loader);
        super.changeClassLoader(loader);
    }

    @Override
    public String getDescriptor() {
        return method.getDescriptor();
    }

    @Override
    public String getDeclaringClassName() {
        return method.getDeclaringClass().getCanonicalName();
    }

    @Override
    public String getMethodName() {
        return method.getName();
    }
}
