package de.unisb.cs.st.evosuite.mutation.HOM;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.MutationsClassAdapter;
import de.unisb.cs.st.javalanche.mutation.results.persistence.MutationManager;
import de.unisb.st.bytecodetransformer.processFiles.BytecodeTransformer;

public class HOMTransformer extends BytecodeTransformer {

	String className = "";

	private final MutationManager mm = new MutationManager();

	// protected boolean static_hack =
	// Properties.getPropertyOrDefault("static_hack", false);

	@Override
	protected ClassVisitor classVisitorFactory(ClassWriter cw) {
		ClassVisitor cv = cw;
		return new MutationsClassAdapter(cv, mm);

		// ClassVisitor cv = new CheckClassAdapter(cw);
		// if (MutationProperties.TRACE_BYTECODE) {
		// cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
		// }
		// cv = new CFGClassAdapter(cv, "dummy");
		// ClassVisitor cv = new TraceClassVisitor(cw, new
		// PrintWriter(System.out));
		// ClassVisitor cv = new HOMClassAdapter(cw);
		// ClassVisitor cv = new MutationsClassAdapter(cw);

		// cv = new ExecutionPathClassAdapter(cv, className);
		// cv = new StringClassAdapter(cv, className);
		// cv = new CheckClassAdapter(cv);
		// if(static_hack)
		// cv = new StaticInitializationClassAdapter(cv, className);

		// cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
		// cv = new CFGClassAdapter(cv, className);
		// return cv;
	}

}
