package org.evosuite.symbolic;

public class ConstraintTypeCounter {

	private final int[] countersByType = new int[8];

	private static final int INTEGER_CONSTRAINT_KEY = 0b001;
	private static final int REAL_CONSTRAINT_KEY = 0b010;
	private static final int STRING_CONSTRAINT_KEY = 0b100;

	public void addNewConstraint(boolean isInteger, boolean isReal,
			boolean isString) {
		int key = getKey(isInteger, isReal, isString);
		countersByType[key]++;
	}

	private static int getKey(boolean isIntegerConstraint,
			boolean isRealConstraint, boolean isStringConstraint) {
		int key = 0;
		if (isIntegerConstraint) {
			key = key | INTEGER_CONSTRAINT_KEY;
		}
		if (isRealConstraint) {
			key = key | REAL_CONSTRAINT_KEY;
		}
		if (isStringConstraint) {
			key = key | STRING_CONSTRAINT_KEY;
		}
		return key;
	}

	public void clear() {
		for (int k : countersByType) {
			countersByType[k] = 0;
		}
	}

	public int getTotalNumberOfConstraints() {
		int count = 0;
		for (int k : countersByType) {
			count += countersByType[k];
		}
		return count;
	}

	public int getIntegerOnlyConstraints() {
		int key = getKey(true, false, false);
		return countersByType[key];
	}

	public int getRealOnlyConstraints() {
		int key = getKey(false, true, false);
		return countersByType[key];
	}

	public int getStringOnlyConstraints() {
		int key = getKey(false, false, true);
		return countersByType[key];
	}

	public int getIntegerAndRealConstraints() {
		int key = getKey(true, true, false);
		return countersByType[key];
	}

	public int getIntegerAndStringConstraints() {
		int key = getKey(true, false, true);
		return countersByType[key];
	}

	public int getRealAndStringConstraints() {
		int key = getKey(false, true, true);
		return countersByType[key];
	}

	public int getIntegerRealAndStringConstraints() {
		int key = getKey(true, true, true);
		return countersByType[key];
	}

}
