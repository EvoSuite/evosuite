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
package org.evosuite.ga.operators.crossover;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.variables.DoubleVariable;
import org.evosuite.ga.variables.Variable;
import org.evosuite.utils.Randomness;

/**
 * Simulated Binary Crossover (SBX)
 *
 * @author José Campos
 */
public class SBXCrossover extends CrossOverFunction<NSGAChromosome> {
    private static final long serialVersionUID = -4258729002155733390L;

    /**
     * EPS defines the minimum difference allowed between real values
     */
    private static final double EPS = 1e-10;

    @Override
    public void crossOver(NSGAChromosome p1, NSGAChromosome p2)
            throws ConstructionFailedException {

        for (int i = 0; i < p1.getNumberOfVariables(); i++) {
            Variable v1 = p1.getVariable(i);
            Variable v2 = p2.getVariable(i);

            if ((v1 instanceof DoubleVariable) && (v2 instanceof DoubleVariable))
                this.doCrossover((DoubleVariable) v1, (DoubleVariable) v2);
        }
    }

    private void doCrossover(DoubleVariable v1, DoubleVariable v2) {
        double distributionIndex = 20.0;

        double rand;
        double y1, y2, yL, yu;
        double c1, c2;
        double alpha, beta, betaq;
        double valueX1, valueX2;

        yL = v1.getLowerBound();
        yu = v1.getUpperBound();

        valueX1 = v1.getValue();
        valueX2 = v2.getValue();

        if (Math.abs(valueX1 - valueX2) > EPS) {
            if (valueX1 < valueX2) {
                y1 = valueX1;
                y2 = valueX2;
            } else {
                y1 = valueX2;
                y2 = valueX1;
            }

            rand = Randomness.nextDouble();
            beta = 1.0 + (2.0 * (y1 - yL) / (y2 - y1));
            alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

            if (rand <= (1.0 / alpha))
                betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
            else
                betaq = Math.pow((1.0 / (2.0 - rand * alpha)), (1.0 / (distributionIndex + 1.0)));

            c1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
            beta = 1.0 + (2.0 * (yu - y2) / (y2 - y1));
            alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

            if (rand <= (1.0 / alpha))
                betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
            else
                betaq = Math.pow((1.0 / (2.0 - rand * alpha)), (1.0 / (distributionIndex + 1.0)));

            c2 = 0.5 * ((y1 + y2) + betaq * (y2 - y1));

            if (c1 < yL)
                c1 = yL;
            if (c2 < yL)
                c2 = yL;

            if (c1 > yu)
                c1 = yu;
            if (c2 > yu)
                c2 = yu;

            if (Randomness.nextDouble() <= 0.5) {
                valueX1 = c2;
                valueX2 = c1;
            } else {
                valueX1 = c1;
                valueX2 = c2;
            }

            v1.setValue(valueX1);
            v2.setValue(valueX2);
        }
    }
}
