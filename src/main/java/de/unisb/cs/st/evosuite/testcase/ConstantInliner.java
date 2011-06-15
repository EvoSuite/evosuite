/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * Inline all primitive values and null references in the test case
 * 
 * @author Gordon Fraser
 * 
 */
public class ConstantInliner extends ExecutionObserver {

	// private static Logger logger = Logger.getLogger(ConstantInliner.class);

	private TestCase test = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	public void inline(TestCase test) {
		this.test = test;
		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		executor.addObserver(this);
		executor.execute(test);
		executor.removeObserver(this);
		assert (test.isValid());

	}

	public void inline(TestChromosome test) {
		inline(test.test);
	}

	public void inline(TestSuiteChromosome suite) {
		for (TestCase test : suite.getTests()) {
			inline(test);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#output(int,
	 * java.lang.String)
	 */
	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.ExecutionObserver#statement(de.unisb
	 * .cs.st.evosuite.testcase.StatementInterface,
	 * de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		for (VariableReference var : statement.getVariableReferences()) {
			if (var.isPrimitive() || var.isString() || (scope.get(var) == null)) {
				ConstantValue value = new ConstantValue(test, var.getGenericClass());
				value.setValue(scope.get(var));
				// logger.info("Statement before inlining: " +
				// statement.getCode());
				statement.replace(var, value);
				// logger.info("Statement after inlining: " +
				// statement.getCode());
			}
		}

	}
}
