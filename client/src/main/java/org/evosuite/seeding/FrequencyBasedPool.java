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
package org.evosuite.seeding;

import org.evosuite.utils.Randomness;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by gordon on 06/01/2017.
 */
public class FrequencyBasedPool<T> {

    private final Map<T, Integer> constants = Collections.synchronizedMap(new LinkedHashMap<>());

    private int numConstants = 0;

    public void addConstant(T value) {
        numConstants++;
        if (!constants.containsKey(value))
            constants.put(value, 1);
        else
            constants.put(value, constants.get(value) + 1);
    }

    public void removeConstant(T value) {
        if (constants.containsKey(value)) {
            int num = constants.get(value);
            if (num <= 1)
                constants.remove(value);
            else
                constants.put(value, num - 1);
        }
    }

    public boolean hasConstant(T value) {
        return constants.containsKey(value);
    }


    public T getRandomConstant() {
        //special case
        if (numConstants == 0) {
            throw new IllegalArgumentException("Cannot select from empty pool");
        }

        double rnd = Randomness.nextDouble() * numConstants;

        for (Map.Entry<T, Integer> entry : constants.entrySet()) {
            int num = entry.getValue();
            if (num > rnd) {
                return entry.getKey();
            } else {
                rnd = rnd - num;
            }
        }

        // Shouldn't happen
        return Randomness.choice(constants.keySet());
    }

}
