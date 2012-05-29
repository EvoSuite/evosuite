/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
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

import java.util.ArrayList;

import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Yury Pavlov
 * 
 */
public class TypeTable {

	private final ArrayList<Var> typeTable = new ArrayList<Var>();

	@Override
	public TypeTable clone() {
		TypeTable copy = new TypeTable();
		copy.typeTable.addAll(typeTable);
		return copy;
	}

	public void addVar(Var var) {
		typeTable.add(var);
	}

	public boolean hasVar(String varName) {
		for (Var tmpVar : typeTable) {
			if (tmpVar.getVarName().equals(varName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return Type of var if exist. For Static var (first letter is capital)
	 * return null.
	 * 
	 * @param varName
	 *            String
	 * @return Type of variable.
	 * @throws ParseException
	 *             if var not found in TT.
	 */
	public Type getType(String varName) throws ParseException {
		// if (!TestParser.isStatic(varName)) {
		for (Var tmpVar : typeTable) {
			if (tmpVar.getVarName().equals(varName)) {
				return tmpVar.getVarType();
			}
		}
		throw new ParseException(null, "Type of: " + varName + " not found!");
		// }

		// return null;
	}

	/**
	 * @param methodCallExpr
	 * @param newTestCase
	 * @return
	 * @throws ParseException
	 */
	// TODO implement with HashSet
	public VariableReference getVarReference(String varName)
			throws ParseException {
		for (Var var : typeTable) {
			if (var.getVarName().equals(varName)) {
				return var.getVarRef();
			}
		}
		throw new ParseException(null, "Var ref of: " + varName
				+ " not found in TT.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for (Var var : typeTable) {
			res.append(var + "\n");
		}
		return res.toString();
	}

	/**
	 * 
	 */
	public void reset() {
		typeTable.clear();
	}

	/**
	 * @param name
	 * @return
	 * @throws ParseException 
	 */
	public Class<?> getClass(String name) throws ParseException {
		for (Var var : typeTable) {
			if (var.getVarName().equals(name)) {
				return var.getVarRef().getVariableClass();
			}
		}
		throw new ParseException(null, "Var's class of: " + name
				+ " not found in TT.");
	}

}
