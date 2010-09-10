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

import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.BytecodeTasks;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.removeSystemExit.RemoveSystemExitTransformer;
import de.unisb.cs.st.javalanche.mutation.javaagent.MutationForRun;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.MutationDecision;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.MutationDecisionFactory;
import de.unisb.cs.st.javalanche.mutation.mutationPossibilities.MutationPossibilityCollector;
import de.unisb.cs.st.javalanche.mutation.properties.MutationProperties;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.results.Mutation.MutationType;
import de.unisb.cs.st.javalanche.mutation.results.persistence.QueryManager;

public class HOMFileTransformer  implements ClassFileTransformer  {

	protected static Logger logger = Logger
			.getLogger(HOMFileTransformer.class);

	static {
		// DB must be loaded before transform method is entered. Otherwise
		// program crashes.
		if (MutationProperties.QUERY_DB_BEFORE_START) {
			Mutation someMutation = new Mutation("SomeMutationToAddToTheDb",
					23, 23, MutationType.ARITHMETIC_REPLACE, false);
			Mutation mutationFromDb = QueryManager
					.getMutationOrNull(someMutation);
			if (mutationFromDb == null) {
				MutationPossibilityCollector mpc1 = new MutationPossibilityCollector();
				mpc1.addPossibility(someMutation);
				mpc1.toDB();
			}
		}

	}

	private static HOMTransformer mutationTransformer = new HOMTransformer();

//	protected static MutationForRun mm = MutationForRun.getAllMutants();
	protected static MutationForRun mm = MutationForRun.getFromDefaultLocation();

	private static Collection<String> classesToMutate = mm.getClassNames();

	private static RemoveSystemExitTransformer systemExitTransformer = new RemoveSystemExitTransformer();
	
	static {
		logger.info("Loading HOMFileTransformer");
		logger.info("Classes to mutate:");
		for(String classname : classesToMutate) {
			logger.info("  "+classname);
		}
	}

	private static MutationDecision mutationDecision = MutationDecisionFactory
			.getStandardMutationDecision(classesToMutate);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader
	 * , java.lang.String, java.lang.Class, java.security.ProtectionDomain,
	 * byte[])
	 */
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className != null) {
			try {
				String classNameWithDots = className.replace('/', '.');
				// logger.info(className + " is passed to transformer");
				if (mutationDecision.shouldBeHandled(classNameWithDots)) {
					logger.debug("Removing calls to System.exit() from class: "
							+ classNameWithDots);
					classfileBuffer = systemExitTransformer
							.transformBytecode(classfileBuffer);
					
					ClassReader reader = new ClassReader(classfileBuffer);
					ClassWriter writer = new ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);
					ClassVisitor cv = writer;
					//cv = new ExecutionPathClassAdapter(cv, className);
					reader.accept(cv, ClassReader.SKIP_FRAMES);
					classfileBuffer = writer.toByteArray();
					
				}
				if (BytecodeTasks.shouldIntegrate(classNameWithDots)) {
					classfileBuffer = BytecodeTasks.integrateTestSuite(
							classfileBuffer, classNameWithDots);
				}
				
				
				
				if (mutationDecision.shouldBeHandled(classNameWithDots)) {
					logger.info("Transforming: " + classNameWithDots);
					byte[] transformedBytecode = null;
					try {
						mutationTransformer.className = classNameWithDots;
						transformedBytecode = mutationTransformer
								.transformBytecode(classfileBuffer);
					} catch (Exception e) {
						logger.info("Exception thrown: " + e);
						e.printStackTrace();
					}
					logger.debug("Class transformed: " + classNameWithDots);
					
					/* FIXME: Doesn't work with ASM 3.2
					String checkClass = checkClass(transformedBytecode);
					if (checkClass != null && checkClass.length() > 0) {
						logger.warn("Check of class failed: " + checkClass);
					}
					*/
					
					return transformedBytecode;
				}
			} catch (Throwable t) {
				logger.fatal(
						"Transformation of class " + className + " failed", t);
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

	/**
	 * Checks if the given class name equals to the test suite property.
	 * 
	 * @param classNameWithDots
	 *            the class name to check
	 * @return true, if
	 */
	public static boolean compareWithSuiteProperty(String classNameWithDots) {
		String testSuiteName = System
				.getProperty(MutationProperties.TEST_SUITE_KEY);
		return testSuiteName != null
				&& classNameWithDots.contains(testSuiteName);

	}

}
