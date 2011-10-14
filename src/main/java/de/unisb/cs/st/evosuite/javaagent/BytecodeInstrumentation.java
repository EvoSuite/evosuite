/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGClassAdapter;
import de.unisb.cs.st.evosuite.primitives.PrimitiveClassAdapter;
import de.unisb.cs.st.evosuite.testcase.TestCluster;

/**
 * The bytecode transformer - transforms bytecode depending on package and
 * whether it is the class under test
 * 
 * @author Gordon Fraser
 * 
 */
public class BytecodeInstrumentation implements ClassFileTransformer {

	protected static Logger logger = LoggerFactory.getLogger(BytecodeInstrumentation.class);

	// private static RemoveSystemExitTransformer systemExitTransformer = new
	// RemoveSystemExitTransformer();

	public boolean isTargetProject(String className) {
		return className.startsWith(Properties.PROJECT_PREFIX);
	}

	private boolean isTargetClassName(String className) {
		return TestCluster.isTargetClassName(className);
	}

	private static boolean isJavaagent = false;

	public static boolean isJavaagent() {
		return isJavaagent;
	}

	static {
		logger.info("Loading bytecode transformer for " + Properties.PROJECT_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader
	 * , java.lang.String, java.lang.Class, java.security.ProtectionDomain,
	 * byte[])
	 */
	@Override
	public byte[] transform(ClassLoader loader, String className,
	        Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
	        byte[] classfileBuffer) throws IllegalClassFormatException {
		isJavaagent = true;
		if (className == null) {
			return classfileBuffer;
		}
		String classNameWithDots = className.replace('/', '.');

		// Some packages we shouldn't touch - hard-coded
		if (!isTargetProject(classNameWithDots)
		        && (classNameWithDots.startsWith("java")
		                || classNameWithDots.startsWith("sun")
		                || classNameWithDots.startsWith("org.aspectj.org.eclipse") || classNameWithDots.startsWith("org.mozilla.javascript.gen.c"))) {
			return classfileBuffer;
		}

		if (!isTargetProject(classNameWithDots)) {
			return classfileBuffer;
		}

		try {
			return transformBytes(className, new ClassReader(classfileBuffer));
		} catch (Throwable t) {
			logger.error("Transformation of class " + className + " failed", t);
			// TODO why all the redundant printStackTrace()s?
			// StringWriter writer = new StringWriter();
			// t.printStackTrace(new PrintWriter(writer));
			// logger.fatal(writer.getBuffer().toString());
			// LogManager.shutdown();
			System.out.flush();
			System.exit(0);
			// throw new RuntimeException(e.getMessage());
		}
		return classfileBuffer;
	}

	public byte[] transformBytes(String className, ClassReader reader) {
		String classNameWithDots = className.replace('/', '.');
		// logger.debug("Removing calls to System.exit() from class: "
		// + classNameWithDots);
		// classfileBuffer = systemExitTransformer
		// .transformBytecode(classfileBuffer);

		ClassWriter writer = new ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);

		ClassVisitor cv = writer;

		// Print out bytecode if debug is enabled
		/*if (logger.isDebugEnabled())
			cv = new TraceClassVisitor(cv, new PrintWriter(System.out));*/

		// Apply transformations to class under test and its owned
		// classes
		if (isTargetClassName(classNameWithDots)) {
			if (Properties.MAKE_ACCESSIBLE) {
				cv = new AccessibleClassAdapter(cv, className);
			}
			cv = new ExecutionPathClassAdapter(cv, className);
			cv = new CFGClassAdapter(cv, className);

		} else if (Properties.MAKE_ACCESSIBLE) {
			// Convert protected/default access to public access
			cv = new AccessibleClassAdapter(cv, className);
		}

		// Collect constant values for the value pool
		cv = new PrimitiveClassAdapter(cv, className);

		// If we need to reset static constructors, make them
		// explicit methods
		if (Properties.STATIC_HACK)
			cv = new StaticInitializationClassAdapter(cv, className);

		if (isTargetClassName(classNameWithDots)) { //classNameWithDots.equals(Properties.TARGET_CLASS)) {
			ClassNode cn = new ClassNode();
			reader.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			ComparisonTransformation cmp = new ComparisonTransformation(cn);
			cn = cmp.transform();

			if (Properties.STRING_REPLACEMENT) {
				StringTransformation st = new StringTransformation(cn);
				cn = st.transform();
			}

			cn.accept(cv);

			if (Properties.TT) {
				logger.info("Testability Transforming " + className);
				TestabilityTransformation tt = new TestabilityTransformation(cn);
				// cv = new TraceClassVisitor(writer, new
				// PrintWriter(System.out));
				//cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
				cv = new CheckClassAdapter(cv);
				tt.transform().accept(cv);
			}

		} else {
			reader.accept(cv, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
		}
		// Print out bytecode if debug is enabled
		/*if(logger.isDebugEnabled())
			cv = new TraceClassVisitor(cv, new PrintWriter(System.out));*/
		
		return writer.toByteArray();
	}
}
