/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jpf.mytest.integer.IntegerNextChoiceProvider;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

/**
 * @author Gordon Fraser
 * 
 */
public class ConcolicMutation {

	protected static Logger logger = Logger.getLogger(ConcolicMutation.class);

	protected TestCaseExecutor executor = new TestCaseExecutor();

	protected static String dirName = System.getProperty("java.io.tmpdir")
	        + "/TempClasses";

	protected static String className = "TestCase";
	//	        + Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.indexOf("."),
	//	                                            Properties.TARGET_CLASS.lastIndexOf("."));

	protected static String classPath = System.getProperty("java.class.path") + ":"
	        + dirName;

	public ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			logger.debug("Executing test");
			result.exceptions = executor.run(test);
			executor.setLogging(true);
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

	private Method getMarkMethod(PrimitiveStatement statement) {
		Class<?> clazz = statement.getReturnValue().getVariableClass();
		if (clazz.equals(Boolean.class) || clazz.equals(boolean.class))
			return org.objectweb.asm.commons.Method.getMethod("boolean mark(boolean)");
		else if (clazz.equals(Character.class) || clazz.equals(char.class))
			return org.objectweb.asm.commons.Method.getMethod("char mark(char)");
		else if (clazz.equals(Integer.class) || clazz.equals(int.class))
			return org.objectweb.asm.commons.Method.getMethod("int mark(int)");
		else if (clazz.equals(Short.class) || clazz.equals(short.class))
			return org.objectweb.asm.commons.Method.getMethod("short mark(short)");
		else if (clazz.equals(Long.class) || clazz.equals(long.class))
			return org.objectweb.asm.commons.Method.getMethod("long mark(long)");
		else if (clazz.equals(Float.class) || clazz.equals(float.class))
			return org.objectweb.asm.commons.Method.getMethod("float mark(float)");
		else if (clazz.equals(Double.class) || clazz.equals(double.class))
			return org.objectweb.asm.commons.Method.getMethod("double mark(double)");
		else if (clazz.equals(Byte.class) || clazz.equals(byte.class))
			return org.objectweb.asm.commons.Method.getMethod("byte mark(byte)");
		else if (clazz.equals(String.class))
			// FIXME: Probably not for Strings?
			return org.objectweb.asm.commons.Method.getMethod("String mark(String)");
		else {
			logger.fatal("Found primitive of unknown type: " + clazz.getName());
			return null; // FIXME
		}
	}

	private void getPrimitiveValue(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        PrimitiveStatement statement) {
		Class<?> clazz = statement.getReturnValue().getVariableClass();
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
		else if (clazz.equals(String.class))
			mg.push(((String) statement.getValue()));
		else
			logger.fatal("Found primitive of unknown type: " + clazz.getName());
	}

	private byte[] getBytecode(PrimitiveStatement target, TestCase test) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null,
		         "java/lang/Object", null);

		Method m = Method.getMethod("void <init> ()");
		GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, m, null, null, cw);
		mg.loadThis();
		mg.invokeConstructor(Type.getType(Object.class), m);
		mg.returnValue();
		mg.endMethod();

		int num = 0;
		ExecutionResult result = runTest(test);

		// main method
		m = Method.getMethod("void main (String[])");
		mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, m, null, null,
		        cw);
		Map<Integer, Integer> locals = new HashMap<Integer, Integer>();
		for (Statement statement : test.getStatements()) {
			logger.debug("Current statement: " + statement.getCode());
			if (statement == target) {
				getPrimitiveValue(mg, locals, target); // TODO: Possibly cast?
				mg.invokeStatic(Type.getType("Ljpf/mytest/primitive/ConcolicMarker;"),
				                getMarkMethod(target));
				target.getReturnValue().storeBytecode(mg, locals);

			} else {
				statement.getBytecode(mg, locals, result.exceptions.get(num));
			}
			num++;
		}
		mg.visitInsn(Opcodes.RETURN);
		mg.endMethod();
		cw.visitEnd();

		return cw.toByteArray();
	}

	public void writeTestCase(PrimitiveStatement statement, TestCase test) {
		File dir = new File(dirName);
		dir.mkdir();
		File file = new File(dirName + "/", className + ".class");
		try {
			FileOutputStream stream = new FileOutputStream(file);
			byte[] bytecode = getBytecode(statement, test);
			stream.write(bytecode);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	public void mutate(PrimitiveStatement statement, TestCase test) {
		writeTestCase(statement, test);
		logger.error("Generating test for class " + className + " with classPath "
		        + classPath);
		System.out.println("Generating new value for statement " + statement.getCode());
		System.out.println(test.toCode());
		jpf.mytest.generator.execution.TestCase jpfTest = new jpf.mytest.generator.execution.TestCase(
		        new IntegerNextChoiceProvider(), new IntegerNextChoiceProvider(),
		        className, classPath, null);
		Map<String, Object> values = jpfTest.getPCG().getAlternativePath();
		logger.error("Concolic execution done.");
		System.out.println("Concolic execution done.");
		if (values != null && !values.isEmpty()) {
			Object val = values.values().toArray()[0];
			if (val != null) {
				if (val instanceof Long) {
					Long value = (Long) val;
					System.out.println("New value is " + value);
					statement.setValue(value.intValue());
				} else {
					System.out.println("New value is not long " + val);
				}
			} else {
				System.out.println("New value is null");

			}
			//logger.info("Created value: " + values.values().toArray()[0]);
		} else {
			if (values == null) {
				System.out.println("Return value is null");
				values = jpfTest.getPCG().getAlternativePath();
				System.out.println("Second concolic execution done.");
				if (values != null && !values.isEmpty()) {
					Object val = values.values().toArray()[0];
					if (val != null) {
						if (val instanceof Long) {
							Long value = (Long) val;
							System.out.println("New value is " + value);
							statement.setValue(value.intValue());
						} else {
							System.out.println("New value is not long " + val);
						}
					} else {
						System.out.println("New value is null");

					}
				} else {
					System.out.println("Second constraint system failed");
				}
			} else
				System.out.println("Return value is empty");
		}
	}
}
