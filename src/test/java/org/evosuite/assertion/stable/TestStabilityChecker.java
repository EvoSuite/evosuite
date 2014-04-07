package org.evosuite.assertion.stable;

import java.util.List;

import org.evosuite.Properties;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.sandbox.Sandbox;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;

public abstract class TestStabilityChecker {
	public static boolean checkStability(List<TestCase> list) {
		return true;
	}
	private static boolean checkStability0(List<TestCase> list) {
		int n = list.size();
		boolean previousRunOnSeparateProcess = Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS;
		boolean previousSandbox = Properties.SANDBOX;
		Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = true;
//		Properties.SANDBOX = true;
		
//		if (!Sandbox.isSecurityManagerInitialized()) {
//			Sandbox.initializeSecurityManagerForSUT();
//		}
		TestCaseExecutor.initExecutor(); //needed because it gets pulled down after the search

		for (TestCase tc : list) {
			if (tc.isUnstable()) {
				return false;
			}
		}



		try {
			JUnitAnalyzer.removeTestsThatDoNotCompile(list);
			if (n != list.size()) {
				return false;
			}

			JUnitAnalyzer.handleTestsThatAreUnstable(list);
			if (n != list.size()) {
				return false;
			}

			for (TestCase tc : list) {
				if (tc.isUnstable()) {
					return false;
				}
			}

			return true;
		} finally {
//			Sandbox.resetDefaultSecurityManager();
			Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = previousRunOnSeparateProcess;
//			Properties.SANDBOX = previousSandbox;
		}
	}

}
