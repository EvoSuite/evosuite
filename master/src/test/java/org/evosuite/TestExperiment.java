package org.evosuite;

import com.examples.with.different.packagename.Compositional;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

import java.util.List;

/**
 * @author Jose Miguel Rojas
 */
public class TestExperiment {

    @Test
    public void testExperiment() {
        EvoSuite evosuite = new EvoSuite();
        System.setProperty("user.dir","/Users/jmr/Systems/sf110/5_templateit");
        Properties.TARGET_CLASS = "org.templateit.Poi2ItextUtil";
        Properties.INHERITANCE_FILE = "/Users/jmr/Systems/sf110/5_templateit/evosuite-files/inheritance.xml.gz";
        Properties.PROPERTIES_FILE = "/Users/jmr/Systems/sf110/5_templateit/evosuite-files/evosuite.properties";
        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.METHOD,
                Properties.Criterion.METHODTRACE,
                Properties.Criterion.METHODNOEXCEPTION,
                Properties.Criterion.LINE,
                Properties.Criterion.ONLYBRANCH,
                Properties.Criterion.OUTPUT,
                Properties.Criterion.EXCEPTION,
                Properties.Criterion.ONLYMUTATION
        };
        Properties.ANALYSIS_CRITERIA="METHOD,METHODTRACE,METHODNOEXCEPTION,LINE,ONLYBRANCH,OUTPUT,EXCEPTION,ONLYMUTATION";
        Properties.COMPOSITIONAL_FITNESS = true;
        Properties.MINIMIZE = true;

        String[] command = new String[]{"-generateSuite", "-class", Properties.TARGET_CLASS, "-projectCP", "/Users/jmr/Systems/sf110/5_templateit/build/classes:/Users/jmr/Systems/sf110/5_templateit/lib/bcmail-jdk14-138.jar:/Users/jmr/Systems/sf110/5_templateit/lib/bcprov-jdk14-138.jar:/Users/jmr/Systems/sf110/5_templateit/lib/commons-logging-1.1.jar:/Users/jmr/Systems/sf110/5_templateit/lib/itext-2.1.4.jar:/Users/jmr/Systems/sf110/5_templateit/lib/log4j-1.2.13.jar:/Users/jmr/Systems/sf110/5_templateit/lib/poi-3.2-FINAL.jar:/Users/jmr/Systems/sf110/5_templateit/templateit.jar"};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
    }

    protected GeneticAlgorithm<?> getGAFromResult(Object result) {
        assert (result instanceof List);
        List<List<TestGenerationResult>> results = (List<List<TestGenerationResult>>) result;
        assert (results.size() == 1);
        //return results.iterator().next().getGeneticAlgorithm();
        return results.get(0).get(0).getGeneticAlgorithm();
    }
}
