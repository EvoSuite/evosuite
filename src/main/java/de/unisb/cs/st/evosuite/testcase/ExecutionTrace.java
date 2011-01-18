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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

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
		
		public MethodCall(String className, String methodName) {
			class_name = className;
			method_name = methodName;
			line_trace = new ArrayList<Integer>();
			branch_trace = new ArrayList<Integer>();
			true_distance_trace = new ArrayList<Double>();
			false_distance_trace = new ArrayList<Double>();
		}
		
		public String toString() {
			StringBuffer ret = new StringBuffer();
			ret.append(class_name);
			ret.append(":");
			ret.append(method_name);
			ret.append("\n");
			//ret.append("Lines: ");
			//for(Integer line : line_trace) {
			//	ret.append(" "+line);
			//}
			//ret.append("\n");
			ret.append("Branches: ");
			for(Integer branch : branch_trace) {
				ret.append(" "+branch);
			}
			ret.append("\n");
			ret.append("True Distances: ");
			for(Double distance : true_distance_trace) {
				ret.append(" "+distance);
			}
			ret.append("False Distances: ");
			for(Double distance : false_distance_trace) {
				ret.append(" "+distance);
			}
			ret.append("\n");
			return ret.toString();
		}
		
		public MethodCall clone() {
			MethodCall copy = new MethodCall(class_name, method_name);
			copy.line_trace           = new ArrayList<Integer>(line_trace);
			copy.branch_trace         = new ArrayList<Integer>(branch_trace);
			copy.true_distance_trace  = new ArrayList<Double>(true_distance_trace);
			copy.false_distance_trace = new ArrayList<Double>(false_distance_trace);
			return copy;
		}
	}
	
	// finished_calls;
	public List<MethodCall> finished_calls = new ArrayList<MethodCall>();
	
	// active calls
	Deque<MethodCall> stack = new LinkedList<MethodCall>();
	
	// Coverage information
	public Map<String, Map<String, Map<Integer, Integer>>> coverage = new HashMap<String, Map<String, Map<Integer, Integer> > >();

	// Data information
	public Map<String, Map<String, Map<Integer, Integer>>> return_data = new HashMap<String, Map<String, Map<Integer, Integer>>>();
	

	// Refactoring
	public Map<String, Integer> covered_methods = Collections.synchronizedMap(new HashMap<String, Integer>());
	public Map<String, Integer> covered_predicates = Collections.synchronizedMap(new HashMap<String, Integer>());
	public Map<String, Double> true_distances   = Collections.synchronizedMap(new HashMap<String, Double>());
	public Map<String, Double> false_distances  = Collections.synchronizedMap(new HashMap<String, Double>());
	
	public HashMap<String,HashMap<Integer,Integer>> passedDefs = new HashMap<String,HashMap<Integer,Integer>>();
	public HashMap<String,HashMap<Integer,Integer>> passedUses = new HashMap<String,HashMap<Integer,Integer>>();
	
	public ExecutionTrace() {
		stack.add(new MethodCall("", "")); // Main method
	}
	
	/**
	 * Add a new method call to stack
	 * 
	 * @param classname
	 * @param methodname
	 */
	public void enteredMethod(String classname, String methodname) {
		String id = classname+"."+methodname;
		if(!covered_methods.containsKey(id))
			covered_methods.put(id, 1);
		else
			covered_methods.put(id, covered_methods.get(id) + 1);
		
		if(trace_calls) {
			logger.trace("Entered method "+classname+"/"+methodname);
			stack.push(new MethodCall(classname, methodname));
		}
	}
	
	/**
	 * Pop last method call from stack
	 * 
	 * @param classname
	 * @param methodname
	 */
	public void exitMethod(String classname, String methodname) {
		if(trace_calls) {

			if(!stack.isEmpty() && !(stack.peek().method_name.equals(methodname))) {
				logger.info("Expecting "+stack.peek().method_name+", got "+methodname);
				if(stack.peek().method_name.equals("") && !stack.peek().branch_trace.isEmpty()) {
					logger.info("Found main method");
					finished_calls.add(stack.pop());
				} else {
					stack.peek();
					//stack.pop();
				}
			} else {
				//assert(stack.peek().method_name.equals(methodname));
				//logger.info("Tracing call to: "+stack.peek().method_name);
				finished_calls.add(stack.pop());
			}
		}
//		if(!(stack.peek().class_name.equals(classname) && stack.peek().method_name.equals(methodname))) {
//			logger.info("Expecting "+stack.peek().class_name+"."+stack.peek().method_name+", got "+classname+"."+methodname);
//		} else {
//			assert(stack.peek().class_name.equals(classname) && stack.peek().method_name.equals(methodname));
//			finished_calls.add(stack.pop());
//		}
	}
	
	/**
	 * Add line to currently active method call
	 * @param line
	 */
	public void linePassed(String className, String methodName, int line) {
		if(trace_calls) {
			if(stack.isEmpty()) {
				logger.warn("Method stack is empty!");
			} else {
				stack.peek().line_trace.add(line);
			}
		}
		if(!coverage.containsKey(className))
			coverage.put(className, new HashMap<String, Map<Integer, Integer>>());

		if(!coverage.get(className).containsKey(methodName))
			coverage.get(className).put(methodName, new HashMap<Integer, Integer>());

		if(!coverage.get(className).get(methodName).containsKey(line))
			coverage.get(className).get(methodName).put(line, 1);
		else
			coverage.get(className).get(methodName).put(line, coverage.get(className).get(methodName).get(line) + 1);
	}

	public void returnValue(String className, String methodName, int value) {
		if(!return_data.containsKey(className))
			return_data.put(className, new HashMap<String, Map<Integer, Integer>>());
		
		if(!return_data.get(className).containsKey(methodName))
			return_data.get(className).put(methodName, new HashMap<Integer, Integer>());
		
		if(!return_data.get(className).get(methodName).containsKey(value)) {
			logger.info("Got return value "+value);
			return_data.get(className).get(methodName).put(value, 1);
		}
		else {
			logger.info("Got return value again "+value);
			return_data.get(className).get(methodName).put(value, return_data.get(className).get(methodName).get(value) + 1);
		}
	}
	
	/**
	 * Add branch to currently active method call
	 * 
	 * @param branch
	 * @param true_distance
	 * @param false_distance
	 */
	public void branchPassed(int branch, int bytecode_id, double true_distance, double false_distance) {
		if(trace_calls) {
			stack.peek().branch_trace.add(bytecode_id);
			stack.peek().true_distance_trace.add(true_distance );
			stack.peek().false_distance_trace.add(false_distance);
		}
				
		String id = ""+branch;
		if(!covered_predicates.containsKey(id))
			covered_predicates.put(id, 1);
		else
			covered_predicates.put(id, covered_predicates.get(id) + 1);

		if(!true_distances.containsKey(id))
			true_distances.put(id, true_distance);
		else
			true_distances.put(id, Math.min(true_distances.get(id),true_distance));

		if(!false_distances.containsKey(id))
			false_distances.put(id, false_distance);
		else
			false_distances.put(id, Math.min(false_distances.get(id),false_distance));
	}
	
	/**
	 * Reset to 0
	 */
	public void clear() {
		finished_calls = new ArrayList<MethodCall>();
		stack = new LinkedList<MethodCall>();

//		stack.clear();
//		finished_calls.clear();
		stack.add(new MethodCall("", "")); // Main method
		coverage = new HashMap<String, Map<String, Map<Integer, Integer> > >();
		return_data = new HashMap<String, Map<String, Map<Integer, Integer> > >();

		true_distances  = Collections.synchronizedMap(new HashMap<String, Double>());
		false_distances = Collections.synchronizedMap(new HashMap<String, Double>());
		covered_methods = Collections.synchronizedMap(new HashMap<String, Integer>());
		covered_predicates = Collections.synchronizedMap(new HashMap<String, Integer>());
	}
	
	/**
	 * Create a deep copy
	 */
	public ExecutionTrace clone() {
		ExecutionTrace copy = new ExecutionTrace();
		for(MethodCall call : finished_calls) {
			copy.finished_calls.add(call.clone());
		}
		//copy.finished_calls.addAll(finished_calls);
		copy.coverage = new HashMap<String, Map<String, Map<Integer, Integer> > >();
		if(coverage != null)
			copy.coverage.putAll(coverage); 
		copy.return_data = new HashMap<String, Map<String, Map<Integer, Integer> > >();
		copy.return_data.putAll(return_data);
		/*
		if(stack != null && !stack.isEmpty() && stack.peek().method_name != null && stack.peek().method_name.equals("")) {
			logger.info("Copying main method");
			copy.finished_calls.add(stack.peek());
		}
		*/
		copy.true_distances.putAll(true_distances);
		copy.false_distances.putAll(false_distances);
		copy.covered_methods.putAll(covered_methods);
		copy.covered_predicates.putAll(covered_predicates);
		copy.passedDefs.putAll(passedDefs);
		copy.passedUses.putAll(passedUses);
		return copy;
	}
	
	public void finishCalls() {
		synchronized (stack) {
			while(!stack.isEmpty()) {
				finished_calls.add(stack.pop());
			}
		}
	}
	
	public boolean isMethodExecuted(Mutation m) {
		String classname  = m.getClassName();
		String methodname = m.getMethodName();
		
		return (coverage.containsKey(classname) && coverage.get(classname).containsKey(methodname));
	}
	
	public String toString() {
		StringBuffer ret = new StringBuffer();
		for(MethodCall m : finished_calls) {
			ret.append(m);
		}
		ret.append("\nCovered methods: ");
		for(Entry<String,Integer> entry : covered_methods.entrySet()) {
			ret.append(entry.getKey()+": "+entry.getValue()+", ");
		}
		ret.append("\nCovered predicates: ");
		for(Entry<String,Integer> entry : covered_predicates.entrySet()) {
			ret.append(entry.getKey()+": "+entry.getValue()+", ");
		}
		ret.append("\nTrue distances: ");
		for(Entry<String,Double> entry : true_distances.entrySet()) {
			ret.append(entry.getKey()+": "+entry.getValue()+", ");
		}
		ret.append("\nFalse distances: ");
		for(Entry<String,Double> entry : false_distances.entrySet()) {
			ret.append(entry.getKey()+": "+entry.getValue()+", ");
		}
		return ret.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((coverage == null) ? 0 : coverage.hashCode());
		result = prime * result
				+ ((finished_calls == null) ? 0 : finished_calls.hashCode());
		result = prime * result
				+ ((return_data == null) ? 0 : return_data.hashCode());
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
