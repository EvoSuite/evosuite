/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage;

import au.com.bytecode.opencsv.CSVReader;
import com.examples.with.different.packagename.Calculator;
import com.examples.with.different.packagename.PureEnum;
import com.examples.with.different.packagename.mutation.MutationPropagation;
import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.SystemTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jrojas
 * Edited by José Campos
 * 
 */
public class CoveredGoalsCountSystemTest extends SystemTestBase {

	@Before
	public void prepare() {
		try {
			FileUtils.deleteDirectory(new File("evosuite-report"));
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
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
        assertEquals("58", rows.get(1)[3]); // Covered_Goals
        assertEquals("58", rows.get(1)[4]); // Total_Goals
    }

    @Test
    public void testCoveredGoalsCountCSV_WithMinimizationTimeout() throws IOException {

    	EvoSuite evosuite = new EvoSuite();

    	String targetClass = MutationPropagation.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.STRONGMUTATION
        };
        Properties.OUTPUT_VARIABLES="TARGET_CLASS,criterion,Coverage,Covered_Goals,Total_Goals";
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

        String[] command = new String[] {
    		"-class", targetClass,
    		"-Dsearch_budget=40000",
    		"-Dminimization_timeout=0",
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
        assertEquals("STRONGMUTATION", rows.get(1)[1]); // criterion
        assertEquals("1.0", rows.get(1)[2]); // Coverage
        assertEquals("24", rows.get(1)[3]); // Covered_Goals
        assertEquals("24", rows.get(1)[4]); // Total_Goals
    }
    
    @Test
    public void testCoveredGoalsCountCSV_SingleCriterionBranch_Enums() throws IOException {

    	EvoSuite evosuite = new EvoSuite();

    	String targetClass = PureEnum.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.BRANCH
        };
        Properties.OUTPUT_VARIABLES="TARGET_CLASS,criterion,Coverage,Covered_Goals,Total_Goals,BranchCoverage";
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
        assertEquals("BRANCH", rows.get(1)[1]); // criterion
        assertEquals("1.0", rows.get(1)[2]); // Coverage
        assertEquals("0", rows.get(1)[3]); // Covered_Goals
        assertEquals("0", rows.get(1)[4]); // Total_Goals
        assertEquals("1.0", rows.get(1)[5]); // BranchCoverage
    }
    
    @Test
    public void testCoveredGoalsCountCSV_SingleCriterionBranch_Random_Enums() throws IOException {

    	EvoSuite evosuite = new EvoSuite();

    	String targetClass = PureEnum.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.BRANCH
        };
        Properties.OUTPUT_VARIABLES="TARGET_CLASS,criterion,Coverage,Covered_Goals,Total_Goals";
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

        String[] command = new String[] {
    		"-class", targetClass,
    		"-generateRandom"
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
        assertEquals("BRANCH", rows.get(1)[1]); // criterion
        assertEquals("1.0", rows.get(1)[2]); // Coverage
        assertEquals("0", rows.get(1)[3]); // Covered_Goals
        assertEquals("0", rows.get(1)[4]); // Total_Goals
    }
}
