package de.unisb.cs.st.evosuite.ma.parser;

import de.unisb.cs.st.evosuite.testcase.VariableReference;
import japa.parser.ParseException;
import japa.parser.ast.type.Type;

/**
 * @author Yury Pavlov
 * 
 */
public class Var {

	private String varName;

	private Type varType;

	private VariableReference varRef;

	public Var(String varName, Type varType,
			VariableReference varRef) throws ParseException {
		if (varName == "") {
			throw new ParseException(null, "A var's name can't be leer.");
		} else if (varType == null) {
			throw new ParseException(null, "A var's type can't be null.");
		} else if (varRef == null) {
			throw new ParseException(null, "A var's references can't be null.");
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
		return "Var name: " + varName + " | Var type: " + varType
				+ " | Var reference: " + varRef;
	}
}
