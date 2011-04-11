/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.cfg;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.coverage.concurrency.ConcurrencyCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.concurrency.ConcurrencyInstrumentation;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.AbstractMutationAdapter;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.testability.TransformationHelper;

/**
 * At the end of each method, create a minimized control flow graph for the
 * method and store it. In addition, this adapter also adds instrumentation for
 * branch distance measurement
 * 
 * defUse, concurrency and LCSAJs instrumentation is also added (if the
 * properties are set).
 * 
 * @author Gordon Fraser
 * 
 */
public class CFGMethodAdapter extends AbstractMutationAdapter {

	private final MethodVisitor next;
	private final String plain_name;
	private final List<Mutation> mutants;
	private int access = 0;

	public static final List<String> EXCLUDE = Arrays.asList("<clinit>",
	                                                         "__STATIC_RESET()V",
	                                                         "__STATIC_RESET");

	/**
	 * The set of all methods which
	 */
	public static Set<String> methods = new HashSet<String>();

	private static Map<String, Map<String, ControlFlowGraph>> completeGraphs = new HashMap<String, Map<String, ControlFlowGraph>>();
	private static Map<String, Map<String, ControlFlowGraph>> graphs = new HashMap<String, Map<String, ControlFlowGraph>>();
	private static Map<String, Map<String, Double>> diameters = new HashMap<String, Map<String, Double>>();

	private static Logger logger = Logger.getLogger(CFGMethodAdapter.class);

	private final String methodName, className;

	public CFGMethodAdapter(String className, int access, String name, String desc,
	        String signature, String[] exceptions, MethodVisitor mv,
	        List<Mutation> mutants) {
		super(new MethodNode(access, name, desc, signature, exceptions), className,
		        name.replace('/', '.'), null, desc);
		this.next = mv;
		this.className = className; // .replace('/', '.');
		this.access = access;
		this.methodName = name + desc;
		this.plain_name = name;
		this.mutants = mutants;
	}

	@Override
	public void visitEnd() {

		boolean isExcludedMethod = EXCLUDE.contains(methodName);
		boolean isMainMethod = plain_name.equals("main") && Modifier.isStatic(access);

		List<MethodInstrumentation> instrumentations = new ArrayList<MethodInstrumentation>();

		instrumentations.add(new BranchInstrumentation());

		if (Properties.CRITERION.equalsIgnoreCase(ConcurrencyCoverageFactory.CONCURRENCY_COVERAGE_CRITERIA)) {
			instrumentations.add(new ConcurrencyInstrumentation());
		} else if (Properties.CRITERION.equalsIgnoreCase("lcsaj")) {
			instrumentations.add(new LCSAJsInstrumentation());
		} else if (Properties.CRITERION.equalsIgnoreCase("defuse")) {
			instrumentations.add(new DefUseInstrumentation());
		} else if (Properties.CRITERION.equalsIgnoreCase("path")) {
			instrumentations.add(new PrimePathInstrumentation());
		}

		boolean executeOnMain = false;
		boolean executeOnExcluded = false;

		for (MethodInstrumentation instrumentation : instrumentations) {
			executeOnMain = executeOnMain || instrumentation.executeOnMainMethod();
			executeOnExcluded = executeOnExcluded
			        || instrumentation.executeOnExcludedMethods();
		}

		// super.visitEnd();
		// Generate CFG of method
		MethodNode mn = (MethodNode) mv;

		//Only instrument if the method is (not main and not excluded) or (the MethodInstrumentation wants it anyway)
		if ((!isMainMethod || executeOnMain) && (!isExcludedMethod || executeOnExcluded)) {

			// MethodNode mn = new CFGMethodNode((MethodNode)mv);
			// System.out.println("Generating CFG for "+ className+"."+mn.name +
			// " ("+mn.desc +")");
			CFGGenerator g = new CFGGenerator(mutants);
			logger.info("Generating CFG for method " + methodName);

			try {
				g.getCFG(className, methodName, mn);
				logger.trace("Method graph for " + className + "." + methodName
				        + " contains " + g.getGraph().vertexSet().size() + " nodes for "
				        + g.getFrames().length + " instructions");
			} catch (AnalyzerException e) {
				logger.warn("Analyzer exception while analyzing " + className + "."
				        + methodName);
				e.printStackTrace();
			}

			// non-minimized cfg needed for defuse-coverage and control dependence
			// calculation
			addCompleteCFG(className, methodName, g.getGraph());
			addCFG(className, methodName, g.getMinimalGraph());
			logger.info("Created CFG for method " + methodName);

			// if(!Properties.MUTATION) {
			Graph<CFGVertex, DefaultEdge> graph = g.getGraph();

			for (MethodInstrumentation instrumentation : instrumentations) {
				instrumentation.analyze(mn, graph, className, methodName, access);
			}

			handleBranchlessMethods();
			logger.info("Analyzing method " + methodName);

			String id = className + "." + methodName;
			if (isUsable()) {
				methods.add(id);
				logger.debug("Counting: " + id);
			}
		}

		mn.accept(next);
	}

	private void handleBranchlessMethods() {
		String id = className + "." + methodName;
		if (BranchPool.getBranchCountForMethod(id) == 0) {
			if (isUsable()) {
				if (Properties.TESTABILITY_TRANSFORMATION) {
					String vname = methodName.replace("(", "|(");
					if (TransformationHelper.hasValkyrieMethod(className, vname))
						return;
				}

				logger.debug("Method has no branches: " + id);
				BranchPool.addBranchlessMethod(id);
			}
		}
	}

	private boolean isUsable() {
		return !((this.access & Opcodes.ACC_SYNTHETIC) > 0 || (this.access & Opcodes.ACC_BRIDGE) > 0)
		        && !methodName.contains("<clinit>")
		        && !(methodName.contains("<init>") && (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)
		        && (Properties.USE_DEPRECATED || (access & Opcodes.ACC_DEPRECATED) != Opcodes.ACC_DEPRECATED);
	}

	public static void addCFG(String classname, String methodname,
	        DirectedMultigraph<CFGVertex, DefaultEdge> graph) {
		if (!graphs.containsKey(classname)) {
			graphs.put(classname, new HashMap<String, ControlFlowGraph>());
			diameters.put(classname, new HashMap<String, Double>());
		}
		Map<String, ControlFlowGraph> methods = graphs.get(classname);
		logger.debug("Added CFG for class " + classname + " and method " + methodname);
		methods.put(methodname, new ControlFlowGraph(graph, true));
		FloydWarshall<CFGVertex, DefaultEdge> f = new FloydWarshall<CFGVertex, DefaultEdge>(
		        graph);
		diameters.get(classname).put(methodname, f.getDiameter());
		logger.debug("Calculated diameter for " + classname + ": " + f.getDiameter());
	}

	public static void addCompleteCFG(String classname, String methodname,
	        DefaultDirectedGraph<CFGVertex, DefaultEdge> graph) {
		if (!completeGraphs.containsKey(classname)) {
			completeGraphs.put(classname, new HashMap<String, ControlFlowGraph>());
		}
		Map<String, ControlFlowGraph> methods = completeGraphs.get(classname);
		logger.debug("Added complete CFG for class " + classname + " and method "
		        + methodname);
		methods.put(methodname, new ControlFlowGraph(graph, false));
		//ControlFlowGraph cfg = new ControlFlowGraph(graph, false);
		//cfg.toDot(classname + "_" + methodname + ".dot");
	}

	public static ControlFlowGraph getCFG(String classname, String methodname) {
		logger.debug("Getting CFG for class " + classname + " and method " + methodname);
		if (graphs.get(classname) == null)
			return null;
		return graphs.get(classname).get(methodname);
	}

	public static ControlFlowGraph getCompleteCFG(String classname, String methodname) {
		if (completeGraphs.get(classname) == null)
			return null;
		return completeGraphs.get(classname).get(methodname);
	}
}
