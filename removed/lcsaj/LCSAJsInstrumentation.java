/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

package org.evosuite.instrumentation.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.evosuite.Properties;
import org.evosuite.Properties.Strategy;
import org.evosuite.classpath.ResourceList;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.lcsaj.LCSAJ;
import org.evosuite.coverage.lcsaj.LCSAJPool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.setup.DependencyAnalysis;
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

/**
 * <p>
 * LCSAJsInstrumentation class.
 * </p>
 * 
 * @author copied from CFGMethodAdapter
 */
public class LCSAJsInstrumentation implements MethodInstrumentation {

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	//using external lib
	@Override
	public void analyze(ClassLoader classLoader, MethodNode mn, String className,
	        String methodName, int access) {

		Queue<LCSAJ> lcsaj_queue = new LinkedList<LCSAJ>();
		HashSet<Integer> targets_reached = new HashSet<Integer>();

		AbstractInsnNode start = mn.instructions.getFirst();
		int startID = 0;

		// TODO: This should replace the hack below
		if (methodName.startsWith("<init>")) {
			Iterator<AbstractInsnNode> j = mn.instructions.iterator();
			boolean constructorInvoked = false;
			while (j.hasNext()) {
				AbstractInsnNode in = j.next();
				startID++;
				if(!constructorInvoked) {
					if (in.getOpcode() == Opcodes.INVOKESPECIAL) {
						MethodInsnNode cn = (MethodInsnNode) in;
						Collection<String> superClasses = DependencyAnalysis.getInheritanceTree().getSuperclasses(className);
						superClasses.add(className);
						String classNameWithDots = ResourceList.getClassNameFromResourcePath(cn.owner);
						if (superClasses.contains(classNameWithDots)) {
							constructorInvoked = true;
							break;
						}
					} else {
						continue;
					}
				}
			}
		}
		
		/*
		if (methodName.startsWith("<init>")) {
			if (mn.instructions.size() >= 4) {
				start = mn.instructions.get(4);
				startID = 4;
			}
		}
		*/
		
		LCSAJ a = new LCSAJ(
		        className,
		        methodName,
		        BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
		                                                                        methodName,
		                                                                        startID,
		                                                                        start));
		lcsaj_queue.add(a);

		targets_reached.add(0);

		ArrayList<TryCatchBlockNode> tc_blocks = (ArrayList<TryCatchBlockNode>) mn.tryCatchBlocks;

		for (TryCatchBlockNode t : tc_blocks) {
			LCSAJ b = new LCSAJ(
			        className,
			        methodName,
			        BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
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
			                               BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
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

				}

				if (!targets_reached.contains(targetPosition)) {
					LCSAJ c = new LCSAJ(
					        className,
					        methodName,
					        BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
					                                                                        methodName,
					                                                                        targetPosition,
					                                                                        target));
					lcsaj_queue.add(c);

					targets_reached.add(targetPosition);
				}

			} else if (next instanceof TableSwitchInsnNode) {

				TableSwitchInsnNode tswitch = (TableSwitchInsnNode) next;
				List<LabelNode> allTargets = tswitch.labels;

				for (LabelNode target : allTargets) {

					int targetPosition = mn.instructions.indexOf(target);

					if (!targets_reached.contains(targetPosition)) {

						LCSAJ b = new LCSAJ(
						        className,
						        methodName,
						        BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
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

		if (Properties.STRATEGY != Strategy.EVOSUITE)
			addInstrumentation(classLoader, mn, className, methodName);

		//		if (Properties.WRITE_CFG)
		//			for (LCSAJ l : LCSAJPool.getLCSAJs(className, methodName)) {
		//				LCSAJGraph graph = new LCSAJGraph(l, false);
		//				String graphDestination = "evosuite-graphs/LCSAJGraphs/" + className
		//				        + "/" + methodName;
		//				File dir = new File(graphDestination);
		//				if (dir.mkdirs())
		//					graph.generate(new File(graphDestination + "/LCSAJGraph no: "
		//					        + l.getID() + ".dot"));
		//				else if (dir.exists())
		//					graph.generate(new File(graphDestination + "/LCSAJGraph no: "
		//					        + l.getID() + ".dot"));
		//			}
	}

	@SuppressWarnings("unchecked")
	private void addInstrumentation(ClassLoader classLoader, MethodNode mn,
	        String className, String methodName) {
		RawControlFlowGraph graph = GraphPool.getInstance(classLoader).getRawCFG(className,
		                                                                         methodName);
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for (BytecodeInstruction v : graph.vertexSet()) {

				// If this is in the CFG and it's a branch...
				if (in.equals(v.getASMNode())) {
					if (v.isForcedBranch()) {
						LCSAJPool.addLCSAJBranch(BranchPool.getInstance(classLoader).getBranchForInstruction(v));

						int branchId = BranchPool.getInstance(classLoader).getActualBranchIdForNormalBranchInstruction(v);
						InsnList instrumentation = new InsnList();
						instrumentation.add(new LdcInsnNode(v.getASMNode().getOpcode()));
						instrumentation.add(new LdcInsnNode(branchId));
						instrumentation.add(new LdcInsnNode(v.getInstructionId()));
						instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
						        "org/evosuite/testcase/ExecutionTracer",
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
	 * @see org.evosuite.cfg.MethodInstrumentation#executeOnExcludedMethods()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean executeOnExcludedMethods() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.MethodInstrumentation#executeOnMainMethod()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean executeOnMainMethod() {
		return false;
	}
	
}
