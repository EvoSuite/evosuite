package org.evosuite.symbolic.vm;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
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

	protected static Logger logger = LoggerFactory
			.getLogger(SymbolicHeap.class);

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
				return this.owner.equals(that.owner)
						&& this.name.equals(that.name);
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
	 * Number of modCount operations before GC() is executed.
	 */
	private static final int SYMBOLIC_GC_THRESHOLD = 9000000;

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
	 * @param className
	 * 
	 * @return
	 */
	public NonNullReference newReference(Type objectType) {

		if (objectType.getClassName() == null)
			throw new IllegalArgumentException();

		return new NonNullReference(objectType, newInstanceCount++);
	}

	/**
	 * Stores a mapping between identityHashCodes and NonNullReferences. Every
	 * time the NonNullReference for a given Object (non String) is needed, this
	 * mapping is used.
	 * 
	 */
	private final Map<Integer, NonNullReference> nonNullRefs = new THashMap<Integer, NonNullReference>();

	/**
	 * Stores a mapping between NonNullReferences and their symbolic values. The
	 * Expression<?> contains at least one symbolic variable.
	 */
	private final Map<FieldKey, Map<NonNullReference, Expression<?>>> symb_fields = new THashMap<FieldKey, Map<NonNullReference, Expression<?>>>();

	/**
	 * Mapping between for symbolic values stored in static fields. The
	 * Expression<?> contains at least one symbolic variable.
	 */
	private final Map<FieldKey, Expression<?>> symb_static_fields = new THashMap<FieldKey, Expression<?>>();

	/**
	 * This counter is used to count the number of operations before GC() is
	 * executed
	 */
	private int modCount;

	/**
	 * Updates an instance field. The symbolic expression is stored iif it is
	 * not a constant expression (i.e. it has at least one variable).
	 * 
	 * @param owner
	 * @param name
	 * @param conc_receiver
	 *            The concrete Object receiver instance
	 * @param symb_receiver
	 *            A symbolic NonNullReference instance
	 * @param symb_value
	 *            The Expression to be stored. Null value means the previous
	 *            symbolic expression has to be erased.
	 */
	public void putField(String owner, String name, Object conc_receiver,
			NonNullReference symb_receiver, Expression<?> symb_value) {

		Map<NonNullReference, Expression<?>> symb_field = getOrCreateSymbolicField(
				owner, name);
		if (symb_value == null || !symb_value.containsSymbolicVariable()) {
			symb_field.remove(symb_receiver);
		} else {
			symb_field.put(symb_receiver, symb_value);
		}

		monitor_gc();
	}

	private void monitor_gc() {
		modCount++;
		if (modCount > SYMBOLIC_GC_THRESHOLD) {
			symbolic_gc();
			modCount = 0;
		}
	}

	private Map<NonNullReference, Expression<?>> getOrCreateSymbolicField(
			String owner, String name) {
		FieldKey k = new FieldKey(owner, name);
		Map<NonNullReference, Expression<?>> symb_field = symb_fields.get(k);
		if (symb_field == null) {
			symb_field = new THashMap<NonNullReference, Expression<?>>();
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
	public IntegerValue getField(String owner, String name,
			Object conc_receiver, NonNullReference symb_receiver,
			long conc_value) {

		Map<NonNullReference, Expression<?>> symb_field = getOrCreateSymbolicField(
				owner, name);
		IntegerValue symb_value = (IntegerValue) symb_field.get(symb_receiver);
		if (symb_value == null
				|| ((Long) symb_value.getConcreteValue()).longValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewIntegerConstant(conc_value);
			symb_field.remove(symb_receiver);
		}

		monitor_gc();
		return symb_value;
	}

	/**
	 * Symbolic Garbage collector
	 */
	private void symbolic_gc() {

		logger.debug("DSE: starting symbolic heap garbage collection");

		Set<NonNullReference> collected_refs = new THashSet<NonNullReference>();
		// field reys
		for (Entry<FieldKey, Map<NonNullReference, Expression<?>>> symb_field_entry : symb_fields
				.entrySet()) {

			Map<NonNullReference, Expression<?>> symb_field = symb_field_entry
					.getValue();
			Set<NonNullReference> keySet = new THashSet<NonNullReference>(
					symb_field.keySet());
			for (NonNullReference non_null_ref : keySet) {
				if (non_null_ref.isCollectable()) {
					symb_field.remove(non_null_ref);
					collected_refs.add(non_null_ref);
				}
			}
		}

		// array refs
		Set<NonNullReference> keySet = new THashSet<NonNullReference>(
				this.symb_arrays.keySet());
		for (NonNullReference array_ref : keySet) {
			if (array_ref.isCollectable()) {
				symb_arrays.remove(array_ref);
				collected_refs.add(array_ref);
			}
		}

		// stored null refs
		Set<Entry<Integer, NonNullReference>> entry_set = new THashSet<Entry<Integer, NonNullReference>>(
				this.nonNullRefs.entrySet());
		for (Entry<Integer, NonNullReference> entry : entry_set) {
			if (entry.getValue().isCollectable()) {
				nonNullRefs.remove(entry.getKey());
				collected_refs.add(entry.getValue());

			}
		}

		logger.debug("Nr of collected non-null references = "
				+ collected_refs.size());
		logger.debug("DSE: symbolic heap garbage collection ended");

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
	public RealValue getField(String className, String fieldName,
			Object conc_receiver, NonNullReference symb_receiver,
			double conc_value) {

		Map<NonNullReference, Expression<?>> symb_field = getOrCreateSymbolicField(
				className, fieldName);
		RealValue symb_value = (RealValue) symb_field.get(symb_receiver);
		if (symb_value == null
				|| ((Double) symb_value.getConcreteValue()).doubleValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewRealConstant(conc_value);
			symb_field.remove(symb_receiver);
		}

		monitor_gc();
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
	public StringValue getField(String className, String fieldName,
			Object conc_receiver, NonNullReference symb_receiver,
			String conc_value) {

		Map<NonNullReference, Expression<?>> symb_field = getOrCreateSymbolicField(
				className, fieldName);
		StringValue symb_value = (StringValue) symb_field.get(symb_receiver);
		if (symb_value == null
				|| !((String) symb_value.getConcreteValue()).equals(conc_value)) {
			symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
			symb_field.remove(symb_receiver);
		}

		monitor_gc();
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
	public Expression<?> getField(String className, String fieldName,
			Object conc_receiver, NonNullReference symb_receiver) {

		Map<NonNullReference, Expression<?>> symb_field = getOrCreateSymbolicField(
				className, fieldName);
		Expression<?> symb_value = symb_field.get(symb_receiver);
		monitor_gc();
		return symb_value;
	}

	public void putStaticField(String owner, String name,
			Expression<?> symb_value) {

		FieldKey k = new FieldKey(owner, name);
		if (symb_value == null || !symb_value.containsSymbolicVariable()) {
			symb_static_fields.remove(k);
		} else {
			symb_static_fields.put(k, symb_value);
		}

		monitor_gc();
	}

	public IntegerValue getStaticField(String owner, String name,
			long conc_value) {

		FieldKey k = new FieldKey(owner, name);
		IntegerValue symb_value = (IntegerValue) symb_static_fields.get(k);
		if (symb_value == null
				|| ((Long) symb_value.getConcreteValue()).longValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewIntegerConstant(conc_value);
			symb_static_fields.remove(k);
		}

		monitor_gc();
		return symb_value;

	}

	public RealValue getStaticField(String owner, String name, double conc_value) {

		FieldKey k = new FieldKey(owner, name);
		RealValue symb_value = (RealValue) symb_static_fields.get(k);
		if (symb_value == null
				|| ((Double) symb_value.getConcreteValue()).doubleValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewRealConstant(conc_value);
			symb_static_fields.remove(k);
		}

		monitor_gc();
		return symb_value;

	}

	public StringValue getStaticField(String owner, String name,
			String conc_value) {

		FieldKey k = new FieldKey(owner, name);
		StringValue symb_value = (StringValue) symb_static_fields.get(k);
		if (symb_value == null
				|| !((String) symb_value.getConcreteValue()).equals(conc_value)) {
			symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
			symb_static_fields.remove(k);
		}

		monitor_gc();
		return symb_value;
	}

	public Reference getReference(Object conc_ref) {
		if (conc_ref == null) {
			return NullReference.getInstance();

		} else {

			int identityHashCode = System.identityHashCode(conc_ref);
			NonNullReference symb_ref = nonNullRefs.get(identityHashCode);
			if (symb_ref == null
					|| symb_ref.getWeakConcreteObject() != conc_ref) {
				// unknown object or out of synch object
				symb_ref = new NonNullReference(Type.getType(conc_ref
						.getClass()), newInstanceCount++);
				symb_ref.initializeReference(conc_ref);
				nonNullRefs.put(identityHashCode, symb_ref);
			}
			return symb_ref;
		}
	}

	public void array_store(Object conc_array, NonNullReference symb_array,
			int conc_index, Expression<?> symb_value) {

		Map<Integer, Expression<?>> symb_array_contents = getOrCreateSymbolicArray(symb_array);

		if (symb_value == null || !symb_value.containsSymbolicVariable()) {
			symb_array_contents.remove(conc_index);
		} else {
			symb_array_contents.put(conc_index, symb_value);
		}

		monitor_gc();
	}

	private final Map<NonNullReference, Map<Integer, Expression<?>>> symb_arrays = new THashMap<NonNullReference, Map<Integer, Expression<?>>>();

	public static final String $STRING_BUILDER_CONTENTS = "$stringBuilder_contents";

	public static String $STRING_BUFFER_CONTENTS = "$stringBuffer_contents";

	public static String $BIG_INTEGER_CONTENTS = "$bigInteger_contents";

	public static String $STRING_TOKENIZER_VALUE = "$stringTokenizerValue";

	public static String $STRING_READER_VALUE = "$stringReaderValue";

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

	private Map<Integer, Expression<?>> getOrCreateSymbolicArray(
			NonNullReference symb_array_ref) {

		Map<Integer, Expression<?>> symb_array_contents = symb_arrays
				.get(symb_array_ref);
		if (symb_array_contents == null) {
			symb_array_contents = new THashMap<Integer, Expression<?>>();
			symb_arrays.put(symb_array_ref, symb_array_contents);
		}

		return symb_array_contents;
	}

	public StringValue array_load(NonNullReference symb_array, int conc_index,
			String conc_value) {

		Map<Integer, Expression<?>> symb_array_contents = getOrCreateSymbolicArray(symb_array);
		StringValue symb_value = (StringValue) symb_array_contents
				.get(conc_index);
		if (symb_value == null
				|| !((String) symb_value.getConcreteValue()).equals(conc_value)) {
			symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
			symb_array_contents.remove(conc_index);
		}

		monitor_gc();
		return symb_value;
	}

	public IntegerValue array_load(NonNullReference symb_array, int conc_index,
			long conc_value) {

		Map<Integer, Expression<?>> symb_array_contents = getOrCreateSymbolicArray(symb_array);
		IntegerValue symb_value = (IntegerValue) symb_array_contents
				.get(conc_index);
		if (symb_value == null
				|| ((Long) symb_value.getConcreteValue()).longValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewIntegerConstant(conc_value);
			symb_array_contents.remove(conc_index);
		}

		monitor_gc();
		return symb_value;
	}

	public RealValue array_load(NonNullReference symb_array, int conc_index,
			double conc_value) {

		Map<Integer, Expression<?>> symb_array_contents = getOrCreateSymbolicArray(symb_array);
		RealValue symb_value = (RealValue) symb_array_contents.get(conc_index);
		if (symb_value == null
				|| ((Double) symb_value.getConcreteValue()).doubleValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewRealConstant(conc_value);
			symb_array_contents.remove(conc_index);
		}

		monitor_gc();
		return symb_value;
	}

	public void initializeReference(Object conc_ref, Reference symb_ref) {
		if (conc_ref != null) {
			NonNullReference symb_non_null_ref = (NonNullReference) symb_ref;
			if (!symb_non_null_ref.isInitialized()) {
				symb_non_null_ref.initializeReference(conc_ref);
				int identityHashCode = System.identityHashCode(conc_ref);
				nonNullRefs.put(identityHashCode, symb_non_null_ref);
			}
		}
	}

	/**
	 * Constructor
	 */
	public SymbolicHeap() {
	}
}
