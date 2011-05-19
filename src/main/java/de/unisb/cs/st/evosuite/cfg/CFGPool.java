package de.unisb.cs.st.evosuite.cfg;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

public class CFGPool {

	private static Logger logger = Logger.getLogger(CFGPool.class);
	
	
	/**
	 * Complete control flow graph, contains each bytecode instruction, each
	 * label and line number node Think of the direct Known Subclasses of
	 * http://
	 * asm.ow2.org/asm33/javadoc/user/org/objectweb/asm/tree/AbstractInsnNode
	 * .html for a complete list of the nodes in this cfg
	 */
	private static Map<String, Map<String, ControlFlowGraph>> completeCFGs = new HashMap<String, Map<String, ControlFlowGraph>>();

	/**
	 * Minimized control flow graph. This graph only contains the first and last
	 * node (usually a LABEL and IRETURN), nodes which create branches (all
	 * jumps/switches except GOTO) and nodes which were mutated.
	 */
	private static Map<String, Map<String, ControlFlowGraph>> minimizedCFGs = new HashMap<String, Map<String, ControlFlowGraph>>();

	private static Map<String, Map<String, Double>> diameters = new HashMap<String, Map<String, Double>>();	
	
	
	
	public static void addMinimizedCFG(String className, String methodName,
	        DirectedMultigraph<BytecodeInstruction, DefaultEdge> graph) {
		if (!minimizedCFGs.containsKey(className)) {
			minimizedCFGs.put(className, new HashMap<String, ControlFlowGraph>());
			diameters.put(className, new HashMap<String, Double>());
		}
		Map<String, ControlFlowGraph> methods = minimizedCFGs.get(className);
		logger.debug("Added CFG for class " + className + " and method " + methodName);
		methods.put(methodName, new ControlFlowGraph(graph, true));
		FloydWarshall<BytecodeInstruction, DefaultEdge> f = new FloydWarshall<BytecodeInstruction, DefaultEdge>(
		        graph);
		diameters.get(className).put(methodName, f.getDiameter());
		logger.debug("Calculated diameter for " + className + ": " + f.getDiameter());
	}

	public static void addCompleteCFG(String className, String methodName,
	        DirectedGraph<BytecodeInstruction, DefaultEdge> graph) {
		if (!completeCFGs.containsKey(className)) {
			completeCFGs.put(className, new HashMap<String, ControlFlowGraph>());
		}
		Map<String, ControlFlowGraph> methods = completeCFGs.get(className);
		logger.debug("Added complete CFG for class " + className + " and method "
		        + methodName);
		methods.put(methodName, new ControlFlowGraph(graph, false));
		//ControlFlowGraph cfg = new ControlFlowGraph(graph, false);
		//cfg.toDot(classname + "_" + methodname + ".dot");
	}

	public static ControlFlowGraph getMinimizedCFG(String className, String methodName) {
		logger.debug("Getting minimzed CFG for " + className + "." + methodName);
		if(minimizedCFGs.get(className) == null)
			return null;
		
		return minimizedCFGs.get(className).get(methodName);
	}

	public static ControlFlowGraph getCompleteCFG(String className, String methodName) {
		logger.debug("Getting complete CFG for " + className + "." + methodName);
		if (completeCFGs.get(className) == null)
			return null;
		
		return completeCFGs.get(className).get(methodName);
	}
}
