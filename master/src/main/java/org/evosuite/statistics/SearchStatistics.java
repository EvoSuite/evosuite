package org.evosuite.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.statistics.backend.CSVStatisticsBackend;
import org.evosuite.statistics.backend.ConsoleStatisticsBackend;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.statistics.backend.HTMLStatisticsBackend;
import org.evosuite.statistics.backend.StatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Listener;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This singleton collects all the data values reported by the client node
 * 
 * @author gordon
 *
 */
public class SearchStatistics implements Listener<ClientStateInformation>{

	private static final long serialVersionUID = -1859683466333302151L;

	/** Singleton instance */
	private static SearchStatistics instance = null;

	private static final Logger logger = LoggerFactory.getLogger(SearchStatistics.class);

	/** Map of client id to best individual received from that client so far */
	private Map<String, TestSuiteChromosome> bestIndividual = new HashMap<String, TestSuiteChromosome>();

	/** Backend used to output the data */
	private StatisticsBackend backend = null;

	/** Output variables and their values */ 
	private Map<String, OutputVariable<?>> outputVariables = new TreeMap<String, OutputVariable<?>>();

	/** Variable factories to extract output variables from chromosomes */
	private Map<String, ChromosomeOutputVariableFactory<?>> variableFactories = new TreeMap<String, ChromosomeOutputVariableFactory<?>>(); 

	/** Variable factories to extract sequence variables */
	private Map<String, SequenceOutputVariableFactory<?>> sequenceOutputVariableFactories = new TreeMap<String, SequenceOutputVariableFactory<?>>();

	/** Keep track of how far EvoSuite progressed */
	private ClientState currentState = ClientState.INITIALIZATION;

	private long currentStateStarted = System.currentTimeMillis();

	private long searchStartTime = 0L;

	private long startTime = System.currentTimeMillis();

	private List<List<TestGenerationResult>> results = new ArrayList<List<TestGenerationResult>>();

	private SearchStatistics() { 
		switch(Properties.STATISTICS_BACKEND) {
		case CONSOLE:
			backend = new ConsoleStatisticsBackend();
			break;
		case CSV:
			backend = new CSVStatisticsBackend();
			break;
		case HTML:
			backend = new HTMLStatisticsBackend();
			break;
		case DEBUG:
			backend = new DebugStatisticsBackend();
			break;
		case NONE:
		default:
			// If no backend is specified, there is no output
			backend = null;
		}
		initFactories();
		setOutputVariable(RuntimeVariable.Random_Seed, Randomness.getSeed());
		sequenceOutputVariableFactories.put(RuntimeVariable.CoverageTimeline.name(), new CoverageSequenceOutputVariableFactory());
		sequenceOutputVariableFactories.put(RuntimeVariable.FitnessTimeline.name(), new FitnessSequenceOutputVariableFactory());
		sequenceOutputVariableFactories.put(RuntimeVariable.SizeTimeline.name(), new SizeSequenceOutputVariableFactory());
		sequenceOutputVariableFactories.put(RuntimeVariable.LengthTimeline.name(), new LengthSequenceOutputVariableFactory());
		// sequenceOutputVariableFactories.put("Generation_History", new GenerationSequenceOutputVariableFactory());
		if(MasterServices.getInstance().getMasterNode() != null)
			MasterServices.getInstance().getMasterNode().addListener(this);
	}

	public static SearchStatistics getInstance() {
		if(instance == null)
			instance = new SearchStatistics();

		return instance;
	}

	public static void clearInstance() {
		instance = null;
	}

	/**
	 * This method is called when a new individual is sent from a client.
	 * The individual represents the best individual of the current generation.
	 * 
	 * @param rmiClientIdentifier
	 * @param individual
	 */
	public void currentIndividual(String rmiClientIdentifier, Chromosome individual) {
		if(backend == null)
			return;

		logger.debug("Received individual");
		bestIndividual.put(rmiClientIdentifier, (TestSuiteChromosome) individual);
		for(SequenceOutputVariableFactory<?> v : sequenceOutputVariableFactories.values()) {
			v.update((TestSuiteChromosome) individual);
		}
		for(ChromosomeOutputVariableFactory<?> v : variableFactories.values()) {
			setOutputVariable(v.getVariable((TestSuiteChromosome) individual));
		}
	}

	/**
	 * Set an output variable to a value directly 
	 * 
	 * @param name
	 * @param value
	 */
	public void setOutputVariable(RuntimeVariable variable, Object value) {
		setOutputVariable(new OutputVariable<Object>(variable.toString(), value));
	}

	public void setOutputVariable(OutputVariable<?> variable) {
		outputVariables.put(variable.getName(), variable);
	}

	public void addTestGenerationResult(List<TestGenerationResult> result) {
	    results.add(result);
	}

	public List<List<TestGenerationResult>> getTestGenerationResults() {
		return results;
	}

	/**
	 * Retrieve list of possible variables
	 *  
	 * @return
	 */
	private List<String> getAllOutputVariableNames() {
		List<String> variableNames = new ArrayList<String>();

		String[] essentials = new String[] {  //TODO maybe add some more
				"TARGET_CLASS" , "criterion", 
				RuntimeVariable.Coverage.toString(),
				RuntimeVariable.BranchCoverage.toString(),
				RuntimeVariable.Total_Goals.toString(),
				RuntimeVariable.Covered_Goals.toString()
				};
		variableNames.addAll(Arrays.asList(essentials));
		
		/* cannot use what we received, as due to possible bugs/errors those might not be constant
		variableNames.addAll(outputVariables.keySet());
		variableNames.addAll(variableFactories.keySet());
		variableNames.addAll(sequenceOutputVariableFactories.keySet());
		*/
		return variableNames;
	}

	/**
	 * Retrieve list of output variables that the user will get to see.
	 * If output_variables is not set, then all variables will be returned
	 * 
	 * @return
	 */
	private Collection<String> getOutputVariableNames() {
		List<String> variableNames = new ArrayList<String>();
		if(Properties.OUTPUT_VARIABLES == null) {
			variableNames.addAll(getAllOutputVariableNames());
		} else {
			variableNames.addAll(Arrays.asList(Properties.OUTPUT_VARIABLES.split(",")));
		}
		return variableNames;
	}

	/**
	 * Extract output variables from input <code>individual</code>.
	 * Add also all the other needed search-level variables. 
	 * 
	 * @param individual
	 * @return <code>null</code> if some data is missing
	 */
	private Map<String, OutputVariable<?>> getOutputVariables(TestSuiteChromosome individual) {
		Map<String, OutputVariable<?>> variables = new LinkedHashMap<String, OutputVariable<?>>();
		
		for(String variableName : getOutputVariableNames()) {
			if(outputVariables.containsKey(variableName)) {
				//values directly sent by the client
				variables.put(variableName, outputVariables.get(variableName));
			} else if(Properties.getParameters().contains(variableName)) {
				// values used to define the search, ie the -D given as input to EvoSuite
				variables.put(variableName, new PropertyOutputVariableFactory(variableName).getVariable());
			} else if(variableFactories.containsKey(variableName)) {
				//values extracted from the individual
				variables.put(variableName, variableFactories.get(variableName).getVariable(individual));
			} else if(sequenceOutputVariableFactories.containsKey(variableName)) {
				/*
				 * time related values, which will be expanded in a list of values
				 * through time
				 */
				for(OutputVariable<?> var : sequenceOutputVariableFactories.get(variableName).getOutputVariables()) {
					variables.put(var.getName(), var); 
				}
			}
			else {
				logger.error("No obtained value for output variable: "+variableName);
				return null;
			}
		}

		return variables;
	}

	/**
	 * Write result to disk using selected backend
	 * 
	 * @return true if the writing was successful
	 */
	public boolean writeStatistics() {
		logger.info("Writing statistics");
		if(backend == null)
			return false;

		outputVariables.put(RuntimeVariable.Total_Time.name(), new OutputVariable<Object>(RuntimeVariable.Total_Time.name(), System.currentTimeMillis() - startTime));

		if(bestIndividual.isEmpty()) {
			logger.error("No statistics has been saved because EvoSuite failed to generate any test case");
			return false;
		}	

		TestSuiteChromosome individual = bestIndividual.values().iterator().next();
		Map<String,OutputVariable<?>> map = getOutputVariables(individual);
		if(map==null){
			logger.error("Not going to write down statistics data, as some are missing");
			return false;
		} 			

		boolean valid = RuntimeVariable.validateRuntimeVariables(map);
		if(!valid){
			logger.error("Not going to write down statistics data, as some data is invalid");
			return false;
		} else {
			backend.writeData(individual, map);
			return true;
		}
	}
	
	/**
	 * Write result to disk using selected backend
	 * 
	 * @return true if the writing was successful
	 */
	public boolean writeStatisticsForAnalysis() {
		logger.info("Writing statistics");
		if(backend == null) {
			LoggingUtils.getEvoLogger().info("Backend is null");
			return false;
		}

		outputVariables.put(RuntimeVariable.Total_Time.name(), new OutputVariable<Object>(RuntimeVariable.Total_Time.name(), System.currentTimeMillis() - startTime));

		TestSuiteChromosome individual = new TestSuiteChromosome();
		Map<String,OutputVariable<?>> map = getOutputVariables(individual);
		if(map==null){
			logger.error("Not going to write down statistics data, as some are missing");
			return false;
		} 			

		boolean valid = RuntimeVariable.validateRuntimeVariables(map);
		if(!valid){
			logger.error("Not going to write down statistics data, as some data is invalid");
			return false;
		} else {
			backend.writeData(individual, map);
			return true;
		}
	}

	/**
	 * Process status update event received from client
	 */
	@Override
	public void receiveEvent(ClientStateInformation information) {
		if(information.getState() != currentState) {
			logger.info("Received status update: "+information);
			if(information.getState() == ClientState.SEARCH) {
				searchStartTime = System.currentTimeMillis();
				for(SequenceOutputVariableFactory<?> factory : sequenceOutputVariableFactories.values()) {
					factory.setStartTime(searchStartTime);
				}
			}
			OutputVariable<Long> time = new OutputVariable<Long>("Time_"+currentState.getName(), System.currentTimeMillis() - currentStateStarted);
			outputVariables.put(time.getName(), time);
			currentState = information.getState();
			currentStateStarted = System.currentTimeMillis();
		}

	}

	/**
	 * Create default factories
	 */
	private void initFactories() {
		variableFactories.put(RuntimeVariable.Length.name(), new ChromosomeLengthOutputVariableFactory());
		variableFactories.put(RuntimeVariable.Size.name(), new ChromosomeSizeOutputVariableFactory());
		variableFactories.put(RuntimeVariable.Coverage.name(), new ChromosomeCoverageOutputVariableFactory());
		variableFactories.put(RuntimeVariable.Fitness.name(), new ChromosomeFitnessOutputVariableFactory());
	}

	/**
	 * Total length of a test suite
	 */
	private static class ChromosomeLengthOutputVariableFactory extends ChromosomeOutputVariableFactory<Integer> {
		public ChromosomeLengthOutputVariableFactory() {
			super(RuntimeVariable.Length);
		}

		@Override
		protected Integer getData(TestSuiteChromosome individual) {
			return individual.totalLengthOfTestCases();
		}
	}

	/**
	 * Number of tests in a test suite
	 */
	private static class ChromosomeSizeOutputVariableFactory extends ChromosomeOutputVariableFactory<Integer> {
		public ChromosomeSizeOutputVariableFactory() {
			super(RuntimeVariable.Size);
		}

		@Override
		protected Integer getData(TestSuiteChromosome individual) {
			return individual.size();
		}
	}

	/**
	 * Fitness value of a test suite
	 */
	private static class ChromosomeFitnessOutputVariableFactory extends ChromosomeOutputVariableFactory<Double> {
		public ChromosomeFitnessOutputVariableFactory() {
			super(RuntimeVariable.Fitness);
		}

		@Override
		protected Double getData(TestSuiteChromosome individual) {
			return individual.getFitness();
		}
	}

	/**
	 * Coverage value of a test suite
	 */
	private static class ChromosomeCoverageOutputVariableFactory extends ChromosomeOutputVariableFactory<Double> {
		public ChromosomeCoverageOutputVariableFactory() {
			super(RuntimeVariable.Coverage);
		}

		@Override
		protected Double getData(TestSuiteChromosome individual) {
			return individual.getCoverage();
		}
	}

	/**
	 * Sequence variable for fitness values
	 */
	private static class FitnessSequenceOutputVariableFactory extends SequenceOutputVariableFactory<Double> {

		public FitnessSequenceOutputVariableFactory() {
			super(RuntimeVariable.FitnessTimeline);
		}

		@Override
		protected Double getValue(TestSuiteChromosome individual) {
			return individual.getFitness();
		}
	}

	/**
	 * Sequence variable for coverage values
	 */
	private static class CoverageSequenceOutputVariableFactory extends SequenceOutputVariableFactory<Double> {

		public CoverageSequenceOutputVariableFactory() {
			super(RuntimeVariable.CoverageTimeline);
		}

		@Override
		public Double getValue(TestSuiteChromosome individual) {
			return individual.getCoverage();
		}
	}

	/**
	 * Sequence variable for number of tests
	 */
	private static class SizeSequenceOutputVariableFactory extends SequenceOutputVariableFactory<Integer> {

		public SizeSequenceOutputVariableFactory() {
			super(RuntimeVariable.SizeTimeline);
		}

		@Override
		public Integer getValue(TestSuiteChromosome individual) {
			return individual.size();
		}
	}

	/**
	 * Sequence variable for total length of tests
	 */
	private static class LengthSequenceOutputVariableFactory extends SequenceOutputVariableFactory<Integer> {

		public LengthSequenceOutputVariableFactory() {
			super(RuntimeVariable.LengthTimeline);
		}

		@Override
		public Integer getValue(TestSuiteChromosome individual) {
			return individual.totalLengthOfTestCases();
		}
	}
}
