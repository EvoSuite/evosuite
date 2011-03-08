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

package de.unisb.cs.st.evosuite.testcase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUsePool;
import de.unisb.cs.st.evosuite.coverage.dataflow.Definition;
import de.unisb.cs.st.evosuite.coverage.dataflow.Use;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * Keep a trace of the program execution
 * 
 * @author Gordon Fraser
 * 
 */
public class ExecutionTrace {

	private static Logger logger = Logger.getLogger(ExecutionTrace.class);

	public static boolean trace_calls = false;

	public static void disableTraceCalls() {
		trace_calls = false;
	}

	public static void enableTraceCalls() {
		trace_calls = true;
	}

	public class MethodCall {
		public String class_name;
		public String method_name;
		public List<Integer> line_trace;
		public List<Integer> branch_trace;
		public List<Double> true_distance_trace;
		public List<Double> false_distance_trace;
		public ArrayList<HashMap<String,Integer>> active_definitions_trace; 
		public int methodID;
		public int callingObjectID;

		public MethodCall(String className, String methodName, int methodID, int callingObjectID) {
			class_name = className;
			method_name = methodName;
			line_trace = new ArrayList<Integer>();
			branch_trace = new ArrayList<Integer>();
			true_distance_trace = new ArrayList<Double>();
			false_distance_trace = new ArrayList<Double>();
			this.methodID = methodID;
			this.callingObjectID = callingObjectID;
			if(Properties.CRITERION.equals("defuse")) // this might take some memory
				active_definitions_trace = new ArrayList<HashMap<String,Integer>>();
		}

		@Override
		public String toString() {
			StringBuffer ret = new StringBuffer();
			ret.append(class_name);
			ret.append(":");
			ret.append(method_name);
			ret.append("\n");
			// ret.append("Lines: ");
			// for(Integer line : line_trace) {
			// ret.append(" "+line);
			// }
			// ret.append("\n");
			ret.append("Branches: ");
			for (Integer branch : branch_trace) {
				ret.append(" " + branch);
			}
			ret.append("\n");
			ret.append("True Distances: ");
			for (Double distance : true_distance_trace) {
				ret.append(" " + distance);
			}
			ret.append("False Distances: ");
			for (Double distance : false_distance_trace) {
				ret.append(" " + distance);
			}
			ret.append("\n");
			return ret.toString();
		}

		@Override
		public MethodCall clone() {
			MethodCall copy = new MethodCall(class_name, method_name, methodID, callingObjectID);
			copy.line_trace = new ArrayList<Integer>(line_trace);
			copy.branch_trace = new ArrayList<Integer>(branch_trace);
			copy.true_distance_trace = new ArrayList<Double>(true_distance_trace);
			copy.false_distance_trace = new ArrayList<Double>(false_distance_trace);
			copy.callingObjectID = callingObjectID;
			copy.methodID = methodID;
			if(Properties.CRITERION.equals("defuse"))
				copy.active_definitions_trace = new ArrayList<HashMap<String,Integer>>(active_definitions_trace); 
			
			return copy;
		}
	}

	// finished_calls;
	public List<MethodCall> finished_calls = new ArrayList<MethodCall>();

	// active calls
	Deque<MethodCall> stack = new LinkedList<MethodCall>();

	// Coverage information
	public Map<String, Map<String, Map<Integer, Integer>>> coverage = new HashMap<String, Map<String, Map<Integer, Integer>>>();

	// Data information
	public Map<String, Map<String, Map<Integer, Integer>>> return_data = new HashMap<String, Map<String, Map<Integer, Integer>>>();

	// Refactoring

	
	// TODO research: Collections.synchronizedMap with other maps in there
	// for each Variable-Name these maps hold the data for which objectID which Definition or Use respectively were passed and at which time (duCounter)
	public Map<String,HashMap<Integer,HashMap<Integer,Integer>>> passedDefs = new HashMap<String,HashMap<Integer,HashMap<Integer,Integer>>>();
	public Map<String,HashMap<Integer,HashMap<Integer,Integer>>> passedUses = new HashMap<String,HashMap<Integer,HashMap<Integer,Integer>>>();

	public Map<String, Integer> covered_methods = new HashMap<String, Integer>();
	public Map<String, Integer> covered_predicates = new HashMap<String, Integer>();
	public Map<String, Double> true_distances = new HashMap<String, Double>();
	public Map<String, Double> false_distances = new HashMap<String, Double>();

	// number of seen Definitions and uses for indexing purposes
	private int duCounter = 0;
	
	// for defuse-coverage it is important to keep track of all the objects that called the ExecutionTracer
	private int objectCounter = 0;
	public Map<Integer,Object> knownCallerObjects = Collections.synchronizedMap(new HashMap<Integer,Object>());	
	
	private int methodID = 0;
	
	public HashMap<Integer,HashMap<String,Integer>> activeDefinitions = new HashMap<Integer, HashMap<String,Integer>>();
	
	public ExecutionTrace() {
		stack.add(new MethodCall("", "",0,0)); // Main method
	}

	/**
	 * Add a new method call to stack
	 * 
	 * @param classname
	 * @param methodname
	 */
	public void enteredMethod(String classname, String methodname, Object caller) {
		String id = classname + "." + methodname;
		if (!covered_methods.containsKey(id))
			covered_methods.put(id, 1);
		else
			covered_methods.put(id, covered_methods.get(id) + 1);

		if (trace_calls) {
			int callingObjectID = registerObject(caller);
			methodID++;
			MethodCall call = new MethodCall(classname, methodname,methodID,callingObjectID);
			if(Properties.CRITERION.equals("defuse")) {
				// TODO	research that this doesn't break BranchFitness Calculation
				call.branch_trace.add(-1);
				call.true_distance_trace.add(0.0);
				call.false_distance_trace.add(1.0);
				call.active_definitions_trace.add(getCopyOfActiveDefinitions(callingObjectID));
			}
			stack.push(call);
		}
	}

	/**
	 * Pop last method call from stack
	 * 
	 * @param classname
	 * @param methodname
	 */
	public void exitMethod(String classname, String methodname) {
		if (trace_calls) {

			if (!stack.isEmpty() && !(stack.peek().method_name.equals(methodname))) {
				logger.debug("Expecting " + stack.peek().method_name + ", got "
				        + methodname);
				if (stack.peek().method_name.equals("")
				        && !stack.peek().branch_trace.isEmpty()) {
					logger.info("Found main method");
					finished_calls.add(stack.pop());
				} else {
					// Usually, this happens if we use mutation testing and the
					// mutation
					// causes an unexpected exception or timeout
					stack.pop();
				}
			} else {
				finished_calls.add(stack.pop());
			}
		}
	}

	/**
	 * Add line to currently active method call
	 * 
	 * @param line
	 */
	public void linePassed(String className, String methodName, int line) {
		if (trace_calls) {
			if (stack.isEmpty()) {
				logger.warn("Method stack is empty: " + className + "." + methodName);
			} else {
				stack.peek().line_trace.add(line);
			}
		}
		if (!coverage.containsKey(className))
			coverage.put(className, new HashMap<String, Map<Integer, Integer>>());

		if (!coverage.get(className).containsKey(methodName))
			coverage.get(className).put(methodName, new HashMap<Integer, Integer>());

		if (!coverage.get(className).get(methodName).containsKey(line))
			coverage.get(className).get(methodName).put(line, 1);
		else
			coverage.get(className).get(methodName).put(line,
			                                            coverage.get(className).get(methodName).get(line) + 1);
	}

	public void returnValue(String className, String methodName, int value) {
		if (!return_data.containsKey(className))
			return_data.put(className, new HashMap<String, Map<Integer, Integer>>());

		if (!return_data.get(className).containsKey(methodName))
			return_data.get(className).put(methodName, new HashMap<Integer, Integer>());

		if (!return_data.get(className).get(methodName).containsKey(value)) {
			// logger.info("Got return value "+value);
			return_data.get(className).get(methodName).put(value, 1);
		} else {
			// logger.info("Got return value again "+value);
			return_data.get(className).get(methodName).put(value,
			                                               return_data.get(className).get(methodName).get(value) + 1);
		}
	}

	/**
	 * Add branch to currently active method call
	 * 
	 * @param branch
	 * @param true_distance
	 * @param false_distance
	 */
	public void branchPassed(int branch, int bytecode_id, double true_distance,
	        double false_distance) {
		if (trace_calls) {
			stack.peek().branch_trace.add(bytecode_id);
			stack.peek().true_distance_trace.add(true_distance);
			stack.peek().false_distance_trace.add(false_distance);
			if(Properties.CRITERION.equals("defuse")) {
				stack.peek().active_definitions_trace.add(getCopyOfActiveDefinitions(stack.peek().callingObjectID));
			}
		}

		String id = "" + branch;
		if (!covered_predicates.containsKey(id))
			covered_predicates.put(id, 1);
		else
			covered_predicates.put(id, covered_predicates.get(id) + 1);

		if (!true_distances.containsKey(id))
			true_distances.put(id, true_distance);
		else
			true_distances.put(id, Math.min(true_distances.get(id), true_distance));

		if (!false_distances.containsKey(id))
			false_distances.put(id, false_distance);
		else
			false_distances.put(id, Math.min(false_distances.get(id), false_distance));
	}
	
	
	private HashMap<String, Integer> getCopyOfActiveDefinitions(int objectID) {
		
		HashMap<String,Integer> r = new HashMap<String,Integer>();
		if(activeDefinitions.get(objectID) == null)
			return r;
		
		for(String var : activeDefinitions.get(objectID).keySet()) {
			r.put(var,activeDefinitions.get(objectID).get(var));
		}
		return r;
	}

	public void definitionPassed(String className, String varName,
			String methodName, Object caller, int branchID, int defID) {
		
		Definition def = DefUsePool.getDefinitionByDefID(defID);
		if(def == null)
			throw new IllegalStateException("expect DefUsePool to known defIDs that are passed by instrumented code");
		
		int objectID = registerObject(caller);
		// if this is a static variable, treat objectID as zero for consistency in the representation of static data
		if(objectID != 0) { 		
			if(def.isStaticDU())
				objectID = 0;
		}
		
		if(passedDefs.get(varName)==null) 
			passedDefs.put(varName,new HashMap<Integer,HashMap<Integer,Integer>>());
		HashMap<Integer, Integer> defs = passedDefs.get(varName).get(objectID);
		if (defs == null)
			defs = new HashMap<Integer, Integer>();
		defs.put(duCounter, defID);
		passedDefs.get(varName).put(objectID, defs);
		
		// TODO objectID = 0 OK for static variables?
		
		if(activeDefinitions.get(objectID) == null)
			activeDefinitions.put(objectID, new HashMap<String,Integer>());
		activeDefinitions.get(objectID).put(def.getDUVariableName(), defID);
		
		duCounter++;
	}
	
	public void usePassed(String className, String varName, String methodName,
			Object caller, int branchID, int useID) {
		
		int objectID = registerObject(caller);
		// if this is a static variable, treat objectID as zero for consistency in the representation of static data
		if(objectID != 0) {
			Use use = DefUsePool.getUseByUseID(useID);
			if(use == null)
				throw new IllegalStateException("expect DefUsePool to known defIDs that are passed by instrumented code");
			if(use.isStaticDU())
				objectID = 0;
		}		
		
		if(passedUses.get(varName)==null) 
			passedUses.put(varName,new HashMap<Integer,HashMap<Integer,Integer>>());
		
		HashMap<Integer, Integer> uses = passedUses.get(varName).get(objectID);
		if (uses == null)
			uses = new HashMap<Integer, Integer>();

		uses.put(duCounter, useID);
		passedUses.get(varName).put(objectID, uses);

		duCounter++;
	}	
	
	/**
	 * Returns the objecectID for the given object.
	 * 
	 *  The ExecutionTracer keeps track of all objects it gets called from in order to
	 *  distinguish them later in the fitness calculation for the defuse-Coverage-Criterion. 
	 */
	private int registerObject(Object caller) {
		if(caller == null)
			return 0;
		
		for(Integer objectID : knownCallerObjects.keySet()) {
			if(knownCallerObjects.get(objectID)==caller)
				return objectID;
		}
		// object unknown so far
		objectCounter++;
		knownCallerObjects.put(objectCounter, caller);
		return objectCounter;
	}	
	
	public ExecutionTrace getTraceForObject(int objectID) {
		ExecutionTrace r = clone();
		
//		System.out.println("cutting trace for object "+objectID);
//		System.out.println("calls before cutting: "+r.finished_calls.size());
		
		// WARNING  this will not affect this.true_distances and other fields of ExecutionTrace
		// TODO 	research, that this is enough for the fitness-calculation of BranchCoverageTestFitness->getFitness()
		ArrayList<Integer> removableCalls = new ArrayList<Integer>();
		for(int i=0; i<r.finished_calls.size();i++) {
			MethodCall call = r.finished_calls.get(i);
			if(call.callingObjectID != objectID && call.callingObjectID!=0){
//				System.out.println("want to remove "+i);
				removableCalls.add(i);
			}
		}
		Collections.sort(removableCalls);
		for(int i=removableCalls.size()-1; i>=0;i--) {
//			System.out.println("going to remove "+removableCalls.get(i));
			Object o = r.finished_calls.remove((int)removableCalls.get(i));
//			if(o==null)
//				System.out.println("null returnd");
		}
		
//		System.out.println("calls after cutting: "+r.finished_calls.size());
//		for(MethodCall call : r.finished_calls) {
//			System.out.println("  "+call.method_name+" on object "+call.callingObjectID);
//		}
		
		return r;
	}

	public ExecutionTrace getTraceForDefinition(Definition def) {
		ExecutionTrace r = clone();
		
		// WARNING  this will not affect this.true_distances and other fields of ExecutionTrace
		// TODO 	research, that this is enough for the fitness-calculation of BranchCoverageTestFitness->getFitness() and doesn't break it
		
		ArrayList<Integer> removableCalls = new ArrayList<Integer>();
		for(int callPos=0;callPos<r.finished_calls.size();callPos++) {
			MethodCall call = r.finished_calls.get(callPos);
			ArrayList<Integer> removableIndices = new ArrayList<Integer>();
			for(int i = 0;i<call.active_definitions_trace.size();i++) {
				Map<String,Integer> activeDefs = call.active_definitions_trace.get(i);
				if(activeDefs == null) // this shouldn't happen
					continue;
				if(activeDefs.get(def.getDUVariableName()) == null || activeDefs.get(def.getDUVariableName()) != def.getDefID())
					removableIndices.add(i);
			}
			Collections.sort(removableIndices);
			for(int i=removableIndices.size()-1;i>=0;i--) {
				call.active_definitions_trace.remove(removableIndices.get(i));
			}
			if(call.active_definitions_trace.size() == 0)
				removableCalls.add(callPos);
		}
		Collections.sort(removableCalls);
		for(int i=removableCalls.size()-1;i>=0;i--) {
			r.finished_calls.remove(removableCalls.get(i));
		}

		return r;
	}
	
	

	/**
	 * Reset to 0
	 */
	public void clear() {
		finished_calls = new ArrayList<MethodCall>();
		stack = new LinkedList<MethodCall>();

		// stack.clear();
		// finished_calls.clear();
		stack.add(new MethodCall("", "",0,0)); // Main method
		coverage = new HashMap<String, Map<String, Map<Integer, Integer>>>();
		return_data = new HashMap<String, Map<String, Map<Integer, Integer>>>();

		methodID = 0;
		duCounter = 0;
		objectCounter = 0;
		activeDefinitions = new HashMap<Integer,HashMap<String,Integer>>();
		knownCallerObjects = new HashMap<Integer,Object>();
		true_distances = new HashMap<String, Double>();
		false_distances = new HashMap<String, Double>();
		covered_methods = new HashMap<String, Integer>();
		covered_predicates = new HashMap<String, Integer>();
 	    passedDefs = new HashMap<String,HashMap<Integer,HashMap<Integer,Integer>>>();
		passedUses = new HashMap<String,HashMap<Integer,HashMap<Integer,Integer>>>();
	}

	/**
	 * Create a deep copy
	 */
	@Override
	public ExecutionTrace clone() {

		ExecutionTrace copy = new ExecutionTrace();
		for (MethodCall call : finished_calls) {
			copy.finished_calls.add(call.clone());
		}
		// copy.finished_calls.addAll(finished_calls);
		copy.coverage = new HashMap<String, Map<String, Map<Integer, Integer>>>();
		if (coverage != null)
			copy.coverage.putAll(coverage);
		copy.return_data = new HashMap<String, Map<String, Map<Integer, Integer>>>();
		copy.return_data.putAll(return_data);
		/*
		 * if(stack != null && !stack.isEmpty() && stack.peek().method_name !=
		 * null && stack.peek().method_name.equals("")) {
		 * logger.info("Copying main method");
		 * copy.finished_calls.add(stack.peek()); }
		 */
		copy.true_distances.putAll(true_distances);
		copy.false_distances.putAll(false_distances);
		copy.covered_methods.putAll(covered_methods);
		copy.covered_predicates.putAll(covered_predicates);
		copy.passedDefs.putAll(passedDefs);
		copy.passedUses.putAll(passedUses);
		copy.methodID = methodID;
		copy.duCounter = duCounter;
		copy.objectCounter = objectCounter;
		copy.knownCallerObjects.putAll(knownCallerObjects);
		copy.activeDefinitions.putAll(activeDefinitions);
		return copy;
	}

	public void finishCalls() {
		synchronized (stack) {
			logger.trace("Calls left on stack: " + stack.size());
			while (!stack.isEmpty()) {
				logger.trace("Call: " + stack.peek());
				finished_calls.add(stack.pop());
			}
		}
	}

	public boolean isMethodExecuted(Mutation m) {
		String classname = m.getClassName();
		String methodname = m.getMethodName();

		return (coverage.containsKey(classname) && coverage.get(classname).containsKey(methodname));
	}
	
	public String toDefUseTraceInformation() {
		StringBuffer r = new StringBuffer();
		r.append("Definitions: \n");
		for(String var : passedDefs.keySet()) {
			r.append(" for variable: "+var+":\n");
			for(Integer objectID : passedDefs.get(var).keySet()) {
				r.append("   on object "+objectID+":\n");
				for(Integer duPos : passedDefs.get(var).get(objectID).keySet()) {
					r.append("     #"+duPos+": Def "+passedDefs.get(var).get(objectID).get(duPos)+"\n");
				}
			}
		}
		r.append("Uses: \n");
		for(String var : passedUses.keySet()) {
			r.append(" for variable: "+var+":\n");
			for(Integer objectID : passedUses.get(var).keySet()) {
				r.append("  on object "+objectID+":\n");
				for(Integer duPos : passedUses.get(var).get(objectID).keySet()) {
					r.append("   #"+duPos+": Use "+passedUses.get(var).get(objectID).get(duPos)+"\n");
				}
			}
		}		
		return r.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		for (MethodCall m : finished_calls) {
			ret.append(m);
		}
		ret.append("\nCovered methods: ");
		for (Entry<String, Integer> entry : covered_methods.entrySet()) {
			ret.append(entry.getKey() + ": " + entry.getValue() + ", ");
		}
		ret.append("\nCovered predicates: ");
		for (Entry<String, Integer> entry : covered_predicates.entrySet()) {
			ret.append(entry.getKey() + ": " + entry.getValue() + ", ");
		}
		ret.append("\nTrue distances: ");
		for (Entry<String, Double> entry : true_distances.entrySet()) {
			ret.append(entry.getKey() + ": " + entry.getValue() + ", ");
		}
		ret.append("\nFalse distances: ");
		for (Entry<String, Double> entry : false_distances.entrySet()) {
			ret.append(entry.getKey() + ": " + entry.getValue() + ", ");
		}
		return ret.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coverage == null) ? 0 : coverage.hashCode());
		result = prime * result
		        + ((finished_calls == null) ? 0 : finished_calls.hashCode());
		result = prime * result + ((return_data == null) ? 0 : return_data.hashCode());
		result = prime * result + ((stack == null) ? 0 : stack.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecutionTrace other = (ExecutionTrace) obj;
		if (coverage == null) {
			if (other.coverage != null)
				return false;
		} else if (!coverage.equals(other.coverage))
			return false;
		if (finished_calls == null) {
			if (other.finished_calls != null)
				return false;
		} else if (!finished_calls.equals(other.finished_calls))
			return false;
		if (return_data == null) {
			if (other.return_data != null)
				return false;
		} else if (!return_data.equals(other.return_data))
			return false;
		if (stack == null) {
			if (other.stack != null)
				return false;
		} else if (!stack.equals(other.stack))
			return false;
		return true;
	}

}
