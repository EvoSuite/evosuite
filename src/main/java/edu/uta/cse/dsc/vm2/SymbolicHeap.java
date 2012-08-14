package edu.uta.cse.dsc.vm2;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.StringExpression;

public final class SymbolicHeap {

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
	public NonNullReference newReference(String className) {
		return new NonNullReference(className, newInstanceCount++);
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
		Map<NonNullReference, Expression<?>> symb_field;
		FieldKey k = new FieldKey(owner, name);
		symb_field = symb_fields.get(k);
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
	public IntegerExpression getField(String owner, String name,
			Object conc_receiver, NonNullReference symb_receiver,
			long conc_value) {

		Map<NonNullReference, Expression<?>> symb_field = getOrCreateSymbolicField(
				owner, name);
		IntegerExpression symb_value = (IntegerExpression) symb_field
				.get(symb_receiver);
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
		// TODO Auto-generated method stub

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
	public RealExpression getField(String className, String fieldName,
			Object conc_receiver, NonNullReference symb_receiver,
			double conc_value) {

		Map<NonNullReference, Expression<?>> symb_field = getOrCreateSymbolicField(
				className, fieldName);
		RealExpression symb_value = (RealExpression) symb_field
				.get(symb_receiver);
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
	 * @param receiverConcrete
	 * @param symb_receiver
	 * @param conc_value
	 * @return
	 */
	public StringExpression getField(String className, String fieldName,
			Object receiverConcrete, NonNullReference symb_receiver,
			String conc_value) {

		Map<NonNullReference, Expression<?>> symb_field = getOrCreateSymbolicField(
				className, fieldName);
		StringExpression symb_value = (StringExpression) symb_field
				.get(symb_receiver);
		if (symb_value == null
				|| !((String) symb_value.getConcreteValue()).equals(conc_value)) {
			symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
			symb_field.remove(symb_receiver);
		}

		monitor_gc();
		return symb_value;
	}

	/**
	 * 
	 * @param className
	 * @param fieldName
	 * @param receiverConcrete
	 * @param symb_receiver
	 * @param conc_value
	 * @return
	 */
	public Reference getField(String className, String fieldName,
			Object receiverConcrete, NonNullReference symb_receiver,
			Object conc_value) {

		Reference symb_ref = getReference(conc_value);
		monitor_gc();
		return symb_ref;
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

	public IntegerExpression getStaticField(String owner, String name,
			long conc_value) {

		FieldKey k = new FieldKey(owner, name);
		IntegerExpression symb_value = (IntegerExpression) symb_static_fields
				.get(k);
		if (symb_value == null
				|| ((Long) symb_value.getConcreteValue()).longValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewIntegerConstant(conc_value);
			symb_static_fields.remove(k);
		}

		monitor_gc();
		return symb_value;

	}

	public RealExpression getStaticField(String owner, String name,
			double conc_value) {

		FieldKey k = new FieldKey(owner, name);
		RealExpression symb_value = (RealExpression) symb_static_fields.get(k);
		if (symb_value == null
				|| ((Double) symb_value.getConcreteValue()).doubleValue() != conc_value) {
			symb_value = ExpressionFactory.buildNewRealConstant(conc_value);
			symb_static_fields.remove(k);
		}

		monitor_gc();
		return symb_value;

	}

	public StringExpression getStaticField(String owner, String name,
			String conc_value) {

		FieldKey k = new FieldKey(owner, name);
		StringExpression symb_value = (StringExpression) symb_static_fields
				.get(k);
		if (symb_value == null
				|| !((String) symb_value.getConcreteValue()).equals(conc_value)) {
			symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
			symb_static_fields.remove(k);
		}

		monitor_gc();
		return symb_value;
	}

	public Reference getStaticField(String owner, String fieldName,
			Object conc_value) {

		Reference symb_ref = getReference(conc_value);
		monitor_gc();
		return symb_ref;

	}

	public Reference getReference(Object conc_value) {
		if (conc_value == null) {

			return NullReference.getInstance();
		} else if (conc_value instanceof String) {

			String string = (String) conc_value;
			return new StringReference(
					ExpressionFactory.buildNewStringConstant(string));
		} else {

			int identityHashCode = System.identityHashCode(conc_value);
			NonNullReference symb_ref = nonNullRefs.get(identityHashCode);
			if (symb_ref == null
					|| symb_ref.getWeakConcreteObject() != conc_value) {
				// unknown object or out of synch object
				symb_ref = new NonNullReference(
						conc_value.getClass().getName(), newInstanceCount++);
				symb_ref.initializeReference(conc_value);
				nonNullRefs.put(identityHashCode, symb_ref);
			}
			return symb_ref;
		}
	}

}
