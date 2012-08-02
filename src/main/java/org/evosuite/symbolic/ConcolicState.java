package org.evosuite.symbolic;

import java.util.HashMap;
import java.util.Map;

import edu.uta.cse.dsc.MainConfig;
import edu.uta.cse.dsc.ast.ArrayReference;
import edu.uta.cse.dsc.ast.BitVector32;
import edu.uta.cse.dsc.ast.JvmExpression;
import edu.uta.cse.dsc.ast.JvmVariable;
import edu.uta.cse.dsc.ast.Reference;
import edu.uta.cse.dsc.ast.reference.LiteralNonNullReference;
import edu.uta.cse.dsc.ast.z3array.JavaFieldVariable;
import edu.uta.cse.dsc.ast.z3array.Z3ArrayVariable;

/**
 * Maps each variable/mapping to its symbolic/concrete value
 * 
 * @author galeotti
 * 
 */
public final class ConcolicState {

	private static class State {

		private final HashMap<Z3ArrayVariable<?, ?>, HashMap<LiteralNonNullReference, JvmExpression>> fieldValues = new HashMap<Z3ArrayVariable<?, ?>, HashMap<LiteralNonNullReference, JvmExpression>>();
		private final HashMap<Z3ArrayVariable<?, ?>, HashMap<ArrayReference<?>, HashMap<BitVector32, JvmExpression>>> arrayValues = new HashMap<Z3ArrayVariable<?, ?>, HashMap<ArrayReference<?>, HashMap<BitVector32, JvmExpression>>>();

		private final HashMap<JvmVariable, JvmExpression> variableValues = new HashMap<JvmVariable, JvmExpression>();

		public void clear() {
			this.fieldValues.clear();
			this.variableValues.clear();
			this.arrayValues.clear();
		}

		public void putNewValue(JvmVariable variable, JvmExpression value) {
			this.variableValues.put(variable, value);
		}

		public JvmExpression getValue(JvmVariable variable) {

			if (!this.variableValues.containsKey(variable)) {

				throw new IllegalArgumentException("Variable "
						+ variable.getName() + " is undefined!");
			}

			return this.variableValues.get(variable);
		}

		public JvmExpression getValue(Z3ArrayVariable<?, ?> map_var,
				JvmExpression index_expr) {
			if (this.fieldValues.containsKey(map_var)) {

				Reference index_reference = (Reference) index_expr;
				LiteralNonNullReference literal_index = (LiteralNonNullReference) index_reference;
				HashMap<LiteralNonNullReference, JvmExpression> fieldContents = this.fieldValues
						.get(map_var);
				JvmExpression valueOf = fieldContents.get(literal_index);
				return valueOf;

			} else {
				throw new IllegalArgumentException("Mapping variable "
						+ map_var.getName() + " is not defined!");

			}

		}

		public boolean containsVariable(JvmVariable v) {
			return this.variableValues.containsKey(v);
		}

		private boolean containsVariable(Z3ArrayVariable<?, ?> v) {
			return this.fieldValues.containsKey(v);
		}

		public void putNewValue(Z3ArrayVariable<?, ?> fresh_map_var,
				JvmExpression reference_index, JvmExpression value) {

			checkFieldNotExists(fresh_map_var);

			HashMap<LiteralNonNullReference, JvmExpression> symbolicFieldMapping = new HashMap<LiteralNonNullReference, JvmExpression>();
			LiteralNonNullReference index = (LiteralNonNullReference) reference_index;
			symbolicFieldMapping.put(index, value);
			fieldValues.put(fresh_map_var, symbolicFieldMapping);
		}

		private void checkFieldNotExists(Z3ArrayVariable<?, ?> fresh_map_var) {
			if (this.fieldValues.containsKey(fresh_map_var)) {
				throw new IllegalArgumentException(fresh_map_var.getName()
						+ " was already defined in mapping!");
			}
		}

		public void putNewValue(Z3ArrayVariable<?, ?> fresh_map_var,
				Z3ArrayVariable<?, ?> map_var, LiteralNonNullReference index,
				JvmExpression value) {

			checkFieldNotExists(fresh_map_var);
			checkFieldExists(map_var);

			HashMap<LiteralNonNullReference, JvmExpression> fieldMapping = fieldValues
					.get(map_var);
			HashMap<LiteralNonNullReference, JvmExpression> newFieldMapping = new HashMap<LiteralNonNullReference, JvmExpression>(
					fieldMapping);
			newFieldMapping.put(index, value);
			fieldValues.put(fresh_map_var, newFieldMapping);
		}

		private void checkFieldExists(Z3ArrayVariable<?, ?> map_var) {
			if (!fieldValues.containsKey(map_var)) {
				fieldValues.keySet();
				throw new IllegalArgumentException(
						"I can not update map variable " + map_var
								+ " because it is not defined!");
			}
		}

		public void putNewValue(Z3ArrayVariable<?, ?> fresh_map_var,
				Z3ArrayVariable<?, ?> map_var, ArrayReference<?> arrayRef,
				JvmExpression index, JvmExpression value) {

			checkArrayNotExists(fresh_map_var);
			if (map_var != null) {
				checkArrayExists(map_var);
			}

			HashMap<ArrayReference<?>, HashMap<BitVector32, JvmExpression>> oldArrayToArrayContents;
			if (map_var != null) {
				oldArrayToArrayContents = arrayValues.get(map_var);
			} else {
				oldArrayToArrayContents = new HashMap<ArrayReference<?>, HashMap<BitVector32, JvmExpression>>();
			}

			HashMap<ArrayReference<?>, HashMap<BitVector32, JvmExpression>> newArrayToArrayContents = new HashMap<ArrayReference<?>, HashMap<BitVector32, JvmExpression>>();

			for (ArrayReference<?> arrayReference : oldArrayToArrayContents
					.keySet()) {
				HashMap<BitVector32, JvmExpression> oldArrayContents = oldArrayToArrayContents
						.get(arrayReference);
				HashMap<BitVector32, JvmExpression> newArrayContents = new HashMap<BitVector32, JvmExpression>(
						oldArrayContents);
				newArrayToArrayContents.put(arrayReference, newArrayContents);
			}

			if (!oldArrayToArrayContents.containsKey(arrayRef)) {
				newArrayToArrayContents.put(arrayRef,
						new HashMap<BitVector32, JvmExpression>());
			}
			BitVector32 int_index = (BitVector32) index;
			newArrayToArrayContents.get(arrayRef).put(int_index, value);

			this.arrayValues.put(fresh_map_var, newArrayToArrayContents);

		}

		public void putNewValue(Z3ArrayVariable<?, ?> fresh_map_variable,
				ArrayReference<?> arrayRef, JvmExpression index,
				JvmExpression value) {

			checkArrayNotExists(fresh_map_variable);

			BitVector32 int_index = (BitVector32) index;
			HashMap<BitVector32, JvmExpression> arrayContents = new HashMap<BitVector32, JvmExpression>();
			arrayContents.put(int_index, value);

			HashMap<ArrayReference<?>, HashMap<BitVector32, JvmExpression>> arrayToContentsMap = new HashMap<ArrayReference<?>, HashMap<BitVector32, JvmExpression>>();
			arrayToContentsMap.put(arrayRef, arrayContents);

			arrayValues.put(fresh_map_variable, arrayToContentsMap);

		}

		private void checkArrayNotExists(Z3ArrayVariable<?, ?> v) {
			if (this.arrayValues.containsKey(v)) {
				throw new IllegalArgumentException(v.getName()
						+ " was already defined in mapping!");
			}
		}

		private void checkArrayExists(Z3ArrayVariable<?, ?> v) {
			if (!this.arrayValues.containsKey(v)) {
				throw new IllegalArgumentException(v.getName()
						+ " was not defined in mapping!");
			}
		}

		public JvmExpression getValue(Z3ArrayVariable<?, ?> map_var,
				ArrayReference<?> array_ref, JvmExpression index) {

			BitVector32 array_index = (BitVector32) index;
			JvmExpression valueOf = arrayValues.get(map_var).get(array_ref)
					.get(array_index);
			if (valueOf == null) {
				// return array default value
				throw new UnsupportedOperationException();
			}
			return valueOf;
		}
	}

	/**
	 * symbolic variables
	 */
	private final HashMap<JvmVariable, String> symbolicVariables;

	/**
	 * Symbolic state
	 */
	private final State symbolicState = new State();

	/**
	 * Concrete state
	 */
	private final State concreteState = new State();

	public ConcolicState(Map<JvmVariable, String> symbolicVariables) {
		this.symbolicVariables = new HashMap<JvmVariable, String>(
				symbolicVariables);
	}

	public void updateZ3ArrayVariable(Z3ArrayVariable<?, ?> fresh_map_var,
			Z3ArrayVariable<?, ?> map_var, JvmExpression index,
			JvmExpression symbolic_value, JvmExpression concrete_value) {

		checkNotContainsMapVar(fresh_map_var);

		Reference index_reference = (Reference) index;
		LiteralNonNullReference literal_index = (LiteralNonNullReference) index_reference;

		symbolicState.putNewValue(fresh_map_var, map_var, literal_index,
				symbolic_value);

		concreteState.putNewValue(fresh_map_var, map_var, literal_index,
				concrete_value);

	}

	public void updateZ3ArrayVariable(Z3ArrayVariable<?, ?> fresh_map_var,
			Z3ArrayVariable<?, ?> map_var, ArrayReference<?> arrayRef,
			JvmExpression index, JvmExpression symbolic_value,
			JvmExpression concrete_value) {

		symbolicState.putNewValue(fresh_map_var, map_var, arrayRef, index,
				symbolic_value);

		concreteState.putNewValue(fresh_map_var, map_var, arrayRef, index,
				concrete_value);

	}

	public void updateJavaFieldVariable(Z3ArrayVariable<?, ?> fresh_map_var,
			JavaFieldVariable java_field_variable, Reference reference_index,
			JvmExpression symbolic_value, JvmExpression concrete_value) {

		LiteralNonNullReference index = (LiteralNonNullReference) reference_index;
		checkNotContainsMapVar(fresh_map_var);
		symbolicState.putNewValue(fresh_map_var, index, symbolic_value);
		concreteState.putNewValue(fresh_map_var, index, concrete_value);
	}

	private void checkNotContainsMapVar(Z3ArrayVariable<?, ?> fresh_map_var) {
		if (this.symbolicState.containsVariable(fresh_map_var)) {
			throw new IllegalArgumentException(fresh_map_var.getName()
					+ " was already defined in mapping!");
		}
	}

	public void updateJvmVariable(JvmVariable fresh_var,
			JvmExpression symbolic_value, JvmExpression concrete_value) {

		if (symbolic_value == null) {
			throw new IllegalArgumentException(
					"Cannot store null for variable " + fresh_var.getName());
		}

		symbolicState.putNewValue(fresh_var, symbolic_value);
		concreteState.putNewValue(fresh_var, concrete_value);

	}

	public void clear() {
		this.symbolicState.clear();
		this.concreteState.clear();
	}

	public boolean containsVariable(JvmVariable v) {
		return this.symbolicState.containsVariable(v);
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

		JvmExpression symbolicValue = this.symbolicState.getValue(map_var,
				index_expr);
		return symbolicValue;

	}

	/**
	 * This method looks the concrete values table for a definition of variable
	 * v. If the variable name ends with <code>#arrayLength</code> the length of
	 * the literal array is returned.
	 * 
	 * @param v
	 * @return
	 */
	public JvmExpression getSymbolicValue(JvmVariable v) {
		return this.symbolicState.getValue(v);
	}

	/**
	 * This method looks the concrete values table for a definition of variable
	 * v. If the variable name ends with <code>#arrayLength</code> the length of
	 * the literal array is returned.
	 * 
	 * @param v
	 * @return
	 */
	public JvmExpression getConcreteValue(JvmVariable v) {

		return this.concreteState.getValue(v);
	}

	public JvmExpression getConcreteValue(Z3ArrayVariable<?, ?> map_var,
			JvmExpression index_expr) {

		return concreteState.getValue(map_var, index_expr);
	}

	public void updateArrayContents(Z3ArrayVariable<?, ?> fresh_map_variable,
			ArrayReference<?> arrayRef, JvmExpression arrayIndex,
			JvmExpression symbolic_value, JvmExpression concrete_value) {

		this.symbolicState.putNewValue(fresh_map_variable, arrayRef,
				arrayIndex, symbolic_value);
		this.concreteState.putNewValue(fresh_map_variable, arrayRef,
				arrayIndex, concrete_value);
	}

	public JvmExpression getSymbolicValue(Z3ArrayVariable<?, ?> map_var,
			ArrayReference<?> array_ref, JvmExpression index) {

		return this.symbolicState.getValue(map_var, array_ref, index);
	}

	public JvmExpression getConcreteValue(Z3ArrayVariable<?, ?> map_var,
			ArrayReference<?> array_ref, JvmExpression index) {
		return this.concreteState.getValue(map_var, array_ref, index);
	}
}