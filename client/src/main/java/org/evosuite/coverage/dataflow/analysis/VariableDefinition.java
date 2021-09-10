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
package org.evosuite.coverage.dataflow.analysis;

import org.evosuite.graphs.cfg.BytecodeInstruction;

/**
 * A VariableDefinition consisting of a defining BytecodeInstruction and a
 * MethodCall.
 * <p>
 * Used in Inter-Method pair search algorithm to differentiate between
 * Intra-Method pairs and Inter-Method Pairs.
 * <p>
 * More or less just a pair of a BytecodeInstruction and a Methodcall.
 *
 * @author Andre Mis
 */
public class VariableDefinition {
    private final BytecodeInstruction definition;
    private final MethodCall call;

    public VariableDefinition(BytecodeInstruction definition,
                              MethodCall call) {
        this.definition = definition;
        this.call = call;
    }

    public BytecodeInstruction getDefinition() {
        return definition;
    }

    public MethodCall getMethodCall() {
        return call;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((call == null) ? 0 : call.hashCode());
        result = prime * result
                + ((definition == null) ? 0 : definition.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VariableDefinition other = (VariableDefinition) obj;
        if (call == null) {
            if (other.call != null)
                return false;
        } else if (!call.equals(other.call))
            return false;
        if (definition == null) {
            return other.definition == null;
        } else return definition.equals(other.definition);
    }

    public String toString() {
        return definition.toString() + " in " + call.toString();
    }
}