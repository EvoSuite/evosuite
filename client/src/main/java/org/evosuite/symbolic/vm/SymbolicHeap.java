/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.vm;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.str.StringValue;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author galeotti
 * 
 */
public final class SymbolicHeap {

	protected static final Logger logger = LoggerFactory.getLogger(SymbolicHeap.class);

	private static final class FieldKey {
		private String owner;
		private String name;

		public FieldKey(String owner, String name) {
			this.owner = owner;
			this.name = name;
		}

		@Override
		public int hashCode() {
			return this.owner.hashCode() + this.name.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o.getClass().equals(FieldKey.class)) {
				FieldKey that = (FieldKey) o;
				return this.owner.equals(that.owner) && this.name.equals(that.name);
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return this.owner + "/" + this.name;
		}
	}

	/**
	 * Counter for instances
	 */
	private int newInstanceCount = 0;

	/**
	 * This constructor is for references created in instrumented code (NEW,
	 * ANEW, NEWARRAY, etc).
	 * 
	 * It is the only way of creating uninitialized non-null references.
	 * 
	 * @param exceptionClassName
	 * 
	 * @return
	 */
	public ReferenceConstant buildNewReferenceConstant(Type objectType) {

		if (objectType.getClassName() == null)
			throw new IllegalArgumentException();

		final int newInstanceId = newInstanceCount++;
		return new ReferenceConstant(objectType, newInstanceId);
	}

	/**
	 * Stores a mapping between identityHashCodes and NonNullReferences. Every
	 * time the NonNullReference for a given Object (non String) is needed, this
	 * mapping is used.
	 * 
	 */
	private final Map<Integer, ReferenceExpression> nonNullRefs = new HashMap<Integer, ReferenceExpression>();

	/**
	 * Stores a mapping between NonNullReferences and their symbolic values. The
	 * Expression<?> contains at least one symbolic variable.
	 */
	private final Map<FieldKey, Map<ReferenceExpression, Expression<?>>> symb_fields = new HashMap<FieldKey, Map<ReferenceExpression, Expression<?>>>();

	/**
	 * Mapping between for symbolic values stored in static fields. The
	 * Expression<?> contains at least one symbolic variable.
	 */
	private final Map<FieldKey, Expression<?>> symb_static_fields = new HashMap<FieldKey, Expression<?>>();

	/**
	 * Updates an instance field. The symbolic expression is stored iif it is
	 * not a constant expression (i.e. it has at least one variable).
	 * 
	 * @param className
	 * @param fieldName
	 * @param conc_receiver
	 *            The concrete Object receiver instance
	 * @param symb_receiver
	 *            A symbolic NonNullReference instance
	 * @param symb_value
	 *            The Expression to be stored. Null value means the previous
	 *            symbolic expression has to be erased.
	 */
	public void putField(String className, String fieldName, Object conc_receiver, ReferenceExpression symb_receiver,
			Expression<?> symb_value) {

		Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(className, fieldName);
		if (symb_value == null || !symb_value.containsSymbolicVariable()) {
			symb_field.remove(symb_receiver);
		} else {
			symb_field.put(symb_receiver, symb_value);
		}
	}

	private Map<ReferenceExpression, Expression<?>> getOrCreateSymbolicField(String owner, String name) {
		FieldKey k = new FieldKey(owner, name);
		Map<ReferenceExpression, Expression<?>> symb_field = symb_fields.get(k);
		if (symb_field == null) {
			symb_field = new HashMap<ReferenceExpression, Expression<?>>();
			symb_fields.put(k, symb_field);
		}

		return symb_field;
	}

	/**
	 * Returns a stored symbolic expression for an int field or created one
	 * 
	 * @param owner
	 * @param name
	 * @param conc_receiver
	 * @param symb_receiver
	 * @param conc_value
	 * @return
	 */
	public IntegerValue getField(String owner, String name, Object conc_receiver, ReferenceExpression symb_receiver,
			long conc_value) {

		Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(owner, name);
		IntegerValue symb_value = (IntegerValue) symb_field.get(symb_receiver);
		if (symb_value == null || ((Long) symb_value.getConcreteValue()).longValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewIntegerConstant(conc_value);
			symb_field.remove(symb_receiver);
		}

		return symb_value;
	}

	/**
	 * 
	 * @param className
	 * @param fieldName
	 * @param conc_receiver
	 * @param symb_receiver
	 * @param conc_value
	 * @return
	 */
	public RealValue getField(String className, String fieldName, Object conc_receiver,
			ReferenceExpression symb_receiver, double conc_value) {

		Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(className, fieldName);
		RealValue symb_value = (RealValue) symb_field.get(symb_receiver);
		if (symb_value == null || ((Double) symb_value.getConcreteValue()).doubleValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewRealConstant(conc_value);
			symb_field.remove(symb_receiver);
		}

		return symb_value;
	}

	/**
	 * 
	 * @param className
	 * @param fieldName
	 * @param conc_receiver
	 * @param symb_receiver
	 * @param conc_value
	 * @return
	 */
	public StringValue getField(String className, String fieldName, Object conc_receiver,
			ReferenceExpression symb_receiver, String conc_value) {

		Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(className, fieldName);
		StringValue symb_value = (StringValue) symb_field.get(symb_receiver);
		if (symb_value == null || !((String) symb_value.getConcreteValue()).equals(conc_value)) {
			symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
			symb_field.remove(symb_receiver);
		}

		return symb_value;
	}

	/**
	 * No default concrete value means the return value could be false!
	 * 
	 * @param className
	 * @param fieldName
	 * @param conc_receiver
	 * @param symb_receiver
	 * @return
	 */
	public Expression<?> getField(String className, String fieldName, Object conc_receiver,
			ReferenceExpression symb_receiver) {

		Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(className, fieldName);
		Expression<?> symb_value = symb_field.get(symb_receiver);
		return symb_value;
	}

	public void putStaticField(String owner, String name, Expression<?> symb_value) {

		FieldKey k = new FieldKey(owner, name);
		if (symb_value == null || !symb_value.containsSymbolicVariable()) {
			symb_static_fields.remove(k);
		} else {
			symb_static_fields.put(k, symb_value);
		}

	}

	public IntegerValue getStaticField(String owner, String name, long conc_value) {

		FieldKey k = new FieldKey(owner, name);
		IntegerValue symb_value = (IntegerValue) symb_static_fields.get(k);
		if (symb_value == null || ((Long) symb_value.getConcreteValue()).longValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewIntegerConstant(conc_value);
			symb_static_fields.remove(k);
		}

		return symb_value;

	}

	public RealValue getStaticField(String owner, String name, double conc_value) {

		FieldKey k = new FieldKey(owner, name);
		RealValue symb_value = (RealValue) symb_static_fields.get(k);
		if (symb_value == null || ((Double) symb_value.getConcreteValue()).doubleValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewRealConstant(conc_value);
			symb_static_fields.remove(k);
		}

		return symb_value;

	}

	public StringValue getStaticField(String owner, String name, String conc_value) {

		FieldKey k = new FieldKey(owner, name);
		StringValue symb_value = (StringValue) symb_static_fields.get(k);
		if (symb_value == null || !((String) symb_value.getConcreteValue()).equals(conc_value)) {
			symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
			symb_static_fields.remove(k);
		}

		return symb_value;
	}

	/**
	 * Returns a <code>ReferenceConstant</code> if the concrete reference is
	 * null. Otherwise, it looks in the list of non-null symbolic references for
	 * a symbolic reference with the concrete value. If it is found, that
	 * symbolic reference is returned, otherwise a new reference constant is
	 * created (and added ot the list of non-null symbolic references)
	 * 
	 * @param conc_ref
	 * @return
	 */
	public ReferenceExpression getReference(Object conc_ref) {
		if (conc_ref == null) {
			// null reference
			ReferenceConstant nullConstant = ExpressionFactory.buildNewNullExpression();
			return nullConstant;
		} else {
			int identityHashCode = System.identityHashCode(conc_ref);
			if (nonNullRefs.containsKey(identityHashCode)) {
				// already known object
				ReferenceExpression symb_ref = nonNullRefs.get(identityHashCode);
				return symb_ref;
			} else {
				// unknown object
				final Type type = Type.getType(conc_ref.getClass());
				ReferenceConstant ref_constant = new ReferenceConstant(type, newInstanceCount++);
				ref_constant.initializeReference(conc_ref);
				nonNullRefs.put(identityHashCode, ref_constant);
				return ref_constant;
			}
		}
	}

	/**
	 * Builds a new reference variable using a var_name and a concrete obhect
	 * The concrete object can be null.
	 * 
	 * @param conc_object
	 * @param var_name
	 * @return
	 */
	public ReferenceVariable buildNewReferenceVariable(Object conc_object, String var_name) {
		final Type referenceType;
		if (conc_object == null) {
			referenceType = Type.getType(Object.class);
		} else {
			referenceType = Type.getType(conc_object.getClass());
		}
		final int newInstanceId = newInstanceCount++;
		final ReferenceVariable r = new ReferenceVariable(referenceType, newInstanceId, var_name, conc_object);
		return r;
	}

	public void array_store(Object conc_array, ReferenceExpression symb_array, int conc_index,
			Expression<?> symb_value) {

		Map<Integer, Expression<?>> symb_array_contents = getOrCreateSymbolicArray(symb_array);

		if (symb_value == null || !symb_value.containsSymbolicVariable()) {
			symb_array_contents.remove(conc_index);
		} else {
			symb_array_contents.put(conc_index, symb_value);
		}

	}

	private final Map<ReferenceExpression, Map<Integer, Expression<?>>> symb_arrays = new HashMap<ReferenceExpression, Map<Integer, Expression<?>>>();

	public static final String $STRING_BUILDER_CONTENTS = "$stringBuilder_contents";

	public static final String $STRING_BUFFER_CONTENTS = "$stringBuffer_contents";

	public static final String $BIG_INTEGER_CONTENTS = "$bigInteger_contents";

	public static final String $STRING_TOKENIZER_VALUE = "$stringTokenizerValue";

	public static final String $STRING_READER_VALUE = "$stringReaderValue";

	public static final String $MATCHER_INPUT = "$matcherInput";

	public static final String $BOOLEAN_VALUE = "$booleanValue";

	public static final String $BYTE_VALUE = "$byteValue";

	public static final String $CHAR_VALUE = "$charValue";

	public static final String $SHORT_VALUE = "$shortValue";

	public static final String $LONG_VALUE = "$longValue";

	public static final String $INT_VALUE = "$intValue";

	public static final String $FLOAT_VALUE = "$floatValue";

	public static final String $DOUBLE_VALUE = "$doubleValue";

	public static final String $STRING_VALUE = "$stringValue";

	private Map<Integer, Expression<?>> getOrCreateSymbolicArray(ReferenceExpression symb_array_ref) {

		Map<Integer, Expression<?>> symb_array_contents = symb_arrays.get(symb_array_ref);
		if (symb_array_contents == null) {
			symb_array_contents = new HashMap<Integer, Expression<?>>();
			symb_arrays.put(symb_array_ref, symb_array_contents);
		}

		return symb_array_contents;
	}

	public StringValue array_load(ReferenceExpression symb_array, int conc_index, String conc_value) {

		Map<Integer, Expression<?>> symb_array_contents = getOrCreateSymbolicArray(symb_array);
		StringValue symb_value = (StringValue) symb_array_contents.get(conc_index);
		if (symb_value == null || !((String) symb_value.getConcreteValue()).equals(conc_value)) {
			symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
			symb_array_contents.remove(conc_index);
		}

		return symb_value;
	}

	public IntegerValue array_load(ReferenceExpression symb_array, int conc_index, long conc_value) {

		Map<Integer, Expression<?>> symb_array_contents = getOrCreateSymbolicArray(symb_array);
		IntegerValue symb_value = (IntegerValue) symb_array_contents.get(conc_index);
		if (symb_value == null || ((Long) symb_value.getConcreteValue()).longValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewIntegerConstant(conc_value);
			symb_array_contents.remove(conc_index);
		}

		return symb_value;
	}

	public RealValue array_load(ReferenceExpression symb_array, int conc_index, double conc_value) {

		Map<Integer, Expression<?>> symb_array_contents = getOrCreateSymbolicArray(symb_array);
		RealValue symb_value = (RealValue) symb_array_contents.get(conc_index);
		if (symb_value == null || ((Double) symb_value.getConcreteValue()).doubleValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewRealConstant(conc_value);
			symb_array_contents.remove(conc_index);
		}

		return symb_value;
	}

	/**
	 * Initializes a reference using a concrete object
	 * 
	 * @param conc_ref
	 * @param symb_ref
	 */
	public void initializeReference(Object conc_ref, ReferenceExpression symb_ref) {
		if (conc_ref != null) {
			if (!symb_ref.isInitialized()) {
				symb_ref.initializeReference(conc_ref);
				int identityHashCode = System.identityHashCode(conc_ref);
				nonNullRefs.put(identityHashCode, symb_ref);
			}
		}
	}

	/**
	 * Constructor
	 */
	public SymbolicHeap() {
	}
}
