/**
 * 
 */
package org.evosuite.primitives;

import org.evosuite.Properties;
import org.evosuite.utils.DefaultRandomAccessQueue;
import org.evosuite.utils.RandomAccessQueue;

/**
 * @author Gordon Fraser
 * 
 */
public class DynamicConstantPool implements ConstantPool {

	private final RandomAccessQueue<String> stringPool = new DefaultRandomAccessQueue<String>();

	private final RandomAccessQueue<Integer> intPool = new DefaultRandomAccessQueue<Integer>();

	private final RandomAccessQueue<Double> doublePool = new DefaultRandomAccessQueue<Double>();

	private final RandomAccessQueue<Long> longPool = new DefaultRandomAccessQueue<Long>();

	private final RandomAccessQueue<Float> floatPool = new DefaultRandomAccessQueue<Float>();

	public DynamicConstantPool() {
		/*
		 * all pools HAVE to be non-empty 
		 */

		stringPool.restrictedAdd("");
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
			stringPool.restrictedAdd((String) object);
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

}
