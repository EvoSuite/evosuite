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

import static org.evosuite.dse.util.Assertions.check;

import java.lang.reflect.Member;


/**
 * Frame at the bottom of the invocation stack.
 * Keep this on the stack frame, in case we accidentally
 * transitively invoke some instrumented method.  
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class FakeBottomFrame extends Frame {


	/**
	 * Constructor
	 */
	public FakeBottomFrame() {
		super(0);
		super.invokeInstrumentedCode(false);
	}
	
	@Override
	public void invokeInstrumentedCode(boolean b) {
	  check(false);
	}
	
	@Override
	public int getNrFormalParameters() {
	  check(false);
	  return 0;
	}
	
  @Override
  public int getNrFormalParametersTotal() {
    check(false);
    return 0;
  }	
	
	@Override
	public Member getMember() {
		return null;
	}
}
