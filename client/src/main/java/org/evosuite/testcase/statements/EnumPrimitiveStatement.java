/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
/**
 * 
 */
package org.evosuite.testcase.statements;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * <p>
 * EnumPrimitiveStatement class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class EnumPrimitiveStatement<T extends Enum<T>> extends PrimitiveStatement<T> {

	private static final long serialVersionUID = -7027695648061887082L;

	private transient T[] constants;

	private transient Class<T> enumClass;

	/**
	 * <p>
	 * Constructor for EnumPrimitiveStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @param <T>
	 *            a T object.
	 */
	@SuppressWarnings("unchecked")
	public EnumPrimitiveStatement(TestCase tc, Class<T> clazz) {
		super(tc, clazz, null);
		enumClass = clazz;
		boolean tracerEnabled = ExecutionTracer.isEnabled();
		if (tracerEnabled)
			ExecutionTracer.disable();

		try {
			if (clazz.getEnumConstants().length > 0) {
				this.value = clazz.getEnumConstants()[0];
				constants = clazz.getEnumConstants();

			} else {
				// Coping with empty enums is a bit of a mess
				constants = (T[]) new Enum[0];
			}
		} catch (Throwable t) {
			// Loading the Enum class might fail
			constants = (T[]) new Enum[0];
		}
		if (tracerEnabled)
			ExecutionTracer.enable();
	}

	/**
	 * <p>
	 * Constructor for EnumPrimitiveStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param value
	 *            a T object.
	 */
	@SuppressWarnings("unchecked")
	public EnumPrimitiveStatement(TestCase tc, T value) {
		super(tc, value.getClass(), value);
		boolean tracerEnabled = ExecutionTracer.isEnabled();
		if (tracerEnabled)
			ExecutionTracer.disable();

		enumClass = (Class<T>) retrieveEnumClass(value.getClass());
		constants = (T[]) retrieveEnumClass(value.getClass()).getEnumConstants();

		if (tracerEnabled)
			ExecutionTracer.enable();

	}

	private static Class<?> retrieveEnumClass(Class<?> clazz) {
		if (clazz.isEnum())
			return clazz;
		else if (clazz.getEnclosingClass() != null && clazz.getEnclosingClass().isEnum())
			return clazz.getEnclosingClass();
		else if (clazz.getDeclaringClass() != null && clazz.getDeclaringClass().isEnum())
			return clazz.getDeclaringClass();
		else
			throw new RuntimeException("Cannot find enum class: " + clazz);
	}

	/**
	 * <p>
	 * Getter for the field <code>enumClass</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<T> getEnumClass() {
		return enumClass;
	}

	/**
	 * <p>
	 * getEnumValues
	 * </p>
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public List<T> getEnumValues() {
		return Arrays.asList(constants);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#delta()
	 */
	/** {@inheritDoc} */
	@Override
	public void delta() {
		if (constants.length <= 1) {
			return;
		}

		int pos = 0;
		for (pos = 0; pos < constants.length; pos++) {
			if (constants[pos].equals(value)) {
				break;
			}
		}
		boolean delta = Randomness.nextBoolean();
		if (delta) {
			pos++;
		} else {
			pos--;
		}
		if (pos >= constants.length) {
			pos = 0;
		} else if (pos < 0) {
			pos = constants.length - 1;
		}

		value = constants[pos];
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#hasMoreThanOneValue()
	 */
	@Override
	public boolean hasMoreThanOneValue() {
		return constants.length > 1;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#zero()
	 */
	/** {@inheritDoc} */
	@Override
	public void zero() {
		if (constants.length == 0)
			return;

		value = constants[0];
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	/** {@inheritDoc} */
	@Override
	public void randomize() {
		if (constants.length > 1) {
			int pos = Randomness.nextInt(constants.length);
			value = constants[pos];
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#changeClassLoader(java.lang.ClassLoader)
	 */
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void changeClassLoader(ClassLoader loader) {
		try {
			int pos = 0;
			for (pos = 0; pos < constants.length; pos++) {
				if (constants[pos].equals(value)) {
					break;
				}
			}

			enumClass = (Class<T>) loader.loadClass(enumClass.getName());
			constants = enumClass.getEnumConstants(); // wtf
			if (constants==null) {
				/**
				 * I am not sure why, but sometimes it looks as getEnumConstants()
				 * returns a null value even when the enumClass is actually an enum 
				 * with values. So, the hack I found was simply to retry this again.
				 */
				constants = enumClass.getEnumConstants();
			}
			//TODO: the hack above has a drawback: sometimes the new constant array doesn't contain the element at constants[pos]
			if (constants.length > pos) {
				value = constants[pos];
			}
		} catch (ClassNotFoundException e) {
			logger.warn("Class not found - keeping old class loader ", e);
		} catch (SecurityException e) {
			logger.warn("Class not found - keeping old class loader ", e);
		}
		super.changeClassLoader(loader);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		GenericClass currentClass = new GenericClass(enumClass);
		oos.writeObject(currentClass);
		int pos = 0;
		for (pos = 0; pos < constants.length; pos++) {
			if (constants[pos].equals(value)) {
				break;
			}
		}
		oos.writeInt(pos);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		GenericClass enumGenericClass = (GenericClass) ois.readObject();
		int pos = ois.readInt();
		
		enumClass = (Class<T>) enumGenericClass.getRawClass();
		try {
			if (enumClass.getEnumConstants().length > 0) {
				this.value = enumClass.getEnumConstants()[0];
				constants = enumClass.getEnumConstants();
				if(constants.length > 0)
					value = constants[pos];

			} else {
				// Coping with empty enums is a bit of a mess
				constants = (T[]) new Enum[0];
			}
		} catch (Throwable t) {
			// Loading the Enum class might fail
			constants = (T[]) new Enum[0];
		}
	}
}
