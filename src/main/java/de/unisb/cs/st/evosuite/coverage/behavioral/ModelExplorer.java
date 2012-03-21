package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisaarland.cs.st.adabu.trans.model.TransitiveObjectModel;
import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.StandardGA;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import de.unisb.cs.st.evosuite.junit.TestSuiteWriter;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseMinimizer;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/** Class that provides the methods to generate the object behavior model. */
public class ModelExplorer {
	
	/** The default name of the exploration test. */
	public static final String TEST_NAME = "TmpExploreTest";
	
	/** The class to mine the explored object model for. */
	private Class<?> cut;
	
	/** The handler holding the edge data for the model. */
	private MethodCallHandler handler = new MethodCallHandler();
	
	/** The class object executing <tt>ADABU</tt>. */
	private AdabuRunner adabuRunner = new AdabuRunner();
	
	/**
	 * Creates a new <tt>ModelExplorer</tt> with given
	 * class under test.</p>
	 * 
	 * @param cut - the class to mine the explored object model for.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given class under test is <tt>null</tt>.
	 */
	public ModelExplorer(Class<?> cut) {
		if (cut == null)
			throw new IllegalArgumentException("The given class under test is null!");
		
		this.cut = cut;
	}
	
	/**
	 * Generates an object behavior model for the class under test
	 * given by {@link ModelExplorer#cut}.</br>
	 * The object behavior model is returned in a simple
	 * graph representation.</p>
	 * 
	 * @return the graph representation of the explored model or
	 *         <tt>null</tt> if the object model could not be mined.        
	 * 
	 * @see BehavioralCoverage
	 */
	public BCGraph generateExploredModel() {
		List<TestCase> exploreTests;
		TransitiveObjectModel objectModel;
		TransitiveObjectModel abstractModel;
		BCGraph graph;
		
		// generate the initializing tests containing the constructor calls
		exploreTests = createInitialModelTest();
		if (exploreTests.isEmpty()) // abort if no test was created
			return null;
		
		System.out.println("* Generating the initial model test successful: " + exploreTests); // for debugging TODO delete
		
		// write the test into the temporary file
		writeTmpJUnitTests(exploreTests);
		
		// mine the object model using ADABU
		objectModel = adabuRunner.mineObjectModel(null);
		if (objectModel == null) // not able to mine the model
			return null;
		
		System.out.println("* Generating the object model successful: " + objectModel); // for debugging TODO delete
		
		// create the graph to be covered
		graph = new BCGraph(objectModel, handler);
		
		System.out.println("* Generating the graph was successful: " + graph); // for debugging TODO delete
		
		// generate the corresponding abstract model using ADABU
		abstractModel = adabuRunner.getAbstractModel(objectModel);
		
		System.out.println("* Generating the abstract model was successful: " + abstractModel); // for debugging TODO delete
		
		// check if there are declared methods to expand the graph
		if (cut.getDeclaredMethods().length != 0) {
			// expand the graph until no more abstract states are created
			int abstractStates = 0; int i = 0; // TODO delete integer i
			System.out.println("* Starting to explore the model - #States: " + abstractModel.getModel().getNumberOfNodes()); // for debugging TODO delete
			while (abstractStates < abstractModel.getModel().getNumberOfNodes() && i < 2) {i++;
				// update the abstract states counter
				abstractStates = abstractModel.getModel().getNumberOfNodes();
				
				// generate the expanded test
				exploreTests = createModelTest(graph);
				
				System.out.println("* Generating the expanding model test successful: " + exploreTests); // for debugging TODO delete
				
				// check whether new test cases were created
				if (exploreTests.isEmpty()) break;
				
				// write the test into the temporary file
				writeTmpJUnitTests(exploreTests);
				
				// mine the expanded object model
				objectModel =  adabuRunner.mineObjectModel(objectModel);
				
				System.out.println("* Generating the object model successful: " + objectModel); // for debugging TODO delete
				
				// create the new graph to cover
				graph = new BCGraph(objectModel, handler);
				
				System.out.println("* Generating the graph was successful: " + graph); // for debugging TODO delete
				
				// generate the abstract model using ADABU
				abstractModel = adabuRunner.getAbstractModel(objectModel);
				
				System.out.println("* New abstract model - #States: " + abstractModel.getModel().getNumberOfNodes()); // for debugging TODO delete
			}
		}
		return graph;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Creates the initial test-suite to mine the basic object behavior model
	 * containing all possible constructor calls of the class under test.</p>
	 * 
	 * @return the list of test-cases for initializing the
	 *         object behavior model.
	 */
	private List<TestCase> createInitialModelTest() {
		List<TestCase> result;
		GeneticAlgorithm ga;
		
		// abort if the class is an interface, abstract class, enumeration type, primitive type or array type
		String msg = "* Warning: Abort during model initialization - the class " + cut.getName();
		if (cut.isInterface()) { // check for interface
			System.out.println(msg + " is an interface");
			return new ArrayList<TestCase>(0);
		}
		int mod = cut.getModifiers();
		if (Modifier.isAbstract(mod)) { // check for abstract class
			System.out.println(msg + " is abstract");
			return new ArrayList<TestCase>(0);
		}
		if (cut.isEnum()) { // check for enumeration type
			System.out.println(msg + " is of type enum");
			return new ArrayList<TestCase>(0);
		}
		if (cut.isPrimitive()) { // check for primitive type
			System.out.println(msg + " is a primitive type");
			return new ArrayList<TestCase>(0);
		}
		if (cut.isArray()) { // check for array type
			System.out.println(msg + " is an array type");
			return new ArrayList<TestCase>(0);
		}
		
		// check if there is a public constructor
		if (cut.getConstructors().length == 0) {
			System.out.println(msg + " has no public constructors");
			return new ArrayList<TestCase>(0);
		}
		
		// generate the tests containing all constructor calls
		result = new ArrayList<TestCase>();
		
		// set up the genetic algorithm
		ga = new StandardGA(new RandomLengthTestFactory());
		ga.getSelectionFunction().setMaximize(false); // need to minimize for zero fitness goal
		ga.addStoppingCondition(new ZeroFitnessStoppingCondition());
		
		// set up the coverage goals
		TestFitnessFactory goal_factory = new BehavioralCoverageFactory(cut.getDeclaredConstructors());
		List<TestFitnessFunction> goals = goal_factory.getCoverageGoals();
		
		// for every fitness function (goal) generate a test-case
		for (TestFitnessFunction fitness_function : goals) {
			// reset the genetic algorithm
			ga.resetStoppingConditions();
			ga.clearPopulation();
			ga.setFitnessFunction(fitness_function); // add fitness function
			
			// generate the solution
			ga.generateSolution();
			
			// try to minimize the best generated test
			TestChromosome best = (TestChromosome) ga.getBestIndividual();
			TestCaseMinimizer minimizer = new TestCaseMinimizer(fitness_function);
			minimizer.minimize(best);
			
			// add the best test-case to result
			result.add(best.getTestCase());
		}
		
		// add new tests to method handler
		for (TestCase test : result) {
			handler.addTestCase(null, test); // null for empty object state
		}
		return result;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Creates the expanded test-suite to explore the object behavior model
	 * containing the calls for every <tt>public</tt>-method of the class under test
	 * in all undiscovered states.
	 * 
	 * <p><b>Note:</b> This method <b>does</b> modify the graph given
	 * as parameter. A sequence of transitions is calculated for every end node
	 * of the given graph and all corresponding edges of the sequence are set
	 * discovered <tt>true</tt> whereby for every discovered edge the transition
	 * sequence leading to this edge is stored accordingly.</p>
	 * 
	 * @param graph - the graph holding the test sequences.
	 * 
	 * @return the list of test-cases for exploring the
	 *         object behavior model.
	 */
	private List<TestCase> createModelTest(BCGraph graph) {
		List<TestCase> result;
		GeneticAlgorithm ga;
		Map<BCNode,TransitionSequence> nodeToTransSeq = new HashMap<BCNode,TransitionSequence>();
		Set<BCNode> nodesToReach = graph.getEndNodes();
		
		// create the transition sequences to the end nodes
		if (!nodesToReach.isEmpty()) {
			// nodes to examine
			LinkedList<BCNode> nodesToExamine = new LinkedList<BCNode>();
			nodesToExamine.add(graph.getStartNode());
			
			// breadth-first search
			while (!nodesToExamine.isEmpty()) {
				// extract the first node
				BCNode node = nodesToExamine.pollFirst();
				
				// get a transition sequence of an incoming edge
				TransitionSequence alpha = new TransitionSequence(); // is empty for start node
				for (BCEdge incomingEdge : node.getIncomingEdges()) {
					if (incomingEdge.isDiscovered()) {
						alpha.addAll(incomingEdge.getTransitionSequence());
						break;
					}
				}
				
				// update all undiscovered outgoing edges
				for (BCEdge edge : node.getUndiscoveredOutgoingEdges()) {
					TransitionSequence newAlpha = new TransitionSequence(alpha);
					newAlpha.add(edge.getTransition());
					edge.setTransitionSequence(newAlpha, false); // isLastInAlpha flag is irrelevant
					edge.setDiscovered(true);
					
					// check whether the end node of the edge needs to be reached
					BCNode endNode = edge.getEndNode();
					if (nodesToReach.contains(endNode)) {
						nodesToReach.remove(endNode);
						if (!(endNode instanceof BCExceptionNode))
							nodeToTransSeq.put(endNode, newAlpha);
					} else {
						// add the end node to list of nodes to discover
						if (!nodesToExamine.contains(endNode))
							nodesToExamine.add(endNode);
					}
				}
			}
		}
		
		// check whether all states are already discovered
		if (nodeToTransSeq.isEmpty()) // no new test cases can be created
			return new ArrayList<TestCase>(0);
		
		// generate the test to expand the graph
		result = new ArrayList<TestCase>();
		
		// set up the genetic algorithm
		ga = new StandardGA(new RandomLengthTestFactory());
		ga.getSelectionFunction().setMaximize(false); // need to minimize for zero fitness goal
		ga.addStoppingCondition(new ZeroFitnessStoppingCondition());
		
		// for each transition sequence - append the calls for each public method
		for (BCNode node : nodeToTransSeq.keySet()) {
			// set up the coverage goals
			TestFitnessFactory goal_factory = new BehavioralCoverageFactory(cut.getDeclaredMethods());
			List<TestFitnessFunction> goals = goal_factory.getCoverageGoals();
			
			// for every fitness function (goal) generate a test-case
			for (TestFitnessFunction fitness_function : goals) {
				TransitionSequence test_seq =
						new TransitionSequence(nodeToTransSeq.get(node)); // old test sequence
				
				// reset the genetic algorithm
				ga.resetStoppingConditions();
				ga.clearPopulation();
				ga.setFitnessFunction(fitness_function); // add fitness function
				
				// generate the solution
				ga.generateSolution();
				
				// try to minimize the best generated test
				TestChromosome best = (TestChromosome) ga.getBestIndividual();
				TestCaseMinimizer minimizer = new TestCaseMinimizer(fitness_function);
				minimizer.minimize(best);
				
				// add new test to method handler
				TestCase bestTest = best.getTestCase();
				handler.addTestCase(node.getObjectState(), bestTest);
				
				// remove all unnecessary statements - i.e. the constructor part of the test
				if (!bestTest.isEmpty()) {
					Set<VariableReference> var_refs;
					StatementInterface statement;
					
					statement = bestTest.getStatement(bestTest.size()-1);
					assert (statement instanceof MethodStatement)
						: "The last statement is no method statement: " + bestTest;
					MethodStatement methodStatement = (MethodStatement) statement;
					var_refs = methodStatement.getVariableReferences();
					var_refs.remove(methodStatement.getCallee()); // remove caller reference
					var_refs.remove(methodStatement.getReturnValue()); // remove return value
					
					// set valid caller reference - reduces inconsistency
					methodStatement.setCallee(methodStatement.getReturnValue());
					
					// for every statement check whether it has a variable reference
					for (int i = bestTest.size()-2 ; i >= 0; i--) {
						statement = bestTest.getStatement(i);
						if (var_refs.contains(statement.getReturnValue())) {
							// update the set of variable references
							var_refs.addAll(statement.getVariableReferences());
							var_refs.remove(statement.getReturnValue()); // remove return value
						} else {
							bestTest.remove(i);
						}
					}
				}
				
				// add the new test-case to test sequence
				test_seq.add(bestTest);
				
				result.add(test_seq.getTestCase());
			}
		}
		return result;
	}
	
	/**
	 * Creates a <tt>JUnit</tt> test-suite in byte-code with given test-cases.</br>
	 * The test-suite file is set up in the output directory
	 * given by {@link Properties#OUTPUT_DIR},</br>
	 * whereby the name of the file is the default name
	 * given by {@link ModelExplorer#TEST_NAME}.</p>
	 * 
	 * @param tests - the list of test-cases to create the <tt>JUnit</tt> test-suite for.
	 */
	public static void writeTmpJUnitTests(List<TestCase> tests) {
		TestSuiteWriter suite = new TestSuiteWriter();
		suite.insertTests(tests);
		String dir = Properties.OUTPUT_DIR;
		suite.writeTestSuiteClass(TEST_NAME, dir);
	}
}
