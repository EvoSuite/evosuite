/**
 * 
 */
package org.evosuite.seeding;

import org.evosuite.Properties;
import org.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class ConstantPoolManager {

	private static ConstantPoolManager instance = new ConstantPoolManager();

	private ConstantPool[] pools;
	private double[] probabilities;

	/*
	 * We treat it in a special way, for now, just for making experiments
	 * easier to run
	 */
	private static final int DYNAMIC_POOL_INDEX = 2;

	private ConstantPoolManager() {
		init();
	}

	private void init() {
		pools = new ConstantPool[] { new StaticConstantPool(), new StaticConstantPool(),
		        new DynamicConstantPool() };

		initDefaultProbabilities();
	}

	private void initDefaultProbabilities() {
		probabilities = new double[pools.length];
//		double p = 1d / probabilities.length;
		double p = (1d - Properties.DYNAMIC_POOL) / (probabilities.length - 1);
		for (int i = 0; i < probabilities.length; i++) {
			probabilities[i] = p;
		}
		probabilities[DYNAMIC_POOL_INDEX] = Properties.DYNAMIC_POOL;
		normalizeProbabilities();
	}

	private void normalizeProbabilities() {
		double sum = 0d;
		for (double p : probabilities) {
			sum += p;
		}
		double delta = 1d / sum;
		for (int i = 0; i < probabilities.length; i++) {
			probabilities[i] = probabilities[i] * delta;
		}
	}

	public static ConstantPoolManager getInstance() {
		return instance;
	}

	/*
	 * Note: the indexes are hard coded for now. We do it because maybe
	 * in the future we might want to extend this class, so still we need to
	 * use arrays 
	 */

	public void addSUTConstant(Object value) {
		pools[0].add(value);
	}

	public void addNonSUTConstant(Object value) {
		pools[1].add(value);
	}

	public void addDynamicConstant(Object value) {
		pools[DYNAMIC_POOL_INDEX].add(value);
	}

	public ConstantPool getConstantPool() {
		double p = Randomness.nextDouble();
		double k = 0d;
		for (int i = 0; i < probabilities.length; i++) {
			k += probabilities[i];
			if (p < k) {
				return pools[i];
			}
		}
		/*
		 * This should not happen, but you never know with double computations...
		 */
		return pools[0];
	}
	
	public ConstantPool getDynamicConstantPool() {
		return pools[DYNAMIC_POOL_INDEX];
	}

	public void reset() {
		init();
	}
}
