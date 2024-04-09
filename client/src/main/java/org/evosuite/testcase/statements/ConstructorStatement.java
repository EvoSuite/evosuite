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

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.dse.VM;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.execution.UncompilableCodeException;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericConstructor;
import org.objectweb.asm.Type;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This statement represents a constructor call, generating a new instance of any given class, e.g.,
 * {@code Stack stack = new Stack()}. Value and type of the of the statement are defined by the
 * object constructed in the call.
 *
 * @author Gordon Fraser
 */
public class ConstructorStatement extends EntityWithParametersStatement {

    private static final long serialVersionUID = -3035570485633271957L;

    private GenericConstructor constructor;

    private static final List<String> primitiveClasses = Arrays.asList("char", "int", "short",
            "long", "boolean",
            "float", "double",
            "byte");

    /**
     * <p>
     * Constructor for ConstructorStatement.
     * </p>
     *
     * @param tc          a {@link org.evosuite.testcase.TestCase} object.
     * @param constructor a {@link java.lang.reflect.Constructor} object.
     * @param parameters  a {@link java.util.List} object.
     */
    public ConstructorStatement(TestCase tc, GenericConstructor constructor,
                                List<VariableReference> parameters) {
        super(tc, new VariableReferenceImpl(tc, constructor.getOwnerClass()), parameters,
                constructor.getConstructor().getAnnotations(), constructor.getConstructor().getParameterAnnotations());
        this.constructor = constructor;
    }

    /**
     * This constructor allows you to use an already existing VariableReference
     * as retvar. This should only be done, iff an old statement is replaced
     * with this statement. And already existing objects should in the future
     * reference this object.
     *
     * @param tc          a {@link org.evosuite.testcase.TestCase} object.
     * @param constructor a {@link java.lang.reflect.Constructor} object.
     * @param retvar      a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param parameters  a {@link java.util.List} object.
     */
    public ConstructorStatement(TestCase tc, GenericConstructor constructor,
                                VariableReference retvar, List<VariableReference> parameters) {
        super(tc, retvar, parameters,
                constructor.getConstructor().getAnnotations(), constructor.getConstructor().getParameterAnnotations());
        assert (tc.size() > retvar.getStPosition()); //as an old statement should be replaced by this statement
        this.constructor = constructor;
    }

    /**
     * <p>
     * Constructor for ConstructorStatement.
     * </p>
     *
     * @param tc          a {@link org.evosuite.testcase.TestCase} object.
     * @param constructor a {@link java.lang.reflect.Constructor} object.
     * @param retvar      a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param parameters  a {@link java.util.List} object.
     * @param check       a boolean.
     */
    protected ConstructorStatement(TestCase tc, GenericConstructor constructor,
                                   VariableReference retvar, List<VariableReference> parameters, boolean check) {
        super(tc, retvar, parameters,
                constructor.getConstructor().getAnnotations(), constructor.getConstructor().getParameterAnnotations());
        assert !check;
        this.constructor = constructor;
    }

    /**
     * <p>
     * Getter for the field <code>constructor</code>.
     * </p>
     *
     * @return a {@link java.lang.reflect.Constructor} object.
     */
    public GenericConstructor getConstructor() {
        return constructor;
    }

    /**
     * <p>
     * Setter for the field <code>constructor</code>.
     * </p>
     *
     * @param constructor a {@link java.lang.reflect.Constructor} object.
     */
    public void setConstructor(GenericConstructor constructor) {
        this.constructor = constructor;
        retval.setType(constructor.getReturnType());
    }

    /**
     * <p>
     * getReturnType
     * </p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getReturnType(Class<?> clazz) {
        String retVal = ClassUtils.getShortClassName(clazz);
        if (primitiveClasses.contains(retVal))
            return clazz.getSimpleName();

        return retVal;
    }

    // TODO: Handle inner classes (need instance parameter for newInstance)

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable execute(final Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            InstantiationException, IllegalAccessException {
        //PrintStream old_out = System.out;
        //PrintStream old_err = System.err;
        //System.setOut(out);
        //System.setErr(out);

        logger.trace("Executing constructor " + constructor.toString());
        final Object[] inputs = new Object[parameters.size()];
        Throwable exceptionThrown = null;

        try {
            return super.exceptionHandler(new Executer() {

                @Override
                public void execute() throws InvocationTargetException,
                        IllegalArgumentException, IllegalAccessException,
                        InstantiationException, CodeUnderTestException {

                    java.lang.reflect.Type[] parameterTypes = constructor.getParameterTypes();
                    for (int i = 0; i < parameters.size(); i++) {
                        VariableReference parameterVar = parameters.get(i);
                        try {
                            inputs[i] = parameterVar.getObject(scope);
                        } catch (CodeUnderTestException e) {
                            throw e;
                            //throw new CodeUnderTestException(e.getCause());
                            // throw CodeUnderTestException.throwException(e.getCause());
                        } catch (Throwable e) {
                            //FIXME: this does not seem to propagate to client root. Is this normal behavior?
                            logger.error("Class " + Properties.TARGET_CLASS
                                    + ". Error encountered: " + e);
                            assert (false);
                            throw new EvosuiteError(e);
                        }
                        if (inputs[i] != null && !TypeUtils.isAssignable(inputs[i].getClass(), parameterTypes[i])) {
                            // TODO: This used to be a check of the declared type, but the problem is that
                            //       Generic types are not updated during execution, so this may fail:
                            //!parameterVar.isAssignableTo(parameterTypes[i])) {
                            throw new CodeUnderTestException(
                                    new UncompilableCodeException("Cannot assign " + parameterVar.getVariableClass().getName() + " to " + parameterTypes[i]));
                        }
                        if (inputs[i] == null && constructor.getConstructor().getParameterTypes()[i].isPrimitive()) {
                            throw new CodeUnderTestException(new NullPointerException());
                        }

                    }

                    // If this is a non-static member class, the first parameter must not be null
                    if (constructor.getConstructor().getDeclaringClass().isMemberClass()
                            && !Modifier.isStatic(constructor.getConstructor().getDeclaringClass().getModifiers())) {
                        if (inputs[0] == null) {
                            // throw new NullPointerException();
                            throw new CodeUnderTestException(new NullPointerException());
                        }
                    }

                    Object ret = constructor.getConstructor().newInstance(inputs);

                    try {
                        // assert(retval.getVariableClass().isAssignableFrom(ret.getClass())) :"we want an " + retval.getVariableClass() + " but got an " + ret.getClass();
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
            VM.disableCallBacks();
            exceptionThrown = e.getCause();
            if (logger.isDebugEnabled()) {
                try {
                    logger.debug("Exception thrown in constructor: " + e.getCause());
                }
                //this can happen if SUT throws exception on toString
                catch (Exception ex) {
                    logger.debug("Exception thrown in constructor and SUT gives issue when calling e.getCause()",
                            ex);
                }
            }
        }
        return exceptionThrown;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        ArrayList<VariableReference> new_params = new ArrayList<>();
        for (VariableReference r : parameters) {
            new_params.add(r.copy(newTestCase, offset));
        }

        AbstractStatement copy = new ConstructorStatement(newTestCase,
                constructor.copy(), new_params);
        // copy.assertions = copyAssertions(newTestCase, offset);

        return copy;
    }


    /**
     * <p>
     * getParameterReferences
     * </p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<VariableReference> getParameterReferences() {
        return parameters;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#getNumParameters()
     */
    @Override
    public int getNumParameters() {
        return parameters.size();
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

        ConstructorStatement ms = (ConstructorStatement) s;
        if (ms.parameters.size() != parameters.size())
            return false;

        if (!this.constructor.equals(ms.constructor))
            return false;

        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).equals(ms.parameters.get(i)))
                return false;
        }

        return retval.equals(ms.retval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 41;
        int result = 1;
        result = prime * result + ((constructor == null) ? 0 : constructor.hashCode());
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
        ex.addAll(Arrays.asList(constructor.getConstructor().getExceptionTypes()));
        return ex;
    }


    /**
     * Go through parameters of constructor call and apply local search
     *
     * @param test
     * @param factory
     */
    /* (non-Javadoc)
     * @see org.evosuite.testcase.AbstractStatement#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.TestFactory)
     */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {

        if (Randomness.nextDouble() >= Properties.P_CHANGE_PARAMETER)
            return false;

        List<VariableReference> parameters = getParameterReferences();
        if (parameters.isEmpty())
            return false;
        double pParam = 1.0 / parameters.size();
        boolean changed = false;
        for (int numParameter = 0; numParameter < parameters.size(); numParameter++) {
            if (Randomness.nextDouble() < pParam) {
                if (mutateParameter(test, numParameter))
                    changed = true;
            }
        }
        return changed;
    }


    @Override
    public boolean isAccessible() {
        if (!constructor.isAccessible())
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

        ConstructorStatement ms = (ConstructorStatement) s;
        if (ms.parameters.size() != parameters.size())
            return false;

        if (!this.constructor.equals(ms.constructor))
            return false;

        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).same(ms.parameters.get(i)))
                return false;
        }

        return retval.same(ms.retval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericConstructor getAccessibleObject() {
        return constructor;
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
        constructor.changeClassLoader(loader);
        super.changeClassLoader(loader);
    }

    @Override
    public String toString() {
        return constructor.getName() + Type.getConstructorDescriptor(constructor.getConstructor());
    }

    @Override
    public String getDescriptor() {
        return constructor.getDescriptor();
    }

    @Override
    public String getDeclaringClassName() {
        return constructor.getDeclaringClass().getCanonicalName();
    }

    @Override
    public String getMethodName() {
        return "<init>";
    }
}
