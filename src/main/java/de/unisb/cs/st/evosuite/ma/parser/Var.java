/**
 * 
 */
package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.ast.type.Type;

/**
 * @author Yury Pavlov
 *
 */
public class Var {
	private String varName;
	private Type varType;
	
	public Var(String varName, Type varType) {
		this.varName = varName;
		this.varType = varType;
	}
	
	/**
	 * @return the varName
	 */
	public String getVarName() {
		return varName;
	}
	
	/**
	 * @return the varType
	 */
	public Type getVarType() {
		return varType;
	}
}
