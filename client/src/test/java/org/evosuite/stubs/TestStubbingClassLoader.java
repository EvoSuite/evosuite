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
package org.evosuite.stubs;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.evosuite.PackageInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

public class TestStubbingClassLoader extends ClassLoader {

	private final ClassLoader classLoader;
	
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	
	public TestStubbingClassLoader() {
		classLoader = TestStubbingClassLoader.class.getClassLoader();
	}
	
	public static String[] getPackagesShouldNotBeInstrumented() {
		//explicitly blocking client projects such as specmate is only a
		//temporary solution, TODO allow the user to specify 
		//packages that should not be instrumented
		return new String[] { "java.", "javax.", "sun.", PackageInfo.getEvoSuitePackage(), "org.exsyst",
					          "de.unisb.cs.st.testcarver", "de.unisb.cs.st.evosuite",  "org.uispec4j", 
					          "de.unisb.cs.st.specmate", "org.xml", "org.w3c",
					          "testing.generation.evosuite", "com.yourkit", "com.vladium.emma.", "daikon.",
					          // Need to have these in here to avoid trouble with UnsatisfiedLinkErrors on Mac OS X and Java/Swing apps
					          "apple.", "com.apple.", "com.sun", "org.junit", "junit.framework",
					          "org.apache.xerces.dom3", "de.unisl.cs.st.bugex", 
					          "corina.cross.Single" // I really don't know what is wrong with this class, but we need to exclude it 
		};
	}
	
	public static boolean checkIfCanInstrument(String className) {
		for (String s : getPackagesShouldNotBeInstrumented()) {
			if (className.startsWith(s)) {
				return false;
			}
		}
		return true;
	}
	
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
					Class<?> instrumentedClass = instrumentClass(name);
					return instrumentedClass;
				}
			}

		}
	}
	
	private Class<?> instrumentClass(String fullyQualifiedTargetClass)
	        throws ClassNotFoundException {
		try {
			String className = fullyQualifiedTargetClass.replace('.', '/');

			InputStream is = ClassLoader.getSystemResourceAsStream(className + ".class");
			if (is == null) {
				throw new ClassNotFoundException("Class '" + className + ".class"
					        + "' should be in target project, but could not be found!");
			}
			ClassReader reader = new ClassReader(is);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			TraceClassVisitor tc = new TraceClassVisitor(writer, new PrintWriter(System.err));
			// CheckClassAdapter ca = new CheckClassAdapter(tc);
			StubClassVisitor visitor = new StubClassVisitor(tc, fullyQualifiedTargetClass);
			reader.accept(visitor, ClassReader.SKIP_FRAMES);

			byte[] byteBuffer = writer.toByteArray();
			Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0,
			                              byteBuffer.length);
			classes.put(fullyQualifiedTargetClass, result);
			return result;
		} catch (Throwable t) {
			throw new ClassNotFoundException(t.getMessage(), t);
		}
	}
}
