/**
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
package org.evosuite.seeding;

import org.evosuite.Properties;
import org.evosuite.utils.RandomAccessQueue;
import org.evosuite.utils.Randomness;

import java.util.*;

/**
 * Created by gordon on 06/01/2017.
 */
public class FrequencyBasedRandomAccessQueue<T> implements RandomAccessQueue<T> {

    private final FrequencyBasedPool<T> values = new FrequencyBasedPool<T>();

    private final Queue<T> queue = new ArrayDeque<T>();

    /* (non-Javadoc)
     * @see org.evosuite.primitives.RandomAccessQueue#restrictedAdd(java.lang.Object)
     */
    @Override
    public void restrictedAdd(T value) {
        values.addConstant(value);
        queue.add(value);
        reduceSize();
    }

    private void reduceSize() {
        if (queue.size() > Properties.DYNAMIC_POOL_SIZE) {
            T value = queue.poll();
            values.removeConstant(value);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.primitives.RandomAccessQueue#getRandomValue()
     */
    @Override
    public T getRandomValue() {
        return values.getRandomConstant();
    }

    @Override
    public String toString() {
        String res = "[ ";
        Iterator<T> itr = queue.iterator();
        while(itr.hasNext()) {
            Object element = itr.next();
            res += element + " ";
        }
        res += "]";
        return res;
    }

}
