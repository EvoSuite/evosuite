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
package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.Properties;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;

public class TestCaseSecondaryObjective {

    public static void setSecondaryObjectives() {
        for (Properties.SecondaryObjective secondaryObjective : Properties.SECONDARY_OBJECTIVE) {
            try {
                SecondaryObjective<TestChromosome> secondaryObjectiveInstance = null;
                switch (secondaryObjective) {
                    case AVG_LENGTH:
                    case MAX_LENGTH:
                    case TOTAL_LENGTH:
                        secondaryObjectiveInstance = new MinimizeLengthSecondaryObjective();
                        break;
                    case EXCEPTIONS:
                        secondaryObjectiveInstance = new MinimizeExceptionsSecondaryObjective();
                        break;
                    default:
                        throw new RuntimeException("ERROR: asked for unknown secondary objective \""
                                + secondaryObjective.name() + "\"");
                }
                TestChromosome.addSecondaryObjective(secondaryObjectiveInstance);
            } catch (Throwable t) {
            } // Not all objectives make sense for tests
        }
    }
}
