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
package org.evosuite.testsuite.similarity;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.*;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by gordon on 18/12/2015.
 */
public class DiversityObserver implements SearchListener {

    private static final Logger logger = LoggerFactory.getLogger(DiversityObserver.class);

    @Override
    public void iteration(GeneticAlgorithm<?> algorithm) {
        List<TestSuiteChromosome> individuals = (List<TestSuiteChromosome>) algorithm.getPopulation();
        double diversity = 0.0;
        int numComparisons = 0;
        for(int i = 0; i < individuals.size() - 1; i++) {
            for(int j = i+1; j < individuals.size(); j++) {
                double pairDiversity = getSuiteSimilarity(individuals.get(i), individuals.get(j));
                logger.debug("Adding diversity of pair "+i+", "+j+" of "+pairDiversity);
                diversity += pairDiversity;
                numComparisons += 1;
            }
        }
        diversity = 1.0 - diversity/numComparisons;
        logger.info("Resulting diversity for "+numComparisons +" pairs: "+diversity);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.DiversityTimeline, diversity);

    }

    public static final int GAP_PENALTY = -2;

    /**
     * Naive similarity comparison between suites simply consists of merging all tests to a single test
     * for each suite, and then comparing these tests
     *
     * @param suite1
     * @param suite2
     * @return
     */
    public static double getSuiteSimilarity(TestSuiteChromosome suite1, TestSuiteChromosome suite2) {
        TestCase test1 = new DefaultTestCase();
        for(TestCase test : suite1.getTests()) {
            for(Statement s : test) {
                // These are not valid tests as the variables still point to the original test
                // but that doesn't matter as we're not executing the test
                test1.addStatement(s);
            }
        }
        TestCase test2 = new DefaultTestCase();
        for(TestCase test : suite2.getTests()) {
            for(Statement s : test) {
                test2.addStatement(s);
            }
        }

        return getNeedlemanWunschScore(test1, test2);
    }

    // TODO: Similarity based on vectors of types of calls

    /**
     * Sequence alignment based distance
     * @param test1
     * @param test2
     * @return
     */
    public static double getNeedlemanWunschScore(TestCase test1, TestCase test2) {
        int[][] matrix = new int[test1.size()+1][test2.size()+1];

        for(int i = 0; i <= test1.size(); i++)
            matrix[i][0] = GAP_PENALTY * i;

        for(int i = 0; i <= test2.size(); i++)
            matrix[0][i] = GAP_PENALTY * i;

        for(int x = 1; x <= test1.size(); x++) {
            for(int y = 1; y <= test2.size(); y++) {
                int upLeft = matrix[x-1][y-1] + getStatementSimilarity(test1.getStatement(x-1), test2.getStatement(y-1));
                int insert = matrix[x-1][y] + GAP_PENALTY;
                int delete = matrix[x][y-1] + GAP_PENALTY;
                matrix[x][y]= Math.max(upLeft, Math.max(delete, insert));
            }
        }
//        printMatrix(matrix);

        // Normalize
        double max = Math.max(test1.size(), test2.size()) * Math.abs(GAP_PENALTY); // max +
        if(max == 0.0) {
            return 0.0;
        }
        return matrix[test1.size()][test2.size()] / max;
    }

    // matches are given +1, mismatches are given -
    private static int getStatementSimilarity(Statement s1, Statement s2) {
        int similarity = 0;
        if(s1.getClass() == s2.getClass()) {
            similarity += 1;
            if(s1 instanceof ConstructorStatement) {
                if(getUnderlyingType((ConstructorStatement) s1).equals(getUnderlyingType((ConstructorStatement) s2)))
                    similarity += 1;
            } else if(s1 instanceof PrimitiveStatement) {
                if(getUnderlyingType((PrimitiveStatement) s1).equals(getUnderlyingType((PrimitiveStatement) s2)))
                    similarity += 1;
            } else if(s1 instanceof MethodStatement) {
                if(getUnderlyingType((MethodStatement) s1).equals(getUnderlyingType((MethodStatement) s2)))
                    similarity += 1;
            } else if(s1 instanceof FieldStatement) {
                if(getUnderlyingType((FieldStatement) s1).equals(getUnderlyingType((FieldStatement) s2)))
                    similarity += 1;
            }
            // TOOD: If underlying type is the same, further benefit
        }
        else {
            similarity = -2;
        }

        return similarity;
    }

    private static Class<?> getUnderlyingType(ConstructorStatement cs) {
        return cs.getReturnClass();
    }

    private static Class<?> getUnderlyingType(MethodStatement ms) {
        return ms.getMethod().getDeclaringClass();
    }

    private static Class<?> getUnderlyingType(FieldStatement fs) {
        return fs.getField().getDeclaringClass();
    }

    private static Class<?> getUnderlyingType(PrimitiveStatement ps) {
        return ps.getReturnClass();
    }


    public static void printMatrix(int[][] matrix) {
        for(int x = 0; x < matrix.length; x++) {
            for(int y = 0; y < matrix[x].length; y++) {
                System.out.print(" "+matrix[x][y]);
            }
            System.out.println();
        }
    }

    @Override
    public void searchStarted(GeneticAlgorithm<?> algorithm) {

    }

    @Override
    public void searchFinished(GeneticAlgorithm<?> algorithm) {

    }

    @Override
    public void fitnessEvaluation(Chromosome individual) {

    }

    @Override
    public void modification(Chromosome individual) {

    }
}
