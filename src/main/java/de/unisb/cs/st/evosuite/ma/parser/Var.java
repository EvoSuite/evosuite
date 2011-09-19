/**
 * 
 */
package de.unisb.cs.st.evosuite.ma.parser;

import de.unisb.cs.st.evosuite.testcase.VariableReference;
import japa.parser.ast.type.Type;

/**
 * @author Yury Pavlov
 * 
 */
public class Var {

	/**
	 * @uml.property  name="varName"
	 */
	private String varName;

	/**
	 * @uml.property  name="varBinding"
	 */
	private String varBinding;

	/**
	 * @uml.property  name="varType"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Type varType;

	/**
	 * @uml.property  name="varRef"
	 * @uml.associationEnd  
	 */
	private VariableReference varRef;

	public Var(String varName, String varBinding, Type varType,
			VariableReference varRef) {
		this.varName = varName;
		this.varBinding = varBinding;
		this.varType = varType;
		this.varRef = varRef;
	}

	/**
	 * @return  String
	 * @uml.property  name="varName"
	 */
	public String getVarName() {
		return varName;
	}

	/**
	 * @return  the varBinding
	 * @uml.property  name="varBinding"
	 */
	public String getVarBinding() {
		return varBinding;
	}

	/**
	 * @return  Type
	 * @uml.property  name="varType"
	 */
	public Type getVarType() {
		return varType;
	}

	/**
	 * @return  the varRef
	 * @uml.property  name="varRef"
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
		return "Var name: " + varName + " | Var binding: " + varBinding
				+ " | Var type: " + varType + " | Var reference: " + varRef;
	}
}
