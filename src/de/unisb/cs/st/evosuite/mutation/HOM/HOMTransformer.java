package de.unisb.cs.st.evosuite.mutation.HOM;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import de.unisb.st.bytecodetransformer.processFiles.BytecodeTransformer;

public class HOMTransformer extends BytecodeTransformer {

	String className = "";
	
	@Override
	protected ClassVisitor classVisitorFactory(ClassWriter cw) {
		//ClassVisitor cv = new CheckClassAdapter(cw);
		//if (MutationProperties.TRACE_BYTECODE) {
		//	cv = new TraceClassVisitor(cv, new PrintWriter(MutationPreMain.sysout));
		//}
		//cv = new CFGClassAdapter(cv, "dummy");
		ClassVisitor cv = new HOMClassAdapter(cw);
//		cv = new CFGClassAdapter(cv, className);
		return cv; 
	}

}
