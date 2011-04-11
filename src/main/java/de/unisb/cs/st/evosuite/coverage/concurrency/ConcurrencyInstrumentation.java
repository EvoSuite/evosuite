/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.Iterator;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.MethodInstrumentation;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

/**
 * @author Sebastian Steenbuck
 *
 */
public class ConcurrencyInstrumentation implements MethodInstrumentation{
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void analyze(MethodNode mn, Graph<CFGVertex, DefaultEdge> graph, String className, String methodName, int access) {
		ControlFlowGraph completeCFG = CFGMethodAdapter.getCompleteCFG(className, methodName);
		Iterator<AbstractInsnNode> instructions = mn.instructions.iterator();
		while (instructions.hasNext()) {
			AbstractInsnNode instruction = instructions.next();
			for (CFGVertex v : graph.vertexSet()) {
				if (instruction.equals(v.getNode())){
					v.branchId = completeCFG.getVertex(v.getId()).branchId;
				}
				//#TODO steenbuck the true should be some command line option to activate the concurrency stuff
				if (true && 
						instruction.equals(v.getNode()) && 
						v.isFieldUse() &&
						instruction instanceof FieldInsnNode &&
						 //#FIXME steenbuck we should also instrument fields, which access primitive types.
						//#FIXME steenbuck apparently some objects (like Boolean, Integer) are not working with this, likely a different Signature (disappears when all getfield/getstatic points are instrumented)
						((FieldInsnNode)instruction).desc.startsWith("L")) { //we only want objects, as primitive types are passed by value
					// adding instrumentation for scheduling-coverage
					mn.instructions.insert(v.getNode(),
							getConcurrencyInstrumentation(v, v.branchId));

					// keeping track of definitions
					/*if (v.isDefinition())
						DefUsePool.addDefinition(v);

					// keeping track of uses
					if (v.isUse())
						DefUsePool.addUse(v);*/

				}

			}
		}
	}
	
	private InsnList getConcurrencyInstrumentation(CFGVertex v, int currentBranch) {
		InsnList instrumentation = new InsnList();
		switch (v.getNode().getOpcode()) {
		case Opcodes.GETFIELD:
		case Opcodes.GETSTATIC:
			//System.out.println("as seen in instrument:" + v.node.getClass() + " branchID: " + currentBranch +  " line " + v.line_no);
			int accessID = LockRuntime.getFieldAccessID();
			LockRuntime.mapFieldAccessIDtoCFGid(accessID, currentBranch, v);
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new IntInsnNode(Opcodes.BIPUSH, accessID));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					LockRuntime.RUNTIME_CLASS, 
					LockRuntime.RUNTIME_SCHEDULER_CALL_METHOD,
					"(Ljava/lang/Object;I)V"));
			break;
		default:
			//Should never be reached. As the method calling this method checks for the opcodes
			throw new AssertionError();
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
		return true;
	}
	
	
}
