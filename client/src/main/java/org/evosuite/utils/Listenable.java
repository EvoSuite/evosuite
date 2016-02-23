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
package org.evosuite.utils;

import java.io.Serializable;

/**
 * {@link java.util.Observable} should be an interface. It is not so we cannot
 * use it if the class to implement it has already a certain parent.
 *
 * @author roessler
 * @param <T>
 *            The type of the event.
 */
public interface Listenable<T> extends Serializable {

	/**
	 * Add a listener. He will be notified of changes via the
	 * {@link Listener#fireEvent(Object)} method.
	 *
	 * @param listener
	 *            The listener to add.
	 * @param <T> a T object.
	 */
	void addListener(Listener<T> listener);

	/**
	 * Remove the given Listener such that he will no longer receive event
	 * updates. If the listener is removed several times or was not added prior
	 * to removal, nothing happens.
	 *
	 * @param listener
	 *            The listener to remove.
	 */
	void deleteListener(Listener<T> listener);
}
