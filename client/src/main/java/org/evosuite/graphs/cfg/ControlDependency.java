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

import java.io.Serializable;
import java.util.Objects;

public class ControlDependency implements Serializable, Comparable<ControlDependency> {

    private static final long serialVersionUID = 6288839964561655730L;

    private final Branch branch;
    private final boolean branchExpressionValue;

    /**
     * <p>Constructor for ControlDependency.</p>
     *
     * @param branch                a {@link org.evosuite.coverage.branch.Branch} object.
     * @param branchExpressionValue a boolean.
     */
    public ControlDependency(Branch branch, boolean branchExpressionValue) {
        if (branch == null)
            throw new IllegalArgumentException(
                    "control dependencies for the root branch are not permitted (null)");

        this.branch = branch;
        this.branchExpressionValue = branchExpressionValue;
    }

    /**
     * <p>Getter for the field <code>branch</code>.</p>
     *
     * @return a {@link org.evosuite.coverage.branch.Branch} object.
     */
    public Branch getBranch() {
        return branch;
    }

    /**
     * <p>Getter for the field <code>branchExpressionValue</code>.</p>
     *
     * @return a boolean.
     */
    public boolean getBranchExpressionValue() {
        return branchExpressionValue;
    }

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((branch == null) ? 0 : branch.hashCode());
//		result = prime * result + (branchExpressionValue ? 1231 : 1237);
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
//		ControlDependency other = (ControlDependency) obj;
//		if (branch == null) {
//			if (other.branch != null)
//				return false;
//		} else if (!branch.equals(other.branch))
//			return false;
//		if (branchExpressionValue != other.branchExpressionValue)
//			return false;
//		return true;
//	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        String r = "CD " + branch;

        if (!branch.isSwitchCaseBranch()) {
            if (branchExpressionValue)
                r += " - TRUE";
            else
                r += " - FALSE";
        }

        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControlDependency that = (ControlDependency) o;
        return branchExpressionValue == that.branchExpressionValue &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {

        return Objects.hash(branch, branchExpressionValue);
    }

    @Override
    public int compareTo(ControlDependency o) {
        int x = branch.compareTo(o.branch);
        if (x != 0)
            return x;

        if (branchExpressionValue == o.branchExpressionValue) {
            return 0;
        } else if (branchExpressionValue) {
            return 1;
        } else {
            return -1;
        }
    }
}
