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
 *
 * Since the {@link java.util.Listenable} is a class rather than an Interface,
 * one cannot use it in any case. Therefore we need our own implementation.
 *
 * @author roessler
 * @param <T>
 *            The type of the event.
 */
public interface Listener<T> extends Serializable {
	/**
	 * <p>receiveEvent</p>
	 *
	 * @param event a T object.
	 * @param <T> a T object.
	 */
	void receiveEvent(T event);
}
