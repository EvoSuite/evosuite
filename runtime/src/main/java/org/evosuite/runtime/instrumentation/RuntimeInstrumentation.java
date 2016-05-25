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

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.util.ComputeClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for the bytecode instrumentation
 * needed for the generated JUnit test cases.
 *
 * <p>
 * Note: the instrumentation will be part of the final JUnit tests and, as such, we should
 * only keep the instrumentation that affect the functional behavior (so, no branch distances, etc).
 *
 * Created by arcuri on 6/11/14.
 */
public class RuntimeInstrumentation {

	private static Logger logger = LoggerFactory.getLogger(RuntimeInstrumentation.class);

	/**
	 * If we are re-instrumenting a class, then we cannot change its
	 * signature: eg add new methods
	 *
	 * TODO: remove once we fix instrumentation
	 */
	private volatile boolean retransformingMode;

	/**
	 * This should ONLY be set by SystemTest
	 */
	private static boolean avoidInstrumentingShadedClasses = false;

	public RuntimeInstrumentation(){
		retransformingMode = false;
	}

	public void setRetransformingMode(boolean on){
		retransformingMode = on;
	}

	/**
	 * WARN: This should ONLY be called by SystemTest
	 */
	public static void setAvoidInstrumentingShadedClasses(boolean avoidInstrumentingShadedClasses) {
		RuntimeInstrumentation.avoidInstrumentingShadedClasses = avoidInstrumentingShadedClasses;
	}

	public static boolean getAvoidInstrumentingShadedClasses() {
		return RuntimeInstrumentation.avoidInstrumentingShadedClasses;
	}

	public static boolean checkIfCanInstrument(String className) {
		for (String s : ExcludedClasses.getPackagesShouldNotBeInstrumented()) {
			if (className.startsWith(s)) {
				return false;
			}
		}

		if(className.contains("EnhancerByMockito")){
			//very special case, as Mockito will create classes on the fly
			return false;
		}

		return true;
	}


	public byte[] transformBytes(ClassLoader classLoader, String className,
			ClassReader reader, boolean skipInstrumentation) {

		String classNameWithDots = className.replace('/', '.');

		if (!checkIfCanInstrument(classNameWithDots)) {
			throw new IllegalArgumentException("Should not transform a shared class ("
					+ classNameWithDots + ")! Load by parent (JVM) classloader.");
		}

		int asmFlags = ClassWriter.COMPUTE_FRAMES;
		ClassWriter writer = new ComputeClassWriter(asmFlags);

		ClassVisitor cv = writer;

		if(!skipInstrumentation) {
			if (RuntimeSettings.resetStaticState && !retransformingMode) {
			/*
			 * FIXME: currently reset does add a new method, but that does no work
			 * when retransformingMode :(
			 */
				CreateClassResetClassAdapter resetClassAdapter = new CreateClassResetClassAdapter(cv, className, true);
				cv = resetClassAdapter;
			}

			if (RuntimeSettings.isUsingAnyMocking()) {
				cv = new MethodCallReplacementClassAdapter(cv, className, !retransformingMode);
			}

			cv = new KillSwitchClassAdapter(cv);

			cv = new RemoveFinalClassAdapter(cv);

			if (RuntimeSettings.maxNumberOfIterationsPerLoop >= 0) {
				cv = new LoopCounterClassAdapter(cv);
			}
		}
		ClassNode cn = new AnnotatedClassNode();

		int readFlags = ClassReader.SKIP_FRAMES;
		reader.accept(cn, readFlags);


		cv = new JSRInlinerClassVisitor(cv);

		try {
			cn.accept(cv);
		} catch (Throwable ex) {
			logger.error("Error while instrumenting class "+className+": "+ex.getMessage(),ex);
		}

		return writer.toByteArray();
	}

}