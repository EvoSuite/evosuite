package org.evosuite.ga.metaheuristics.paes;

import org.evosuite.ga.Chromosome;

import java.util.List;

/**
 * Interface of an Archive for a Pareto Archived PaesGA Strategy.
 * Provides an overview over the non-dominated {@link Chromosome}chromosomes, that can be
 * considered whether a candidate is accepted or rejected.
 *
 * Created by Sebastian on 10.04.2018.
 */
public interface Archive<C extends Chromosome> {
    /**
     * Removes all dominated {@link Chromosome} from the archive and adds
     * the given one, if it is not dominated by any chromosome in the archive.
     * If the archive is full and the given chromosome would increase
     * the diversity of the archive, one of the least crowded chromosomes is removed
     * in order to add the given one.
     *
     * @param c the chromosomes to be added
     * @return whether the chromosome {@param c} was added
     */
    boolean add(C c);

    /**
     * Gives a {@link List} List of all{@link Chromosome} in the archive
     *
     * @return {@link List} of all {@link Chromosome} stored in the archive
     */
    List<C> getChromosomes();

    /**
     * Decides whether a {@link Chromosome} candidate is less crowded
     * than another {@link Chromosome}
     *
     * @param candidate {@link Chromosome} chromosome that should be compared to another
     * @param current {@link Chromosome} chromosome the candidate should be compared
     * @return whether the candidate or the current solution is lees crowded
     */
    boolean decide(C candidate, C current);

    /**
     * Removes all {@link Chromosome} chromosomes that are dominated by {@param c} a
     * given chromosome
     *
     * @param c {@link Chromosome} all stored chromosome are removed, if they are
     *                            dominated by this one.
     */
    void removeDominated(C c);
}
