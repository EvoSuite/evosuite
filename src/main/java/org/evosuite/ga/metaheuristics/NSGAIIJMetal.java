package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NSGA-II implementation
 * 
 * @article{Deb:2002,
            author = {Deb, K. and Pratap, A. and Agarwal, S. and Meyarivan, T.},
            title = {{A Fast and Elitist Multiobjective Genetic Algorithm: NSGA-II}},
            journal = {Trans. Evol. Comp},
            issue_date = {April 2002},
            volume = {6},
            number = {2},
            month = apr,
            year = {2002},
            issn = {1089-778X},
            pages = {182--197},
            numpages = {16},
            url = {http://dx.doi.org/10.1109/4235.996017},
            doi = {10.1109/4235.996017},
            acmid = {2221582},
            publisher = {IEEE Press},
            address = {Piscataway, NJ, USA}}
 *
 * @author José Campos
 */
public class NSGAIIJMetal<T extends Chromosome>
    extends GeneticAlgorithm<T>
{
    private static final long serialVersionUID = 146182080947267628L;

    private static final Logger logger = LoggerFactory.getLogger(NSGAIIJMetal.class);

    /**
     * Constructor
     * 
     * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object
     */
    public NSGAIIJMetal(ChromosomeFactory<T> factory)
    {
        super(factory);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected void evolve()
    {
        // Create the offSpring solutionSet
        List<T> offspringPopulation = new ArrayList<T>(population.size());

        for (int i = 0; i < (population.size() / 2); i++)
        {
            if (currentIteration < Properties.SEARCH_BUDGET)
            {
                // Selection
                T parent1 = selectionFunction.select(population);
                T parent2 = selectionFunction.select(population);

                /*System.out.println("Parent 1 : " + parent1.getFitness(this.getFitnessFunctions().get(0)) + ", " + parent1.getFitness(this.getFitnessFunctions().get(1)));
                System.out.println("Parent 2 : " + parent2.getFitness(this.getFitnessFunctions().get(0)) + ", " + parent2.getFitness(this.getFitnessFunctions().get(1)));*/

                // Crossover
                T offspring1 = (T) parent1.clone();
                T offspring2 = (T) parent2.clone();
                try
                {
                    if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE)
                        crossoverFunction.crossOver(offspring1, offspring2);
                }
                catch (Exception e)
                {
                    logger.info("CrossOver failed");
                }

                /*System.out.println("Offspring 1 : " + offspring1.getFitness(this.getFitnessFunctions().get(0)) + ", " + offspring1.getFitness(this.getFitnessFunctions().get(1)));
                System.out.println("Offspring 2 : " + offspring2.getFitness(this.getFitnessFunctions().get(0)) + ", " + offspring2.getFitness(this.getFitnessFunctions().get(1)));*/

                // Mutation
                if (Randomness.nextDouble() <= Properties.MUTATION_RATE)
                {
                    notifyMutation(offspring1);
                    offspring1.mutate();
                    notifyMutation(offspring2);
                    offspring2.mutate();
                }

                /*System.out.println("Offspring 1 : " + offspring1.getFitness(this.getFitnessFunctions().get(0)) + ", " + offspring1.getFitness(this.getFitnessFunctions().get(1)));
                System.out.println("Offspring 2 : " + offspring2.getFitness(this.getFitnessFunctions().get(0)) + ", " + offspring2.getFitness(this.getFitnessFunctions().get(1)));*/

                // Evaluate
                for (final FitnessFunction<T> ff : this.getFitnessFunctions())
                {
                    ff.getFitness(offspring1);
                    ff.getFitness(offspring2);
                }

                /*System.out.println("Offspring 1 : " + offspring1.getFitness(this.getFitnessFunctions().get(0)) + ", " + offspring1.getFitness(this.getFitnessFunctions().get(1)));
                System.out.println("Offspring 2 : " + offspring2.getFitness(this.getFitnessFunctions().get(0)) + ", " + offspring2.getFitness(this.getFitnessFunctions().get(1)));*/

                offspringPopulation.add(offspring1);
                offspringPopulation.add(offspring2);
                currentIteration += 2;
            }
        }
        /*for (T front : population)
        {
            System.out.println(front.getFitness(this.getFitnessFunctions().get(0)) + ","
                            + front.getFitness(this.getFitnessFunctions().get(1)));
        }
        System.out.println("-------------");
        for (T front : offspringPopulation)
        {
            System.out.println(front.getFitness(this.getFitnessFunctions().get(0)) + ","
                            + front.getFitness(this.getFitnessFunctions().get(1)));
        }
        System.out.println("-------------");
        System.out.println("-------------");*/

        // Create the solutionSet union of solutionSet and offSpring
        List<T> union = union(population, offspringPopulation);

        // Ranking the union
        List<List<T>> ranking = fastNonDominatedSort(union);
        /*for (List<T> fronts : ranking)
        {
            for (T front : fronts)
            {
                System.out.println(front.getRank() + " | " + front.getDistance() + " | ("
                                + front.getFitness(this.getFitnessFunctions().get(0)) + ","
                                + front.getFitness(this.getFitnessFunctions().get(1)) + ")");
            }
        }*/
        //System.exit(-1);

        int remain = population.size();
        int index = 0;
        List<T> front = null;
        population.clear();

        // Obtain the next front
        front = ranking.get(index);

        while ((remain > 0) && (remain >= front.size()))
        {
            // Assign crowding distance to individuals
            crowingDistanceAssignment(front);
            // Add the individuals of this front
            for (int k = 0; k < front.size(); k++)
                population.add(front.get(k));

            // Decrement remain
            remain = remain - front.size();

            // Obtain the next front
            index++;
            if (remain > 0)
                front = ranking.get(index);
        }

        // Remain is less than front(index).size, insert only the best one
        if (remain > 0)
        {
            // front contains individuals to insert
            crowingDistanceAssignment(front);

            Collections.sort(front, new Comparator<T>()
            {
                @Override
                public int compare(T o1, T o2)
                {
                    if (o1 == null)
                        return 1;
                    else if (o2 == null)
                        return -1;

                    int flagComparatorRank = rankComparator(o1, o2);
                    if (flagComparatorRank != 0)
                        return flagComparatorRank;

                    /* His rank is equal, then distance crowding comparator */
                    double distance1 = o1.getDistance();
                    double distance2 = o2.getDistance();

                    if (distance1 > distance2)
                        return -1;
                    if (distance1 < distance2)
                        return 1;

                    return 0;
                }
            });

            for (int k = 0; k < remain; k++)
                population.add(front.get(k));

            remain = 0;
        }

        currentIteration++;
    }

    /** {@inheritDoc} */
    @Override
    public void initializePopulation()
    {
        logger.info("executing initializePopulation function");

        notifySearchStarted();
        currentIteration = 0;

        // Create a random parent population P0
        this.generateInitialPopulation(Properties.POPULATION);

        this.notifyIteration();
    }

    /** {@inheritDoc} */
    @Override
    public void generateSolution()
    {
        logger.info("executing generateSolution function");

        if (population.isEmpty())
            initializePopulation();

        while (!isFinished())
        {
            evolve();
            this.notifyIteration();
        }

        /*List<List<T>> ranking = fastNonDominatedSort(population);
        for (T p : ranking.get(0)) {
            for (FitnessFunction<T> ff : this.getFitnessFunctions()) {
                System.out.print(p.getFitness(ff) + ",");
            }
            System.out.print("\n");
        }*/

        notifySearchFinished();
    }

    private int rankComparator(T o1, T o2)
    {
        if (o1 == null)
            return 1;
        else if (o2 == null)
            return -1;

        if (o1.getRank() < o2.getRank())
            return -1;
        if (o1.getRank() > o2.getRank())
            return 1;

        return 0;
    }

    private List<T> union(List<T> population, List<T> offspringPopulation)
    {
        // Check the correct size. In development
        int newSize = population.size() + offspringPopulation.size();
        if (newSize < Properties.POPULATION)
            newSize = Properties.POPULATION;

        // Create a new population
        List<T> union = new ArrayList<T>(newSize);
        for (int i = 0; i < population.size(); i++)
            union.add(population.get(i));

        for (int i = population.size(); i < (population.size() + offspringPopulation.size()); i++)
            union.add(offspringPopulation.get(i - population.size()));

        return union;
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
     * Fast nondominated sorting
     * 
     * @param population Population to sort using domination
     * @param N Number of solutions (stop condition)
     * @return Return the list of identified fronts
     */
    @SuppressWarnings("unchecked")
    protected List<List<T>> fastNonDominatedSort(List<T> union)
    {
        // dominateMe[i] contains the number of solutions dominating i
        int[] dominateMe = new int[union.size()];

        // iDominate[k] contains the list of solutions dominated by k
        List<Integer>[] iDominate = new List[union.size()];

        // front[i] contains the list of individuals belonging to the front i
        List<Integer>[] front = new List[union.size() + 1];

        // flagDominate is an auxiliar encodings.variable
        int flagDominate;

        // Initialize the fronts
        for (int i = 0; i < front.length; i++)
            front[i] = new LinkedList<Integer>();

        // Fast non dominated sorting algorithm
        for (int p = 0; p < union.size(); p++)
        {
            // Initialize the list of individuals that i dominate and the number
            // of individuals that dominate me
            iDominate[p] = new LinkedList<Integer>();
            dominateMe[p] = 0;
        }

        for (int p = 0; p < (union.size() - 1); p++)
        {
            // For all q individuals , calculate if p dominates q or vice versa
            for (int q = p + 1; q < union.size(); q++)
            {
                flagDominate = dominanceComparator(union.get(p), union.get(q));
                if (flagDominate == -1)
                {
                    iDominate[p].add(q);
                    dominateMe[q]++;
                }
                else if (flagDominate == 1)
                {
                    iDominate[q].add(p);
                    dominateMe[p]++;
                }
            }
            // If nobody dominates p, p belongs to the first front
        }
        for (int p = 0; p < union.size(); p++)
        {
            if (dominateMe[p] == 0)
            {
                front[0].add(p);
                union.get(p).setRank(0);
            }
        }

        // Obtain the rest of fronts
        int i = 0;
        Iterator<Integer> it1, it2; // Iterators
        while (front[i].size() != 0)
        {
            i++;
            it1 = front[i - 1].iterator();
            while (it1.hasNext())
            {
                it2 = iDominate[it1.next()].iterator();
                while (it2.hasNext())
                {
                    int index = it2.next();
                    dominateMe[index]--;
                    if (dominateMe[index] == 0)
                    {
                        front[i].add(index);
                        union.get(index).setRank(i);
                    }
                }
            }
        }

        List<List<T>> ranking = new ArrayList<List<T>>(i);
        // 0,1,2,....,i-1 are front, then i fronts
        for (int j = 0; j < i; j++)
        {
            List<T> f = new ArrayList<T>(front[j].size());
            it1 = front[j].iterator();
            while (it1.hasNext())
                f.add(union.get(it1.next()));

            ranking.add(f);
        }

        return ranking;
    }

    protected void crowingDistanceAssignment(List<T> f)
    {
        int size = f.size();

        if (size == 0)
            return;

        if (size == 1)
        {
            f.get(0).setDistance(Double.POSITIVE_INFINITY);
            return;
        }

        if (size == 2)
        {
            f.get(0).setDistance(Double.POSITIVE_INFINITY);
            f.get(1).setDistance(Double.POSITIVE_INFINITY);
            return;
        }

        // Use a new SolutionSet to avoid altering the original solutionSet
        List<T> front = new ArrayList<T>(size);
        front.addAll(f);

        for (int i = 0; i < size; i++)
            front.get(i).setDistance(0.0);

        double objetiveMaxn;
        double objetiveMinn;
        double distance;

        final boolean ascendingOrder_ = true; // FIXME: remove me
        for (final FitnessFunction<?> ff : this.getFitnessFunctions())
        {
            // Sort the population by Obj n
            Collections.sort(front, new Comparator<T>()
            {
                @Override
                public int compare(T o1, T o2)
                {
                    if (o1 == null)
                        return 1;
                    else if (o2 == null)
                        return -1;

                    double objetive1 = o1.getFitness(ff);
                    double objetive2 = o2.getFitness(ff);
                    if (ascendingOrder_)
                    {
                        if (objetive1 < objetive2)
                            return -1;
                        else if (objetive1 > objetive2)
                            return 1;
                        else
                            return 0;
                    }
                    else
                    {
                        if (objetive1 < objetive2)
                            return 1;
                        else if (objetive1 > objetive2)
                            return -1;
                        else
                            return 0;
                    }
                }
            });
            objetiveMinn = front.get(0).getFitness(ff);
            objetiveMaxn = front.get(front.size() - 1).getFitness(ff);

            // Set de crowding distance
            front.get(0).setDistance(Double.POSITIVE_INFINITY);
            front.get(size - 1).setDistance(Double.POSITIVE_INFINITY);

            for (int j = 1; j < size - 1; j++)
            {
                distance = front.get(j + 1).getFitness(ff) - front.get(j - 1).getFitness(ff);
                distance = distance / (objetiveMaxn - objetiveMinn);
                distance += front.get(j).getDistance();
                front.get(j).setDistance(distance);
            }
        }
    }
}
