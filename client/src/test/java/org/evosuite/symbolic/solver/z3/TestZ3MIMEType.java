package org.evosuite.symbolic.solver.z3;

import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestMIMEType;
import org.junit.Test;

public class TestZ3MIMEType extends TestZ3 {

	@Test
	public void test() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestMIMEType.testMIMEType(solver);
	}
}
