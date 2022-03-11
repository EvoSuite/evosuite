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
package org.evosuite.ga.operators.mutation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class MutationHistory<T extends MutationHistoryEntry> implements Iterable<T>,
        Serializable {

    private static final long serialVersionUID = -8543180637106924913L;

    private final List<T> mutations = new ArrayList<>();

    public void clear() {
        mutations.clear();
    }

    public void addMutationEntry(T entry) {
        mutations.add(entry);
    }

    public List<T> getMutations() {
        return Collections.unmodifiableList(mutations);
    }

    @Override
    public Iterator<T> iterator() {
        return mutations.iterator();
    }

    public int size() {
        return mutations.size();
    }

    public boolean isEmpty() {
        return mutations.isEmpty();
    }

    public void set(MutationHistory<T> other) {
        mutations.addAll(other.getMutations());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return mutations.stream()
                .map(T::toString)
                .collect(joining("\n"));
    }
}
