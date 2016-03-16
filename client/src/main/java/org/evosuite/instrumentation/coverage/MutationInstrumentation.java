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
/**
 * 
 */
package org.evosuite.instrumentation.coverage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.classpath.ResourceList;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationObserver;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.instrumentation.BooleanValueInterpreter;
import org.evosuite.instrumentation.mutation.InsertUnaryOperator;
import org.evosuite.instrumentation.mutation.MutationOperator;
import org.evosuite.instrumentation.mutation.ReplaceArithmeticOperator;
import org.evosuite.instrumentation.mutation.ReplaceBitwiseOperator;
import org.evosuite.instrumentation.mutation.ReplaceComparisonOperator;
import org.evosuite.instrumentation.mutation.ReplaceConstant;
import org.evosuite.instrumentation.mutation.ReplaceVariable;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * MutationInstrumentation class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class MutationInstrumentation implements MethodInstrumentation {

	private static Logger logger = LoggerFactory.getLogger(MethodInstrumentation.class);

	private final List<MutationOperator> mutationOperators;

	private Frame[] frames = new Frame[0];

	/**
	 * <p>
	 * Constructor for MutationInstrumentation.
	 * </p>
	 */
	public MutationInstrumentation() {
		mutationOperators = new ArrayList<MutationOperator>();

		// FIXME: Don't include > < >= <= for boolean comparisons
		mutationOperators.add(new ReplaceComparisonOperator());
		mutationOperators.add(new ReplaceBitwiseOperator());
		mutationOperators.add(new ReplaceArithmeticOperator());
		mutationOperators.add(new ReplaceVariable());
		mutationOperators.add(new ReplaceConstant());
		// mutationOperators.add(new NegateCondition());
		// FIXME: Don't apply to boolean values!
		mutationOperators.add(new InsertUnaryOperator());

		// FIXME: Can't check return types because of side effects
		//mutationOperators.add(new DeleteStatement());
		//mutationOperators.add(new DeleteField());
		// TODO: Replace iinc?
	}

	private void getFrames(MethodNode mn, String className) {
		try {
			Analyzer a = new Analyzer(new BooleanValueInterpreter(mn.desc,
			        (mn.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC));
			a.analyze(className, mn);
			this.frames = a.getFrames();
		} catch (Exception e) {
			logger.info("1. Error during analysis: " + e);
			//e.printStackTrace();
			// TODO: Handle error
		}

	}

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.instrumentation.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, int)
	 */
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void analyze(ClassLoader classLoader, MethodNode mn, String className,
	        String methodName, int access) {

		if (methodName.startsWith("<clinit>"))
			return;

		if (methodName.startsWith(ClassResetter.STATIC_RESET)) 
			return;
		
		RawControlFlowGraph graph = GraphPool.getInstance(classLoader).getRawCFG(className,
		                                                                         methodName);
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();

		getFrames(mn, className);

		boolean constructorInvoked = false;
		if (!methodName.startsWith("<init>"))
			constructorInvoked = true;

		logger.info("Applying mutation operators ");
		int frameIndex = 0;
		int numMutants = 0;
		if (frames.length != mn.instructions.size()) {
			logger.error("Number of frames does not match number number of bytecode instructions: "
			        + frames.length + "/" + mn.instructions.size());
			logger.error("Skipping mutation of method " + className + "." + methodName);
			return;
		}
		//assert (frames.length == mn.instructions.size()) : "Length " + frames.length
		//        + " vs " + mn.instructions.size();
		while (j.hasNext()) {
			Frame currentFrame = frames[frameIndex++];
			AbstractInsnNode in = j.next();
			if (!constructorInvoked) {
				if (in.getOpcode() == Opcodes.INVOKESPECIAL) {
					if (className.matches(".*\\$\\d+$")) {
						// We will not find the superclasses of an anonymous class this way
						// so best not mutate the constructor
						continue;
					}
					MethodInsnNode cn = (MethodInsnNode) in;
					Set<String> superClasses = new HashSet<String>();
					if(DependencyAnalysis.getInheritanceTree() != null && DependencyAnalysis.getInheritanceTree().hasClass(className))
						superClasses.addAll(DependencyAnalysis.getInheritanceTree().getSuperclasses(className));
					superClasses.add(className);
					String classNameWithDots = ResourceList.getClassNameFromResourcePath(cn.owner);
					if (superClasses.contains(classNameWithDots)) {
						constructorInvoked = true;
					}
				} else {
					continue;
				}
			}

			for (BytecodeInstruction v : graph.vertexSet()) {

				// If this is in the CFG
				if (in.equals(v.getASMNode())) {
					logger.info(v.toString());
					List<Mutation> mutations = new LinkedList<Mutation>();

					// TODO: More than one mutation operator might apply to the same instruction
					for (MutationOperator mutationOperator : mutationOperators) {

						if (numMutants++ > Properties.MAX_MUTANTS_PER_METHOD) {
							logger.info("Reached maximum number of mutants per method");
							break;
						}
						//logger.info("Checking mutation operator on instruction " + v);
						if (mutationOperator.isApplicable(v)) {
							logger.info("Applying mutation operator "
							        + mutationOperator.getClass().getSimpleName());
							mutations.addAll(mutationOperator.apply(mn, className,
							                                        methodName, v,
							                                        currentFrame));
						}
					}
					if (!mutations.isEmpty()) {
						logger.info("Adding instrumentation for mutation");
						//InsnList instrumentation = getInstrumentation(in, mutations);
						addInstrumentation(mn, in, mutations);
					}
				}
				if (numMutants > Properties.MAX_MUTANTS_PER_METHOD) {
					break;
				}

			}
		}
		j = mn.instructions.iterator();

		logger.info("Result of mutation: ");
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			logger.info(new BytecodeInstruction(classLoader, className, methodName, 0, 0,
			        in).toString());
		}
		logger.info("Done.");
		// mn.maxStack += 3;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.instrumentation.MethodInstrumentation#executeOnMainMethod()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean executeOnMainMethod() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.instrumentation.MethodInstrumentation#executeOnExcludedMethods()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean executeOnExcludedMethods() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * <p>
	 * addInstrumentation
	 * </p>
	 * 
	 * @param mn
	 *            a {@link org.objectweb.asm.tree.MethodNode} object.
	 * @param original
	 *            a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
	 * @param mutations
	 *            a {@link java.util.List} object.
	 */
	protected void addInstrumentation(MethodNode mn, AbstractInsnNode original,
	        List<Mutation> mutations) {

		InsnList instructions = new InsnList();

		// call mutationTouched(mutationObject.getId());
		// TODO: All mutations in the id are touched, not just one!
		for (Mutation mutation : mutations) {
			instructions.add(mutation.getInfectionDistance());
			instructions.add(new LdcInsnNode(mutation.getId()));
			MethodInsnNode touched = new MethodInsnNode(Opcodes.INVOKESTATIC,
			        Type.getInternalName(ExecutionTracer.class), "passedMutation",
			        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
			                Type.DOUBLE_TYPE, Type.INT_TYPE }), false);
			instructions.add(touched);
		}

		LabelNode endLabel = new LabelNode();
		for (Mutation mutation : mutations) {
			LabelNode nextLabel = new LabelNode();

			LdcInsnNode mutationId = new LdcInsnNode(mutation.getId());
			instructions.add(mutationId);
			FieldInsnNode activeId = new FieldInsnNode(Opcodes.GETSTATIC,
			        Type.getInternalName(MutationObserver.class), "activeMutation", "I");
			instructions.add(activeId);
			instructions.add(new JumpInsnNode(Opcodes.IF_ICMPNE, nextLabel));
			instructions.add(mutation.getMutation());
			instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabel));
			instructions.add(nextLabel);
		}

		mn.instructions.insertBefore(original, instructions);
		mn.instructions.insert(original, endLabel);
	}
}
