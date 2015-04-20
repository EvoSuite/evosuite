package org.evosuite.junit.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.evosuite.Properties;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.utils.Utils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;


/**
 * Old dead code that does not seem in use any longer
 */
@Deprecated
public class OldJWriteUtils extends TestSuiteWriter{

	
	/**
	 * FIXME: this filter assumes "Test" as prefix, but would be better to have
	 * it as postfix (and as a variable)
	 * 
	 */
	class TestFilter implements IOFileFilter {
		@Override
		public boolean accept(File f, String s) {
			return s.toLowerCase().endsWith(".java") && s.startsWith("Test");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.io.filefilter.IOFileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File file) {
			return file.getName().toLowerCase().endsWith(".java")
			        && file.getName().startsWith("Test");
		}
	}

	/**
	 * Check if test suite has a test case that is a prefix of test.
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a boolean.
	 */
	public static boolean hasPrefix(TestCase test, Collection<TestCase> testCases) {
		for (TestCase t : testCases) {
			if (t.isPrefix(test))
				return true;
		}
		return false;
	}

	/**
	 * Get bytecode representation of test class
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @return an array of byte.
	 */
	public byte[] getBytecode(String name) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		String prefix = Properties.TARGET_CLASS.substring(0,
		                                                  Properties.TARGET_CLASS.lastIndexOf(".")).replace(".",
		                                                                                                    "/");
		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, prefix + "/" + name, null,
		         "junit/framework/TestCase", null);

		Method m = Method.getMethod("void <init> ()");
		GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
		mg.loadThis();
		mg.invokeConstructor(Type.getType(junit.framework.TestCase.class), m);
		mg.returnValue();
		mg.endMethod();

		int num = 0;
		for (TestCase test : testCases) {
			ExecutionResult result = runTest(test);
			m = Method.getMethod("void test" + num + " ()");
			mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
			testToBytecode(test, mg, result.exposeExceptionMapping());
			num++;
		}

		// main method
		m = Method.getMethod("void main (String[])");
		mg = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
		mg.push(1);
		mg.newArray(Type.getType(String.class));
		mg.dup();
		mg.push(0);
		mg.push(Properties.CLASS_PREFIX + "." + name);
		mg.arrayStore(Type.getType(String.class));
		// mg.invokeStatic(Type.getType(org.junit.runner.JUnitCore.class),
		// Method.getMethod("void main (String[])"));
		mg.invokeStatic(Type.getType(junit.textui.TestRunner.class),
		                Method.getMethod("void main (String[])"));
		mg.returnValue();
		mg.endMethod();

		cw.visitEnd();
		return cw.toByteArray();
	}

	private void testToBytecode(TestCase test, GeneratorAdapter mg,
	        Map<Integer, Throwable> exceptions) {
		Map<Integer, Integer> locals = new HashMap<Integer, Integer>();
		mg.visitAnnotation("Lorg/junit/Test;", true);
		int num = 0;
		for (Statement statement : test) {
			logger.debug("Current statement: " + statement.getCode());
			statement.getBytecode(mg, locals, exceptions.get(num));
			num++;
		}
		mg.visitInsn(Opcodes.RETURN);
		mg.endMethod();

	}
	
	/**
	 * Create JUnit test suite in bytecode
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @param directory
	 *            a {@link java.lang.String} object.
	 */
	public void writeTestSuiteClass(String name, String directory) {
		String dir = TestSuiteWriterUtils.makeDirectory(directory);
		File file = new File(dir + "/" + name + ".class");
		byte[] bytecode = getBytecode(name);
		try {
			FileOutputStream stream = new FileOutputStream(file);
			try {
				stream.write(bytecode);
			} finally {
				stream.close();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	/**
	 * Update/create the main file of the test suite. The main test file simply
	 * includes all automatically generated test suites in the same directory
	 * 
	 * @param directory
	 *            Directory of generated test files
	 */
	public void writeTestSuiteMainFile(String directory) {

		File file = new File(TestSuiteWriterUtils.mainDirectory(directory) + "/GeneratedTestSuite.java");

		StringBuilder builder = new StringBuilder();
		if (!Properties.PROJECT_PREFIX.equals("")) {
			builder.append("package ");
			builder.append(Properties.PROJECT_PREFIX);
			// builder.append(".GeneratedTests;");
			builder.append(";\n\n");
		}
		List<String> suites = new ArrayList<String>();

		File basedir = new File(directory);
		Iterator<File> i = FileUtils.iterateFiles(basedir, new TestFilter(),
		                                          TrueFileFilter.INSTANCE);
		while (i.hasNext()) {
			File f = i.next();
			String name = f.getPath().replace(directory, "").replace(".java", "").replace("/",
			                                                                              ".");

			if (name.startsWith("."))
				name = name.substring(1);
			suites.add(name);
		}
		builder.append(TestSuiteWriterUtils.getAdapter().getSuite(suites));
		Utils.writeFile(builder.toString(), file);
	}
}
