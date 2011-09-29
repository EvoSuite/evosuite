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

package de.unisb.cs.st.evosuite.primitives;

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

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(PrimitivePool.class);

	private static PrimitivePool instance = null;

	private Set<String> string_pool = null;

	private Set<Integer> int_pool = null;

	private Set<Double> double_pool = null;

	private Set<Long> long_pool = null;

	private Set<Float> float_pool = null;

	private PrimitivePool() {
		string_pool = new HashSet<String>();
		string_pool.add("<xml>");
		string_pool.add("</xml>");
		string_pool.add("<test>");
		string_pool.add("</test>");

		int_pool = new HashSet<Integer>();
		int_pool.add(0);
		int_pool.add(1);
		int_pool.add(-1);

		long_pool = new HashSet<Long>();
		long_pool.add(0L);
		long_pool.add(1L);
		long_pool.add(-1L);

		float_pool = new HashSet<Float>();
		float_pool.add(0.0f);
		float_pool.add(1.0f);
		float_pool.add(-1.0f);

		double_pool = new HashSet<Double>();
		double_pool.add(0.0);
		double_pool.add(1.0);
		double_pool.add(-1.0);
	}

	public static PrimitivePool getInstance() {
		if (instance == null)
			instance = new PrimitivePool();

		return instance;
	}

	public void add(Object object) {
		// Integer, a Float, a Long, a Double a
		logger.debug("Adding to pool: " + object);
		if (object == null)
			return;
		else if (object instanceof String) {
			if (!((String) object).startsWith("mutationId"))
				string_pool.add((String) object);
		} else if (object instanceof Integer) {
			if (Properties.RESTRICT_POOL) {
				int val = (Integer) object;
				if (Math.abs(val) < Properties.MAX_INT && val != Integer.MAX_VALUE
				        && val != Integer.MIN_VALUE) {
					int_pool.add((Integer) object);
				}
			} else {
				int_pool.add((Integer) object);
			}
		} else if (object instanceof Float) {
			if (Math.abs((Float) object) < Properties.MAX_INT)
				float_pool.add((Float) object);
		} else if (object instanceof Long) {
			if (Math.abs((Long) object) < Properties.MAX_INT)
				long_pool.add((Long) object);
		} else if (object instanceof Double) {
			if (Math.abs((Double) object) < Properties.MAX_INT)
				double_pool.add((Double) object);
		}
	}

	public Set<String> getStrings() {
		return string_pool;
	}

	public Set<Integer> getIntegers() {
		return int_pool;
	}

	public Set<Float> getFloats() {
		return float_pool;
	}

	public Set<Double> getDoubles() {
		return double_pool;
	}

	public Set<Long> getLongs() {
		return long_pool;
	}

	public String getRandomString() {
		return Randomness.choice(string_pool);
	}

	public int getRandomInt() {
		int r = Randomness.choice(int_pool);
		return r;
	}

	public float getRandomFloat() {
		return Randomness.choice(float_pool);
	}

	public double getRandomDouble() {
		return Randomness.choice(double_pool);
	}

	public long getRandomLong() {
		return Randomness.choice(long_pool);
	}

}
