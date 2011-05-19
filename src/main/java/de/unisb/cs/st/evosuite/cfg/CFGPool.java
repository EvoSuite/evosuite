package de.unisb.cs.st.evosuite.cfg;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class CFGPool {

	private static Logger logger = Logger.getLogger(CFGPool.class);
	
	
	/**
	 * Complete control flow graph, contains each bytecode instruction, each
	 * label and line number node Think of the direct Known Subclasses of
	 * http://
	 * asm.ow2.org/asm33/javadoc/user/org/objectweb/asm/tree/AbstractInsnNode
	 * .html for a complete list of the nodes in this cfg
	 */
	private static Map<String, Map<String, RawControlFlowGraph>> completeCFGs = new HashMap<String, Map<String, RawControlFlowGraph>>();

	/**
	 * Minimized control flow graph. This graph only contains the first and last
	 * node (usually a LABEL and IRETURN), nodes which create branches (all
	 * jumps/switches except GOTO) and nodes which were mutated.
	 */
	private static Map<String, Map<String, ActualControlFlowGraph>> minimizedCFGs = new HashMap<String, Map<String, ActualControlFlowGraph>>();

	//TODO do these get used anywhere?
//	private static Map<String, Map<String, Double>> diameters = new HashMap<String, Map<String, Double>>();	
	
	
	
	public static void addMinimizedCFG(ActualControlFlowGraph cfg) {
		String className = cfg.getClassName();
		String methodName = cfg.getMethodName();
		
		if (!minimizedCFGs.containsKey(className)) {
			minimizedCFGs.put(className, new HashMap<String, ActualControlFlowGraph>());
//			diameters.put(className, new HashMap<String, Double>());
		}
		Map<String, ActualControlFlowGraph> methods = minimizedCFGs.get(className);
		logger.debug("Added CFG for class " + className + " and method " + methodName);
		cfg.finalize();
		methods.put(methodName, cfg);
		
//		diameters.get(className).put(methodName, cfg.getDiameter());
//		logger.debug("Calculated diameter for " + className + ": " + cfg.getDiameter());
	}

	public static void addCompleteCFG(RawControlFlowGraph cfg) {
		String className = cfg.getClassName();
		String methodName = cfg.getMethodName();
		
		if (!completeCFGs.containsKey(className)) {
			completeCFGs.put(className, new HashMap<String, RawControlFlowGraph>());
		}
		Map<String, RawControlFlowGraph> methods = completeCFGs.get(className);
		logger.debug("Added complete CFG for class " + className + " and method "
		        + methodName);
		methods.put(methodName, cfg);
		//ControlFlowGraph cfg = new ControlFlowGraph(graph, false);
		//cfg.toDot(classname + "_" + methodname + ".dot");
	}

	public static ActualControlFlowGraph getMinimizedCFG(String className, String methodName) {
		logger.debug("Getting minimzed CFG for " + className + "." + methodName);
		if(minimizedCFGs.get(className) == null)
			return null;
		
		return minimizedCFGs.get(className).get(methodName);
	}

	public static RawControlFlowGraph getCompleteCFG(String className, String methodName) {
		logger.debug("Getting complete CFG for " + className + "." + methodName);
		if (completeCFGs.get(className) == null)
			return null;
		
		return completeCFGs.get(className).get(methodName);
	}
}
