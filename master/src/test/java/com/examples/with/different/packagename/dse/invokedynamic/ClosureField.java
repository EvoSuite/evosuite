/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename.dse.invokedynamic;

import java.util.function.Function;

/**
 * Simple example of closures as fields.
 *
 * Here the method descriptor of the call made in test will differ from the one
 * used when begging executing the closure (intCompare).
 *
 * See the resulting bytecode for more information.
 *
 * @author Ignacio Lebrero
 */
class ClosureField {
    public Function<Integer, Boolean> intCompare;

    public ClosureField() {
        // Closure lambda.
        Integer y = new Integer(12);
        Integer z = new Integer(22);
        this.intCompare = x -> (x > y && x < z);
    }

    boolean test(int x) {
        return this.intCompare.apply(new Integer(x));
    }
}