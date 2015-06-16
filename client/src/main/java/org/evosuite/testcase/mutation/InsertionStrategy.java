package org.evosuite.testcase.mutation;

import org.evosuite.testcase.TestCase;

public interface InsertionStrategy {

	public int insertStatement(TestCase test, int lastPosition);
}
