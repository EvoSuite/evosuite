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
package com.examples.with.different.packagename.dse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This will make more sense when we add complex object support to the Symbolic Engine.
 * <p>
 * //TODO: Stream API seems to need special treatmen as the code flow is of the form:
 * instrumented code -> call to stream API -> un-instrumented code -> call to lambda -> instrumented code
 * This way we loose track of symbolic elements in the first un-instrumented code section.
 *
 * @author Ignacio Lebrero
 */
public class StreamAPIExample {
    public static int test(int a) {
        List<Integer> list = new ArrayList() {{
            add(2);
        }};

        Integer i = list
                .stream()
                .map(val -> {
                    if (a == 3) {
                        return val * 2;
                    } else {
                        if (a == 7) {
                            return val * 3;
                        } else {
                            return val;
                        }
                    }
                }).findFirst()
                .orElse(0);

        if (i == 2) {
            return 1;
        } else if (i == 4) {
            return 2;
        } else if (i == 6) {
            return 3;
        } else {
            return 4;
        }
    }
}
