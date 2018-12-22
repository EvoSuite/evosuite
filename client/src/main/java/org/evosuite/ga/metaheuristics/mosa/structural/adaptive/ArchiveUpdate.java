package org.evosuite.ga.metaheuristics.mosa.structural.adaptive;

import org.evosuite.ga.Chromosome;

/**
 * @author Giovanni Grano
 */
public interface ArchiveUpdate<T extends Chromosome> {

    boolean isBetterSolution(T currentSolution, T candidateSolution);
}
