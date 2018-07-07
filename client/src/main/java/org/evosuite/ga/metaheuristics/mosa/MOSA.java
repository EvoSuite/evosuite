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
package org.evosuite.ga.metaheuristics.mosa;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.evosuite.ClientProcess;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.OnlyCrowdingComparator;
import org.evosuite.ga.operators.ranking.CrowdingDistance;
import org.evosuite.ga.operators.selection.BestKSelection;
import org.evosuite.ga.operators.selection.RandomKSelection;
import org.evosuite.ga.operators.selection.RankSelection;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.utils.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Many-Objective Sorting Algorithm (MOSA) described in the
 * paper "Reformulating branch coverage as a many-objective optimization problem".
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew
 */
public class MOSA<T extends Chromosome> extends AbstractMOSA<T> {

	private static final long serialVersionUID = 146182080947267628L;

	private static final Logger logger = LoggerFactory.getLogger(MOSA.class);
	
    /** immigrant groups from neighbouring client */
    protected ConcurrentLinkedQueue<List<T>> immigrants = new ConcurrentLinkedQueue<>();
    
    protected SelectionFunction<T> emigrantsSelection;

	/** Crowding distance measure to use */
	protected CrowdingDistance<T> distance = new CrowdingDistance<T>();

	/**
	 * Constructor based on the abstract class {@link AbstractMOSA}
	 * @param factory
	 */
	public MOSA(ChromosomeFactory<T> factory) {
		super(factory);
		
		switch (Properties.SELECT_EMIGRANT_FUNCTION) {
            case RANK:
                this.emigrantsSelection = new RankSelection<>();
                break;
            case RANDOMK:
                this.emigrantsSelection = new RandomKSelection<>();
                break;
            default:
                this.emigrantsSelection = new BestKSelection<>();
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void evolve() {
		List<T> offspringPopulation = this.breedNextGeneration();

		// Create the union of parents and offSpring
		List<T> union = new ArrayList<T>();
		union.addAll(this.population);
		union.addAll(offspringPopulation);
		
		// for parallel runs: integrate possible immigrants
        if (Properties.PARALLEL_RUN > 1 && !immigrants.isEmpty()) {
            
            union.addAll(immigrants.poll());
        }

		Set<FitnessFunction<T>> uncoveredGoals = this.getUncoveredGoals();

		// Ranking the union
		logger.debug("Union Size =" + union.size());
		// Ranking the union using the best rank algorithm (modified version of the non dominated sorting algorithm)
		this.rankingFunction.computeRankingAssignment(union, uncoveredGoals);

		int remain = this.population.size();
		int index = 0;
		List<T> front = null;
		this.population.clear();

		// Obtain the next front
		front = this.rankingFunction.getSubfront(index);

		while ((remain > 0) && (remain >= front.size()) && !front.isEmpty()) {
			// Assign crowding distance to individuals
			this.distance.fastEpsilonDominanceAssignment(front, uncoveredGoals);
			// Add the individuals of this front
			this.population.addAll(front);
			
			// Decrement remain
			remain = remain - front.size();
			
			// Obtain the next front
			index++;
			if (remain > 0) {
				front = this.rankingFunction.getSubfront(index);
			}
		}

		// Remain is less than front(index).size, insert only the best one
		if (remain > 0 && !front.isEmpty()) { // front contains individuals to insert
			this.distance.fastEpsilonDominanceAssignment(front, uncoveredGoals);
			Collections.sort(front, new OnlyCrowdingComparator());
			for (int k = 0; k < remain; k++) {
				this.population.add(front.get(k));
			}

			remain = 0;
		}

        // for parallel runs: collect best k individuals for migration
        if (Properties.PARALLEL_RUN > 1 && (currentIteration + 1) % Properties.FREQUENCY == 0
                && !this.population.isEmpty()) {
            HashSet<T> emigrants = new HashSet<>(emigrantsSelection.select(this.population, Properties.RATE));
            ClientServices.getInstance().getClientNode().emigrate(emigrants);
        }
		
		this.currentIteration++;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void generateSolution() {
		logger.info("executing generateSolution function");

		// keep track of covered goals
		this.fitnessFunctions.forEach(this::addUncoveredGoal);

		// initialize population
		if (this.population.isEmpty()) {
			this.initializePopulation();
		}

		// Calculate dominance ranks and crowding distance
		this.rankingFunction.computeRankingAssignment(this.population, this.getUncoveredGoals());
		for (int i = 0; i < this.rankingFunction.getNumberOfSubfronts(); i++) {
			this.distance.fastEpsilonDominanceAssignment(this.rankingFunction.getSubfront(i), this.getUncoveredGoals());
		}

		Listener<Set<? extends Chromosome>> listener = null;
		
        if (Properties.PARALLEL_RUN > 1) {
            listener = new Listener<Set<? extends Chromosome>>() {
                @Override
                public void receiveEvent(Set<? extends Chromosome> event) {
                    immigrants.add(new LinkedList<T>((Set<? extends T>) event));
                }
            };
            
            ClientServices.getInstance().getClientNode().addListener(listener);
        }
        
        // TODO add here dynamic stopping condition
		while (!this.isFinished() && this.getNumberOfUncoveredGoals() > 0) {
			this.evolve();
            this.notifyIteration();
        }

        if (Properties.PARALLEL_RUN > 1) {
            ClientServices.getInstance().getClientNode().deleteListener(listener);
            
            if ("ClientNode0".equals(ClientProcess.identifier)) {
                //collect all end result test cases
                Set<Set<? extends Chromosome>> collectedSolutions = ClientServices.getInstance()
                        .getClientNode().getBestSolutions();

                logger.debug("ClientNode0: Received " + collectedSolutions.size() + " solution sets");
                this.mergeSolutions(collectedSolutions);
                this.notifyIteration();
            } else {
                //send end result test cases to ClientNode0
                Set<T> solutionsSet = new HashSet<T>(getSolutions());
                logger.debug(ClientProcess.identifier + ": Sending " + solutionsSet.size()
                                + " solutions to ClientNode0.");
                ClientServices.getInstance().getClientNode().sendBestSolution(solutionsSet);
            }
        }
        
		// storing the time needed to reach the maximum coverage
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Time2MaxCoverage,
                this.budgetMonitor.getTime2MaxCoverage());
		this.notifySearchFinished();
	}
	
	private void mergeSolutions(Set<Set<? extends Chromosome>> solutions) {
	    // Add own solutions
        List<T> union = new ArrayList<T>(this.population);
        int minSize = getSolutions().size();
        
        // Add solutions from all other clients
        for (Set<? extends Chromosome> solution : solutions) {
            union.addAll((Set<? extends T>) solution);
            
            if (solution.size() < minSize) {
                minSize = solution.size();
            }
        }
        
        Set<FitnessFunction<T>> uncoveredGoals = this.getUncoveredGoals();

        // Ranking the union
        logger.debug("Union Size =" + union.size());
        // Ranking the union using the best rank algorithm (modified version of the non dominated sorting algorithm)
        this.rankingFunction.computeRankingAssignment(union, uncoveredGoals);

        int remain = minSize;
        int index = 0;
        this.population.clear();

        // Obtain the first front
        List<T> front = this.rankingFunction.getSubfront(index);

        while ((remain > 0) && (remain >= front.size()) && !front.isEmpty()) {
            // Assign crowding distance to individuals
            this.distance.fastEpsilonDominanceAssignment(front, uncoveredGoals);
            // Add the individuals of this front
            this.population.addAll(front);

            // Decrement remain
            remain = remain - front.size();

            // Obtain the next front
            index++;
            if (remain > 0) {
                front = this.rankingFunction.getSubfront(index);
            }
        }

        // Remain is less than front(index).size, insert only the best one
        if (remain > 0 && !front.isEmpty()) { // front contains individuals to insert
            this.distance.fastEpsilonDominanceAssignment(front, uncoveredGoals);
            Collections.sort(front, new OnlyCrowdingComparator());
            for (int k = 0; k < remain; k++) {
                this.population.add(front.get(k));
            }

            remain = 0;
        }

        this.currentIteration++;
    }
}
