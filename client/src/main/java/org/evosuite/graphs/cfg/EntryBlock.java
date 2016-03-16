/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.graphs.cfg;
public class EntryBlock extends BasicBlock {

	private static final long serialVersionUID = -4279595207017734232L;

	/**
	 * <p>Constructor for EntryBlock.</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 */
	public EntryBlock(String className, String methodName) {
		super(className, methodName);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEntryBlock() {
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "EntryBlock for method "+methodName;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getName();
	}
}
