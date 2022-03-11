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
import java.util.stream.Collectors;

/**
 * <p>GenerationUtil class</p>
 * <p>
 * Useful to get data (total, averages, etc) from a {@code Generation} instance.
 *
 * @author José Campos
 */
public abstract class GenerationUtil {

    /**
     * Total Numbers
     */

    /**
     * Returns the total length (i.e., number of statements) of a successful generation
     *
     * @param generation
     * @return total length or 0 if the generation failed
     */
    public static int getNumberStatements(Generation generation) {
        if (generation == null || generation.isFailed() || generation.getSuite() == null) {
            return 0;
        }

        return generation.getSuite().getTotalNumberOfStatements().intValue();
    }

    /**
     * Returns the total time (minutes) spent on a generation
     *
     * @param generation
     * @return total time (minutes) spent on a generation or 0 if the generation failed
     */
    public static int getTotalEffort(Generation generation) {
        if (generation == null || generation.isFailed() || generation.getSuite() == null) {
            return 0;
        }

        return (int) Math.ceil(generation.getSuite().getTotalEffortInSeconds().doubleValue() / 60.0);
    }

    /**
     * Returns the total time (minutes) settled by the scheduler
     *
     * @param generation
     * @return total time (minutes) settled by the scheduler or 0 if the generation failed
     */
    public static int getTimeBudget(Generation generation) {
        if (generation == null) {
            return 0;
        }

        return (int) Math.ceil(generation.getTimeBudgetInSeconds().doubleValue() / 60.0);
    }

    /**
     * Returns the total number of generated tests of a successful generation
     *
     * @param generation
     * @return total number of tests or 0 if the generation failed
     */
    public static int getNumberTests(Generation generation) {
        if (generation == null || generation.isFailed() || generation.getSuite() == null) {
            return 0;
        }

        return generation.getSuite().getNumberOfTests().intValue();
    }

    /**
     * Returns all criteria used on a successful generation
     *
     * @param generation
     * @return all criteria used or an empty Set<> if the generation failed
     */
    public static Set<String> getCriteria(Generation generation) {
        if (generation == null || generation.isFailed() || generation.getSuite() == null) {
            return new HashSet<>();
        }

        return generation.getSuite().getCoverage().parallelStream()
                .map(c -> c.getCriterion())
                .collect(Collectors.toSet());
    }

    /**
     * Returns the coverage of a particular criterion of a successful generation
     *
     * @param generation
     * @param criterionName
     * @return coverage of a criterion or 0.0 if the generation failed
     */
    public static double getCriterionCoverage(Generation generation, String criterionName) {
        if (generation == null || generation.isFailed() || generation.getSuite() == null) {
            return 0.0;
        }

        for (Coverage coverage : generation.getSuite().getCoverage()) {
            if (coverage.getCriterion().equals(criterionName)) {
                return coverage.getCoverageValue();
            }
        }

        return 0.0; // criterionName not found
    }

    /**
     * Averages
     */

    /**
     * Returns the overall coverage of a successful generation
     *
     * @param generation
     * @return overall coverage or 0.0 if the generation failed
     */
    public static double getOverallCoverage(Generation generation) {
        if (generation == null || generation.isFailed() || generation.getSuite() == null) {
            return 0.0;
        }

        return generation.getSuite().getCoverage().parallelStream()
                .mapToDouble(c -> c.getCoverageValue())
                .average()
                .getAsDouble();
    }
}
