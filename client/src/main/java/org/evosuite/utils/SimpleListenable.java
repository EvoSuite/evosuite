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
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.utils;

import java.util.ArrayList;
import java.util.Collection;
public class SimpleListenable<T> implements Listenable<T> {

	private static final long serialVersionUID = 8100518628763448338L;

	protected final Collection<Listener<T>> listeners = new ArrayList<Listener<T>>();

	/** {@inheritDoc} */
	@Override
	public void addListener(Listener<T> listener) {
		listeners.add(listener);
	}

	/** {@inheritDoc} */
	@Override
	public void deleteListener(Listener<T> listener) {
		listeners.remove(listener);
	}

	/**
	 * <p>fireEvent</p>
	 *
	 * @param event a T object.
	 */
	public void fireEvent(T event) {
		for (Listener<T> listener : listeners) {
			listener.receiveEvent(event);
		}
	}

}
