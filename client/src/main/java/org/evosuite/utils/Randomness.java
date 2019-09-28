/*
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
package org.evosuite.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

/**
 * Unique random number accessor
 * 
 * @author Gordon Fraser
 */
public class Randomness implements Serializable {

	private static final long serialVersionUID = -5934455398558935937L;

	private static final Logger logger = LoggerFactory.getLogger(Randomness.class);
	private static final int parallelComputationThreshold = 50; // just arbitrarily picked
	private static final int binarySearchThreshold = 10;        // just arbitrarily picked

	private static long seed = 0;

	private static Random random = null;

	private static Randomness instance = new Randomness();

	private Randomness() {
		Long seed_parameter = Properties.RANDOM_SEED;
		if (seed_parameter != null) {
			seed = seed_parameter;
			logger.info("Random seed: {}", seed);
		} else {
			seed = System.currentTimeMillis();
			logger.info("No seed given. Using {}.", seed);
		}
		random = new MersenneTwister(seed);
	}

	/**
	 * <p>
	 * Getter for the field <code>instance</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.utils.Randomness} object.
	 */
	public static Randomness getInstance() {
		if (instance == null) {
			instance = new Randomness();
		}
		return instance;
	}

	/**
	 * <p>
	 * nextBoolean
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public static boolean nextBoolean() {
		return random.nextBoolean();
	}

	/**
	 * Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the
	 * specified value {@code max} (exclusive).
	 *
	 * @param max the upper bound
	 * @return a random number between 0 and {@code max - 1}
	 * @see Random#nextInt(int)
	 */
	public static int nextInt(int max) {
		return random.nextInt(max);
	}

	public static double nextGaussian() {
		return random.nextGaussian();
	}
	
	/**
	 * Returns a pseudorandom, uniformly distributed int value between the lower bound {@code min}
	 * (inclusive) and the upper bound {@code max} (exclusive).
	 *
	 * @param min the lower bound
	 * @param max the upper bound
	 * @return a random number between {@code min} and {@code max}
	 */
	public static int nextInt(int min, int max) {
		return random.nextInt(max - min) + min;
	}

	/**
	 * <p>
	 * nextInt
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int nextInt() {
		return random.nextInt();
	}

	/**
	 * <p>
	 * nextChar
	 * </p>
	 * 
	 * @return a char.
	 */
	public static char nextChar() {
		return (char) (nextInt(32, 128));
		//return random.nextChar();
	}

	/**
	 * <p>
	 * nextShort
	 * </p>
	 * 
	 * @return a short.
	 */
	public static short nextShort() {
		return (short) (random.nextInt(2 * 32767) - 32767);
	}

	/**
	 * <p>
	 * nextLong
	 * </p>
	 * 
	 * @return a long.
	 */
	public static long nextLong() {
		return random.nextLong();
	}

	/**
	 * <p>
	 * nextByte
	 * </p>
	 * 
	 * @return a byte.
	 */
	public static byte nextByte() {
		return (byte) (random.nextInt(256) - 128);
	}

	/**
	 * <p>
	 * returns a randomly generated double in the range [0,1]
	 * </p>
	 * 
	 * @return a double between 0.0 and 1.0
	 */
	public static double nextDouble() {
		return random.nextDouble();
	}

	public static double nextDouble(final double upper) {
		return nextDouble(0, upper);
	}

	/**
	 * <p>
	 * nextDouble
	 * </p>
	 * 
	 * @param min
	 *            a double.
	 * @param max
	 *            a double.
	 * @return a double.
	 */
	public static double nextDouble(double min, double max) {
		return min + (random.nextDouble() * (max - min));
	}

	/**
	 * <p>
	 * nextFloat
	 * </p>
	 * 
	 * @return a float.
	 */
	public static float nextFloat() {
		return random.nextFloat();
	}

	/**
	 * <p>
	 * Setter for the field <code>seed</code>.
	 * </p>
	 * 
	 * @param seed
	 *            a long.
	 */
	public static void setSeed(long seed) {
		Randomness.seed = seed;
		random.setSeed(seed);
	}

	/**
	 * <p>
	 * Getter for the field <code>seed</code>.
	 * </p>
	 * 
	 * @return a long.
	 */
	public static long getSeed() {
		return seed;
	}

	/**
	 * <p>
	 * choice
	 * </p>
	 * 
	 * @param list
	 *            a {@link java.util.List} object.
	 * @param <T>
	 *            a T object.
	 * @return a T object or <code>null</code> if <code>list</code> is empty.
	 */
	public static <T> T choice(List<T> list) {
		if (list.isEmpty())
			return null;

		int position = random.nextInt(list.size());
		return list.get(position);
	}

	/**
	 * <p>
	 * choice
	 * </p>
	 * 
	 * @param set
	 *            a {@link java.util.Collection} object.
	 * @param <T>
	 *            a T object.
	 * @return a T object or <code>null</code> if <code>set</code> is empty.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T choice(Collection<T> set) {
		if (set.isEmpty())
			return null;

		int position = random.nextInt(set.size());
		return (T) set.toArray()[position];
	}

	/**
	 * <p>
	 * choice
	 * </p>
	 * 
	 * @param elements
	 *            a T object.
	 * @param <T>
	 *            a T object.
	 * @return a T object or <code>null</code> if <code>elements.length</code> is zero.
	 */
	public static <T> T choice(T... elements) {
		if (elements.length == 0)
			return null;

		int position = random.nextInt(elements.length);
		return elements[position];
	}

	/**
	 * <p>
	 * shuffle
	 * </p>
	 * 
	 * @param list
	 *            a {@link java.util.List} object.
	 */
	public static void shuffle(List<?> list) {
		Collections.shuffle(list, random);
	}

	public static void shuffle(Object[] array) {
		ArrayUtils.shuffle(array);
	}

	/**
	 * <p>
	 * nextString
	 * </p>
	 * 
	 * @param length
	 *            a int.
	 * @return a {@link java.lang.String} object.
	 */
	public static String nextString(int length) {
		char[] characters = new char[length];
		for (int i = 0; i < length; i++)
			characters[i] = nextChar();
		return new String(characters);
	}

	/**
	 * Performs a roulette wheel selection on the given collection of choices. The probability of
	 * an element being selected is given by the supplied mapper function and directly proportional
	 * to the double value it produces. Mappers must produce values in the interval
	 * (0, Double.MAX_VALUE). (Note that the bounds are exclusive.) Higher values correspond to a
	 * higher probability of being selected. Returns an optional result, which is empty if the
	 * given collection was empty or an error occurred, and contains the chosen element otherwise.
	 *
	 * @param choices elements to choose from
	 * @param mapper function that computes the probability of an element being selected
	 * @param <T> the type of the elements to choose from
	 * @return an optional result, which is empty if there are no elements to choose from or an
	 * error occurred, and contains the chosen element otherwise
	 */
	public static <T> Optional<T> rouletteWheelSelect(final Collection<T> choices,
													  ToDoubleFunction<T> mapper) {
		Objects.requireNonNull(choices);
		Objects.requireNonNull(mapper);

		if (choices.isEmpty()) {
			logger.debug("Nothing to choose from");
			return Optional.empty();
		}

		/*
		 * The collection of choices could be unordered (e.g., when it's a set). Still, the inner
		 * workings of the roulette wheel selection require some arbitrary but fixed order. We
		 * impose this order by converting the collection to an array (arrays also offer
		 * good performance and random access, which the algorithm also benefits from). The imposed
		 * order, even though being arbitrary, has no impact on the outcome of the selection,
		 * since it's fixed during the course of the selection.
		 */
		@SuppressWarnings("unchecked")
		final T[] cs = choices.toArray((T[]) new Object[0]);
		shuffle(cs); // just to be sure

		if (cs.length == 1) {
			return Optional.of(cs[0]);
		}

		if (cs.length == 2) {
			final double cs0 = mapper.applyAsDouble(cs[0]);
			final double cs1 = mapper.applyAsDouble(cs[1]);

			// The mapper must produce non-negative values.
			if (cs0 < 0 || cs1 < 0) {
				logger.error("Mapper produced some negative results: {} {}", cs0, cs1);
				return Optional.empty();
			}

			final double sum = cs0 + cs1;
			if (!(Double.isFinite(sum) && sum > 0)) {
				logger.error("invalid interval length {}", sum);
				return Optional.empty();
			}

			final double pivot = nextDouble(sum);
			final T c = cs0 < pivot ? cs[0] : cs[1];
			return Optional.of(c);
		}

		// The prefix sum of the mapped values is used to determine the index of the chosen
		// element later on.
		final double[] prefixSum = prefixSum(cs, mapper);

		if (prefixSum == null) {
			logger.error("Error during computation of prefix sum");
			return Optional.empty();
		}

		final double sum = prefixSum[prefixSum.length - 1];
		if (!(Double.isFinite(sum) && sum > 0)) {
			logger.error("invalid interval length {}", sum);
			return Optional.empty();
		}

		// We spin the roulette wheel and obtain a pivot point. This is the point on the wheel
		// where the roulette ball falls onto after having lost all of its momentum.
		final double pivot = nextDouble(sum);

		// Finds the pocket on the wheel where the pivot point is located in and converts it to an
		// array  index. This index corresponds to the selected goal.
		final int index = findIndex(prefixSum, pivot);

		return Optional.of(cs[index]);
	}

	private static <T> double[] prefixSum(final T[] elements, ToDoubleFunction<T> mapper) {
		final double[] prefixSum;

		final boolean parallelComputation = elements.length > parallelComputationThreshold;
		if (parallelComputation) {
			prefixSum = Arrays.stream(elements).parallel()
					.mapToDouble(mapper)
					.toArray();
			Arrays.parallelPrefix(prefixSum, Double::sum);
		} else {
			prefixSum = new double[elements.length];
			prefixSum[0] = mapper.applyAsDouble(elements[0]);
			for (int i = 1; i < elements.length; i++) {
				prefixSum[i] = prefixSum[i - 1] + mapper.applyAsDouble(elements[i]);
			}
		}

		// The mapper must produce non-negative values only. If this is satisfied, the array must
		// be sorted by construction.
		final boolean strictlySorted;
		if (parallelComputation) {
			strictlySorted = IntStream.range(0, prefixSum.length - 1)
					.parallel()
					.allMatch(i -> prefixSum[i] < prefixSum[i + 1]);
		} else {
			strictlySorted = IntStream.range(0, prefixSum.length - 1)
					.allMatch(i -> prefixSum[i] < prefixSum[i + 1]);
		}

		if (!strictlySorted) {
			logger.error("prefix sums array is not strictly sorted");
			return null;
		}

		return prefixSum;
	}

	/**
	 * Searches the given strictly sorted array for the specified key and returns the appropriate
	 * index where the key is found. If the array does not contain the key, the insertion point of
	 * the key (i.e. the index where it would be inserted) is returned instead.
	 *
	 * @param sortedArray the array to be searched (must be sorted and not contain duplicates)
	 * @param key         the value to search for in the array
	 * @return the index of the key or its insertion point if the key is not found
	 */
	private static int findIndex(final double[] sortedArray, final double key) {
		final boolean binarySearch = sortedArray.length > binarySearchThreshold;
		if (binarySearch) {
			final int index = Arrays.binarySearch(sortedArray, key);
			return index < 0 ? ~index : index;
		} else { // linear search
			final int lastIndex = sortedArray.length - 1;
			for (int i = 0; i < lastIndex; i++) {
				if (key < sortedArray[i]) { // the array is sorted and free of duplicates
					return i;
				}
			}
			return lastIndex;
		}
	}
}
