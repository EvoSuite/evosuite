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
package org.evosuite.javaagent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.evosuite.Properties;
import org.evosuite.utils.ResourceList;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <em>Note:</em> Do not inadvertently use multiple instances of this class in
 * the application! This may lead to hard to detect and debug errors. Yet this
 * class cannot be an singleton as it might be necessary to do so...
 * 
 * @author roessler
 * @author Gordon Fraser
 */
public class InstrumentingClassLoader extends ClassLoader {
	private final static Logger logger = LoggerFactory.getLogger(InstrumentingClassLoader.class);
	private final BytecodeInstrumentation instrumentation;
	private final ClassLoader classLoader;
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

	public InstrumentingClassLoader() {
		this(new BytecodeInstrumentation());
		setClassAssertionStatus(Properties.TARGET_CLASS, true);
	}

	public InstrumentingClassLoader(BytecodeInstrumentation instrumentation) {
		super(InstrumentingClassLoader.class.getClassLoader());
		classLoader = InstrumentingClassLoader.class.getClassLoader();
		this.instrumentation = instrumentation;
	}

	/**
	 * Check if we can instrument the given class
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
	 * 
	 * @return the names of class packages EvoSuite is not going to instrument
	 */
	public static String[] getPackagesShouldNotBeInstrumented() {
		//explicitly blocking client projects such as specmate is only a
		//temporary solution, TODO allow the user to specify 
		//packages that should not be instrumented
		return new String[] { "java.", "javax.", "sun.", "org.evosuite",
		        "org.specmate", "org.xml", "org.w3c",
		        "testing.generation.evosuite", "com.yourkit",
		        // Need to have these in here to avoid trouble with UnsatisfiedLinkErrors on Mac OS X and Java/Swing apps
		        "apple.", "com.apple.", "org.junit", "junit.framework"
		};
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		//if (instrumentation.isTargetProject(name)) {
		// if (TestCluster.isTargetClassName(name)) {
		if (!checkIfCanInstrument(name)
		        || (Properties.VIRTUAL_FS && (name.startsWith("org.apache.commons.vfs") || name.startsWith("org.apache.commons.logging")))) {
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
					Class<?> instrumentedClass = null;
					//LoggingUtils.muteCurrentOutAndErrStream();
					try {
						instrumentedClass = instrumentClass(name);
					} finally {
						//LoggingUtils.restorePreviousOutAndErrStream();
					}
					return instrumentedClass;
				}
			}

		}
		//} else {
		//	logger.trace("Not instrumenting: " + name);
		//}
		/*
		Class<?> result = findLoadedClass(name);
		if (result != null) {
		return result;
		}
		result = classLoader.loadClass(name);
		return result;
		*/
	}

	private InputStream findTargetResource(String name) throws FileNotFoundException {
		Pattern pattern = Pattern.compile(name);
		Collection<String> resources = ResourceList.getResources(pattern);
		if (resources.isEmpty())
			throw new FileNotFoundException(name);
		else
			return new FileInputStream(resources.iterator().next());
	}

	private Class<?> instrumentClass(String fullyQualifiedTargetClass)
	        throws ClassNotFoundException {
		logger.info("Instrumenting class '" + fullyQualifiedTargetClass + "'.");
		try {
			String className = fullyQualifiedTargetClass.replace('.', '/');
			InputStream is = ClassLoader.getSystemResourceAsStream(className + ".class");
			if (is == null) {
				try {
					is = findTargetResource(".*" + className + ".class");
				} catch (FileNotFoundException e) {
					throw new ClassNotFoundException("Class '" + className + ".class"
					        + "' should be in target project, but could not be found!");
				}
			}
			byte[] byteBuffer = instrumentation.transformBytes(className,
			                                                   new ClassReader(is));
			Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0,
			                              byteBuffer.length);
			classes.put(fullyQualifiedTargetClass, result);
			logger.info("Keeping class: " + fullyQualifiedTargetClass);
			return result;
		} catch (Exception e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		}
	}

}
