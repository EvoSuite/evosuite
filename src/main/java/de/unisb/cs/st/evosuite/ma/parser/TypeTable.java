/**
 * 
 */
package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.ParseException;
import japa.parser.ast.type.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Yury Pavlov
 *
 */
public class TypeTable {
	
	private final ArrayList<Var> typeTable = new ArrayList<Var>();
	
	private final List<VariableReference> listVariableReferences = new ArrayList<VariableReference>();
	

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
	
	public void addVariableReference(Collection<VariableReference> varReferences) {
		listVariableReferences.addAll(varReferences);
	}
	
	/**
	 * @param methodCallExpr
	 * @param newTestCase
	 * @return
	 */
	//TODO implement with HashSet
	public VariableReference getVarReference(String calleeName) {
		for (VariableReference variableReference : listVariableReferences) {
			if (variableReference.getName().equals(calleeName)) {
				return variableReference;
			}
		}
		return null;
	}
	
//	/**
//	 * @param args
//	 * @return
//	 */
//	public List<VariableReference> getVarReferences(Collection<String> calleeNames) {
//		List<VariableReference> res = new ArrayList<VariableReference>();
//		for (String calleeName : calleeNames) {
//			res.add(getVarReference(calleeName));
//		}
//		return res;
//	}

}
