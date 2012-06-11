package de.unisb.cs.st.evosuite.junit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;

import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;

public class TestRuntimeValuesDeterminer {

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

	public static class VariableValues {
		private final String variableName;

		private final List<RuntimeValue> values = new ArrayList<RuntimeValue>();

		private VariableValues(String variableName) {
			assert variableName != null;
			this.variableName = variableName;
		}

		public void addValue(int lineNumber, Object value) {
			values.add(new RuntimeValue(lineNumber, value));
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
			VariableValues other = (VariableValues) obj;
			if (!variableName.equals(other.variableName)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return variableName.hashCode();
		}

		@Override
		public String toString() {
			return "VariableValues [variableName=" + variableName + "]";
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
					continue;
				}
				if ((insnNode.getType() == AbstractInsnNode.VAR_INSN)
						|| (insnNode.getType() == AbstractInsnNode.FIELD_INSN)
						|| (insnNode.getType() == AbstractInsnNode.IINC_INSN)
						|| (insnNode.getType() == AbstractInsnNode.INT_INSN)) {
					methodNode.instructions.insert(insnNode, getInstrumentation(insnNode));
				}
			}
			methodNode.accept(next);
		}

		private InsnList getInstrumentation(AbstractInsnNode insnNode) {
			int opcode = insnNode.getOpcode();
			InsnList instrumentation = new InsnList();
			switch (opcode) {
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
			case Opcodes.IASTORE: // -
			case Opcodes.LASTORE: // -
			case Opcodes.FASTORE: // -
			case Opcodes.DASTORE: // -
			case Opcodes.AASTORE: // -
			case Opcodes.BASTORE: // -
			case Opcodes.CASTORE: // -
			case Opcodes.SASTORE:
				throw new RuntimeException("Not implemented!");
			case Opcodes.PUTSTATIC:
				throw new RuntimeException("Not implemented!");
			case Opcodes.PUTFIELD: // -
				if (insnNode instanceof FieldInsnNode) {
					FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
					instrumentation.add(new InsnNode(Opcodes.DUP));
					instrumentation.add(new LdcInsnNode(fieldInsnNode.owner));
					instrumentation.add(new LdcInsnNode(fieldInsnNode.name));
					instrumentation.add(new LdcInsnNode(currentLine));
					instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
							"de/unisb/cs/st/evosuite/junit/TestRuntimeValuesDeterminer", "fieldValueChanged",
							"(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V"));
					logger.debug("Adding fieldValueChanged for field {}#{} in line {}.", new Object[] {
							fieldInsnNode.owner, fieldInsnNode.name, currentLine });
					return instrumentation;
				}
				throw new RuntimeException("Not implemented!");
			}
			return instrumentation;
		}

		private InsnList localVarValue(AbstractInsnNode insnNode, int opositeOpcode, String param) {
			InsnList instrumentation = new InsnList();
			VarInsnNode varInsnNode = (VarInsnNode) insnNode;
			MethodNode methodNode = (MethodNode) mv;
			if (methodNode.localVariables.size() <= varInsnNode.var) {
				throw new RuntimeException("varInsnNode is pointing outside of local variables!");
			}
			LocalVariableNode localVariableNode = (LocalVariableNode) methodNode.localVariables.get(varInsnNode.var);
			instrumentation.add(new VarInsnNode(opositeOpcode, varInsnNode.var));
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

		public TransformingClassLoader() {
			// nothing special
		}

		@Override
		public Class<?> loadClass(String fullyQualifiedTargetClass) throws ClassNotFoundException {
			if (isSystemClass(fullyQualifiedTargetClass)
					|| this.getClass().getName().startsWith(fullyQualifiedTargetClass)) {
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

	public static void fieldValueChanged(Object newValue, String owner, String fieldName, int lineNumber) {
		System.out.println("FieldValue " + owner + "#" + fieldName + " changed in line " + lineNumber + " to value: "
				+ newValue);
	}

	public static void localVarValueChanged(double newValue, String localVar, int lineNumber) {
		System.out.println("LocalVarValue " + localVar + " changed in line " + lineNumber + " to value: " + newValue);
	}

	public static void localVarValueChanged(float newValue, String localVar, int lineNumber) {
		System.out.println("LocalVarValue " + localVar + " changed in line " + lineNumber + " to value: " + newValue);
	}

	public static void localVarValueChanged(int newValue, String localVar, int lineNumber) {
		System.out.println("LocalVarValue " + localVar + " changed in line " + lineNumber + " to value: " + newValue);
	}

	public static void localVarValueChanged(long newValue, String localVar, int lineNumber) {
		System.out.println("LocalVarValue " + localVar + " changed in line " + lineNumber + " to value: " + newValue);
	}

	public static void localVarValueChanged(Object newValue, String localVar, int lineNumber) {
		System.out.println("LocalVarValue " + localVar + " changed in line " + lineNumber + " to value: " + newValue);
	}

	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestRuntimeValuesDeterminer.class);

	private static TestRuntimeValuesDeterminer instance;

	private Map<String, VariableValues> mapping = new HashMap<String, VariableValues>();

	private final Map<String, Map<String, List<RuntimeValue>>> methodVariables = new HashMap<String, Map<String, List<RuntimeValue>>>();

	private final String testClass;

	public TestRuntimeValuesDeterminer(String testClass) {
		this.testClass = testClass;
	}

	public void determineRuntimeValues() {
		Class<?> testClass = instrumentTest();
		// testClass.getConstructors().length
		boolean enabled = ExecutionTracer.isEnabled();
		ExecutionTracer.disable();
		Result result = JUnitCore.runClasses(testClass);
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
		// TODO Implement:
		// instrument test
		// run test
		// determine values of all variables
		// and execution of all branches during runtime
	}

	public <T> T getValue(String variable, int line) {
		// TODO implement
		return null;
	}

	// TODO We need a mapping from the generated bytecode instructions
	// to the code that are responsible for these
	public boolean wasExecuted(int lineNr) {
		// TODO Implement
		// register execution of every line
		return false;
	}

	private Class<?> instrumentTest() {
		logger.info("Instrumenting class '{}'.", testClass);
		TransformingClassLoader classLoader = new TransformingClassLoader();
		try {
			return classLoader.loadClass(testClass);
		} catch (ClassNotFoundException exc) {
			throw new RuntimeException(exc);
		}
	}
}
