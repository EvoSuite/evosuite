package de.unisb.cs.st.evosuite.junit;

import java.io.IOException;
import java.io.InputStream;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

public class TracingTestRunner extends ClassLoader {

	private static class StatementTracingVisitor extends ClassVisitor {

		final String className;

		public StatementTracingVisitor(String className, ClassWriter writer) {
			super(Opcodes.ASM4, writer);
			this.className = className;
		}

		@Override
		public MethodVisitor visitMethod(int access, final String name, String desc, String signature,
				String[] exceptions) {
			return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, name, desc, signature, exceptions)) {
				@Override
				public void visitLineNumber(int line, Label start) {
					visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
					visitLdcInsn(className + "#" + name + ":" + line);
					visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
					super.visitLineNumber(line, start);
				}
			};
		}
	}

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TracingTestRunner.class);

	public static void main(String... args) {
		if (args.length < 1) {
			System.out.println("Give test class to run as argument.");
		}
		new TracingTestRunner().traceTest(args[0]);
	}

	@Override
	public Class<?> loadClass(String fullyQualifiedTargetClass) throws ClassNotFoundException {
		if (isSystemClass(fullyQualifiedTargetClass)) {
			logger.info("Not instrumenting class '{}'.", fullyQualifiedTargetClass);
			return super.loadClass(fullyQualifiedTargetClass);
		}
		logger.info("Instrumenting class '{}'.", fullyQualifiedTargetClass);
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
		ClassVisitor cv = new StatementTracingVisitor(fullyQualifiedTargetClass, writer);
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

	private void traceTest(String testCaseName) {
		Class<?> testCase = null;
		try {
			testCase = loadClass(testCaseName);
		} catch (ClassNotFoundException exc) {
			throw new RuntimeException(exc);
		}
		Result result = JUnitCore.runClasses(testCase);
		if (!result.getFailures().isEmpty()) {
			logger.info("{} tests failed: {}", result.getFailureCount(), result.getFailures());
		}
	}
	
	public static void traceStatement(String location){
		
	}
}
