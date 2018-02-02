/**
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
package org.evosuite.ga.metaheuristics;

import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.DominanceComparator;
import org.evosuite.ga.comparators.StrengthFitnessComparator;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * SPEA2 implementation.
 * 
 * @techreport{ZLT:2001,
            author = {E. Zitzler and M. Laumanns and L. Thiele},
            title = {{SPEA2: Improving the Strength Pareto Evolutionary Algorithm}},
            institution = {Computer Engineering and Networks Laboratory (TIK), Swiss Federal
            Institute of Technology (ETH), Zurich, Switzerland},
            year = {2001},
            number = {103}}
 *
 * @author José Campos
 */
public class SPEA2<T extends Chromosome> extends GeneticAlgorithm<T> {

  private static final long serialVersionUID = -7638497183625040479L;

  private static final Logger logger = LoggerFactory.getLogger(SPEA2.class);

  private DominanceComparator comparator;

  // TODO should we use 'archive' from GeneticAlgorithm class?
  private List<T> archive = null;

  public SPEA2(ChromosomeFactory<T> factory) {
    super(factory);
    this.comparator = new DominanceComparator();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void evolve() {
    /*
     * Reproduction
     */

    List<T> offspringPopulation = new ArrayList<T>(Properties.POPULATION);
    while (offspringPopulation.size() < Properties.POPULATION) {
      // TODO SelectionFunction has to be a BinaryTournamentSelection, i.e.,
      // a TournamenteSelection with 2 tournaments
      // TODO we might want to use BinaryTournamentSelectionCrowdedComparison
      T parent1 = this.selectionFunction.select(this.archive);
      T parent2 = this.selectionFunction.select(this.archive);

      T offspring1 = (T) parent1.clone();
      T offspring2 = (T) parent2.clone();

      if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
        try {
          this.crossoverFunction.crossOver(offspring1, offspring2);
        } catch (ConstructionFailedException e) {
          logger.error("Crossover failed: " + e.getMessage());
          e.printStackTrace();
        }
      }

      if (Randomness.nextDouble() <= Properties.MUTATION_RATE) {
        this.notifyMutation(offspring1);
        offspring1.mutate();
        this.notifyMutation(offspring2);
        offspring2.mutate();
      }

      offspringPopulation.add(offspring1);
      offspringPopulation.add(offspring2);
    }

    /*
     * Evaluation
     */

    for (T element : offspringPopulation) {
      for (final FitnessFunction<T> ff : this.getFitnessFunctions()) {
        ff.getFitness(element);
        notifyEvaluation(element);
      }
    }

    /*
     * Replacement
     */

    this.population.clear();
    this.population.addAll(offspringPopulation);

    this.currentIteration++;
  }

  @Override
  public void initializePopulation() {
    this.notifySearchStarted();
    this.currentIteration = 0;

    // Generate an initial population P0
    this.generateInitialPopulation(Properties.POPULATION);
    // and create an empty archive of the same size
    this.archive = new ArrayList<T>(Properties.POPULATION);

    for (T element : this.population) {
      for (final FitnessFunction<T> ff : this.getFitnessFunctions()) {
        ff.getFitness(element);
        notifyEvaluation(element);
      }
    }
    this.updateArchive();
    this.writeIndividuals(this.archive);

    this.notifyIteration();
  }

  @Override
  public void generateSolution() {
    if (this.population.isEmpty()) {
      this.initializePopulation();
    }

    while (!isFinished()) {
      this.evolve();
      this.updateArchive();
      this.notifyIteration();
      this.writeIndividuals(this.archive);
    }

    // replace population object with archive, so that when 'getBestIndividuals()'
    // function is called, the correct list of solutions is returned
    this.population = this.archive;

    this.notifySearchFinished();
  }

  private void updateArchive() {
    List<T> union = new ArrayList<T>(2 * Properties.POPULATION);
    union.addAll(population);
    union.addAll(this.archive);
    this.computeStrength(union);
    this.archive = this.environmentalSelection(union);
  }

  /**
   * 
   * @param population
   * @return
   */
  protected List<T> environmentalSelection(List<T> union) {

    List<T> populationCopy = new ArrayList<T>(union.size());
    populationCopy.addAll(union);

    // First step is to copy all nondominated individuals, i.e., those
    // which have a fitness lower than one, from archive and population
    // to the archive of the next generation
    List<T> tmpPopulation = new ArrayList<T>(populationCopy.size());
    Iterator<T> it = populationCopy.iterator();
    while (it.hasNext()) {
      T individual = it.next();
      if (individual.getDistance() < 1.0) {
        tmpPopulation.add(individual);
        it.remove();
      }
    }

    // If the nondominated front fits exactly into the archive, the environmental
    // selection step is completed
    if (tmpPopulation.size() == Properties.POPULATION) {
      return tmpPopulation;
    }
    // If archive is too small, the best dominated individuals in the previous
    // archive and population are copied to the new archive
    else if (tmpPopulation.size() < Properties.POPULATION) {
      Collections.sort(populationCopy, new StrengthFitnessComparator());
      int remain = (union.size() < Properties.POPULATION ? union.size() : Properties.POPULATION) - tmpPopulation.size();
      for (int i = 0; i < remain; i++) {
        tmpPopulation.add(populationCopy.get(i));
      }

      return tmpPopulation;
    }

    // when the size of the current nondominated (multi)set exceeds the archive size,
    // an archive truncation procedure is invoked which iteratively removes individuals
    // from the new front until if fits exactly into the archive. the individual which
    // has the minimum distance to another individual is chosen at each stage; if there
    // are several individuals with minimum distance the tie is broken by considering the
    // second smallest distances and so forth.

    double[][] distance = this.euclideanDistanceMatrix(tmpPopulation);

    List<List<Pair<Integer, Double>>> distanceList = new LinkedList<List<Pair<Integer, Double>>>();
    for (int i = 0; i < tmpPopulation.size(); i++) {
      List<Pair<Integer, Double>> distanceNodeList = new LinkedList<Pair<Integer, Double>>();

      for (int j = 0; j < tmpPopulation.size(); j++) {
        if (i != j) {
          distanceNodeList.add(Pair.of(j, distance[i][j]));
        }
      }

      // sort by distance so that later we can just get the first element, i.e.,
      // the one with the smallest distance
      Collections.sort(distanceNodeList, new Comparator<Pair<Integer, Double>>() {
        @Override
        public int compare(Pair<Integer, Double> pair1, Pair<Integer, Double> pair2) {
          if (pair1.getRight() < pair2.getRight()) {
            return -1;
          } else if (pair1.getRight() > pair2.getRight()) {
            return 1;
          } else {
            return 0;
          }
        }
      });

      distanceList.add(distanceNodeList);
    }

    while (tmpPopulation.size() > Properties.POPULATION) {
      double minDistance = Double.POSITIVE_INFINITY;
      int minimumIndex = -1;

      for (int i = 0; i < distanceList.size(); i++) {
        List<Pair<Integer, Double>> distances = distanceList.get(i);
        Pair<Integer, Double> point = distances.get(0);

        // as this list is sorted, we just need to get the first element of it.
        if (point.getRight() < minDistance) {
          minDistance = point.getRight();
          minimumIndex = i;
        } else if (point.getRight() == minDistance) {
          // as there is a tie, the th smallest distances as to to be searched for

          // find the k-th smallest distance that is not equal to the one just
          // selected. i.e., go through all distances and skip the ones that
          // are equal.
          for (int k = 0; k < distances.size(); k++) {
            double kdist1 = distances.get(k).getRight();
            double kdist2 = distanceList.get(minimumIndex).get(k).getRight();

            if (kdist1 == kdist2) {
              continue;
            } else if (kdist1 < kdist2) {
              minimumIndex = i;
            }

            break;
          }
        }
      }

      assert minimumIndex != -1;

      // remove the solution with the smallest distance
      tmpPopulation.remove(minimumIndex);
      distanceList.remove(minimumIndex);

      // remove from the neighbours' list of neighbours, the one we just removed
      for (List<Pair<Integer, Double>> distances : distanceList) {
        ListIterator<Pair<Integer, Double>> iterator = distances.listIterator();
        while (iterator.hasNext()) {
          if (iterator.next().getLeft() == minimumIndex) {
            iterator.remove();
            // TODO can we break the loop? is there any chance that 'distances'
            // has repeated elements?!
          }
        }
      }
    }

    return tmpPopulation;
  }

  /**
   * 
   * @param solutions
   */
  protected void computeStrength(List<T> solution) {
    // count the number of individuals each solution dominates
    int[] strength = new int[solution.size()];
    for (int i = 0; i < solution.size() - 1; i++) {
      for (int j = i + 1; j < solution.size(); j++) {
        int comparison = this.comparator.compare(solution.get(i), solution.get(j));
        if (comparison < 0) {
          strength[i]++;
        } else if (comparison > 0) {
          strength[j]++;
        }
      }
    }

    // the raw fitness is the sum of the dominance counts (strength)
    // of all dominated solutions
    double[] rawFitness = new double[solution.size()];
    for (int i = 0; i < solution.size() - 1; i++) {
      for (int j = i + 1; j < solution.size(); j++) {
        int comparison = this.comparator.compare(solution.get(i), solution.get(j));
        if (comparison > 0) {
          rawFitness[i] += strength[j];
        } else if (comparison < 0) {
          rawFitness[j] += strength[i];
        }
      }
    }

    // Add the distance to the k-th individual. In the reference paper of SPEA2,
    // k = sqrt(population.size()), but a value of k = 1 is recommended. See
    // http://www.tik.ee.ethz.ch/pisa/selectors/spea2/spea2_documentation.txt

    double[][] distance = this.euclideanDistanceMatrix(solution);
    int k = 1;
    for (int i = 0; i < distance.length; i++) {
      Arrays.sort(distance[i]);
      double kDistance = 1.0 / (distance[i][k] + 2.0);
      // TODO for now let's use 'distance' field, however the right
      // name should be 'strength' or 'fitness-strength'
      solution.get(i).setDistance(rawFitness[i] + kDistance);
    }
  }

  /**
   * Returns a matrix with the euclidean distance between each pair of solutions in the population.
   * 
   * @param solution
   * @return
   */
  protected double[][] euclideanDistanceMatrix(List<T> solution) {
    double[][] distance = new double[solution.size()][solution.size()];

    for (int i = 0; i < solution.size(); i++) {
      distance[i][i] = 0.0;
      for (int j = i + 1; j < solution.size(); j++) {
        distance[i][j] = this.distanceBetweenObjectives(solution.get(i), solution.get(j));
        distance[j][i] = distance[i][j];
      }
    }

    return distance;
  }

  /**
   * Returns the euclidean distance between a pair of solutions in the objective space.
   * 
   * @param t1
   * @param t2
   * @return
   */
  protected double distanceBetweenObjectives(T t1, T t2) {
    double distance = 0.0;

    // perform euclidean distance
    for (FitnessFunction<?> ff : t1.getFitnessValues().keySet()) {
      double diff = t1.getFitness(ff) - t2.getFitness(ff);
      distance += Math.pow(diff, 2.0);
    }

    return Math.sqrt(distance);
  }
}
