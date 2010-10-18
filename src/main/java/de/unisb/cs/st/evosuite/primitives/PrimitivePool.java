/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.primitives;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Randomness;

/**
 * @author Gordon Fraser
 *
 */
public class PrimitivePool {

	@SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(PrimitivePool.class);
	
	private static PrimitivePool instance = null;
	
	private static final int MAX_INT = Properties.getPropertyOrDefault("max_int", 256);
	
	private Randomness randomness = Randomness.getInstance();
	
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
		if(instance == null)
			instance = new PrimitivePool();
		
		return instance;
	}
		
	public void add(Object object) {
		// Integer, a Float, a Long, a Double a
		
		if(object == null)
			return;
		else if(object instanceof String) {
			string_pool.add((String)object);
		} else if(object instanceof Integer) {
			int val = (Integer)object;
			if(Math.abs(val) < MAX_INT && val != Integer.MAX_VALUE && val != Integer.MIN_VALUE) {
				int_pool.add((Integer)object);
			}
		} else if(object instanceof Float) {
			if(Math.abs((Float)object) < MAX_INT)
				float_pool.add((Float)object);
		} else if(object instanceof Long) {
			if(Math.abs((Long)object) < MAX_INT)
				long_pool.add((Long)object);
		} else if(object instanceof Double) {
			if(Math.abs((Double)object) < MAX_INT) 	
				double_pool.add((Double)object);			
		}
	}
	
	public String getRandomString() {
		return randomness.choice(string_pool);
	}
	
	public int getRandomInt() {
		return randomness.choice(int_pool);
	}
	
	public float getRandomFloat() {
		return randomness.choice(float_pool);
	}

	public double getRandomDouble() {
		return randomness.choice(double_pool);
	}

	public long getRandomLong() {
		return randomness.choice(long_pool);
	}


}
