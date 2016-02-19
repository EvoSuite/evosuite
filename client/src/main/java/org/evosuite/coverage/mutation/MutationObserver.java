/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
/**
 * 
 */
package org.evosuite.coverage.mutation;

/**
 * <p>MutationObserver class.</p>
 *
 * @author Gordon Fraser
 */
public class MutationObserver {

	/** Constant <code>activeMutation=-1</code> */
	public static int activeMutation = -1;

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
		if (mutation != null)
			activeMutation = mutation.getId();
	}

	/**
	 * <p>activateMutation</p>
	 *
	 * @param id a int.
	 */
	public static void activateMutation(int id) {
		activeMutation = id;
	}

	/**
	 * <p>deactivateMutation</p>
	 */
	public static void deactivateMutation() {
		activeMutation = -1;
	}

	/**
	 * <p>deactivateMutation</p>
	 *
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public static void deactivateMutation(Mutation mutation) {
		activeMutation = -1;
	}

}
