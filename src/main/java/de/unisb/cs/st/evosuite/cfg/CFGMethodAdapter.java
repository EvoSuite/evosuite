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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUsePool;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJ;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJPool;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.AbstractMutationAdapter;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.testability.TransformationHelper;

/**
 * At the end of each method, create a minimized control flow graph for the
 * method and store it. In addition, this adapter also adds instrumentation for
 * branch distance measurement
 * 
 * @author Gordon Fraser
 * 
 */
public class CFGMethodAdapter extends AbstractMutationAdapter {

	MethodVisitor next;
	String plain_name;
	Label last_label = null;
	List<Mutation> mutants;
	int access = 0;

	public static final List<String> EXCLUDE = Arrays.asList("<clinit>",
	                                                         "__STATIC_RESET()V",
	                                                         "__STATIC_RESET");

	public static Set<String> methods = new HashSet<String>();

	private static int currentLineNumber = -1;

	private static Map<String, Map<String, ControlFlowGraph>> completeGraphs = new HashMap<String, Map<String, ControlFlowGraph>>();
	private static Map<String, Map<String, ControlFlowGraph>> graphs = new HashMap<String, Map<String, ControlFlowGraph>>();
	private static Map<String, Map<String, Double>> diameters = new HashMap<String, Map<String, Double>>();

	public CFGMethodAdapter(String className, int access, String name, String desc,
	        String signature, String[] exceptions, MethodVisitor mv,
	        List<Mutation> mutants) {
		super(new MethodNode(access, name, desc, signature, exceptions), className,
		        name.replace('/', '.'), null, desc);
		next = mv;
		this.className = className; // .replace('/', '.');
		this.access = access;
		this.methodName = name + desc;
		this.plain_name = name;
		this.mutants = mutants;
	}

	private static Logger logger = Logger.getLogger(CFGMethodAdapter.class);

	private final String methodName, className;

	private InsnList getInstrumentation(int opcode, int id) {
		InsnList instrumentation = new InsnList();

		String methodID = className + "." + methodName;

		switch (opcode) {
		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFLE:
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(IIII)V"));
			BranchPool.countBranch(methodID);
			logger.debug("Adding passedBranch val=?, opcode=" + opcode + ", branch="
			        + BranchPool.getBranchCounter() + ", bytecode_id=" + id);

			break;
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
			instrumentation.add(new InsnNode(Opcodes.DUP2));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(IIIII)V"));
			BranchPool.countBranch(methodID);

			break;
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
			instrumentation.add(new InsnNode(Opcodes.DUP2));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(Ljava/lang/Object;Ljava/lang/Object;III)V"));
			BranchPool.countBranch(methodID);
			break;
		case Opcodes.IFNULL:
		case Opcodes.IFNONNULL:
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(opcode));
			// instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
			        "(Ljava/lang/Object;III)V"));
			BranchPool.countBranch(methodID);
			break;
		case Opcodes.GOTO:
			break;
		/*
		case Opcodes.TABLESWITCH:
		instrumentation.add(new InsnNode(Opcodes.DUP));
		instrumentation.add(new LdcInsnNode(opcode));
		// instrumentation.add(new LdcInsnNode(id));
		instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
		instrumentation.add(new LdcInsnNode(id));
		instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
		        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
		        "(IIII)V"));
		BranchPool.countBranch(methodID);
		break;
		case Opcodes.LOOKUPSWITCH:
		instrumentation.add(new InsnNode(Opcodes.DUP));
		instrumentation.add(new LdcInsnNode(opcode));
		// instrumentation.add(new LdcInsnNode(id));
		instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
		instrumentation.add(new LdcInsnNode(id));
		instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
		        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer", "passedBranch",
		        "(IIII)V"));
		BranchPool.countBranch(methodID);
		break;
		*/
		}
		return instrumentation;
	}

	/**
	 * Creates the instrumentation needed to track defs and uses
	 * 
	 */
    private InsnList getInstrumentation(CFGVertex v, int currentBranch, boolean staticContext) {
		InsnList instrumentation = new InsnList();

		if(v.isUse()) {
			instrumentation.add(new LdcInsnNode(className));
			instrumentation.add(new LdcInsnNode(v.getDUVariableName()));
			instrumentation.add(new LdcInsnNode(methodName));
			if(staticContext) {
				instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
			} else {
				instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0));
			}
			instrumentation.add(new LdcInsnNode(currentBranch));
			instrumentation.add(new LdcInsnNode(DefUsePool.getUseCounter()));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedUse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;II)V"));
		}

		if(v.isDefinition()) {
			instrumentation.add(new LdcInsnNode(className));
			instrumentation.add(new LdcInsnNode(v.getDUVariableName()));
			instrumentation.add(new LdcInsnNode(methodName));
			if(staticContext) {
				instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
			} else {
				instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0));
			}
			instrumentation.add(new LdcInsnNode(currentBranch));
			instrumentation.add(new LdcInsnNode(DefUsePool.getDefCounter()));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedDefinition", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;II)V"));
		}
		
		return instrumentation;
	}

	private InsnList getInstrumentation(CFGVertex v, MethodNode mn) {
		InsnList instrumentation = new InsnList();

		String methodID = className + "." + methodName;
		switch (v.node.getOpcode()) {
		case Opcodes.TABLESWITCH:
			TableSwitchInsnNode tsin = (TableSwitchInsnNode) v.node;
			int num = 0;
			for (int i = tsin.min; i <= tsin.max; i++) {
				instrumentation.add(new InsnNode(Opcodes.DUP));
				instrumentation.add(new LdcInsnNode(i));
				instrumentation.add(new LdcInsnNode(Opcodes.IF_ICMPEQ));
				instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
				instrumentation.add(new LdcInsnNode(v.id));
				//instrumentation.add(new LdcInsnNode(
				//        mn.instructions.indexOf((LabelNode) tsin.labels.get(num))));

				instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				        "passedBranch", "(IIIII)V"));
				BranchPool.countBranch(methodID);
				BranchPool.addBranch(v);
				num++;
			}
			// Default branch is covered if the last case is false
			break;
		case Opcodes.LOOKUPSWITCH:
			LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) v.node;
			logger.info("Found lookupswitch with " + lsin.keys.size() + " keys");
			for (int i = 0; i < lsin.keys.size(); i++) {
				instrumentation.add(new InsnNode(Opcodes.DUP));
				instrumentation.add(new LdcInsnNode(
				        ((Integer) lsin.keys.get(i)).intValue()));
				instrumentation.add(new LdcInsnNode(Opcodes.IF_ICMPEQ));
				instrumentation.add(new LdcInsnNode(BranchPool.getBranchCounter()));
				instrumentation.add(new LdcInsnNode(v.id));
				//				instrumentation.add(new LdcInsnNode(
				//				        mn.instructions.indexOf((LabelNode) lsin.labels.get(i))));
				instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				        "passedBranch", "(IIIII)V"));
				BranchPool.countBranch(methodID);
				BranchPool.addBranch(v);
			}
			// Default branch is covered if the last case is false
			break;
		}

		return instrumentation;
	}

	@Override
	public void visitEnd() {

		// super.visitEnd();
		// Generate CFG of method
		MethodNode mn = (MethodNode) mv;

		if (plain_name.equals("main") && Modifier.isStatic(access)) {
			mn.accept(next);
			return;
		}

		if (plain_name.equals("<clinit>") && !Properties.CRITERION.equals("defuse")) {
			mn.accept(next);
			return;
		}

		if (EXCLUDE.contains(methodName) && !Properties.CRITERION.equals("defuse")) {
			mn.accept(next);
			return;
		}

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
		analyzeBranchVertices(mn, graph);
		if (Properties.CRITERION.equalsIgnoreCase("defuse"))
			analyzeDefUseVertices(mn, graph);
		if (Properties.CRITERION.equalsIgnoreCase("lcsaj"))
			analyzeLCSAJs(mn, graph);
		handleBranchlessMethods();
		logger.info("Analyzing for method " + methodName);

		String id = className + "." + methodName;
		if (isUsable()) {
			methods.add(id);
			logger.debug("Counting: " + id);
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

	@SuppressWarnings("unchecked")
	private void analyzeBranchVertices(MethodNode mn, Graph<CFGVertex, DefaultEdge> graph) {

		Iterator<AbstractInsnNode> j = mn.instructions.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for (CFGVertex v : graph.vertexSet()) {
				// updating some information in the CFGVertex
				if (in.equals(v.node)) {
					if (v.isLineNumber()) {
						currentLineNumber = v.getLineNumber();
					}
					v.className = className;
					v.methodName = methodName;
					v.line_no = currentLineNumber;
				}
				// If this is in the CFG and it's a branch...
				if (in.equals(v.node) && v.isBranch() && !v.isMutation()
				        && !v.isMutationBranch()) {
					mn.instructions.insert(v.node.getPrevious(),
					                       getInstrumentation(v.node.getOpcode(), v.id));

					BranchPool.addBranch(v);
				} else if (in.equals(v.node) && v.isTableSwitch()) {
					TableSwitchInsnNode n = (TableSwitchInsnNode) in;
					mn.instructions.insertBefore(v.node, getInstrumentation(v, mn));
				} else if (in.equals(v.node) && v.isLookupSwitch()) {
					LookupSwitchInsnNode n = (LookupSwitchInsnNode) in;
					mn.instructions.insertBefore(v.node, getInstrumentation(v, mn));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void analyzeDefUseVertices(MethodNode mn, Graph<CFGVertex, DefaultEdge> graph) {

		ControlFlowGraph completeCFG = getCompleteCFG(className, methodName);
		Iterator<AbstractInsnNode> j = mn.instructions.iterator();
		while (j.hasNext()) {

			AbstractInsnNode in = j.next();
			for (CFGVertex v : graph.vertexSet()) {

				if (in.equals(v.node))
					v.branchID = completeCFG.getVertex(v.id).branchID;

				if (Properties.CRITERION.equals("defuse") && in.equals(v.node)
				        && (v.isDU())) {

					// keeping track of uses
					boolean isValidDU = false;
					if (v.isUse()) 
						isValidDU = DefUsePool.addUse(v);
					// keeping track of definitions
					if (v.isDefinition())
						isValidDU = DefUsePool.addDefinition(v) || isValidDU;

					if (isValidDU) {
						boolean staticContext = v.isStaticDU() || ((access & Opcodes.ACC_STATIC) > 0);
						// adding instrumentation for defuse-coverage
						mn.instructions.insert(v.node.getPrevious(),
						                       getInstrumentation(v, v.branchID, staticContext));						
					}
				}
			}
		}
	}

	private void analyzeLCSAJs(MethodNode mn, Graph<CFGVertex, DefaultEdge> graph) {

		Queue<LCSAJ> lcsaj_queue = new LinkedList<LCSAJ>();

		LCSAJ a = new LCSAJ(className, methodName);
		a.addInstruction(0, mn.instructions.getFirst(), true);
		lcsaj_queue.add(a);

		while (!lcsaj_queue.isEmpty()) {
			LCSAJ current_lcsaj = lcsaj_queue.poll();

			int position = mn.instructions.indexOf(current_lcsaj.getLastNodeAccessed());
			if (position + 1 >= mn.instructions.size()) {
				// New LCSAJ for current + return
				LCSAJPool.add_lcsaj(className, methodName, current_lcsaj);
				continue;
			}

			AbstractInsnNode next = mn.instructions.get(position + 1);
			current_lcsaj.addInstruction(position + 1, next, false);
			if (next instanceof JumpInsnNode) {
				JumpInsnNode jump = (JumpInsnNode) next;
				// New LCSAJ for current + jump to target
				LCSAJPool.add_lcsaj(className, methodName, current_lcsaj);

				LCSAJ b = new LCSAJ(className, methodName, current_lcsaj);
				b.addInstruction(position + 1, jump, false);
				lcsaj_queue.add(b);

				if (jump.getOpcode() != Opcodes.GOTO) {
					LabelNode target = jump.label;
					LCSAJ c = new LCSAJ(className, methodName);
					c.addInstruction(mn.instructions.indexOf(target), target, true);
					lcsaj_queue.add(c);
				}

			} else if (next instanceof TableSwitchInsnNode) {
				TableSwitchInsnNode tswitch = (TableSwitchInsnNode) next;
				List<LabelNode> allTargets = tswitch.labels;
				for (LabelNode target : allTargets) {
					LCSAJ b = new LCSAJ(className, methodName);
					b.addInstruction(mn.instructions.indexOf(target), target, true);
					lcsaj_queue.add(b);
				}

			} else if (next instanceof InsnNode) {
				InsnNode insn = (InsnNode) next;
				switch (insn.getOpcode()) {
				case Opcodes.ATHROW:
				case Opcodes.RETURN:
				case Opcodes.ARETURN:
				case Opcodes.IRETURN:
				case Opcodes.DRETURN:
				case Opcodes.LRETURN:
				case Opcodes.FRETURN:
					// New LCSAJ for current + throw
					LCSAJPool.add_lcsaj(className, methodName, current_lcsaj);
					break;
				default:
					lcsaj_queue.add(current_lcsaj);

				}
			} else {
				lcsaj_queue.add(current_lcsaj);
			}
		}
		logger.info("Found " + LCSAJPool.getSize() + " LCSAJs");
	}

	private boolean isUsable() {
		return !((this.access & Opcodes.ACC_SYNTHETIC) > 0 || (this.access & Opcodes.ACC_BRIDGE) > 0)
		        && !methodName.contains("<clinit>")
		        && !(methodName.contains("<init>") && (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE);
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
	}

	public static ControlFlowGraph getCFG(String classname, String methodname) {
		logger.debug("Getting CFG for class " + classname + " and method " + methodname);
		return graphs.get(classname).get(methodname);
	}

	public static ControlFlowGraph getCompleteCFG(String classname, String methodname) {
		return completeGraphs.get(classname).get(methodname);
	}
}
