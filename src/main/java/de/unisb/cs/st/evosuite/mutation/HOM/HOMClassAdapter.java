package de.unisb.cs.st.evosuite.mutation.HOM;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.arithmetic.ArithmeticReplaceMethodAdapter;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.negateJumps.NegateJumpsMethodAdapter;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.replaceIntegerConstant.RicMethodAdapter;
import de.unisb.cs.st.javalanche.mutation.results.persistence.MutationManager;

public class HOMClassAdapter extends ClassAdapter {

	private String className;

	private Map<Integer, Integer> ricPossibilities = new HashMap<Integer, Integer>();

	private Map<Integer, Integer> arithmeticPossibilities = new HashMap<Integer, Integer>();

	private Map<Integer, Integer> negatePossibilities = new HashMap<Integer, Integer>();

	//private Map<Integer, Integer> removeCallsPossibilities = new HashMap<Integer, Integer>();

	private final MutationManager mutationManager;

	public HOMClassAdapter(ClassVisitor cv) {
		this(cv, new MutationManager());
		
	}

	public HOMClassAdapter(ClassVisitor cv, MutationManager mm) {
		super(cv);
		this.mutationManager = mm;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		className = name;
	}


	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, final String[] exceptions) {
		
		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		//mv = new CFGMethodAdapter(className, access, name, desc, signature, exceptions, mv);
		//mv = new CheckMethodAdapter(mv);
		//mv = new ExecutionPathAdapter(mv, className, name, desc);
		//mv = new StringReplacementMethodAdapter(access, desc, mv);
		mv = new RicMethodAdapter(mv, className, name, ricPossibilities,
				mutationManager, desc);
		mv = new NegateJumpsMethodAdapter(mv, className, name,
				negatePossibilities, mutationManager, desc);
		mv = new ArithmeticReplaceMethodAdapter(mv, className, name,
				arithmeticPossibilities, mutationManager, desc);

		

		//mv = new RemoveMethodCallsMethodAdapter(mv, className, name,
		//		removeCallsPossibilities, mutationManager, desc);
		return mv;
	}
}