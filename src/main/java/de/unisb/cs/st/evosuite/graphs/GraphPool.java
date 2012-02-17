package de.unisb.cs.st.evosuite.graphs;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.graphs.ccfg.ClassControlFlowGraph;
import de.unisb.cs.st.evosuite.graphs.ccg.ClassCallGraph;
import de.unisb.cs.st.evosuite.graphs.cdg.ControlDependenceGraph;
import de.unisb.cs.st.evosuite.graphs.cfg.ActualControlFlowGraph;
import de.unisb.cs.st.evosuite.graphs.cfg.RawControlFlowGraph;

/**
 * Gives access to all Graphs computed during CUT analysis such as CFGs created
 * by the CFGGenerator and BytcodeAnalyzer in the CFGMethodAdapter
 * 
 * For each CUT and each of their methods a Raw- and an ActualControlFlowGraph
 * instance are stored within this pool. Additionally a ControlDependenceGraph
 * is computed and stored for each such method.
 * 
 * This pool also offers the possibility to generate the ClassCallGraph and
 * ClassControlFlowGraph for a CUT. They represents the call hierarchy and
 * interaction of different methods within a class.
 * 
 * @author Andre Mis
 */
public class GraphPool {

	private static Logger logger = LoggerFactory.getLogger(GraphPool.class);

	/**
	 * Complete control flow graph, contains each bytecode instruction, each
	 * label and line number node Think of the direct Known Subclasses of
	 * http://
	 * asm.ow2.org/asm33/javadoc/user/org/objectweb/asm/tree/AbstractInsnNode
	 * .html for a complete list of the nodes in this cfg
	 * 
	 * Maps from classNames to methodNames to corresponding RawCFGs
	 */
	private static Map<String, Map<String, RawControlFlowGraph>> rawCFGs = new HashMap<String, Map<String, RawControlFlowGraph>>();

	/**
	 * Minimized control flow graph. This graph only contains the first and last
	 * node (usually a LABEL and IRETURN), nodes which create branches (all
	 * jumps/switches except GOTO) and nodes which were mutated.
	 * 
	 * Maps from classNames to methodNames to corresponding ActualCFGs
	 */
	private static Map<String, Map<String, ActualControlFlowGraph>> actualCFGs = new HashMap<String, Map<String, ActualControlFlowGraph>>();

	/**
	 * Control Dependence Graphs for each method.
	 * 
	 * Maps from classNames to methodNames to corresponding CDGs
	 */
	private static Map<String, Map<String, ControlDependenceGraph>> controlDependencies = new HashMap<String, Map<String, ControlDependenceGraph>>();

	// retrieve graphs

	public static RawControlFlowGraph getRawCFG(String className,
			String methodName) {

		if (rawCFGs.get(className) == null) {
			logger.warn("Class unknown: " + className);
			logger.warn(rawCFGs.keySet().toString());
			return null;
		}

		return rawCFGs.get(className).get(methodName);
	}

	public static Map<String, RawControlFlowGraph> getRawCFGs(String className) {
		if (rawCFGs.get(className) == null) {
			logger.warn("Class unknown: " + className);
			logger.warn(rawCFGs.keySet().toString());
			return null;
		}

		return rawCFGs.get(className);
	}

	public static ActualControlFlowGraph getActualCFG(String className,
			String methodName) {

		if (actualCFGs.get(className) == null)
			return null;

		return actualCFGs.get(className).get(methodName);
	}

	public static ControlDependenceGraph getCDG(String className,
			String methodName) {

		if (controlDependencies.get(className) == null)
			return null;

		return controlDependencies.get(className).get(methodName);
	}

	// register graphs

	public static void registerRawCFG(RawControlFlowGraph cfg) {
		String className = cfg.getClassName();
		String methodName = cfg.getMethodName();

		if (className == null || methodName == null)
			throw new IllegalStateException(
					"expect class and method name of CFGs to be set before entering the GraphPool");

		if (!rawCFGs.containsKey(className)) {
			rawCFGs.put(className, new HashMap<String, RawControlFlowGraph>());
		}
		Map<String, RawControlFlowGraph> methods = rawCFGs.get(className);
		logger.debug("Added complete CFG for class " + className
				+ " and method " + methodName);
		methods.put(methodName, cfg);

		if (Properties.WRITE_CFG)
			cfg.toDot();
	}

	public static void registerActualCFG(ActualControlFlowGraph cfg) {
		String className = cfg.getClassName();
		String methodName = cfg.getMethodName();

		if (className == null || methodName == null)
			throw new IllegalStateException(
					"expect class and method name of CFGs to be set before entering the GraphPool");

		if (!actualCFGs.containsKey(className)) {
			actualCFGs.put(className,
					new HashMap<String, ActualControlFlowGraph>());
			// diameters.put(className, new HashMap<String, Double>());
		}
		Map<String, ActualControlFlowGraph> methods = actualCFGs.get(className);
		logger.debug("Added CFG for class " + className + " and method "
				+ methodName);
		cfg.finalise();
		methods.put(methodName, cfg);

		if (Properties.WRITE_CFG)
			cfg.toDot();

		createAndRegisterControlDependence(cfg);
	}

	private static void createAndRegisterControlDependence(
			ActualControlFlowGraph cfg) {

		ControlDependenceGraph cd = new ControlDependenceGraph(cfg);

		String className = cd.getClassName();
		String methodName = cd.getMethodName();

		if (className == null || methodName == null)
			throw new IllegalStateException(
					"expect class and method name of CFGs to be set before entering the GraphPool");

		if (!controlDependencies.containsKey(className))
			controlDependencies.put(className,
					new HashMap<String, ControlDependenceGraph>());
		Map<String, ControlDependenceGraph> cds = controlDependencies
				.get(className);

		cds.put(methodName, cd);
		if (Properties.WRITE_CFG)
			cd.toDot();
	}

	public static void computeCCFGs() {

		for (String className : rawCFGs.keySet()) {
			ClassCallGraph ccg = new ClassCallGraph(className);
			if (Properties.WRITE_CFG)
				ccg.toDot();

			ClassControlFlowGraph ccfg = new ClassControlFlowGraph(ccg);
			if (Properties.WRITE_CFG)
				ccfg.toDot();
		}
	}

	public static void clear() {
		rawCFGs.clear();
		actualCFGs.clear();
		controlDependencies.clear();
	}

	public static void clear(String className) {
		rawCFGs.remove(className);
		actualCFGs.remove(className);
		controlDependencies.remove(className);
	}

	public static void clear(String className, String methodName) {
		if (rawCFGs.containsKey(className))
			rawCFGs.get(className).remove(methodName);
		if (actualCFGs.containsKey(className))
			actualCFGs.get(className).remove(methodName);
		if (controlDependencies.containsKey(className))
			controlDependencies.get(className).remove(methodName);
	}

}
