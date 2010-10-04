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


package de.unisb.cs.st.evosuite.testcase;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
//import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.AbstractVisitor;

import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.FloydWarshall;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * This class collects information about chosen branches/paths at runtime
 * @author Gordon Fraser
 *
 */
public class ExecutionTracer {
	
	private static Logger logger = Logger.getLogger(ExecutionTracer.class);
	
	private final int CLASS_DISTANCE = 1000;
	private final int METHOD_DISTANCE = 100;
	
	private static ExecutionTracer instance = null;
	
	private Map<String, Map <String, ControlFlowGraph > > graphs;
	private Map<String, Map <String, Double > > diameters;
		
	private boolean disabled = false;
	
	private int num_statements = 0;
	
	public static void disable() {
		ExecutionTracer tracer = ExecutionTracer.getExecutionTracer();
		tracer.disabled = true;
		//logger.info("** DISABLE ***");
	}
	public static void enable() {
		ExecutionTracer tracer = ExecutionTracer.getExecutionTracer();
		tracer.disabled = false;
		//logger.info("** ENABLE ***");
	}
	public static boolean isEnabled() {
		ExecutionTracer tracer = ExecutionTracer.getExecutionTracer();
		return !tracer.disabled;
	}
	
	//private List<TraceEntry> trace;
	private ExecutionTrace trace;

	//private List<String> class_access;
	//private List<String> method_calls;
//	private List< List<String> > trace;
	//private List< List<Integer> > trace;
	
	public static ExecutionTracer getExecutionTracer() {
		if(instance == null) {
			instance = new ExecutionTracer();
		}
		return instance;
	}

	/**
	 * Reset for new execution
	 */
	public void clear() {
		trace = new ExecutionTrace();
		num_statements = 0;
	}
	
	/**
	 * Return trace of current execution
	 * @return
	 */
	public ExecutionTrace getTrace() {
		trace.finishCalls();
		ExecutionTrace copy = trace.clone();
		//copy.finishCalls();
		return copy;
	}
	
	public void addCFG(String classname, String methodname, DirectedMultigraph<CFGVertex, DefaultEdge> graph) {
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
	
	public ControlFlowGraph getCFG(String classname, String methodname) {
		return graphs.get(classname).get(methodname);
	}
	
	/**
	 * Called by instrumented code whenever a new method is called
	 * 
	 * @param classname
	 * @param methodname
	 */
	public static void enteredMethod(String classname, String methodname) {
		ExecutionTracer tracer = getExecutionTracer();
		logger.trace("Entering method "+classname+"."+methodname);
		tracer.trace.enteredMethod(classname, methodname);
	}
	
	/**
	 * Called by instrumented code whenever a return values is produced
	 * 
	 * @param classname
	 * @param methodname
	 * @param value
	 */
	public static void returnValue(int value, String className, String methodName) {
		ExecutionTracer tracer = getExecutionTracer();
		tracer.trace.returnValue(className, methodName, value);
	}

	/**
	 * Called by instrumented code whenever a return values is produced
	 * 
	 * @param classname
	 * @param methodname
	 * @param value
	 */
	public static void returnValue(Object value, String className, String methodName) {
		if (value == null) {
			returnValue(0, className, methodName);
			return;
		}
		StringBuilder tmp = null;
		try {
			// setLineCoverageDeactivated(true);
			//logger.warn("Disabling tracer: returnValue");
			ExecutionTracer.disable();
			tmp = new StringBuilder(value.toString());
		} catch (Throwable t) {
			// TODO: Do we need this?
			/*
			InstrumentExclude.addExcludeReturn(className, methodName);
			logger.warn(
					"To string for return object throws an exception. Class: "
							+ className + " MethodName: " + methodName, t);
			InstrumentExclude.save();
			*/
			return;
		} finally {
			ExecutionTracer.enable();
		}
		int index = 0;
		int position = 0;
		boolean found = false;
		boolean deleteAddresses = true;
		char c = ' ';
		// quite fast method to detect memory addresses in Strings.
		while ((position = tmp.indexOf("@", index)) > 0) {
			for (index = position + 1; index < position + 17
					&& index < tmp.length(); index++) {
				c = tmp.charAt(index);
				if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
						|| (c >= 'A' && c <= 'F')) {
					found = true;
				} else {
					break;
				}
			}
			if (deleteAddresses && found) {
				tmp.delete(position + 1, index);
			}
		}

		returnValue(tmp.toString().hashCode(), className, methodName);
	}

	/**
	 * Called by instrumented code whenever a method is left
	 * 
	 * @param classname
	 * @param methodname
	 */
	public static void leftMethod(String classname, String methodname) {
		ExecutionTracer tracer = getExecutionTracer();
		tracer.trace.exitMethod(classname, methodname);		
		logger.trace("Left method "+classname+"."+methodname);
	}

	/**
	 * Called by the instrumented code each time a new source line is executed
	 * @param line
	 */
	public static void passedLine(String className, String methodName, int line) {
		ExecutionTracer tracer = getExecutionTracer();
		if(tracer.disabled)
			return;
		
		tracer.trace.linePassed(className, methodName, line);
	}
	
	/**
	 * Called by the instrumented code each time a new branch is taken
	 * @param val
	 * @param opcode
	 * @param line
	 */
	public static void passedBranch(int val, int opcode, int branch, int bytecode_id) {
		ExecutionTracer tracer = getExecutionTracer();
		//logger.info("passedBranch val="+val+", opcode="+opcode+", branch="+branch+", bytecode_id="+bytecode_id);
		if(tracer.disabled)
			return;

		
		//logger.trace("Called passedBranch1 with opcode "+AbstractVisitor.OPCODES[opcode]+" and val "+val+" in branch "+branch);
		double distance_true  = 0.0;
		double distance_false = 0.0;
		switch(opcode) {
		case Opcodes.IFEQ:
			distance_true  = Math.abs((double)val); // The greater abs is, the further away from 0
			distance_false = distance_true == 0 ? 1.0 : 0.0; // Anything but 0 is good
			if(distance_true == distance_false)
				logger.error("0 a: True and false distance are equal!");
			if(distance_true < 0)
				logger.error("0 a: True distance < 0 "+val);
			if(distance_false < 0)
				logger.error("0 a: False distance < 0 "+val);
			break;
		case Opcodes.IFNE:
			distance_false = Math.abs((double)val); // The greater abs is, the further away from 0
			distance_true  = distance_false == 0 ? 1.0 : 0.0; // Anything but 0 leads to NE
			if(distance_true == distance_false)
				logger.error("0 b: True and false distance are equal!");
			if(distance_true < 0)
				logger.error("0 b: True distance < 0: "+val);
			if(distance_false < 0)
				logger.error("0 b: False distance < 0 "+val);
			break;
		case Opcodes.IFLT:
			distance_true  = val >= 0 ? (double)val + 1.0 : 0.0; // The greater, the further away from < 0 
			distance_false = val < 0  ? 0.0 - (double)val + 1.0 : 0.0; // The smaller, the further away from < 0 
			if(distance_true == distance_false)
				logger.error("0 c: True and false distance are equal!");
			if(distance_true < 0)
				logger.error("0 c: True distance < 0!");
			if(distance_false < 0)
				logger.error("0 c: False distance < 0!");
			break;
		case Opcodes.IFGT:
			distance_true  = val <= 0 ? 0.0 - (double)val + 1.0: 0.0; 
			distance_false = val > 0  ? (double)val + 1.0 : 0.0;
			if(distance_true == distance_false)
				logger.error("0 d: True and false distance are equal!");
			if(distance_true < 0)
				logger.error("0 d: True distance < 0!");
			if(distance_false < 0)
				logger.error("0 d: False distance < 0!");
			break;
		case Opcodes.IFGE:
			distance_true  = val < 0 ? 0.0 - (double)val + 1.0: 0.0; 
			distance_false = val >= 0  ? (double)val + 1.0: 0.0;
			if(distance_true == distance_false)
				logger.error("0 e: True and false distance are equal!");
			if(distance_true < 0)
				logger.error("0 e: True distance < 0!");
			if(distance_false < 0)
				logger.error("0 e: False distance < 0!");
			break;
		case Opcodes.IFLE:
			distance_true  = val > 0 ? (double)val + 1.0: 0.0; // The greater, the further away from < 0 
			distance_false = val <= 0 ? 0.0 - (double)val + 1.0: 0.0; // The smaller, the further away from < 0 
			if(distance_true == distance_false)
				logger.error("0 f: True and false distance are equal!");
			if(distance_true < 0)
				logger.error("0 f: True distance < 0!");
			if(distance_false < 0)
				logger.error("0 f: False distance < 0!");
			break;
		default:
			logger.error("Unknown opcode: "+opcode);
		
		}
		logger.trace("Branch distance true : "+distance_true);
		logger.trace("Branch distance false: "+distance_false);


		// Add current branch to control trace
		tracer.trace.branchPassed(branch, bytecode_id, distance_true, distance_false);
//		tracer.trace.branchPassed(branch, distance_true, distance_false);
	}
	
	/**
	 * Called by the instrumented code each time a new branch is taken
	 * 
	 * @param val1
	 * @param val2
	 * @param opcode
	 * @param line
	 */
	public static void passedBranch(int val1, int val2, int opcode, int branch, int bytecode_id) {
		ExecutionTracer tracer = getExecutionTracer();
		if(tracer.disabled)
			return;

		logger.trace("Called passedBranch2 with opcode "+AbstractVisitor.OPCODES[opcode]+", val1="+val1+", val2="+val2+" in branch "+branch);
		double distance_true  = 0;
		double distance_false = 0;
		switch(opcode) {
		case Opcodes.IF_ICMPEQ:
			distance_true  = Math.abs((double)val1 - (double)val2); // The greater the difference, the further away
			distance_false = distance_true == 0 ? 1.0 : 0.0; // Anything but 0 is good
			if(distance_true < 0)
				logger.error("1 a: True distance < 0!");
			if(distance_false < 0)
				logger.error("1 a: False distance < 0!");
			break;
		case Opcodes.IF_ICMPNE:
			distance_false = Math.abs((double)val1 - (double)val2); // The greater abs is, the further away from 0
			distance_true  = distance_false == 0 ? 1.0 : 0.0; // Anything but 0 leads to NE
			if(distance_true < 0)
				logger.error("1 b: True distance < 0!");
			if(distance_false < 0)
				logger.error("1 b: False distance < 0!");
			break;
		case Opcodes.IF_ICMPLT:  // val1 < val2?
			distance_true  = val1 >= val2 ? (double)val1 - (double)val2 + 1.0: 0.0; // The greater, the further away from < 0 
			distance_false = val1 < val2  ? (double)val2 - (double)val1 + 1.0: 0.0; // The smaller, the further away from < 0 
			if(distance_true < 0)
				logger.error("1 c: True distance < 0!");
			if(distance_false < 0)
				logger.error("1 c: False distance < 0!");
			break;
		case Opcodes.IF_ICMPGE:
			distance_false = val1 >= val2  ? (double)val1 - (double)val2 + 1.0: 0.0; // The greater, the further away from < 0 
			distance_true  = val1 < val2 ? (double)val2 - (double)val1 + 1.0: 0.0; // The smaller, the further away from < 0 
			if(distance_true < 0)
				logger.error("1 d: True distance < 0: "+val1+"/"+val2+" -> "+distance_true);
			if(distance_false < 0)
				logger.error("1 d: False distance < 0!");
			break;
		case Opcodes.IF_ICMPGT:
			distance_false = val1 > val2 ? (double)val1 - (double)val2 + 1.0: 0.0; // The greater, the further away from < 0 
			distance_true  = val1 <= val2 ? (double)val2 - (double)val1  + 1.0: 0.0; // The smaller, the further away from < 0 
			if(distance_true < 0)
				logger.error("1 e: True distance < 0!");
			if(distance_false < 0)
				logger.error("1 e: False distance < 0!");
			break;
		case Opcodes.IF_ICMPLE:
			distance_true  = val1 > val2  ? (double)val1 - (double)val2 + 1.0: 0.0; // The greater, the further away from < 0 
			distance_false = val1 <= val2 ? (double)val2 - (double)val1 + 1.0: 0.0; // The smaller, the further away from < 0 
			if(distance_true < 0)
				logger.error("1 f: True distance < 0!");
			if(distance_false < 0)
				logger.error("1 f: False distance < 0!");
			break;
		default:
			logger.error("Unknown opcode: "+opcode);
		}
		logger.trace("Branch distance true: "+distance_true);
		logger.trace("Branch distance false: "+distance_false);

		
		if(distance_true == distance_false)
			logger.error("1: True and false distance are equal!");
		// Add current branch to control trace
		tracer.trace.branchPassed(branch, bytecode_id, distance_true, distance_false);
//		tracer.trace.branchPassed(branch, distance_true, distance_false);

	}
	
	/**
	 * Called by the instrumented code each time a new branch is taken
	 * 
	 * @param val1
	 * @param val2
	 * @param opcode
	 * @param line
	 */
	public static void passedBranch(Object val1, Object val2, int opcode, int branch, int bytecode_id) {
		ExecutionTracer tracer = getExecutionTracer();
		if(tracer.disabled)
			return;

		logger.trace("Called passedBranch3 with opcode "+AbstractVisitor.OPCODES[opcode]); //+", val1="+val1+", val2="+val2+" in branch "+branch);
		double distance_true  = 0;
		double distance_false = 0;
		//logger.warn("Disabling tracer: passedBranch with 2 Objects");
		
		switch(opcode) {
		case Opcodes.IF_ACMPEQ:
			if(val1 == null) {
				distance_true = val2 == null ? 0.0 : 1.0;
			}
			else {
				disable();
				try {
					distance_true = val1.equals(val2) ? 1.0 : 0.0;
				} catch(Throwable t) {
					logger.debug("Equality raised exception: "+t);
					distance_true = 1.0;
				} finally {
					enable();
				}
			}
			break;
		case Opcodes.IF_ACMPNE:
			if(val1 == null) {
				distance_true = val2 == null ? 1.0 : 0.0;
			}
			else {
				disable();
				try {
					// FIXME: This will lead to a call of passedBranch
					distance_true = val1.equals(val2) ? 0.0 : 1.0;
				} catch(Exception e) {
					logger.debug("Caught exception during comparison: "+e);
					distance_true = 1.0;
				} finally {
					enable();
				}
			}
			break;
		}
		
		
		distance_false = distance_true == 0 ? 1.0 : 0.0;
		logger.trace("Branch distance true: "+distance_true);
		logger.trace("Branch distance false: "+distance_false);
		if(distance_true == distance_false)
			logger.error("2: True and false distance are equal!");

		if(distance_true < 0)
			logger.error("2: True distance < 0!");
		if(distance_false < 0)
			logger.error("2: False distance < 0!");

		// Add current branch to control trace
		tracer.trace.branchPassed(branch, bytecode_id, distance_true, distance_false);
//		tracer.trace.branchPassed(branch, distance_true, distance_false);
	}
	
	/**
	 * Called by the instrumented code each time a new branch is taken
	 * 
	 * @param val
	 * @param opcode
	 * @param line
	 */
	public static void passedBranch(Object val, int opcode, int branch, int bytecode_id) {
		ExecutionTracer tracer = getExecutionTracer();
		if(tracer.disabled)
			return;
		
		//logger.warn("Disabling tracer in passedBranch");
		//disable();
		//logger.trace("Called passedBranch4 with opcode "+AbstractVisitor.OPCODES[opcode]+", val="+val+" in branch "+branch);
		double distance_true = 0;
		double distance_false = 0;
		switch(opcode) {
		case Opcodes.IFNULL:
			distance_true = val == null  ? 0.0 : 1.0;
			break;
		case Opcodes.IFNONNULL:
			distance_true = val == null  ? 1.0 : 0.0;
			break;
		default:
			logger.error("Warning: encountered opcode "+opcode);
		}
		distance_false = distance_true == 0 ? 1.0 : 0.0;
		//enable();
		
		logger.trace("Branch distance true: "+distance_true);
		logger.trace("Branch distance false: "+distance_false);
		if(distance_true == distance_false)
			logger.error("3: True and false distance are equal!");
		if(distance_true < 0)
			logger.error("3: True distance < 0!");
		if(distance_false < 0)
			logger.error("3: False distance < 0!");

		// Add current branch to control trace
		tracer.trace.branchPassed(branch, bytecode_id, distance_true, distance_false);
//		tracer.trace.branchPassed(branch, distance_true, distance_false);
	}
	
	public static void statementExecuted() {
		ExecutionTracer tracer = getExecutionTracer();
		if(!tracer.disabled)
			tracer.num_statements++;
	}

	public int getNumStatementsExecuted() {
		return num_statements;
	}

	
	public int getApproachLevel(Mutation mutation, ExecutionTrace trace) {
		String classname = mutation.getClassName().replace('.','/');
		String methodname = mutation.getMethodName();
		if(!graphs.containsKey(classname)) {
			return CLASS_DISTANCE;
		}
		int min = METHOD_DISTANCE;

		for(MethodCall m : trace.finished_calls) {
			if(m.class_name.equals(classname) && m.method_name.equals(methodname)) {
				int distance = graphs.get(classname).get(methodname).getControlDistance(m.branch_trace, mutation.getId());
				if(distance < min) {
					min = distance;
				}
			}
		}
		return min;
	}

	public double getBranchDistance(Mutation mutation, ExecutionTrace trace) {
		String classname = mutation.getClassName().replace('.','/');
		String methodname = mutation.getMethodName();
		if(!graphs.containsKey(classname)) {
			return CLASS_DISTANCE;
		}
		double min = METHOD_DISTANCE;

		for(MethodCall m : trace.finished_calls) {
			if(m.class_name.equals(classname) && m.method_name.equals(methodname)) {
				// TODO: Which one is the point of diversion?
				double distance = graphs.get(classname).get(methodname).getBranchDistance(m.branch_trace, m.true_distance_trace, mutation.getId());
				if(distance < min) {
					min = distance;
				}
			}
		}
		
		return min;
	}

	
	public void checkMutations() {
		int total = 0;
		
		for(Map<String,ControlFlowGraph> classgraphs : graphs.values()) {
			for(ControlFlowGraph g : classgraphs.values()) {
				total += g.getMutations().size();
			}
		}
		logger.info("Found a total of "+total+" mutations");
	}
	
	// TODO: Also need method name?
	public double getDiameter(Mutation mutation) {
		String classname = mutation.getClassName(); //replace('.','/');
		if(!graphs.containsKey(classname)) {
			logger.error("Cannot find mutated class: "+classname);
			return Double.POSITIVE_INFINITY;
		}

		String methodname = mutation.getMethodName();
		if(methodname == null)
			methodname = "<init>";

		if(!graphs.get(classname).containsKey(methodname)) {
			logger.error("Cannot find mutated method: "+methodname);
			logger.error(mutation);
			return Double.POSITIVE_INFINITY;
		}
		
		if(graphs.get(classname).get(methodname).containsMutation(mutation.getId()))
			return diameters.get(classname).get(methodname);
		else {
			logger.error("Mutated method: "+mutation.getClassName()+"."+methodname+" does not contain mutation "+mutation.getId());
			for(Long l : graphs.get(classname).get(methodname).getMutations()) {
				logger.error(" -> " + l);
			}
		}

		/*
		if(graphs.get(classname).get(methodname).containsVertex(-1 * mutation.getId().intValue())) 
			return diameters.get(classname).get(methodname);
		else {
			logger.error("Mutated method: "+mutation.getClassName()+"."+methodname+" does not contain mutation "+mutation.getId());
			logger.error(mutation);
			Set<Integer> vertices = graphs.get(classname).get(methodname).vertexSet();
			
			for(Integer x : vertices) {
				logger.info("  Method "+methodname+" contains "+x);
			}
			for(String cname : graphs.keySet()) {
				for(String mname : graphs.get(cname).keySet()) {
					if(graphs.get(cname).get(mname).containsVertex(-1 * mutation.getId().intValue()))
						logger.info("Method "+cname+"."+mname+" contains the mutant!");
				}
			}
			
		}
		*/
				
		return Double.POSITIVE_INFINITY;
	}
	

	
	private ExecutionTracer() {
		//method_calls = new ArrayList<String>();
		//class_access = new ArrayList<String>();
		//trace = new ArrayList<List<Integer> > ();
		trace = new ExecutionTrace();
		graphs = new HashMap<String, Map <String, ControlFlowGraph > >();
		diameters = new HashMap<String, Map <String, Double> > ();
	}
	
	
}
