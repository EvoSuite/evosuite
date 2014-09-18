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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.classpath.ResourceList;
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

	/**
	 * <p>
	 * Constructor for InstrumentingClassLoader.
	 * </p>
	 */
	public InstrumentingClassLoader() {
		this(new BytecodeInstrumentation());
		setClassAssertionStatus(Properties.TARGET_CLASS, true);
	}

	/**
	 * <p>
	 * Constructor for InstrumentingClassLoader.
	 * </p>
	 * 
	 * @param instrumentation
	 *            a {@link org.evosuite.instrumentation.BytecodeInstrumentation}
	 *            object.
	 */
	public InstrumentingClassLoader(BytecodeInstrumentation instrumentation) {
		super(InstrumentingClassLoader.class.getClassLoader());
		classLoader = InstrumentingClassLoader.class.getClassLoader();
		this.instrumentation = instrumentation;
	}

	public List<String> getViewOfInstrumentedClasses(){
		List<String> list = new ArrayList<>();
		list.addAll(classes.keySet());
		return list;
	}
	
	
	public Class<?> loadClassFromFile(String fullyQualifiedTargetClass, String fileName) throws ClassNotFoundException {

		String className = fullyQualifiedTargetClass.replace('.', '/');
		InputStream is = null;
		try {
			is = new FileInputStream(new File(fileName));
			byte[] byteBuffer = instrumentation.transformBytes(this, className,
			                                                   new ClassReader(is));
			createPackageDefinition(fullyQualifiedTargetClass);
			Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0,
			                              byteBuffer.length);
			classes.put(fullyQualifiedTargetClass, result);
			logger.info("Keeping class: " + fullyQualifiedTargetClass);
			return result;
		} catch (Throwable t) {
			logger.info("Error while loading class: "+t);
			throw new ClassNotFoundException(t.getMessage(), t);
		} finally {
			if(is != null)
				try {
					is.close();
				} catch (IOException e) {
					throw new Error(e);
				}
		}	
	}
	
	/** {@inheritDoc} */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if("<evosuite>".equals(name))
			throw new ClassNotFoundException();
		//if (instrumentation.isTargetProject(name)) {
		// if (TestCluster.isTargetClassName(name)) {
		if (!BytecodeInstrumentation.checkIfCanInstrument(name)
		        //|| (Properties.VIRTUAL_FS && (name.startsWith("org.apache.commons.vfs") || name.startsWith("org.apache.commons.logging")))
		        ) {
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

	private Class<?> instrumentClass(String fullyQualifiedTargetClass)
	        throws ClassNotFoundException {
		logger.info("Instrumenting class '" + fullyQualifiedTargetClass + "'.");
		
		InputStream is = null;
		try {
			String className = fullyQualifiedTargetClass.replace('.', '/');

			is = ResourceList.getClassAsStream(fullyQualifiedTargetClass);
			
			if (is == null) {
				throw new ClassNotFoundException("Class '" + className + ".class"
						+ "' should be in target project, but could not be found!");
			}
			
			byte[] byteBuffer = instrumentation.transformBytes(this, className,
			                                                   new ClassReader(is));
			createPackageDefinition(fullyQualifiedTargetClass);
			Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0,
			                              byteBuffer.length);
			classes.put(fullyQualifiedTargetClass, result);
			logger.info("Keeping class: " + fullyQualifiedTargetClass);
			return result;
		} catch (Throwable t) {
			logger.info("Error while loading class: "+t);
			throw new ClassNotFoundException(t.getMessage(), t);
		} finally {
			if(is != null)
				try {
					is.close();
				} catch (IOException e) {
					throw new Error(e);
				}
		}
	}

	public boolean hasInstrumentedClass(String className) {
		return classes.containsKey(className);
	}

	/**
	 * Before a new class is defined, we need to create a package definition for it
	 * 
	 * @param className
	 */
	private void createPackageDefinition(String className){
		int i = className.lastIndexOf('.');
		if (i != -1) {
		    String pkgname = className.substring(0, i);
		    // Check if package already loaded.
		    Package pkg = getPackage(pkgname);
		    if(pkg==null){
		    		definePackage(pkgname, null, null, null, null, null, null, null);
		    		logger.info("Defined package (3): "+getPackage(pkgname)+", "+getPackage(pkgname).hashCode());
		    }
	    }
	}
	
	public BytecodeInstrumentation getInstrumentation() {
		return instrumentation;
	}

	
}
