package org.evosuite.junit.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class BaseRule implements TestRule {

	@Override
	public Statement apply(Statement base, Description description) {
		return statement(base);
	}
	
	private Statement statement(final Statement base) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				before();
				try {
					base.evaluate();
				} finally {
					after();
				}
			}
		};
	}
	
	protected abstract void before();
	
	protected abstract void after();
}
