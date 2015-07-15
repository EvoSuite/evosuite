package org.evosuite.coverage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.Calculator;
import com.examples.with.different.packagename.SingleMethod;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by jrojas
 */
public class TestCoveredGoalsCount extends SystemTest {

    @Test
    public void testCoveredGoalsCount() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = SingleMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.TEST_ARCHIVE = false;// || true;
        Properties.MINIMIZE = false;// || true;
        Properties.COVERAGE = false;// || true;
        Properties.POPULATION = 1;
        Properties.MAX_LENGTH = 3;
        Properties.CHROMOSOME_LENGTH = 3;

        Properties.CRITERION = new Properties.Criterion[] {Properties.Criterion.ONLYLINE};

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        System.out.println("EvolvedTestSuite:\n" + best);
        System.out.println("CoveredGoals: " + best.getCoveredGoals());
        Assert.assertEquals("getCoveredGoals().size()", 1, best.getCoveredGoals().size());
        Assert.assertEquals("getNumOfCoveredGoals()", 1, best.getNumOfCoveredGoals());
    }

    @Test
    public void testCoveredGoalsCountCSV_SingleCriterion() throws IOException {

    	EvoSuite evosuite = new EvoSuite();

    	String targetClass = Calculator.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.WEAKMUTATION
        };
        Properties.OUTPUT_VARIABLES="TARGET_CLASS,criterion,Coverage,Covered_Goals,Total_Goals";
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

        String[] command = new String[] {
    		"-class", targetClass,
    		"-generateSuite"
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        String statistics_file = System.getProperty("user.dir") + File.separator + Properties.REPORT_DIR + File.separator + "statistics.csv";
        System.out.println("Statistics file " + statistics_file);

        CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertTrue(rows.size() == 2);
        reader.close();

        assertEquals(targetClass, rows.get(1)[0]); // TARGET_CLASS
        assertEquals("WEAKMUTATION", rows.get(1)[1]); // criterion
        assertEquals("1.0", rows.get(1)[2]); // Coverage
        assertEquals("48", rows.get(1)[3]); // Covered_Goals
        assertEquals("48", rows.get(1)[4]); // Total_Goals

        /**
         * FIXME
         *  
         * Why the list of covered goals also include Branch goals ?
         * 
         * Note: this test case is failing for MUTATION, STRONGMUTATION, or WEAKMUTATION
         */
    }

    @Test
    public void testCoveredGoalsCountCSV_MultipleCriterion() throws IOException {

    	EvoSuite evosuite = new EvoSuite();

    	String targetClass = Calculator.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.BRANCH,
        	Properties.Criterion.LINE,
        	Properties.Criterion.ONLYMUTATION
        };
        Properties.OUTPUT_VARIABLES="TARGET_CLASS,criterion,Coverage,Covered_Goals,Total_Goals";
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

        String[] command = new String[] {
    		"-class", targetClass,
    		"-generateSuite"
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        String statistics_file = System.getProperty("user.dir") + File.separator + Properties.REPORT_DIR + File.separator + "statistics.csv";
        System.out.println("Statistics file " + statistics_file);

        CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertTrue(rows.size() == 2);
        reader.close();

        assertEquals(targetClass, rows.get(1)[0]); // TARGET_CLASS
        assertEquals("BRANCH;LINE;ONLYMUTATION", rows.get(1)[1]); // criterion
        assertEquals("1.0", rows.get(1)[2]); // Coverage
        assertEquals("57", rows.get(1)[3]); // Covered_Goals
        assertEquals("57", rows.get(1)[4]); // Total_Goals
    }
}
