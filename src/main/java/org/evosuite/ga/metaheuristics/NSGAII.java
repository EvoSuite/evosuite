package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class NSGAII<T extends Chromosome> extends GeneticAlgorithm<T>
{
    private static final long serialVersionUID = 146182080947267628L;

    private static final Logger logger = LoggerFactory.getLogger(NSGAII.class);

    /**
     * Constructor
     * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object
     */
    public NSGAII(ChromosomeFactory<T> factory) {
        super(factory);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected void evolve() {
        logger.info("executing evolve function");

        List<T> Rt = new ArrayList<T>();
        Rt.addAll(population);

        // execute binary tournment selection, crossover, and mutation to
        // create a offspring population Qt of size N
        List<T> offspringPopulation = new ArrayList<T>(Properties.POPULATION); 
        for (int i = 0; i < Properties.POPULATION / 2; i++) {
            // Selection
            T parent1 = selectionFunction.select(population);
            T parent2 = selectionFunction.select(population);

            T offspring1 = (T)parent1.clone();
            T offspring2 = (T)parent2.clone();

            // CrossOver
            try {
                if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE)
                    crossoverFunction.crossOver(offspring1, offspring2);
            } catch (Exception e) {
                logger.info("CrossOver failed");
            }

            // Mutation
            if (Randomness.nextDouble() <= Properties.MUTATION_RATE) {
                notifyMutation(offspring1);
                offspring1.mutate();
                notifyMutation(offspring2);
                offspring2.mutate();
            }

            offspringPopulation.add(offspring1);
            offspringPopulation.add(offspring2);
        }

        Rt.addAll(offspringPopulation);

        List<List<T>> fronts = this.fastNonDominatedSort(Rt, Properties.POPULATION);

        int i = 0;
        List<T> newGeneration = new ArrayList<T>(Properties.POPULATION);
        while (newGeneration.size() + fronts.get(i).size() <= Properties.POPULATION) {
            // calculating crowding-distance in Fi
            this.crowingDistanceAssignment(fronts.get(i));

            // include ith nondominated front in the parent pop
            newGeneration.addAll(fronts.get(i));

            i++;
        }

        // set distance
        this.crowingDistanceAssignment(fronts.get(i));
        // sort in descending order using the crowded comparison operator
        this.crowdedComparisonOperator(fronts.get(i));

        // choose the first (N - size(newGeneration)) elements of Fi
        int number_iteractions = Properties.POPULATION - newGeneration.size();
        for (int j = 0; j < number_iteractions; j++)
            newGeneration.add(fronts.get(i).get(j));

        // sort in descending order using the crowded comparison operator
        this.crowdedComparisonOperator(newGeneration);
        population = newGeneration;
        currentIteration++;
    }

    /** {@inheritDoc} */
    @Override
    public void initializePopulation() {
        logger.info("executing initializePopulation function");

        notifySearchStarted();
        currentIteration = 0;

        // Create a random parent population P0
        this.generateInitialPopulation(Properties.POPULATION);

        this.notifyIteration();
    }

    /** {@inheritDoc} */
    @Override
    public void generateSolution() {
        logger.info("executing generateSolution function");

        if (population.isEmpty())
            initializePopulation();

        while (!isFinished())
        {
            for (final FitnessFunction<T> ff : this.getFitnessFunctions()) {
                for (T p : population)
                    ff.getFitness(p);
            }

            // Set rank
            List<List<T>> fronts = this.fastNonDominatedSort(population, Properties.POPULATION);
            // Set distance
            for (List<T> f : fronts)
                this.crowingDistanceAssignment(f);
            // P0 is sorted based on the nondomination
            for (List<T> f : fronts)
                this.crowdedComparisonOperator(f);
            this.crowdedComparisonOperator(population);

            evolve();
            this.notifyIteration();
        }

        /*for (T p : population) {
            for (FitnessFunction<T> ff : this.getFitnessFunctions()) {
                System.out.printf("%f,", p.getFitness(ff));
            }
            System.out.print("\n");
        }*/

        notifySearchFinished();
    }

    protected List<T> crowingDistanceAssignment(List<T> front) {
        int l = front.size() - 1; // number of solutions in 'front'

        // initialize distance
        for (T p : front)
            p.setDistance(0.0);

        for (final FitnessFunction<T> ff : this.getFitnessFunctions()) {
            // sort in ascending order, using each objective value
            Collections.sort(front, new Comparator<T>() {
                @Override
                public int compare(T i, T j) {
                    return Double.compare(i.getFitness(ff), j.getFitness(ff));
                }
            });

            double fmin = front.get(0).getFitness(ff);
            double fmax = front.get(l).getFitness(ff);

            front.get(0).setDistance(Double.MAX_VALUE);
            front.get(l).setDistance(Double.MAX_VALUE);

            for (int i = 1; i < l - 1; i++) {
                double normalization = (front.get(i+1).getFitness(ff) - front.get(i-1).getFitness(ff)) /
                                        (fmax - fmin);
                if (Double.compare(normalization, Double.NaN) == 0)
                    normalization = 0.0;
                front.get(i).setDistance( front.get(i).getDistance() + normalization );
            }
        }

        return front;
    }

    /**
     * 
     * @param front
     * @return
     */
    protected List<T> crowdedComparisonOperator(List<T> front) {
        Collections.sort(front, new Comparator<T>() {
            @Override
            public int compare(T i, T j) {
                if (i.getRank() < j.getRank())
                    return -1;
                else if (i.getRank() > j.getRank())
                    return 1;

                // i.getRank() == j.getRank()
                if (i.getDistance() > j.getDistance())
                    return -1;
                else if (i.getDistance() < j.getDistance())
                    return 1;

                return 0;
            }
        });

        return front;
    }

    /**
     * Fast nondominated sorting
     * 
     * @param population Population to sort using domination
     * @param N Number of solutions (stop condition)
     * @return Return the list of identified fronts
     */
    @SuppressWarnings("unchecked")
    protected List<List<T>> fastNonDominatedSort(List<T> population, int N) {
        // reset population in terms of Pareto front
        for (T p : population) {
            p.setRank(0);
            p.setHowManyDominateMe(0);
            p.resetChromosomeDominated();
        }

        //int n = 0;

        List<List<T>> fronts = new ArrayList<List<T>>();
        List<T> f = new ArrayList<T>();

        // Find all p's that belongs to the First Front (0)
        for (int i = 0; i < population.size(); i++)
        {
            T p = population.get(i);

            for (int j = 0; j < population.size(); j++)
            {
                if (i == j) continue ;

                T q = population.get(j);

                // if p dominates q
                if (Chromosome.isDominated(p, q))
                    p.addChromosomeDominated(q); // add q to the set of solutions dominated by p
                else if (Chromosome.isDominated(q, p))
                    p.setHowManyDominateMe( p.getHowManyDominateMe() + 1 ); // increment the domination count
            }

            // if p is not dominated, belongs to the First Front (0)
            if (!p.isDominated()) {
                p.setRank(0);
                f.add(p);

                //n++;
            }
        }

        if (!f.isEmpty())
            fronts.add(f); // add the First Front (0) to the global Fronts

        int i = 0;
        while ((!f.isEmpty()) /*&&
                (n < N)*/
                ) // stop if we have already enough solutions
        {
            f = new ArrayList<T>();

            for (T p : fronts.get(i)) {
                List<T> chromosomeDominated = (List<T>) p.getChromosomeDominated();
                for (T q : chromosomeDominated) {
                    q.setHowManyDominateMe( q.getHowManyDominateMe() - 1 );

                    // if q is not dominated, belongs to the next front
                    if (!q.isDominated()) {
                        q.setRank(i + 1);
                        f.add(q);

                        //n++;
                    }
                }
            }
            i++;

            if (!f.isEmpty())
                fronts.add(f); // add the next Front
        }

        return fronts;
    }
}
