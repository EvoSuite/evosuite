package de.unisb.cs.st.testability;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Created by Yanchuan Li Date: 12/15/10 Time: 11:26 AM
 */
public class BytecodeTransformer implements ClassFileTransformer {

	private static Logger log = Logger.getLogger(BytecodeTransformer.class);

	@Override
	public byte[] transform(ClassLoader classLoader, String className, Class<?> aClass,
	        ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		//        log.info("checking:[" + className + "]");
		String classNameWithDots = className.replace('/', '.');
		//log.info("checking " + classNameWithDots + " ...");

		if (classNameWithDots.startsWith("java") || classNameWithDots.startsWith("sun")
		        || classNameWithDots.startsWith("org.aspectj.org.eclipse")
		        || classNameWithDots.startsWith("org.mozilla.javascript.gen.c")
		        || !TransformationHelper.checkPackage(className)) {
			//log.info("class unchanged " + classNameWithDots + "...");
			return classfileBuffer;
		} else {
			if (TransformationHelper.checkPackage(className)) {
				log.info("start instrumsentation at " + classNameWithDots + "...");
				ClassNode cn = new ClassNode();
				log.debug("class:" + className + " access:" + cn.access);
				ClassReader cr = new ClassReader(classfileBuffer);
				cr.accept(cn, ClassReader.EXPAND_FRAMES);

				if (!cn.superName.endsWith(Type.getInternalName(Exception.class))) {
					try {
						//transform all fields;
						ClassNodeTransformer cnt = new ClassNodeTransformer(cn);
						ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
						ClassVisitor cv = new TraceClassVisitor(writer, new PrintWriter(
						        System.out));
						cnt.transform().accept(cv);
						//                        ClassVisitor cca = new CheckClassAdapter(cv);
						//                        cnt.transform().accept(cca);
						return writer.toByteArray();
					} catch (Throwable t) {
						log.error("error");
						log.fatal("Transformation of class " + className + " failed", t);
						StringWriter swriter = new StringWriter();
						t.printStackTrace(new PrintWriter(swriter));
						log.fatal(swriter.getBuffer().toString());
						t.printStackTrace();
						System.exit(0);

					}
				}
			} else {
				//                log.info("cannot transform " + classNameWithDots + "...");
			}

		}
		return classfileBuffer;
	}

}
