/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcarver.extraction;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ResourceList;
import org.evosuite.testcarver.instrument.Instrumenter;
import org.evosuite.runtime.instrumentation.JSRInlinerClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 */
public class CarvingClassLoader extends ClassLoader {
	private final static Logger logger = LoggerFactory.getLogger(CarvingClassLoader.class);
	private final Instrumenter instrumenter = new Instrumenter();
	private final ClassLoader classLoader;
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

	/**
	 * <p>
	 * Constructor for InstrumentingClassLoader.
	 * </p>
	 */
	public CarvingClassLoader() {
		classLoader = CarvingClassLoader.class.getClassLoader();
	}



	/**
	 * Check if we can instrument the given class
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean checkIfCanInstrument(String className) {
		for (String s : getPackagesShouldNotBeInstrumented()) {
			if (className.startsWith(s)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * getPackagesShouldNotBeInstrumented
	 * </p>
	 * 
	 * @return the names of class packages EvoSuite is not going to instrument
	 */
	public static String[] getPackagesShouldNotBeInstrumented() {
		//explicitly blocking client projects such as specmate is only a
		//temporary solution, TODO allow the user to specify 
		//packages that should not be instrumented
		return new String[] { "java.", "javax.", "sun.", "org.evosuite", "org.exsyst",
				"de.unisb.cs.st.testcarver", "de.unisb.cs.st.evosuite",  "org.uispec4j", 
				"de.unisb.cs.st.specmate", "org.xml", "org.w3c",
				"testing.generation.evosuite", "com.yourkit", "com.vladium.emma.", "daikon.",
				// Need to have these in here to avoid trouble with UnsatisfiedLinkErrors on Mac OS X and Java/Swing apps
				"apple.", "com.apple.", "com.sun", "org.junit", "junit.framework",
				"org.apache.xerces.dom3", "de.unisl.cs.st.bugex", 
				"corina.cross.Single" // I really don't know what is wrong with this class, but we need to exclude it 
		};
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (!checkIfCanInstrument(name)) {
			Class<?> result = findLoadedClass(name);
			if (result != null) {
				return result;
			}
			result = classLoader.loadClass(name);
			return result;

		} else {
			Class<?> result = findLoadedClass(name);
			if (result != null) {
				return result;
			} else {

				result = classes.get(name);
				if (result != null) {
					return result;
				} else {
					logger.info("Seeing class for first time: " + name);
					Class<?> instrumentedClass = instrumentClass(name);
					return instrumentedClass;
				}
			}

		}
	}


	private Class<?> instrumentClass(String fullyQualifiedTargetClass)
			throws ClassNotFoundException {
		logger.info("Instrumenting class '" + fullyQualifiedTargetClass + "'.");

		try {
			String className = fullyQualifiedTargetClass.replace('.', '/');

			InputStream is = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getClassAsStream(className);
			if(is == null){
				throw new ClassNotFoundException("Class '" + className + ".class"
						+ "' should be in target project, but could not be found!");
			}

			ClassReader reader = new ClassReader(is);
			ClassNode classNode = new ClassNode();
			reader.accept(classNode, ClassReader.SKIP_FRAMES);
			instrumenter.transformClassNode(classNode, className);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			classNode.accept(new JSRInlinerClassVisitor(writer));
			//classNode.accept(writer);
			byte[] byteBuffer = writer.toByteArray();
			Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0,
					byteBuffer.length);
			if(Modifier.isPrivate(result.getModifiers())) {
				logger.info("REPLACING PRIVATE CLASS "+fullyQualifiedTargetClass);
				result = super.loadClass(fullyQualifiedTargetClass);
			}
			classes.put(fullyQualifiedTargetClass, result);
			logger.info("Keeping class: " + fullyQualifiedTargetClass);
			return result;
		} catch (Throwable t) {
			logger.info("Error: " + t);
			for(StackTraceElement e : t.getStackTrace()) {
				logger.info(e.toString());
			}
			throw new ClassNotFoundException(t.getMessage(), t);
		}
	}
}
