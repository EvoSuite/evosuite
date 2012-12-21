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

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.rmi.ClientServices;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * DefUseInstrumentation class.
 * </p>
 * 
 * @author Andre Mis
 */
public class DefUseInstrumentation implements MethodInstrumentation {

	private static Logger logger = LoggerFactory.getLogger(DefUseInstrumentation.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb
	 * .asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String,
	 * java.lang.String)
	 */
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void analyze(ClassLoader classLoader, MethodNode mn, String className,
	        String methodName, int access) {
		RawControlFlowGraph completeCFG = GraphPool.getInstance(classLoader).getRawCFG(className,
		                                                                               methodName);
		logger.info("Applying DefUse instrumentation on CFG with "+completeCFG.vertexCount() +" nodes");
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for (BytecodeInstruction v : completeCFG.vertexSet()) {

				if ((Properties.CRITERION == Criterion.DEFUSE
				        || Properties.CRITERION == Criterion.ALLDEFS
				        || Properties.CRITERION == Criterion.ANALYZE || TestSuiteGenerator.analyzing)
				        && in.equals(v.getASMNode()) && v.isDefUse()) {

					boolean isValidDU = false;

//					if(v.isLocalArrayDefinition()) {
//						LoggingUtils.getEvoLogger().info(
//							"LOCAL ARRAY VAR DEF " + v.toString()+" loaded by "+v.getSourceOfStackInstruction(2).toString());
//					}
					
					if (v.isMethodCallOfField()) {
						// keep track of field method calls, though we do not
						// know
						// how to handle them at this point during the analysis
						// (need complete CCFGs first)
						isValidDU = DefUsePool.addAsFieldMethodCall(v);
					} else {
						// keep track of uses
						if (v.isUse())
							isValidDU = DefUsePool.addAsUse(v);
						// keep track of definitions
						if (v.isDefinition())
							isValidDU = DefUsePool.addAsDefinition(v) || isValidDU;
					}
					if (isValidDU) {
						boolean staticContext = v.isStaticDefUse()
						        || ((access & Opcodes.ACC_STATIC) > 0);
						// adding instrumentation for defuse-coverage
						InsnList instrumentation = getInstrumentation(v, staticContext,
						                                              className,
						                                              methodName);
						if (instrumentation == null)
							throw new IllegalStateException("error instrumenting node "
							        + v.toString());

						mn.instructions.insertBefore(v.getASMNode(), instrumentation);
					}
				}
			}
		}
	}

	/**
	 * Creates the instrumentation needed to track defs and uses
	 * 
	 */
	private InsnList getInstrumentation(BytecodeInstruction v, boolean staticContext,
	        String className, String methodName) {
		InsnList instrumentation = new InsnList();

		if (!v.isDefUse()) {
			logger.warn("unexpected DefUseInstrumentation call for a non-DU-instruction");
			return instrumentation;
		}

		if (DefUsePool.isKnownAsFieldMethodCall(v)) {
			addCallingObjectInstrumentation(staticContext, instrumentation);
			// field method calls get special treatment:
			// during instrumentation it is not clear whether a field method
			// call constitutes a definition or a use. So the instrumentation
			// will call a special function of the ExecutionTracer which will
			// redirect the call to either passedUse() or passedDefinition()
			// using the information available during runtime (the CCFGs)
			instrumentation.add(new LdcInsnNode(DefUsePool.getDefUseCounter()));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "org/evosuite/testcase/ExecutionTracer", "passedFieldMethodCall",
			        "(Ljava/lang/Object;I)V"));

			return instrumentation;
		}

		if (DefUsePool.isKnownAsUse(v)) {
			addCallingObjectInstrumentation(staticContext, instrumentation);
			instrumentation.add(new LdcInsnNode(DefUsePool.getUseCounter()));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "org/evosuite/testcase/ExecutionTracer", "passedUse",
			        "(Ljava/lang/Object;I)V"));
		}
		if (DefUsePool.isKnownAsDefinition(v)) {
			addCallingObjectInstrumentation(staticContext, instrumentation);
			instrumentation.add(new LdcInsnNode(DefUsePool.getDefCounter()));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "org/evosuite/testcase/ExecutionTracer", "passedDefinition",
			        "(Ljava/lang/Object;I)V"));
		}

		return instrumentation;
	}

	private void addCallingObjectInstrumentation(boolean staticContext,
	        InsnList instrumentation) {
		// the object on which the DU is covered is passed by the
		// instrumentation.
		// If we are in a static context, null is passed instead
		if (staticContext) {
			instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
		} else {
			instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0)); // "this"
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.cfg.MethodInstrumentation#executeOnExcludedMethods ()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean executeOnExcludedMethods() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.cfg.MethodInstrumentation#executeOnMainMethod()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean executeOnMainMethod() {
		return false;
	}

}
