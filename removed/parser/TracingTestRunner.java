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

import java.io.BufferedReader;
import java.io.FileReader;
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

/**
 * <p>TracingTestRunner class.</p>
 *
 * @author Gordon Fraser
 */
public class TracingTestRunner extends ClassLoader {

	private static class StatementTracingVisitor extends ClassVisitor {

		final String className;

		public StatementTracingVisitor(String className, ClassWriter writer) {
			super(Opcodes.ASM9, writer);
			this.className = className;
		}

		@Override
		public MethodVisitor visitMethod(int access, final String name, String desc, String signature,
				String[] exceptions) {
			return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, signature, exceptions)) {
				@Override
				public void visitLineNumber(int line, Label start) {
					visitLdcInsn(className + "#" + name + ":" + line);
					visitMethodInsn(Opcodes.INVOKESTATIC, "org/evosuite/junit/TracingTestRunner",
							"traceStatement", "(Ljava/lang/String;)V");
					super.visitLineNumber(line, start);
				}
			};
		}
	}

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TracingTestRunner.class);

	/**
	 * <p>main</p>
	 *
	 * @param args a {@link java.lang.String} object.
	 */
	public static void main(String... args) {
		if (args.length < 1) {
			System.out.println("Give test class to run as argument.");
		}
		new TracingTestRunner().traceTest(args[0]);
	}

	/**
	 * <p>traceStatement</p>
	 *
	 * @param location a {@link java.lang.String} object.
	 */
	public static void traceStatement(String location) {
		String code = readCode(location).trim();
		if (code.isEmpty() || code.equals("}")) {
			return;
		}
		System.out.println(location + ":\t\t" + code);
	}

	private static String readCode(String location) {
		String javaFile = location;
		BufferedReader reader = null;
		try {
			javaFile = javaFile.split("\\$")[0];
			javaFile = javaFile.split("#")[0];
			javaFile = javaFile.replaceAll("\\.", "/");
			javaFile = "src/test/java/" + javaFile + ".java";
			int lineNr = Integer.parseInt(location.split(":")[1]);
			reader = new BufferedReader(new FileReader(javaFile));
			String line = null;
			for (int lineCnt = 0; lineCnt < lineNr; lineCnt++) {
				line = reader.readLine();
			}
			return line;
		} catch (Exception exc) {
			logger.error("Encountered exception opening file '{}':", javaFile, exc);
			throw new RuntimeException(exc);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException exc) {
					// muted
				}
			}
		}
	}

	/** {@inheritDoc} */
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
		if (className.equals(this.getClass().getName())) {
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
}
