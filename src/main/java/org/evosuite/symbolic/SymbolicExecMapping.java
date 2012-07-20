package org.evosuite.symbolic;

import java.util.HashMap;
import java.util.Map;

import edu.uta.cse.dsc.ast.JvmExpression;
import edu.uta.cse.dsc.ast.reference.LiteralNonNullReference;
import edu.uta.cse.dsc.ast.z3array.JavaFieldVariable;

/**
 * Represents a map
 * LiteralNonNullReference->JvmExpression
 * 
 * e.g.
 * int fields, long fields, Object fields are modelled respectively 
 * as 
 * LiteralNonNullReference->BitVector32
 * LiteralNonNullReference->BitVector64
 * LiteralNonNullReference->LiteralReference
 *
 * @author galeotti
 *
 * @param <T>
 */
class SymbolicExecMapping<T extends JvmExpression> {
	private JavaFieldVariable javaFieldVariable;
	private Map<String, T> values = new HashMap<String, T>();

	public SymbolicExecMapping(SymbolicExecMapping<T> mapping) {
		this(mapping.javaFieldVariable);
		values.putAll(mapping.values);
	}

	public SymbolicExecMapping(JavaFieldVariable javaFieldVariable) {
		this.javaFieldVariable = javaFieldVariable;
	}

	public void put(LiteralNonNullReference index, T value) {
		String index_str = index.toString();
		this.values.put(index_str, value);
	}

	public boolean containsKey(LiteralNonNullReference index) {
		String index_str = index.toString();
		return values.containsKey(index_str);
	}

	public T get(LiteralNonNullReference index) {
		String index_str = index.toString();
		return values.get(index_str);
	}

	public JavaFieldVariable getJavaFieldVariable() {
		return javaFieldVariable;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return null;
	}

}