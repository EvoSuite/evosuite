package org.evosuite.ga.operators.selection;

import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.utils.Randomness;

/**
 * This class implements an operator for binary selections using the same code in Deb's NSGA-II implementation
 */
public class BinaryTournament<T extends Chromosome>
    extends SelectionFunction<T>
{
    /**
     * 
     */
    private static final long serialVersionUID = -6853195126789856216L;

    /**
     * a_ stores a permutation of the solutions in the solutionSet used
     */
    private int a_[];

    /**
     * index_ stores the actual index for selection
     */
    private int index_ = 0;

    /**
     * Performs the operation
     * 
     * @param object Object representing a SolutionSet
     * @return the selected solution
     */
    @Override
    public int getIndex(List<T> population)
    {
        if (index_ == 0) // Create the permutation
            a_ = intPermutation(population.size());

        int index1 = index_;
        T solution1 = population.get(a_[index1]);
        int index2 = index_ + 1;
        T solution2 = population.get(a_[index2]);

        index_ = (index_ + 2) % population.size();

        int flag = dominanceComparator(solution1, solution2);
        if (flag == -1)
            return index1;
        else if (flag == 1)
            return index2;
        else if (solution1.getDistance() > solution2.getDistance())
            return index1;
        else if (solution2.getDistance() > solution1.getDistance())
            return index2;
        else if (Randomness.nextDouble() < 0.5)
            return index1;
        else
            return index2;
    }

    /**
     * Compares two solutions
     * 
     * @param object1 Object representing the first <code>Solution</code>.
     * @param object2 Object representing the second <code>Solution</code>.
     * @return -1, or 0, or 1 if solution1 dominates solution2, both are non-dominated, or solution1 is dominated by
     *         solution22, respectively.
     */
    private int dominanceComparator(T solution1, T solution2)
    {
        int dominate1; // dominate1 indicates if some objective of solution1
                       // dominates the same objective in solution2. dominate2
        int dominate2; // is the complementary of dominate1.

        dominate1 = 0;
        dominate2 = 0;

        int flag; // stores the result of the comparison
        double value1, value2;

        for (FitnessFunction<?> ff : solution1.getFitnesses().keySet())
        {
            value1 = solution1.getFitness(ff);
            value2 = solution2.getFitness(ff);
            if (value1 < value2)
                flag = -1;
            else if (value1 > value2)
                flag = 1;
            else
                flag = 0;

            if (flag == -1)
                dominate1 = 1;
            if (flag == 1)
                dominate2 = 1;
        }

        if (dominate1 == dominate2)
            return 0; // No one dominate the other
        if (dominate1 == 1)
            return -1; // solution1 dominate
        return 1; // solution2 dominate
    }

    /**
     * Returns a permutation vector between the 0 and (length - 1)
     */
    private int[] intPermutation(int length)
    {
        int[] aux = new int[length];
        int[] result = new int[length];

        // First, create an array from 0 to length - 1.
        // Also is needed to create an random array of size length
        for (int i = 0; i < length; i++)
        {
            result[i] = i;
            aux[i] = Randomness.nextInt(0, length - 1);
        }

        // Sort the random array with effect in result, and then we obtain a
        // permutation array between 0 and length - 1
        for (int i = 0; i < length; i++)
        {
            for (int j = i + 1; j < length; j++)
            {
                if (aux[i] > aux[j])
                {
                    int tmp;
                    tmp = aux[i];
                    aux[i] = aux[j];
                    aux[j] = tmp;
                    tmp = result[i];
                    result[i] = result[j];
                    result[j] = tmp;
                }
            }
        }

        return result;
    }
}
