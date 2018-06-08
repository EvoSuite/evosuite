package org.evosuite.ga.metaheuristics.paes.Grid;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface of a Node of a grid. These nodes can be subdivided into
 * other nodes or into leaf nodes
 */
public interface GridNodeInterface<C extends Chromosome> {

    /**
     * checks whether an array is inside the bounds of this node.
     *
     * @param values array of values checked whether they are inside the node
     * @return whether {@param values} are in bounds
     */
    boolean isInBounds(Map<FitnessFunction<?>, Double> values);

    /**
     * gives the amount of {@link Chromosome} are contained in the node
     *
     * @return amount of {@link Chromosome} are contained in the node
     */
    int count();

    /**
     * adds a {@link Chromosome} to the grid node.
     *
     * @param c the {@link Chromosome} that should be added.
     */
    void add(C c);

    /**
     * deletes an given {@link Chromosome} from the grid
     *
     * @param c the {@link Chromosome} to be deleted from the grid
     */
    void delete(C c);

    /**
     * deletes all given {@link Chromosome} from the grid
     *
     * @param c {@link Collection} of {@link Chromosome} to be removed from the grid.
     */
    void deleteAll(Collection<C> c);

    /**
     * gives all {@link Chromosome} stored in the grid
     *
     * @return a {@link List} of {@link Chromosome} stored in the grid
     */
    List<C> getAll();

    /**
     * searches for the {@link GridLocation} with the highest amount
     * of stored {@link Chromosome}. If there are 2 equal crowded locations,
     * the one of the oldest subtree is returned.
     *
     * @return the most crowded {@link GridLocation}
     */
    GridLocation<C> mostCrowdedRegion();

    GridNodeInterface<C> current_mostCrowdedRegion();

    /**
     * searches for the leaf-region of a given {@link Chromosome}.
     *
     * @param c the {@link Chromosome}, the region is required.
     * @return the {@link GridLocation} of {@param c} or null, if
     *          the {@link GridLocation} would be empty.
     */
    GridLocation<C> region(C c);

    /**
     * searches for the region of a given {@link Chromosome} with
     * a given depth
     */
    GridNodeInterface<C> region(C c, int depth);

    /**
     * searches for the child-region containing the given {@link Chromosome}
     *
     * @param c the {@link Chromosome}, the region is required.
     * @return the {@link GridNodeInterface} of {@param c} or null, if it would
     *          be empty.
     */
    GridNodeInterface<C> current_region(C c);

    /**
     * Decides which {@link Chromosome} is rated over another;
     *
     * @param candidate compared to {@param current}
     * @param current compared to {@param current}
     * @param recursive whether the comparison is recursive for every level int the grid or
 *                      only the leaf nodes
     * @return is positive if {@param candidate} is rated over {@param current},
     *          is negative if {@param current} is rated over {@param candidate},
     *          is zero if {@param candidate} and {@param current} are rated the same
     */
    int decide(C candidate, C current, boolean recursive);

    /**
     * searches recursively for the most crowded child-region
     *
     * @return is the found region
     */
    GridLocation<C> recursiveMostCrowdedRegion();

    /**
     * @return Whether the node is a leaf-node or not.
     */
    boolean isLeaf();
}
