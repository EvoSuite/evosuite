package org.evosuite.ga.metaheuristics.paes.Grid;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

import java.util.*;

/**
 * A node in a tree structured archive of a Pareto archived
 * evolution strategy. Stores other GridNodes or {@link GridLocation}
 * depending on the depth
 *
 * @param <C> class extending {@link org.evosuite.ga.Chromosome}. Objects of this class
 *            are stored in the leaf nodes.
 */
public class GridNode<C extends Chromosome> implements GridNodeInterface<C> {
    private Map<FitnessFunction<?>, Double> lowerBounds;
    private Map<FitnessFunction<?>, Double> upperBounds;
    private int depth;
    private ArrayList<GridNodeInterface<C>> children = new ArrayList<>();

    /**
     * creates an GridNode with given bounds, subdivided into a grid with
     * a given depth
     *
     * @param lowerBounds the minimum values of the grid
     * @param upperBounds the maximum values of the grid
     * @param depth the depth of the sub grid
     */
    public GridNode(Map<FitnessFunction<?>, Double> lowerBounds, Map<FitnessFunction<?>, Double> upperBounds, int depth){
        if(lowerBounds.size() != upperBounds.size())
            throw new IllegalArgumentException("lower and upper bounds must have the same length");
        if(depth < 0)
            throw new IllegalArgumentException("depth must be greater or equal to 0");
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
        this.depth = depth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInBounds(Map<FitnessFunction<?>, Double> values) {
        if(values.size() != this.lowerBounds.size() || values.size() != this.upperBounds.size())
            throw new IllegalArgumentException("values got wrong length");
        for(FitnessFunction<?> ff : values.keySet()){
            if(values.get(ff)> this.upperBounds.get(ff) || values.get(ff) < this.lowerBounds.get(ff))
                return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count() {
        int sum = 0;
        for(GridNodeInterface<C> child : children)
            sum += child.count();
        return sum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(C c) {
        for(GridNodeInterface<C> child : children){
            if(child.isInBounds(c.getFitnessValues())){
                child.add(c);
                return;
            }
        }
        Map<FitnessFunction<?>, Double> scores = c.getFitnessValues();
        if(scores.size() != this.upperBounds.size() || scores.size() != this.lowerBounds.size())
            throw new IllegalArgumentException("scores of c got wrong length");
        Map<FitnessFunction<?>, Double> upperBounds = new LinkedHashMap<>();
        Map<FitnessFunction<?>, Double> lowerBounds = new LinkedHashMap<>();
        for (FitnessFunction<?> ff : this.upperBounds.keySet()) {
            double delimiter = (this.upperBounds.get(ff)+this.lowerBounds.get(ff))/2;
            if(scores.get(ff) < delimiter){
                upperBounds.put(ff,delimiter);
                lowerBounds.put(ff, this.lowerBounds.get(ff));
            } else{
                upperBounds.put(ff, this.upperBounds.get(ff));
                lowerBounds.put(ff, delimiter);
            }
        }
        GridNodeInterface<C> newChild;
        if(this.depth == 1)
            newChild = new GridLocation<>(lowerBounds, upperBounds);
        else
            newChild = new GridNode<>(lowerBounds, upperBounds, this.depth-1);
        newChild.add(c);
        this.children.add(newChild);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(C c) {
        List<GridNodeInterface<C>> empty = new ArrayList<>();
        for(GridNodeInterface<C> child :children) {
            child.delete(c);
            if(child.count() == 0)
                empty.add(child);
        }
        children.removeAll(empty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(Collection<C> cs){
        for (C c: cs) {
            this.delete(c);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<C> getAll() {
        List<C> elements = new ArrayList<>();
        for (GridNodeInterface<C> child: children)
            elements.addAll(child.getAll());
        return elements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridLocation<C> mostCrowdedRegion() {
        GridLocation<C> mostCrowded = null;
        int best_count = 0;
        for (GridNodeInterface<C> child : children) {
            GridLocation<C> childMostCrowded = child.mostCrowdedRegion();
            if(childMostCrowded.count() > best_count){
                mostCrowded = childMostCrowded;
                best_count = childMostCrowded.count();
            } else if(childMostCrowded.count() == best_count){
                GridNodeInterface<C> candidateRegion;
                C candidate = childMostCrowded.getAll().get(0);
                GridNodeInterface<C> currentRegion;
                C current = mostCrowded.getAll().get(0);
                int current_level = this.depth;
                do {
                    candidateRegion = this.region(candidate, current_level);
                    currentRegion = this.region(current, current_level);
                    if(candidateRegion.count() > currentRegion.count()){
                        mostCrowded = childMostCrowded;
                        best_count = childMostCrowded.count();
                    } else if(currentRegion.count() > candidateRegion.count())
                        break;
                    else
                        continue;
                } while(candidateRegion.count() == currentRegion.count() && current_level-- > 0);
            }
        }
        return mostCrowded;
    }

    @Override
    public GridNodeInterface<C> current_mostCrowdedRegion() {
        int max = 0;
        GridNodeInterface<C> best = null;
        for(GridNodeInterface<C> g : children){
            if(g.count() > max) {
                best = g;
                max = g.count();
            }
        }
            return best;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridLocation<C> region(C c) {
        Map<FitnessFunction<?>, Double> scores = c.getFitnessValues();
        for (GridNodeInterface<C> child : children) {
            if(child.isInBounds(scores)){
                return child.region(c);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridNodeInterface<C> region(C c, int depth){
        Map<FitnessFunction<?>, Double> scores = c.getFitnessValues();
        for(GridNodeInterface<C> child: children){
            if(child.isInBounds(scores)){
                if(depth == 0)
                    return child;
                return child.region(c, depth-1);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridNodeInterface<C> current_region(C c) {
        for (GridNodeInterface<C> child: children) {
            if (child.isInBounds(c.getFitnessValues()))
                return child;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int decide(C candidate, C current, boolean recursive){
        if(recursive)
            return decide_recursive(candidate, current);
        else
            return decide_leaf(candidate, current);
    }

    private int decide_leaf(C candidate, C current){
        int check_level = this.depth;
        int candidate_score;
        int current_score;
        do {
            GridNodeInterface candidate_region = this.region(candidate, check_level);
            GridNodeInterface current_region = this.region(current, check_level);
            candidate_score = candidate_region != null ? candidate_region.count() : 0;
            current_score = current_region != null ? current_region.count() : 0;
        } while(candidate_score == current_score && check_level-- > 0);
        return candidate_score - current_score;
    }

    private int decide_recursive(C candidate, C current){
        GridNodeInterface candidate_region = this.current_region(candidate);
        GridNodeInterface<C> current_region = this.current_region(current);
        if(current_region == null && candidate_region == null)
            return 0;
        if(current_region == null)
            return 1;
        if(candidate_region == null)
            return -1;
        while(candidate_region.count() == current_region.count()){
            candidate_region = candidate_region.current_region(candidate);
            current_region = current_region.current_region(current);
            if(current_region == null && candidate_region == null)
                return 0;
            if(current_region == null)
                return 1;
            if(candidate_region == null)
                return -1;
            if(candidate_region.isLeaf() && current_region.isLeaf())
                break;
        }
        return candidate_region.count() - current_region.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridLocation<C> recursiveMostCrowdedRegion() {
        return this.current_mostCrowdedRegion().recursiveMostCrowdedRegion();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
