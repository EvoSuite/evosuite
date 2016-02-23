/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.instrumentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.runtime.util.Inputs;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instrumenting class loader used in special cases in the generated JUnit tests
 * when Java Agent is not used
 */
public class EvoClassLoader extends ClassLoader {
	private final static Logger logger = LoggerFactory.getLogger(EvoClassLoader.class);
	private final RuntimeInstrumentation instrumentation;
	private final ClassLoader classLoader;
	private final Map<String, Class<?>> classes = new HashMap<>();
	private final Set<String> skipInstrumentationForPrefix = new HashSet<>();

	public EvoClassLoader() {
		this(new RuntimeInstrumentation());
	}

	/**
	 * <p>
	 * Constructor for InstrumentingClassLoader.
	 * </p>
	 * 
	 * @param instrumentation
	 *            a {@link org.evosuite.runtime.instrumentation.RuntimeInstrumentation}
	 *            object.
	 */
	public EvoClassLoader(RuntimeInstrumentation instrumentation) {
		super(EvoClassLoader.class.getClassLoader());
		classLoader = EvoClassLoader.class.getClassLoader();
		this.instrumentation = instrumentation;
	}

	public void skipInstrumentation(String prefix) throws IllegalArgumentException{
		Inputs.checkNull(prefix);
		skipInstrumentationForPrefix.add(prefix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if ("<evosuite>".equals(name))
			throw new ClassNotFoundException();

		//first check if already loaded
		if (!RuntimeInstrumentation.checkIfCanInstrument(name)) {
			Class<?> result = findLoadedClass(name);
			if (result != null) {
				return result;
			}
			result = classLoader.loadClass(name);
			return result;
		}
		
		Class<?> result = classes.get(name);
		if (result != null) {
			return result;
		} else {
			logger.info("Seeing class for first time: " + name);
			Class<?> instrumentedClass = instrumentClass(name);
			return instrumentedClass;
		}

	}


	private Class<?> instrumentClass(String fullyQualifiedTargetClass)
	        throws ClassNotFoundException {
		logger.info("Instrumenting class '" + fullyQualifiedTargetClass + "'.");
		
		InputStream is = null;
		try {
			String className = fullyQualifiedTargetClass.replace('.', '/');
			is = classLoader.getResourceAsStream(className + ".class");
			
			if (is == null) {
				throw new ClassNotFoundException("Class '" + className + ".class"
						+ "' should be in target project, but could not be found!");
			}
			boolean shouldSkip = skipInstrumentationForPrefix.stream().anyMatch(s -> fullyQualifiedTargetClass.startsWith(s));
			byte[] byteBuffer = instrumentation.transformBytes(this, className,
			                                                   new ClassReader(is), shouldSkip);
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
	
	public RuntimeInstrumentation getInstrumentation() {
		return instrumentation;
	}

	
}
