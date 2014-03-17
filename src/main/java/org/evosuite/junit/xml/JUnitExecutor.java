package org.evosuite.junit.xml;

import java.util.List;

import org.evosuite.junit.JUnitFailure;
import org.evosuite.junit.JUnitResult;
import org.evosuite.junit.JUnitResultBuilder;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class JUnitExecutor {

	public JUnitResult execute(Class<?>... testClasses) {

		JUnitCore core = new JUnitCore();
		Result result = core.run(testClasses);

		JUnitResultBuilder builder = new JUnitResultBuilder();
		JUnitResult junitResult = builder.build(result);

		return junitResult;
	}
}
