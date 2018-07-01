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
    private GridNode<C> parent;

    /**
     * creates an GridNode with given bounds, subdivided into a grid with
     * a given depth
     *
     * @param lowerBounds the minimum values of the grid
     * @param upperBounds the maximum values of the grid
     * @param depth the depth of the sub grid
     */
    public GridNode(Map<FitnessFunction<?>, Double> lowerBounds, Map<FitnessFunction<?>, Double> upperBounds, int depth, GridNode<C> parent){
        if(lowerBounds.size() != upperBounds.size())
            throw new IllegalArgumentException("lower and upper bounds must have the same length");
        if(depth < 0)
            throw new IllegalArgumentException("depth must be greater or equal to 0");
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
        this.depth = depth;
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInBounds(C c) {
        for(FitnessFunction<?> ff : this.upperBounds.keySet()){
            if(c.getFitness(ff)> this.upperBounds.get(ff) || c.getFitness(ff) < this.lowerBounds.get(ff))
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
            if(child.isInBounds(c)){
                child.add(c);
                return;
            }
        }
        Map<FitnessFunction<?>, Double> upperBounds = new LinkedHashMap<>();
        Map<FitnessFunction<?>, Double> lowerBounds = new LinkedHashMap<>();
        for (FitnessFunction<?> ff : this.upperBounds.keySet()) {
            double delimiter = (this.upperBounds.get(ff)+this.lowerBounds.get(ff))/2;
            if(c.getFitness(ff) < delimiter){
                upperBounds.put(ff,delimiter);
                lowerBounds.put(ff, this.lowerBounds.get(ff));
            } else{
                upperBounds.put(ff, this.upperBounds.get(ff));
                lowerBounds.put(ff, delimiter);
            }
        }
        GridNodeInterface<C> newChild;
        if(this.depth == 1)
            newChild = new GridLocation<>(lowerBounds, upperBounds, this);
        else
            newChild = new GridNode<>(lowerBounds, upperBounds, this.depth-1, this);
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
                int candidateCount,currentCount,current_level = this.depth;
                candidateRegion = childMostCrowded;
                currentRegion = mostCrowded;
                candidateCount = candidateRegion != null? candidateRegion.count() : 1;
                currentCount = currentRegion != null? currentRegion.count() : 1;
                current_level--;
                while(candidateCount == currentCount && current_level-- > 0) {
                    if(candidateRegion == null)
                        candidateRegion = this.region(candidate, current_level);
                    else
                        candidateRegion = candidateRegion.getParent();
                    if(currentRegion == null)
                        currentRegion = this.region(current, current_level);
                    else
                        currentRegion = currentRegion.getParent();
                    candidateCount = candidateRegion == null ? 1 : candidateRegion.count();
                    currentCount = currentRegion == null ? 1 : currentRegion.count();
                    if(candidateCount > currentCount){
                        mostCrowded = childMostCrowded;
                        best_count = childMostCrowded.count();
                    } else if(currentCount > candidateCount)
                        break;
                    else
                        continue;
                }
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
        for (GridNodeInterface<C> child : children) {
            if(child.isInBounds(c))
                return child.region(c);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridNodeInterface<C> region(C c, int depth){
        for(GridNodeInterface<C> child: children){
            if(child.isInBounds(c)) {
                if (depth == 0)
                    return child;
                return child.region(c, depth - 1);
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
            if (child.isInBounds(c))
                return child;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @param candidate
     * @param current
     * @return
     */
    @Override
    public int decide(C candidate, C current){
        int check_level = this.depth;
        GridNodeInterface candidate_region = this.region(candidate, this.depth);
        GridNodeInterface current_region = this.region(current, this.depth);
        int candidate_score = candidate_region != null ? candidate_region.count() : 0;
        int current_score = current_region != null ? current_region.count() : 0;
        check_level--;
        while(candidate_score == current_score && check_level-- > 0){
            if(candidate_region == null)
                candidate_region = this.region(candidate, check_level);
            else
                candidate_region = candidate_region.getParent();
            if(current_region == null)
                current_region = this.region(current, check_level);
            else
                current_region = current_region.getParent();
            candidate_score = candidate_region != null ? candidate_region.count() : 0;
            current_score = current_region != null ? current_region.count() : 0;
        }
        return candidate_score - current_score;
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

    @Override
    public Set<FitnessFunction<?>> getObjectives() {
        return this.upperBounds.keySet();
    }

    @Override
    public Map<FitnessFunction<?>, Double> getUpperBounds() {
        return this.upperBounds;
    }

    @Override
    public Map<FitnessFunction<?>, Double> getLowerBounds() {
        return this.lowerBounds;
    }

    @Override
    public GridNode<C> getParent() {
        return this.parent;
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }


    public static void main(String[] args){
        DummyFitnessFunction ff1 = new DummyFitnessFunction();
        DummyFitnessFunction ff2 = new DummyFitnessFunction();
        DummyFitnessFunction ff3 = new DummyFitnessFunction();
        Map<FitnessFunction<?>,Double> upperBounds = new LinkedHashMap<>();
        Map<FitnessFunction<?>,Double> lowerBounds = new LinkedHashMap<>();
        upperBounds.put(ff1,1.0);
        upperBounds.put(ff2,1.0);
        upperBounds.put(ff3,1.0);
        lowerBounds.put(ff1,0.0);
        lowerBounds.put(ff2,0.0);
        lowerBounds.put(ff3,0.0);
        GridNode<DummyChromosome> node = new GridNode<>(lowerBounds,upperBounds,3,null);
        DummyChromosome c1 = new DummyChromosome();
        DummyChromosome c2 = new DummyChromosome();
        DummyChromosome c3 = new DummyChromosome();
        DummyChromosome current = new DummyChromosome();
        DummyChromosome candidate = new DummyChromosome();
        c1.setFitness(ff1, 1);
        c1.setFitness(ff2, 0.1);
        c1.setFitness(ff3, 0.3);
        c2.setFitness(ff1, 0.99);
        c2.setFitness(ff2, 0.11);
        c2.setFitness(ff3, 0.29);
        c3.setFitness(ff1, 0.4);
        c3.setFitness(ff2, 0.7);
        c3.setFitness(ff3, 0.1);
        current.setFitness(ff1, 0.9);
        current.setFitness(ff2, 0.1);
        current.setFitness(ff3, 0.31);
        candidate.setFitness(ff1,0.39);
        candidate.setFitness(ff2, 0.71);
        candidate.setFitness(ff3, 0.1);
        node.add(c1);
        node.add(c2);
        node.add(c3);
        GridLocation<DummyChromosome> l1 = node.region(c1);
        GridLocation<DummyChromosome> l2 = node.region(c2);
        GridLocation<DummyChromosome> l3 = node.region(c3);
        GridLocation<DummyChromosome> lcurrent = node.region(current);
        GridLocation<DummyChromosome> lcandidate = node.region(candidate);
        int x= node.decide(candidate,current);
    }
}