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
package org.evosuite.ga;

import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.utils.Randomness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Created by gordon on 14/05/2016.
 */
public class DummyChromosome extends Chromosome<DummyChromosome> {

    private static final long serialVersionUID = 1119496483428808761L;
    private final List<Integer> values = new ArrayList<>();

    public DummyChromosome(int... values) {
        for (int x : values) {
            this.values.add(x);
        }
    }

    public DummyChromosome(Collection<Integer> values) {
        this.values.addAll(values);
    }

    public DummyChromosome(DummyChromosome other) {
        this.values.addAll(other.values);
    }

    @Override
    public DummyChromosome clone() {
        return new DummyChromosome(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DummyChromosome that = (DummyChromosome) o;

        return Objects.equals(values, that.values);

    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public int compareSecondaryObjective(DummyChromosome o) {
        return 0;
    }

    @Override
    public void mutate() {
        if (values.isEmpty())
            return;

        double P = 1.0 / values.size();
        for (int i = 0; i < values.size(); i++) {
            if (Randomness.nextDouble() < P) {
                values.set(i, Randomness.nextInt());
            }
        }
        this.increaseNumberOfMutations();
        this.setChanged(true);
    }

    public int get(int position) {
        return values.get(position);
    }

    public List<Integer> getGenes() {
        return values;
    }

    @Override
    public void crossOver(DummyChromosome other, int position1, int position2) throws ConstructionFailedException {

        while (values.size() > position1) {
            values.remove(position1);
        }

        for (int num = position2; num < other.size(); num++) {
            values.add(other.get(num));
        }

        this.setChanged(true);
    }

    @Override
    public boolean localSearch(LocalSearchObjective<DummyChromosome> objective) {
        return false;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public DummyChromosome self() {
        return this;
    }
}
