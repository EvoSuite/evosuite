/**
 * 
 */
package org.evosuite.coverage;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;

/**
 * @author Gordon Fraser
 * 
 */
public class CoverageAnalysis {

	private static final Logger logger = LoggerFactory.getLogger(CoverageAnalysis.class);

	private static Map<String, StringBuffer> coverageBitString = new TreeMap<String, StringBuffer>();

	private static boolean isMutationCriterion(Properties.Criterion criterion) {
		switch (criterion) {
		case MUTATION:
		case WEAKMUTATION:
		case STRONGMUTATION:
		case ONLYMUTATION:
			return true;
		default:
			return false;
		}
	}

	private static void reinstrument(TestSuiteChromosome testSuite,
	        Properties.Criterion criterion) {

		// do not reinstrument a testSuite for criterion that we have
		// been optimizing (because we already have coverage for that)
		if (ArrayUtil.contains(Properties.CRITERION, criterion)) {
			return ;
		}

		if (Properties.SECONDARY_OBJECTIVE.toLowerCase().contains("ibranch")
				|| Properties.SECONDARY_OBJECTIVE.toLowerCase().contains("archiveibranch")) {
			ExecutionTracer.enableContext();
		}
		if (!ExecutionTracer.isTraceCallsEnabled()) {
			ExecutionTracer.enableTraceCalls();
		}

		testSuite.setChanged(true);
		for (TestChromosome test : testSuite.getTestChromosomes()) {
			test.setChanged(true);
			test.clearCachedResults(); // clears last execution result and last mutation result
		}

        Properties.Criterion oldCriterion[] = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
		Properties.CRITERION = new Properties.Criterion[] { criterion };

		logger.info("Re-instrumenting for criterion: " + criterion);
		TestGenerationContext.getInstance().resetContext();

		// Need to load class explicitly in case there are no test cases.
		// If there are tests, then this is redundant
		Properties.getTargetClass();

		// TODO: Now all existing test cases have reflection objects pointing to the wrong classloader
		logger.info("Changing classloader of test suite for criterion: " + criterion);

		for (TestChromosome test : testSuite.getTestChromosomes()) {
			DefaultTestCase dtest = (DefaultTestCase) test.getTestCase();
			dtest.changeClassLoader(TestGenerationContext.getInstance().getClassLoaderForSUT());
		}
		Properties.CRITERION = oldCriterion;
	}

	public static void analyzeCriteria(TestSuiteChromosome testSuite, String criteria) {

		// If coverage of target criteria is not already measured
		if(!Properties.COVERAGE) {
			for (Criterion c : Properties.CRITERION) {
				// Analyse coverage for enabled criteria
				// LoggingUtils.getEvoLogger().info("  - " + c.name());
				logger.debug("Measuring coverage of target criterion "+c);
				analyzeCoverage(testSuite, c.name());
			}
		}

        for (String extraCriterion : Arrays.asList(criteria.toUpperCase().split(",")))
        {
        	if (extraCriterion.equals("CBRANCH")){
    			Properties.INSTRUMENT_METHOD_CALLS = true;
    		}
            // Analyse coverage for extra criteria
            if (! ArrayUtil.contains(Properties.CRITERION, extraCriterion)) {
    		    logger.debug("Measuring additional coverage of target criterion "+extraCriterion);
                analyzeCoverage(testSuite, extraCriterion);
            }
        }
	}

	public static void analyzeCoverage(TestSuiteChromosome testSuite, String criterion) {
		try {
			logger.info("Measuring coverage of criterion: "
			                                         + criterion);

			Properties.Criterion crit = Properties.Criterion.valueOf(criterion.toUpperCase());
			analyzeCoverage(testSuite, crit);
		} catch (IllegalArgumentException e) {
			LoggingUtils.getEvoLogger().info("* Unknown coverage criterion: " + criterion);
		}
	}

	public static RuntimeVariable getCoverageVariable(Properties.Criterion criterion) {
		switch (criterion) {
		case ALLDEFS:
			return RuntimeVariable.AllDefCoverage;
		case BRANCH:
			return RuntimeVariable.BranchCoverage;
		case CBRANCH:
			return RuntimeVariable.CBranchCoverage;
		case EXCEPTION:
			return RuntimeVariable.ExceptionCoverage;
		case DEFUSE:
			return RuntimeVariable.DefUseCoverage;
		case STATEMENT:
			return RuntimeVariable.StatementCoverage;
		case RHO:
			return RuntimeVariable.RhoCoverage;
		case AMBIGUITY:
			return RuntimeVariable.AmbiguityCoverage;
		case STRONGMUTATION:
		case MUTATION:
			return RuntimeVariable.MutationScore;
		case ONLYMUTATION:
			return RuntimeVariable.OnlyMutationScore;
		case WEAKMUTATION:
			return RuntimeVariable.WeakMutationScore;
		case ONLYBRANCH:
			return RuntimeVariable.OnlyBranchCoverage;
		case METHODTRACE:
			return RuntimeVariable.MethodTraceCoverage;
		case METHOD:
			return RuntimeVariable.MethodCoverage;
		case METHODNOEXCEPTION:
			return RuntimeVariable.MethodNoExceptionCoverage;
		case ONLYLINE:
		case LINE:
			return RuntimeVariable.LineCoverage;
		case OUTPUT:
			return RuntimeVariable.OutputCoverage;
		case INPUT:
			return RuntimeVariable.InputCoverage;
		case IBRANCH:
			return RuntimeVariable.IBranchCoverage;
		case REGRESSION:
		default:
			throw new RuntimeException("Criterion not supported: " + criterion);

		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void analyzeCoverage(TestSuiteChromosome testSuite, Properties.Criterion criterion) {

		TestSuiteChromosome testSuiteCopy = testSuite.clone();

		reinstrument(testSuiteCopy, criterion);
		TestFitnessFactory factory = FitnessFunctions.getFitnessFactory(criterion);

		for(TestChromosome test : testSuiteCopy.getTestChromosomes()) {
			test.getTestCase().clearCoveredGoals();

			// independently of mutation being a main or secondary criteria,
			// test cases have to be 'changed'. with this, isCovered() will
			// re-execute test cases and it will be able to find the covered goals
			if (isMutationCriterion(criterion)) {
				test.setChanged(true);
			}
		}

		List<TestFitnessFunction> goals = factory.getCoverageGoals();
        Collections.sort(goals);

		StringBuffer buffer = new StringBuffer(goals.size());
		int covered = 0;

		for (TestFitnessFunction goal : goals) {
			if (goal.isCoveredBy(testSuiteCopy)) {
				logger.debug("Goal {} is covered", goal);
				covered++;
                buffer.append("1");
			} else {
				logger.debug("Goal {} is not covered", goal);
                buffer.append("0");
			}
		}

		coverageBitString.put(criterion.name(), buffer);
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.CoverageBitString,
				coverageBitString.size() == 0 ? "0" : coverageBitString.values().toString().replace("[", "").replace("]", "").replace(", ", ""));

        RuntimeVariable bitStringVariable = getBitStringVariable(criterion);
        if(bitStringVariable != null){
            String goalBitString = buffer.toString();
            ClientServices.getInstance().getClientNode().trackOutputVariable(bitStringVariable,goalBitString);
        }

		if (goals.isEmpty()) {
			if (criterion == Properties.Criterion.MUTATION
			        || criterion == Properties.Criterion.STRONGMUTATION) {
				ClientServices.getInstance().getClientNode().trackOutputVariable(
                        RuntimeVariable.MutationScore, 1.0);
			}
			LoggingUtils.getEvoLogger().info("* Coverage of criterion " + criterion + ": 100% (no goals)");
			ClientServices.getInstance().getClientNode().trackOutputVariable(getCoverageVariable(criterion), 1.0);
		} else {

			ClientServices.getInstance().getClientNode().trackOutputVariable(
                    getCoverageVariable(criterion), (double) covered / (double) goals.size());

			if (criterion == Properties.Criterion.MUTATION
			        || criterion == Properties.Criterion.STRONGMUTATION) {
				ClientServices.getInstance().getClientNode().trackOutputVariable(
                        RuntimeVariable.MutationScore, (double) covered  / (double) goals.size());
			}

			LoggingUtils.getEvoLogger().info("* Coverage of criterion "
                    + criterion
                    + ": "
                    + NumberFormat.getPercentInstance().format((double) covered  / (double) goals.size()));

			LoggingUtils.getEvoLogger().info("* Total number of goals: " + goals.size());
			LoggingUtils.getEvoLogger().info("* Number of covered goals: " + covered);

		}
	}

    public static RuntimeVariable getBitStringVariable(Properties.Criterion criterion){
        switch (criterion){
        	case EXCEPTION:
        		return RuntimeVariable.ExceptionCoverageBitString;
        	case DEFUSE:
        		return RuntimeVariable.DefUseCoverageBitString;
        	case ALLDEFS:
        		return RuntimeVariable.AllDefCoverageBitString;
        	case BRANCH:
                return RuntimeVariable.BranchCoverageBitString;
        	case CBRANCH:
        		return RuntimeVariable.CBranchCoverageBitString;
        	case IBRANCH:
        		return RuntimeVariable.IBranchCoverageBitString;
        	case ONLYBRANCH:
        		return RuntimeVariable.OnlyBranchCoverageBitString;
        	case MUTATION:
        	case STRONGMUTATION:
        		return RuntimeVariable.MutationCoverageBitString;
        	case WEAKMUTATION:
        		return RuntimeVariable.WeakMutationCoverageBitString;
        	case ONLYMUTATION:
        		return RuntimeVariable.OnlyMutationCoverageBitString;
        	case METHODTRACE:
        		return RuntimeVariable.MethodTraceCoverageBitString;
        	case METHOD:
        		return RuntimeVariable.MethodCoverageBitString;
        	case METHODNOEXCEPTION:
        		return RuntimeVariable.MethodNoExceptionCoverageBitString;
        	case OUTPUT:
        		return RuntimeVariable.OutputCoverageBitString;
        	case INPUT:
        		return RuntimeVariable.InputCoverageBitString;
        	case STATEMENT:
        		return RuntimeVariable.StatementCoverageBitString;
        	case LINE:
        	case ONLYLINE:
        		return RuntimeVariable.LineCoverageBitString;
            default:
            	throw new RuntimeException("Criterion not supported: " + criterion);
        }
    }
}
