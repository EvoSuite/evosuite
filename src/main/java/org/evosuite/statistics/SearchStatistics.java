package org.evosuite.statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Listener;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchStatistics implements Listener<ClientStateInformation>{

	private static final long serialVersionUID = -1859683466333302151L;

	private static SearchStatistics instance = null;
	
	private static Logger logger = LoggerFactory.getLogger(SearchStatistics.class);
	
	private Map<String, TestSuiteChromosome> bestIndividual = new HashMap<String, TestSuiteChromosome>();
	
	private Map<String, ChromosomeOutputVariableFactory<?>> variableFactories = new TreeMap<String, ChromosomeOutputVariableFactory<?>>(); 
	
	private StatisticsBackend backend = null;
	
	private Map<String, OutputVariable<?>> outputVariables = new TreeMap<String, OutputVariable<?>>();

	private Map<String, SequenceOutputVariableFactory<?>> sequenceOutputVariableFactories = new TreeMap<String, SequenceOutputVariableFactory<?>>();

	private ClientState currentState = ClientState.INITIALIZATION;
	
	private long currentStateStarted = System.currentTimeMillis();
	
	private long startTime = 0L;
	
	private SearchStatistics() { 
		switch(Properties.STATISTICS_BACKEND) {
		case CONSOLE:
			backend = new ConsoleStatisticsBackend();
			break;
		case CSV:
			backend = new CSVStatisticsBackend();
			break;
		case NONE:
		default:
			backend = null;
		}
		initFactories();
		outputVariables.put("seed", new OutputVariable<Long>("seed", Randomness.getSeed()));
		sequenceOutputVariableFactories.put("Coverage_History", new CoverageSequenceOutputVariableFactory());
		sequenceOutputVariableFactories.put("Fitness_History", new FitnessSequenceOutputVariableFactory());
		sequenceOutputVariableFactories.put("Size_History", new SizeSequenceOutputVariableFactory());
		sequenceOutputVariableFactories.put("Length_History", new LengthSequenceOutputVariableFactory());
		sequenceOutputVariableFactories.put("Generation_History", new GenerationSequenceOutputVariableFactory());
		MasterServices.getInstance().getMasterNode().addListener(this);
	}
	
	public static SearchStatistics getInstance() {
		if(instance == null)
			instance = new SearchStatistics();
		
		return instance;
	}
	
	public void currentIndividual(String rmiClientIdentifier, Chromosome individual) {
		if(backend == null)
			return;
		
		logger.info("Received individual");
		bestIndividual.put(rmiClientIdentifier, (TestSuiteChromosome) individual);
		for(SequenceOutputVariableFactory<?> v : sequenceOutputVariableFactories.values()) {
			v.update((TestSuiteChromosome) individual);
		}
	}
	
	public void setOutputVariable(String name, Object value) {
		// TODO: If there already exists that key and the value is different, issue warning?
		outputVariables.put(name, new OutputVariable<Object>(name, value));
	}
	
	private List<String> getAllOutputVariableNames() {
		String[] essentials = new String[] { "TARGET_CLASS" };
		List<String> variableNames = new ArrayList<String>();
		variableNames.addAll(Arrays.asList(essentials));
		variableNames.addAll(outputVariables.keySet());
		variableNames.addAll(variableFactories.keySet());
		variableNames.addAll(sequenceOutputVariableFactories.keySet());
		return variableNames;
	}
	
	private Collection<String> getOutputVariableNames() {
		List<String> variableNames = new ArrayList<String>();
		if(Properties.OUTPUT_VARIABLES == null) {
			variableNames.addAll(getAllOutputVariableNames());
		} else {
			variableNames.addAll(Arrays.asList(Properties.OUTPUT_VARIABLES.split(",")));
		}
		return variableNames;
	}
	
	
	private List<OutputVariable<?>> getOutputVariables(Chromosome individual) {
		List<OutputVariable<?>> variables = new ArrayList<OutputVariable<?>>();
		
		for(String variableName : getOutputVariableNames()) {
			if(outputVariables.containsKey(variableName)) {
				variables.add(outputVariables.get(variableName));
			} else if(Properties.getParameters().contains(variableName)) {
				variables.add(new PropertyOutputVariableFactory(variableName).getVariable());
			} else if(variableFactories.containsKey(variableName)) {
				// TODO: Iterator mess
				variables.add(variableFactories.get(variableName).getVariable((TestSuiteChromosome) bestIndividual.values().iterator().next()));
			} else if(sequenceOutputVariableFactories.containsKey(variableName)) {
				variables.addAll(sequenceOutputVariableFactories.get(variableName).getOutputVariables());
			}
			else {
				throw new IllegalArgumentException("No such output variable: "+variableName);
			}
		}
		
		return variables;
	}
	
	public void writeStatistics() {
		logger.info("Writing statistics");
		if(backend == null)
			return;
		
		File outputDir = new File(Properties.REPORT_DIR);
		outputDir.mkdirs();
		
		if(!bestIndividual.isEmpty()) {
			Chromosome individual = bestIndividual.values().iterator().next();
			backend.writeData(getOutputVariables(individual));
		}
	}

	@Override
	public void receiveEvent(ClientStateInformation information) {
		if(information.getState() != currentState) {
			logger.info("Received status update: "+information);
			if(information.getState() == ClientState.SEARCH) {
				startTime = System.currentTimeMillis();
				for(SequenceOutputVariableFactory<?> factory : sequenceOutputVariableFactories.values()) {
					factory.setStartTime(startTime);
				}
			}
			OutputVariable<Long> time = new OutputVariable<Long>("time_"+currentState.getName(), System.currentTimeMillis() - currentStateStarted);
			outputVariables.put(time.getName(), time);
			currentState = information.getState();
			currentStateStarted = System.currentTimeMillis();
		}

	}
	
	private void initFactories() {
		variableFactories.put("best_length", new ChromosomeLengthOutputVariableFactory());
		variableFactories.put("best_size", new ChromosomeSizeOutputVariableFactory());
		variableFactories.put("best_coverage", new ChromosomeCoverageOutputVariableFactory());
		variableFactories.put("best_fitness", new ChromosomeFitnessOutputVariableFactory());
	}
	
	private static class ChromosomeLengthOutputVariableFactory extends ChromosomeOutputVariableFactory<Integer> {
		public ChromosomeLengthOutputVariableFactory() {
			super("Length");
		}

		@Override
		protected Integer getData(TestSuiteChromosome individual) {
			return individual.totalLengthOfTestCases();
		}
	}
	
	private static class ChromosomeSizeOutputVariableFactory extends ChromosomeOutputVariableFactory<Integer> {
		public ChromosomeSizeOutputVariableFactory() {
			super("Size");
		}

		@Override
		protected Integer getData(TestSuiteChromosome individual) {
			return individual.size();
		}
	}

	private static class ChromosomeFitnessOutputVariableFactory extends ChromosomeOutputVariableFactory<Double> {
		public ChromosomeFitnessOutputVariableFactory() {
			super("Fitness");
		}

		@Override
		protected Double getData(TestSuiteChromosome individual) {
			return individual.getFitness();
		}
	}

	private static class ChromosomeCoverageOutputVariableFactory extends ChromosomeOutputVariableFactory<Double> {
		public ChromosomeCoverageOutputVariableFactory() {
			super("Coverage");
		}

		@Override
		protected Double getData(TestSuiteChromosome individual) {
			return individual.getCoverage();
		}
	}
	
	private static class FitnessSequenceOutputVariableFactory extends SequenceOutputVariableFactory<Double> {

		public FitnessSequenceOutputVariableFactory() {
			super("Fitness_History");
		}
		
		@Override
		protected Double getValue(TestSuiteChromosome individual) {
			return individual.getFitness();
		}
	}

	private static class CoverageSequenceOutputVariableFactory extends SequenceOutputVariableFactory<Double> {

		public CoverageSequenceOutputVariableFactory() {
			super("Coverage_History");
		}
		
		@Override
		public Double getValue(TestSuiteChromosome individual) {
			return individual.getCoverage();
		}
	}

	private static class SizeSequenceOutputVariableFactory extends SequenceOutputVariableFactory<Integer> {

		public SizeSequenceOutputVariableFactory() {
			super("Size_History");
		}
		
		@Override
		public Integer getValue(TestSuiteChromosome individual) {
			return individual.size();
		}
	}

	private static class LengthSequenceOutputVariableFactory extends SequenceOutputVariableFactory<Integer> {

		public LengthSequenceOutputVariableFactory() {
			super("Length_History");
		}
		
		@Override
		public Integer getValue(TestSuiteChromosome individual) {
			return individual.totalLengthOfTestCases();
		}
	}
	
	private static class GenerationSequenceOutputVariableFactory extends SequenceOutputVariableFactory<Integer> {

		public GenerationSequenceOutputVariableFactory() {
			super("Generation_History");
		}
		
		@Override
		public Integer getValue(TestSuiteChromosome individual) {
			return individual.getAge();
		}
	}

}
