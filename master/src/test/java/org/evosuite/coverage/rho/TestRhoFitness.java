package org.evosuite.coverage.rho;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.Properties.Strategy;
import org.evosuite.Properties.TestFactory;
import org.evosuite.SystemTest;
import org.evosuite.statistics.RuntimeVariable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.Compositional;

import au.com.bytecode.opencsv.CSVReader;

public class TestRhoFitness extends SystemTest {

	private void writeMatrix(String MATRIX_CONTENT) {
		String path = Properties.REPORT_DIR + File.separator;
		final File tmp = new File(path);
		tmp.mkdirs();

		try {
			final File matrix = new File(path + File.separator + Properties.TARGET_CLASS + ".matrix");
			matrix.createNewFile();

			FileWriter fw = new FileWriter(matrix.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(MATRIX_CONTENT);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Before
	public void prepare() {
		RhoCoverageFactory.reset();
		try {
			FileUtils.deleteDirectory(new File("evosuite-report"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Properties.CRITERION = new Properties.Criterion[] {
			Properties.Criterion.RHO
		};
		Properties.STRATEGY = Strategy.ENTBUG;
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 60;

		Properties.TEST_ARCHIVE = false;
		Properties.TEST_FACTORY = TestFactory.RANDOM;
		Properties.MINIMIZE = false;
		Properties.MINIMIZE_VALUES = false;
		Properties.INLINE = false;
		Properties.ASSERTIONS = false;
		Properties.USE_EXISTING_COVERAGE = false;
	}

	@Test
	public void testZeroRhoScoreWithoutPreviousCoverage() throws IOException {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = Compositional.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.OUTPUT_VARIABLES = RuntimeVariable.RhoScore.name();
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

		String[] command = new String[] {
			"-class", targetClass,
			"-generateTests"
		};

		Object result = evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);

		List<?> goals = RhoCoverageFactory.getGoals();
		assertEquals(11, goals.size());

		String statistics_file = System.getProperty("user.dir") + File.separator + Properties.REPORT_DIR + File.separator + "statistics.csv";

		CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertTrue(rows.size() == 2);
        reader.close();

        assertEquals("0.5", rows.get(1)[0]);
	}

	@Test
	public void testZeroRhoScoreWithPreviousCoverage() throws IOException {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = Compositional.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		String previous_tmp_coverage =
			"1 1 1 1 1 1 1 1 1 1 1 +\n" +
			"1 1 1 1 0 0 0 0 0 0 0 +\n";
		this.writeMatrix(previous_tmp_coverage);
		Properties.USE_EXISTING_COVERAGE = true;

		Properties.OUTPUT_VARIABLES = RuntimeVariable.RhoScore.name();
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

		String[] command = new String[] {
			"-class", targetClass,
			"-generateTests"
		};

		Object result = evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);

		List<?> goals = RhoCoverageFactory.getGoals();
		assertEquals(11, goals.size());
		assertEquals(15, RhoCoverageFactory.getNumber_of_Ones());
		assertEquals(2, RhoCoverageFactory.getNumber_of_Test_Cases());
		assertEquals((15.0 / 11.0 / 2.0) - 0.5, RhoCoverageFactory.getRho(), 0.0001);

		String statistics_file = System.getProperty("user.dir") + File.separator + Properties.REPORT_DIR + File.separator + "statistics.csv";

		CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertTrue(rows.size() == 2);
        reader.close();

        assertEquals("0.5", rows.get(1)[0]);
	}
}
