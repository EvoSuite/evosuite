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
package org.evosuite.graphs.cfg;

import org.evosuite.coverage.branch.Branch;
import org.jgrapht.graph.DefaultEdge;

public class ControlFlowEdge extends DefaultEdge {

    private static final long serialVersionUID = -5009449930477928101L;

    private ControlDependency cd;
    private boolean isExceptionEdge;

    /**
     * <p>Constructor for ControlFlowEdge.</p>
     */
    public ControlFlowEdge() {
        this.cd = null;
        this.isExceptionEdge = false;
    }

    /**
     * <p>Constructor for ControlFlowEdge.</p>
     *
     * @param isExceptionEdge a boolean.
     */
    public ControlFlowEdge(boolean isExceptionEdge) {
        this.isExceptionEdge = isExceptionEdge;
    }

    /**
     * <p>Constructor for ControlFlowEdge.</p>
     *
     * @param cd              a {@link org.evosuite.graphs.cfg.ControlDependency} object.
     * @param isExceptionEdge a boolean.
     */
    public ControlFlowEdge(ControlDependency cd, boolean isExceptionEdge) {
        this.cd = cd;
        this.isExceptionEdge = isExceptionEdge;
    }


    /**
     * Sort of a copy constructor
     *
     * @param clone a {@link org.evosuite.graphs.cfg.ControlFlowEdge} object.
     */
    public ControlFlowEdge(ControlFlowEdge clone) {
        if (clone != null) {
            this.cd = clone.cd;
            this.isExceptionEdge = clone.isExceptionEdge;
        }
    }

    /**
     * <p>getControlDependency</p>
     *
     * @return a {@link org.evosuite.graphs.cfg.ControlDependency} object.
     */
    public ControlDependency getControlDependency() {
        return cd;
    }

    /**
     * <p>hasControlDependency</p>
     *
     * @return a boolean.
     */
    public boolean hasControlDependency() {
        return cd != null;
    }

    /**
     * <p>getBranchInstruction</p>
     *
     * @return a {@link org.evosuite.coverage.branch.Branch} object.
     */
    public Branch getBranchInstruction() {
        if (cd == null)
            return null;

        return cd.getBranch();
    }

    /**
     * <p>isExceptionEdge</p>
     *
     * @return a boolean.
     */
    public boolean isExceptionEdge() {
        return isExceptionEdge;
    }

    /**
     * <p>getBranchExpressionValue</p>
     *
     * @return a boolean.
     */
    public boolean getBranchExpressionValue() {
        if (hasControlDependency())
            return cd.getBranchExpressionValue();

        return true;
    }

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((cd == null) ? 0 : cd.hashCode());
//		result = prime * result + (isExceptionEdge ? 1231 : 1237);
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		ControlFlowEdge other = (ControlFlowEdge) obj;
//		if (cd == null) {
//			if (other.cd != null)
//				return false;
//		} else if (!cd.equals(other.cd))
//			return false;
//		if (isExceptionEdge != other.isExceptionEdge)
//			return false;
//		return true;
//	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String r = "";
        if (isExceptionEdge)
            r += "E ";
        if (cd != null)
            r += cd.toString();
        return r;
    }
}
