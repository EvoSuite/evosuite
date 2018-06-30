package org.evosuite.ga.metaheuristics.paes;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.paes.Grid.GridLocation;
import org.evosuite.ga.metaheuristics.paes.Grid.GridNode;
import org.evosuite.ga.metaheuristics.paes.Grid.GridNodeInterface;

import java.util.*;

/**
 * PAES Archive to store non-dominated {@link Chromosome} in a Genetic algorithm.
 *
 * Created by Sebastian on 10.04.2018.
 */
public class PaesArchive<C extends Chromosome> implements PaesArchiveInterface<C> {
    private static final boolean USE_RECURSIVE_GRID_CROWDED = false;
    private static boolean USE_BEST_SCORE = false;
    private static final int MAX_SIZE = 100;
    private static final int GRID_LAYER_DEPTH = 5;
    private GridNodeInterface<C> grid;
    private List<C> archivedChromosomes = new ArrayList<>();
    private List<FitnessFunction<?>> fitnessFunctions;
    private double min_value;
    private double max_value;

    /**
     * Constructor for a new Archive for a {@param gridDimension}-dimensional space
     * from {@param min_value} to {@param max_value}
     *
     * @param fitnessFunctions
     * @param min_value
     * @param max_value
     */
    public PaesArchive(Set<FitnessFunction<?>> fitnessFunctions, double min_value, double max_value){
        this.fitnessFunctions = new ArrayList<>();
        this.fitnessFunctions.addAll(fitnessFunctions);
        Map<FitnessFunction<?>, Double> lowerBounds = new LinkedHashMap<>();
        Map<FitnessFunction<?>, Double> upperBounds = new LinkedHashMap<>();
        for(FitnessFunction<?> ff : fitnessFunctions){
            lowerBounds.put(ff, min_value);
            upperBounds.put(ff, max_value);
        }
        this.min_value = min_value;
        this.max_value = max_value;
        grid = new GridNode<>(lowerBounds, upperBounds, PaesArchive.GRID_LAYER_DEPTH, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(C c) {
        this.removeDominated(c);
        for (C chromosome: archivedChromosomes)
            if(chromosome.dominates(c))
                return false;
        if(this.archivedChromosomes.size() < PaesArchive.MAX_SIZE){
            this.archivedChromosomes.add(c);
            this.grid.add(c);
            return true;
        } else {
            GridLocation<C> mostCrowded =
                    PaesArchive.USE_RECURSIVE_GRID_CROWDED ? grid.recursiveMostCrowdedRegion() : grid.mostCrowdedRegion();
            if(mostCrowded.isInBounds(c))
                return false;
            GridLocation<C> region = grid.region(c);
            if(region != null && region.count() >= mostCrowded.count() && !PaesArchive.USE_RECURSIVE_GRID_CROWDED)
                return false;
            C deleted = mostCrowded.getAll().get(0);
            if(region != null)
                region.add(c);
            else
                grid.add(c);
            grid.delete(deleted);
            archivedChromosomes.remove(deleted);
            archivedChromosomes.add(c);
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<C> getChromosomes() {
        return archivedChromosomes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean decide(C candidate, C current) {
        if(PaesArchive.USE_BEST_SCORE){
            int candidateBestScoreCount = this.getBestScoreCount(candidate);
            int currentBestScoreCount = this.getBestScoreCount(current);
            if(candidateBestScoreCount > currentBestScoreCount)
                return true;
            else if(currentBestScoreCount > candidateBestScoreCount)
                return false;
        }
        int dif = this.grid.decide(candidate, current, PaesArchive.USE_RECURSIVE_GRID_CROWDED);
        return dif > 0;
    }

    private int getBestScoreCount(C chromosome) {
        Map<FitnessFunction<?>, Double> fitnessValues = chromosome.getFitnessValues();
        LinkedHashMap<FitnessFunction<?>, Boolean> defeated = new LinkedHashMap<>();
        int count = 0;
        for(C c : archivedChromosomes){
            for(FitnessFunction<?> ff : fitnessValues.keySet()){
                if(!defeated.get(ff) && (chromosome.getFitness(ff) > c.getFitness(ff))) {
                    defeated.put(ff, true);
                    ++count;
                }
            }
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDominated(C c) {
        List<C> dominated = new ArrayList<>();
        for (C chromosome: archivedChromosomes)
            if(c.dominates(chromosome))
                dominated.add(chromosome);
        archivedChromosomes.removeAll(dominated);
        grid.deleteAll(dominated);
    }

    @Override
    public void updateFitnessFunctions(Set<FitnessFunction<?>> fitnessFunctions) {
        Map<FitnessFunction<?>,Double> upperBounds = new HashMap<>();
        Map<FitnessFunction<?>,Double> lowerBounds = new HashMap<>();
        for(FitnessFunction<?> ff : fitnessFunctions){
            upperBounds.put(ff, max_value);
            lowerBounds.put(ff, min_value);
        }
        this.grid = new GridNode<>(lowerBounds,upperBounds,PaesArchive.GRID_LAYER_DEPTH, null);
        for(C c : archivedChromosomes) {
            grid.add(c);
        }
    }
}


