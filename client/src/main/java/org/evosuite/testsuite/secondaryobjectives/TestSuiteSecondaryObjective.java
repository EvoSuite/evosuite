/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testsuite.secondaryobjectives;

import org.evosuite.Properties;
import org.evosuite.coverage.ibranch.IBranchSecondaryObjective;
import org.evosuite.coverage.rho.RhoTestSuiteSecondaryObjective;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testsuite.TestSuiteChromosome;


public class TestSuiteSecondaryObjective {


    public static void setSecondaryObjectives() {
        for (Properties.SecondaryObjective secondaryObjective : Properties.SECONDARY_OBJECTIVE) {
            SecondaryObjective<TestSuiteChromosome> secondaryObjectiveInstance = null;
            switch (secondaryObjective) {
                case AVG_LENGTH:
                    secondaryObjectiveInstance = new MinimizeAverageLengthSecondaryObjective();
                    break;
                case MAX_LENGTH:
                    secondaryObjectiveInstance = new MinimizeMaxLengthSecondaryObjective();
                    break;
                case TOTAL_LENGTH:
                    secondaryObjectiveInstance = new MinimizeTotalLengthSecondaryObjective();
                    break;
                case EXCEPTIONS:
                    secondaryObjectiveInstance = new MinimizeExceptionsSecondaryObjective();
                    break;
                case SIZE:
                    secondaryObjectiveInstance = new MinimizeSizeSecondaryObjective();
                    break;
                case IBRANCH:
                    secondaryObjectiveInstance = new IBranchSecondaryObjective();
                    break;
                case RHO:
                    secondaryObjectiveInstance = new RhoTestSuiteSecondaryObjective();
                    break;
                default:
                    throw new RuntimeException(
                            "ERROR: asked for unknown secondary objective \"" + secondaryObjective.name() + "\"");
            }
            TestSuiteChromosome.addSecondaryObjective(secondaryObjectiveInstance);
        }
    }
}
