/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * @author Copied from CFGMethodAdapter
 * 
 */
public class BranchInstrumentation implements MethodInstrumentation {

	private static Logger logger = Logger.getLogger(BranchInstrumentation.class);
	private static int currentLineNumber = -1;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void analyze(MethodNode mn, Graph<BytecodeInstruction, DefaultEdge> graph,
	        String className, String methodName, int access) {
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for (BytecodeInstruction v : graph.vertexSet()) {
				// updating some information in the CFGVertex
				if (in.equals(v.getASMNode())) {
					if (v.isLineNumber()) {
						currentLineNumber = v.getLineNumber();
					}
					v.setClassName(className);
					v.setMethodName(methodName);
					v.setLineNumber(currentLineNumber);
				}
				// If this is in the CFG and it's a branch...
				if (in.equals(v.getASMNode())) {
					if (v.isBranch() && !v.isMutation() && !v.isMutationBranch()) {
						mn.instructions.insert(v.getASMNode().getPrevious(),
						                       getInstrumentation(v.getASMNode().getOpcode(),
						                                          v.getId(), className,
						                                          methodName));

						BranchPool.addBranch(v);
					} else if (v.isTableSwitch()) {
						mn.instructions.insertBefore(v.getASMNode(),
						                             getInstrumentation(v, mn, className,
						                                                methodName));
					} else if (v.isLookupSwitch()) {
						mn.instructions.insertBefore(v.getASMNode(),
						                             getInstrumentation(v, mn, className,
						                                                methodName));
					} else if (v.isThrow()) {
						if (Properties.CRITERION == Criterion.LCSAJ) {
							InsnList instrumentation = new InsnList();
							instrumentation.add(new LdcInsnNode(v.getASMNode().getOpcode()));
							instrumentation.add(new LdcInsnNode(
							        BranchPool.getBranchCounter()));
							instrumentation.add(new LdcInsnNode(v.getId()));
							instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
							        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
							        "passedUnconditionalBranch", "(III)V"));
							BranchPool.countBranch(className + "." + methodName);
							mn.instructions.insertBefore(v.getASMNode(), instrumentation);
						}
					}
				}
			}
		}
	}

	private InsnList getInstrumentation(BytecodeInstruction v, MethodNode mn, String className,
	        String methodName) {
		InsnList instrumentation = new InsnList();

		String methodID = className + "." + methodName;
		switch (v.getASMNode().getOpcode()) {
		case Opcodes.TABLESWITCH:
			TableSwitchInsnNode tsin = (TableSwitchInsnNode) v.getASMNode();
			int num = 0;
			for (int i = tsin.min; i <= tsin.max; i++) {
				instrumentation.add(new InsnNode(Opcodes.DUP));
				instrumentation.add(new LdcInsnNode(i));
				instrumentation.add(new LdcInsnNode(Opcodes.IF_ICMPEQ));
				instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
				instrumentation.add(new LdcInsnNode(v.getId()));
				//instrumentation.add(new LdcInsnNode(
				//        mn.instructions.indexOf((LabelNode) tsin.labels.get(num))));

				instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				        "passedBranch", "(IIIII)V"));
				BranchPool.countBranch(methodID);
				BranchPool.addBranch(v);
				num++;
			}
			// Default branch is covered if the last case is false
			break;
		case Opcodes.LOOKUPSWITCH:
			LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) v.getASMNode();
			logger.info("Found lookupswitch with " + lsin.keys.size() + " keys");
			for (int i = 0; i < lsin.keys.size(); i++) {
				instrumentation.add(new InsnNode(Opcodes.DUP));
				instrumentation.add(new LdcInsnNode(
				        ((Integer) lsin.keys.get(i)).intValue()));
				instrumentation.add(new LdcInsnNode(Opcodes.IF_ICMPEQ));
				instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
				instrumentation.add(new LdcInsnNode(v.getId()));
				//				instrumentation.add(new LdcInsnNode(
				//				        mn.instructions.indexOf((LabelNode) lsin.labels.get(i))));
				instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				        "passedBranch", "(IIIII)V"));
				BranchPool.countBranch(methodID);
				BranchPool.addBranch(v);
			}
			// Default branch is covered if the last case is false
			break;
		}

		return instrumentation;
	}

	private InsnList getInstrumentation(int opcode, int id, String className,
	        String methodName) {
		InsnList instrumentation = new InsnList();

		String methodID = className + "." + methodName;

		switch (opcode) {
		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFLE:
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(IIII)V"));
			BranchPool.countBranch(methodID);
			logger.debug("Adding passedBranch val=?, opcode=" + opcode + ", branch="
			        + BranchPool.getBranchCounter() + ", bytecode_id=" + id);

			break;
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
			instrumentation.add(new InsnNode(Opcodes.DUP2));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(IIIII)V"));
			BranchPool.countBranch(methodID);

			break;
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
			instrumentation.add(new InsnNode(Opcodes.DUP2));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(Ljava/lang/Object;Ljava/lang/Object;III)V"));
			BranchPool.countBranch(methodID);
			break;
		case Opcodes.IFNULL:
		case Opcodes.IFNONNULL:
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(Ljava/lang/Object;III)V"));
			BranchPool.countBranch(methodID);
			break;
		case Opcodes.GOTO:
			if (Properties.CRITERION == Criterion.LCSAJ) {
				instrumentation.add(new LdcInsnNode(opcode));
				instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
				instrumentation.add(new LdcInsnNode(id));
				instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				        "passedUnconditionalBranch", "(III)V"));
				BranchPool.countBranch(methodID);
			}
			break;
		/*
		case Opcodes.TABLESWITCH:
		instrumentation.add(new InsnNode(Opcodes.DUP));
		instrumentation.add(new LdcInsnNode(opcode));
		// instrumentation.add(new LdcInsnNode(id));
		instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
		instrumentation.add(new LdcInsnNode(id));
		instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
		    "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
		    "(IIII)V"));
		BranchPool.countBranch(methodID);
		break;
		case Opcodes.LOOKUPSWITCH:
		instrumentation.add(new InsnNode(Opcodes.DUP));
		instrumentation.add(new LdcInsnNode(opcode));
		// instrumentation.add(new LdcInsnNode(id));
		instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
		instrumentation.add(new LdcInsnNode(id));
		instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
		    "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
		    "(IIII)V"));
		BranchPool.countBranch(methodID);
		break;
		 */
		}
		return instrumentation;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#executeOnExcludedMethods()
	 */
	@Override
	public boolean executeOnExcludedMethods() {
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#executeOnMainMethod()
	 */
	@Override
	public boolean executeOnMainMethod() {
		return false;
	}

}
