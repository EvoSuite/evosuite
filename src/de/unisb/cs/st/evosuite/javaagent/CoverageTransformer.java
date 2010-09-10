/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import de.unisb.cs.st.evosuite.cfg.CFGClassAdapter;
import de.unisb.st.bytecodetransformer.processFiles.BytecodeTransformer;

/**
 * @author Gordon Fraser
 *
 */
public class CoverageTransformer extends BytecodeTransformer {

	String className = "";

	@Override
	protected ClassVisitor classVisitorFactory(ClassWriter arg) {
		ClassVisitor cv = new CFGClassAdapter(arg, className);
		return cv; 
	}

}
