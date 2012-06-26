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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.dataflow.DefUse;
import org.evosuite.coverage.dataflow.DefUseFactory;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;


/**
 * @author copied from CFGMethodAdapter
 * 
 */
public class DefUseInstrumentation implements MethodInstrumentation {

	private static Logger logger = LoggerFactory
			.getLogger(DefUseInstrumentation.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb
	 * .asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String,
	 * java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void analyze(MethodNode mn, String className, String methodName,
			int access) {
		RawControlFlowGraph completeCFG = GraphPool.getRawCFG(className,
				methodName);
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();
		while (j.hasNext()) {

			AbstractInsnNode in = j.next();
			for (BytecodeInstruction v : completeCFG.vertexSet()) {

				// if (in.equals(v.getASMNode()))
				// v.branchId =
				// completeCFG.getInstruction(v.getId()).getBranchId();

				if ((Properties.CRITERION == Criterion.DEFUSE
						|| Properties.CRITERION == Criterion.ALLDEFS || Properties.CRITERION == Criterion.ANALYZE || TestSuiteGenerator.analyzing)
						&& in.equals(v.getASMNode()) && (v.isDefUse())) {

					// keeping track of uses
					boolean isValidDU = false;
					if (v.isUse())
						isValidDU = DefUsePool.addAsUse(v);
					// keeping track of definitions
					if (v.isDefinition())
						isValidDU = DefUsePool.addAsDefinition(v) || isValidDU;

					if (isValidDU) {
						boolean staticContext = v.isStaticDefUse()
								|| ((access & Opcodes.ACC_STATIC) > 0);
						// adding instrumentation for defuse-coverage
						InsnList instrumentation = getInstrumentation(v,
								staticContext, className, methodName);
						if (instrumentation == null)
							throw new IllegalStateException(
									"error instrumenting node " + v.toString());

						// AbstractInsnNode prev = v.getASMNode().getPrevious();
						// if(prev == null) // no previous instruction
						// mn.instructions.insert(instrumentation);
						// else
						mn.instructions.insertBefore(v.getASMNode(),
								instrumentation);
					}
				}
			}
		}
	}

	/**
	 * Creates the instrumentation needed to track defs and uses
	 * 
	 */
	private InsnList getInstrumentation(BytecodeInstruction v,
			boolean staticContext, String className, String methodName) {
		InsnList instrumentation = new InsnList();

		// DONE you only have to pass the defID/useID and not the variable and
		// class anymore, that can be retrieved from the DefUsePool
		// TODO clean up
		// TODO sanity check matching method/class names and field values of v?
		if (!v.isDefUse()) {
			logger
					.warn("unexpected DefUseInstrumentation call for a non-DU-instruction");
			return instrumentation;
		}
		DefUse targetDU = DefUseFactory.makeInstance(v);
		// System.out.println("instrumenting: "+targetDU.toString());
		if (DefUsePool.isKnownAsUse(v)) {

			if (targetDU.getUseId() != DefUsePool.getUseCounter())
				throw new IllegalStateException(v.toString() + " "
						+ targetDU.toString());

//			instrumentation.add(new LdcInsnNode(className));
//			instrumentation.add(new LdcInsnNode(targetDU.getDUVariableName()));
//			instrumentation.add(new LdcInsnNode(methodName));
			if (staticContext) {
				instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
			} else {
				instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0)); // "this"
			}
			instrumentation.add(new LdcInsnNode(DefUsePool.getUseCounter()));
			instrumentation
					.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
							"org/evosuite/testcase/ExecutionTracer",
							"passedUse",
							"(Ljava/lang/Object;I)V"));
		}
		if (DefUsePool.isKnownAsDefinition(v)) {
//			instrumentation.add(new LdcInsnNode(className));
//			instrumentation.add(new LdcInsnNode(targetDU.getDUVariableName()));
//			instrumentation.add(new LdcInsnNode(methodName));
			if (staticContext) {
				instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
			} else {
				instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0)); // "this"
			}
			instrumentation.add(new LdcInsnNode(DefUsePool.getDefCounter()));
			instrumentation
					.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
							"org/evosuite/testcase/ExecutionTracer",
							"passedDefinition",
							"(Ljava/lang/Object;I)V"));
		}
		return instrumentation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.cfg.MethodInstrumentation#executeOnExcludedMethods
	 * ()
	 */
	@Override
	public boolean executeOnExcludedMethods() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.cfg.MethodInstrumentation#executeOnMainMethod()
	 */
	@Override
	public boolean executeOnMainMethod() {
		return false;
	}

}
