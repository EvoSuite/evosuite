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
package org.evosuite.coverage.dataflow;

import org.evosuite.graphs.cfg.BytecodeInstruction;

/**
 * An object of this class corresponds to a Use inside the class under test.
 * <p>
 * Uses are created by the DefUseFactory via the DefUsePool.
 *
 * @author Andre Mis
 */
public class Use extends DefUse {

    private static final long serialVersionUID = -4951547090794898658L;

    Use(BytecodeInstruction wrap) {
        super(wrap);
        if (!DefUsePool.isKnownAsUse(wrap))
            throw new IllegalArgumentException("Instruction must be known as a Use by the DefUsePool");
    }

}
