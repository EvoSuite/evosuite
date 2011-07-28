/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.cfg.RawControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;

/**
 * @author Copied from CFGMethodAdapter
 * 
 */
public class BranchInstrumentation implements MethodInstrumentation {

	private static Logger logger = LoggerFactory.getLogger(BranchInstrumentation.class);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void analyze(MethodNode mn, String className, String methodName, int access) {
		RawControlFlowGraph graph = CFGPool.getRawCFG(className, methodName);
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for (BytecodeInstruction v : graph.vertexSet()) {

				// If this is in the CFG and it's a branch...
				if (in.equals(v.getASMNode())) {
					if (v.isBranch()) {
						mn.instructions.insert(v.getASMNode().getPrevious(),
						                       getInstrumentation(v));

					} else if (v.isSwitch()) {
						mn.instructions.insertBefore(v.getASMNode(),
						                             getSwitchInstrumentation(v, mn, className,
						                                                methodName));
					}
				}
			}
		}
	}

	private InsnList getSwitchInstrumentation(BytecodeInstruction v, MethodNode mn,
	        String className, String methodName) {
		InsnList instrumentation = new InsnList();

		if(!v.isSwitch())
			throw new IllegalArgumentException("switch instruction expected");
		
		Map<Integer,Integer> caseValuesToBranchIDs = BranchPool.getBranchIdMapForSwitch(v);
		if (caseValuesToBranchIDs == null || caseValuesToBranchIDs.isEmpty())
			throw new IllegalStateException(
			        "expect BranchPool to know at least one branchID for each switch instruction");

		String methodID = className + "." + methodName;

		for (Integer targetCaseValue : caseValuesToBranchIDs.keySet()) {
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(targetCaseValue));
			instrumentation.add(new LdcInsnNode(Opcodes.IF_ICMPEQ));
			instrumentation.add(new LdcInsnNode(caseValuesToBranchIDs.get(targetCaseValue)));
			instrumentation.add(new LdcInsnNode(v.getInstructionId()));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
			        "passedBranch", "(IIIII)V"));
			BranchPool.countBranch(methodID);
		}

		return instrumentation;
	}

	private InsnList getInstrumentation(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");
		if (!instruction.isActualBranch())
			throw new IllegalArgumentException("branch instruction expected");

		int opcode = instruction.getASMNode().getOpcode();
		int instructionId = instruction.getInstructionId();
		String className = instruction.getClassName();
		String methodName = instruction.getMethodName();

		int branchId = BranchPool.getActualBranchIdForInstruction(instruction);
		if (branchId < 0)
			throw new IllegalStateException(
			        "expect BranchPool to know branchId for alle branch instructions");

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
			instrumentation.add(new LdcInsnNode(branchId));
			instrumentation.add(new LdcInsnNode(instructionId));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(IIII)V"));
			BranchPool.countBranch(methodID);
			logger.debug("Adding passedBranch val=?, opcode=" + opcode + ", branch="
			        + branchId + ", bytecode_id=" + instructionId);

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
			instrumentation.add(new LdcInsnNode(branchId));
			instrumentation.add(new LdcInsnNode(instructionId));
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
			instrumentation.add(new LdcInsnNode(branchId));
			instrumentation.add(new LdcInsnNode(instructionId));
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
			instrumentation.add(new LdcInsnNode(branchId));
			instrumentation.add(new LdcInsnNode(instructionId));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(Ljava/lang/Object;III)V"));
			BranchPool.countBranch(methodID);
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
