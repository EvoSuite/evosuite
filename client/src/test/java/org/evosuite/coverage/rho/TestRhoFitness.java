package org.evosuite.coverage.rho;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

import org.evosuite.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author José Campos
 */
public class TestRhoFitness
{
    private static int NUMBER_OF_ONES = 5;
    private static int NUMBER_OF_TEST_CASES = 3;

    private static String MATRIX_CONTENT =
                    "1 0 0 1 +\n" +
                    "0 1 1 0 -\n" +
                    "0 0 1 0 +\n";

    @BeforeClass
    public static void prepare() throws IOException
    {
        Properties.REPORT_DIR = System.getProperty("java.io.tmpdir") + File.separator
                        + UUID.randomUUID().toString() + File.separator
                        + Properties.REPORT_DIR;
        Properties.TARGET_CLASS = "tmpClass";

        String path = Properties.REPORT_DIR + File.separator + "data";
        final File tmp = new File(path);
        tmp.mkdirs();

        final File matrix = new File(path + File.separator + Properties.TARGET_CLASS + ".matrix");
        matrix.createNewFile();

        FileWriter fw = new FileWriter(matrix.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(MATRIX_CONTENT);
        bw.close();
    }

    @Before
    public void reset() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        Field field = RhoCoverageFactory.class.getDeclaredField("number_of_ones");
        field.setAccessible(true);
        field.set(null, 0);

        field = RhoCoverageFactory.class.getDeclaredField("number_of_test_cases");
        field.setAccessible(true);
        field.set(null, 0);
    }

    @Test
    public void testLoadCoverageMatrix()
    {
        RhoCoverageFactory.loadCoverage();

        Assert.assertEquals(RhoCoverageFactory.getNumberOnes(), NUMBER_OF_ONES);
        Assert.assertEquals(RhoCoverageFactory.getNumberTestCases(), NUMBER_OF_TEST_CASES);
    }

    @Test
    public void testLoadCoverageMatrixWithoutPreviousCoverage()
    {
        Properties.TARGET_CLASS = "no_class";
        RhoCoverageFactory.loadCoverage();

        Assert.assertEquals(RhoCoverageFactory.getNumberOnes(), 0);
        Assert.assertEquals(RhoCoverageFactory.getNumberTestCases(), 0);
    }

    @Test
    public void testGetFitnessUsingPreviousCoverage()
    {
        // TODO
    }

    @Test
    public void testGetFitnessWithoutUsingPreviousCoverage()
    {
        // TODO
    }
}
