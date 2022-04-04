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
package com.examples.with.different.packagename;

import java.util.Collection;

/**
 * Defines a collection that allows objects to be removed in some well-defined order.
 * <p>
 * The removal order can be based on insertion order (eg, a FIFO queue or a
 * LIFO stack), on access order (eg, an LRU cache), on some arbitrary comparator
 * (eg, a priority queue) or on any other well-defined ordering.
 * <p>
 * Note that the removal order is not necessarily the same as the iteration
 * order.  A <code>Buffer</code> implementation may have equivalent removal
 * and iteration orders, but this is not required.
 * <p>
 * This interface does not specify any behavior for
 * {@link Object#equals(Object)} and {@link Object#hashCode} methods.  It
 * is therefore possible for a <code>Buffer</code> implementation to also
 * also implement {@link java.util.List}, {@link java.util.Set} or
 * {@link Bag}.
 *
 * @author Avalon
 * @author Berin Loritsch
 * @author Paul Jack
 * @author Stephen Colebourne
 * @version $Revision: 646777 $ $Date: 2008-04-10 13:33:15 +0100 (Thu, 10 Apr 2008) $
 * @since Commons Collections 2.1
 */
public interface Buffer extends Collection {

    /**
     * Gets and removes the next object from the buffer.
     *
     * @return the next object in the buffer, which is also removed
     * @throws BufferUnderflowException if the buffer is already empty
     */
    Object remove();

    /**
     * Gets the next object from the buffer without removing it.
     *
     * @return the next object in the buffer, which is not removed
     * @throws BufferUnderflowException if the buffer is empty
     */
    Object get();

}
