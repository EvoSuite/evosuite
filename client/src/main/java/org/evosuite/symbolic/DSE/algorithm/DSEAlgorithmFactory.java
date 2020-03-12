/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.DSE.algorithm;

import org.evosuite.coverage.FitnessFunctionsUtils;
import org.evosuite.symbolic.DSE.DSEStatistics;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.KeepSearchingCriteriaStrategies.LastExecutionCreatedATestCaseStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathPruningStrategies.AlreadySeenSkipStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathSelectionStrategies.generationalGenerationStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.TestCaseBuildingStrategies.DefaultTestCaseBuildingStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.TestCaseSelectionStrategies.LastTestCaseSelectionStrategy;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.List;

/**
 * Factory of DSE Algorithms
 * Please add a citation to the paper / source of information from which the algorithm is based.
 *
 * @author ilebrero
 */
public class DSEAlgorithmFactory {
    private final static String DSE_ALGORITHM_TYPE_NOT_PROVIDED = "A DSE algorithm type must be provided";

    /**
    * Statistics object for when creating a customized algorithm
    */
    private final DSEStatistics dseStatistics = DSEStatistics.getInstance();

    public DSEAlgorithm getDSEAlgorithm(DSEAlgorithms dseAlgorithmType) {
        if (dseAlgorithmType == null) {
            throw new IllegalArgumentException(DSE_ALGORITHM_TYPE_NOT_PROVIDED);
        }

        switch (dseAlgorithmType) {
            case SAGE:
                return buildSAGEAlgorithm();
            default:
                throw new IllegalStateException("DSEAlgorithm not yet implemented: " + dseAlgorithmType.name());
        }
    }

    /**
     * Default version of the DSE algorithm.
     * Based on Godefroid P., Levin Y. M. & Molnar D. (2008) Automated Whitebox Fuzz Testing
     *
     * OBS: the only difference is that we model the incremental block coverage as incremental line coverage.
     *
     * @return
     */
    private DSEAlgorithm buildSAGEAlgorithm() {
        DSEAlgorithm algorithm = new DSEAlgorithm(
            new AlreadySeenSkipStrategy(),
            new LastExecutionCreatedATestCaseStrategy(),
            new generationalGenerationStrategy(),
            new DefaultTestCaseBuildingStrategy(),
            new LastTestCaseSelectionStrategy()
        );

        List<TestSuiteFitnessFunction> sageFitnessFunctions = FitnessFunctionsUtils.getFitnessFunctions(DSEAlgorithms.SAGE.getCriteria());
        algorithm.addFitnessFunctions(sageFitnessFunctions);

        return algorithm;
    }
}

