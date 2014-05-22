package org.evosuite.ga.problems.metrics;

/**
 * Spacing
 * 
 * @inproceedings{Van:2000,
                  author={Van Veldhuizen, D.A. and Lamont, G.B.},
                  booktitle={Evolutionary Computation, 2000. Proceedings of the 2000 Congress on},
                  title={{On Measuring Multiobjective Evolutionary Algorithm Performance}},
                  year={2000},
                  month={},
                  volume={1},
                  pages={204-211},
                  doi={10.1109/CEC.2000.870296}}
 *
 * @author José Campos
 */
public class Spacing extends Metrics
{
    public double evaluate(double[][] front)
    {
        double d[] = new double[front.length];
        double dbar = 0.0;

        for (int i = 0; i < front.length; i++)
        {
            double min = Double.POSITIVE_INFINITY;

            for (int j = 0; j < front.length; j++)
            {
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
