/**
 * 
 */
package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.ast.type.Type;

import java.util.ArrayList;

/**
 * @author Yury Pavlov
 *
 */
public class TypeTable {
	ArrayList<Var> typeTable = new ArrayList<Var>();
	
	/**
	 * 
	 */
	public TypeTable() {
	}
	
	public void addVar(Var var) {
		typeTable.add(var);
	}
	
	public Type getType(String varName) {
		for (Var tmpVar : typeTable) {
			if (tmpVar.getVarName().equals(varName)) {
				return tmpVar.getVarType();
			}
		}
		return null;
	}

}
