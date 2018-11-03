/**
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

import com.googlecode.gentyref.GenericTypeReflector;

import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.testcase.statements.environment.EnvironmentStatements;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.numeric.*;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.Randomness;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;

/**
 * Statement assigning a primitive numeric value
 *
 * @param <T>
 * @author Gordon Fraser
 */
public abstract class PrimitiveStatement<T> extends AbstractStatement {

    private static final long serialVersionUID = -7721106626421922833L;

    /**
     * The value
     */
    protected transient T value;

    /**
     * <p>
     * Constructor for PrimitiveStatement.
     * </p>
     *
     * @param tc     a {@link org.evosuite.testcase.TestCase} object.
     * @param varRef a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param value  a T object.
     */
    public PrimitiveStatement(TestCase tc, VariableReference varRef, T value) {
        super(tc, varRef);
        this.value = value;
    }

    /**
     * Constructor
     *
     * @param value a T object.
     * @param tc    a {@link org.evosuite.testcase.TestCase} object.
     * @param type  a {@link java.lang.reflect.Type} object.
     */
    public PrimitiveStatement(TestCase tc, Type type, T value) {
        super(tc, new VariableReferenceImpl(tc, type));
        this.value = value;
    }

    public PrimitiveStatement(TestCase tc, GenericClass clazz, T value) {
        super(tc, new VariableReferenceImpl(tc, clazz));
        this.value = value;
    }

    /**
     * Access the value
     *
     * @return a T object.
     */
    public T getValue() {
        return value;
    }

    /**
     * Set the value
     *
     * @param val a T object.
     */
    public void setValue(T val) {
        this.value = val;
    }

    public boolean hasMoreThanOneValue() {
        return true;
    }

    public static PrimitiveStatement<?> getPrimitiveStatement(TestCase tc, Class<?> clazz) {
        return getPrimitiveStatement(tc, new GenericClass(clazz));
    }

    /**
     * Generate a primitive statement for given type initialized with default
     * value (0)
     *
     * @param tc    a {@link org.evosuite.testcase.TestCase} object.
     * @param genericClass a {@link java.lang.reflect.Type} object.
     * @return a {@link org.evosuite.testcase.statements.PrimitiveStatement} object.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static PrimitiveStatement<?> getPrimitiveStatement(TestCase tc,
                                                              GenericClass genericClass) {
        // TODO This kills the benefit of inheritance.
        // Let each class implement the clone method instead

        Class<?> clazz = genericClass.getRawClass();
        PrimitiveStatement<?> statement;

        if (clazz == boolean.class) {
            statement = new BooleanPrimitiveStatement(tc);
        } else if (clazz == int.class) {
            statement = new IntPrimitiveStatement(tc);
        } else if (clazz == char.class) {
            statement = new CharPrimitiveStatement(tc);
        } else if (clazz == long.class) {
            statement = new LongPrimitiveStatement(tc);
        } else if (clazz.equals(double.class)) {
            statement = new DoublePrimitiveStatement(tc);
        } else if (clazz == float.class) {
            statement = new FloatPrimitiveStatement(tc);
        } else if (clazz == short.class) {
            statement = new ShortPrimitiveStatement(tc);
        } else if (clazz == byte.class) {
            statement = new BytePrimitiveStatement(tc);
        } else if (clazz.equals(String.class)) {
            statement = new StringPrimitiveStatement(tc);
        } else if (GenericTypeReflector.erase(clazz).isEnum()) {
            statement = new EnumPrimitiveStatement(tc, GenericTypeReflector.erase(clazz));
        } else if (EnvironmentStatements.isEnvironmentData(clazz)) {
            statement = EnvironmentStatements.getStatement(clazz, tc);
        } else if (clazz == Class.class) {
            final List<Type> types = genericClass.getParameterTypes();

            Type typeParameter = null;
            if (!types.isEmpty()) {
                typeParameter = types.get(0);
                logger.debug("Creating class primitive with value " + typeParameter);
                if (typeParameter instanceof WildcardType) {
                    statement = new ClassPrimitiveStatement(
                            tc,
                            GenericTypeReflector.erase(((WildcardType) typeParameter).getUpperBounds()[0]));
                } else {
                    statement = new ClassPrimitiveStatement(tc,
                            GenericTypeReflector.erase(typeParameter));
                }
            } else {
                logger.debug("Creating class primitive with random value / "
                        + genericClass);
                statement = new ClassPrimitiveStatement(tc);
            }
            /*
						if (genericClass.hasWildcardTypes()) {
							Class<?> bound = GenericTypeReflector.erase(TypeUtils.getImplicitUpperBounds((WildcardType) typeParameter)[0]);
							if (!bound.equals(Object.class)) {
								Set<Class<?>> assignableClasses = TestClusterGenerator.getConcreteClasses(bound,
								                                                                          DependencyAnalysis.getInheritanceTree());
								statement = new ClassPrimitiveStatement(tc, genericClass,
								        assignableClasses);
							} else {
								statement = new ClassPrimitiveStatement(tc);
							}
						} else {
						*/
			/*
			if (typeParameter instanceof Class<?>) {
				logger.debug("Creating class primitive with value " + typeParameter);
				statement = new ClassPrimitiveStatement(tc, (Class<?>) typeParameter);
			} else {
				logger.debug("Creating class primitive with random value / "
				        + typeParameter);
				statement = new ClassPrimitiveStatement(tc);
			}
			*/
            //}
        } else {
            throw new RuntimeException("Getting unknown type: " + clazz + " / "
                    + clazz.getClass());
        }
        return statement;
    }

    /**
     * Create random primitive statement
     *
     * @param tc       a {@link org.evosuite.testcase.TestCase} object.
     * @param clazz    a {@link java.lang.reflect.Type} object.
     * @param position an integer.
     * @return a {@link org.evosuite.testcase.statements.PrimitiveStatement} object.
     */
    public static PrimitiveStatement<?> getRandomStatement(TestCase tc,
                                                           GenericClass clazz, int position) {

        PrimitiveStatement<?> statement = getPrimitiveStatement(tc, clazz);
        statement.randomize();
        return statement;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        @SuppressWarnings("unchecked")
        PrimitiveStatement<T> clone = (PrimitiveStatement<T>) getPrimitiveStatement(newTestCase,
                retval.getGenericClass());
        clone.setValue(value);
        // clone.assertions = copyAssertions(newTestCase, offset);
        return clone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable execute(Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        Throwable exceptionThrown = null;

        try {
            retval.setObject(scope, value);
        } catch (CodeUnderTestException e) {
            exceptionThrown = e;
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
        return references;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(VariableReference var1, VariableReference var2) {
        if (retval.equals(var1)) {
            retval = var2;
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

        PrimitiveStatement<?> ps = (PrimitiveStatement<?>) s;
        return (retval.equals(ps.retval) && value.equals(ps.value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 21;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * Add a random delta to the value
     */
    public abstract void delta();

    /**
     * Reset value to default value 0
     */
    public abstract void zero();

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VariableReference> getUniqueVariableReferences() {
        return new ArrayList<VariableReference>(getVariableReferences());
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

        PrimitiveStatement<?> ps = (PrimitiveStatement<?>) s;

        boolean sameValue = false;
        if (value == null) {
            sameValue = (ps.value == null);
        } else {
            sameValue = value.equals(ps.value);
        }

        assert retval != null && ps.retval != null;

        return (sameValue && retval.same(ps.retval));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getCode();
    }

    @SuppressWarnings("unused")
    private void mutateTransformedBoolean(TestCase test) {
        if (Randomness.nextDouble() > Properties.RANDOM_PERTURBATION) {
            boolean done = false;
            for (Statement s : test) {
                if (s instanceof MethodStatement) {
                    MethodStatement ms = (MethodStatement) s;
                    List<VariableReference> parameters = ms.getParameterReferences();
                    int index = parameters.indexOf(retval);
                    if (index >= 0) {
                        Method m = ms.getMethod().getMethod();
                        org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(m);
                        if (types[index].equals(org.objectweb.asm.Type.BOOLEAN_TYPE)) {
                            logger.warn("MUTATING");
                            ((IntPrimitiveStatement) this).negate();
                            done = true;
                            break;
                        }

                    }
                }
            }
            if (!done)
                randomize();
        } else {
            randomize();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        if (!hasMoreThanOneValue())
            return false;

        T oldVal = value;

        while (value == oldVal && value != null) {
            if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
                // When using TT, then an integer may represent a boolean,
                // and we would lose "negation" as a mutation
                if (Properties.TT && getClass().equals(IntPrimitiveStatement.class)) {
                    if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
                        // mutateTransformedBoolean(test);
                        ((IntPrimitiveStatement) this).negate();

                    } else
                        randomize();
                } else {
                    randomize();
                }
            } else
                delta();
        }
        return true;
    }

    /**
     * Set to a random value
     */
    public abstract void randomize();

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericAccessibleObject<?> getAccessibleObject() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignmentStatement() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeClassLoader(ClassLoader loader) {
        super.changeClassLoader(loader);
    }

}
