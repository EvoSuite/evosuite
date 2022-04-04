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

package com.examples.with.different.packagename.generic;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gordon Fraser
 */
public class GuavaExample2<B> extends HashMap<Class<? extends B>, B> {

    private static final long serialVersionUID = 2299279734390251599L;

    private GuavaExample2(Class<? extends B> key, B element) {
        put(key, element);
    }

    private GuavaExample2() {

    }

    public static <B> Builder<B> builder() {
        return new Builder<>();
    }

    public static final class Builder<B> {

        private final Map<Class<? extends B>, B> map = new HashMap<>();

        public <T extends B> Builder<B> add(Class<T> key, T value) {
            map.put(key, value);
            return this;
        }

        public GuavaExample2<B> build() {
            if (map.isEmpty())
                return new GuavaExample2<>();
            else {
                Entry<Class<? extends B>, B> entry = map.entrySet().iterator().next();
                return new GuavaExample2<>(entry.getKey(), entry.getValue());
            }
        }
    }

}
