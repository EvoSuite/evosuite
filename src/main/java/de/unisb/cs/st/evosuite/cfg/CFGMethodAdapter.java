/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;


import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.AbstractMutationAdapter;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * At the end of each method, create a minimized control flow graph for the method and store it.
 * In addition, this adapter also adds instrumentation for branch distance measurement
 * 
 * @author Gordon Fraser
 *
 */
public class CFGMethodAdapter extends AbstractMutationAdapter {

	MethodVisitor next;
	String plain_name;
	Label last_label = null;
	static int current_line = 0;
	List<Mutation> mutants;
	int access = 0;
	
	public static final List<String> EXCLUDE = Arrays.asList("<clinit>", "__STATIC_RESET()V", "__STATIC_RESET");
	
	public static Map<String, Integer> branch_count = new HashMap<String, Integer>();

	public static Map<String, Map<String, Map<Integer,Integer>>> branch_map = new HashMap<String, Map<String, Map<Integer,Integer>>>();

	public static Set<String> branchless_methods = new HashSet<String>();

	public static Set<String> methods = new HashSet<String>();

	public static int branch_counter = 0;
	
	// maps: classname -> methodName  -> DUVarName -> branchID -> List of Defs as CFGVertex in that branch 
	public static Map<String, Map<String, Map<String, Map<Integer,List<CFGVertex>>>>> def_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<CFGVertex>>>>>();

	// maps: classname -> methodName  -> DUVarName -> branchID -> List of Defs as CFGVertex in that branch
	public static Map<String, Map<String, Map<String, Map<Integer,List<CFGVertex>>>>> use_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<CFGVertex>>>>>();	
	
	// maps the branch_counter field of this class to its bytecodeID in the CFG
	public static Map<Integer, Integer> branchCounterToBytecodeID = new HashMap<Integer, Integer>();
	
	public static int def_counter = 0;
	public static int use_counter = 0;
	
	private static int currentLineNumber = -1; // TODO should be merged with current_line? (which doesnt work i guess)

	private static Map<String, Map <String, ControlFlowGraph > > completeGraphs = new HashMap<String, Map <String, ControlFlowGraph > >();
	private static Map<String, Map <String, ControlFlowGraph > > graphs = new HashMap<String, Map <String, ControlFlowGraph > >();
	private static Map<String, Map <String, Double > > diameters = new HashMap<String, Map <String, Double> > ();	
	
	public CFGMethodAdapter(String className, int access, String name, String desc, String signature, String[] exceptions, MethodVisitor mv, List<Mutation> mutants) {
		super(new MethodNode(access, name, desc, signature, exceptions), className, name.replace('/', '.'), null, desc);
		next = mv;
		this.className = className; //.replace('/', '.');
		this.access = access;
		this.methodName = name+desc;
		this.plain_name = name;
		this.mutants = mutants;
	}

	private static Logger logger = Logger.getLogger(CFGMethodAdapter.class);

	private String methodName, className;

	private void countBranch() {
			String id = className+"."+methodName;
			if(!branch_count.containsKey(id)) {
				branch_count.put(id, 1);
			}
			else
				branch_count.put(id, branch_count.get(id) + 1);
	}
	
	private InsnList getInstrumentation(int opcode, int id) {
		InsnList instrumentation = new InsnList();
		

		switch(opcode) {
		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFLE:
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(opcode));
//			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(branch_counter));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(IIII)V"));
			countBranch();
			logger.debug("Adding passedBranch val=?, opcode="+opcode+", branch="+branch_counter+", bytecode_id="+id);

			break;
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
			instrumentation.add(new InsnNode(Opcodes.DUP2));
			instrumentation.add(new LdcInsnNode(opcode));
//			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(branch_counter));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(IIIII)V"));
			countBranch();


			break;
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
			instrumentation.add(new InsnNode(Opcodes.DUP2));
			instrumentation.add(new LdcInsnNode(opcode));
			//instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(branch_counter));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(Ljava/lang/Object;Ljava/lang/Object;III)V"));
			countBranch();
			break;
		case Opcodes.IFNULL:
		case Opcodes.IFNONNULL:
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(opcode));
//			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(branch_counter));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(Ljava/lang/Object;III)V"));
			countBranch();
			break;
		case Opcodes.GOTO:
			break;
		case Opcodes.TABLESWITCH:
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(opcode));
//			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(branch_counter));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(IIII)V"));
			countBranch();
			break;
		case Opcodes.LOOKUPSWITCH:
			instrumentation.add(new InsnNode(Opcodes.DUP));
			instrumentation.add(new LdcInsnNode(opcode));
//			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new LdcInsnNode(branch_counter));
			instrumentation.add(new LdcInsnNode(id));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedBranch", "(IIII)V"));
			countBranch();
			break;
		}	
		return instrumentation;
	}
	
	/**
	 * Creates the instrumentation needed to track defs and uses
	 * 
	 */
	private InsnList getInstrumentation(CFGVertex v, int currentBranch) {
		InsnList instrumentation = new InsnList();


		switch(v.node.getOpcode()) {
		case Opcodes.PUTFIELD:
		case Opcodes.PUTSTATIC:
			instrumentation.add(new LdcInsnNode(className));
			instrumentation.add(new LdcInsnNode(v.getDUVariableName()));
			instrumentation.add(new LdcInsnNode(methodName));
			instrumentation.add(new LdcInsnNode(currentBranch));
			instrumentation.add(new LdcInsnNode(def_counter));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedFieldDefinition", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)V"));
			break;
		case Opcodes.GETFIELD:
		case Opcodes.GETSTATIC:
			instrumentation.add(new LdcInsnNode(className));
			instrumentation.add(new LdcInsnNode(v.getDUVariableName()));
			instrumentation.add(new LdcInsnNode(methodName));
			instrumentation.add(new LdcInsnNode(currentBranch));
			instrumentation.add(new LdcInsnNode(use_counter));
			instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
					"passedFieldUse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)V"));
			break;			
		}
		
		return instrumentation;
	}
	
	@SuppressWarnings("unchecked")
	public void visitEnd() {

		//super.visitEnd();

		// Generate CFG of method
		MethodNode mn = (MethodNode) mv;
		
		if(plain_name.equals("<clinit>")) {
			mn.accept(next);
			return;
		}

		if(EXCLUDE.contains(methodName)) {
			mn.accept(next);
			return;
		}
		
		//MethodNode mn = new CFGMethodNode((MethodNode)mv);
		//System.out.println("Generating CFG for "+ className+"."+mn.name + " ("+mn.desc +")");
		CFGGenerator g = new CFGGenerator(mutants);
		
		try {
			g.getCFG(className, methodName, mn);
			logger.trace("Method graph for "+className+"."+methodName+" contains "+g.getGraph().vertexSet().size()+" nodes for "+g.getFrames().length+" instructions");
		} catch (AnalyzerException e) {
			logger.warn("Analyzer exception while analyzing "+className+"."+methodName);
			e.printStackTrace();			
		}

		// non-minimized cfg needed for defuse-coverage
		ControlFlowGraph completeCFG = null;
		if(Properties.CRITERION.equals("defuse")) {
			addCompleteCFG(className,methodName,g.graph);
			completeCFG = getCompleteCFG(className, methodName);
		}
		

		addCFG(className, methodName, g.getMinimalGraph());

		//if(!Properties.MUTATION) {
		Graph<CFGVertex, DefaultEdge> graph = g.getGraph();
		Iterator<AbstractInsnNode> j = mn.instructions.iterator(); 
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for(CFGVertex v : graph.vertexSet()) {
				
				// If this is in the CFG and it's a branch...
				if(in.equals(v.node) && v.isBranch() && !v.isMutation() && !v.isMutationBranch()) {
					mn.instructions.insert(v.node.getPrevious(), getInstrumentation(v.node.getOpcode(), v.id));
					//if(!v.isMutatedBranch()) {
						if(!branch_map.containsKey(className))
							branch_map.put(className, new HashMap<String, Map<Integer,Integer>>());
						if(!branch_map.get(className).containsKey(methodName))
							branch_map.get(className).put(methodName, new HashMap<Integer,Integer>());
						branch_map.get(className).get(methodName).put(v.id, branch_counter);

						if(Properties.CRITERION.equals("defuse")) {
							CFGVertex branchVertex = completeCFG.getVertex(v.id);
							branchVertex.branchID = branch_counter;
							completeCFG.markBranchIDs(branchVertex);
							branchCounterToBytecodeID.put(branch_counter, v.id);
						}
						
						logger.debug("Branch "+branch_counter+" at line "+v.id+" - "+current_line);
						// TODO: Associate branch_counter with v.id?
						branch_counter++;
					//}
				}
			}
		}
		
		j = mn.instructions.iterator(); 
		while (j.hasNext()) { // TODO merge with previous while
			
			AbstractInsnNode in = j.next();
			for(CFGVertex v : graph.vertexSet()) {
				

				if(in.equals(v.node)) { //&& Properties.CRITERION.equals("defuse")) {
					if (v.isLineNumber()) {
						currentLineNumber = v.getLineNumber();
					}
					
					v.className = className;
					v.methodName = methodName;
					v.line_no = currentLineNumber;
					
				}	
				if(Properties.CRITERION.equals("defuse") && in.equals(v.node) && (v.isDU())) {

					v.branchID = completeCFG.getVertex(v.id).branchID;
					
					// adding instrumentation for defuse-coverage
					mn.instructions.insert(v.node.getPrevious(), getInstrumentation(v, v.branchID));

					// keeping track of all defs and uses
					if(v.isDefinition()) {
						
						logger.info("Found Def "+def_counter+" in "+methodName+":"+v.branchID+(v.branchExpressionValue?"t":"f")+"("+currentLineNumber+")"+" for var "+v.getDUVariableName());
						
						List<CFGVertex> defs = initDefUseMap(def_map, className, methodName, v.getDUVariableName(), v.branchID);	
						defs.add(v);
						v.duID = def_counter;
						def_counter++;
					}
					if(v.isUse()) {
						
						if(v.isLocalVarUse() && !hasEntryForVariable(def_map, className, methodName, v.getDUVariableName()))
							continue; // Not a real local variable

						logger.info("Found Use "+use_counter+" in "+methodName+":"+v.branchID+(v.branchExpressionValue?"t":"f")+"("+currentLineNumber+")"+" for var "+v.getDUVariableName());
					
						List<CFGVertex> uses = initDefUseMap(use_map, className, methodName, v.getDUVariableName(), v.branchID);
						uses.add(v);						
						v.duID = use_counter;
						use_counter++;
					}
				}
		
			}
		}

		String id = className+"."+methodName;
		if(!branch_count.containsKey(id)) {
			if(isUsable()) {
				logger.debug("Method has no branches: "+id);
				branchless_methods.add(id);
			}
		}
		
		if(isUsable()) {
			methods.add(id);
			logger.debug("Counting: "+id);
		}		
		mn.accept(next);
	}
	

	private boolean isUsable() {
		return !((this.access & Opcodes.ACC_SYNTHETIC) > 0 || (this.access & Opcodes.ACC_BRIDGE) > 0 ) 
				&& !methodName.contains("<clinit>")
				&& !(methodName.contains("<init>") && (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE);
	}
	
	private List<CFGVertex> initDefUseMap(
			Map<String, Map<String, Map<String, Map<Integer, List<CFGVertex>>>>> map,
			String className, String methodName, String varName, Integer branchID) {

		if(!map.containsKey(className))
			map.put(className, new HashMap<String, Map<String, Map<Integer,List<CFGVertex>>>>());
		if(!map.get(className).containsKey(methodName)) 
			map.get(className).put(methodName, new HashMap<String, Map<Integer,List<CFGVertex>>>());

		
		if(!map.get(className).get(methodName).containsKey(varName))
			map.get(className).get(methodName).put(varName, new HashMap<Integer,List<CFGVertex>>());
		if(!map.get(className).get(methodName).get(varName).containsKey(branchID))
			map.get(className).get(methodName).get(varName).put(branchID, new ArrayList<CFGVertex>());
		
		return map.get(className).get(methodName).get(varName).get(branchID);
	}

	private boolean hasEntryForVariable(
			Map<String, Map<String, Map<String, Map<Integer, List<CFGVertex>>>>> map,
			String className, String methodName, String varName) {
		
		if(map.get(className) == null)
			return false;
		if(map.get(className).get(methodName) == null)
			return false;
		if(map.get(className).get(methodName).get(varName) == null)
			return false;
		if(map.get(className).get(methodName).get(varName).size() > 0)
			return true;
	
		return false;
	}
	
	public static void addCFG(String classname, String methodname, DirectedMultigraph<CFGVertex, DefaultEdge> graph) {
		if(!graphs.containsKey(classname)) {
			graphs.put(classname, new HashMap<String, ControlFlowGraph >());
			diameters.put(classname, new HashMap<String, Double>());
		}
		Map<String, ControlFlowGraph > methods = graphs.get(classname);
        logger.debug("Added CFG for class "+classname+" and method "+methodname);
		methods.put(methodname, new ControlFlowGraph(graph));
		FloydWarshall<CFGVertex,DefaultEdge> f = new FloydWarshall<CFGVertex,DefaultEdge>(graph);
		diameters.get(classname).put(methodname, f.getDiameter());
        logger.debug("Calculated diameter for "+classname+": "+f.getDiameter());
	}
	
	public static void addCompleteCFG(String classname, String methodname, DefaultDirectedGraph<CFGVertex, DefaultEdge> graph) {
		if(!completeGraphs.containsKey(classname)) {
			completeGraphs.put(classname, new HashMap<String, ControlFlowGraph >());
		}
		Map<String, ControlFlowGraph > methods = completeGraphs.get(classname);
        logger.debug("Added complete CFG for class "+classname+" and method "+methodname);
		methods.put(methodname, new ControlFlowGraph(graph));
	}	
	
	public static ControlFlowGraph getCFG(String classname, String methodname) {
		return graphs.get(classname).get(methodname);
	}
	
	public static ControlFlowGraph getCompleteCFG(String classname, String methodname) {
		return completeGraphs.get(classname).get(methodname);
	}	
}
