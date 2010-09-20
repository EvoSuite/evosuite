/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.ga.Chromosome;


/**
 * @author Gordon Fraser
 *
 */
public class BranchCoverageFitnessFunction extends TestSuiteFitnessFunction {

	private static Logger logger = Logger.getLogger(TestSuiteFitnessFunction.class);
	
	public final int total_branches = CFGMethodAdapter.branch_counter;

	public final int branchless_methods = CFGMethodAdapter.branchless_methods.size();

	public final int total_methods = TestCluster.getInstance().num_defined_methods; 
	
	public int covered_branches = 0;
	
	public int covered_methods = 0;
	
	public double best_fitness = Double.MAX_VALUE;
		
	public final int total_goals = 2 * total_branches + branchless_methods;
	
	public BranchCoverageFitnessFunction() {
		logger.info("Total goals: "+total_goals);
		logger.info("Total branches: "+total_branches);
	}
	
	public double getCoverage(Chromosome individual) {
		
		TestSuiteChromosome suite = (TestSuiteChromosome)individual;
		List<ExecutionResult> results = runTestSuite(suite);
		double fitness = 0.0;
		Map<String, Double> true_distance = new HashMap<String, Double>();
		Map<String, Double> false_distance = new HashMap<String, Double>();
		Map<String, Integer> predicate_count = new HashMap<String, Integer>();
		Map<String, Integer> call_count = new HashMap<String, Integer>();
		
		for(ExecutionResult result : results) {
			for(Entry<String, Integer> entry : result.trace.covered_methods.entrySet()) {
				if(!call_count.containsKey(entry.getKey()))
					call_count.put(entry.getKey(), entry.getValue());
				else {
					call_count.put(entry.getKey(), call_count.get(entry.getKey()) + entry.getValue());
				}
			}
			for(Entry<String, Integer> entry : result.trace.covered_predicates.entrySet()) {
				if(!predicate_count.containsKey(entry.getKey()))
					predicate_count.put(entry.getKey(), entry.getValue());
				else {
					predicate_count.put(entry.getKey(), predicate_count.get(entry.getKey()) + entry.getValue());
				}
			}
			for(Entry<String, Double> entry : result.trace.true_distances.entrySet()) {
				if(!true_distance.containsKey(entry.getKey()))
					true_distance.put(entry.getKey(), entry.getValue());
				else {
					true_distance.put(entry.getKey(), Math.min(true_distance.get(entry.getKey()), entry.getValue()));
				}
			}
			for(Entry<String, Double> entry : result.trace.false_distances.entrySet()) {
				if(!false_distance.containsKey(entry.getKey()))
					false_distance.put(entry.getKey(), entry.getValue());
				else {
					false_distance.put(entry.getKey(), Math.min(false_distance.get(entry.getKey()), entry.getValue()));
				}
			}

			/*
			for(MethodCall call : result.trace.finished_calls) {
				String id = call.class_name+":"+call.method_name+":";
				logger.trace("Analyzing call: "+call.class_name+"."+call.method_name);
				if(TestCluster.getInstance().test_methods.contains(call.class_name+"."+call.method_name)) {
					if(!call_count.containsKey(id))
						call_count.put(id, 1);
					else {
						call_count.put(id, call_count.get(id) + 1);
					}
//					//if(call.method_name.contains("$"))
	//					logger.trace("Skipping: "+call.class_name+"."+call.method_name);
		//				continue;
				}

				
				for(int i = 0; i<call.branch_trace.size(); i++) {
					String key = id+call.branch_trace.get(i);
					
					if(!predicate_count.containsKey(key))
						predicate_count.put(key, 1);
					else
						predicate_count.put(key, predicate_count.get(key) + 1);
					// Distance to this branch is 0, distance to other branch is in distance_trace

					
					if(!true_distance.containsKey(key) || true_distance.get(key) > call.true_distance_trace.get(i)) {
						true_distance.put(key, (double)call.true_distance_trace.get(i));
					}
					if(!false_distance.containsKey(key) || false_distance.get(key) > call.false_distance_trace.get(i)) {
						false_distance.put(key, (double)call.false_distance_trace.get(i));
					}
				}
			}
				*/
		}
		
		int num = 0;
		int num_covered = 0;

		//for(Entry<String, Double> entry : true_distance.entrySet()) {
		//	logger.trace("Branch "+entry.getKey()+": "+normalize(entry.getValue())+"/"+normalize(false_distance.get(entry.getKey())));
		//}
		for(String key : predicate_count.keySet()) {
			Integer val = predicate_count.get(key);
			if(val == 1 && true_distance.get(key) > 0)
				true_distance.put(key, 1.0);
			else if(val == 1 && false_distance.get(key) > 0)
				false_distance.put(key, 1.0);
		}
		for(Double val : true_distance.values()) {
			fitness += normalize(val);
			num++;
			if(val == 0)
				num_covered ++;
		}
		for(Double val : false_distance.values()) {
			fitness += normalize(val);
			num++;
			if(val == 0)
				num_covered ++;
		}

		return num_covered / (1.0*total_branches);
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
	
	/**
	 * Execute all tests and count covered branches
	 */
	/*
	public double getFitnessBroken(Chromosome individual) {
		logger.trace("Calculating branch fitness");
		
		long start = System.currentTimeMillis();
		
		TestSuiteChromosome suite = (TestSuiteChromosome)individual;
		long estart = System.currentTimeMillis();
		List<ExecutionResult> results = runTestSuite(suite);
		long eend = System.currentTimeMillis();
		double fitness = 0.0;
		Map<String, Double> true_distance = new HashMap<String, Double>();
		Map<String, Double> false_distance = new HashMap<String, Double>();
		Map<String, Integer> predicate_count = new HashMap<String, Integer>();
		Map<String, Integer> call_count = new HashMap<String, Integer>();
		
		for(ExecutionResult result : results) {
			
			 if(hasTimeout(result)) {
				updateIndividual(individual, total_branches*2 + total_methods);
				suite.coverage = 0.0;
				logger.info("Test case has timed out, setting fitness to max value "+(total_branches*2 + total_methods));
				return total_branches*2 + total_methods;
			}
			
			for(Entry<String, Integer> entry : result.trace.covered_methods.entrySet()) {
				if(!call_count.containsKey(entry.getKey()))
					call_count.put(entry.getKey(), entry.getValue());
				else {
					call_count.put(entry.getKey(), call_count.get(entry.getKey()) + entry.getValue());
				}
			}
			for(Entry<String, Integer> entry : result.trace.covered_predicates.entrySet()) {
				if(!predicate_count.containsKey(entry.getKey()))
					predicate_count.put(entry.getKey(), entry.getValue());
				else {
					predicate_count.put(entry.getKey(), predicate_count.get(entry.getKey()) + entry.getValue());
				}
			}
			for(Entry<String, Double> entry : result.trace.true_distances.entrySet()) {
				if(!true_distance.containsKey(entry.getKey()))
					true_distance.put(entry.getKey(), entry.getValue());
				else {
					true_distance.put(entry.getKey(), Math.min(true_distance.get(entry.getKey()), entry.getValue()));
				}
			}
			for(Entry<String, Double> entry : result.trace.false_distances.entrySet()) {
				if(!false_distance.containsKey(entry.getKey()))
					false_distance.put(entry.getKey(), entry.getValue());
				else {
					false_distance.put(entry.getKey(), Math.min(false_distance.get(entry.getKey()), entry.getValue()));
				}
			}
		}
		
		int num = 0;
		int num_covered = 0;

		//for(Entry<String, Double> entry : true_distance.entrySet()) {
		//	logger.trace("Branch "+entry.getKey()+": "+normalize(entry.getValue())+"/"+normalize(false_distance.get(entry.getKey())));
		//}
		
		for(String key : predicate_count.keySet()) {
			Integer val = predicate_count.get(key);
			//if(val == 1) {
				//logger.info("Predicate covered: "+key+" "+true_distance.get(key)+" / "+false_distance.get(key));
			//}
			if(val == 1 && true_distance.get(key) > 0) {
				logger.debug("Predicate "+key+" was only covered once, missing true");
				if(false_distance.get(key) != 0)
					logger.error("WARGH! This doesn't match!");
				false_distance.put(key, 1.0);
			}
			else if(val == 1 && false_distance.get(key) > 0) {
				logger.debug("Predicate "+key+" was only covered once, missing false");
				if(true_distance.get(key) != 0)
					logger.error("WARGH! This doesn't match!");
				true_distance.put(key, 1.0);
			} else {
				logger.debug("Predicate "+key+" was covered "+val);
			}
		}
		for(Double val : true_distance.values()) {
			logger.debug("True branch distance: "+val);
			fitness += normalize(val);
			num++;
			logger.debug("Fitness after branch distance: "+fitness);
			if(val == 0) {
				num_covered ++;
			}
		}
		for(Double val : false_distance.values()) {
			logger.debug("False branch distance: "+val);
			fitness += normalize(val);
			logger.debug("Fitness after branch distance: "+fitness);
			num++;
			if(val == 0) {
				num_covered ++;
			}
		}
		
		for(String call : call_count.keySet()) {
			logger.debug("  "+call+": "+call_count.get(call));
		}
		if(call_count.size() < total_methods + 1) { // +1 for the call of the test case
			logger.debug("Missing calls: "+(total_methods - call_count.size())+"/"+total_methods);
			fitness += total_methods - call_count.size();
		}
		
		logger.debug("Got data for "+num+" branches, covered "+num_covered+" total "+(total_branches*2)+", covered "+call_count.size()+" methods out of "+total_methods);
		
		// How many branches are there in total?
		fitness += 2 * total_branches - num;
		if(num_covered > covered_branches) {
			logger.info("Best individual covers "+covered_branches+"/"+(total_branches*2)+" branches");
			covered_branches = Math.max(covered_branches, num_covered);
		}
//		covered_methods  = Math.max(covered_methods,  call_count.size());
		
		logger.debug("Fitness: "+fitness+", size: "+suite.size()+", length: "+suite.length());
		updateIndividual(individual, fitness);

		long end = System.currentTimeMillis();
		if(end-start > 1000) {
			logger.info("Executing tests took    : "+(eend-estart)+"ms");
			logger.info("Calculating fitness took: "+(end-start)+"ms");
		}
		suite.coverage = num_covered;
		for(String e : CFGMethodAdapter.branchless_methods) {
			if(call_count.keySet().contains(e))
				suite.coverage += 1.0;
			
		}
		
		suite.coverage = suite.coverage / total_goals;
		
		return fitness;
	}
	*/

	/**
	 * Execute all tests and count covered branches
	 */
	public double getFitness(Chromosome individual) {
		logger.trace("Calculating branch fitness");

		if(total_branches != CFGMethodAdapter.branch_counter)
			logger.warn("AHAAAAA");

		long start = System.currentTimeMillis();
		
		TestSuiteChromosome suite = (TestSuiteChromosome)individual;
		long estart = System.currentTimeMillis();
		List<ExecutionResult> results = runTestSuite(suite);
		long eend = System.currentTimeMillis();
		double fitness = 0.0;
		Map<String, Double> true_distance = new HashMap<String, Double>();
		Map<String, Double> false_distance = new HashMap<String, Double>();
		Map<String, Integer> predicate_count = new HashMap<String, Integer>();
		Map<String, Integer> call_count = new HashMap<String, Integer>();

		for(ExecutionResult result : results) {
			 if(hasTimeout(result)) {
				updateIndividual(individual, total_branches*2 + total_methods);
				suite.coverage = 0.0;
				logger.info("Test case has timed out, setting fitness to max value "+(total_branches*2 + total_methods));
				return total_branches*2 + total_methods;
			}
			
			for(Entry<String, Integer> entry : result.trace.covered_methods.entrySet()) {
				if(!call_count.containsKey(entry.getKey()))
					call_count.put(entry.getKey(), entry.getValue());
				else {
					call_count.put(entry.getKey(), call_count.get(entry.getKey()) + entry.getValue());
				}
			}
			for(Entry<String, Integer> entry : result.trace.covered_predicates.entrySet()) {
				if(!predicate_count.containsKey(entry.getKey()))
					predicate_count.put(entry.getKey(), entry.getValue());
				else {
					predicate_count.put(entry.getKey(), predicate_count.get(entry.getKey()) + entry.getValue());
				}
			}
			for(Entry<String, Double> entry : result.trace.true_distances.entrySet()) {
				if(!true_distance.containsKey(entry.getKey()))
					true_distance.put(entry.getKey(), entry.getValue());
				else {
					true_distance.put(entry.getKey(), Math.min(true_distance.get(entry.getKey()), entry.getValue()));
				}
			}
			for(Entry<String, Double> entry : result.trace.false_distances.entrySet()) {
				if(!false_distance.containsKey(entry.getKey()))
					false_distance.put(entry.getKey(), entry.getValue());
				else {
					false_distance.put(entry.getKey(), Math.min(false_distance.get(entry.getKey()), entry.getValue()));
				}
			}
		}
		
		int num_covered = 0;
		int uncovered = 0;
		
		//logger.info("Got data for predicates: " + predicate_count.size()+"/"+total_branches);
		for(String key : predicate_count.keySet()) {
			//logger.info("Key: "+key);
			int num_executed  = predicate_count.get(key);
			double df = true_distance.get(key);
			double dt = false_distance.get(key);
			if(df < 0.0)
				logger.warn("DF is less than zero!");
			if(dt < 0.0)
				logger.warn("DT is less than zero!");
			if(num_executed == 1) {
				if(df != 0.0 && dt != 0.0)
					logger.warn("WAAAAARGH!");
				fitness += 1.0; // + normalize(df) + normalize(dt);
			} else {
				fitness += normalize(df) + normalize(dt);
			}
			if(df == 0.0) 
				num_covered ++;
			else
				uncovered++;
			if(dt == 0.0) 
				num_covered ++;
			else
				uncovered++;
		}
		//logger.info("Fitness after branch distances: "+fitness);
		//for(String call : call_count.keySet()) {
		//	logger.info("  "+call+": "+call_count.get(call));
		//}
		/*
		if(call_count.size() < total_methods) { // +1 for the call of the test case
			//logger.info("Missing calls: "+(total_methods - call_count.size())+"/"+total_methods);
			fitness += total_methods - call_count.size();
		}
		*/
//		logger.info("Method calls : "+(total_methods - call_count.size())+"/"+total_methods+" ("+CFGMethodAdapter.methods.size()+")");
		int missing_methods = 0;
		for(String e : CFGMethodAdapter.methods) {
			if(!call_count.containsKey(e)) {
				//logger.info("Missing method: "+e);
				fitness += 1.0;
				missing_methods += 1;
			}
		}

		
		//logger.info("Fitness after missing methods: "+fitness);
		
		
		// How many branches are there in total?
		//fitness += 2 * total_branches - num;
		fitness += 2*(total_branches - predicate_count.size());
		//logger.info("Missing branches: "+(2*(total_branches - predicate_count.size()))+"/"+(2*total_branches));
		//logger.info("Missing methods: "+missing_methods+"/"+total_methods);
		//logger.info("Uncovered branches: "+uncovered);
		//logger.info("Fitness after missing branches: "+fitness);
		//logger.info("Got data for "+predicate_count.size()+" predicates, covered "+num_covered+" total "+(total_branches*2)+", covered "+call_count.size()+" methods out of "+total_methods);

		if(num_covered > covered_branches) {
			covered_branches = Math.max(covered_branches, num_covered);
			logger.info("(Branches) Best individual covers "+covered_branches+"/"+(total_branches*2)+" branches and "+(total_methods - missing_methods)+"/"+total_methods+" methods");
			logger.info("Fitness: "+fitness+", size: "+suite.size()+", length: "+suite.length());
		}
//		if(call_count.size() > covered_methods) {
		if((total_methods - missing_methods) > covered_methods) {
			logger.info("(Methods) Best individual covers "+covered_branches+"/"+(total_branches*2)+" branches and "+(total_methods - missing_methods)+"/"+total_methods+" methods");
			covered_methods = (total_methods - missing_methods);
			logger.info("Fitness: "+fitness+", size: "+suite.size()+", length: "+suite.length());
			
		}
		if(fitness < best_fitness) {
			logger.info("(Fitness) Best individual covers "+covered_branches+"/"+(total_branches*2)+" branches and "+(total_methods - missing_methods)+"/"+total_methods+" methods");
			best_fitness = fitness;
			logger.info("Fitness: "+fitness+", size: "+suite.size()+", length: "+suite.length());
			
		}
		
//		covered_methods  = Math.max(covered_methods,  call_count.size());
		
		//logger.info("Fitness: "+fitness+", size: "+suite.size()+", length: "+suite.length());
		updateIndividual(individual, fitness);

		long end = System.currentTimeMillis();
		if(end-start > 1000) {
			logger.info("Executing tests took    : "+(eend-estart)+"ms");
			logger.info("Calculating fitness took: "+(end-start)+"ms");
		}
		suite.coverage = num_covered;
		for(String e : CFGMethodAdapter.branchless_methods) {
			if(call_count.keySet().contains(e))
				suite.coverage += 1.0;
			
		}
		
		suite.coverage = suite.coverage / total_goals;
				
		return fitness;
	}
	
	public double getCheckFitness(Chromosome individual) {
		int num_changed = 0;
		int num_tests = individual.size();
		
		TestSuiteChromosome orig = (TestSuiteChromosome)individual;
		TestSuiteChromosome clone = (TestSuiteChromosome)((TestSuiteChromosome)individual).clone();
		
		for(TestChromosome test : ((TestSuiteChromosome)individual).tests) {
			if(test.isChanged())
				num_changed++;
		}
		int num_unchanged = num_tests - num_changed;
		double fitness1 = getFitness(individual);
		
		for(TestChromosome test : clone.tests) {
			test.setChanged(true);			
		}
		double fitness2 = getFitness(clone);

		if(fitness1 != fitness2) {
			logger.info("Fitness mismatch: "+fitness1+"/"+fitness2);
			logger.info("Reused "+num_unchanged+"/"+num_tests+" results");
			logger.info("Executed "+num_changed+"/"+num_tests+" results");			

			
			for(int i = 0; i<individual.size(); i++) {
				logger.info("Test "+i);
				logger.info("Orig:");
				logger.info(orig.tests.get(i).last_result);
				logger.info("\nCopy:");
				logger.info(clone.tests.get(i).last_result);
				logger.info(orig.tests.get(i).test.toCode());
			}
			
			
//			for(TestChromosome test : ((TestSuiteChromosome)individual).tests) {
//				logger.info(test.test.toCode());
//			}

		}
		
		return fitness1;
	}

	
	// Execute all (changed) test cases and remember all branch distances
	// Fitness = sum of normalized branch distances
	
}
