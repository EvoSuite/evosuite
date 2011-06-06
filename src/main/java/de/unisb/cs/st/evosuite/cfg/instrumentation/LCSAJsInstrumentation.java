/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

//import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJ;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJPool;

/**
 * @author copied from CFGMethodAdapter
 * 
 */
public class LCSAJsInstrumentation implements MethodInstrumentation {

//	private static Logger logger = Logger.getLogger(LCSAJsInstrumentation.class);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	//using external lib
	@Override
	public void analyze(MethodNode mn, String className, String methodName, int access) {

		Queue<LCSAJ> lcsaj_queue = new LinkedList<LCSAJ>();
		HashSet<Integer> targets_reached = new HashSet<Integer>();
		
		LCSAJ a = new LCSAJ(className, methodName,mn.instructions.getFirst(),0,-1);
		lcsaj_queue.add(a);
		
		targets_reached.add(0);
		
		ArrayList<TryCatchBlockNode> tc_blocks = (ArrayList<TryCatchBlockNode>)mn.tryCatchBlocks;
	
		for (TryCatchBlockNode t : tc_blocks){
			LCSAJ b  = new LCSAJ(className,methodName,t.handler,mn.instructions.indexOf(t.handler),-1);
			lcsaj_queue.add(b);
		}
		
		while (!lcsaj_queue.isEmpty()) {

			LCSAJ current_lcsaj = lcsaj_queue.poll();
			int position = mn.instructions.indexOf(current_lcsaj.getLastNodeAccessed());
			// go to next bytecode instruction
			position++;
			
			if (position >= mn.instructions.size()) {
				// New LCSAJ for current + return
				LCSAJPool.add_lcsaj(className, methodName, current_lcsaj);
				continue;
			}

			AbstractInsnNode next = mn.instructions.get(position);
			current_lcsaj.lookupInstruction(position, next);

			if (next instanceof JumpInsnNode) {
				
				JumpInsnNode jump = (JumpInsnNode) next;
				// New LCSAJ for current + jump to target
				LCSAJPool.add_lcsaj(className, methodName, current_lcsaj);
				LabelNode target = jump.label;
				int targetPosition = mn.instructions.indexOf(target);
				
				if (jump.getOpcode() != Opcodes.GOTO) {
					
					LCSAJ copy = new LCSAJ(current_lcsaj);
					lcsaj_queue.add(copy);
					
					if (!targets_reached.contains(targetPosition)){
						LCSAJ c = new LCSAJ(className, methodName,target,targetPosition,position);
						lcsaj_queue.add(c);
					}                                             
				}
				if (!targets_reached.contains(targetPosition))
					targets_reached.add(targetPosition);
				
			} else if (next instanceof TableSwitchInsnNode) {

				TableSwitchInsnNode tswitch = (TableSwitchInsnNode) next;
				List<LabelNode> allTargets = tswitch.labels;

				for (LabelNode target : allTargets) {

					int target_position = mn.instructions.indexOf(target);

					if (!targets_reached.contains(target_position)) {

						LCSAJ b = new LCSAJ(className, methodName,target,target_position,position);
						lcsaj_queue.add(b);
						
						targets_reached.add(target_position);
					}
				}

			} else if (next instanceof InsnNode ) {
				InsnNode insn = (InsnNode) next;
				// New LCSAJ for current + throw / return
				if (	insn.getOpcode() == Opcodes.ATHROW
						||	insn.getOpcode() == Opcodes.RETURN
						||	insn.getOpcode() == Opcodes.ARETURN
						||	insn.getOpcode() == Opcodes.IRETURN
						||	insn.getOpcode() == Opcodes.DRETURN
						||	insn.getOpcode() == Opcodes.LRETURN
						||	insn.getOpcode() == Opcodes.FRETURN)		
					LCSAJPool.add_lcsaj(className, methodName, current_lcsaj);
				else 
					lcsaj_queue.add(current_lcsaj);
			} else 
				lcsaj_queue.add(current_lcsaj);
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
