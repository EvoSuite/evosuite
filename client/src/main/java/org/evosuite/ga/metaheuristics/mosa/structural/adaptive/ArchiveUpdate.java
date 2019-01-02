package org.evosuite.ga.metaheuristics.mosa.structural.adaptive;

import org.evosuite.ga.Chromosome;

/**
 * @author Giovanni Grano
 */
public abstract class ArchiveUpdate<T extends Chromosome> {

    abstract boolean isBetterSolution(T archivedSolution, T candidateSolution);

    double normalize(double value) {
        return (value)/(value+1);
    }
}
