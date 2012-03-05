/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * GA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * GA. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unique random number accessor
 * 
 * @author Gordon Fraser
 * 
 */
public class Randomness implements Serializable {

	private static final long serialVersionUID = -5934455398558935937L;

	private static Logger logger = LoggerFactory.getLogger(Randomness.class);

	private static long seed = 0;

	private static Random random = null;

	private static Randomness instance = new Randomness();

	private Randomness() {
		String seed_parameter = System.getProperty("random.seed");
		if (seed_parameter != null) {
			try {
				seed = Long.parseLong(seed_parameter);
				logger.info("Random seed: {}", seed);
			} catch (Exception e) {
				seed = System.currentTimeMillis();
				logger.warn("Could not parse parameter \"{}\", using random seed: {}",
				            seed_parameter, seed);
			}
		} else {
			seed = System.currentTimeMillis();
			logger.info("No seed given. Using {}.", seed);
		}
		random = new MersenneTwister(seed);
	}

	public static Randomness getInstance() {
		if (instance == null) {
			instance = new Randomness();
		}
		return instance;
	}

	public static boolean nextBoolean() {
		return random.nextBoolean();
	}

	public static int nextInt(int max) {
		return random.nextInt(max);
	}

	public static int nextInt(int min, int max) {
		return random.nextInt(max - min) + min;
	}

	public static int nextInt() {
		return random.nextInt();
	}

	public static char nextChar() {
		return (char) (random.nextInt(127));
		//return random.nextChar();
	}

	public static short nextShort() {
		return (short) (random.nextInt(2 * 32767) - 32767);
	}

	public static long nextLong() {
		return random.nextLong();
	}

	public static byte nextByte() {
		return (byte) (random.nextInt(256) - 128);
	}

	public static double nextDouble() {
		return random.nextDouble();
	}

	public static float nextFloat() {
		return random.nextFloat();
	}

	public static void setSeed(long seed) {
		Randomness.seed = seed;
		random.setSeed(seed);
	}

	public static long getSeed() {
		return seed;
	}

	public static <T> T choice(List<T> list) {
		if (list.isEmpty())
			return null;

		int position = random.nextInt(list.size());
		return list.get(position);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T choice(Collection<T> set) {
		if (set.isEmpty())
			return null;

		int position = random.nextInt(set.size());
		return (T) set.toArray()[position];
	}

	public static <T> T choice(T... elements) {
		if (elements.length == 0)
			return null;

		int position = random.nextInt(elements.length);
		return elements[position];
	}

	public static void shuffle(List<?> list) {
		Collections.shuffle(list, random);
	}

	public static String nextString(int length) {
		char[] characters = new char[length];
		for (int i = 0; i < length; i++)
			characters[i] = nextChar();
		return new String(characters);
	}
}
