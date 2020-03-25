package org.evosuite.ga;

import java.util.Collection;

public abstract class NoveltyFunction<T extends Chromosome> {

    public abstract double getDistance(T individual1, T individual2);

    public double getNovelty(T individual, Collection<T> population) {
        double distance = population.stream()
                .filter(other -> other != individual)
                .mapToDouble(other -> getDistance(individual, other))
                .sum();

        distance /= (population.size() - 1);

        return distance;
    }
}
