/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.ambiguity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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
public class TestAmbiguityFitness
{
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
        Field field = AmbiguityCoverageFactory.class.getDeclaredField("transposedMatrix");
        field.setAccessible(true);
        field.set(null, new ArrayList<StringBuilder>());
    }

    @Test
    public void testTransposedMatrix()
    {
        AmbiguityCoverageFactory.loadCoverage();
        List<StringBuilder> transposedMatrix = AmbiguityCoverageFactory.getTransposedMatrix();

        Assert.assertEquals(transposedMatrix.size(), 4);
        Assert.assertEquals(transposedMatrix.get(0).toString(), "100");
        Assert.assertEquals(transposedMatrix.get(1).toString(), "010");
        Assert.assertEquals(transposedMatrix.get(2).toString(), "011");
        Assert.assertEquals(transposedMatrix.get(3).toString(), "100");
    }

    @Test
    public void testTransposedMatrixWithoutPreviousCoverage()
    {
        Properties.TARGET_CLASS = "no_class";
        AmbiguityCoverageFactory.loadCoverage();
        List<StringBuilder> transposedMatrix = AmbiguityCoverageFactory.getTransposedMatrix();

        Assert.assertEquals(transposedMatrix.size(), 0);
    }

    @Test
    public void testAmbiguity()
    {
        AmbiguityCoverageFactory.loadCoverage();
        List<StringBuilder> matrix = AmbiguityCoverageFactory.getTransposedMatrix();

        Assert.assertEquals(matrix.size(), 4);
        Assert.assertEquals(AmbiguityCoverageFactory.getAmbiguity(matrix), 0.25, 0.00);
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
