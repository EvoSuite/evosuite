/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.evosuite.Properties;

/**
 * 
 * @author José Campos
 */
public abstract class Metrics
{
    public static double[][] readFront(String problemName)
        throws IOException
    {
        double[][] front = new double[Properties.POPULATION][2];
        int index = 0;

        InputStream in = ClassLoader.getSystemResourceAsStream(problemName);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            String[] split = sCurrentLine.split(",");
            front[index][0] = Double.valueOf(split[0]);
            front[index][1] = Double.valueOf(split[1]);

            index++;
        }
        br.close();

        return front;
    }

    protected double euclideanDistance(double[] a, double[] b)
    {
        double distance = 0.0;
        for (int i = 0; i < a.length; i++)
            distance += Math.pow(a[i] - b[i], 2.0);

        return Math.sqrt(distance);
    }
}
