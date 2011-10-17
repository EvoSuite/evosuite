/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.cfg.RawControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;

/**
 * @author Copied from CFGMethodAdapter
 * 
 */
public class BranchInstrumentation implements MethodInstrumentation {

	private static Logger logger = LoggerFactory
			.getLogger(BranchInstrumentation.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb
	 * .asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String,
	 * java.lang.String, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void analyze(MethodNode mn, String className, String methodName,
			int access) {
		RawControlFlowGraph graph = CFGPool.getRawCFG(className, methodName);
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for (BytecodeInstruction v : graph.vertexSet()) {

				// If this is in the CFG and it's a branch...
				if (in.equals(v.getASMNode())) {
					if (v.isBranch()) {
						mn.instructions.insertBefore(v.getASMNode(),
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
	
	private InsnList getInstrumentation(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");
		if (!instruction.isActualBranch())
			throw new IllegalArgumentException("branch instruction expected");
		if (!BranchPool.isKnownAsNormalBranchInstruction(instruction))
			throw new IllegalArgumentException(
					"expect given instruction to be known by the BranchPool as a normal branch isntruction");

		int opcode = instruction.getASMNode().getOpcode();
		int instructionId = instruction.getInstructionId();
		int branchId = BranchPool
				.getActualBranchIdForNormalBranchInstruction(instruction);
		if (branchId < 0)
			throw new IllegalStateException(
					"expect BranchPool to know branchId for alle branch instructions");

		InsnList instrumentation = new InsnList();

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
					"de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(IIII)V"));
			logger
					.debug("Adding passedBranch val=?, opcode=" + opcode
							+ ", branch=" + branchId + ", bytecode_id="
							+ instructionId);

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
					"de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(IIIII)V"));
			break;
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
			instrumentation.add(new InsnNode(Opcodes.DUP2));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(branchId));
			instrumentation.add(new LdcInsnNode(instructionId));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					"de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch",
					"(Ljava/lang/Object;Ljava/lang/Object;III)V"));
			break;
		case Opcodes.IFNULL:
		case Opcodes.IFNONNULL:
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(branchId));
			instrumentation.add(new LdcInsnNode(instructionId));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					"de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(Ljava/lang/Object;III)V"));
			break;
		}
		return instrumentation;
	}

	/**
	 * Creates the instrumentation for switch statements as follows:
	 * 
	 * For each case <key>: in the switch, two calls to the ExecutionTracer are
	 * added to the instrumentation, indicating whether the case is hit directly
	 * or not. This is done by addInstrumentationForSwitchCases().
	 * 
	 * Additionally in order to trace the execution of the default: case of the
	 * switch, the following instrumentation is added using
	 * addDefaultCaseInstrumentation():
	 * 
	 * A new switch, holding the same <key>s as the original switch we want to
	 * cover. All cases point to a label after which a call to the
	 * ExecutionTracer is added, indicating that the default case was not hit
	 * directly. Symmetrically the new switch has a default case: holding a call
	 * to the ExecutionTracer to indicate that the default will be hit directly.
	 */
	private InsnList getSwitchInstrumentation(BytecodeInstruction v,
			MethodNode mn, String className, String methodName) {
		InsnList instrumentation = new InsnList();

		if (!v.isSwitch())
			throw new IllegalArgumentException("switch instruction expected");

		addInstrumentationForDefaultSwitchCase(v, instrumentation);

		addInstrumentationForSwitchCases(v, instrumentation, className,
				methodName);

		return instrumentation;
	}

	/**
	 * For each actual case <key>: of a switch this method adds instrumentation
	 * for the Branch corresponding to that case to the given instruction list.
	 */
	private void addInstrumentationForSwitchCases(BytecodeInstruction v,
			InsnList instrumentation, String className, String methodName) {

		if (!v.isSwitch())
			throw new IllegalArgumentException("switch instruction expected");

		List<Branch> caseBranches = BranchPool.getCaseBranchesForSwitch(v);

		if (caseBranches == null || caseBranches.isEmpty())
			throw new IllegalStateException(
					"expect BranchPool to know at least one Branch for each switch instruction");

		for (Branch targetCaseBranch : caseBranches) {
			if (targetCaseBranch.isDefaultCase())
				continue; // handled elsewhere

			Integer targetCaseValue = targetCaseBranch.getTargetCaseValue();
			Integer targetCaseBranchId = targetCaseBranch.getActualBranchId();

			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(targetCaseValue));
			instrumentation.add(new LdcInsnNode(Opcodes.IF_ICMPEQ));
			instrumentation.add(new LdcInsnNode(targetCaseBranchId));
			instrumentation.add(new LdcInsnNode(v.getInstructionId()));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					"de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(IIIII)V"));
		}
	}

	private void addInstrumentationForDefaultSwitchCase(BytecodeInstruction v,
			InsnList instrumentation) {

		if (v.isTableSwitch())
			addInstrumentationForDefaultTableswitchCase(v, instrumentation);

		if (v.isLookupSwitch())
			addInstrumentationForDefaultLookupswitchCase(v, instrumentation);

	}

	private void addInstrumentationForDefaultTableswitchCase(
			BytecodeInstruction v, InsnList instrumentation) {

		if (!v.isTableSwitch())
			throw new IllegalArgumentException(
					"tableswitch instruction expected");

		// setup instructions

		TableSwitchInsnNode toInstrument = (TableSwitchInsnNode) v.getASMNode();

		LabelNode caseLabel = new LabelNode();
		LabelNode defaultLabel = new LabelNode();
		LabelNode endLabel = new LabelNode();

		int keySize = (toInstrument.max - toInstrument.min) + 1;
		LabelNode[] caseLabels = new LabelNode[keySize];
		for (int i = 0; i < keySize; i++)
			caseLabels[i] = caseLabel;

		TableSwitchInsnNode mySwitch = new TableSwitchInsnNode(
				toInstrument.min, toInstrument.max, defaultLabel, caseLabels);

		// add instrumentation
		addDefaultCaseInstrumentation(v, instrumentation, mySwitch,
				defaultLabel, caseLabel, endLabel);

	}

	private void addInstrumentationForDefaultLookupswitchCase(
			BytecodeInstruction v, InsnList instrumentation) {

		if (!v.isLookupSwitch())
			throw new IllegalArgumentException("lookup switch expected");

		// setup instructions
		LookupSwitchInsnNode toInstrument = (LookupSwitchInsnNode) v
				.getASMNode();

		LabelNode caseLabel = new LabelNode();
		LabelNode defaultLabel = new LabelNode();
		LabelNode endLabel = new LabelNode();

		int keySize = toInstrument.keys.size();

		int[] keys = new int[keySize];
		LabelNode[] labels = new LabelNode[keySize];
		for (int i = 0; i < keySize; i++) {
			keys[i] = (Integer) toInstrument.keys.get(i);
			labels[i] = caseLabel;
		}

		LookupSwitchInsnNode myLookup = new LookupSwitchInsnNode(defaultLabel,
				keys, labels);

		addDefaultCaseInstrumentation(v, instrumentation, myLookup,
				defaultLabel, caseLabel, endLabel);

	}

	private void addDefaultCaseInstrumentation(BytecodeInstruction v,
			InsnList instrumentation, AbstractInsnNode mySwitch,
			LabelNode defaultLabel, LabelNode caseLabel, LabelNode endLabel) {

		int defaultCaseBranchId = BranchPool.getDefaultBranchForSwitch(v)
				.getActualBranchId();

		// add helper switch
		instrumentation.add(new InsnNode(Opcodes.DUP));
		instrumentation.add(mySwitch);

		// add call for default case not covered
		instrumentation.add(caseLabel);
		addDefaultCaseNotCoveredCall(v, instrumentation, defaultCaseBranchId);

		// jump over default (break)
		instrumentation.add(new JumpInsnNode(Opcodes.GOTO, endLabel));

		// add call for default case covered
		instrumentation.add(defaultLabel);
		addDefaultCaseCoveredCall(v, instrumentation, defaultCaseBranchId);

		instrumentation.add(endLabel);

	}

	private void addDefaultCaseCoveredCall(BytecodeInstruction v,
			InsnList instrumentation, int defaultCaseBranchId) {

		instrumentation.add(new LdcInsnNode(0));
		instrumentation.add(new LdcInsnNode(Opcodes.IFEQ));
		instrumentation.add(new LdcInsnNode(defaultCaseBranchId));
		instrumentation.add(new LdcInsnNode(v.getInstructionId()));
		instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				"de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				"passedBranch", "(IIII)V"));

	}

	private void addDefaultCaseNotCoveredCall(BytecodeInstruction v,
			InsnList instrumentation, int defaultCaseBranchId) {

		instrumentation.add(new LdcInsnNode(0));
		instrumentation.add(new LdcInsnNode(Opcodes.IFNE));
		instrumentation.add(new LdcInsnNode(defaultCaseBranchId));
		instrumentation.add(new LdcInsnNode(v.getInstructionId()));
		instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				"de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				"passedBranch", "(IIII)V"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#executeOnExcludedMethods
	 * ()
	 */
	@Override
	public boolean executeOnExcludedMethods() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#executeOnMainMethod()
	 */
	@Override
	public boolean executeOnMainMethod() {
		return false;
	}

}
