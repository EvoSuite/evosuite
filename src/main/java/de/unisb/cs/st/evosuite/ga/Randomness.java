/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with GA.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Unique random number accessor
 * 
 * @author Gordon Fraser
 *
 */
public class Randomness {

	private static Logger logger = Logger.getLogger(Randomness.class);
	
	private static Randomness instance = null;
	
	private long seed = 0;
	
	private MersenneTwister random = null;
	
	private Randomness() {
		String seed_parameter = System.getProperty("random.seed");
		if(seed_parameter != null) {
			try {
				seed = Long.parseLong(seed_parameter);
				random = new MersenneTwister(seed);
			} catch(Exception e) {
				seed = System.currentTimeMillis();
				random = new MersenneTwister();
			}
		}
		logger.info("Random seed: "+seed);
		random = new MersenneTwister(seed);
	}
	
	public static Randomness getInstance() {
		if(instance == null) {
			instance = new Randomness();
		}
		return instance;
	}
	
	public boolean nextBoolean() {
		return random.nextBoolean();
	}
	
	public int nextInt(int max) {
		return random.nextInt(max);
	}

	public int nextInt(int min, int max) {
		return random.nextInt(max-min) + min;
	}

	public int nextInt() {
		return random.nextInt();
	}
	
	public char nextChar() {
		return (char)(random.nextInt(127));
		//return random.nextChar();
	}

	public short nextShort() {
		return (short) (random.nextInt(2 * 32767) - 32767);
	}
	
	public byte nextByte() {
		return (byte) (random.nextInt(256) - 128);
	}
	
	public double nextDouble() {
		return random.nextDouble();
	}
	
	public float nextFloat() {
		return random.nextFloat();
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
		random.setSeed(seed);
	}
	
	public long getSeed() {
		return seed;
	}

	public <T> T choice(List<T> list) {
		if(list.isEmpty())
			return null;
		
		int position = random.nextInt(list.size());
		return list.get(position);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T choice(Set<T> set) {
		if(set.isEmpty())
			return null;
		
		int position = random.nextInt(set.size());
		return (T) set.toArray()[position];
	}
	
	public <T> T choice(T... elements) {
		if(elements.length == 0)
			return null;
		
		int position = random.nextInt(elements.length);
		return elements[position];
	}
	
	public void shuffle(List<?> list) {
		Collections.shuffle(list, random);
	}

	public String nextString(int length) {
		char[] characters = new char[length];
		for(int i = 0; i < length; i++)
			characters[i] = nextChar();
		return new String(characters);
	}
}
