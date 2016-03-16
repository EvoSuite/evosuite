/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
/**
 * 
 */
package org.evosuite.instrumentation.coverage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.utils.ArrayUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
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

			    if ((ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
			            || ArrayUtil.contains(Properties.CRITERION, Criterion.ALLDEFS))
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
						                                              methodName,
						                                              mn);
						if (instrumentation == null)
							throw new IllegalStateException("error instrumenting node "
							        + v.toString());

						if (v.isMethodCallOfField())
							mn.instructions.insertBefore(v.getASMNode(), instrumentation);
						else if(v.isArrayStoreInstruction())
							mn.instructions.insertBefore(v.getSourceOfArrayReference().getASMNode(), instrumentation);

						// Loading of an array is already handled by ALOAD
						// AILOAD would only be needed if we define DU pairs on 
						// array indices
						//						else if(v.isArrayLoadInstruction())
						//							mn.instructions.insertBefore(v.getSourceOfArrayReference().getASMNode(), instrumentation);
						else if(v.isUse())
							mn.instructions.insert(v.getASMNode(), instrumentation);
						else
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
	        String className, String methodName, MethodNode mn) {
		InsnList instrumentation = new InsnList();

		if (!v.isDefUse()) {
			logger.warn("unexpected DefUseInstrumentation call for a non-DU-instruction");
			return instrumentation;
		}

		if (DefUsePool.isKnownAsFieldMethodCall(v)) {
			return getMethodInstrumentation(v, staticContext, instrumentation, mn);
		}

		if (DefUsePool.isKnownAsUse(v)) {
			// The actual object that is defined is on the stack _after_ the load instruction
			addObjectInstrumentation(v, instrumentation, mn);
			addCallingObjectInstrumentation(staticContext, instrumentation);
			instrumentation.add(new LdcInsnNode(DefUsePool.getUseCounter()));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(ExecutionTracer.class), "passedUse",
			        "(Ljava/lang/Object;Ljava/lang/Object;I)V"));
		}
		if (DefUsePool.isKnownAsDefinition(v)) {
			// The actual object that is defined is on the stack _before_ the store instruction
			addObjectInstrumentation(v, instrumentation, mn);
			addCallingObjectInstrumentation(staticContext, instrumentation);
			instrumentation.add(new LdcInsnNode(DefUsePool.getDefCounter()));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(org.evosuite.testcase.execution.ExecutionTracer.class), "passedDefinition",
			        "(Ljava/lang/Object;Ljava/lang/Object;I)V"));
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
	
	@SuppressWarnings({ "unchecked", "unused" })
	private int getNextLocalVariable(MethodNode mn) {
		int var = 1;
		List<LocalVariableNode> nodes = mn.localVariables;
		for(LocalVariableNode varNode : nodes) {
			if(varNode.index >= var) {
				var = varNode.index + 1;
			}
		}
		return var;
	}
	
	private void addObjectInstrumentation(BytecodeInstruction instruction, InsnList instrumentation, MethodNode mn) {
		if(instruction.isLocalVariableDefinition()) {
			if(instruction.getASMNode().getOpcode() == Opcodes.ALOAD) {
				instrumentation.add(new InsnNode(Opcodes.DUP));				
			} else {
				instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
			}
		} else if(instruction.isLocalVariableUse()){
			if(instruction.getASMNode().getOpcode() == Opcodes.ASTORE) {
				instrumentation.add(new InsnNode(Opcodes.DUP));
			} else {
				instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
			}
		} else if(instruction.isArrayStoreInstruction()) {
			// Object, index, value
			instrumentation.add(new InsnNode(Opcodes.DUP));
//		} else if(instruction.isArrayLoadInstruction()) {
//			instrumentation.add(new InsnNode(Opcodes.DUP));
		} else if(instruction.isFieldNodeDU()) {
			// TODO: FieldNodeDU takes care of ArrayStore - why?
			Type type = Type.getType(instruction.getFieldType());
			if(type.getSort() == Type.OBJECT) {
				instrumentation.add(new InsnNode(Opcodes.DUP));
			} else {
				instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
			}
		} else if(instruction.isMethodCall()) {
			Type type = Type.getReturnType(instruction.getMethodCallDescriptor());
			if(type.getSort() == Type.OBJECT) {
				instrumentation.add(new InsnNode(Opcodes.DUP));
			} else {
				instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private int getNextLocalNum(MethodNode mn) {
		List<LocalVariableNode> variables = mn.localVariables;
		int max = 0;
		for(LocalVariableNode node : variables) {
			if(node.index > max)
				max = node.index;
		}
		return max + 1;
	}
	
	private InsnList getMethodInstrumentation(BytecodeInstruction call, boolean staticContext, InsnList instrumentation, MethodNode mn) {


		String descriptor = call.getMethodCallDescriptor();
		Type[] args = Type.getArgumentTypes(descriptor);
		int loc = getNextLocalNum(mn);
		Map<Integer, Integer> to = new HashMap<Integer, Integer>();
		for (int i = args.length - 1; i >= 0; i--) {
			Type type = args[i];
			instrumentation.add(new VarInsnNode(type.getOpcode(Opcodes.ISTORE), loc));
			to.put(i, loc);
			loc++;
		}

		// instrumentation.add(new InsnNode(Opcodes.DUP));//callee
		addObjectInstrumentation(call, instrumentation, mn);
		addCallingObjectInstrumentation(staticContext, instrumentation);
		// field method calls get special treatment:
		// during instrumentation it is not clear whether a field method
		// call constitutes a definition or a use. So the instrumentation
		// will call a special function of the ExecutionTracer which will
		// redirect the call to either passedUse() or passedDefinition()
		// using the information available during runtime (the CCFGs)
		instrumentation.add(new LdcInsnNode(DefUsePool.getDefUseCounter()));
		instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				PackageInfo.getNameWithSlash(ExecutionTracer.class), "passedFieldMethodCall",
		        "(Ljava/lang/Object;Ljava/lang/Object;I)V"));
		

		for (int i = 0; i < args.length; i++) {
			Type type = args[i];
			instrumentation.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), to.get(i)));
		}
		
		return instrumentation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.cfg.MethodInstrumentation#executeOnExcludedMethods ()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean executeOnExcludedMethods() {
		return false;
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
