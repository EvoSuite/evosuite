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
 */
package org.evosuite.ma.parser;

import japa.parser.ParseException;
import japa.parser.ast.type.Type;

import java.util.ArrayList;

import org.evosuite.testcase.VariableReference;


/**
 * <p>TypeTable class.</p>
 *
 * @author Yury Pavlov
 */
public class TypeTable implements Cloneable {

	private final ArrayList<Var> typeTable = new ArrayList<Var>();

	/** {@inheritDoc} */
	@Override
	public TypeTable clone() {
		TypeTable copy = new TypeTable();
		copy.typeTable.addAll(typeTable);
		return copy;
	}

	/**
	 * <p>addVar</p>
	 *
	 * @param var a {@link org.evosuite.ma.parser.Var} object.
	 */
	public void addVar(Var var) {
		typeTable.add(var);
	}

	/**
	 * <p>hasVar</p>
	 *
	 * @param varName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
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
	 * @throws japa.parser.ParseException
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
	 * <p>getVarReference</p>
	 *
	 * @throws japa.parser.ParseException if any.
	 * @param varName a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	// TODO implement with HashSet
	public VariableReference getVarReference(String varName) throws ParseException {
		for (Var var : typeTable) {
			if (var.getVarName().equals(varName)) {
				return var.getVarRef();
			}
		}
		throw new ParseException(null, "Var ref of: " + varName + " not found in TT.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for (Var var : typeTable) {
			res.append(var + "\n");
		}
		return res.toString();
	}

	/**
	 * <p>reset</p>
	 */
	public void reset() {
		typeTable.clear();
	}

	/**
	 * <p>getClass</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @throws japa.parser.ParseException if any.
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<?> getClass(String name) throws ParseException {
		for (Var var : typeTable) {
			if (var.getVarName().equals(name)) {
				return var.getVarRef().getVariableClass();
			}
		}
		throw new ParseException(null, "Var's class of: " + name + " not found in TT.");
	}

}
