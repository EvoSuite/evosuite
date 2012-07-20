package org.evosuite.symbolic;

import java.util.HashMap;
import java.util.Map;

import edu.uta.cse.dsc.ast.BitVector32;
import edu.uta.cse.dsc.ast.BitVector64;
import edu.uta.cse.dsc.ast.DoubleExpression;
import edu.uta.cse.dsc.ast.FloatExpression;
import edu.uta.cse.dsc.ast.JvmExpression;
import edu.uta.cse.dsc.ast.JvmVariable;
import edu.uta.cse.dsc.ast.Reference;
import edu.uta.cse.dsc.ast.bitvector.BitVector32Variable;
import edu.uta.cse.dsc.ast.bitvector.BitVector64Variable;
import edu.uta.cse.dsc.ast.fp.DoubleVariable;
import edu.uta.cse.dsc.ast.fp.FloatVariable;
import edu.uta.cse.dsc.ast.reference.LiteralNonNullReference;
import edu.uta.cse.dsc.ast.reference.LiteralReference;
import edu.uta.cse.dsc.ast.z3array.JavaFieldVariable;
import edu.uta.cse.dsc.ast.z3array.Z3ArrayVariable;

/**
 * Maps each variable/mapping to its symbolic state
 * 
 * @author galeotti
 * 
 */
public class SymbolicExecState {

	private final HashMap<String, SymbolicExecMapping<BitVector32>> intMappings;
	private final HashMap<String, SymbolicExecMapping<BitVector64>> longMappings;
	private final HashMap<String, SymbolicExecMapping<LiteralReference>> refMappings;
	private final HashMap<String, SymbolicExecMapping<FloatExpression>> floatMappings;
	private final HashMap<String, SymbolicExecMapping<DoubleExpression>> doubleMappings;

	private final HashMap<String, BitVector32> intVariables;
	private final HashMap<String, BitVector64> longVariables;
	private final HashMap<String, FloatExpression> floatVariables;
	private final HashMap<String, DoubleExpression> doubleVariables;

	private final HashMap<String, LiteralReference> refVariables;
	private final HashMap<JvmVariable, String> symbolicVariables;

	private final HashMap<String, JvmExpression>[] all_mappings;

	public SymbolicExecState(Map<JvmVariable, String> symbolicVariables) {

		this.intMappings = new HashMap<String, SymbolicExecMapping<BitVector32>>();
		this.longMappings = new HashMap<String, SymbolicExecMapping<BitVector64>>();
		this.floatMappings = new HashMap<String, SymbolicExecMapping<FloatExpression>>();
		this.doubleMappings = new HashMap<String, SymbolicExecMapping<DoubleExpression>>();
		this.refMappings = new HashMap<String, SymbolicExecMapping<LiteralReference>>();

		all_mappings = new HashMap[] { intMappings, longMappings, refMappings,
				floatMappings, doubleMappings };

		this.intVariables = new HashMap<String, BitVector32>();
		this.longVariables = new HashMap<String, BitVector64>();
		this.floatVariables = new HashMap<String, FloatExpression>();
		this.doubleVariables = new HashMap<String, DoubleExpression>();
		this.refVariables = new HashMap<String, LiteralReference>();

		this.symbolicVariables = new HashMap<JvmVariable, String>(
				symbolicVariables);

	}

	public void updateSymbolicMapping(Z3ArrayVariable<?, ?> map_var,
			LiteralNonNullReference index_reference,
			BitVector32 new_symbolic_value) {
		String map_var_str = map_var.getName();
		if (!intMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var
					+ " is not defined as a int mapping!");
		}
		checkNonNull(new_symbolic_value);

		intMappings.get(map_var_str).put(index_reference, new_symbolic_value);
	}

	private static void checkNonNull(JvmExpression e) {
		if (e == null) {
			throw new IllegalArgumentException(
					"expression is expected to be non null!");
		}
	}

	public void updateSymbolicMapping(Z3ArrayVariable<?, ?> map_var,
			LiteralNonNullReference index_reference,
			BitVector64 new_symbolic_value) {
		String map_var_str = map_var.getName();
		if (!longMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var
					+ " is not defined as a long mapping!");
		}
		checkNonNull(new_symbolic_value);

		longMappings.get(map_var_str).put(index_reference, new_symbolic_value);
	}

	public void updateSymbolicMapping(Z3ArrayVariable<?, ?> map_var,
			LiteralNonNullReference index_reference,
			LiteralReference new_symbolic_value) {
		String map_var_str = map_var.getName();
		if (!refMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var
					+ " is not defined as a Object mapping!");
		}

		checkNonNull(new_symbolic_value);

		refMappings.get(map_var_str).put(index_reference, new_symbolic_value);
	}

	private boolean isBitVector32(Class<?> type) {
		return type.equals(Byte.TYPE) || type.equals(Short.TYPE)
				|| type.equals(Integer.TYPE) || type.equals(Character.TYPE)
				|| type.equals(Boolean.TYPE);
	}

	public void declareNewSymbolicMapping(Z3ArrayVariable<?, ?> fresh_map_var,
			JavaFieldVariable java_field_variable) {
		Class<?> fieldType = java_field_variable.getField().getType();

		checkNotContainsMapVar(fresh_map_var);
		String fresh_map_var_str = fresh_map_var.getName();

		// switch according to class
		if (fieldType.isPrimitive() && isBitVector32(fieldType)) {
			SymbolicExecMapping<BitVector32> mapping = new SymbolicExecMapping<BitVector32>(
					java_field_variable);
			intMappings.put(fresh_map_var_str, mapping);
		} else if (fieldType.isPrimitive() && fieldType.equals(Long.TYPE)) {
			SymbolicExecMapping<BitVector64> mapping = new SymbolicExecMapping<BitVector64>(
					java_field_variable);
			longMappings.put(fresh_map_var_str, mapping);

		} else if (fieldType.isPrimitive() && fieldType.equals(Float.TYPE)) {
			SymbolicExecMapping<FloatExpression> mapping = new SymbolicExecMapping<FloatExpression>(
					java_field_variable);
			floatMappings.put(fresh_map_var_str, mapping);

		} else if (fieldType.isPrimitive() && fieldType.equals(Double.TYPE)) {
			SymbolicExecMapping<DoubleExpression> mapping = new SymbolicExecMapping<DoubleExpression>(
					java_field_variable);
			doubleMappings.put(fresh_map_var_str, mapping);

		} else if (Object.class.isAssignableFrom(fieldType)) {
			SymbolicExecMapping<LiteralReference> mapping = new SymbolicExecMapping<LiteralReference>(
					java_field_variable);
			refMappings.put(fresh_map_var_str, mapping);

		} else {
			throw new IllegalArgumentException("Cannot handle fields of type "
					+ fieldType.getName() + " (yet)");
		}

	}

	private void checkNotContainsMapVar(Z3ArrayVariable<?, ?> fresh_map_var) {

		String fresh_map_var_str = fresh_map_var.getName();
		for (HashMap<?, ?> mapping : this.all_mappings) {
			if (mapping.containsKey(fresh_map_var_str))
				throw new IllegalArgumentException(fresh_map_var.getName()
						+ " was already defined in mapping!");
		}

	}

	public void declareNewSymbolicMapping(Z3ArrayVariable<?, ?> fresh_map_var,
			Z3ArrayVariable<?, ?> old_map_var) {

		String old_map_var_str = old_map_var.getName();
		checkNotContainsMapVar(fresh_map_var);

		String fresh_map_var_str = fresh_map_var.getName();

		if (intMappings.containsKey(old_map_var_str)) {
			// int
			SymbolicExecMapping<BitVector32> old_map = intMappings
					.get(old_map_var_str);
			SymbolicExecMapping<BitVector32> new_map = new SymbolicExecMapping<BitVector32>(
					old_map);

			intMappings.put(fresh_map_var_str, new_map);

		} else if (longMappings.containsKey(old_map_var_str)) {
			// long
			SymbolicExecMapping<BitVector64> old_map = longMappings
					.get(old_map_var_str);
			SymbolicExecMapping<BitVector64> new_map = new SymbolicExecMapping<BitVector64>(
					old_map);
			longMappings.put(fresh_map_var_str, new_map);

		} else if (refMappings.containsKey(old_map_var_str)) {
			// object

			SymbolicExecMapping<LiteralReference> old_map = refMappings
					.get(old_map_var_str);
			SymbolicExecMapping<LiteralReference> new_map = new SymbolicExecMapping<LiteralReference>(
					old_map);
			refMappings.put(fresh_map_var_str, new_map);

		} else if (floatMappings.containsKey(old_map_var_str)) {
			// float
			SymbolicExecMapping<FloatExpression> old_map = floatMappings
					.get(old_map_var_str);
			SymbolicExecMapping<FloatExpression> new_map = new SymbolicExecMapping<FloatExpression>(
					old_map);

			floatMappings.put(fresh_map_var_str, new_map);
		} else if (doubleMappings.containsKey(old_map_var_str)) {
			// double
			SymbolicExecMapping<DoubleExpression> old_map = doubleMappings
					.get(old_map_var_str);
			SymbolicExecMapping<DoubleExpression> new_map = new SymbolicExecMapping<DoubleExpression>(
					old_map);

			doubleMappings.put(fresh_map_var_str, new_map);
		} else {
			throw new RuntimeException("Implement this case");
		}

	}

	public void declareNewSymbolicVariable(JvmVariable fresh_var,
			BitVector32 symbolic_value) {

		String fresh_var_str = fresh_var.getName();

		if (intVariables.containsKey(fresh_var_str))
			throw new IllegalArgumentException(fresh_var.getName()
					+ " is already defined as an int variable!");

		checkNonNull(symbolic_value);

		intVariables.put(fresh_var_str, symbolic_value);
	}

	public void declareNewSymbolicVariable(JvmVariable fresh_var,
			BitVector64 symbolic_value) {

		String fresh_var_str = fresh_var.getName();
		if (longVariables.containsKey(fresh_var_str))
			throw new IllegalArgumentException(fresh_var.getName()
					+ " is already defined as an long variable!");

		checkNonNull(symbolic_value);

		longVariables.put(fresh_var_str, symbolic_value);
	}

	public void declareNewSymbolicVariable(JvmVariable fresh_var,
			LiteralReference symbolic_value) {

		String fresh_var_str = fresh_var.getName();
		if (refVariables.containsKey(fresh_var_str))
			throw new IllegalArgumentException(fresh_var.getName()
					+ " is already defined as an long variable!");

		checkNonNull(symbolic_value);

		refVariables.put(fresh_var_str, symbolic_value);
	}

	public void clear() {
		// mappings
		for (HashMap<?, ?> mapping : this.all_mappings) {
			mapping.clear();
		}

		// variables
		this.intVariables.clear();
		this.longVariables.clear();
		this.refVariables.clear();
		this.floatVariables.clear();
		this.doubleVariables.clear();

	}

	public boolean isSymbolicIntMapping(Z3ArrayVariable<?, ?> map_var) {
		String map_var_str = map_var.getName();
		return this.intMappings.containsKey(map_var_str);
	}

	public boolean isSymbolicLongMapping(Z3ArrayVariable<?, ?> map_var) {
		String map_var_str = map_var.getName();
		return this.longMappings.containsKey(map_var_str);
	}

	public boolean isSymbolicRefMapping(Z3ArrayVariable<?, ?> map_var) {
		String map_var_str = map_var.getName();
		return this.refMappings.containsKey(map_var_str);
	}

	public boolean isAlreadyDefined(BitVector32Variable v) {
		String v_str = v.getName();
		return this.intVariables.containsKey(v_str);
	}

	public BitVector32 getSymbolicIntValue(BitVector32Variable v) {
		String v_str = v.getName();
		if (!this.intVariables.containsKey(v_str)) {
			throw new IllegalArgumentException(v.getName()
					+ " was not found in symbolic state!");
		}
		return this.intVariables.get(v_str);
	}
	
	public FloatExpression getSymbolicFloatValue(FloatVariable v) {
		String v_str = v.getName();
		if (!this.floatVariables.containsKey(v_str)) {
			throw new IllegalArgumentException(v.getName()
					+ " was not found in symbolic state!");
		}
		return this.floatVariables.get(v_str);
	}

	public BitVector32 getSymbolicIntValue(Z3ArrayVariable<?, ?> map_var,
			LiteralNonNullReference literal_index) {

		String map_var_str = map_var.getName();
		if (!intMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var.getName()
					+ " is not defined as a symbolic int mapping!");
		}
		if (!intMappings.get(map_var_str).containsKey(literal_index)) {
			throw new IllegalArgumentException(literal_index
					+ " is not a valid index in mapping " + map_var.getName());

		}
		return intMappings.get(map_var_str).get(literal_index);
	}

	public BitVector64 getSymbolicLongValue(Z3ArrayVariable<?, ?> map_var,
			LiteralNonNullReference index_literal) {
		String map_var_str = map_var.getName();
		if (!longMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var.getName()
					+ " is not defined as a symbolic long mapping!");
		}
		if (!longMappings.get(map_var_str).containsKey(index_literal)) {
			throw new IllegalArgumentException(index_literal
					+ " is not a valid index in mapping " + map_var.getName());

		}
		return longMappings.get(map_var_str).get(index_literal);
	}

	public LiteralReference getSymbolicRefValue(Z3ArrayVariable<?, ?> map_var,
			LiteralNonNullReference index_literal) {
		String map_var_str = map_var.getName();
		if (!refMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var.getName()
					+ " is not defined as a symbolic refence mapping!");
		}
		if (!refMappings.get(map_var_str).containsKey(index_literal)) {
			throw new IllegalArgumentException(index_literal
					+ " is not a valid index in mapping " + map_var.getName());

		}
		return refMappings.get(map_var_str).get(index_literal);
	}

	public boolean isMarked(JvmVariable v) {
		return this.symbolicVariables.containsKey(v);
	}

	public LiteralReference getSymbolicRefValue(Reference v) {
		String v_str = v.toString();
		if (!this.refVariables.containsKey(v_str)) {
			throw new IllegalArgumentException(v.toString()
					+ " was not found in symbolic state!");
		}
		return this.refVariables.get(v_str);
	}

	public boolean isAlreadyDefined(BitVector64Variable v) {
		String v_str = v.getName();
		return this.longVariables.containsKey(v_str);
	}

	public String getSymbolicName(JvmVariable b) {
		if (!this.symbolicVariables.containsKey(b))
			throw new IllegalArgumentException(b.toString()
					+ " is not a symbolic variable!");

		return this.symbolicVariables.get(b);
	}

	public void updateSymbolicMapping(Z3ArrayVariable<?, ?> map_var,
			LiteralNonNullReference index_reference,
			FloatExpression new_symbolic_value) {

		String map_var_str = map_var.getName();
		if (!floatMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var
					+ " is not defined as a float mapping!");
		}
		checkNonNull(new_symbolic_value);

		floatMappings.get(map_var_str).put(index_reference, new_symbolic_value);
	}

	public void updateSymbolicMapping(Z3ArrayVariable<?, ?> map_var,
			LiteralNonNullReference index_reference,
			DoubleExpression new_symbolic_value) {

		String map_var_str = map_var.getName();
		if (!doubleMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var
					+ " is not defined as a double mapping!");
		}
		checkNonNull(new_symbolic_value);

		doubleMappings.get(map_var_str)
				.put(index_reference, new_symbolic_value);
	}

	public boolean isSymbolicFloatMapping(Z3ArrayVariable<?, ?> map_var) {
		String map_var_str = map_var.getName();
		return this.floatMappings.containsKey(map_var_str);
	}

	public boolean isSymbolicDoubleMapping(Z3ArrayVariable<?, ?> map_var) {
		String map_var_str = map_var.getName();
		return this.doubleMappings.containsKey(map_var_str);
	}

	public FloatExpression getSymbolicFloatValue(Z3ArrayVariable<?, ?> map_var,
			LiteralNonNullReference index_literal) {

		String map_var_str = map_var.getName();
		if (!floatMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var.getName()
					+ " is not defined as a symbolic float mapping!");
		}
		if (!floatMappings.get(map_var_str).containsKey(index_literal)) {
			throw new IllegalArgumentException(index_literal
					+ " is not a valid index in mapping " + map_var.getName());

		}
		return floatMappings.get(map_var_str).get(index_literal);
	}

	public DoubleExpression getSymbolicDoubleValue(
			Z3ArrayVariable<?, ?> map_var, LiteralNonNullReference index_literal) {

		String map_var_str = map_var.getName();
		if (!doubleMappings.containsKey(map_var_str)) {
			throw new IllegalArgumentException(map_var.getName()
					+ " is not defined as a symbolic double mapping!");
		}
		if (!doubleMappings.get(map_var_str).containsKey(index_literal)) {
			throw new IllegalArgumentException(index_literal
					+ " is not a valid index in mapping " + map_var.getName());

		}
		return doubleMappings.get(map_var_str).get(index_literal);
	}

	public void declareNewSymbolicVariable(JvmVariable fresh_var,
			FloatExpression symbolic_value) {

		String fresh_var_str = fresh_var.getName();

		if (floatVariables.containsKey(fresh_var_str))
			throw new IllegalArgumentException(fresh_var.getName()
					+ " is already defined as an float variable!");

		checkNonNull(symbolic_value);

		floatVariables.put(fresh_var_str, symbolic_value);
	}

	public void declareNewSymbolicVariable(JvmVariable fresh_var,
			DoubleExpression symbolic_value) {

		String fresh_var_str = fresh_var.getName();

		if (doubleVariables.containsKey(fresh_var_str))
			throw new IllegalArgumentException(fresh_var.getName()
					+ " is already defined as an double variable!");

		checkNonNull(symbolic_value);

		doubleVariables.put(fresh_var_str, symbolic_value);
	}

	public BitVector64 getSymbolicLongValue(BitVector64Variable v) {
		String v_str = v.getName();
		if (!this.longVariables.containsKey(v_str)) {
			throw new IllegalArgumentException(v.getName()
					+ " was not found in symbolic state!");
		}
		return this.longVariables.get(v_str);
	}

	public boolean isAlreadyDefined(FloatVariable v) {
		String v_str = v.getName();
		return this.floatVariables.containsKey(v_str);
	}
	
	
	public boolean isAlreadyDefined(DoubleVariable v) {
		String v_str = v.getName();
		return this.doubleVariables.containsKey(v_str);
	}

	public DoubleExpression getSymbolicDoubleValue(DoubleVariable v) {
		String v_str = v.getName();
		if (!this.doubleVariables.containsKey(v_str)) {
			throw new IllegalArgumentException(v.getName()
					+ " was not found in symbolic state!");
		}
		return this.doubleVariables.get(v_str);
	}
}