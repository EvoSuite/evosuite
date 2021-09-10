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

public class RuntimeUser {

    private final Runtime runtime;

    public RuntimeUser() {
        runtime = Runtime.getRuntime();
    }

    public long freeMemory() {
        return runtime.freeMemory();
    }

    public long totalMemory() {
        return runtime.totalMemory();
    }

    public long maxMemory() {
        return runtime.maxMemory();
    }

    public int availableProcs() {
        return runtime.availableProcessors();
    }


}
