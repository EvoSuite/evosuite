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
package org.evosuite.xsd;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>CUTUtil class</p>
 * <p>
 * Useful to get data (total, averages, etc) from a {@code CUT} instance.
 *
 * @author José Campos
 */
public abstract class CUTUtil {

    /**
     * Total Numbers
     */

    /**
     * Returns the total length (i.e., number of statements) of the latest successful generation
     *
     * @param cut
     * @return total length or 0 if there is not any successful generation for {@code CUT}
     */
    public static int getNumberStatements(CUT cut) {
        Generation latestSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(cut);
        if (latestSuccessfulGeneration == null) {
            return 0;
        }

        return GenerationUtil.getNumberStatements(latestSuccessfulGeneration);
    }

    /**
     * Returns the total time (minutes) spent on the latest generation
     *
     * @param cut
     * @return
     */
    public static int getTotalEffort(CUT cut) {
        Generation latestSuccessfulGeneration = CUTUtil.getLatestGeneration(cut);
        return GenerationUtil.getTotalEffort(latestSuccessfulGeneration);
    }

    /**
     * Returns the total time (minutes) settled by the scheduler on the latest generation
     *
     * @param cut
     * @return
     */
    public static int getTimeBudget(CUT cut) {
        Generation latestSuccessfulGeneration = CUTUtil.getLatestGeneration(cut);
        return GenerationUtil.getTimeBudget(latestSuccessfulGeneration);
    }

    /**
     * Returns the total time (minutes) spent on a generation with an ID equal to generationID
     *
     * @param cut
     * @param generationID
     * @return total time (minutes) spent on a generation with ID equal to generationID, or 0 if there
     * is not a generation with the ID equal to generationID
     */
    public static int getTotalEffort(CUT cut, int generationID) {
        if (generationID >= cut.getGeneration().size()) {
            return 0;
        }

        Generation generation = cut.getGeneration().get(generationID);
        return GenerationUtil.getTotalEffort(generation);
    }

    /**
     * Returns the total time (minutes) settled by the scheduler on a generation with an ID equal to
     * generationID
     *
     * @param cut
     * @param generationID
     * @return total time (minutes) settled by the scheduler on a generation with ID equal to
     * generationID, or 0 if there is not a generation with the ID equal to generationID
     */
    public static int getTimeBudget(CUT cut, int generationID) {
        if (generationID >= cut.getGeneration().size()) {
            return 0;
        }

        Generation generation = cut.getGeneration().get(generationID);
        return GenerationUtil.getTimeBudget(generation);
    }

    /**
     * Returns the total number of generated tests of the latest successful generation
     *
     * @param cut
     * @return total number of tests or 0 if there is not any successful generation for {@code CUT}
     */
    public static int getNumberTests(CUT cut) {
        Generation latestSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(cut);
        if (latestSuccessfulGeneration == null) {
            return 0;
        }

        return GenerationUtil.getNumberTests(latestSuccessfulGeneration);
    }

    /**
     * Returns all criteria used on the latest successful generation
     *
     * @param cut
     * @return all criteria used or an empty Set<> if there is not any successful generation for {@code CUT}
     */
    public static Set<String> getCriteria(CUT cut) {
        Generation latestSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(cut);
        if (latestSuccessfulGeneration == null) {
            return new HashSet<>();
        }

        return GenerationUtil.getCriteria(latestSuccessfulGeneration);
    }

    /**
     * Returns the coverage of a particular criterion of the latest successful generation
     *
     * @param cut
     * @param criterionName
     * @return coverage of a criterion or 0.0 if there is not any successful generation for {@code CUT}
     */
    public static double getCriterionCoverage(CUT cut, String criterionName) {
        Generation latestSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(cut);
        if (latestSuccessfulGeneration == null) {
            return 0.0;
        }

        return GenerationUtil.getCriterionCoverage(latestSuccessfulGeneration, criterionName);
    }

    /**
     * Averages
     */

    /**
     * Returns the overall coverage of the latest successful generation
     *
     * @param cut
     * @return overall coverage or or 0.0 if there is not any successful generation for {@code CUT}
     */
    public static double getOverallCoverage(CUT cut) {
        Generation latestSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(cut);
        if (latestSuccessfulGeneration == null) {
            return 0.0;
        }

        return GenerationUtil.getOverallCoverage(latestSuccessfulGeneration);
    }

    /**
     * Aux
     */

    /**
     * Returns the latest test generation
     *
     * @param cut
     * @return
     */
    public static Generation getLatestGeneration(CUT cut) {
        return cut.getGeneration().get(cut.getGeneration().size() - 1);
    }

    /**
     * Returns the latest successful test generation
     *
     * @param cut
     * @return the latest successful test generation or null if: one of the latest generation failed
     * and the class was modified; or if there is not any successful generation for {@code CUT}
     */
    public static Generation getLatestSuccessfulGeneration(CUT cut) {
        for (int i = cut.getGeneration().size() - 1; i >= 0; i--) {
            Generation g = cut.getGeneration().get(i);

            // if a test generation failed and the class under test
            // was modified, we can argue that there is not a valid
            // last successful generation
            if (g.isFailed() && g.isModified()) {
                return null;
            } else if (!g.isFailed() && g.getSuite() != null) {
                // however, if there is generation that ended successfully
                // and has a test suite, return it.
                return g;
            }
        }

        return null;
    }
}
