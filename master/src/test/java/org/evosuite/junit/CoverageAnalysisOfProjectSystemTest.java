/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.SystemTestBase;
import org.evosuite.continuous.persistency.CsvJUnitData;
import org.evosuite.statistics.SearchStatistics;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.Calculator;
import com.examples.with.different.packagename.CalculatorTest;
import com.examples.with.different.packagename.ClassNumberUtils;
import com.examples.with.different.packagename.ClassNumberUtilsTest;
import com.examples.with.different.packagename.ClassWithPrivateInterfaces;
import com.examples.with.different.packagename.ClassWithPrivateInterfacesTest;
import com.examples.with.different.packagename.FinalClass;
import com.examples.with.different.packagename.FinalClassTest;
import com.examples.with.different.packagename.StringUtils;
import com.examples.with.different.packagename.StringUtilsEqualsIndexOfTest;
import com.examples.with.different.packagename.WordUtils;
import com.examples.with.different.packagename.WordUtilsTest;
import com.examples.with.different.packagename.coverage.MethodWithSeveralInputArguments;
import com.examples.with.different.packagename.coverage.TestMethodWithSeveralInputArguments;

import com.opencsv.CSVReader;

public class CoverageAnalysisOfProjectSystemTest extends SystemTestBase {

    private File classes_directory = null;
    private File tests_directory = null;

    @Before
    public void prepare() {
        try {
            FileUtils.deleteDirectory(new File("evosuite-report"));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        Properties.TARGET_CLASS = "";
        Properties.OUTPUT_VARIABLES = null;
        Properties.STATISTICS_BACKEND = StatisticsBackend.DEBUG;
        Properties.COVERAGE_MATRIX = false;

        SearchStatistics.clearInstance();
        CoverageAnalysis.reset();
    }

    @After
    public void clean() {
        try {
            if (this.classes_directory != null && this.classes_directory.exists()) {
                FileUtils.deleteDirectory(this.classes_directory);
            }
            if (this.tests_directory != null && this.tests_directory.exists()) {
                FileUtils.deleteDirectory(this.tests_directory);
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        this.classes_directory = null;
        this.tests_directory = null;
    }

    @Test
    public void testMoreThanOneClassOneCriterion() throws IOException, CsvException {
        createFakeProject();

        EvoSuite evosuite = new EvoSuite();

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.LINE
        };
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

        String[] command = new String[]{
                "-target", this.classes_directory.getAbsolutePath(),
                "-Djunit=" + this.tests_directory.getAbsolutePath(),
                "-projectCP", this.classes_directory.getAbsolutePath() + File.pathSeparator + this.tests_directory.getAbsolutePath(),
                "-measureCoverage"
        };

        SearchStatistics statistics = (SearchStatistics) evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        String statistics_file = System.getProperty("user.dir") + File.separator + Properties.REPORT_DIR + File.separator + "statistics.csv";

        CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertEquals(8, rows.size()); // header + 7 classes
        reader.close();

        // assert TargetClasses
        List<String> classes = CsvJUnitData.getValues(rows, "TARGET_CLASS");
        assertTrue(classes.contains(Calculator.class.getCanonicalName()));
        assertTrue(classes.contains(MethodWithSeveralInputArguments.class.getCanonicalName()));
        assertTrue(classes.contains(WordUtils.class.getCanonicalName()));
        assertTrue(classes.contains(FinalClass.class.getCanonicalName()));
        assertTrue(classes.contains(StringUtils.class.getCanonicalName()));
        assertTrue(classes.contains(ClassNumberUtils.class.getCanonicalName()));
        assertTrue(classes.contains(ClassWithPrivateInterfaces.class.getCanonicalName()));

        // assert Coverage
        List<String> coverages = CsvJUnitData.getValues(rows, "Coverage");
        assertEquals(7, coverages.size());
        Collections.sort(coverages);
        assertEquals(0.80, Double.valueOf(coverages.get(0)), 0.01);
        assertEquals(0.80, Double.valueOf(coverages.get(1)), 0.00);
        assertEquals(0.90, Double.valueOf(coverages.get(2)), 0.00);
        assertEquals(0.93, Double.valueOf(coverages.get(3)), 0.01);
        assertEquals(0.95, Double.valueOf(coverages.get(4)), 0.00);
        assertEquals(1.00, Double.valueOf(coverages.get(5)), 0.00);
        assertEquals(1.00, Double.valueOf(coverages.get(6)), 0.00);
    }

    @Test
    public void testMoreThanOneClassMoreThanOneCriterion() throws IOException, CsvException {
        createFakeProject();

        EvoSuite evosuite = new EvoSuite();

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.BRANCH,
                Properties.Criterion.LINE
        };
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

        String[] command = new String[]{
                "-target", this.classes_directory.getAbsolutePath(),
                "-Djunit=" + this.tests_directory.getAbsolutePath(),
                "-projectCP", this.classes_directory.getAbsolutePath() + File.pathSeparator + this.tests_directory.getAbsolutePath(),
                "-measureCoverage"
        };

        SearchStatistics statistics = (SearchStatistics) evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        String statistics_file = System.getProperty("user.dir") + File.separator + Properties.REPORT_DIR + File.separator + "statistics.csv";

        CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertEquals(8, rows.size()); // header + 7 classes
        reader.close();

        // assert TargetClasses
        List<String> classes = CsvJUnitData.getValues(rows, "TARGET_CLASS");
        assertTrue(classes.contains(Calculator.class.getCanonicalName()));
        assertTrue(classes.contains(MethodWithSeveralInputArguments.class.getCanonicalName()));
        assertTrue(classes.contains(WordUtils.class.getCanonicalName()));
        assertTrue(classes.contains(FinalClass.class.getCanonicalName()));
        assertTrue(classes.contains(StringUtils.class.getCanonicalName()));
        assertTrue(classes.contains(ClassNumberUtils.class.getCanonicalName()));
        assertTrue(classes.contains(ClassWithPrivateInterfaces.class.getCanonicalName()));

        // assert Coverage
        List<String> coverages = CsvJUnitData.getValues(rows, "Coverage");
        assertEquals(7, coverages.size());
        Collections.sort(coverages);
//        assertEquals(0.45, Double.valueOf(coverages.get(0)), 0.01);
//        assertEquals(0.47, Double.valueOf(coverages.get(1)), 0.01);
//        assertEquals(0.52, Double.valueOf(coverages.get(2)), 0.01);
//        assertEquals(0.58, Double.valueOf(coverages.get(3)), 0.02);
//        assertEquals(0.88, Double.valueOf(coverages.get(4)), 0.01);
//        assertEquals(1.00, Double.valueOf(coverages.get(5)), 0.00);
//        assertEquals(1.00, Double.valueOf(coverages.get(6)), 0.00);
        assertTrue(Double.valueOf(coverages.get(0)) > 0.0);
        assertTrue(Double.valueOf(coverages.get(1)) > 0.0);
        assertTrue(Double.valueOf(coverages.get(2)) > 0.0);
        assertTrue(Double.valueOf(coverages.get(3)) > 0.0);
        assertTrue(Double.valueOf(coverages.get(4)) > 0.0);
        assertTrue(Double.valueOf(coverages.get(5)) > 0.0);
        assertTrue(Double.valueOf(coverages.get(6)) > 0.0);
    }

    private void createFakeProject() throws IOException, CsvException {
        String classesRootDir = System.getProperty("user.dir") + File.separator + "target" + File.separator + "test-classes" + File.separator;

        this.classes_directory = Files.createTempDirectory("fakeProject_classes_").toFile();
        this.classes_directory.mkdir();

        this.tests_directory = Files.createTempDirectory("fakeProject_tests_").toFile();
        this.tests_directory.mkdir();

        // copy some classes
        copyClassFile(classesRootDir, this.classes_directory.getAbsolutePath(), Calculator.class);
        copyClassFile(classesRootDir, this.tests_directory.getAbsolutePath(), CalculatorTest.class);
        copyClassFile(classesRootDir, this.classes_directory.getAbsolutePath(), MethodWithSeveralInputArguments.class);
        copyClassFile(classesRootDir, this.tests_directory.getAbsolutePath(), TestMethodWithSeveralInputArguments.class);
        copyClassFile(classesRootDir, this.classes_directory.getAbsolutePath(), WordUtils.class);
        copyClassFile(classesRootDir, this.tests_directory.getAbsolutePath(), WordUtilsTest.class);
        copyClassFile(classesRootDir, this.classes_directory.getAbsolutePath(), FinalClass.class);
        copyClassFile(classesRootDir, this.tests_directory.getAbsolutePath(), FinalClassTest.class);
        copyClassFile(classesRootDir, this.classes_directory.getAbsolutePath(), StringUtils.class);
        copyClassFile(classesRootDir, this.tests_directory.getAbsolutePath(), StringUtilsEqualsIndexOfTest.class);
        copyClassFile(classesRootDir, this.classes_directory.getAbsolutePath(), ClassNumberUtils.class);
        copyClassFile(classesRootDir, this.tests_directory.getAbsolutePath(), ClassNumberUtilsTest.class);
        copyClassFile(classesRootDir, this.classes_directory.getAbsolutePath(), ClassWithPrivateInterfaces.class);
        copyClassFile(classesRootDir, this.tests_directory.getAbsolutePath(), ClassWithPrivateInterfacesTest.class);
    }

    private void copyClassFile(String root, String tmp, Class<?> clazz) throws IOException {
        // create structure
        new File(tmp, clazz.getPackage().getName().replace(".", File.separator)).mkdirs();
        // copy .class file
        Files.copy(Paths.get(root + clazz.getCanonicalName().replace(".", File.separator) + ".class"),
                Paths.get(tmp + File.separator + clazz.getCanonicalName().replace(".", File.separator) + ".class"),
                StandardCopyOption.COPY_ATTRIBUTES);
    }
}
