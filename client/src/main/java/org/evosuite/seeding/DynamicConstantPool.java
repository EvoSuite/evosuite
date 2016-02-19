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
package org.evosuite.seeding;

import org.evosuite.Properties;
import org.evosuite.utils.DefaultRandomAccessQueue;
import org.evosuite.utils.RandomAccessQueue;
import org.objectweb.asm.Type;

/**
 * @author Gordon Fraser
 * 
 */
public class DynamicConstantPool implements ConstantPool {

	private final RandomAccessQueue<String> stringPool = new DefaultRandomAccessQueue<String>();

	private final RandomAccessQueue<Type> typePool = new DefaultRandomAccessQueue<Type>();

	private final RandomAccessQueue<Integer> intPool = new DefaultRandomAccessQueue<Integer>();

	private final RandomAccessQueue<Double> doublePool = new DefaultRandomAccessQueue<Double>();

	private final RandomAccessQueue<Long> longPool = new DefaultRandomAccessQueue<Long>();

	private final RandomAccessQueue<Float> floatPool = new DefaultRandomAccessQueue<Float>();

	public DynamicConstantPool() {
		/*
		 * all pools HAVE to be non-empty 
		 */
		stringPool.restrictedAdd("");
		if (Properties.TARGET_CLASS != null && !Properties.TARGET_CLASS.isEmpty()) {
			typePool.restrictedAdd(Type.getObjectType(Properties.TARGET_CLASS));
		} else {
			typePool.restrictedAdd(Type.getType(Object.class));
		}
		intPool.restrictedAdd(0);
		longPool.restrictedAdd(0L);
		floatPool.restrictedAdd(0.0f);
		doublePool.restrictedAdd(0.0);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.primitives.ConstantPool#getRandomString()
	 */
	@Override
	public String getRandomString() {
		return stringPool.getRandomValue();
	}

	@Override
	public Type getRandomType() {
		return typePool.getRandomValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.primitives.ConstantPool#getRandomInt()
	 */
	@Override
	public int getRandomInt() {
		return intPool.getRandomValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.primitives.ConstantPool#getRandomFloat()
	 */
	@Override
	public float getRandomFloat() {
		return floatPool.getRandomValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.primitives.ConstantPool#getRandomDouble()
	 */
	@Override
	public double getRandomDouble() {
		return doublePool.getRandomValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.primitives.ConstantPool#getRandomLong()
	 */
	@Override
	public long getRandomLong() {
		return longPool.getRandomValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.primitives.ConstantPool#add(java.lang.Object)
	 */
	@Override
	public void add(Object object) {
		// We don't add null because this is explicitly handled in the TestFactory
		if (object == null)
			return;

		if (object instanceof String) {
			String string = (String) object;
			if(string.length() > Properties.MAX_STRING)
				return;
			// String literals are constrained to 65535 bytes 
			// as they are stored in the constant pool
			if (string.length() > 65535)
				return;
			stringPool.restrictedAdd(string);
		} else if (object instanceof Type) {
			typePool.restrictedAdd((Type) object);
		}

		else if (object instanceof Integer) {
			if (Properties.RESTRICT_POOL) {
				int val = (Integer) object;
				if (Math.abs(val) < Properties.MAX_INT) {
					intPool.restrictedAdd((Integer) object);
				}
			} else {
				intPool.restrictedAdd((Integer) object);
			}
		} else if (object instanceof Long) {
			if (Properties.RESTRICT_POOL) {
				long val = (Long) object;
				if (Math.abs(val) < Properties.MAX_INT) {
					longPool.restrictedAdd((Long) object);
				}
			} else {
				longPool.restrictedAdd((Long) object);
			}
		} else if (object instanceof Float) {
			if (Properties.RESTRICT_POOL) {
				float val = (Float) object;
				if (Math.abs(val) < Properties.MAX_INT) {
					floatPool.restrictedAdd((Float) object);
				}
			} else {
				floatPool.restrictedAdd((Float) object);
			}
		} else if (object instanceof Double) {
			if (Properties.RESTRICT_POOL) {
				double val = (Double) object;
				if (Math.abs(val) < Properties.MAX_INT) {
					doublePool.restrictedAdd((Double) object);
				}
			} else {
				doublePool.restrictedAdd((Double) object);
			}
		}
	}

	@Override
	public String toString() {
		String res = new String("DynamicConstantPool:{");
		res += "stringPool=" + stringPool.toString() + " ; ";
		res += "typePool=" + typePool.toString() + " ; ";
		res += "intPool=" + intPool.toString() + " ; ";
		res += "longPool=" + longPool.toString() + " ; ";
		res += "floatPool=" + floatPool.toString() + " ; ";
		res += "doublePool=" + doublePool.toString() + "}";	
		return res;
	}

	
}
