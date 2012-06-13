package de.unisb.cs.st.evosuite.junit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;

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

		public TestValuesDeterminerClassVisitor(ClassWriter cw) {
			super(Opcodes.ASM4, cw);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodNode methodNode = new MethodNode(access, name, desc, signature, exceptions);
			MethodVisitor next = super.visitMethod(access, name, desc, signature, exceptions);
			return new TestValuesDeterminerMethodVisitor(methodNode, next);
		}
	}

	private static class TestValuesDeterminerMethodVisitor extends MethodVisitor {

		private final org.slf4j.Logger logger = org.slf4j.LoggerFactory
				.getLogger(TestRuntimeValuesDeterminer.TestValuesDeterminerClassVisitor.class);

		private int currentLine;
		private MethodVisitor next;

		public TestValuesDeterminerMethodVisitor(MethodNode methodNode, MethodVisitor next) {
			super(Opcodes.ASM4, methodNode);
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
				throw new RuntimeException("Not implemented!");
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
					// "de/unisb/cs/st/evosuite/junit/TestRuntimeValuesDeterminer",
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
					"de/unisb/cs/st/evosuite/junit/TestRuntimeValuesDeterminer", "execLine", "(I)V"));
			return instrumentation;
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
			LocalVariableNode localVariableNode = (LocalVariableNode) methodNode.localVariables.get(varIdx);
			instrumentation.add(new VarInsnNode(opositeOpcode, varIdx));
			instrumentation.add(new LdcInsnNode(localVariableNode.name));
			instrumentation.add(new LdcInsnNode(currentLine));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					"de/unisb/cs/st/evosuite/junit/TestRuntimeValuesDeterminer", "localVarValueChanged", "(" + param
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
			if (!testClass.equals(fullyQualifiedTargetClass)){
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
			ClassVisitor cv = new TestValuesDeterminerClassVisitor(writer);
			CheckClassAdapter checkClassAdapter = new CheckClassAdapter(cv);
			reader.accept(checkClassAdapter, ClassReader.SKIP_FRAMES);
			byte[] byteBuffer = writer.toByteArray();
			Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0, byteBuffer.length);
			return result;
		}

		private boolean isSystemClass(String className) {
			if (className.startsWith("java.")) {
				return true;
			}
			if (className.startsWith("sun.")) {
				return true;
			}
			if (className.startsWith("org.junit.") || className.startsWith("junit.framework")) {
				return true;
			}
			return false;
		}
	}

	public static void execLine(int lineNumber) {
		instance.lineExecuted(lineNumber);
	}

	public static void fieldValueChanged(Object newValue, String owner, String fieldName, int lineNumber) {
		System.out.println("FieldValue " + owner + "#" + fieldName + " changed in line " + lineNumber + " to value: "
				+ newValue);
	}

	public static void localVarValueChanged(double newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	public static void localVarValueChanged(float newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	public static void localVarValueChanged(int newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	public static void localVarValueChanged(long newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	public static void localVarValueChanged(Object newValue, String localVar, int lineNumber) {
		instance.localVarValueChanged(localVar, lineNumber, newValue);
	}

	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestRuntimeValuesDeterminer.class);

	private static TestRuntimeValuesDeterminer instance;

	private final Map<Integer, Integer> lineExecCnts = new HashMap<Integer, Integer>();
	private final Map<String, CursorableTrace> methodTraces = new HashMap<String, CursorableTrace>();
	private final Map<String, Map<String, List<RuntimeValue>>> methodVariables = new HashMap<String, Map<String, List<RuntimeValue>>>();
	private final String testClass;
	private String currentTest;
	private CursorableTrace currentTrace;
	private Map<String, Object> variableValues;

	public TestRuntimeValuesDeterminer(String testClass) {
		this.testClass = testClass;
		if (instance == null) {
			instance = this;
		} else {
			throw new RuntimeException("Already got an instance of TestRuntimeValuesDeterminer (existing is for test "
					+ instance.testClass + ").");
		}
	}

	public void determineRuntimeValues() {
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

	public int getExecutionCount(int lineNumber) {
		Integer result = lineExecCnts.get(lineNumber);
		if (result == null) {
			return 0;
		}
		return result;
	}

	public CursorableTrace getMethodTrace(String method) {
		return methodTraces.get(method);
	}

	public Object getValue(String method, String variable, Integer line, Integer iteration) {
		// TODO This method does not work:
		// we cannot keep track of when to use which value
		// in inner loops with conditional code
		// better: keep a list of current values of the variables
		// for each line add that list
		// create a trace class, that traverses this list
		// and internally keeps a 'cursor'
		Map<String, List<RuntimeValue>> variableValues = methodVariables.get(method);
		List<RuntimeValue> values = variableValues.get(variable);
		int iterCnt = 0;
		RuntimeValue lastValue = null;
		for (RuntimeValue runtimeValue : values) {
			if ((lastValue != null) && (lastValue.getLineNumber() >= runtimeValue.getLineNumber())) {
				iterCnt++;
			}
			if (iterCnt > iteration) {
				return lastValue.getValue();
			}
			if (iteration == iterCnt) {
				if (runtimeValue.getLineNumber() == line) {
					return runtimeValue.getValue();
				}
				if (runtimeValue.getLineNumber() > line) {
					return lastValue;
				}
			}
			lastValue = runtimeValue;
		}
		throw new RuntimeException("Line was not executed that often!");
	}

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
	}
}
