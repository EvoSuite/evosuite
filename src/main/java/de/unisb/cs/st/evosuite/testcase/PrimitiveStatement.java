/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Randomness;
import de.unisb.cs.st.evosuite.primitives.PrimitivePool;

/**
 * Statement assigning a primitive numeric value
 * 
 * @author Gordon Fraser
 * 
 * @param <T>
 */
public class PrimitiveStatement<T> extends Statement {

	private static int MAX_STRING = Properties.STRING_LENGTH;

	private static int MAX_INT = Properties.MAX_INT;

	private static double P_pool = Properties.PRIMITIVE_POOL;

	private static Randomness randomness = Randomness.getInstance();

	private static PrimitivePool primitive_pool = PrimitivePool.getInstance();

	/**
	 * The value
	 */
	T value;

	/**
	 * Constructor
	 * 
	 * @param reference
	 * @param value
	 */
	public PrimitiveStatement(VariableReference reference, T value) {
		this.retval = reference;
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T val) {
		this.value = val;
	}

	/**
	 * Create random primitive statement
	 * 
	 * @param reference
	 * @param clazz
	 * @return
	 */
	public static PrimitiveStatement<?> getRandomStatement(VariableReference reference,
	        Type clazz) {

		if (clazz == boolean.class) {
			return new PrimitiveStatement<Boolean>(reference, randomness.nextBoolean());
		} else if (clazz == int.class) {
			if (randomness.nextDouble() >= P_pool)
				return new PrimitiveStatement<Integer>(reference, new Integer(
				        (randomness.nextInt(2 * MAX_INT) - MAX_INT)));
			else
				return new PrimitiveStatement<Integer>(reference,
				        primitive_pool.getRandomInt());

		} else if (clazz == char.class) {
			// Only ASCII chars?
			return new PrimitiveStatement<Character>(reference, (randomness.nextChar()));
		} else if (clazz == long.class) {
			int max = Math.min(MAX_INT, 32767);
			if (randomness.nextDouble() >= P_pool)
				return new PrimitiveStatement<Long>(reference, new Long(
				        (randomness.nextInt(2 * max) - max)));
			else
				return new PrimitiveStatement<Long>(reference,
				        primitive_pool.getRandomLong());

		} else if (clazz.equals(double.class)) {
			if (randomness.nextDouble() >= P_pool)
				return new PrimitiveStatement<Double>(reference,
				        (randomness.nextInt(2 * MAX_INT) - MAX_INT)
				                + randomness.nextDouble());
			else
				return new PrimitiveStatement<Double>(reference,
				        primitive_pool.getRandomDouble());

		} else if (clazz == float.class) {
			if (randomness.nextDouble() >= P_pool)
				return new PrimitiveStatement<Float>(reference,
				        (randomness.nextInt(2 * MAX_INT) - MAX_INT)
				                + randomness.nextFloat());
			else
				return new PrimitiveStatement<Float>(reference,
				        primitive_pool.getRandomFloat());

		} else if (clazz == short.class) {
			int max = Math.min(MAX_INT, 32767);
			if (randomness.nextDouble() >= P_pool)
				return new PrimitiveStatement<Short>(reference, new Short(
				        (short) (randomness.nextInt(2 * max) - max)));
			else
				return new PrimitiveStatement<Short>(reference, new Short(
				        (short) primitive_pool.getRandomInt()));

		} else if (clazz == byte.class) {
			if (randomness.nextDouble() >= P_pool)
				return new PrimitiveStatement<Byte>(reference, new Byte(
				        (byte) (randomness.nextInt(256) - 128)));
			else
				return new PrimitiveStatement<Byte>(reference, new Byte(
				        (byte) (primitive_pool.getRandomInt())));

		} else if (clazz.equals(String.class)) {
			if (randomness.nextDouble() >= P_pool)
				return new PrimitiveStatement<String>(reference,
				        randomness.nextString(randomness.nextInt(MAX_STRING)));
			else
				return new PrimitiveStatement<String>(reference,
				        primitive_pool.getRandomString());
		}
		logger.error("Getting unknown type: " + clazz + " / " + clazz.getClass());

		assert (false);
		return null;
	}

	@Override
	public String getCode(Throwable exception) {
		if (retval.getVariableClass().equals(char.class)
		        || retval.getVariableClass().equals(Character.class))
			return ((Class<?>) retval.getType()).getSimpleName() + " " + retval.getName()
			        + " = '" + StringEscapeUtils.escapeJava(value.toString()) + "';";
		else if (retval.getVariableClass().equals(String.class)) {
			return ((Class<?>) retval.getType()).getSimpleName() + " " + retval.getName()
			        + " = \"" + StringEscapeUtils.escapeJava((String) value) + "\";";
		} else if (retval.getVariableClass().equals(float.class)
		        || retval.getVariableClass().equals(Float.class)) {
			return ((Class<?>) retval.getType()).getSimpleName() + " " + retval.getName()
			        + " = " + value + "F;";
		} else if (retval.getVariableClass().equals(long.class)
		        || retval.getVariableClass().equals(Long.class)) {
			return ((Class<?>) retval.getType()).getSimpleName() + " " + retval.getName()
			        + " = " + value + "L;";
		} else
			return ((Class<?>) retval.getType()).getSimpleName() + " " + retval.getName()
			        + " = " + value + ";";
	}

	@Override
	public StatementInterface clone() {
		return new PrimitiveStatement<T>(retval.clone(), value);
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {
		// Add primitive variable to pool
		scope.set(retval, value);
		return exceptionThrown;
	}

	@Override
	public void adjustVariableReferences(int position, int delta) {
		retval.adjust(delta, position);
		adjustAssertions(position, delta);
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		return references;
	}

	@Override
	public boolean equals(StatementInterface s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		PrimitiveStatement<?> ps = (PrimitiveStatement<?>) s;
		return (retval.equals(ps.retval) && value.equals(ps.value));
	}

	@Override
	public int hashCode() {
		final int prime = 21;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	private static String removeCharAt(String s, int pos) {
		return s.substring(0, pos) + s.substring(pos + 1);
	}

	private static String replaceCharAt(String s, int pos, char c) {
		return s.substring(0, pos) + c + s.substring(pos + 1);
	}

	private static String insertCharAt(String s, int pos, char c) {
		return s.substring(0, pos) + c + s.substring(pos);
	}

	private String StringInsert(String s, int pos) {
		final double ALPHA = 0.5;
		int count = 1;

		while (randomness.nextDouble() <= Math.pow(ALPHA, count)
		        && s.length() < MAX_STRING) {
			count++;
			// logger.info("Before insert: '"+s+"'");
			s = insertCharAt(s, pos, randomness.nextChar());
			// logger.info("After insert: '"+s+"'");
		}
		return s;
	}

	@SuppressWarnings("unchecked")
	private void deltaString() {

		String s = (String) value;

		final double P2 = 1d / 3d;
		double P = 1d / s.length();
		// Delete
		if (randomness.nextDouble() < P2) {
			for (int i = s.length(); i > 0; i--) {
				if (randomness.nextDouble() < P) {
					// logger.info("Before remove at "+i+": '"+s+"'");
					s = removeCharAt(s, i - 1);
					// logger.info("After remove: '"+s+"'");
				}
			}
		}
		P = 1d / s.length();
		// Change
		if (randomness.nextDouble() < P2) {
			for (int i = 0; i < s.length(); i++) {
				if (randomness.nextDouble() < P) {
					// logger.info("Before change: '"+s+"'");
					s = replaceCharAt(s, i, randomness.nextChar());
					// logger.info("After change: '"+s+"'");
				}
			}
		}

		// Insert
		if (randomness.nextDouble() < P2) {
			// for(int i = 0; i < s.length(); i++) {
			// if(randomness.nextDouble() < P) {
			int pos = 0;
			if (s.length() > 0)
				pos = randomness.nextInt(s.length());
			s = StringInsert(s, pos);
			// }
			// }
		}
		value = (T) s;
		// logger.info("Mutated string now is: "+value);
	}

	/**
	 * Create random primitive statement
	 * 
	 * @param reference
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void randomize() {
		if (value instanceof Boolean) {
			value = (T) new Boolean(randomness.nextBoolean());
		} else if (value instanceof Integer) {
			if (randomness.nextDouble() >= P_pool)
				value = (T) new Integer((randomness.nextInt(2 * MAX_INT) - MAX_INT));
			else
				value = (T) new Integer(primitive_pool.getRandomInt());
		} else if (value instanceof Character) {
			value = (T) new Character(randomness.nextChar());
		} else if (value instanceof Long) {
			int max = Math.min(MAX_INT, 32767);
			if (randomness.nextDouble() >= P_pool)
				value = (T) new Long((randomness.nextInt(2 * max) - max));
			else
				value = (T) new Long(primitive_pool.getRandomLong());
		} else if (value instanceof Double) {
			if (randomness.nextDouble() >= P_pool)
				value = (T) new Double((randomness.nextInt(2 * MAX_INT) - MAX_INT)
				        + randomness.nextDouble());
			else
				value = (T) new Double(primitive_pool.getRandomDouble());

		} else if (value instanceof Float) {
			if (randomness.nextDouble() >= P_pool)
				value = (T) new Float((randomness.nextInt(2 * MAX_INT) - MAX_INT)
				        + randomness.nextFloat());
			else
				value = (T) new Float(primitive_pool.getRandomFloat());
		} else if (value instanceof Short) {
			int max = Math.min(MAX_INT, 32767);
			if (randomness.nextDouble() >= P_pool)
				value = (T) new Short((short) (randomness.nextInt(2 * max) - max));
			else
				value = (T) new Short((short) primitive_pool.getRandomInt());
		} else if (value instanceof Byte) {
			if (randomness.nextDouble() >= P_pool)
				value = (T) new Byte((byte) (randomness.nextInt(256) - 128));
			else
				value = (T) new Byte((byte) (primitive_pool.getRandomInt()));
		} else if (value instanceof String) {
			if (randomness.nextDouble() >= P_pool)
				value = (T) randomness.nextString(randomness.nextInt(MAX_STRING));
			else
				value = (T) primitive_pool.getRandomString();
		}
	}

	@SuppressWarnings("unchecked")
	public void delta() {

		//double delta = 40.0 * randomness.nextDouble() - 20.0;
		int delta = randomness.nextInt(40) - 20;
		if (value instanceof Boolean) {
			value = (T) Boolean.valueOf(!((Boolean) value).booleanValue());
		} else if (value instanceof Integer) {
			value = (T) new Integer(((Integer) value).intValue() + delta);
		} else if (value instanceof Character) {
			value = (T) new Character((char) (((Character) value).charValue() + delta));
		} else if (value instanceof Long) {
			value = (T) new Long(((Long) value).longValue() + delta);
		} else if (value instanceof Double) {
			value = (T) new Double(((Double) value).doubleValue() + delta
			        + randomness.nextDouble());
		} else if (value instanceof Float) {
			value = (T) new Float(((Float) value).floatValue() + delta
			        + randomness.nextFloat());
		} else if (value instanceof Short) {
			value = (T) new Short((short) (((Short) value).shortValue() + delta));
		} else if (value instanceof Byte) {
			value = (T) new Byte((byte) (((Byte) value).byteValue() + delta));
		} else if (value instanceof String) {
			deltaString();
		}

	}

	@SuppressWarnings("unchecked")
	public void increment() {
		if (value instanceof Boolean) {
			value = (T) Boolean.valueOf(!((Boolean) value).booleanValue());
		} else if (value instanceof Integer) {
			value = (T) new Integer(((Integer) value).intValue() + 1);
		} else if (value instanceof Character) {
			value = (T) new Character((char) (((Character) value).charValue() + 1));
		} else if (value instanceof Long) {
			value = (T) new Long(((Long) value).longValue() + 1);
		} else if (value instanceof Double) {
			value = (T) new Double(((Double) value).doubleValue() + 1.0);
		} else if (value instanceof Float) {
			value = (T) new Float(((Float) value).floatValue() + 1.0);
		} else if (value instanceof Short) {
			value = (T) new Short((short) (((Short) value).shortValue() + 1));
		} else if (value instanceof Byte) {
			value = (T) new Byte((byte) (((Byte) value).byteValue() + 1));
		}
	}

	@SuppressWarnings("unchecked")
	public void decrement() {
		if (value instanceof Boolean) {
			value = (T) Boolean.valueOf(!((Boolean) value).booleanValue());
		} else if (value instanceof Integer) {
			value = (T) new Integer(((Integer) value).intValue() - 1);
		} else if (value instanceof Character) {
			value = (T) new Character((char) (((Character) value).charValue() - 1));
		} else if (value instanceof Long) {
			value = (T) new Long(((Long) value).longValue() - 1);
		} else if (value instanceof Double) {
			value = (T) new Double(((Double) value).doubleValue() - 1.0);
		} else if (value instanceof Float) {
			value = (T) new Float(((Float) value).floatValue() - 1.0);
		} else if (value instanceof Short) {
			value = (T) new Short((short) (((Short) value).shortValue() - 1));
		} else if (value instanceof Byte) {
			value = (T) new Byte((byte) (((Byte) value).byteValue() - 1));

		}
	}

	@SuppressWarnings("unchecked")
	public void zero() {
		if (value instanceof Boolean) {
			value = (T) Boolean.FALSE;
		} else if (value instanceof Integer) {
			value = (T) new Integer(0);
		} else if (value instanceof Character) {
			value = (T) new Character((char) 0);
		} else if (value instanceof Long) {
			value = (T) new Long(0);
		} else if (value instanceof Double) {
			value = (T) new Double(0.0);
		} else if (value instanceof Float) {
			value = (T) new Float(0.0);
		} else if (value instanceof Short) {
			value = (T) new Short((short) 0);
		} else if (value instanceof Byte) {
			value = (T) new Byte((byte) 0);

		}
	}

	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if (retval.equals(oldVar))
			retval = newVar;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getBytecode(org.objectweb.
	 * asm.commons.GeneratorAdapter)
	 */
	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        Throwable exception) {
		Class<?> clazz = value.getClass();
		if (!clazz.equals(retval.getVariableClass())) {
			mg.cast(org.objectweb.asm.Type.getType(clazz),
			        org.objectweb.asm.Type.getType(retval.getVariableClass()));
		}
		if (clazz.equals(Boolean.class) || clazz.equals(boolean.class))
			mg.push(((Boolean) value).booleanValue());
		else if (clazz.equals(Character.class) || clazz.equals(char.class))
			mg.push(((Character) value).charValue());
		else if (clazz.equals(Integer.class) || clazz.equals(int.class))
			mg.push(((Integer) value).intValue());
		else if (clazz.equals(Short.class) || clazz.equals(short.class))
			mg.push(((Short) value).shortValue());
		else if (clazz.equals(Long.class) || clazz.equals(long.class))
			mg.push(((Long) value).longValue());
		else if (clazz.equals(Float.class) || clazz.equals(float.class))
			mg.push(((Float) value).floatValue());
		else if (clazz.equals(Double.class) || clazz.equals(double.class))
			mg.push(((Double) value).doubleValue());
		else if (clazz.equals(Byte.class) || clazz.equals(byte.class))
			mg.push(((Byte) value).byteValue());
		else if (clazz.equals(String.class))
			mg.push(((String) value));
		else
			logger.fatal("Found primitive of unknown type: " + clazz.getName());
		retval.storeBytecode(mg, locals);
		// mg.storeLocal(retval.statement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getUniqueVariableReferences()
	 */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return new ArrayList<VariableReference>(getVariableReferences());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#replaceUnique(de.unisb.cs.
	 * st.evosuite.testcase.VariableReference,
	 * de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replaceUnique(VariableReference old_var, VariableReference new_var) {
		if (retval == old_var)
			retval = new_var;
		if (retval.array == old_var)
			retval.array = new_var;
	}
}
