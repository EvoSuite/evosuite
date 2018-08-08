package org.evosuite.symbolic;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.evosuite.Properties;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.vm.ConstraintFactory;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.localsearch.DSETestGenerator;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a DSE algorithm *as* a subclass of genetic algorithm.
 * 
 * @author jgaleotti
 *
 * @param <T>
 */
public class DSEAlgorithm extends GeneticAlgorithm<TestSuiteChromosome> {

  private static final Logger logger = LoggerFactory.getLogger(DSEAlgorithm.class);

  /**
   * A cache of previous results from the constraint solver
   */
  private final Map<Set<Constraint<?>>, SolverResult> queryCache =
      new HashMap<Set<Constraint<?>>, SolverResult>();

  /**
   * Applies DSE test generation on a static non-private method until a stopping condition is met or
   * all queries have been explored.
   * 
   * @param staticEntryMethod
   * 
   */
  private void generateTestCasesAndAppendToBestIndividual(Method staticEntryMethod) {

    double fitnessBeforeAddingDefaultTest = this.getBestIndividual().getFitness();
    logger.debug("Fitness before adding default test case:" + fitnessBeforeAddingDefaultTest);

    List<TestCase> generatedTests = new ArrayList<TestCase>();

    TestCase testCaseWithDefaultValues = buildTestCaseWithDefaultValues(staticEntryMethod);

    getBestIndividual().addTest(testCaseWithDefaultValues);
    generatedTests.add(testCaseWithDefaultValues);

    logger.debug(
        "Created new default test case with default values:" + testCaseWithDefaultValues.toCode());

    calculateFitnessAndSortPopulation();
    double fitnessAfterAddingDefaultTest = this.getBestIndividual().getFitness();
    logger.debug("Fitness after adding default test case: " + fitnessAfterAddingDefaultTest);

    if (fitnessAfterAddingDefaultTest == 0) {
      logger.debug("No more DSE test generation since fitness is 0");
      return;
    }

    HashSet<Set<Constraint<?>>> pathConditions = new HashSet<Set<Constraint<?>>>();

    for (int currentTestIndex = 0; currentTestIndex < generatedTests
        .size(); currentTestIndex++) {

      TestCase currentTestCase = generatedTests.get(currentTestIndex);

      if (this.isFinished()) {
        logger.debug("DSE test generation met a stopping condition. Exiting with "
            + generatedTests.size() + " generated test cases for method "
            + staticEntryMethod.getName());
        return;
      }

      logger.debug("Starting concolic execution of test case: " + currentTestCase.toCode());

      TestCase clonedTestCase = currentTestCase.clone();

      final PathCondition pathCondition =
          ConcolicExecution.executeConcolic((DefaultTestCase) clonedTestCase);
      logger
          .debug("Path condition collected with : " + pathCondition.size() + " branches");

      Set<Constraint<?>> constraintsSet = canonicalize(pathCondition.getConstraints());
      pathConditions.add(constraintsSet);
      logger.debug("Number of stored path condition: " + pathConditions.size());

      for (int i = pathCondition.size() - 1; i >= 0; i--) {
        logger.debug("negating index " + i + " of path condition");

        List<Constraint<?>> query = DSETestGenerator.buildQuery(pathCondition, i);

        Set<Constraint<?>> constraintSet = canonicalize(query);

        if (queryCache.containsKey(constraintSet)) {
          logger.debug("skipping solving of current query since it is in the query cache");
          continue;
        }

        if (isSubSetOf(constraintSet, queryCache.keySet())) {
          logger.debug(
              "skipping solving of current query because it is satisfiable and solved by previous path condition");
          continue;
        }

        if (pathConditions.contains(constraintSet)) {
          logger.debug("skipping solving of current query because of existing path condition");
          continue;

        }

        if (isSubSetOf(constraintSet, pathConditions)) {
          logger.debug(
              "skipping solving of current query because it is satisfiable and solved by previous path condition");
          continue;
        }

        if (this.isFinished()) {
          logger.debug("DSE test generation met a stopping condition. Exiting with "
              + generatedTests.size() + " generated test cases for method "
              + staticEntryMethod.getName());
          return;
        }

        logger.debug("Solving query with  " + query.size() + " constraints");

        List<Constraint<?>> varBounds = createVarBounds(query);
        query.addAll(varBounds);

        SolverResult result = DSETestGenerator.solve(query);

        queryCache.put(constraintSet, result);
        logger.debug("Number of stored entries in query cache : " + queryCache.keySet().size());

        if (result == null) {
          logger.debug("Solver outcome is null (probably failure/unknown");
        } else if (result.isSAT()) {
          logger.debug("query is SAT (solution found)");
          Map<String, Object> solution = result.getModel();
          logger.debug("solver found solution " + solution.toString());

          TestCase newTest = DSETestGenerator.updateTest(currentTestCase, solution);
          logger.debug("Created new test case from SAT solution:" + newTest.toCode());
          generatedTests.add(newTest);

          double fitnessBeforeAddingNewTest = this.getBestIndividual().getFitness();
          logger.debug("Fitness before adding new test" + fitnessBeforeAddingNewTest);

          getBestIndividual().addTest(newTest);

          calculateFitness(getBestIndividual());

          double fitnessAfterAddingNewTest = this.getBestIndividual().getFitness();
          logger.debug("Fitness after adding new test " + fitnessAfterAddingNewTest);

          this.notifyIteration();

          if (fitnessAfterAddingNewTest == 0) {
            logger.debug("No more DSE test generation since fitness is 0");
            return;
          }

        } else {
          assert (result.isUNSAT());
          logger.debug("query is UNSAT (no solution found)");
        }
      }
    }

    logger.debug("DSE test generation finished for method " + staticEntryMethod.getName()
        + ". Exiting with " + generatedTests.size() + " generated test cases");
    return;
  }

  protected static HashSet<Constraint<?>> canonicalize(List<Constraint<?>> query) {
    return new HashSet<Constraint<?>>(query);
  }

  private static List<Constraint<?>> createVarBounds(List<Constraint<?>> query) {

    Set<Variable<?>> variables = new HashSet<Variable<?>>();
    for (Constraint<?> constraint : query) {
      variables.addAll(constraint.getVariables());
    }

    List<Constraint<?>> boundsForVariables = new ArrayList<Constraint<?>>();
    for (Variable<?> variable : variables) {
      if (variable instanceof IntegerVariable) {
        IntegerVariable integerVariable = (IntegerVariable) variable;
        Long minValue = integerVariable.getMinValue();
        Long maxValue = integerVariable.getMaxValue();
        if (maxValue == Long.MAX_VALUE && minValue == Long.MIN_VALUE) {
          // skip constraints for Long variables
          continue;
        }
        IntegerConstant minValueExpr = ExpressionFactory.buildNewIntegerConstant(minValue);
        IntegerConstant maxValueExpr = ExpressionFactory.buildNewIntegerConstant(maxValue);
        IntegerConstraint minValueConstraint = ConstraintFactory.gte(integerVariable, minValueExpr);
        IntegerConstraint maxValueConstraint = ConstraintFactory.lte(integerVariable, maxValueExpr);
        boundsForVariables.add(minValueConstraint);
        boundsForVariables.add(maxValueConstraint);

      } else if (variable instanceof RealVariable) {
        // skip
      } else if (variable instanceof StringVariable) {
        // skip
      } else {
        throw new UnsupportedOperationException(
            "Unknown variable type " + variable.getClass().getName());
      }
    }

    return boundsForVariables;
  }

  /**
   * Returns true if the constraints in the query are a subset of any of the constraints in the set
   * of queries
   * 
   * @param query
   * @param queries
   * @return
   */
  private static boolean isSubSetOf(Set<Constraint<?>> query,
      Collection<Set<Constraint<?>>> queries) {
    for (Set<Constraint<?>> pathCondition : queries) {
      if (pathCondition.containsAll(query)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Builds a default test case for a static target method
   * 
   * @param targetStaticMethod
   * @return
   */
  private static DefaultTestCase buildTestCaseWithDefaultValues(Method targetStaticMethod) {
    TestCaseBuilder testCaseBuilder = new TestCaseBuilder();

    Type[] argumentTypes = Type.getArgumentTypes(targetStaticMethod);
    Class<?>[] argumentClasses = targetStaticMethod.getParameterTypes();

    ArrayList<VariableReference> arguments = new ArrayList<VariableReference>();
    for (int i = 0; i < argumentTypes.length; i++) {

      Type argumentType = argumentTypes[i];
      Class<?> argumentClass = argumentClasses[i];

      switch (argumentType.getSort()) {
        case Type.BOOLEAN: {
          VariableReference booleanVariable = testCaseBuilder.appendBooleanPrimitive(false);
          arguments.add(booleanVariable);
          break;
        }
        case Type.BYTE: {
          VariableReference byteVariable = testCaseBuilder.appendBytePrimitive((byte) 0);
          arguments.add(byteVariable);
          break;
        }
        case Type.CHAR: {
          VariableReference charVariable = testCaseBuilder.appendCharPrimitive((char) 0);
          arguments.add(charVariable);
          break;
        }
        case Type.SHORT: {
          VariableReference shortVariable = testCaseBuilder.appendShortPrimitive((short) 0);
          arguments.add(shortVariable);
          break;
        }
        case Type.INT: {
          VariableReference intVariable = testCaseBuilder.appendIntPrimitive(0);
          arguments.add(intVariable);
          break;
        }
        case Type.LONG: {
          VariableReference longVariable = testCaseBuilder.appendLongPrimitive(0L);
          arguments.add(longVariable);
          break;
        }
        case Type.FLOAT: {
          VariableReference floatVariable = testCaseBuilder.appendFloatPrimitive((float) 0.0);
          arguments.add(floatVariable);
          break;
        }
        case Type.DOUBLE: {
          VariableReference doubleVariable = testCaseBuilder.appendDoublePrimitive(0.0);
          arguments.add(doubleVariable);
          break;
        }
        case Type.ARRAY: {
          VariableReference arrayVariable = testCaseBuilder.appendArrayStmt(argumentClass, 0);
          arguments.add(arrayVariable);
          break;
        }
        case Type.OBJECT: {
          if (argumentClass.equals(String.class)) {
            VariableReference stringVariable = testCaseBuilder.appendStringPrimitive("");
            arguments.add(stringVariable);
          } else {
            VariableReference objectVariable = testCaseBuilder.appendNull(argumentClass);
            arguments.add(objectVariable);
          }
          break;
        }
        default: {
          throw new UnsupportedOperationException();
        }
      }
    }

    testCaseBuilder.appendMethod(null, targetStaticMethod,
        arguments.toArray(new VariableReference[] {}));
    DefaultTestCase testCase = testCaseBuilder.getDefaultTestCase();

    return testCase;
  }

  /**
   * Creates a DSE algorithm for test generation.
   */
  public DSEAlgorithm() {
    super(null);
  }

  /**
   * 
   */
  private static final long serialVersionUID = 964984026539409121L;

  /**
   * This algorithm does not evolve populations
   */
  @Override
  protected void evolve() {
    // skip
  }

  /**
   * The population is initialized with an empty test suite
   */
  @Override
  public void initializePopulation() {
    TestSuiteChromosome individual = new TestSuiteChromosome();
    population.clear();
    population.add(individual);
    calculateFitness(individual);
  }

  /**
   * Returns a set with the static methods of a class
   * 
   * @param targetClass a class instance
   * @return
   */
  private static List<Method> getTargetStaticMethods(Class<?> targetClass) {
    Method[] declaredMethods = targetClass.getDeclaredMethods();
    List<Method> targetStaticMethods = new LinkedList<Method>();
    for (Method m : declaredMethods) {

      if (!Modifier.isStatic(m.getModifiers())) {
        continue;
      }

      if (Modifier.isPrivate(m.getModifiers())) {
        continue;
      }

      if (m.getName().equals(ClassResetter.STATIC_RESET)) {
        continue;
      }

      targetStaticMethods.add(m);
    }
    return targetStaticMethods;
  }

  /**
   * Applies the DSE test generation using the initial population as the initial test cases
   */
  @Override
  public void generateSolution() {
    this.notifySearchStarted();
    this.initializePopulation();

    final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

    List<Method> targetStaticMethods = getTargetStaticMethods(targetClass);
    Collections.sort(targetStaticMethods, new MethodComparator());
    logger.debug("Found " + targetStaticMethods.size() + " as entry points for DSE");

    for (Method entryMethod : targetStaticMethods) {

      if (this.isFinished()) {
        logger.debug("A stoping condition was met. No more tests can be generated using DSE.");
        break;
      }

      if (getBestIndividual().getFitness() == 0) {
        logger.debug("Best individual reached zero fitness");
        break;
      }

      logger.debug("Generating tests for entry method" + entryMethod.getName());
      int testCaseCount = getBestIndividual().getTests().size();
      generateTestCasesAndAppendToBestIndividual(entryMethod);
      int numOfGeneratedTestCases = getBestIndividual().getTests().size() - testCaseCount;
      logger.debug(numOfGeneratedTestCases + " tests were generated for entry method "
          + entryMethod.getName());

    }

    this.updateFitnessFunctionsAndValues();
    this.notifySearchFinished();
  }

}
