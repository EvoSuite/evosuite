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
package org.evosuite.ga.problems.metrics;

/**
 * Spacing
 *
 * @author José Campos
 * @inproceedings{Van:2000, author={Van Veldhuizen, D.A. and Lamont, G.B.},
 * booktitle={Evolutionary Computation, 2000. Proceedings of the 2000 Congress on},
 * title={{On Measuring Multiobjective Evolutionary Algorithm Performance}},
 * year={2000},
 * month={},
 * volume={1},
 * pages={204-211},
 * doi={10.1109/CEC.2000.870296}}
 */
public class Spacing extends Metrics {
    public double evaluate(double[][] front) {
        double[] d = new double[front.length];
        double dbar = 0.0;

        for (int i = 0; i < front.length; i++) {
            double min = Double.POSITIVE_INFINITY;

            for (int j = 0; j < front.length; j++) {
                if (i == j)
                    continue;

                min = Math.min(min, this.euclideanDistance(front[i], front[j]));
            }

            d[i] = min;
            dbar += min;
        }

        double sum = 0.0;
        for (int i = 0; i < front.length; i++)
            sum += Math.pow(d[i] - dbar, 2.0);

        double spacing = Math.sqrt(sum / (front.length - 1));
        return spacing;
    }
}
