/**
 * 
 */
package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.ParseException;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.type.Type;

import java.util.ArrayList;

import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Yury Pavlov
 * 
 */
public class TypeTable {

	/**
	 * @uml.property name="typeTable"
	 * @uml.associationEnd multiplicity="(0 -1)"
	 *                     elementType="de.unisb.cs.st.evosuite.ma.parser.Var"
	 */
	private final ArrayList<Var> typeTable = new ArrayList<Var>();

	public void addVar(Var var) {
		typeTable.add(var);
	}

	/**
	 * Return Type of var if exist. For Static var (first letter is capital)
	 * return null.
	 * 
	 * @param varName
	 * @return type of variable.
	 * @throws ParseException
	 *             if var not found in TT.
	 */
	public Type getType(String varName) throws ParseException {
		if (!TestParser.isStatic(varName)) {
			for (Var tmpVar : typeTable) {
				if (tmpVar.getVarName().equals(varName)) {
					return tmpVar.getVarType();
				}
			}
			throw new ParseException(null, "Type of: " + varName
					+ " not found!");
		}

		return null;
	}

	/**
	 * @param scope
	 * @return
	 * @throws ParseException
	 */
	public Type getType(Expression expr) throws ParseException {
		if (expr instanceof NameExpr) {
			return getType(((NameExpr) expr).getName());
		}
		if (expr instanceof FieldAccessExpr) {
			FieldAccessExpr fieldExpr = (FieldAccessExpr) expr;
			if (!TestParser.isStatic(fieldExpr.getScope().toString())) {
				return getType(fieldExpr.getScope().toString());
			}
		}
		// TODO Auto-generated method stub
		return null;
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
		String res = "";
		for (Var var : typeTable) {
			res += var + "\n";
		}
		return res;
	}

}
