package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.ParseException;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.NameExpr;
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
	 * @param scope
	 * @return
	 * @throws ParseException
	 */
	public Type getType(Expression expr) throws ParseException {
		if (expr instanceof NameExpr) {
			return getType(((NameExpr) expr).getName());
		}
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
