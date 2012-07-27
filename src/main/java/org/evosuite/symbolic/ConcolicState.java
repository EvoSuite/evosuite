package org.evosuite.symbolic;

import java.util.HashMap;
import java.util.Map;

import edu.uta.cse.dsc.ast.BitVector32;
import edu.uta.cse.dsc.ast.JvmExpression;
import edu.uta.cse.dsc.ast.JvmVariable;
import edu.uta.cse.dsc.ast.Reference;
import edu.uta.cse.dsc.ast.reference.LiteralNonNullReference;
import edu.uta.cse.dsc.ast.z3array.JavaFieldVariable;
import edu.uta.cse.dsc.ast.z3array.Z3ArrayLiteral;
import edu.uta.cse.dsc.ast.z3array.Z3ArrayVariable;

/**
 * Maps each variable/mapping to its symbolic/concrete value
 * 
 * @author galeotti
 * 
 */
public final class ConcolicState {

	/**
	 * symbolic variables
	 */
	private final HashMap<JvmVariable, String> symbolicVariables;

	/**
	 * Symbolic state
	 */
	private final HashMap<Z3ArrayVariable<?, ?>, HashMap<LiteralNonNullReference, JvmExpression>> symbolicFieldMappings = new HashMap<Z3ArrayVariable<?, ?>, HashMap<LiteralNonNullReference, JvmExpression>>();
	private final HashMap<Z3ArrayVariable<?, ?>, HashMap<BitVector32, JvmExpression>> symbolicArrayMappings = new HashMap<Z3ArrayVariable<?, ?>, HashMap<BitVector32, JvmExpression>>();
	private final HashMap<JvmVariable, JvmExpression> symbolicValues = new HashMap<JvmVariable, JvmExpression>();

	/**
	 * Concrete state
	 */
	private final HashMap<Z3ArrayVariable<?, ?>, HashMap<LiteralNonNullReference, JvmExpression>> concreteFieldMappings = new HashMap<Z3ArrayVariable<?, ?>, HashMap<LiteralNonNullReference, JvmExpression>>();
	private final HashMap<Z3ArrayVariable<?, ?>, HashMap<BitVector32, JvmExpression>> concreteArrayMappings = new HashMap<Z3ArrayVariable<?, ?>, HashMap<BitVector32, JvmExpression>>();
	private final HashMap<JvmVariable, JvmExpression> concreteValues = new HashMap<JvmVariable, JvmExpression>();

	public ConcolicState(Map<JvmVariable, String> symbolicVariables) {
		this.symbolicVariables = new HashMap<JvmVariable, String>(
				symbolicVariables);
	}

	public void updateExistingMapping(Z3ArrayVariable<?, ?> fresh_map_var,
			Z3ArrayVariable<?, ?> map_var, JvmExpression index,
			JvmExpression symbolic_value, JvmExpression concrete_value) {

		checkNotContainsMapVar(fresh_map_var);

		if (symbolicFieldMappings.containsKey(map_var)) {
			Reference index_reference = (Reference) index;
			LiteralNonNullReference literal_index = (LiteralNonNullReference) index_reference;

			{
				HashMap<LiteralNonNullReference, JvmExpression> symbolicFieldMapping = symbolicFieldMappings
						.get(map_var);
				HashMap<LiteralNonNullReference, JvmExpression> newSymbolicFiedlMapping = new HashMap<LiteralNonNullReference, JvmExpression>(
						symbolicFieldMapping);
				newSymbolicFiedlMapping.put(literal_index, symbolic_value);
				symbolicFieldMappings.put(fresh_map_var,
						newSymbolicFiedlMapping);
			}
			{
				HashMap<LiteralNonNullReference, JvmExpression> concreteFieldMapping = concreteFieldMappings
						.get(map_var);
				HashMap<LiteralNonNullReference, JvmExpression> newConcreteFiedlMapping = new HashMap<LiteralNonNullReference, JvmExpression>(
						concreteFieldMapping);
				newConcreteFiedlMapping.put(literal_index, concrete_value);
				concreteFieldMappings.put(fresh_map_var,
						newConcreteFiedlMapping);

			}

		} else if (symbolicArrayMappings.containsKey(map_var)) {
			BitVector32 int_index = (BitVector32) index;
			{
				HashMap<BitVector32, JvmExpression> symbolicArrayMapping = symbolicArrayMappings
						.get(map_var);
				HashMap<BitVector32, JvmExpression> newSymbolicArrayMapping = new HashMap<BitVector32, JvmExpression>(
						symbolicArrayMapping);
				newSymbolicArrayMapping.put(int_index, symbolic_value);
				symbolicArrayMappings.put(fresh_map_var,
						newSymbolicArrayMapping);
			}
			{
				HashMap<BitVector32, JvmExpression> concreteArrayMapping = concreteArrayMappings
						.get(map_var);
				HashMap<BitVector32, JvmExpression> newConcreteArrayMapping = new HashMap<BitVector32, JvmExpression>(
						concreteArrayMapping);
				newConcreteArrayMapping.put(int_index, concrete_value);
				concreteArrayMappings.put(fresh_map_var,
						newConcreteArrayMapping);

			}
		} else {
			throw new IllegalArgumentException("I can not update map variable "
					+ map_var + " because it is not defined!");
		}
	}

	public void createNewFieldMapping(Z3ArrayVariable<?, ?> fresh_map_var,
			JavaFieldVariable java_field_variable, Reference reference_index,
			JvmExpression symbolic_value, JvmExpression concrete_value) {

		checkNotContainsMapVar(fresh_map_var);
		{
			HashMap<LiteralNonNullReference, JvmExpression> symbolicFieldMapping = new HashMap<LiteralNonNullReference, JvmExpression>();
			LiteralNonNullReference index = (LiteralNonNullReference) reference_index;
			symbolicFieldMapping.put(index, symbolic_value);
			symbolicFieldMappings.put(fresh_map_var, symbolicFieldMapping);
		}
		{
			HashMap<LiteralNonNullReference, JvmExpression> concreteFieldMapping = new HashMap<LiteralNonNullReference, JvmExpression>();
			LiteralNonNullReference index = (LiteralNonNullReference) reference_index;
			concreteFieldMapping.put(index, concrete_value);
			concreteFieldMappings.put(fresh_map_var, concreteFieldMapping);

		}
	}

	private void checkNotContainsMapVar(Z3ArrayVariable<?, ?> fresh_map_var) {
		if (this.symbolicFieldMappings.containsKey(fresh_map_var)
				|| this.symbolicArrayMappings.containsKey(fresh_map_var)) {
			throw new IllegalArgumentException(fresh_map_var.getName()
					+ " was already defined in mapping!");
		}
	}

	public void declareNewSymbolicVariable(JvmVariable fresh_var,
			JvmExpression symbolic_value, JvmExpression concrete_value) {

		if (symbolic_value == null) {
			throw new IllegalArgumentException(
					"Cannot store null for variable " + fresh_var.getName());
		}

		symbolicValues.put(fresh_var, symbolic_value);
		concreteValues.put(fresh_var, concrete_value);
	}

	public void clear() {
		// mappings
		this.symbolicArrayMappings.clear();
		this.symbolicFieldMappings.clear();
		this.concreteArrayMappings.clear();
		this.concreteFieldMappings.clear();
		// variables
		this.symbolicValues.clear();
		this.concreteValues.clear();
	}

	public boolean isAlreadyDefined(JvmVariable v) {
		return this.symbolicFieldMappings.containsKey(v)
				|| this.symbolicArrayMappings.containsKey(v)
				|| this.symbolicValues.containsKey(v);
	}

	public boolean isMarked(JvmVariable v) {
		return this.symbolicVariables.containsKey(v);
	}

	public String getSymbolicName(JvmVariable b) {
		if (!this.symbolicVariables.containsKey(b))
			throw new IllegalArgumentException(b.toString()
					+ " is not a symbolic variable!");

		return this.symbolicVariables.get(b);
	}

	public JvmExpression getSymbolicValue(Z3ArrayVariable<?, ?> map_var,
			JvmExpression index_expr) {

		if (this.symbolicFieldMappings.containsKey(map_var)) {

			Reference index_reference = (Reference) index_expr;
			LiteralNonNullReference literal_index = (LiteralNonNullReference) index_reference;
			HashMap<LiteralNonNullReference, JvmExpression> symbolicFieldMap = this.symbolicFieldMappings
					.get(map_var);
			return symbolicFieldMap.get(literal_index);

		} else if (this.symbolicArrayMappings.containsKey(map_var)) {

			BitVector32 bitVector32 = (BitVector32) index_expr;
			HashMap<BitVector32, JvmExpression> symbolicArrayMap = this.symbolicArrayMappings
					.get(map_var);
			return symbolicArrayMap.get(bitVector32);

		} else {
			throw new IllegalArgumentException("Mapping variable "
					+ map_var.getName() + " is not defined!");

		}

	}

	public JvmExpression getSymbolicValue(JvmVariable v) {
		if (!this.symbolicValues.containsKey(v)) {
			throw new IllegalArgumentException("Variable " + v.getName()
					+ " is undefined!");
		}

		return this.symbolicValues.get(v);
	}

	public JvmExpression getConcreteValue(JvmVariable v) {
		if (!this.concreteValues.containsKey(v)) {
			throw new IllegalArgumentException("Variable " + v.getName()
					+ " is undefined!");
		}

		return this.concreteValues.get(v);
	}

	public void createNewArrayMapping(Z3ArrayVariable<?, ?> fresh_map_var,
			Z3ArrayLiteral<?, ?> arrayLiteral, BitVector32 int_index,
			JvmExpression symbolic_value, JvmExpression concrete_value) {

		{
			HashMap<BitVector32, JvmExpression> symbolicArrayMapping = new HashMap<BitVector32, JvmExpression>();
			symbolicArrayMapping.put(int_index, symbolic_value);
			this.symbolicArrayMappings.put(fresh_map_var, symbolicArrayMapping);
		}
		{
			HashMap<BitVector32, JvmExpression> concreteArrayMapping = new HashMap<BitVector32, JvmExpression>();
			concreteArrayMapping.put(int_index, concrete_value);
			this.concreteArrayMappings.put(fresh_map_var, concreteArrayMapping);
		}
	}

	public JvmExpression getConcreteValue(Z3ArrayVariable<?, ?> map_var,
			JvmExpression index_expr) {
		if (this.concreteFieldMappings.containsKey(map_var)) {

			Reference index_reference = (Reference) index_expr;
			LiteralNonNullReference literal_index = (LiteralNonNullReference) index_reference;
			HashMap<LiteralNonNullReference, JvmExpression> symbolicFieldMap = this.concreteFieldMappings
					.get(map_var);
			return symbolicFieldMap.get(literal_index);

		} else if (this.concreteArrayMappings.containsKey(map_var)) {

			BitVector32 bitVector32 = (BitVector32) index_expr;
			HashMap<BitVector32, JvmExpression> symbolicArrayMap = this.concreteArrayMappings
					.get(map_var);
			return symbolicArrayMap.get(bitVector32);

		} else {
			throw new IllegalArgumentException("Mapping variable "
					+ map_var.getName() + " is not defined!");

		}
	}
}