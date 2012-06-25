/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.cfg.instrumentation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.cfg.instrumentation.mutation.DeleteField;
import org.evosuite.cfg.instrumentation.mutation.DeleteStatement;
import org.evosuite.cfg.instrumentation.mutation.InsertUnaryOperator;
import org.evosuite.cfg.instrumentation.mutation.MutationOperator;
import org.evosuite.cfg.instrumentation.mutation.ReplaceArithmeticOperator;
import org.evosuite.cfg.instrumentation.mutation.ReplaceBitwiseOperator;
import org.evosuite.cfg.instrumentation.mutation.ReplaceComparisonOperator;
import org.evosuite.cfg.instrumentation.mutation.ReplaceConstant;
import org.evosuite.cfg.instrumentation.mutation.ReplaceVariable;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationObserver;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.javaagent.BooleanValueInterpreter;
import org.evosuite.testcase.ExecutionTracer;
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
 * @author Gordon Fraser
 * 
 */
public class MutationInstrumentation implements MethodInstrumentation {

	private static Logger logger = LoggerFactory.getLogger(MethodInstrumentation.class);

	private final List<MutationOperator> mutationOperators;

	private Frame[] frames = new Frame[0];

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
		mutationOperators.add(new DeleteStatement());
		mutationOperators.add(new DeleteField());
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
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void analyze(MethodNode mn, String className, String methodName, int access) {
		RawControlFlowGraph graph = GraphPool.getRawCFG(className, methodName);
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();

		getFrames(mn, className);

		boolean constructorInvoked = false;
		if (!methodName.startsWith("<init>"))
			constructorInvoked = true;

		logger.info("Applying mutation operators ");
		int frameIndex = 0;
		assert (frames.length == mn.instructions.size()) : "Length " + frames.length
		        + " vs " + mn.instructions.size();
		while (j.hasNext()) {
			Frame currentFrame = frames[frameIndex++];
			AbstractInsnNode in = j.next();
			if (!constructorInvoked) {
				if (in.getOpcode() == Opcodes.INVOKESPECIAL) {
					constructorInvoked = true;
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
			}
		}
		j = mn.instructions.iterator();

		logger.info("Result of mutation: ");
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			logger.info(new BytecodeInstruction(className, methodName, 0, 0, in).toString());
		}
		logger.info("Done.");
		// mn.maxStack += 3;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MethodInstrumentation#executeOnMainMethod()
	 */
	@Override
	public boolean executeOnMainMethod() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MethodInstrumentation#executeOnExcludedMethods()
	 */
	@Override
	public boolean executeOnExcludedMethods() {
		// TODO Auto-generated method stub
		return false;
	}

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
			                Type.DOUBLE_TYPE, Type.INT_TYPE }));
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
