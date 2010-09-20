/**
 * 
 */
package de.unisb.cs.st.evosuite.branch;

import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;


/**
 * @author Gordon Fraser
 *
 */
public class BranchCoverageGoal {

	private static Logger logger = Logger.getLogger(BranchCoverageGoal.class);
	
	int branch_id;
	
	int bytecode_id;
	
	boolean value;
	
	ControlFlowGraph cfg;
	
	String className;
	
	String methodName;
	
	private static TestCaseExecutor executor = new TestCaseExecutor();
	
	@SuppressWarnings("unchecked")
	public class Distance implements Comparable {
		int approach  = 0;
		double branch = 0.0;

		public int compareTo(Object o) {
			if(o instanceof Distance) {
				Distance d = (Distance)o;
				if(approach < d.approach)
					return -1;
				else if(approach > d.approach)
					return 1;
				else {
					if(branch < d.branch)
						return -1;
					else if(branch > d.branch)
						return 1;
					else
						return 0;
				}
			}
			return 0;
		}
	}
	
	public BranchCoverageGoal(int branch_id, int bytecode_id, boolean value, ControlFlowGraph cfg, String className, String methodName) {
		this.branch_id = branch_id;
		this.bytecode_id = bytecode_id;
		this.value = value;
		this.cfg = cfg;
		this.className = className;
		this.methodName = methodName;
	}

	public BranchCoverageGoal(String className, String methodName) {
		this.branch_id = 0;
		this.bytecode_id = 0;
		this.value = true;
		this.cfg = null;
		this.className = className;
		this.methodName = methodName;
	}

	/**
	 * Determine if there is an existing test case covering this goal
	 * @return
	 */
	public boolean isCovered(List<TestCase> tests) {
		for(TestCase test : tests) {
			ExecutionResult result = runTest(test);
			Distance d = getDistance(result);
			if(d.approach == 0 && d.branch == 0.0)
				return true;
		}
		return false;
	}
	
	private static boolean hasTimeout(ExecutionResult result) {
		
		if(result == null) {
			logger.warn("Result is null!");
			return false;
		}
		else if(result.test == null) {
			logger.warn("Test is null!");
			return false;
		}
		int size = result.test.size();
		if(result.exceptions.containsKey(size)) {
			if(result.exceptions.get(size) instanceof TestCaseExecutor.TimeoutExceeded) {
				return true;
			}
		}
		
		return false;
	}
	
	public Distance getDistance(ExecutionResult result) {
		Distance d = new Distance();
		
		if(hasTimeout(result)) {
			logger.info("Has timeout!");
			if(cfg == null) {
				d.approach = 20;
			} else {
				d.approach = cfg.getDiameter() + 2;
			}
			return d;
		}
		
		if(cfg == null) {
			for(MethodCall call : result.trace.finished_calls) {
				if(call.class_name.equals(""))
					continue;
				if((call.class_name+"."+call.method_name).equals(methodName)) {
					return d;
				}
			}
			d.approach = 1;
			return d;
		}
	
		d.approach = cfg.getDiameter() + 1;
		
		// Minimal distance between target node and path
		for(MethodCall call : result.trace.finished_calls) {
			if(call.class_name.equals(className) && call.method_name.equals(methodName)) {
				Distance d2;
				if(value)
					d2 = getDistance(call.branch_trace, call.true_distance_trace, bytecode_id);
				else
					d2 = getDistance(call.branch_trace, call.false_distance_trace, bytecode_id);
				if(d2.compareTo(d) < 0) {
					d = d2;
				}
			}
		}
		
		return d;
	}
	
	public Distance getDistance(List<Integer> path, List<Double> distances, int branch_id) {
		CFGVertex m = cfg.getVertex(branch_id);
		Distance d = new Distance();
		if(m == null) {
			logger.error("Could not find branch node");
			return d;
		}
		
		int min_approach  = cfg.getDiameter() + 1;
		double min_dist = 0.0;
		for(int i = 0; i<path.size(); i++) {
			CFGVertex v = cfg.getVertex(path.get(i));
			if(v != null) {
				int distance = cfg.getDistance(v, m);
				logger.debug("B: Path vertex "+i+" has distance: "+distance+" and branch distance "+distances.get(i));

				if(distance < min_approach && distance >= 0) {
					min_approach = distance;
					min_dist = distances.get(i);
				}
			} else {
				logger.info("Path vertex does not exist in graph");
			}
		}

		d.approach = min_approach;
		d.branch   = min_dist;
		
		return d;
	}
	
	/**
	 * Execute a test case
	 * @param test
	 *   The test case to execute
	 * @param mutant
	 *   The mutation to active (null = no mutation)
	 *   
	 * @return
	 *   Result of the execution
	 */
	private ExecutionResult runTest(TestCase test) {
		
		ExecutionResult result = new ExecutionResult(test, null);
		
		try {
			
			result.exceptions = executor.runWithTrace(test);
			executor.setLogging(true);
			result.trace = ExecutionTracer.getExecutionTracer().getTrace();
		} catch(Exception e) {
			System.out.println("TG: Exception caught: "+e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

	/**
	 * Readable representation
	 */
	public String toString() {
		String name = className+"."+methodName+":"+branch_id;
		if(cfg == null)
			return name;
		if(value)
			return name+" - true";
		else
			return name+" - false";
	}
}
