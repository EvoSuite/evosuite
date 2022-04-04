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
package org.evosuite.symbolic.solver.smt;

public final class SmtIntConstant extends SmtConstant {

    private final long longValue;

    public SmtIntConstant(int constantValue) {
        this.longValue = constantValue;
    }

    public SmtIntConstant(long constantValue) {
        this.longValue = constantValue;
    }

    public long getConstantValue() {
        return longValue;
    }

    @Override
    public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
        return v.visit(this, arg);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (longValue ^ (longValue >>> 32));
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
        SmtIntConstant other = (SmtIntConstant) obj;
        return longValue == other.longValue;
    }
}
