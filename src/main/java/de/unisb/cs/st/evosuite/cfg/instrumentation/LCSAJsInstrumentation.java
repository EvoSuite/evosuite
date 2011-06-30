/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstructionPool;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.cfg.LCSAJGraph;
import de.unisb.cs.st.evosuite.cfg.RawControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJ;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJPool;

/**
 * @author copied from CFGMethodAdapter
 * 
 */
public class LCSAJsInstrumentation implements MethodInstrumentation {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	//using external lib
	@Override
	public void analyze(MethodNode mn, String className, String methodName, int access) {

		Queue<LCSAJ> lcsaj_queue = new LinkedList<LCSAJ>();
		HashSet<Integer> targets_reached = new HashSet<Integer>();

		AbstractInsnNode start = mn.instructions.getFirst();
		int startID = 0;
		// This is ugly, but in the constructor the instrumentation has to come after the call to Object() 
		if (methodName.startsWith("<init>")) {
			start = mn.instructions.get(4);
			startID = 4;
		}
		LCSAJ a = new LCSAJ(className, methodName,
		        BytecodeInstructionPool.getInstruction(className, methodName, startID,
		                                               start));
		lcsaj_queue.add(a);

		targets_reached.add(0);

		ArrayList<TryCatchBlockNode> tc_blocks = (ArrayList<TryCatchBlockNode>) mn.tryCatchBlocks;

		for (TryCatchBlockNode t : tc_blocks) {
			LCSAJ b = new LCSAJ(
			        className,
			        methodName,
			        BytecodeInstructionPool.getInstruction(className,
			                                               methodName,
			                                               mn.instructions.indexOf(t.handler),
			                                               t.handler));
			lcsaj_queue.add(b);
		}

		while (!lcsaj_queue.isEmpty()) {

			LCSAJ currentLCSAJ = lcsaj_queue.poll();
			int position = mn.instructions.indexOf(currentLCSAJ.getLastNodeAccessed());
			// go to next bytecode instruction
			position++;

			if (position >= mn.instructions.size()) {
				// New LCSAJ for current + return
				LCSAJPool.add_lcsaj(className, methodName, currentLCSAJ);
				continue;
			}

			AbstractInsnNode next = mn.instructions.get(position);
			currentLCSAJ.lookupInstruction(position,
			                               BytecodeInstructionPool.getInstruction(className,
			                                                                      methodName,
			                                                                      position,
			                                                                      next));

			if (next instanceof JumpInsnNode) {

				JumpInsnNode jump = (JumpInsnNode) next;
				// New LCSAJ for current + jump to target
				LCSAJPool.add_lcsaj(className, methodName, currentLCSAJ);
				LabelNode target = jump.label;
				int targetPosition = mn.instructions.indexOf(target);

				if (jump.getOpcode() != Opcodes.GOTO) {

					LCSAJ copy = new LCSAJ(currentLCSAJ);
					lcsaj_queue.add(copy);

					if (!targets_reached.contains(targetPosition)) {
						LCSAJ c = new LCSAJ(className, methodName,
						        BytecodeInstructionPool.getInstruction(className,
						                                               methodName,
						                                               targetPosition,
						                                               target));
						lcsaj_queue.add(c);
					}
				}
				if (!targets_reached.contains(targetPosition))
					targets_reached.add(targetPosition);

			} else if (next instanceof TableSwitchInsnNode) {

				TableSwitchInsnNode tswitch = (TableSwitchInsnNode) next;
				List<LabelNode> allTargets = tswitch.labels;

				for (LabelNode target : allTargets) {

					int targetPosition = mn.instructions.indexOf(target);

					if (!targets_reached.contains(targetPosition)) {

						LCSAJ b = new LCSAJ(className, methodName,
						        BytecodeInstructionPool.getInstruction(className,
						                                               methodName,
						                                               targetPosition,
						                                               target));
						lcsaj_queue.add(b);

						targets_reached.add(targetPosition);
					}
				}

			} else if (next instanceof InsnNode) {
				InsnNode insn = (InsnNode) next;
				// New LCSAJ for current + throw / return
				if (insn.getOpcode() == Opcodes.ATHROW
				        || insn.getOpcode() == Opcodes.RETURN
				        || insn.getOpcode() == Opcodes.ARETURN
				        || insn.getOpcode() == Opcodes.IRETURN
				        || insn.getOpcode() == Opcodes.DRETURN
				        || insn.getOpcode() == Opcodes.LRETURN
				        || insn.getOpcode() == Opcodes.FRETURN) {

					LCSAJPool.add_lcsaj(className, methodName, currentLCSAJ);
				} else
					lcsaj_queue.add(currentLCSAJ);
			} else
				lcsaj_queue.add(currentLCSAJ);
		}
		
		addInstrumentation(mn, className, methodName);
		if (Properties.WRITE_CFG)
			for (LCSAJ l : LCSAJPool.getLCSAJs(className, methodName)){
				LCSAJGraph graph = new LCSAJGraph(l,false);
				String graphDestination = "evosuite-graphs/LCSAJGraphs/"+className+"/"+methodName;
				File dir = new File(graphDestination);
				if (dir.mkdirs())
					graph.generate(new File(graphDestination+"/LCSAJGraph no: "+l.getID()+".dot"));
				else if (dir.exists())
					graph.generate(new File(graphDestination+"/LCSAJGraph no: "+l.getID()+".dot"));
			}
			
	}

	@SuppressWarnings("unchecked")
	private void addInstrumentation(MethodNode mn, String className, String methodName) {
		RawControlFlowGraph graph = CFGPool.getRawCFG(className, methodName);
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for (BytecodeInstruction v : graph.vertexSet()) {

				// If this is in the CFG and it's a branch...
				if (in.equals(v.getASMNode())) {
					if (BranchPool.isKnownAsBranch(v) && !v.isBranch()) {
						LCSAJPool.addLCSAJBranch(BranchPool.getBranchForInstruction(v));

						int branchId = BranchPool.getActualBranchIdForInstruction(v);
						InsnList instrumentation = new InsnList();
						instrumentation.add(new LdcInsnNode(v.getASMNode().getOpcode()));
						instrumentation.add(new LdcInsnNode(branchId));
						instrumentation.add(new LdcInsnNode(v.getInstructionId()));
						instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
						        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
						        "passedUnconditionalBranch", "(III)V"));
						if (v.isLabel())
							mn.instructions.insert(v.getASMNode(), instrumentation);
						else
							mn.instructions.insertBefore(v.getASMNode(), instrumentation);
					}
				}
			}
		}
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
