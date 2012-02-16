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

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGClassAdapter;
import de.unisb.cs.st.evosuite.primitives.PrimitiveClassAdapter;
import de.unisb.cs.st.evosuite.testcase.StaticTestCluster;

/**
 * The bytecode transformer - transforms bytecode depending on package and
 * whether it is the class under test
 * 
 * @author Gordon Fraser
 * 
 */
public class BytecodeInstrumentation implements ClassFileTransformer {

	private static Logger logger = LoggerFactory.getLogger(BytecodeInstrumentation.class);

	private static List<ClassAdapterFactory> externalPreVisitors = new ArrayList<ClassAdapterFactory>();

	private static List<ClassAdapterFactory> externalPostVisitors = new ArrayList<ClassAdapterFactory>();

	public static void addClassAdapter(ClassAdapterFactory factory) {
		externalPostVisitors.add(factory);
	}

	public static void addPreClassAdapter(ClassAdapterFactory factory) {
		externalPreVisitors.add(factory);
	}

	public boolean isJavaClass(String className) {
		return className.startsWith("java.") || className.startsWith("sun.")
		        || className.startsWith("javax.");
	}

	public boolean isTargetProject(String className) {
		return (className.startsWith(Properties.PROJECT_PREFIX) || (!Properties.TARGET_CLASS_PREFIX.isEmpty() && className.startsWith(Properties.TARGET_CLASS_PREFIX)))
		        && !className.startsWith("java.")
		        && !className.startsWith("sun.")
		        && !className.startsWith("de.unisb.cs.st.evosuite")
		        && !className.startsWith("javax.")
		        && !className.startsWith("org.xml.sax")
		        && !className.startsWith("apple.")
		        && !className.startsWith("com.apple.")
		        && !className.startsWith("daikon.");
	}

	public boolean shouldTransform(String className) {
		if (!Properties.TT)
			return false;
		switch (Properties.TT_SCOPE) {
		case ALL:
			return true;
		case TARGET:
			if (className.equals(Properties.TARGET_CLASS)
			        || className.startsWith(Properties.TARGET_CLASS + "$"))
				return true;
			break;
		case PREFIX:
			if (className.startsWith(Properties.PROJECT_PREFIX))
				return true;

		}
		return false;
	}

	private boolean isTargetClassName(String className) {
		// TODO: Need to replace this in the long term
		return StaticTestCluster.isTargetClassName(className);
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
		TransformationStatistics.reset();

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		ClassVisitor cv = writer;
		// if(logger.isDebugEnabled())
		if (logger.isDebugEnabled())
			cv = new TraceClassVisitor(cv, new PrintWriter(System.out));

		/*
		if (Properties.TT && classNameWithDots.startsWith(Properties.CLASS_PREFIX)) {
			ClassNode cn = new ClassNode();
			reader.accept(cn, ClassReader.SKIP_FRAMES); //  | ClassReader.SKIP_DEBUG
			BooleanTestabilityPlaceholderTransformer.transform(cn);
			cv = cn;
		}
		*/
		// Apply transformations to class under test and its owned
		// classes
		if (isTargetClassName(classNameWithDots)) {
			logger.debug("Applying target transformation");
			// Print out bytecode if debug is enabled
			cv = new AccessibleClassAdapter(cv, className);
			// cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
			// cv = new CheckClassAdapter(cv, true);
			// cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
			for (ClassAdapterFactory factory : externalPostVisitors) {
				cv = factory.getVisitor(cv, className);
			}
			cv = new ExecutionPathClassAdapter(cv, className);
			cv = new CFGClassAdapter(cv, className);

			for (ClassAdapterFactory factory : externalPreVisitors) {
				cv = factory.getVisitor(cv, className);
			}

		} else {
			logger.debug("Not applying target transformation");
			cv = new YieldAtLineNumberClassAdapter(cv);

			if (Properties.MAKE_ACCESSIBLE) {
				// Convert protected/default access to public access
				cv = new AccessibleClassAdapter(cv, className);
			}
			if (Properties.TT && classNameWithDots.startsWith(Properties.CLASS_PREFIX)) {
				cv = new CFGClassAdapter(cv, className);
			}
		}

		// Collect constant values for the value pool
		cv = new PrimitiveClassAdapter(cv, className);

		// If we need to reset static constructors, make them
		// explicit methods
		if (Properties.STATIC_HACK) {
			cv = new StaticInitializationClassAdapter(cv, className);
		}

		//		if (classNameWithDots.equals(Properties.TARGET_CLASS)) {
		if (classNameWithDots.startsWith(Properties.PROJECT_PREFIX)
		        || (!Properties.TARGET_CLASS_PREFIX.isEmpty() && classNameWithDots.startsWith(Properties.TARGET_CLASS_PREFIX))) {
			ClassNode cn = new ClassNode();
			reader.accept(cn, ClassReader.SKIP_FRAMES); // | ClassReader.SKIP_DEBUG); //  | ClassReader.SKIP_DEBUG
			ComparisonTransformation cmp = new ComparisonTransformation(cn);
			if (isTargetClassName(classNameWithDots)
			        || shouldTransform(classNameWithDots))
				cn = cmp.transform();

			if (Properties.STRING_REPLACEMENT) {
				StringTransformation st = new StringTransformation(cn);
				if (isTargetClassName(classNameWithDots)
				        || shouldTransform(classNameWithDots))
					cn = st.transform();

			}

			if (shouldTransform(classNameWithDots)) {
				logger.info("Testability Transforming " + className);
				ContainerTransformation ct = new ContainerTransformation(cn);
				//if (isTargetClassName(classNameWithDots))
				cn = ct.transform();

				//TestabilityTransformation tt = new TestabilityTransformation(cn);
				BooleanTestabilityTransformation tt = new BooleanTestabilityTransformation(
				        cn);
				// cv = new TraceClassVisitor(writer, new
				// PrintWriter(System.out));
				//cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
				//cv = new CheckClassAdapter(cv);
				try {
					//tt.transform().accept(cv);
					//if (isTargetClassName(classNameWithDots))
					cn = tt.transform();
				} catch (Throwable t) {
					logger.info("1 Error: " + t);
					t.printStackTrace();
					System.exit(0);
				}

				logger.info("Testability Transformation done: " + className);
			}
			cn.accept(cv);

		} else {
			reader.accept(cv, ClassReader.SKIP_FRAMES); // | ClassReader.SKIP_DEBUG); //  | ClassReader.SKIP_DEBUG
		}

		// Print out bytecode if debug is enabled
		// if(logger.isDebugEnabled())
		// cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
		return writer.toByteArray();
	}

}
