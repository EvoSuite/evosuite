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
 * Can be used to create instances of Definition and Use
 * <p>
 * When given an instruction this factory asks the DefUsePool
 * whether it knows this instruction. If it does the pool
 * reveals the initially assigned defUseIDs which are then
 * put into the respective constructors.
 *
 * @author Andre Mis
 */
public class DefUseFactory {

    /**
     * Returns a Use instance given a BytecodeInstruction for which isUse() is
     * true
     *
     * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a {@link org.evosuite.coverage.dataflow.Use} object.
     */
    public static Use makeUse(BytecodeInstruction instruction) {
        if (!instruction.isUse())
            throw new IllegalArgumentException(
                    "expect wrap of a use to create one");
        if (!DefUsePool.isKnown(instruction))
            throw new IllegalArgumentException(
                    "expect DefUsePool to know the given BytecodeInstruction: " + instruction);

        return new Use(instruction);
    }

    /**
     * Returns a Definition instance given a BytecodeInstruction for which
     * isDefinition() is true
     *
     * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a {@link org.evosuite.coverage.dataflow.Definition} object.
     */
    public static Definition makeDefinition(BytecodeInstruction instruction) {
        if (!instruction.isDefinition())
            throw new IllegalArgumentException(
                    "expect wrap of a definition to create one");
        if (!DefUsePool.isKnown(instruction))
            throw new IllegalArgumentException(
                    "expect DefUsePool to know the given BytecodeInstruction");

        return new Definition(instruction);
    }

    /**
     * Convenience method to offer DefUse-Functionality for when
     * it doesn't matter whether a Definition or Use is returned
     * <p>
     * Returns a Definition instance
     * given a BytecodeInstruction for which isDefinition() is true
     * Otherwise returns a Use instance
     * given a BytecodeInstruction for which isUse() is true
     * <p>
     * WARNING: when given the wrap for an IINC this method will return a Definition
     *
     * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a {@link org.evosuite.coverage.dataflow.DefUse} object.
     */
    public static DefUse makeInstance(BytecodeInstruction instruction) {
        if (!instruction.isDefUse())
            throw new IllegalArgumentException("expect wrap of a defuse to create one");
        if (instruction.isDefinition())
            return makeDefinition(instruction);
        if (instruction.isUse())
            return makeUse(instruction);

        throw new IllegalStateException("either isUse() or isDefinition() must return true on a defuse");
    }
}
