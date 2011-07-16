/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
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

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.cfg.RawControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.InsertUnaryOperator;
import de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.MutationOperator;
import de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.NegateCondition;
import de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.ReplaceArithmeticOperator;
import de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.ReplaceBitwiseOperator;
import de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.ReplaceComparisonOperator;
import de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.ReplaceConstant;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationObserver;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;

/**
 * @author Gordon Fraser
 * 
 */
public class MutationInstrumentation implements MethodInstrumentation {

	private static Logger logger = Logger.getLogger(MethodInstrumentation.class);

	private final List<MutationOperator> mutationOperators;

	public MutationInstrumentation() {
		mutationOperators = new ArrayList<MutationOperator>();
		mutationOperators.add(new ReplaceConstant());

		//mutationOperators.add(new ReplaceVariable());
		mutationOperators.add(new ReplaceComparisonOperator());
		mutationOperators.add(new ReplaceBitwiseOperator());
		mutationOperators.add(new ReplaceArithmeticOperator());
		mutationOperators.add(new NegateCondition());
		mutationOperators.add(new InsertUnaryOperator());

		// TODO: Replace iinc

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void analyze(MethodNode mn, String className, String methodName, int access) {
		RawControlFlowGraph graph = CFGPool.getRawCFG(className, methodName);
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();

		logger.info("Applying mutation operators ");
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for (BytecodeInstruction v : graph.vertexSet()) {

				// If this is in the CFG
				if (in.equals(v.getASMNode())) {
					logger.info(v);
					List<Mutation> mutations = new LinkedList<Mutation>();

					// TODO: More than one mutation operator might apply to the same instruction
					for (MutationOperator mutationOperator : mutationOperators) {
						//logger.info("Checking mutation operator on instruction " + v);
						if (mutationOperator.isApplicable(v)) {
							logger.info("Applying mutation operator "
							        + mutationOperator.getClass().getSimpleName());
							mutations.addAll(mutationOperator.apply(mn, className,
							                                        methodName, v));
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
			logger.info(new BytecodeInstruction(className, methodName, 0, 0, in));
		}
		logger.info("Done.");
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
			instructions.add(new LdcInsnNode(mutation.getId()));
			instructions.add(mutation.getInfectionDistance());
			MethodInsnNode touched = new MethodInsnNode(Opcodes.INVOKESTATIC,
			        Type.getInternalName(ExecutionTracer.class), "passedMutation",
			        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE,
			                Type.DOUBLE_TYPE }));
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
