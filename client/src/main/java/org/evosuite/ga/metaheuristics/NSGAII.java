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
package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.CrowdingComparator;
import org.evosuite.ga.comparators.DominanceComparator;
import org.evosuite.ga.comparators.SortByFitness;
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
public class NSGAII<T extends Chromosome>
    extends GeneticAlgorithm<T>
{
    private static final long serialVersionUID = 146182080947267628L;

    private static final Logger logger = LoggerFactory.getLogger(NSGAII.class);

    private DominanceComparator dc;

    /**
     * Constructor
     * 
     * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object
     */
    public NSGAII(ChromosomeFactory<T> factory)
    {
        super(factory);
        this.dc = new DominanceComparator();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected void evolve()
    {
        // Create the offSpring population
        List<T> offspringPopulation = new ArrayList<T>(population.size());

        // execute binary tournment selection, crossover, and mutation to
        // create a offspring population Qt of size N
        for (int i = 0; i < (population.size() / 2); i++)
        {
            // Selection
            T parent1 = selectionFunction.select(population);
            T parent2 = selectionFunction.select(population);

            // Crossover
            T offspring1 = (T) parent1.clone();
            T offspring2 = (T) parent2.clone();

            try {
                if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE)
                    crossoverFunction.crossOver(offspring1, offspring2);
            }
            catch (Exception e) {
                logger.info("CrossOver failed");
            }

            // Mutation
            if (Randomness.nextDouble() <= Properties.MUTATION_RATE) {
                notifyMutation(offspring1);
                offspring1.mutate();
                notifyMutation(offspring2);
                offspring2.mutate();
            }

            // Evaluate
            for (final FitnessFunction<T> ff : this.getFitnessFunctions()) {
                ff.getFitness(offspring1);
                ff.getFitness(offspring2);
            }

            offspringPopulation.add(offspring1);
            offspringPopulation.add(offspring2);
        }

        // Create the population union of Population and offSpring
        List<T> union = union(population, offspringPopulation);

        // Ranking the union
        List<List<T>> ranking = fastNonDominatedSort(union);

        int remain = population.size();
        int index = 0;
        List<T> front = null;
        population.clear();

        // Obtain the next front
        front = ranking.get(index);

        while ((remain > 0) && (remain >= front.size())) {
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
        if (remain > 0) {
            // front contains individuals to insert
            crowingDistanceAssignment(front);

            Collections.sort(front, new CrowdingComparator(true));

            for (int k = 0; k < remain; k++)
                population.add(front.get(k));

            remain = 0;
        }
        //archive
        updateFitnessFunctionsAndValues();
		for (T t : population) {
			if(t.isToBeUpdated()){
			    for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
					fitnessFunction.getFitness(t);
				}
			    t.isToBeUpdated(false);
			}
		}
		//
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

        notifySearchFinished();
    }

    protected List<T> union(List<T> population, List<T> offspringPopulation)
    {
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
     * Fast nondominated sorting
     * 
     * @param population Population to sort using domination
     * @return Return the list of identified fronts
     */
    @SuppressWarnings("unchecked")
    protected List<List<T>> fastNonDominatedSort(List<T> union)
    {
        // dominateMe[i] contains the number of individuals dominating i
        int[] dominateMe = new int[union.size()];

        // iDominate[k] contains the list of individuals dominated by k
        List<Integer>[] iDominate = new List[union.size()];

        // front[i] contains the list of individuals belonging to the front i
        List<Integer>[] front = new List[union.size() + 1];

        // flagDominate is an auxiliar variable
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
            // for all q individuals, calculate if p dominates q or vice versa
            for (int q = p + 1; q < union.size(); q++)
            {
                //flagDominate = dominanceComparator(union.get(p), union.get(q));
                flagDominate = dc.compare(union.get(p), union.get(q));
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
            // if nobody dominates p, p belongs to the first front
        }
        for (int p = 0; p < union.size(); p++)
        {
            if (dominateMe[p] == 0)
            {
                front[0].add(p);
                union.get(p).setRank(0);
            }
        }

        // obtain the rest of fronts
        int i = 0;
        Iterator<Integer> it1, it2;
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
            return ;
        if (size == 1) {
            f.get(0).setDistance(Double.POSITIVE_INFINITY);
            return;
        }
        if (size == 2) {
            f.get(0).setDistance(Double.POSITIVE_INFINITY);
            f.get(1).setDistance(Double.POSITIVE_INFINITY);
            return;
        }

        // use a new Population List to avoid altering the original Population
        List<T> front = new ArrayList<T>(size);
        front.addAll(f);

        for (int i = 0; i < size; i++)
            front.get(i).setDistance(0.0);

        double objetiveMaxn;
        double objetiveMinn;
        double distance;

        for (final FitnessFunction<?> ff : this.getFitnessFunctions())
        {
            // Sort the population by Fit n
            Collections.sort(front, new SortByFitness(ff, true));

            objetiveMinn = front.get(0).getFitness(ff);
            objetiveMaxn = front.get(front.size() - 1).getFitness(ff);

            // set crowding distance
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
