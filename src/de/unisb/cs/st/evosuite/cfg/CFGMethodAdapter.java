/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Javalanche is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.cfg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.util.AbstractVisitor;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.AbstractMutationAdapter;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

class FrameVertexNameProvider implements VertexNameProvider<Integer> {
	
	private InsnList nodes;
	FrameVertexNameProvider(InsnList instructions) {
		this.nodes = instructions;;
	}
	
	public String getVertexName(Integer v) {
		AbstractInsnNode node = nodes.get(v);
		if(node instanceof LabelNode) {
			return "LABEL "+((LabelNode) node).getLabel().toString();
		} else if (node instanceof FieldInsnNode)
			return "Field";
		else if (node instanceof FrameNode)
			return "Frame";
		else if (node instanceof IincInsnNode)
			return "IINC "+((IincInsnNode)node).var;
		else if (node instanceof InsnNode)
			return "INSN "+ ((InsnNode)node).toString();
		else if (node instanceof IntInsnNode)
			return "INT "+((IntInsnNode)node).operand;
		else if (node instanceof JumpInsnNode)
			return "JUMP " + ((JumpInsnNode)node).label.getLabel();
		else if (node instanceof LdcInsnNode)
			return "LDC "+((LdcInsnNode)node).cst; // cst starts with mutationid if this is location of mutation
		else if (node instanceof LineNumberNode)
			return "LINE ";
		else if (node instanceof LookupSwitchInsnNode)
			return "LookupSwitchInsnNode";
		else if (node instanceof MethodInsnNode)
			return "METHOD "+ ((MethodInsnNode)node).name;
		else if (node instanceof MultiANewArrayInsnNode)
			return "MULTIANEWARRAY ";
		else if (node instanceof TableSwitchInsnNode)
			return "TableSwitchInsnNode";
		else if (node instanceof TypeInsnNode)
			return "TYPE ";
		else if (node instanceof VarInsnNode)
			return "VAR " + ((VarInsnNode)node).var;
		else return "Unknown node";
	}
};


class LineNumberProvider implements VertexNameProvider<Integer> {
	
	LineNumberProvider() {
	}
	
	public String getVertexName(Integer v) {
		return ""+v;
	}
};

class InstructionProvider implements VertexNameProvider<AbstractInsnNode> {
	
	public static String getName(AbstractInsnNode node) {
		
		String type = "";
		String opcode = "";
		
		if(node.getOpcode() >= 0 && node.getOpcode() < AbstractVisitor.OPCODES.length)
			opcode = AbstractVisitor.OPCODES[node.getOpcode()];
		if(node.getType() >= 0 && node.getType() < AbstractVisitor.TYPES.length)
			type = AbstractVisitor.TYPES[node.getType()];
		
		
		if(node instanceof LabelNode) {
			return "LABEL "+((LabelNode) node).getLabel().toString() + " Type="+type+", Opcode="+opcode;
		} else if (node instanceof FieldInsnNode)
			return "Field" + " "+node.getOpcode()  + " Type="+type+", Opcode="+opcode;
		else if (node instanceof FrameNode)
			return "Frame" + " "+node.getOpcode() + " Type="+type+", Opcode="+opcode;
		else if (node instanceof IincInsnNode)
			return "IINC "+((IincInsnNode)node).var  + " Type="+type+", Opcode="+opcode;
		else if (node instanceof InsnNode)
			return "INSN "+ ((InsnNode)node).toString() + " Type="+type+", Opcode="+opcode;
		else if (node instanceof IntInsnNode)
			return "INT "+((IntInsnNode)node).operand + " Type="+type+", Opcode="+opcode;
		else if (node instanceof JumpInsnNode)
			return "JUMP " + ((JumpInsnNode)node).label.getLabel()  + " Type="+type+", Opcode="+opcode;
		else if (node instanceof LdcInsnNode)
			return "LDC "+((LdcInsnNode)node).cst + " Type="+type+", Opcode="+opcode; // cst starts with mutationid if this is location of mutation
		else if (node instanceof LineNumberNode)
			return "LINE " + " "+node.getOpcode() + " Type="+type+", Opcode="+opcode;
		else if (node instanceof LookupSwitchInsnNode)
			return "LookupSwitchInsnNode" + " "+node.getOpcode()  + " Type="+type+", Opcode="+opcode;
		else if (node instanceof MethodInsnNode)
			return "METHOD "+ ((MethodInsnNode)node).name + " Type="+type+", Opcode="+opcode;
		else if (node instanceof MultiANewArrayInsnNode)
			return "MULTIANEWARRAY " + " "+node.getOpcode() + " Type="+type+", Opcode="+opcode;
		else if (node instanceof TableSwitchInsnNode)
			return "TableSwitchInsnNode" + " "+node.getOpcode() + " Type="+type+", Opcode="+opcode;
		else if (node instanceof TypeInsnNode)
			return "TYPE " + " "+node.getOpcode() + " Type="+type+", Opcode="+opcode;
		else if (node instanceof VarInsnNode)
			return "VAR " + ((VarInsnNode)node).var + " Type="+type+", Opcode="+opcode;
		else return "Unknown node" + " Type="+type+", Opcode="+opcode;
	}
	
	public String getVertexName(AbstractInsnNode vertex) {
		return getName(vertex);
//		return vertex.toString();
	}
};



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

	public CFGMethodAdapter(String className, int access, String name, String desc, String signature, String[] exceptions, MethodVisitor mv, List<Mutation> mutants) {
		super(new MethodNode(access, name, desc, signature, exceptions), className, name.replace('/', '.'), null, desc);
//		super(mv, className, name.replace('/', '.'), null);
//		super(mv);
		next = mv;
		this.className = className; //.replace('/', '.');
//		this.methodName = name;
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
//				List<Integer> branches = new ArrayList<Integer>();
//				branches.add(id);
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
			logger.info("Adding passedBranch val=?, opcode="+opcode+", branch="+branch_counter+", bytecode_id="+id);

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
	
	
	@SuppressWarnings("unchecked")
	public void visitEnd() {

		//super.visitEnd();
		
		// TODO: Finalize CFG here?
		
		//logger.trace("Reached end of method "+methodName);
		// Generate CFG of method
		MethodNode mn = (MethodNode) mv;
		
		if(EXCLUDE.contains(methodName)) {
			mn.accept(next);
			return;
		}
		
		//MethodNode mn = new CFGMethodNode((MethodNode)mv);
		//System.out.println("Generating CFG for "+ className+"."+mn.name + " ("+mn.desc +")");
		CFGGenerator g = new CFGGenerator(mutants);
		
		try {
			//logger.debug("Generating CFG for "+className+"."+methodName);
			g.getCFG(className, methodName, mn);
			logger.trace("Method graph for "+className+"."+methodName+" contains "+g.getGraph().vertexSet().size()+" nodes for "+g.getFrames().length+" instructions");
			//cfg.print();
			//DirectedMultigraph<CFGVertex, DefaultEdge> graph = g.getMinimalGraph();
			//if(has_mutation) {
			//	ExecutionTracer.getExecutionTracer().addCFG(className, methodName, graph);
			//}
		} catch (AnalyzerException e) {
			// TODO Auto-generated catch block
			logger.warn("Analyzer exception while analyzing "+className+"."+methodName);
			e.printStackTrace();
			
		}


		ExecutionTracer.getExecutionTracer().addCFG(className, methodName, g.getMinimalGraph());

		Graph<CFGVertex, DefaultEdge> graph = g.getGraph();
		Iterator<AbstractInsnNode> j = mn.instructions.iterator(); 
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			for(CFGVertex v : graph.vertexSet()) {
				// If this is in the CFG and it's a branch...
				if(in.equals(v.node) && v.isBranch()) {
					//v.id = branch_counter++;
					/*
					java.util.ListIterator it = mn.instructions.iterator();
					while(it.hasNext()) {
						AbstractInsnNode node = (AbstractInsnNode) it.next();
						logger.info("-> "+InstructionProvider.getName(node));
					}
					*/
					mn.instructions.insert(v.node.getPrevious(), getInstrumentation(v.node.getOpcode(), v.id));
					if(!branch_map.containsKey(className))
						branch_map.put(className, new HashMap<String, Map<Integer,Integer>>());
					if(!branch_map.get(className).containsKey(methodName))
						branch_map.get(className).put(methodName, new HashMap<Integer,Integer>());
					branch_map.get(className).get(methodName).put(v.id, branch_counter);
					
					logger.debug("Branch "+branch_counter+" at line "+v.id+" - "+current_line);
					/*
					it = mn.instructions.iterator();
					while(it.hasNext()) {
						AbstractInsnNode node = (AbstractInsnNode) it.next();
						logger.info("-> "+InstructionProvider.getName(node));
					}
					logger.info(mn.instructions.toString());
					*/
					// TODO: Associate branch_counter with v.id?
					branch_counter++;
					//logger.info("Adding InsnList");
				}
			}
		}


		/*
		logger.info("Dumping file");
		
		try {
			
			FileWriter fstream = new FileWriter(className.replace('/', '.')+"."+plain_name+".dot");
	        BufferedWriter out = new BufferedWriter(fstream);
	    	if(!g.getGraph().vertexSet().isEmpty()) {
	    		FrameVertexNameProvider nameprovider = new FrameVertexNameProvider(mn.instructions);
	    		//	DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>();
	    		//DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>(new IntegerNameProvider(), nameprovider, new IntegerEdgeNameProvider());
	    		//			DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>(new LineNumberProvider(), new LineNumberProvider(), new IntegerEdgeNameProvider());
	    		DOTExporter<CFGVertex, DefaultEdge> exporter = new DOTExporter<CFGVertex, DefaultEdge>(new IntegerNameProvider(), new StringNameProvider(), new IntegerEdgeNameProvider());
	    		exporter.export(out, g.getMinimalGraph());
	    		//exporter.export(out, g.getGraph());
	    	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		String id = className+"."+methodName;
		if(!branch_count.containsKey(id)) {
			if(isUsable()) {
//			if( !((this.access & Opcodes.ACC_SYNTHETIC) > 0 || (this.access & Opcodes.ACC_BRIDGE) > 0)) {
				logger.debug("Method has no branches: "+id);
				branchless_methods.add(id);
			}
		}
		
//		if( !((this.access & Opcodes.ACC_SYNTHETIC) > 0 || (this.access & Opcodes.ACC_BRIDGE) > 0 || (this.access & Opcodes.ACC_ABSTRACT) > 0) 
//				&& !methodName.contains("<clinit>")
//				&& !(methodName.contains("<init>") && (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)) {
		if(isUsable()) {
			methods.add(id);
			logger.debug("Counting: "+id);
		}		
		mn.accept(next);
	}
	
	private boolean isUsable() {
		return !((this.access & Opcodes.ACC_SYNTHETIC) > 0 || (this.access & Opcodes.ACC_BRIDGE) > 0 || (this.access & Opcodes.ACC_ABSTRACT) > 0) 
				&& !methodName.contains("<clinit>")
				&& !(methodName.contains("<init>") && (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE);
		
	}
}
