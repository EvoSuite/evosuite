/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJ;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJPool;

/**
 * @author copied from CFGMethodAdapter
 * 
 */
public class LCSAJsInstrumentation implements MethodInstrumentation {

	private static Logger logger = Logger.getLogger(LCSAJsInstrumentation.class);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	//using external lib
	@Override
	public void analyze(MethodNode mn, Graph<BytecodeInstruction, DefaultEdge> graph,
	        String className, String methodName, int access) {

		Queue<LCSAJ> lcsaj_queue = new LinkedList<LCSAJ>();
		HashSet<Integer> targets_reached = new HashSet<Integer>();
		LCSAJ a = new LCSAJ(className, methodName);
		a.addInstruction(0, mn.instructions.getFirst(), true);
		lcsaj_queue.add(a);

		while (!lcsaj_queue.isEmpty()) {

			LCSAJ current_lcsaj = lcsaj_queue.poll();
			int position = mn.instructions.indexOf(current_lcsaj.getLastNodeAccessed());

			if (position + 1 >= mn.instructions.size()) {
				// New LCSAJ for current + return
				LCSAJPool.add_lcsaj(className, methodName, current_lcsaj);
				continue;
			}

			AbstractInsnNode next = mn.instructions.get(position + 1);
			current_lcsaj.addInstruction(position + 1, next, false);

			if (next instanceof JumpInsnNode) {

				JumpInsnNode jump = (JumpInsnNode) next;
				// New LCSAJ for current + jump to target
				LCSAJPool.add_lcsaj(className, methodName, current_lcsaj);

				if (jump.getOpcode() != Opcodes.GOTO) {

					lcsaj_queue.add(current_lcsaj);

					if (!targets_reached.contains(position + 1)) {

						LabelNode target = jump.label;
						LCSAJ c = new LCSAJ(className, methodName);
						c.addInstruction(mn.instructions.indexOf(target), target, true);
						lcsaj_queue.add(c);
						targets_reached.add(position + 1);

					}
				}

			} else if (next instanceof TableSwitchInsnNode) {

				TableSwitchInsnNode tswitch = (TableSwitchInsnNode) next;
				List<LabelNode> allTargets = tswitch.labels;

				for (LabelNode target : allTargets) {

					int target_position = mn.instructions.indexOf(target);

					if (!targets_reached.contains(target_position)) {

						LCSAJ b = new LCSAJ(className, methodName);
						b.addInstruction(target_position, target, true);

						targets_reached.add(target_position);
						lcsaj_queue.add(b);

					}
				}

			} else if (next instanceof InsnNode) {
				InsnNode insn = (InsnNode) next;
				switch (insn.getOpcode()) {
				case Opcodes.ATHROW:
				case Opcodes.RETURN:
				case Opcodes.ARETURN:
				case Opcodes.IRETURN:
				case Opcodes.DRETURN:
				case Opcodes.LRETURN:
				case Opcodes.FRETURN:
					// New LCSAJ for current + throw
					LCSAJPool.add_lcsaj(className, methodName, current_lcsaj);
					break;
				default:
					lcsaj_queue.add(current_lcsaj);

				}
			} else {
				lcsaj_queue.add(current_lcsaj);
			}
		}
		logger.info("Found " + LCSAJPool.getSize() + " LCSAJs by now");
		System.out.println("LCSAJs completed!!!!!!!!!!!");
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
