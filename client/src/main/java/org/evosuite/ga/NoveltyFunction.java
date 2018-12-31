package org.evosuite.ga;

import java.util.Collection;

public abstract class NoveltyFunction<T extends Chromosome> {

    public abstract double getDistance(T individual1, T individual2);

    public double getNovelty(T individual, Collection<T> population) {
        double distance = 0.0;
        boolean loopStart = false;
        for(T other : population) {
            if(other == individual)
                continue;

            double d = getDistance(individual, other);
            distance += d;
            loopStart = true;
        }

        distance /= (population.size() - 1);

        return distance;
    }
}
