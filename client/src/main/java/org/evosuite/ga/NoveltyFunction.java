package org.evosuite.ga;

import java.util.Collection;

public abstract class NoveltyFunction<T extends Chromosome> {

    public abstract double getDistance(T individual1, T individual2);

    public double getNovelty(T individual, Collection<T> population) {
        double distance = 0.0;
        for(T other : population) {
            if(other == individual)
                continue;

            // this causes the distance vector to be stored in he 'other' individual
            // returns the euclidean distance from the distance vector
            double d = getDistance(individual, other);
            distance += d;
        }

        distance /= (population.size() - 1);

        return distance;
    }

    public abstract void calculateNovelty(Collection<T> population);
}
