package de.unisb.cs.st.evosuite.mutation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.mutation.HOM.HOMObserver;
import de.unisb.cs.st.evosuite.mutation.HOM.HOMSwitcher;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.MethodDistanceGraph;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.javalanche.coverage.distance.MethodDescription;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

public class TestImpactFitness extends TestFitnessFunction {

	double factor_prepared   = Double.parseDouble(System.getProperty("GA.factor_prepared"));
	double factor_activated  = Double.parseDouble(System.getProperty("GA.factor_activated"));
	double factor_methodcall = Double.parseDouble(System.getProperty("GA.factor_executed"));
	double factor_impact     = Double.parseDouble(System.getProperty("GA.factor_impact"));
	double factor_length     = Double.parseDouble(System.getProperty("GA.factor_length"));

	protected HOMSwitcher hom_switcher = new HOMSwitcher();
	

	protected Mutation current_mutation;
	boolean killed;
	double diameter;
	private List<AccessibleObject> mutant_calls = null;
	private MethodDistanceGraph distance_graph = MethodDistanceGraph.getMethodDistanceGraph();
	private MethodDescription mutant_md;

	public TestImpactFitness(Mutation m) {
		setMutation(m);	
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
	public ExecutionResult runTest(TestCase test, Mutation mutant) {
		
		ExecutionResult result = new ExecutionResult(test, mutant);
		
		try {
	        logger.debug("Executing test");
			HOMObserver.resetTouched(); // TODO - is this the right place?
			if(mutant != null) {
				hom_switcher.switchOn(mutant);
				executor.setLogging(false);
			}
			result.exceptions = executor.runWithTrace(test);
			executor.setLogging(true);
			hom_switcher.switchOff(mutant);
			result.trace = ExecutionTracer.getExecutionTracer().getTrace();
			result.output_trace = executor.getTrace();
			result.comparison_trace = comparison_observer.getTrace();
			result.primitive_trace = primitive_observer.getTrace();
			result.inspector_trace = inspector_observer.getTrace();
			result.field_trace = field_observer.getTrace();
			result.null_trace = null_observer.getTrace();
			
			int num = test.size();
			/*
			if(ex != null) {
				result.exception = ex;
				result.exception_statement = test.exception_statement;
				num = test.size() - test.exception_statement;

				if(ex instanceof TestCaseExecutor.TimeoutExceeded) {
					if(mutant != null)
						logger.info("Mutant timed out!");
					else
						logger.info("Program timed out!");
					resetObservers();
					
				}
			}
			*/
			MaxStatementsStoppingCondition.statementsExecuted(num);

			if(mutant != null && HOMObserver.wasTouched(mutant.getId())) {
				result.touched = true;
			}
			//for(TestObserver observer : observers) {
			//	observer.testResult(result);
			//}
		} catch(Exception e) {
			System.out.println("TG: Exception caught: "+e);
			e.printStackTrace();
			System.exit(1);
		}

		//System.out.println("TG: Killed "+result.getNumKilled()+" out of "+mutants.size());
		return result;
	}
	
	/**
	 * Penalty for exceptions raised
	 * @param fitness
	 * @param individual
	 * @param result
	 * @return
	 */
	public double penalizeException(double fitness, ExecutionResult result) {
		if(result.exception != null) {
			logger.debug("Penalizing fitness for exception at "+result.exception_statement+"/"+result.test.size());
			return fitness *  (1.0 * result.exception_statement) / result.test.size();
		}
		else
			return fitness;
	}
	
	public double getMethodDistance(ExecutionResult orig_result) {
		return factor_prepared * 1.0 / getMethodDistance(orig_result.test);
	}
	
	// TODO: We need to record which mutations _would_ have been executed, not only which _have_ been executed
	public double getMutationDistance(ExecutionResult orig_result) {
		double fitness = factor_methodcall;
		// TODO: Add distance for branch, use CDG, add sufficiency condition

		double distance = ExecutionTracer.getExecutionTracer().getApproachLevel(current_mutation, orig_result.trace);
		if(distance <= diameter) {
			logger.debug("Distance: "+distance+" (Diameter: "+diameter+")");
			fitness += factor_activated * (diameter - distance)/diameter;
		} else {
			logger.error("Distance higher than diameter: "+distance+"/"+diameter);
			logger.error(orig_result.test.toCode());
		}
		return fitness;
	}
	
	public double getMutationImpact(ExecutionResult orig_result, ExecutionResult mutant_result) {
		double length = factor_length * orig_result.test.size();

//		return factor_activated + factor_methodcall + factor_impact * (getSumDistance(orig_result.trace, mutant_result.trace) + 
//								getNumAssertions(orig_result, mutant_result)) / (1.0 + length);

		int num_assertions = getNumAssertions(orig_result, mutant_result);
		logger.debug("Number of assertions: "+num_assertions);
		double sum_impact = getSumDistance(orig_result.trace, mutant_result.trace);
		logger.debug("Sum of impact: "+sum_impact);

		double fitness = factor_activated + factor_methodcall; 
				
		if(num_assertions == 0) {
			// TODO :Length only when assertions are reached?
			return fitness + penalizeException(sum_impact/(sum_impact + 1)/(1 + length), mutant_result);
		} else {
			return fitness + 1 + penalizeException(1.0*num_assertions/(num_assertions + 1.0)/(1 + length), mutant_result);
		}
		
		
//		return factor_activated + factor_methodcall + factor_impact * (getSumDistance(orig_result.trace, mutant_result.trace) + 
//				getNumAssertions(orig_result, mutant_result)) / (1.0 + length);
//				hasAssertions(orig_result, mutant_result)) / (1.0 + length);
	}

	
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		// If the mutated method was not executed, the mutation was not executed either
		
		double fitness = 0.0;

		//individual.setMutantExecuted(false);
		if(!result.trace.isMethodExecuted(current_mutation)) {
			logger.debug("Mutant method is not executed");
			fitness = getMethodDistance(result);
			//individual.setMethodExecuted(false);
			
		} else {
			logger.debug("Mutant method has been executed");
			//individual.setMethodExecuted(true);
			
			ExecutionResult mutant_result = runTest(individual.test, current_mutation);
			if(mutant_result.touched) {
				// If mutation was executed measure impact
				logger.debug("Mutation has been executed");
				//individual.setMutantExecuted(true);
				fitness += getMutationImpact(result, mutant_result);
			} else {
				// If mutation was not executed, measure distance
				logger.debug("Mutation has not been executed");
				fitness += getMutationDistance(result);
				
			}
			
			// If there was an exception we won't get useful oracles
			//fitness = penalizeException(fitness, mutant_result);
			
			// Remember that this run lead to an exception
			if(mutant_result.exception != null) {
				//individual.setException(true);
				logger.debug("Mutant raises exception");
			}
			//else
				//individual.setException(false);
		}
		
		fitness = penalizeException(fitness, result);
		logger.debug("Calculated fitness: "+fitness);
		return fitness;
	}

	public void setMutation(Mutation mutation) {
		killed = false;
		current_mutation = mutation;
		diameter = ExecutionTracer.getExecutionTracer().getDiameter(mutation) + 1; // TODO?
		logger.debug("Diameter of mutated method: "+diameter);
		
		TestCluster test_cluster = TestCluster.getInstance();
		this.mutant_calls = test_cluster.getRelatedTestCalls(mutation);
		
		if(mutant_calls.isEmpty()) {
			logger.error("Could not find any related mutant calls for mutation in "+mutation.getClassName()+"."+mutation.getMethodName()+":"+mutation.getLineNumber());
		}
		
		String m_methodname = mutation.getMethodName();
		if(m_methodname == null) {
			logger.error("Method name is null!");
			m_methodname = "<init>";
		}
		String m_classname  = mutation.getClassName();
		//logger.info("Getting method descriptor "+m_classname+" "+m_methodname);
		mutant_md = distance_graph.getMethodDesc(m_classname, m_methodname);
		logger.debug("Mutant method descriptor: "+mutant_md);
		logger.debug("Classname: \""+m_classname+"\", methodname \""+m_methodname+"\"");
	}
	
	private Set<String> getDifference(Map<String, Map<String, Map<Integer, Integer> > > orig, Map<String, Map<String, Map<Integer, Integer> > > mutant) {
		Map<String, Set<String>> handled = new HashMap<String, Set<String>>();
		Set<String> differ  = new HashSet<String>();
		
		for(Entry<String, Map<String, Map<Integer, Integer> > > entry : orig.entrySet()) {
			if(!handled.containsKey(entry.getKey()))
				handled.put(entry.getKey(), new HashSet<String>());
			
			for(Entry<String, Map<Integer, Integer>> method_entry : entry.getValue().entrySet()) {
				if(!mutant.containsKey(entry.getKey())) {
					// Class was not executed on mutant, so add method
					differ.add(entry.getKey()+":"+method_entry.getKey());
				}
				else {
					// Class was also executed on mutant
					
					if(!mutant.get(entry.getKey()).containsKey(method_entry.getKey())) {
						// Method was not executed on mutant, so add method
						differ.add(entry.getKey()+":"+method_entry.getKey());
					}
					else {
						// Method was executed on mutant
						for(Entry<Integer, Integer> line_entry : method_entry.getValue().entrySet()) {
							if(!mutant.get(entry.getKey()).get(method_entry.getKey()).containsKey(line_entry.getKey())) {
								// Line was not executed on mutant, so add
								differ.add(entry.getKey()+":"+method_entry.getKey());
							} else {
								if(!mutant.get(entry.getKey()).get(method_entry.getKey()).get(line_entry.getKey()).equals(line_entry.getValue())) {
									// Line coverage differs, so add
									differ.add(entry.getKey()+":"+method_entry.getKey());
								}
							}
						}
						if(!method_entry.getValue().equals(mutant.get(entry.getKey()).get(method_entry.getKey()))) {
							differ.add(entry.getKey()+":"+method_entry.getKey());							
							//logger.info("Coverage difference on : "+entry.getKey()+":"+method_entry.getKey());
						}
					}
				}
			}	
		}

		return differ;
	}
	
	/**
	 * Compare two coverage maps
	 * @param orig
	 * @param mutant
	 * @return unique number of methods with coverage difference
	 */
	private int getCoverageDifference(Map<String, Map<String, Map<Integer, Integer> > > orig, Map<String, Map<String, Map<Integer, Integer> > > mutant) {		
		Set<String> differ = getDifference(orig, mutant);
		differ.addAll(getDifference(mutant, orig));
		return differ.size();
	}

	private int getNumAssertions(ExecutionResult orig_result, ExecutionResult mutant_result) {
		int num = 0;
		if(orig_result.test.size() == 0)
			return 0;

		int last_num = 0;
		num += orig_result.output_trace.numDiffer(mutant_result.output_trace);
		if(num > last_num)
			logger.info("Found "+(num - last_num)+" output assertions!");
		last_num = num;
		num += orig_result.comparison_trace.numDiffer(mutant_result.comparison_trace); 
		if(num > last_num)
			logger.info("Found "+(num - last_num)+" comparison assertions!");
		last_num = num;
		num += orig_result.primitive_trace.numDiffer(mutant_result.primitive_trace); 
		if(num > last_num)
			logger.info("Found "+(num - last_num)+" primitive assertions!");
		last_num = num;
		num += orig_result.inspector_trace.numDiffer(mutant_result.inspector_trace); 
		if(num > last_num)
			logger.info("Found "+(num - last_num)+" inspector assertions!");
		last_num = num;
		num += orig_result.field_trace.numDiffer(mutant_result.field_trace);
		if(num > last_num)
			logger.info("Found "+(num - last_num)+" field assertions!");
		last_num = num;
		num += orig_result.null_trace.numDiffer(mutant_result.null_trace);
		if(num > last_num)
			logger.info("Found "+(num - last_num)+" null assertions!");
		
		/*
		if(orig_result.exception != null) {
			if(orig_result.exception.getMessage() != null) {
				if(mutant_result.exception == null || !orig_result.exception.getMessage().equals(mutant_result.exception.getMessage())) {
					num++;
				}
			}
		}
		*/
		
		logger.debug("Found "+num+" assertions!");
		return num;
		
		/*
		if(num > 0) {
			logger.debug("Found "+num+" assertions!");
			return 1;
		} else {
			logger.debug("Found 0 assertions!");
		}
			return 0;
			*/
		
//		return num;
	}
	
	private float hasAssertions(ExecutionResult orig_result, ExecutionResult mutant_result) {

		// TODO: Only count the number of statements that have assertions
		
		float num = 0;
		if(orig_result.test.size() == 0)
			return 0;

		if(orig_result.output_trace.differs(mutant_result.output_trace))
			num += 0.5;
		if(orig_result.comparison_trace.differs(mutant_result.comparison_trace))
			num += 1;
		if(orig_result.primitive_trace.differs(mutant_result.primitive_trace))
			num += 1;
		if(orig_result.inspector_trace.differs(mutant_result.inspector_trace))
			num += 1;
		if(orig_result.field_trace.differs(mutant_result.field_trace))
			num += 1;
		
		if(num > 0) {
			logger.debug("Found "+num+" assertions!");
		}
		return num;
	}

	private double getSumDistance(ExecutionTrace orig_trace, ExecutionTrace mutant_trace) {

//		double sum = getCoverageDifference(getCoverage(orig_trace), getCoverage(mutant_trace)); 
		double coverage_impact = getCoverageDifference(orig_trace.coverage, mutant_trace.coverage); 
		logger.debug("Coverage impact: "+coverage_impact);
		double data_impact     = getCoverageDifference(orig_trace.return_data, mutant_trace.return_data); 
		logger.debug("Data impact: "+data_impact);
		
		return coverage_impact + data_impact;
	}
	
	private double getMethodDistance(TestCase t, Method m) {
		int num_param = m.getParameterTypes().length;
		double distance = 3.0 + num_param;
		int num_satisfied = 0;
		boolean have_callee = false;
		List<Boolean> satisfied = new ArrayList<Boolean>();
		for(@SuppressWarnings("unused") Class<?> c: m.getParameterTypes()) {
			satisfied.add(false);
		}
		
		for(Statement s : t.getStatements()) {
			if(s.getReturnValue() == null)
				continue; // Nop
			
			if(!have_callee) {
				if(s.getReturnValue().isAssignableTo(m.getDeclaringClass())) {
					have_callee = true;
				}					
			}
			for(int i = 0; i<num_param; i++) {
				if(!satisfied.get(i)) {
					if(s.getReturnValue().isAssignableTo(m.getGenericParameterTypes()[i])) {
						satisfied.set(i, true);
						num_satisfied++;
					}
				}
			}
		}
		
		if(have_callee)
			distance -= 2.0;
		for(Boolean b : satisfied) {
			if(b)
				distance -= 1.0;
		}
		
		return distance;
		
	}

	private double getMethodDistance(TestCase t, Constructor<?>  c) {
		int num_param = c.getParameterTypes().length;
		double distance = 1.0 + num_param;
		int num_satisfied = 0;
		List<Boolean> satisfied = new ArrayList<Boolean>();
		for(@SuppressWarnings("unused") Class<?> clazz : c.getParameterTypes()) {
			satisfied.add(false);
		}
		
		for(Statement s : t.getStatements()) {
			for(int i = 0; i<num_param; i++) {
				if(!satisfied.get(i)) {
					if(s.getReturnValue().isAssignableTo(c.getGenericParameterTypes()[i])) {
						satisfied.set(i, true);
						num_satisfied++;
					}
				}
			}
		}
		
		for(Boolean b : satisfied) {
			if(b)
				distance -= 1.0;
		}
		
		return distance;
		
	}

	// TODO: We don't even need to execute if mutant method is not called
	private double getMethodDistance(TestCase t) {
		List<Double> distances = new ArrayList<Double>();
		for(AccessibleObject o : mutant_calls) {
			if(o instanceof Method)
				distances.add(getMethodDistance(t, (Method)o));
			else
				distances.add(getMethodDistance(t, (Constructor<?>)o));
		}
		
		if(distances.isEmpty())
			return Double.POSITIVE_INFINITY;
		
//		Collections.sort(distances);
//		return distances.get(distances.size() - 1);
		return Collections.min(distances);
	}


	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
		individual.setSolution(fitness > (factor_prepared + factor_activated));
	}
}
