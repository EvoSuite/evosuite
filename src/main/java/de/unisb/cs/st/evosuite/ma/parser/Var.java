/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.ParseException;
import japa.parser.ast.type.Type;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Yury Pavlov
 * 
 */
public class Var {

	private final String varName;

	private final Type varType;

	private final VariableReference varRef;

	public Var(String varName, Type varType, VariableReference varRef)
	        throws ParseException {
		if (varName.isEmpty()) {
			throw new ParseException(null,
			        "Creating new Var. A var's name can't be leer.");
			// } else if (varType == null) {
			// throw new ParseException(null,
			// "Creating new Var. A var's type can't be null.");
		} else if (varRef == null) {
			throw new ParseException(null,
			        "Creating new Var. A var's references can't be null.");
		}

		this.varName = varName;
		this.varType = varType;
		this.varRef = varRef;
	}

	/**
	 * @return String
	 */
	public String getVarName() {
		return varName;
	}

	/**
	 * @return Type
	 */
	public Type getVarType() {
		return varType;
	}

	/**
	 * @return the varRef
	 */
	public VariableReference getVarRef() {
		return varRef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Var name: " + varName + " | Var type: " + varType + " | Var reference: "
		        + varRef;
	}
}
