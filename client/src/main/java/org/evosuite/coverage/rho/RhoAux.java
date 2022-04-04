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
import org.evosuite.instrumentation.LinePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * RhoAux class.
 *
 * @author José Campos
 */
public class RhoAux {

    private static final Logger logger = LoggerFactory.getLogger(RhoAux.class);

    private static boolean isCUTorNot(String className) {
        return Properties.TARGET_CLASS.equals("") || (className.equals(Properties.TARGET_CLASS)
                || className.startsWith(Properties.TARGET_CLASS + "$"));
    }

    /**
     * Returns the list of lines goals of the CUT
     *
     * @return
     */
    public static List<LineCoverageTestFitness> getLineGoals() {
        List<LineCoverageTestFitness> goals = new ArrayList<>();

    /*TestFitnessFactory<? extends TestFitnessFunction> factory = FitnessFunctions.getFitnessFactory(Properties.Criterion.ONLYLINE);
    for (TestFitnessFunction goal : factory.getCoverageGoals()) {
        goals.add((LineCoverageTestFitness) goal);
    }*/

        for (String className : LinePool.getKnownClasses()) {
            // Only lines in CUT
            if (!isCUTorNot(className)) {
                continue;
            }

            Set<Integer> lines = new LinkedHashSet<>();
            for (String methodName : LinePool.getKnownMethodsFor(className)) {
                lines.addAll(LinePool.getLines(className, methodName));
            }

            for (Integer line : lines) {
                logger.info("Adding line " + line + " for class '" + className + "'");
                // Properties.TARGET_METHOD as to be used instead of methodName, otherwise
                // an CFG exception would be thrown
                goals.add(new LineCoverageTestFitness(className, Properties.TARGET_METHOD, line));
            }
        }

        return goals;
    }
}
