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
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.cfg.instrumentation.BranchInstrumentation;
import de.unisb.cs.st.evosuite.cfg.instrumentation.DefUseInstrumentation;
import de.unisb.cs.st.evosuite.cfg.instrumentation.LCSAJsInstrumentation;
import de.unisb.cs.st.evosuite.cfg.instrumentation.MethodInstrumentation;
import de.unisb.cs.st.evosuite.cfg.instrumentation.PrimePathInstrumentation;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.coverage.concurrency.ConcurrencyInstrumentation;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.AbstractMutationAdapter;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.testability.TransformationHelper;

/**
 * Create a minimized control flow graph for the method and store it. In
 * addition, this adapter also adds instrumentation for branch distance
 * measurement
 * 
 * defUse, concurrency and LCSAJs instrumentation is also added (if the
 * properties are set).
 * 
 * @author Gordon Fraser
 * 
 */
public class CFGMethodAdapter extends AbstractMutationAdapter {

	private static Logger logger = Logger.getLogger(CFGMethodAdapter.class);

	/**
	 * A list of Strings representing method signatures. Methods matching those
	 * signatures are not instrumented and no CFG is generated for them. Except
	 * if some MethodInstrumentation requests it.
	 */
	public static final List<String> EXCLUDE = Arrays.asList("<clinit>",
	                                                         "__STATIC_RESET()V",
	                                                         "__STATIC_RESET");
	/**
	 * The set of all methods which can be used during test case generation This
	 * excludes e.g. synthetic, initializers, private and deprecated methods
	 */
	public static Set<String> methods = new HashSet<String>();

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

	/**
	 * This is the name + the description of the method. It is more like the
	 * signature and less like the name. The name of the method can be found in
	 * this.plain_name
	 */
	private final String methodName;

	private final MethodVisitor next;
	private final String plain_name;
	private final List<Mutation> mutants;
	private final int access;
	private final String className;

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

		if (Properties.CRITERION == Criterion.CONCURRENCY) {
			instrumentations.add(new ConcurrencyInstrumentation());
			instrumentations.add(new BranchInstrumentation());
		} else if (Properties.CRITERION ==Criterion.LCSAJ) {
			instrumentations.add(new LCSAJsInstrumentation());
			instrumentations.add(new BranchInstrumentation());
		} else if (Properties.CRITERION ==Criterion.DEFUSE) {
			instrumentations.add(new BranchInstrumentation());
			instrumentations.add(new DefUseInstrumentation());
		} else if (Properties.CRITERION ==Criterion.PATH) {
			instrumentations.add(new PrimePathInstrumentation());
			instrumentations.add(new BranchInstrumentation());
		} else {
			instrumentations.add(new BranchInstrumentation());
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
		if ((!isMainMethod || executeOnMain) && (!isExcludedMethod || executeOnExcluded)
		        && (access & Opcodes.ACC_ABSTRACT) == 0) {

			// MethodNode mn = new CFGMethodNode((MethodNode)mv);
			// System.out.println("Generating CFG for "+ className+"."+mn.name +
			// " ("+mn.desc +")");
			BytecodeAnalyzer bytecodeAnalyzer = new BytecodeAnalyzer(mutants);
			logger.info("Generating CFG for method " + methodName);

			try {
				bytecodeAnalyzer.analyze(className, methodName, mn);
				logger.trace("Method graph for " + className + "." + methodName
				        + " contains "
				        + bytecodeAnalyzer.retrieveCFGGenerator().getCompleteGraph().vertexSet().size()
				        + " nodes for " + bytecodeAnalyzer.getFrames().length
				        + " instructions");
			} catch (AnalyzerException e) {
				logger.error("Analyzer exception while analyzing " + className + "."
				        + methodName);
				e.printStackTrace();
			}

			// non-minimized cfg needed for defuse-coverage and control
			// dependence
			// calculation
			addCompleteCFG(className, methodName, bytecodeAnalyzer
					.retrieveCFGGenerator().getCompleteGraph());
			addMinimizedCFG(className, methodName, bytecodeAnalyzer
					.retrieveCFGGenerator().getMinimalGraph());
			logger.info("Created CFG for method " + methodName);

			Graph<BytecodeInstruction, DefaultEdge> completeGraph = bytecodeAnalyzer
					.retrieveCFGGenerator().getCompleteGraph();

			//add the actual instrumentation
			for (MethodInstrumentation instrumentation : instrumentations) {
				instrumentation.analyze(mn, completeGraph, className, methodName, access);
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

	/**
	 * See description of CFGMethodAdapter.EXCLUDE
	 * 
	 * @return
	 */
	private boolean isUsable() {
		return !((this.access & Opcodes.ACC_SYNTHETIC) > 0 || (this.access & Opcodes.ACC_BRIDGE) > 0)
		        && !methodName.contains("<clinit>")
		        && !(methodName.contains("<init>") && (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)
		        && (Properties.USE_DEPRECATED || (access & Opcodes.ACC_DEPRECATED) != Opcodes.ACC_DEPRECATED);
	}

	private static void addMinimizedCFG(String classname, String methodname,
	        DirectedMultigraph<BytecodeInstruction, DefaultEdge> graph) {
		if (!minimizedCFGs.containsKey(classname)) {
			minimizedCFGs.put(classname, new HashMap<String, ControlFlowGraph>());
			diameters.put(classname, new HashMap<String, Double>());
		}
		Map<String, ControlFlowGraph> methods = minimizedCFGs.get(classname);
		logger.debug("Added CFG for class " + classname + " and method " + methodname);
		methods.put(methodname, new ControlFlowGraph(graph, true));
		FloydWarshall<BytecodeInstruction, DefaultEdge> f = new FloydWarshall<BytecodeInstruction, DefaultEdge>(
		        graph);
		diameters.get(classname).put(methodname, f.getDiameter());
		logger.debug("Calculated diameter for " + classname + ": " + f.getDiameter());
	}

	private static void addCompleteCFG(String classname, String methodname,
	        DefaultDirectedGraph<BytecodeInstruction, DefaultEdge> graph) {
		if (!completeCFGs.containsKey(classname)) {
			completeCFGs.put(classname, new HashMap<String, ControlFlowGraph>());
		}
		Map<String, ControlFlowGraph> methods = completeCFGs.get(classname);
		logger.debug("Added complete CFG for class " + classname + " and method "
		        + methodname);
		methods.put(methodname, new ControlFlowGraph(graph, false));
		//ControlFlowGraph cfg = new ControlFlowGraph(graph, false);
		//cfg.toDot(classname + "_" + methodname + ".dot");
	}

	public static ControlFlowGraph getMinimizedCFG(String classname, String methodname) {
		logger.debug("Getting minimzed CFG for class " + classname + " and method "
		        + methodname);
		if (minimizedCFGs.get(classname).get(methodname) == null) {
			return null;
		}
		return minimizedCFGs.get(classname).get(methodname);
	}

	public static ControlFlowGraph getCompleteCFG(String classname, String methodname) {
		logger.debug("Getting complete CFG for class " + classname + " and method "
		        + methodname);
		if (completeCFGs.get(classname) == null)
			return null;
		return completeCFGs.get(classname).get(methodname);
	}
}
