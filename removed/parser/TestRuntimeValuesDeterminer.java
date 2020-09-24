/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.junit;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.evosuite.testcase.ExecutionTracer;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * This class executes an existing test case in binary form to determine its
 * runtime values. To do that, the test case (NOT the SUT) is instrumented.
 * 
 * @author roessler
 */
public class TestRuntimeValuesDeterminer extends RunListener {

	public static class CursorableTrace {
		private final List<ExecutedLine> trace = new ArrayList<ExecutedLine>();
		private int idx = 0;

		private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CursorableTrace.class);

		public void advanceLoop() {
			int lastIdx = idx;
			ExecutedLine lastLine = trace.get(idx);
			idx++;
			ExecutedLine nextLine = trace.get(idx);
			while (nextLine.getLineNumber() > lastLine.getLineNumber()) {
				lastLine = nextLine;
				idx++;
				nextLine = trace.get(idx);
			}
			logger.debug("Advancing current line in trace for advanceLoop from {} to {}.", trace.get(lastIdx)
					.getLineNumber(), trace.get(idx).getLineNumber());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CursorableTrace other = (CursorableTrace) obj;
			return trace.equals(other.trace);
		}

		public void executedLine(Integer lineNumber, Map<String, Object> variableValues) {
			trace.add(new ExecutedLine(lineNumber, variableValues));
		}

		public Object getVariableValueAfter(Integer lineNumber, String variable) {
			int lastIdx = idx;
			ExecutedLine executedLine = trace.get(idx);
			while (!executedLine.getLineNumber().equals(lineNumber)) {
				idx++;
				executedLine = trace.get(idx);
			}
			if (lastIdx != idx) {
				logger.debug("Advancing current line in trace for getVariableValuesAfter from {} to {}.",
						trace.get(lastIdx).getLineNumber(), trace.get(idx).getLineNumber());
			}
			logger.debug("Getting variable value for line {} and variable {}.", lineNumber, variable);
			return executedLine.getVariableValues().get(variable);
		}

		@Override
		public int hashCode() {
			return trace.hashCode();
		}

		@Override
		public String toString() {
			return "CursorableTrace[" + idx + "=" + trace.get(idx) + " from " + trace.size() + "]";
		}

		protected void updateLastLineValues(String variable, Object value) {
			ExecutedLine executedLine = trace.get(trace.size() - 1);
			executedLine.getVariableValues().put(variable, value);
		}
	}

	public static class RuntimeValue {
		private final int lineNumber;
		private final Object value;

		public RuntimeValue(int lineNumber, Object value) {
			this.lineNumber = lineNumber;
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			RuntimeValue other = (RuntimeValue) obj;
			if (lineNumber != other.lineNumber) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public Object getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + lineNumber;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return "RuntimeValue [lineNumber=" + lineNumber + ", value=" + value + "]";
		}
	}

	private static class ExecutedLine {
		private final Integer lineNumber;
		private final Map<String, Object> variableValues;

		public ExecutedLine(Integer lineNumber, Map<String, Object> variableValues) {
			assert lineNumber != null;
			this.lineNumber = lineNumber;
			assert variableValues != null;
			this.variableValues = new HashMap<String, Object>(variableValues);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ExecutedLine other = (ExecutedLine) obj;
			if (!lineNumber.equals(other.lineNumber)) {
				return false;
			}
			if (!variableValues.equals(other.variableValues)) {
				return false;
			}
			return true;
		}

		public Integer getLineNumber() {
			return lineNumber;
		}

		public Map<String, Object> getVariableValues() {
			return variableValues;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + lineNumber.hashCode();
			result = prime * result + variableValues.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return "[" + lineNumber + "=" + variableValues + "]";
		}

	}

	private static class TestValuesDeterminerClassVisitor extends ClassVisitor {

		private final String fullyQualifiedTargetClass;

		public TestValuesDeterminerClassVisitor(String fullyQualifiedTargetClass, ClassWriter cw) {
			super(Opcodes.ASM9, cw);
			this.fullyQualifiedTargetClass = fullyQualifiedTargetClass;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodNode methodNode = new MethodNode(access, name, desc, signature, exceptions);
			MethodVisitor next = super.visitMethod(access, name, desc, signature, exceptions);
			return new TestValuesDeterminerMethodVisitor(fullyQualifiedTargetClass, methodNode, next);
		}
	}

	private static class TestValuesDeterminerMethodVisitor extends MethodVisitor {

		private final org.slf4j.Logger logger = org.slf4j.LoggerFactory
				.getLogger(TestRuntimeValuesDeterminer.TestValuesDeterminerClassVisitor.class);

		private final String fullyQualifiedTargetClass;
		private int currentLine;
		private MethodVisitor next;

		public TestValuesDeterminerMethodVisitor(String fullyQualifiedTargetClass, MethodNode methodNode,
				MethodVisitor next) {
			super(Opcodes.ASM9, methodNode);
			this.fullyQualifiedTargetClass = fullyQualifiedTargetClass;
			this.next = next;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void visitEnd() {
			MethodNode methodNode = (MethodNode) mv;
			Iterator<AbstractInsnNode> nodeIter = methodNode.instructions.iterator();
			while (nodeIter.hasNext()) {
				AbstractInsnNode insnNode = nodeIter.next();
				if (insnNode.getType() == AbstractInsnNode.LINE) {
					currentLine = ((LineNumberNode) insnNode).line;
					methodNode.instructions.insertBefore(insnNode, getLineNumberInstrumentation());
					continue;
				}
				if ((insnNode.getType() == AbstractInsnNode.VAR_INSN)
						|| (insnNode.getType() == AbstractInsnNode.FIELD_INSN)
						|| (insnNode.getType() == AbstractInsnNode.IINC_INSN)
						|| (insnNode.getType() == AbstractInsnNode.INT_INSN)
						|| (insnNode.getType() == AbstractInsnNode.INSN)) {
					methodNode.instructions.insert(insnNode, getInstrumentation(insnNode));
				}
			}
			methodNode.accept(next);
		}

		private int getInstructionIndex(AbstractInsnNode insnNode) {
			try {
				Field indexField = AbstractInsnNode.class.getDeclaredField("index");
				indexField.setAccessible(true);
				Object indexValue = indexField.get(insnNode);
				return ((Integer) indexValue).intValue();
			} catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}

		private InsnList getInstrumentation(AbstractInsnNode insnNode) {
			switch (insnNode.getOpcode()) {
			case Opcodes.ISTORE:
				return localVarValue(insnNode, Opcodes.ILOAD, "I");
			case Opcodes.LSTORE:
				return localVarValue(insnNode, Opcodes.LLOAD, "J");
			case Opcodes.FSTORE:
				return localVarValue(insnNode, Opcodes.FLOAD, "F");
			case Opcodes.DSTORE:
				return localVarValue(insnNode, Opcodes.DLOAD, "F");
			case Opcodes.ASTORE:
				return localVarValue(insnNode, Opcodes.ALOAD, "Ljava/lang/Object;");
			case Opcodes.IINC:
				return localVarValue(insnNode, Opcodes.ILOAD, "I");
			case Opcodes.IASTORE:
			case Opcodes.LASTORE: // -
			case Opcodes.FASTORE: // -
			case Opcodes.DASTORE: // -
			case Opcodes.AASTORE: // -
			case Opcodes.BASTORE: // -
			case Opcodes.CASTORE: // -
			case Opcodes.SASTORE:
				// throw new RuntimeException("Not implemented!");
				logger.error("XASTORE not implemented!");
				return new InsnList();
			case Opcodes.PUTSTATIC:
				// throw new RuntimeException("Not implemented!");
				logger.error("PUTSTATIC not implemented!);");
				return new InsnList();
			case Opcodes.PUTFIELD: // -
				if (insnNode instanceof FieldInsnNode) {
					InsnList instrumentation = new InsnList();
					logger.error("FieldInsnNode not implemented!");
					// FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
					// instrumentation.add(new InsnNode(Opcodes.DUP));
					// instrumentation.add(new FieldInsnNode(Opcodes.GETFIELD,
					// fieldInsnNode.owner, fieldInsnNode.name,
					// fieldInsnNode.desc));
					// instrumentation.add(new
					// LdcInsnNode(fieldInsnNode.owner));
					// instrumentation.add(new LdcInsnNode(fieldInsnNode.name));
					// instrumentation.add(new LdcInsnNode(currentLine));
					// instrumentation.add(new
					// MethodInsnNode(Opcodes.INVOKESTATIC,
					// "org/evosuite/junit/TestRuntimeValuesDeterminer",
					// "fieldValueChanged",
					// "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V"));
					// logger.debug("Adding fieldValueChanged for field {}#{} in line {}.",
					// new Object[] {
					// fieldInsnNode.owner, fieldInsnNode.name, currentLine });
					return instrumentation;
				}
				throw new RuntimeException("Not implemented!");
			}
			return new InsnList();
		}

		private InsnList getLineNumberInstrumentation() {
			InsnList instrumentation = new InsnList();
			instrumentation.add(new LdcInsnNode(currentLine));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					"org/evosuite/junit/TestRuntimeValuesDeterminer", "execLine", "(I)V"));
			return instrumentation;
		}

		private LocalVariableNode getLocalVariableNode(int varIdx, AbstractInsnNode insnNode, MethodNode methodNode) {
			int instrIdx = getInstructionIndex(insnNode);
			List<?> localVariables = methodNode.localVariables;
			for (int idx = 0; idx < localVariables.size(); idx++) {
				LocalVariableNode localVariableNode = (LocalVariableNode) localVariables.get(idx);
				if (localVariableNode.index == varIdx) {
					int scopeEndInstrIdx = getInstructionIndex(localVariableNode.end);
					if (scopeEndInstrIdx >= instrIdx) {
						// still valid for current line
						return localVariableNode;
					}
				}
			}
			throw new RuntimeException("Variable with index " + varIdx + " and end >= " + currentLine
					+ " not found for method " + fullyQualifiedTargetClass + "#" + methodNode.name + "!");
		}

		private InsnList localVarValue(AbstractInsnNode insnNode, int opositeOpcode, String param) {
			int varIdx = -1;
			if (insnNode instanceof VarInsnNode) {
				varIdx = ((VarInsnNode) insnNode).var;
			} else if (insnNode instanceof IincInsnNode) {
				varIdx = ((IincInsnNode) insnNode).var;
			} else {
				throw new RuntimeException("Not implemented for type " + insnNode.getClass());
			}
			InsnList instrumentation = new InsnList();
			MethodNode methodNode = (MethodNode) mv;
			if (methodNode.localVariables.size() <= varIdx) {
				throw new RuntimeException("varInsnNode is pointing outside of local variables!");
			}
			LocalVariableNode localVariableNode = getLocalVariableNode(varIdx, insnNode, methodNode);
			instrumentation.add(new VarInsnNode(opositeOpcode, varIdx));
			instrumentation.add(new LdcInsnNode(localVariableNode.name));
			instrumentation.add(new LdcInsnNode(currentLine));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					"org/evosuite/junit/TestRuntimeValuesDeterminer", "localVarValueChanged", "(" + param
							+ "Ljava/lang/String;I)V"));
			logger.debug("Adding localVarValueChanged for var {} in line {}.", localVariableNode.name, currentLine);
			return instrumentation;
		}
	}

	private static class TransformingClassLoader extends ClassLoader {

		private final String testClass;

		public TransformingClassLoader(String testClass) {
			assert testClass != null;
			this.testClass = testClass;
		}

		@Override
		public Class<?> loadClass(String fullyQualifiedTargetClass) throws ClassNotFoundException {
			if (!testClass.equals(fullyQualifiedTargetClass)) {
				return super.loadClass(fullyQualifiedTargetClass);
			}
			String className = fullyQualifiedTargetClass.replace('.', '/');
			InputStream is = java.lang.ClassLoader.getSystemResourceAsStream(className + ".class");
			if (is == null) {
				throw new ClassNotFoundException("Class " + fullyQualifiedTargetClass + "could not be found!");
			}
			ClassReader reader = null;
			try {
				reader = new ClassReader(is);
			} catch (IOException exc) {
				throw new ClassNotFoundException();
			}
			ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
			ClassVisitor cv = new TestValuesDeterminerClassVisitor(fullyQualifiedTargetClass, writer);
			CheckClassAdapter checkClassAdapter = new CheckClassAdapter(cv);
			reader.accept(checkClassAdapter, ClassReader.SKIP_FRAMES);
			byte[] byteBuffer = writer.toByteArray();
			Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0, byteBuffer.length);
			return result;
		}
	}

	/**
	 * <p>
	 * execLine
	 * </p>
	 * 
	 * @param lineNumber
	 *            a int.
	 */
	public static void execLine(int lineNumber) {
		instance.lineExecuted(lineNumber);
	}

	/**
	 * <p>
	 * fieldValueChanged
	 * </p>
	 * 
	 * @param newValue
	 *            a {@link java.lang.Object} object.
	 * @param owner
	 *            a {@link java.lang.String} object.
	 * @param fieldName
	 *            a {@link java.lang.String} object.
	 * @param lineNumber
	 *            a int.
	 */
	public static void fieldValueChanged(Object newValue, String owner, String fieldName, int lineNumber) {
		System.out.println("FieldValue " + owner + "#" + fieldName + " changed in line " + lineNumber + " to value: "
				+ newValue);
	}

	/**
	 * <p>
	 * localVarValueChanged
	 * </p>
	 * 
	 * @param newValue
	 *            a double.
	 * @param localVar
	 *            a {@link java.lang.String} object.
	 * @param lineNumber
	 *            a int.
	 */
	public static void localVarValueChanged(double newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	/**
	 * <p>
	 * localVarValueChanged
	 * </p>
	 * 
	 * @param newValue
	 *            a float.
	 * @param localVar
	 *            a {@link java.lang.String} object.
	 * @param lineNumber
	 *            a int.
	 */
	public static void localVarValueChanged(float newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	/**
	 * <p>
	 * localVarValueChanged
	 * </p>
	 * 
	 * @param newValue
	 *            a int.
	 * @param localVar
	 *            a {@link java.lang.String} object.
	 * @param lineNumber
	 *            a int.
	 */
	public static void localVarValueChanged(int newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	/**
	 * <p>
	 * localVarValueChanged
	 * </p>
	 * 
	 * @param newValue
	 *            a long.
	 * @param localVar
	 *            a {@link java.lang.String} object.
	 * @param lineNumber
	 *            a int.
	 */
	public static void localVarValueChanged(long newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	/**
	 * <p>
	 * localVarValueChanged
	 * </p>
	 * 
	 * @param newValue
	 *            a {@link java.lang.Object} object.
	 * @param localVar
	 *            a {@link java.lang.String} object.
	 * @param lineNumber
	 *            a int.
	 */
	public static void localVarValueChanged(Object newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestRuntimeValuesDeterminer.class);

	private static TestRuntimeValuesDeterminer instance;
	private static Object lock = new Object();

	/**
	 * <p>
	 * Getter for the field <code>instance</code>.
	 * </p>
	 * 
	 * @param testClass
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.junit.TestRuntimeValuesDeterminer} object.
	 */
	public static TestRuntimeValuesDeterminer getInstance(String testClass) {
		synchronized (lock) {
			if ((instance == null) || !instance.testClass.equals(testClass)) {
				instance = new TestRuntimeValuesDeterminer(testClass);
			}
			return instance;
		}
	}

	private final Map<Integer, Integer> lineExecCnts = new HashMap<Integer, Integer>();
	private final Map<String, CursorableTrace> methodTraces = new HashMap<String, CursorableTrace>();
	private final Map<String, Map<String, List<RuntimeValue>>> methodVariables = new HashMap<String, Map<String, List<RuntimeValue>>>();
	private final String testClass;
	private String currentTest;
	private CursorableTrace currentTrace;

	private Map<String, Object> variableValues;

	private TestRuntimeValuesDeterminer(String testClass) {
		this.testClass = testClass;
	}

	/**
	 * <p>
	 * determineRuntimeValues
	 * </p>
	 */
	public void determineRuntimeValues() {
		try {
			synchronized (lock) {
				Class<?> testClass = instrumentTest();
				// testClass.getConstructors().length
				boolean enabled = ExecutionTracer.isEnabled();
				ExecutionTracer.disable();
				JUnitCore jUnitCore = new JUnitCore();
				jUnitCore.addListener(this);
				Result result = jUnitCore.run(testClass);
				currentTest = null;
				logger.info("Ran {} tests to determine runtime values.", result.getRunCount());
				for (Failure failure : result.getFailures()) {
					if (failure.getDescription().getDisplayName().startsWith("initializationError")) {
						failure.getException().printStackTrace();
						throw new RuntimeException(failure.getException());
					}
				}
				if (enabled) {
					ExecutionTracer.enable();
				}
			}
		} catch (RuntimeException exc) {
			if (exc.getCause() instanceof ClassNotFoundException) {
				logger.error("Unable to load class. Will continue without execution.");
			}
		}
	}

	/**
	 * <p>
	 * getExecutionCount
	 * </p>
	 * 
	 * @param lineNumber
	 *            a int.
	 * @return a int.
	 */
	public int getExecutionCount(int lineNumber) {
		Integer result = lineExecCnts.get(lineNumber);
		if (result == null) {
			return 0;
		}
		return result;
	}

	/**
	 * <p>
	 * getMethodTrace
	 * </p>
	 * 
	 * @param method
	 *            a {@link java.lang.String} object.
	 * @return a
	 *         {@link org.evosuite.junit.TestRuntimeValuesDeterminer.CursorableTrace}
	 *         object.
	 */
	public CursorableTrace getMethodTrace(String method) {
		return methodTraces.get(method);
	}

	/** {@inheritDoc} */
	@Override
	public void testStarted(Description description) throws Exception {
		if (!description.getClassName().equals(testClass)) {
			throw new RuntimeException("Wrong test executed. Should be " + testClass + " but was "
					+ description.getClassName());
		}
		currentTest = description.getMethodName();
		currentTrace = new CursorableTrace();
		methodTraces.put(currentTest, currentTrace);
		variableValues = new HashMap<String, Object>();
	}

	private Class<?> instrumentTest() {
		logger.info("Instrumenting class '{}'.", testClass);
		TransformingClassLoader classLoader = new TransformingClassLoader(testClass);
		try {
			return classLoader.loadClass(testClass);
		} catch (ClassNotFoundException exc) {
			throw new RuntimeException(exc);
		}
	}

	private void lineExecuted(Integer lineNumber) {
		Integer execCnt = lineExecCnts.get(lineNumber);
		if (execCnt == null) {
			execCnt = 0;
		}
		execCnt++;
		lineExecCnts.put(lineNumber, execCnt);
		if (currentTrace != null) {
			currentTrace.executedLine(lineNumber, variableValues);
		}
	}

	private void localVarValueChanged(String localVar, int lineNumber, Object newValue) {
		Map<String, List<RuntimeValue>> variableValues = methodVariables.get(currentTest);
		if (variableValues == null) {
			variableValues = new HashMap<String, List<RuntimeValue>>();
			methodVariables.put(currentTest, variableValues);
		}
		List<RuntimeValue> values = variableValues.get(localVar);
		if (values == null) {
			values = new ArrayList<RuntimeValue>();
			variableValues.put(localVar, values);
		}
		values.add(new RuntimeValue(lineNumber, newValue));
		this.variableValues.put(localVar, newValue);
		currentTrace.updateLastLineValues(localVar, newValue);
	}
}
