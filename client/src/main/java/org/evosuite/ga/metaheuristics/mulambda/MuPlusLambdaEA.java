/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics.mulambda;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * (Mu + Lambda) EA
 *
 * @author José Campos
 */
public class MuPlusLambdaEA<T extends Chromosome<T>> extends AbstractMuLambda<T> {

    private static final long serialVersionUID = -8685698059226067598L;

    public MuPlusLambdaEA(ChromosomeFactory<T> factory, int mu, int lambda) {
        super(factory, mu, lambda);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void evolve() {

        List<T> offsprings = new ArrayList<>(this.lambda);

        // create new offsprings by mutating current population
        for (int i = 0; i < this.mu; i++) {
            for (int j = 0; j < this.lambda / this.mu; j++) {
                T offspring = this.population.get(i).clone();
                this.notifyMutation(offspring);

                do {
                    offspring.mutate();
                } while (!offspring.isChanged());

                offsprings.add(offspring);
            }
        }

        // update fitness values of offsprings
        for (T offspring : offsprings) {
            for (FitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
                fitnessFunction.getFitness(offspring);
                this.notifyEvaluation(offspring);
            }
        }

        for (int i = 0; i < this.population.size(); i++) {
            T bestOffspring = this.population.get(i);

            boolean offspring_is_better = false;
            for (T offspring : offsprings) {
                if (isBetterOrEqual(offspring, bestOffspring)) {
                    bestOffspring = offspring;
                    offspring_is_better = true;
                }
            }

            if (offspring_is_better) {
                // replace individual with a better one
                this.population.set(i, bestOffspring);
                // to prevent a population with only equal and dominant
                // individuals, here the best offspring is remove so that
                // it cannot be chosen again. in case of 1+1 and 1+Lambda EA
                // this optimization has no effect.
                offsprings.remove(bestOffspring);
            }

            this.population.get(i).updateAge(this.currentIteration);
        }
        assert this.population.size() == this.mu;

        this.currentIteration++;
    }
}
