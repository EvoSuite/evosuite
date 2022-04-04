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
package org.evosuite.symbolic.solver.avm;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverTimeoutException;

import java.util.Collection;

abstract class VariableAVM {

    protected final Collection<Constraint<?>> cnstr;
    private final long start_time;
    private final long timeout;

    public VariableAVM(Collection<Constraint<?>> cnstr, long startTimeMillis, long timeout) {
        this.cnstr = cnstr;
        this.start_time = startTimeMillis;
        this.timeout = timeout;
    }

    protected boolean isFinished() {
        long current_time = System.currentTimeMillis();
        return (current_time - start_time) > timeout;
    }

    public abstract boolean applyAVM() throws SolverTimeoutException;
}
