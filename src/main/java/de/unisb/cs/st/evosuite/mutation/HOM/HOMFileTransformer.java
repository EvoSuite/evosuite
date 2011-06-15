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

package de.unisb.cs.st.evosuite.mutation.HOM;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.primitives.PrimitiveClassAdapter;
import de.unisb.cs.st.javalanche.mutation.javaagent.MutationsForRun;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.MutationDecision;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.MutationDecisionFactory;

public class HOMFileTransformer implements ClassFileTransformer {

	protected static Logger logger = Logger.getLogger(HOMFileTransformer.class);

	//private final BytecodeTransformer mutationTransformer;
	private static HOMTransformer mutationTransformer = new HOMTransformer();

	public static MutationsForRun mm = MutationsForRun.getFromDefaultLocation();

	private static Collection<String> classesToMutate = mm.getClassNames();

	//private static RemoveSystemExitTransformer systemExitTransformer = new RemoveSystemExitTransformer();

	static {
		logger.info("Loading HOMFileTransformer");
		classesToMutate.add(Properties.TARGET_CLASS);
		//logger.info("Classes to mutate:");
		//for(String classname : classesToMutate) {
		//	logger.info("  "+classname);
		//}
	}

	private static MutationDecision mutationDecision = MutationDecisionFactory.getStandardMutationDecision(classesToMutate);

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
		if (className != null) {
			try {
				String classNameWithDots = className.replace('/', '.');

				if (classNameWithDots.startsWith(Properties.PROJECT_PREFIX)) {
					ClassReader reader = new ClassReader(classfileBuffer);
					ClassWriter writer = new ClassWriter(
					        org.objectweb.asm.ClassWriter.COMPUTE_MAXS);
					ClassVisitor cv = writer;
					cv = new PrimitiveClassAdapter(cv, className);
					reader.accept(cv, ClassReader.SKIP_FRAMES);
					classfileBuffer = writer.toByteArray();
				}

				// logger.info(className + " is passed to transformer");
				//if (mutationDecision.shouldBeHandled(classNameWithDots)) {
				//	logger.debug("Removing calls to System.exit() from class: "
				//			+ classNameWithDots);
				//	classfileBuffer = systemExitTransformer
				//			.transformBytecode(classfileBuffer);
				//}
				/*
				if (BytecodeTasks.shouldIntegrate(classNameWithDots)) {
					classfileBuffer = BytecodeTasks.integrateTestSuite(
							classfileBuffer, classNameWithDots);
				}
				*/
				if (mutationDecision.shouldBeHandled(classNameWithDots)) {
					logger.info("Transforming: " + classNameWithDots);
					byte[] transformedBytecode = null;
					try {
						transformedBytecode = mutationTransformer.transformBytecode(classfileBuffer);

					} catch (Exception e) {
						logger.info("Exception thrown: " + e);
						e.printStackTrace();
					}
					/*
					AsmUtil.checkClass2(transformedBytecode);
					logger.debug("Class transformed: " + classNameWithDots);
					String checkClass = AsmUtil.checkClass(transformedBytecode);
					if (checkClass != null && checkClass.length() > 0) {
						logger.warn("Check of class failed: "
								+ classNameWithDots);
						logger.warn("Message: " + checkClass);
					}
					*/
					return transformedBytecode;
				}
			} catch (Throwable t) {
				logger.fatal("Transformation of class " + className + " failed", t);
				StringWriter writer = new StringWriter();
				t.printStackTrace(new PrintWriter(writer));
				logger.fatal(writer.getBuffer().toString());
				t.printStackTrace();
				System.exit(0);
				// throw new RuntimeException(e.getMessage());
			}
		}
		return classfileBuffer;
	}

	/*
	private String checkClass(byte[] transformedBytecode) {
		ClassReader cr = new ClassReader(transformedBytecode);
		StringWriter sw = new StringWriter();
		CheckClassAdapter check = new CheckClassAdapter(new ClassWriter(
				ClassWriter.COMPUTE_MAXS));
		cr.accept(check, ClassReader.EXPAND_FRAMES);
		// cr.accept(check,0);
		// CheckClassAdapter.verify(cr, false, new PrintWriter(sw));
		return sw.toString();
	}
	*/

}
