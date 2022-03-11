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
package org.evosuite.coverage.rho;

import org.evosuite.Properties;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparingInt;

/**
 * @author José Campos
 */
public class RhoCoverageFactory extends
        AbstractFitnessFactory<LineCoverageTestFitness> implements Serializable {

    private static final long serialVersionUID = -4124074445663735815L;

    private static final Logger logger = LoggerFactory.getLogger(RhoCoverageFactory.class);


    private static List<LineCoverageTestFitness> goals = new ArrayList<>();

    /**
     * Variables to calculate Rho value
     */
    private static int number_of_ones = 0;
    private static int number_of_test_cases = 0;


    private static double rho = 1.0;


    private static final List<List<Integer>> matrix = new ArrayList<>();

    /**
     * Read the coverage of a test suite from a file
     */
    protected static void loadCoverage() {

        if (!new File(Properties.COVERAGE_MATRIX_FILENAME).exists()) {
            return;
        }

        BufferedReader br = null;

        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(Properties.COVERAGE_MATRIX_FILENAME));

            String[] split;
            while ((sCurrentLine = br.readLine()) != null) {
                split = sCurrentLine.split(" ");

                List<Integer> test = new ArrayList<>();
                for (int i = 0; i < split.length - 1; i++) { // - 1, because we do not want to consider test result
                    if (split[i].compareTo("1") == 0) {
                        test.add(goals.get(i).getLine());
                    }
                }

                matrix.add(test);
                number_of_ones += test.size();
                number_of_test_cases++;
            }

            rho = ((double) number_of_ones) / ((double) number_of_test_cases) / ((double) goals.size());
            logger.debug("RhoScore of an existing test suite: " + rho);

            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.RhoScore_T0, rho);
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Size_T0, number_of_test_cases);

            rho = Math.abs(0.5 - rho);
            logger.debug("(RhoScore - 0.5) of an existing test suite: " + rho);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public List<LineCoverageTestFitness> getCoverageGoals() {
        return getGoals();
    }

    /**
     * @return
     */
    public static List<LineCoverageTestFitness> getGoals() {

        if (!goals.isEmpty()) {
            return goals;
        }

        goals = RhoAux.getLineGoals();
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());

        if (Properties.USE_EXISTING_COVERAGE) {
            // extremely important: before loading any previous coverage (i.e., from a coverage
            // matrix) goals need to be sorted. otherwise any previous coverage won't match!
            goals.sort(comparingInt(LineCoverageTestFitness::getLine));
            loadCoverage();
        } else {
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.RhoScore_T0, 1.0);
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Size_T0, number_of_test_cases);
        }

        return goals;
    }

    /**
     * @return
     * @throws Exception
     */
    public static int getNumberGoals() {
        if (goals.isEmpty()) {
            getGoals();
        }
        return goals.size();
    }

    /**
     * @return
     */
    public static int getNumber_of_Ones() {
        return number_of_ones;
    }

    /**
     * @return
     */
    public static int getNumber_of_Test_Cases() {
        assert (number_of_test_cases == matrix.size());
        return number_of_test_cases;
    }

    /**
     * @return
     */
    public static double getRho() {
        return rho;
    }

    /**
     * @return
     */
    public static List<List<Integer>> getMatrix() {
        return matrix;
    }

    /**
     * @param newTest
     * @return
     */
    public static boolean exists(List<Integer> newTest) {
        return matrix.contains(newTest);
    }

    // only for testing
    protected static void reset() {
        goals.clear();
        number_of_ones = 0;
        number_of_test_cases = 0;
        rho = 1.0;
        matrix.clear();
    }
}
