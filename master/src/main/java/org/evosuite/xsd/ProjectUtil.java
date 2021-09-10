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

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>ProjectUtil class</p>
 * <p>
 * Useful to get data (total, averages, etc) from a {@code Project} instance.
 *
 * @author José Campos
 */
public abstract class ProjectUtil {

    /**
     * Total Numbers
     */

    /**
     * Returns the total number of tested classes
     *
     * @param project
     * @return
     */
    public static int getTotalNumberTestedClasses(Project project) {
        return project.getCut().size();
    }

    /**
     * Returns the total number of tested classes on the latest execution
     *
     * @param project
     * @return
     */
    public static int getNumberLatestTestedClasses(Project project) {
        if (project.getCut().isEmpty()) {
            return 0;
        }

        final OptionalInt highestID = project.getCut().parallelStream()
                .mapToInt(c -> CUTUtil.getLatestGeneration(c).getId().intValue()).reduce(Integer::max);
        return (int) project.getCut().parallelStream()
                .filter(c -> CUTUtil.getTimeBudget(c, highestID.getAsInt()) > 0).count();
    }

    /**
     * Returns the total number of testable classes
     *
     * @param project
     * @return
     */
    public static int getNumberTestableClasses(Project project) {
        return project.getTotalNumberOfTestableClasses().intValue();
    }

    /**
     * Returns the total time (minutes) spent on all successful generations
     *
     * @param project
     * @return total time (minutes) or 0 if there is not any {@code CUT}
     */
    public static int getTotalEffort(Project project) {
        if (project.getCut().isEmpty()) {
            return 0;
        }

        final OptionalInt highestID = project.getCut().parallelStream()
                .mapToInt(c -> CUTUtil.getLatestGeneration(c).getId().intValue()).reduce(Integer::max);
        return (int) Math.ceil(project.getCut().parallelStream()
                .mapToDouble(c -> CUTUtil.getTotalEffort(c, highestID.getAsInt())).sum());
    }

    /**
     * Returns the total time (minutes) settled by the scheduler on all successful generations
     *
     * @param project
     * @return total time (minutes) or 0 if there is not any {@code CUT}
     */
    public static int getTimeBudget(Project project) {
        if (project.getCut().isEmpty()) {
            return 0;
        }

        final OptionalInt highestID = project.getCut().parallelStream()
                .mapToInt(c -> CUTUtil.getLatestGeneration(c).getId().intValue()).reduce(Integer::max);
        return (int) Math.ceil(project.getCut().parallelStream()
                .mapToDouble(c -> CUTUtil.getTimeBudget(c, highestID.getAsInt())).sum());
    }

    /**
     * Returns the total number of generated test suites of all successful generations
     *
     * @param project
     * @return number of generated test suites or 0 if there is not any {@code CUT}
     */
    public static int getNumberGeneratedTestSuites(Project project) {
        if (project.getCut().isEmpty()) {
            return 0;
        }

        return ProjectUtil.getAllSuccessfulGenerations(project).size();
    }

    /**
     * Returns all criteria used on all successful generations
     *
     * @param project
     * @return all criteria used or an empty Set<> if there is not any {@code CUT}
     */
    public static Set<String> getUnionCriteria(Project project) {
        Set<String> criteria = new LinkedHashSet<>();
        if (project.getCut().isEmpty()) {
            return criteria;
        }

        return ProjectUtil.getAllSuccessfulGenerations(project).parallelStream()
                .map(g -> GenerationUtil.getCriteria(g)).flatMap(s -> s.stream())
                .collect(Collectors.toSet());
    }

    /**
     * Averages
     */

    /**
     * Returns the average length (i.e., number of statements) of all successful generations
     *
     * @param project
     * @return average length or 0.0 if there is not any {@code CUT}
     */
    public static double getAverageNumberStatements(Project project) {
        if (project.getCut().isEmpty()) {
            return 0.0;
        }

        return ProjectUtil.getAllSuccessfulGenerations(project).parallelStream()
                .mapToDouble(g -> g.getSuite().getTotalNumberOfStatements().doubleValue()).sum()
                / project.getTotalNumberOfTestableClasses().doubleValue();
    }

    /**
     * Returns the overall coverage of all successful generations
     *
     * @param project
     * @return overall coverage or 0.0 if there is not any {@code CUT}
     */
    public static double getOverallCoverage(Project project) {
        if (project.getCut().isEmpty()) {
            return 0.0;
        }

        return ProjectUtil.getAllSuccessfulGenerations(project).parallelStream()
                .mapToDouble(g -> GenerationUtil.getOverallCoverage(g)).sum()
                / project.getTotalNumberOfTestableClasses().doubleValue();
    }

    /**
     * Returns the coverage of a particular criterion of all successful generations
     *
     * @param project
     * @param criterionName
     * @return average coverage of a criterion or 0.0 if there is not any {@code CUT}
     */
    public static double getAverageCriterionCoverage(Project project, String criterionName) {
        if (project.getCut().isEmpty()) {
            return 0.0;
        }

        return ProjectUtil.getAllSuccessfulGenerations(project).parallelStream()
                .mapToDouble(g -> GenerationUtil.getCriterionCoverage(g, criterionName)).sum()
                / project.getTotalNumberOfTestableClasses().doubleValue();
    }

    /**
     * Returns the average number of generated tests of all successful generations
     *
     * @param project
     * @return average number of tests or 0.0 if there is not any {@code CUT}
     */
    public static double getAverageNumberTests(Project project) {
        if (project.getCut().isEmpty()) {
            return 0.0;
        }

        return ProjectUtil.getAllSuccessfulGenerations(project).parallelStream()
                .mapToDouble(g -> g.getSuite().getNumberOfTests().doubleValue()).sum()
                / project.getTotalNumberOfTestableClasses().doubleValue();
    }

    /**
     * Aux
     */

    /**
     * Returns the {@code CUT} object of a particular class name
     *
     * @param project
     * @param className
     * @return a {@code CUT} object or null if there is not any {@code CUT} with class name
     */
    public static CUT getCUT(Project project, String className) {
        return project.getCut().parallelStream()
                .filter(p -> p.getFullNameOfTargetClass().equals(className)).findFirst().orElse(null);
    }

    /**
     * Returns all successful generations
     *
     * @param project
     * @return all successful generations or an empty Set<> if: there is not any {@code CUT}; or if
     * there is not any successful generation at all
     */
    protected static List<Generation> getAllSuccessfulGenerations(Project project) {
        List<Generation> all = new ArrayList<>();
        if (project.getCut().isEmpty()) {
            return all;
        }

        for (CUT cut : project.getCut()) {
            // search for the latest successful test generation of 'cut'
            Generation lastSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(cut);
            if (lastSuccessfulGeneration != null) {
                all.add(lastSuccessfulGeneration);
            }
        }

        return all;
    }
}
