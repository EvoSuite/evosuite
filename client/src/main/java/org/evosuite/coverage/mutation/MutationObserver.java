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

package org.evosuite.coverage.mutation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>MutationObserver class.</p>
 *
 * @author Gordon Fraser
 */
public class MutationObserver {

	public static Set<Integer> activeMutations = new HashSet<Integer>();
	
	public static boolean isActive(int mutationId) {
		return activeMutations.contains(mutationId);
	}

	/**
	 * <p>mutationTouched</p>
	 *
	 * @param mutationID a int.
	 */
	public static void mutationTouched(int mutationID) {

	}

	/**
	 * <p>activateMutation</p>
	 *
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public static void activateMutation(Mutation mutation) {
		activeMutations.add(mutation.getId());
	}

	/**
	 * <p>activateMutation</p>
	 *
	 * @param id a int.
	 */
	public static void activateMutation(int id) {
		activeMutations.add(id);
	}

	/**
	 * <p>deactivateMutation</p>
	 */
	public static void deactivateMutation() {
		activeMutations.clear();
	}

	/**
	 * <p>deactivateMutation</p>
	 *
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public static void deactivateMutation(Mutation mutation) {
		activeMutations.clear();
	}

	public static void activateMutations(Collection<Mutation> mutations) {
		for (Mutation mutation : mutations) {
			activeMutations.add(mutation.getId());
		}
		
	}

}
