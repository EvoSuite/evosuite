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
package com.examples.with.different.packagename.stable;

import java.util.HashMap;
import java.util.Map;

public class MapContainerUser {

    private final Map<Object, Object> myMap = new HashMap<>();

    private final Map<Object, Object> emptyMap = new HashMap<>();

    private final Object myKey;

    private final Object myValue;

    private final Object myOtherKey;

    private final Object myOtherValue;

    public MapContainerUser() {
        myKey = new Object();
        myValue = new Object();
        myMap.put(myKey, myValue);
        myOtherKey = new Object();
        myOtherValue = new Object();
    }

    public boolean containsKeyShouldReturnTrue() {
        return myMap.containsKey(myKey);
    }

    public boolean containsValueShouldReturnTrue() {
        return myMap.containsValue(myValue);
    }

    public boolean containsKeyOnEmptyShouldReturnFalse() {
        return emptyMap.containsKey(myKey);
    }

    public boolean containsValueOnEmptyShouldReturnFalse() {
        return emptyMap.containsValue(myValue);
    }

    public boolean containsValueOnNonEmptyShouldReturnFalse() {
        return myMap.containsValue(myOtherValue);
    }

    public boolean containsKeyOnNonEmptyShouldReturnFalse() {
        return myMap.containsKey(myOtherKey);
    }

    public boolean isEmptyShouldReturnFalse() {
        return myMap.isEmpty();
    }

    public boolean isEmptyShouldReturnTrue() {
        return emptyMap.isEmpty();
    }

}
