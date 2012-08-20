/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.vm.ArithmeticVM;
import org.evosuite.symbolic.vm.CallVM;
import org.evosuite.symbolic.vm.ConcolicMarkerVM;
import org.evosuite.symbolic.vm.FunctionVM;
import org.evosuite.symbolic.vm.HeapVM;
import org.evosuite.symbolic.vm.JumpVM;
import org.evosuite.symbolic.vm.LocalsVM;
import org.evosuite.symbolic.vm.OtherVM;
import org.evosuite.symbolic.vm.PathConstraint;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uta.cse.dsc.DscHandler;
import edu.uta.cse.dsc.IVM;
import edu.uta.cse.dsc.MainConfig;
import edu.uta.cse.dsc.VM;
import edu.uta.cse.dsc.instrument.DscInstrumentingClassLoader;

/**
 * <p>
 * ConcolicExecution class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class ConcolicExecution {

	private File bytecodeFile;

	private static Logger logger = LoggerFactory.getLogger(ConcolicExecution.class);

	private static ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

	private static File tempDir;
	static {
		tempDir = new File(System.getProperty("java.io.tmpdir") + "/"
		        + ManagementFactory.getRuntimeMXBean().getName() + "_"
		        + Long.toString(System.nanoTime()));
		tempDir.mkdir();
		logger.info("Created temporary dir for DSE: " + tempDir.getAbsolutePath());
	}

	/** Constant <code>dirName="tempDir.getAbsolutePath()"</code> */
	protected static String dirName = tempDir.getAbsolutePath();

	/** Constant <code>className="TestCase"</code> */
	protected static String className = Properties.CLASS_PREFIX + ".TestCase";
	// + Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.indexOf("."),
	// Properties.TARGET_CLASS.lastIndexOf("."));

	/**
	 * Constant
	 * <code>classPath="System.getProperty(java.class.path) + :"{trunked}</code>
	 */
	protected static String classPath = System.getProperty("java.class.path") + ":"
	        + dirName;

	/**
	 * <p>
	 * executeConcolic
	 * </p>
	 * 
	 * @param targetName
	 *            a {@link java.lang.String} object.
	 * @param classPath
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	protected List<BranchCondition> executeConcolic(String targetName, String classPath) {

		logger.debug("Setting up Dsc");
		logger.debug("Dsc target=" + targetName);
		logger.debug("Dsc classPath=" + classPath);

		DscHandler dsc_handler = new DscHandler(classPath);
		MainConfig.get().LOG_AST_COUNTS = false;
		MainConfig.get().LOG_MODEL_COUNTS = false;
		MainConfig.get().LOG_PATH_COND_DSC_NOT_NULL = false;
		MainConfig.get().LOG_SUMMARY = false;
		MainConfig.get().USE_MAX = true;
		MainConfig.get().DO_NOT_INSTRUMENT_PREFIXES.add("org.evosuite.symbolic.vm.");

		int dsc_ret_val;
		PathConstraint pc = new PathConstraint();

		DscInstrumentingClassLoader classLoader = new DscInstrumentingClassLoader();
		DscHandler.classLoader = classLoader;

		SymbolicEnvironment env = new SymbolicEnvironment(classLoader);
		List<IVM> listeners = new ArrayList<IVM>();
		listeners.add(new CallVM(env, classLoader));
		listeners.add(new JumpVM(env, pc));
		listeners.add(new HeapVM(env, pc, classLoader));
		listeners.add(new LocalsVM(env));
		listeners.add(new ArithmeticVM(env, pc));
		listeners.add(new OtherVM(env));
		listeners.add(new ConcolicMarkerVM(env));
		listeners.add(new FunctionVM(env));

		VM.vm.setListeners(listeners);

		dsc_ret_val = dsc_handler.mainEntry(new String[] {/*
		                                                  * "conf_evo_dumper.txt" ,
		                                                  */targetName, "main" });

		logger.debug("Dsc ended!");
		if (dsc_ret_val == MainConfig.get().EXIT_SUCCESS) {
			logger.info("Dsc success");

			List<BranchCondition> branches = pc.getBranchConditions();

			logger.info("NrOfBranches=" + branches.size());
			// bytecodeFile.deleteOnExit();

			return branches;

		} else {
			logger.info("Dsc failed!");
			throw new RuntimeException("Dsc failed!");
		}

	}

	/**
	 * Retrieve the path condition for a given test case
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<BranchCondition> getSymbolicPath_2(TestChromosome test) {

		MainConfig.setInstance();
		MainConfig.get().LOG_AST_COUNTS = false;
		MainConfig.get().LOG_MODEL_COUNTS = false;
		MainConfig.get().LOG_PATH_COND_DSC_NOT_NULL = false;
		MainConfig.get().LOG_SUMMARY = false;
		MainConfig.get().USE_MAX = true;

		/**
		 * Instrumenting class loader
		 */
		DscInstrumentingClassLoader classLoader = new DscInstrumentingClassLoader();

		/**
		 * Path constraint and symbolic environment
		 */
		SymbolicEnvironment env = new SymbolicEnvironment(classLoader);
		PathConstraint pc = new PathConstraint();

		/**
		 * VM listeners
		 */
		List<IVM> listeners = new ArrayList<IVM>();
		listeners.add(new CallVM(env, classLoader));
		listeners.add(new JumpVM(env, pc));
		listeners.add(new HeapVM(env, pc, classLoader));
		listeners.add(new LocalsVM(env));
		listeners.add(new ArithmeticVM(env, pc));
		listeners.add(new OtherVM(env));
		listeners.add(new ConcolicMarkerVM(env));
		listeners.add(new FunctionVM(env));
		VM.vm.setListeners(listeners);

		TestChromosome dscCopy = (TestChromosome) test.clone();
		DefaultTestCase defaultTestCase = (DefaultTestCase) dscCopy.getTestCase();
		defaultTestCase.changeClassLoader(classLoader);
		TestCaseExecutor.runTest(dscCopy.getTestCase());

		List<BranchCondition> branches = pc.getBranchConditions();

		return branches;
	}

	/**
	 * Retrieve the path condition for a given test case
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<BranchCondition> getSymbolicPath(TestChromosome test) {

		writeTestCase(getPrimitives(test.getTestCase()), test);
		logger.info("Executing concolic: " + test.getTestCase().toCode());
		List<BranchCondition> conditions = executeConcolic(className, classPath);

		return conditions;

	}

	/**
	 * Get the method that needs to be used to mark this primitive value as
	 * symbolic
	 * 
	 * @param statement
	 * @return
	 */
	private Method getMarkMethod(PrimitiveStatement<?> statement) {
		logger.debug("Statement: " + statement.getCode());
		Class<?> clazz = statement.getReturnValue().getVariableClass();
		if (clazz.equals(Boolean.class) || clazz.equals(boolean.class))
			return org.objectweb.asm.commons.Method.getMethod("boolean mark(boolean,String)");
		else if (clazz.equals(Character.class) || clazz.equals(char.class))
			return org.objectweb.asm.commons.Method.getMethod("char mark(char,String)");
		else if (clazz.equals(Integer.class) || clazz.equals(int.class))
			return org.objectweb.asm.commons.Method.getMethod("int mark(int,String)");
		else if (clazz.equals(Short.class) || clazz.equals(short.class))
			return org.objectweb.asm.commons.Method.getMethod("short mark(short,String)");
		else if (clazz.equals(Long.class) || clazz.equals(long.class))
			return org.objectweb.asm.commons.Method.getMethod("long mark(long,String)");
		else if (clazz.equals(Float.class) || clazz.equals(float.class))
			return org.objectweb.asm.commons.Method.getMethod("float mark(float,String)");
		else if (clazz.equals(Double.class) || clazz.equals(double.class))
			return org.objectweb.asm.commons.Method.getMethod("double mark(double,String)");
		else if (clazz.equals(Byte.class) || clazz.equals(byte.class))
			return org.objectweb.asm.commons.Method.getMethod("byte mark(byte,String)");
		else if (clazz.equals(String.class))
			// FIXME: Probably not for Strings?
			return org.objectweb.asm.commons.Method.getMethod("String mark(String,String)");
		else {
			logger.error("Found primitive of unknown type: " + clazz.getName());
			return null; // FIXME
		}
	}

	/**
	 * Concrete execution
	 * 
	 * @param test
	 * @return
	 */
	private ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			logger.debug("Executing test");
			result = TestCaseExecutor.getInstance().execute(test);
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

	// @SuppressWarnings("rawtypes")
	/**
	 * <p>
	 * getPrimitives
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a {@link java.util.List} object.
	 */
	@SuppressWarnings("unchecked")
	private List<PrimitiveStatement> getPrimitives(TestCase test) {

		List<PrimitiveStatement> p = new ArrayList<PrimitiveStatement>();
		for (StatementInterface s : test) {

			if (s instanceof PrimitiveStatement) {
				PrimitiveStatement ps = (PrimitiveStatement) s;
				if (ps.getValue() == null)
					continue;
				Class<?> t = ps.getReturnClass();
				if (t.equals(Integer.class) || t.equals(int.class)) {
					p.add(ps);
				} else if (t.equals(Boolean.class) || t.equals(boolean.class)) {
					p.add(ps);
				} else if (t.equals(Short.class) || t.equals(short.class)) {
					p.add(ps);
				} else if (t.equals(Byte.class) || t.equals(byte.class)) {
					p.add(ps);
				} else if (t.equals(Long.class) || t.equals(long.class)) {
					p.add(ps);
				} else if (t.equals(Character.class) || t.equals(char.class)) {
					p.add(ps);
					// ==========-------- XXX added for real search
				} else if (t.equals(Float.class) || t.equals(float.class)) {
					p.add(ps);
				} else if (t.equals(Double.class) || t.equals(double.class)) {
					p.add(ps);
					// ==========--------
				} else if (t.equals(String.class)) {
					p.add(ps);
				} else {
					logger.warn("WHAT NON-PRIMITIVE PRIMITIVES ARE THERE?? " + t);
				}
			}
		}
		return p;
	}

	/**
	 * Get concrete value for parameter
	 * 
	 * @param mg
	 * @param locals
	 * @param statement
	 */
	private void getPrimitiveValue(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        PrimitiveStatement<?> statement) {
		// Class<?> clazz = statement.getReturnValue().getVariableClass();
		Class<?> clazz = statement.getValue() != null ? statement.getValue().getClass()
		        : statement.getReturnClass();
		if (clazz.equals(Boolean.class) || clazz.equals(boolean.class))
			mg.push(((Boolean) statement.getValue()).booleanValue());
		else if (clazz.equals(Character.class) || clazz.equals(char.class))
			mg.push(((Character) statement.getValue()).charValue());
		else if (clazz.equals(Integer.class) || clazz.equals(int.class))
			mg.push(((Integer) statement.getValue()).intValue());
		else if (clazz.equals(Short.class) || clazz.equals(short.class))
			mg.push(((Short) statement.getValue()).shortValue());
		else if (clazz.equals(Long.class) || clazz.equals(long.class))
			mg.push(((Long) statement.getValue()).longValue());
		else if (clazz.equals(Float.class) || clazz.equals(float.class))
			mg.push(((Float) statement.getValue()).floatValue());
		else if (clazz.equals(Double.class) || clazz.equals(double.class))
			mg.push(((Double) statement.getValue()).doubleValue());
		else if (clazz.equals(Byte.class) || clazz.equals(byte.class))
			mg.push(((Byte) statement.getValue()).byteValue());
		else if (clazz.equals(String.class)) {
			mg.push(((String) statement.getValue()));
		} else
			logger.error("Found primitive of unknown type: " + clazz.getName());
		/*
		 * if (!clazz.equals(statement.getReturnValue().getVariableClass())) {
		 * mg.cast(org.objectweb.asm.Type.getType(clazz),
		 * org.objectweb.asm.Type.
		 * getType(statement.getReturnValue().getVariableClass())); }
		 */

	}

	/**
	 * Create the bytecode of a class that calls the test with the primitives
	 * marked as symbolic
	 * 
	 * @param target
	 * @param test
	 * @return
	 */
	// @SuppressWarnings("rawtypes")
	// @SuppressWarnings("rawtypes")
	@SuppressWarnings("unchecked")
	private byte[] getBytecode(List<PrimitiveStatement> target, TestChromosome test) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
		         className.replaceAll("\\.", "/"), null, "java/lang/Object", null);

		Method m = Method.getMethod("void <init> ()");
		GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, m, null, null, cw);
		mg.loadThis();
		mg.invokeConstructor(Type.getType(Object.class), m);
		mg.returnValue();
		mg.endMethod();

		int num = 0;
		ExecutionResult result = test.getLastExecutionResult();
		if (result == null)
			result = runTest(test.getTestCase());

		// main method
		m = Method.getMethod("void main (String[])");
		mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, m, null, null,
		        cw);
		Map<Integer, Integer> locals = new HashMap<Integer, Integer>();
		for (StatementInterface statement : test.getTestCase()) {
			logger.debug("Current statement: {}", statement.getCode());
			if (target.contains(statement)) {
				PrimitiveStatement<?> p = (PrimitiveStatement<?>) statement;
				logger.debug("Marking variable: {} - {}", p, p.getReturnValue().getName());
				getPrimitiveValue(mg, locals, p); // TODO: Possibly cast?
				String var_name = p.getReturnValue().getName();
				mg.push(var_name);
				// mg.invokeStatic(Type.getType("Ljpf/mytest/primitive/ConcolicMarker;"),
				// getMarkMethod(p));

				mg.invokeStatic(Type.getType("Lorg/evosuite/symbolic/dsc/ConcolicMarker;"),
				                getMarkMethod(p));
				p.getReturnValue().storeBytecode(mg, locals);

			} else {
				statement.getBytecode(mg, locals,
				                      result.getExceptionThrownAtPosition(num));
			}

			// Only write bytecode up to the point of exception, anything beyond
			// that doesn't count towards coverage
			if (result.isThereAnExceptionAtPosition(num))
				break;
			num++;
		}
		mg.visitInsn(Opcodes.RETURN);
		mg.endMethod();
		cw.visitEnd();

		return cw.toByteArray();
	}

	/**
	 * Write a test case to disk using the specified symbolic values
	 * 
	 * @param statements
	 *            a {@link java.util.List} object.
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 */
	// @SuppressWarnings("rawtypes")
	@SuppressWarnings("unchecked")
	private void writeTestCase(List<PrimitiveStatement> statements, TestChromosome test) {

		// File dir = new File(dirName);
		// dir.mkdir();

		String[] packageRoute = className.split("\\.");

		String className;
		String dirName;
		if (packageRoute.length == 1) {
			dirName = this.dirName;
			className = packageRoute[0];
		} else {
			dirName = this.dirName;
			for (int i = 0; i < packageRoute.length - 1; i++) {
				if (!dirName.equals("")) {
					dirName += File.separator;
				}
				dirName += packageRoute[i];
			}
			className = packageRoute[packageRoute.length - 1];
		}

		File dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		bytecodeFile = new File(dirName + File.separator, className + ".class");
		try {
			FileOutputStream stream = new FileOutputStream(bytecodeFile);
			byte[] bytecode = getBytecode(statements, test);
			stream.write(bytecode);
			// logger.info(dirName);
			// logger.warn(test.getTestCase().toCode());
			// System.exit(0);
		} catch (FileNotFoundException e) {
			logger.warn("FileNotFound during writting bytecode! File");
		} catch (IOException e) {
			logger.warn("IOException writting bytecode!");
		}
	}
}
