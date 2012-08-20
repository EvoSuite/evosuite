/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.primitives.PrimitivePool;
import org.evosuite.runtime.EvoSuiteFile;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.googlecode.gentyref.GenericTypeReflector;

/**
 * Statement assigning a primitive numeric value
 * 
 * @author Gordon Fraser
 * @param <T>
 */
public abstract class PrimitiveStatement<T> extends AbstractStatement {

	private static final long serialVersionUID = -7721106626421922833L;

	/** Constant <code>primitive_pool</code> */
	protected static PrimitivePool primitive_pool = PrimitivePool.getInstance();

	/**
	 * The value
	 */
	T value;

	/**
	 * <p>
	 * Constructor for PrimitiveStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param varRef
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @param value
	 *            a T object.
	 * @param <T>
	 *            a T object.
	 */
	public PrimitiveStatement(TestCase tc, VariableReference varRef, T value) {
		super(tc, varRef);
		this.value = value;
	}

	/**
	 * Constructor
	 * 
	 * @param value
	 *            a T object.
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param type
	 *            a {@link java.lang.reflect.Type} object.
	 */
	public PrimitiveStatement(TestCase tc, Type type, T value) {
		super(tc, new VariableReferenceImpl(tc, type));
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
	 * @param val
	 *            a T object.
	 */
	public void setValue(T val) {
		this.value = val;
	}

	/**
	 * Generate a primitive statement for given type initialized with default
	 * value (0)
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param clazz
	 *            a {@link java.lang.reflect.Type} object.
	 * @return a {@link org.evosuite.testcase.PrimitiveStatement} object.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static PrimitiveStatement<?> getPrimitiveStatement(TestCase tc, Type clazz) {
		// TODO This kills the benefit of inheritance. 
		// Let each class implement the clone method instead

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
		} else if (clazz.equals(EvoSuiteFile.class)) {
			// TODO: Ensure that files were accessed in the first place
			statement = new FileNamePrimitiveStatement(tc, new EvoSuiteFile(
			        Randomness.choice(tc.getAccessedFiles())));
		} else {
			throw new RuntimeException("Getting unknown type: " + clazz + " / "
			        + clazz.getClass());
		}
		return statement;
	}

	/**
	 * Create random primitive statement
	 * 
	 * @param clazz
	 *            a {@link java.lang.reflect.Type} object.
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param type
	 *            a {@link java.lang.reflect.Type} object.
	 * @param position
	 *            a int.
	 * @return a {@link org.evosuite.testcase.PrimitiveStatement} object.
	 */
	public static PrimitiveStatement<?> getRandomStatement(TestCase tc, Type type,
	        int position, Type clazz) {

		PrimitiveStatement<?> statement = getPrimitiveStatement(tc, clazz);
		statement.randomize();
		return statement;

	}

	/** {@inheritDoc} */
	@Override
	public StatementInterface copy(TestCase newTestCase, int offset) {
		@SuppressWarnings("unchecked")
		PrimitiveStatement<T> clone = (PrimitiveStatement<T>) getPrimitiveStatement(newTestCase,
		                                                                            retval.getType());
		clone.setValue(value);
		// clone.assertions = copyAssertions(newTestCase, offset);
		return clone;
	}

	/** {@inheritDoc} */
	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {

		//		assert (retval.isPrimitive() || retval.getVariableClass().isAssignableFrom(value.getClass())) : "we want an "
		//		        + retval.getVariableClass() + " but got an "; // + value.getClass();
		try {
			retval.setObject(scope, value);
		} catch (CodeUnderTestException e) {
			exceptionThrown = e;
		}
		return exceptionThrown;
	}

	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		return references;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		if (retval.equals(var1)) {
			retval = var2;
		}
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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
	 * Push the value on the stack
	 * 
	 * @param mg
	 *            a {@link org.objectweb.asm.commons.GeneratorAdapter} object.
	 */
	protected abstract void pushBytecode(GeneratorAdapter mg);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.Statement#getBytecode(org.objectweb.
	 * asm.commons.GeneratorAdapter)
	 */
	/** {@inheritDoc} */
	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        Throwable exception) {

		pushBytecode(mg);
		if (value != null) {
			Class<?> clazz = value.getClass();
			if (!clazz.equals(retval.getVariableClass())) {
				mg.cast(org.objectweb.asm.Type.getType(clazz),
				        org.objectweb.asm.Type.getType(retval.getVariableClass()));
			}
		}
		retval.storeBytecode(mg, locals);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.Statement#getUniqueVariableReferences()
	 */
	/** {@inheritDoc} */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return new ArrayList<VariableReference>(getVariableReferences());
	}

	/** {@inheritDoc} */
	@Override
	public boolean same(StatementInterface s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		PrimitiveStatement<?> ps = (PrimitiveStatement<?>) s;
		return (retval.same(ps.retval) && value.equals(ps.value));
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getCode();
	}

	private void mutateTransformedBoolean(TestCase test) {
		if (Randomness.nextDouble() > Properties.RANDOM_PERTURBATION) {
			boolean done = false;
			for (StatementInterface s : test) {
				if (s instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) s;
					List<VariableReference> parameters = ms.getParameterReferences();
					int index = parameters.indexOf(retval);
					if (index >= 0) {
						Method m = ms.getMethod();
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

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#mutate(org.evosuite.testcase.TestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean mutate(TestCase test, TestFactory factory) {
		T oldVal = value;

		while (value == oldVal && value != null) {
			if (Randomness.nextDouble() <= Properties.RANDOM_PERTURBATION) {
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

	/** {@inheritDoc} */
	@Override
	public AccessibleObject getAccessibleObject() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssignmentStatement() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#changeClassLoader(java.lang.ClassLoader)
	 */
	/** {@inheritDoc} */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		// No-op
	}

}
