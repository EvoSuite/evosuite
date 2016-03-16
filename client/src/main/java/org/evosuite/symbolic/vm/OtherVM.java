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
package org.evosuite.symbolic.vm;

import org.evosuite.dse.AbstractVM;

/**
 * 
 * @author galeotti
 *
 */
public final class OtherVM extends AbstractVM {

	private final SymbolicEnvironment env;

	public OtherVM(SymbolicEnvironment env) {
		this.env = env;
	}

	@Override
	public void UNUSED() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Enter synchronized region of code
	 */
	@Override
	public void MONITORENTER() {
		// discard symbolic argument
		this.env.topFrame().operandStack.popRef();
		// ignore this instruction
		return;
	}

	@Override
	public void MONITOREXIT() {
		// discard symbolic argument
		this.env.topFrame().operandStack.popRef();
		// ignore this instruction
		return;
	}

	@Override
	public void WIDE() {
		throw new UnsupportedOperationException();
	}

}
