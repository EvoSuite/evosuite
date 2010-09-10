/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.string.StringClassAdapter;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.Excludes;


/**
 * @author Gordon Fraser
 *
 */
public class CoverageInstrumentation implements ClassFileTransformer {

	protected static Logger logger = Logger
	.getLogger(CoverageInstrumentation.class);

	//private static RemoveSystemExitTransformer systemExitTransformer = new RemoveSystemExitTransformer();

	private static CoverageTransformer coverageTransformer = new CoverageTransformer();
	
	private static String target_class = Properties.TARGET_CLASS;
	
	protected boolean static_hack = Properties.getPropertyOrDefault("static_hack", false);
	
	static {
		logger.info("Loading CoverageTransformer");
		//logger.info("Classes to mutate:");
	}
	/* (non-Javadoc)
	 * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader, java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
	 */
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		
		if (className != null) {
			try {
				String classNameWithDots = className.replace('/', '.');
				if (!classNameWithDots.startsWith("java2.util2") && (classNameWithDots.startsWith("java")
						|| classNameWithDots.startsWith("sun")
						|| classNameWithDots.startsWith("org.aspectj.org.eclipse")
						|| classNameWithDots.startsWith("org.mozilla.javascript.gen.c"))) {
					return classfileBuffer;
				}
				if (Excludes.getInstance().shouldExclude(classNameWithDots)) {
					return classfileBuffer;
				}
				if (classNameWithDots.startsWith(Properties.PROJECT_PREFIX)) {

					//logger.debug("Removing calls to System.exit() from class: "
					//			+ classNameWithDots);
					//classfileBuffer = systemExitTransformer
					//.transformBytecode(classfileBuffer);

					logger.debug("Transforming: " + classNameWithDots);
					ClassReader reader = new ClassReader(classfileBuffer);
					ClassWriter writer = new ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);

					ClassVisitor cv = writer;
					if(classNameWithDots.equals(target_class) || (classNameWithDots.startsWith(target_class+"$"))) {
						if(logger.isDebugEnabled())
							cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
						cv = new ExecutionPathClassAdapter(cv, className);
						cv = new StringClassAdapter(cv, className);
						// cv = new CFGClassAdapter(cv, className);
						if(logger.isDebugEnabled())
							cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
					}
						//cv = new CheckClassAdapter(cv);
					if(static_hack)
						cv = new StaticInitializationClassAdapter(cv, className);
					
					
					reader.accept(cv, ClassReader.SKIP_FRAMES);
					classfileBuffer = writer.toByteArray();

					byte[] transformedBytecode = null;
					if(classNameWithDots.equals(target_class) || (classNameWithDots.startsWith(target_class+"$"))) {
						try {
							coverageTransformer.className = classNameWithDots;
							transformedBytecode = coverageTransformer.transformBytecode(classfileBuffer);
						} catch (Exception e) {
							logger.info("Exception thrown: " + e);
							e.printStackTrace();
						}
					} else {
						transformedBytecode = classfileBuffer;
						logger.debug("Class transformed: " + classNameWithDots);
					}
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

}
