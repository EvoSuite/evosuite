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
package de.unisb.cs.st.evosuite.primitives;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimitivePool {

	private static Logger logger = LoggerFactory.getLogger(PrimitivePool.class);

	private static PrimitivePool instance = null;

	private Set<String> stringPool = null;

	private Set<Integer> intPool = null;

	private Set<Double> doublePool = null;

	private Set<Long> longPool = null;

	private Set<Float> floatPool = null;

	private PrimitivePool() {
		stringPool = Collections.synchronizedSet(new HashSet<String>());
		stringPool.add("<xml>");
		stringPool.add("</xml>");
		stringPool.add("<test>");
		stringPool.add("</test>");

		intPool = Collections.synchronizedSet(new HashSet<Integer>());
		intPool.add(0);
		intPool.add(1);
		intPool.add(-1);

		longPool = Collections.synchronizedSet(new HashSet<Long>());
		longPool.add(0L);
		longPool.add(1L);
		longPool.add(-1L);

		floatPool = Collections.synchronizedSet(new HashSet<Float>());
		floatPool.add(0.0f);
		floatPool.add(1.0f);
		floatPool.add(-1.0f);

		doublePool = Collections.synchronizedSet(new HashSet<Double>());
		doublePool.add(0.0);
		doublePool.add(1.0);
		doublePool.add(-1.0);
	}

	public static PrimitivePool getInstance() {
		if (instance == null)
			instance = new PrimitivePool();

		return instance;
	}

	public void add(Object object) {
		// Integer, a Float, a Long, a Double a
		logger.debug("Adding to pool: " + object + " of class ");
		if (object == null)
			return;
		if (object instanceof String) {
			if (!((String) object).startsWith("mutationId"))
				stringPool.add((String) object);
		}

		else if (object instanceof Integer) {
			if (Properties.RESTRICT_POOL) {
				int val = (Integer) object;
				if (Math.abs(val) < Properties.MAX_INT && val != Integer.MAX_VALUE
				        && val != Integer.MIN_VALUE) {
					intPool.add((Integer) object);
				}
			} else {
				intPool.add((Integer) object);
			}
		} else if (object instanceof Long) {
			if (Properties.RESTRICT_POOL) {
				long val = (Long) object;
				if (Math.abs(val) < Properties.MAX_INT && val != Long.MAX_VALUE
				        && val != Long.MIN_VALUE) {
					longPool.add((Long) object);
				}
			} else {
				longPool.add((Long) object);
			}
		} else if (object instanceof Float) {
			if (Properties.RESTRICT_POOL) {
				float val = (Float) object;
				if (Math.abs(val) < Properties.MAX_INT && val != Float.MAX_VALUE
				        && val != -Float.MAX_VALUE) {
					floatPool.add((Float) object);
				}
			} else {
				floatPool.add((Float) object);
			}
		} else if (object instanceof Double) {
			if (Properties.RESTRICT_POOL) {
				double val = (Double) object;
				if (Math.abs(val) < Properties.MAX_INT && val != Double.MAX_VALUE
				        && val != -Double.MAX_VALUE) {
					doublePool.add((Double) object);
				}
			} else {
				doublePool.add((Double) object);
			}
		}
	}

	public Set<String> getStrings() {
		return stringPool;
	}

	public Set<Integer> getIntegers() {
		return intPool;
	}

	public Set<Float> getFloats() {
		return floatPool;
	}

	public Set<Double> getDoubles() {
		return doublePool;
	}

	public Set<Long> getLongs() {
		return longPool;
	}

	public String getRandomString() {
		return Randomness.choice(stringPool);
	}

	public int getRandomInt() {
		int r = Randomness.choice(intPool);
		return r;
	}

	public float getRandomFloat() {
		return Randomness.choice(floatPool);
	}

	public double getRandomDouble() {
		return Randomness.choice(doublePool);
	}

	public long getRandomLong() {
		return Randomness.choice(longPool);
	}

}
