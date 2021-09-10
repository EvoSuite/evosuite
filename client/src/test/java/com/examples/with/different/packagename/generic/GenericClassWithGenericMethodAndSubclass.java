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

/**
 * @author Gordon Fraser
 */
public class GenericClassWithGenericMethodAndSubclass<T> {

    public static class Foo<T> {
        private final T object;

        private Foo(T object) {
            this.object = object;
        }

        public T getObject() {
            return object;
        }
    }

    public final <S extends T> Foo<S> wrap(S object) {
        return new Foo<>(object);
    }

    public boolean test(Foo<T> foo1, Foo<T> foo2) {
        if (foo1.getObject() == foo2.getObject())
            return true;
        else
            return false;
    }
}
