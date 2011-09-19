/**
 * 
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

	/**
	 * @uml.property  name="typeTable"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="de.unisb.cs.st.evosuite.ma.parser.Var"
	 */
	private final ArrayList<Var> typeTable = new ArrayList<Var>();

	public void addVar(Var var) {
		typeTable.add(var);
	}

	public Type getType(String varName) throws ParseException {
		for (Var tmpVar : typeTable) {
			if (tmpVar.getVarName().equals(varName)) {
				return tmpVar.getVarType();
			}
		}
		throw new ParseException(null, "Type of: " + varName + " not found!");
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
		throw new ParseException(null, "variableReference for var: " + varName
				+ " not found in TT.");
	}

	public VariableReference getVarReference() {
		for (Var var : typeTable) {

			return var.getVarRef();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String res = "";
		for (Var var : typeTable) {
			res += var + "\n";
		}
		return res;
	}

}
