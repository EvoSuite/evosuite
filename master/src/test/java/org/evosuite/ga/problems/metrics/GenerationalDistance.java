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
 * Generational Distance
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
public class GenerationalDistance extends Metrics {
    private static int P = 2;

    /**
     * Gets the distance between a point and the nearest one in a given front
     *
     * @param point a point
     * @param front the front that contains the other points to calculate the distances
     * @return the minimun distance between the point and the front
     **/
    private double distanceToClosedPoint(double[] point, double[][] front) {
        double minDistance = this.euclideanDistance(point, front[0]);

        for (int i = 1; i < front.length; i++) {
            double aux = this.euclideanDistance(point, front[i]);
            if (aux < minDistance)
                minDistance = aux;
        }

        return minDistance;
    }

    /**
     * Returns the generational distance value for a given front
     *
     * @param front           the front
     * @param trueParetoFront the true pareto front
     */
    public double evaluate(double[][] front, double[][] trueParetoFront) {
        double[] maximumValue;
        double[] minimumValue;

        double[][] normalizedFront;
        double[][] normalizedParetoFront;

        maximumValue = this.getMaximumValues(trueParetoFront);
        minimumValue = this.getMinimumValues(trueParetoFront);

        normalizedFront = this.getNormalizedFront(front, maximumValue, minimumValue);
        normalizedParetoFront = this.getNormalizedFront(trueParetoFront, maximumValue, minimumValue);

        double sum = 0.0;
        for (int i = 0; i < front.length; i++)
            sum += Math.pow(this.distanceToClosedPoint(normalizedFront[i], normalizedParetoFront), P);

        sum = Math.pow(sum, 1.0 / P);

        double generationalDistance = sum / normalizedFront.length;
        return generationalDistance;
    }
}
