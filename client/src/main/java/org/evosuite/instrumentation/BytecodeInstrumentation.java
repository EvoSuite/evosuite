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
package org.evosuite.instrumentation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.classpath.ResourceList;
import org.evosuite.graphs.cfg.CFGClassAdapter;
import org.evosuite.runtime.instrumentation.AnnotatedClassNode;
import org.evosuite.runtime.instrumentation.CreateClassResetClassAdapter;
import org.evosuite.runtime.instrumentation.MethodCallReplacementClassAdapter;
import org.evosuite.runtime.instrumentation.RuntimeInstrumentation;
import org.evosuite.runtime.reset.ResetManager;
import org.evosuite.seeding.PrimitiveClassAdapter;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcarver.instrument.Instrumenter;
import org.evosuite.testcarver.instrument.TransformerUtil;
import org.evosuite.runtime.util.ComputeClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bytecode transformer - transforms bytecode depending on package and
 * whether it is the class under test
 * 
 * @author Gordon Fraser
 */
public class BytecodeInstrumentation {

	private static Logger logger = LoggerFactory.getLogger(BytecodeInstrumentation.class);

	private static List<ClassAdapterFactory> externalPreVisitors = new ArrayList<ClassAdapterFactory>();

	private static List<ClassAdapterFactory> externalPostVisitors = new ArrayList<ClassAdapterFactory>();

	private final Instrumenter testCarvingInstrumenter;

	/**
	 * <p>
	 * Constructor for BytecodeInstrumentation.
	 * </p>
	 */
	public BytecodeInstrumentation() {
		this.testCarvingInstrumenter = new Instrumenter();
	}

	/**
	 * <p>
	 * addClassAdapter
	 * </p>
	 * 
	 * @param factory
	 *            a {@link org.evosuite.instrumentation.ClassAdapterFactory} object.
	 */
	public static void addClassAdapter(ClassAdapterFactory factory) {
		externalPostVisitors.add(factory);
	}

	/**
	 * <p>
	 * addPreClassAdapter
	 * </p>
	 * 
	 * @param factory
	 *            a {@link org.evosuite.instrumentation.ClassAdapterFactory} object.
	 */
	public static void addPreClassAdapter(ClassAdapterFactory factory) {
		externalPreVisitors.add(factory);
	}

	

	private static String[] getEvoSuitePackages() {
		return new String[] {"org.evosuite", 
				"org.exsyst", 
				"de.unisb.cs.st.testcarver", 
				"de.unisb.cs.st.evosuite",
				"testing.generation.evosuite",
				"de.unisl.cs.st.bugex" };
	}

	/**
	 * Check if we can instrument the given class
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean checkIfCanInstrument(String className) {
		return RuntimeInstrumentation.checkIfCanInstrument(className);
	}
	
	/**
	 * Check if we the class belongs to an EvoSuite package
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean checkIfEvoSuitePackage(String className) {
		for (String s : BytecodeInstrumentation.getEvoSuitePackages()) {
			if (className.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * isTargetProject
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isTargetProject(String className) {
		return (className.startsWith(Properties.PROJECT_PREFIX) || (!Properties.TARGET_CLASS_PREFIX.isEmpty() && className.startsWith(Properties.TARGET_CLASS_PREFIX)))
		        && !className.startsWith("java.")
		        && !className.startsWith("sun.")
		        && !className.startsWith("org.evosuite")
		        && !className.startsWith("org.exsyst")
		        && !className.startsWith("de.unisb.cs.st.evosuite")
		        && !className.startsWith("de.unisb.cs.st.specmate")
		        && !className.startsWith("javax.")
		        && !className.startsWith("org.xml")
		        && !className.startsWith("org.w3c")
		        && !className.startsWith("apple.")
		        && !className.startsWith("com.apple.")
		        && !className.startsWith("daikon.");
	}

	/**
	 * <p>
	 * shouldTransform
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean shouldTransform(String className) {
		if (!Properties.TT)
			return false;
		switch (Properties.TT_SCOPE) {
		case ALL:
			logger.info("Allowing transformation of " + className);
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
		logger.info("Preventing transformation of " + className);
		return false;
	}

	private boolean isTargetClassName(String className) {
		// TODO: Need to replace this in the long term
		return TestCluster.isTargetClassName(className);
	}

	/**
	 * <p>
	 * transformBytes
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param reader
	 *            a {@link org.objectweb.asm.ClassReader} object.
	 * @return an array of byte.
	 */
	public byte[] transformBytes(ClassLoader classLoader, String className,
	        ClassReader reader) {
		int readFlags = ClassReader.SKIP_FRAMES;

		if (Properties.INSTRUMENTATION_SKIP_DEBUG)
			readFlags |= ClassReader.SKIP_DEBUG;

		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);

		if (!checkIfCanInstrument(classNameWithDots)) {
			throw new RuntimeException("Should not transform a shared class ("
			        + classNameWithDots + ")! Load by parent (JVM) classloader.");
		}

		TransformationStatistics.reset();

		/*
		 *  To use COMPUTE_FRAMES we need to remove JSR commands.
		 *  Therefore, we have a JSRInlinerAdapter in NonTargetClassAdapter
		 *  as well as CFGAdapter.
		 */
		int asmFlags = ClassWriter.COMPUTE_FRAMES;
		ClassWriter writer = new ComputeClassWriter(asmFlags);

		ClassVisitor cv = writer;
		if (logger.isDebugEnabled()) {
			cv = new TraceClassVisitor(cv, new PrintWriter(System.err));
		}

		if (Properties.RESET_STATIC_FIELDS) {
			cv = new PutStaticClassAdapter(cv, className);
		}

		if (Properties.PURE_INSPECTORS) {
			CheapPurityAnalyzer purityAnalyzer = CheapPurityAnalyzer.getInstance();
			
			cv = new PurityAnalysisClassVisitor(cv, className, purityAnalyzer);
		}

		// Apply transformations to class under test and its owned
		// classes
		if (DependencyAnalysis.shouldAnalyze(classNameWithDots)) {
			logger.debug("Applying target transformation to class " + classNameWithDots);
			// Print out bytecode if debug is enabled
			if (!Properties.TEST_CARVING && Properties.MAKE_ACCESSIBLE) {
				cv = new AccessibleClassAdapter(cv, className);
			}

			for (ClassAdapterFactory factory : externalPostVisitors) {
				cv = factory.getVisitor(cv, className);
			}

			cv = new ExecutionPathClassAdapter(cv, className);

			cv = new CFGClassAdapter(classLoader, cv, className);

			if (Properties.ERROR_BRANCHES) {
				cv = new ErrorConditionClassAdapter(cv, className);
			}
			//cv = new BoundaryValueClassAdapter(cv, className);

			for (ClassAdapterFactory factory : externalPreVisitors) {
				cv = factory.getVisitor(cv, className);
			}

		} else  {
			logger.debug("Not applying target transformation");
			cv = new NonTargetClassAdapter(cv, className);

			if (Properties.MAKE_ACCESSIBLE) {
				// Convert protected/default access to public access
				cv = new AccessibleClassAdapter(cv, className);
			}
			// If we are doing testability transformation on all classes we need to create the CFG first
			if (Properties.TT && classNameWithDots.startsWith(Properties.CLASS_PREFIX)) {
				cv = new CFGClassAdapter(classLoader, cv, className);
			}
		}

		// Collect constant values for the value pool
		cv = new PrimitiveClassAdapter(cv, className);

		// If we need to reset static constructors, make them
		// explicit methods
		if (Properties.RESET_STATIC_FIELDS) {
			// Create a __STATIC_RESET() cloning the original <clinit> method or create one by default
			CreateClassResetClassAdapter resetClassAdapter = new CreateClassResetClassAdapter(cv, className);
			if (ResetManager.getInstance().getResetFinalFields()) {
				resetClassAdapter.setRemoveFinalModifierOnStaticFields(true);
			} else {
				resetClassAdapter.setRemoveFinalModifierOnStaticFields(false);
			}
			cv = resetClassAdapter;
			// Add a callback before leaving the <clinit> method

			ExitClassInitAdapter exitClassInitAdapter = new ExitClassInitAdapter(cv, className);
			cv = exitClassInitAdapter;
		}

		// Replace calls to System.exit, Random.*, and System.currentTimeMillis
		// and/or replace calls to FileInputStream.available and FIS.skip
		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS) {
			cv = new MethodCallReplacementClassAdapter(cv, className);
		}


		// Testability Transformations
			if (classNameWithDots.startsWith(Properties.PROJECT_PREFIX)
			        || (!Properties.TARGET_CLASS_PREFIX.isEmpty() && classNameWithDots.startsWith(Properties.TARGET_CLASS_PREFIX))
			        || shouldTransform(classNameWithDots)) {
				ClassNode cn = new AnnotatedClassNode();
				reader.accept(cn, readFlags);
				logger.info("Starting transformation of " + className);
	
				if (Properties.STRING_REPLACEMENT) {
					StringTransformation st = new StringTransformation(cn);
					if (isTargetClassName(classNameWithDots)
					        || shouldTransform(classNameWithDots))
						cn = st.transform();
	
				}
				ComparisonTransformation cmp = new ComparisonTransformation(cn);
				if (isTargetClassName(classNameWithDots)
				        || shouldTransform(classNameWithDots)) {
					cn = cmp.transform();
					ContainerTransformation ct = new ContainerTransformation(cn);
					//if (isTargetClassName(classNameWithDots))
					cn = ct.transform();
				}
	
				if (shouldTransform(classNameWithDots)) {
					logger.info("Testability Transforming " + className);
	
					//TestabilityTransformation tt = new TestabilityTransformation(cn);
					BooleanTestabilityTransformation tt = new BooleanTestabilityTransformation(
					        cn, classLoader);
					// cv = new TraceClassVisitor(writer, new
					// PrintWriter(System.out));
					//cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
					//cv = new CheckClassAdapter(cv);
					try {
						//tt.transform().accept(cv);
						//if (isTargetClassName(classNameWithDots))
						cn = tt.transform();
					} catch (Throwable t) {
						throw new Error(t);
					}
	
					logger.info("Testability Transformation done: " + className);
				}
	
				//----- 
				cn.accept(cv);
	
				if (Properties.TEST_CARVING) {
					if (TransformerUtil.isClassConsideredForInstrumentation(className)) {
						final ClassReader cr = new ClassReader(writer.toByteArray());
						final ClassNode cn2 = new ClassNode();
						cr.accept(cn2, ClassReader.EXPAND_FRAMES);
	
						this.testCarvingInstrumenter.transformClassNode(cn2, className);
						final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
						cn2.accept(cw);
	
						if (logger.isDebugEnabled()) {
							final StringWriter sw = new StringWriter();
							cn2.accept(new TraceClassVisitor(new PrintWriter(sw)));
							logger.debug("test carving instrumentation result:\n{}", sw);
						}
	
						return cw.toByteArray();
					}
				}
			} else {
				reader.accept(cv, readFlags);
			}

			
		// Print out bytecode if debug is enabled
		// if(logger.isDebugEnabled())
		// cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
		return writer.toByteArray();
	}

}
