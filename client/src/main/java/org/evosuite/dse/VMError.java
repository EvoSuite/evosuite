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
package org.evosuite.dse;

/**
 * This error can be used to signal an throwable from VM code, below the
 * instrumented class. E.g. the instrumented class calls the method
 * VM.something() which throws and error.
 * 
 * @author galeotti
 */
public class VMError extends Error {

	public VMError(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4986434593568038501L;

}
