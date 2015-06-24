package org.evosuite.symbolic;

import static org.evosuite.symbolic.SymbolicObserverTest.printConstraints;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.java.io.MockFile;
import org.evosuite.runtime.mock.java.net.MockURL;
import org.evosuite.runtime.testdata.EvoSuiteFile;
import org.evosuite.runtime.testdata.EvoSuiteURL;
import org.evosuite.runtime.testdata.FileSystemHandling;
import org.evosuite.runtime.testdata.NetworkHandling;
import org.evosuite.runtime.vfs.VirtualFileSystem;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.TestCaseWithFile;
import com.examples.with.different.packagename.concolic.TestCaseWithURL;

public class ConcolicExecutionEnvironmentTest {

	private static final boolean VFS = Properties.VIRTUAL_FS;
	private static final boolean DEFAULT_REPLACE_CALLS = Properties.REPLACE_CALLS;
	private static final boolean DEFAULT_MOCK_FRAMEWORK_ENABLED = MockFramework
			.isEnabled();

	private List<BranchCondition> executeTest(DefaultTestCase tc) {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000;
		Properties.CONCOLIC_TIMEOUT = 5000000;

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		// ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		return branch_conditions;
	}

	@Test
	public void testDseWithFile() throws SecurityException,
			NoSuchMethodException {
		DefaultTestCase tc = buildTestCaseWithFile();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertTrue(branch_conditions.size() > 0);
	}

	@Test
	public void testDseWithURL() throws SecurityException,
			NoSuchMethodException {
		DefaultTestCase tc = buildTestCaseWithURL();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertTrue(branch_conditions.size() > 0);
	}

	@After
	public void restore() {
		TestGenerationContext.getInstance().resetContext();
		RuntimeSettings.useVFS = VFS;
		Properties.REPLACE_CALLS = DEFAULT_REPLACE_CALLS;
		if (DEFAULT_MOCK_FRAMEWORK_ENABLED) {
			MockFramework.enable();
		} else {
			MockFramework.disable();
		}
	}

	private static DefaultTestCase buildTestCaseWithURL()
			throws SecurityException, NoSuchMethodException {

		TestCaseBuilder tc = new TestCaseBuilder();

		// int int0 = 10;
		VariableReference int0 = tc.appendIntPrimitive(10);

		// String urlString = "http://evosuite.org/hello.txt";
		VariableReference urlStringVarRef = tc
				.appendStringPrimitive("http://evosuite.org/hello.txt");

		// MockURL mockURL0 = MockURL.URL(urlString);
		Method urlMethod = MockURL.class.getMethod("URL", String.class);
		VariableReference mockURLVarRef = tc.appendMethod(null, urlMethod,
				urlStringVarRef);

		// EvoSuiteURL evosuiteURL = new EvoSuiteURL();
		Constructor<EvoSuiteURL> evoSuiteURLCtor = EvoSuiteURL.class
				.getConstructor(String.class);
		VariableReference evosuiteURL = tc.appendConstructor(evoSuiteURLCtor,
				urlStringVarRef);

		// String string0 = "<<FILE CONTENT>>"
		VariableReference string0VarRef = tc
				.appendStringPrimitive("<<FILE CONTENT>>");

		// NetworkHandling.createRemoteTextFile(url, string0)
		Method appendStringToFileMethod = NetworkHandling.class.getMethod(
				"createRemoteTextFile", EvoSuiteURL.class, String.class);
		tc.appendMethod(null, appendStringToFileMethod, evosuiteURL,
				string0VarRef);

		// TestCaseWithURL testCaseWithURL0 = new TestCaseWithURL();
		Constructor<TestCaseWithURL> ctor = TestCaseWithURL.class
				.getConstructor();
		VariableReference testCaseWithURLVarRef = tc.appendConstructor(ctor);

		// String ret_val = dseWithFile0.test((URL) mockURL0);
		Method testMethod = TestCaseWithURL.class.getMethod("test", URL.class);
		tc.appendMethod(testCaseWithURLVarRef, testMethod, mockURLVarRef);

		// DseWithFile.isiZero(int0)
		Method isZeroMethod = TestCaseWithFile.class.getMethod("isZero",
				int.class);
		tc.appendMethod(null, isZeroMethod, int0);

		return tc.getDefaultTestCase();
	}

	@Before
	public void init() {
		Properties.REPLACE_CALLS = true;
		RuntimeSettings.useVFS = true;

		Runtime.getInstance().resetRuntime();
		TestCaseExecutor.getInstance().newObservers();
		TestCaseExecutor.initExecutor();

		MockFramework.enable();
		VirtualFileSystem.getInstance().resetSingleton();
		VirtualFileSystem.getInstance().init();
	}

	private static DefaultTestCase buildTestCaseWithFile()
			throws SecurityException, NoSuchMethodException {

		TestCaseBuilder tc = new TestCaseBuilder();

		// int int0 = 10;
		VariableReference int0 = tc.appendIntPrimitive(10);

		// MockFile mockFile0 = (MockFile)MockFile.createTempFile("tmp",
		// "foo.txt");
		VariableReference prefixVarRef = tc.appendStringPrimitive("temp");
		VariableReference sufixVarRef = tc.appendStringPrimitive(".txt");
		Method createTempFileMethod = MockFile.class.getMethod(
				"createTempFile", String.class, String.class);
		VariableReference mockFileVarRef = tc.appendMethod(null,
				createTempFileMethod, prefixVarRef, sufixVarRef);

		// String path = mockFile0.getPath;
		Method getPathMethod = MockFile.class.getMethod("getPath");
		VariableReference pathVarRef = tc.appendMethod(mockFileVarRef,
				getPathMethod);

		// EvoSuiteFile evosuiteFile = new EvoSuiteFile();
		Constructor<EvoSuiteFile> evoSuiteFileCtor = EvoSuiteFile.class
				.getConstructor(String.class);
		VariableReference evosuiteFile = tc.appendConstructor(evoSuiteFileCtor,
				pathVarRef);

		// String string0 = "<<FILE CONTENT>>"
		VariableReference fileContentVarRef = tc
				.appendStringPrimitive("<<FILE CONTENT>>");

		// FileSystemHandling.appendStringToFile(evosuiteFile, string0)
		Method appendStringToFileMethod = FileSystemHandling.class.getMethod(
				"appendStringToFile", EvoSuiteFile.class, String.class);
		tc.appendMethod(null, appendStringToFileMethod, evosuiteFile,
				fileContentVarRef);

		// DseWithFile dseWithFile0 = new DseWithFile();
		Constructor<TestCaseWithFile> ctor = TestCaseWithFile.class
				.getConstructor();
		VariableReference dseWithFileVarRef = tc.appendConstructor(ctor);

		// String ret_val = dseWithFile0.test((File) mockFile0);
		Method testMethod = TestCaseWithFile.class
				.getMethod("test", File.class);
		tc.appendMethod(dseWithFileVarRef, testMethod, mockFileVarRef);

		// DseWithFile.isiZero(int0)
		Method isZeroMethod = TestCaseWithFile.class.getMethod("isZero",
				int.class);
		tc.appendMethod(null, isZeroMethod, int0);

		return tc.getDefaultTestCase();
	}
}
