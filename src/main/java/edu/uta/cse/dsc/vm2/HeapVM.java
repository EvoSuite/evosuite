package edu.uta.cse.dsc.vm2;

import static edu.uta.cse.dsc.util.Assertions.notNull;
import static edu.uta.cse.dsc.util.Log.logln;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealConstant;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.StringExpression;
import org.objectweb.asm.Type;

import edu.uta.cse.dsc.AbstractVM;
import edu.uta.cse.dsc.instrument.DscInstrumentingClassLoader;

/**
 * Static area (static fields) and heap (instance fields)
 * 
 * FIXME: reset static state before each execution.
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class HeapVM extends AbstractVM {

	private final SymbolicEnvironment env;

	private final DscInstrumentingClassLoader classLoader;

	public HeapVM(SymbolicEnvironment env, PathConstraint pc,
			DscInstrumentingClassLoader classLoader) {
		this.env = env;
		this.classLoader = classLoader;
	}

	/* Fields */

	/**
	 * Resolve (static or instance) field
	 * 
	 * <p>
	 * JVM Specification, Section 5.4.3.2: Field Resolution:
	 * http://java.sun.com/
	 * docs/books/jvms/second_edition/html/ConstantPool.doc.html#71685
	 * 
	 * TODO: Resolve field once and for all, then cache it.
	 */
	public static Field resolveField(Class<?> claz, String name) {
		notNull(claz, name);

		Field[] fields = claz.getDeclaredFields();
		for (Field field : fields)
			if (field.getName().equals(name)) // owner declares the "name" field
				return field;

		Class<?>[] suprs = claz.getInterfaces();
		for (Class<?> supr : suprs) {
			Field res = resolveField(supr, name);
			if (res != null) // super interface declares it
				return res;
		}

		Class<?> supr = claz.getSuperclass();
		if (supr != null) // super class declares it
			return resolveField(supr, name);

		return null;
	}

	/**
	 * GetStatic mypackage/MyClass fieldName FieldType
	 * 
	 * @param owner
	 *            name of a class or interface.
	 * @param fieldName
	 *            name of the field to be read. The owner class or interface
	 *            itself my have declared this field. If owner is a class, then
	 *            this field may also be declared by a - super-class of the
	 *            owner class, or by a - interface implemented by (a super-class
	 *            of) the owner class.
	 * 
	 *            http://java.sun.com/docs/books/jvms/second_edition/html/
	 *            Instructions2.doc5.html#getstatic
	 */
	@Override
	public void GETSTATIC(String owner, String fieldName, String desc) {

		/**
		 * Prepare Class
		 */
		Class<?> claz = env.ensurePrepared(owner); // type name given in
													// bytecode

		Field concrete_field = resolveField(claz, fieldName); // field may be
																// declared by
		// interface
		Class<?> declaringClass = concrete_field.getDeclaringClass();

		if (declaringClass.isInterface()) {
			/*
			 * Unlikely that we ever get here. Java compiler probably computes
			 * value of this (final) field and replaces any
			 * "getstatic MyInterface myField" by "sipush fieldValue" or such.
			 * Even if we get here, there should be no need to prepare this
			 * field, as there has to be an explicit initialization, hence a
			 * <clinit>().
			 */
			logln("Do we have to prepare the static fields of an interface?");
			env.ensurePrepared(declaringClass);
		}

		boolean isAccessible = concrete_field.isAccessible();
		if (!isAccessible) {
			concrete_field.setAccessible(true);
		}

		/**
		 * First, Get symbolic expression. If no symbolic expression exists, use
		 * concrete value. Then, update operand stack according to type
		 */
		Type type = Type.getType(desc);

		try {

			if (type.equals(Type.INT_TYPE)) {

				int value = concrete_field.getInt(null);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getStaticField(owner, fieldName, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.CHAR_TYPE)) {

				char value = concrete_field.getChar(null);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getStaticField(owner, fieldName, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.SHORT_TYPE)) {

				short value = concrete_field.getShort(null);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getStaticField(owner, fieldName, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.BOOLEAN_TYPE)) {

				boolean booleanValue = concrete_field.getBoolean(null);
				int value = booleanValue ? 1 : 0;
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getStaticField(owner, fieldName, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.BYTE_TYPE)) {

				byte value = concrete_field.getByte(null);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getStaticField(owner, fieldName, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.LONG_TYPE)) {

				long value = concrete_field.getLong(null);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getStaticField(owner, fieldName, value);
				env.topFrame().operandStack.pushBv64(intExpr);

			} else if (type.equals(Type.FLOAT_TYPE)) {

				float value = concrete_field.getFloat(null);
				RealExpression fp32 = (RealExpression) env.heap.getStaticField(
						owner, fieldName, (double) value);
				env.topFrame().operandStack.pushFp32(fp32);

			} else if (type.equals(Type.DOUBLE_TYPE)) {

				double value = concrete_field.getDouble(null);
				RealExpression fp64 = (RealExpression) env.heap.getStaticField(
						owner, fieldName, value);
				env.topFrame().operandStack.pushFp64(fp64);

			} else if (type.equals(Type.getType(String.class))) {

				String value = (String) concrete_field.get(null);
				StringExpression strExpr = env.heap.getStaticField(owner,
						fieldName, value);
				if (strExpr == null) {
					env.topFrame().operandStack.pushRef(NullReference
							.getInstance());
				} else {
					env.topFrame().operandStack.pushStringRef(strExpr);
				}

			} else {

				Object value = concrete_field.get(null);
				Reference ref = env.heap
						.getStaticField(owner, fieldName, value);
				env.topFrame().operandStack.pushRef(ref);

			}

			if (!isAccessible) {
				concrete_field.setAccessible(false);
			}

		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc11.html#putstatic
	 */
	@Override
	public void PUTSTATIC(String owner, String name, String desc) {

		/**
		 * Prepare classes
		 */
		Class<?> claz = env.ensurePrepared(owner); // type name given in
													// bytecode
		Field field = resolveField(claz, name);
		/* See GetStatic */
		Class<?> declaringClass = field.getDeclaringClass();
		if (declaringClass.isInterface()) {
			logln("Do we have to prepare the static fields of an interface?");
			env.ensurePrepared(declaringClass);
		}

		/**
		 * Update symbolic state (if needed)
		 */
		Operand value_operand = env.topFrame().operandStack.popOperand();
		Expression<?> symb_value = null;
		if (value_operand instanceof IntegerOperand) {
			IntegerOperand intOp = (IntegerOperand) value_operand;
			symb_value = intOp.getIntegerExpression();
		} else if (value_operand instanceof RealOperand) {
			RealOperand realOp = (RealOperand) value_operand;
			symb_value = realOp.getRealExpression();
		} else if (value_operand instanceof Reference) {
			Reference ref = (Reference) value_operand;
			if (ref instanceof NullReference) {
				symb_value = null;
			} else if (ref instanceof StringReference) {
				StringReference strRef = (StringReference) ref;
				symb_value = strRef.getStringExpression();
			} else {
				// NonNullReference are not stored in the symbolic heap fields
				return;
			}

		}
		env.heap.putStaticField(owner, name, symb_value);

	}

	/**
	 * Allocate space on the heap and push a reference ref to it onto the stack.
	 * 
	 * For each instance field declared by class className, we add a tuple (ref,
	 * default value) to the field's map.
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc10.html#new
	 */
	@Override
	public void NEW(String className) {
		/**
		 * Since this callback is invoked before the actual object creation, we
		 * do nothing.
		 * 
		 * We do not need to discard any elements from the operand stack since
		 * it is given empty.
		 * 
		 * PRE-Stack: empty
		 * 
		 * POST-Stack: objectref (delayed)
		 */
		NonNullReference newObject = this.env.heap.newReference(className);
		env.topFrame().operandStack.pushRef(newObject);
	}

	/**
	 * Retrieve the value of an instance field
	 * 
	 * <p>
	 * Before actually retrieving the value, the JVM will check if the instance
	 * is null. If the receiver instance is null, the JVM will throw a null
	 * pointer exception.
	 * 
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc5.html#getfield
	 */
	@Override
	public void GETFIELD(Object receiverConcrete, String className,
			String fieldName, String desc) {
		// consume symbolic operand
		Reference receiver_ref = env.topFrame().operandStack.popRef();

		Field field = resolveField(classLoader.getClassForName(className),
				fieldName);
		env.ensurePrepared(field.getDeclaringClass());

		boolean isAccessible = field.isAccessible();
		if (!isAccessible) {
			field.setAccessible(true);
		}

		/*
		 * Schedule reference field type to be asserted -- before null check, as
		 * null check will create a new node in path constraint
		 */
		if (receiverConcrete == null) {
			/**
			 * Execution will lead to a NullPointerException
			 */
			return;
		}

		NonNullReference symb_receiver = (NonNullReference) receiver_ref;

		Type type = Type.getType(desc);

		try {

			if (type.equals(Type.INT_TYPE)) {

				int value = field.getInt(receiverConcrete);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getField(className, fieldName, receiverConcrete,
								symb_receiver, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.LONG_TYPE)) {

				long value = field.getLong(receiverConcrete);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getField(className, fieldName, receiverConcrete,
								symb_receiver, value);
				env.topFrame().operandStack.pushBv64(intExpr);

			} else if (type.equals(Type.FLOAT_TYPE)) {

				float value = field.getFloat(receiverConcrete);
				RealExpression fp32 = (RealExpression) env.heap.getField(
						className, fieldName, receiverConcrete, symb_receiver,
						(double) value);
				env.topFrame().operandStack.pushFp32(fp32);

			} else if (type.equals(Type.DOUBLE_TYPE)) {

				double value = field.getDouble(receiverConcrete);
				RealExpression fp64 = (RealExpression) env.heap.getField(
						className, fieldName, receiverConcrete, symb_receiver,
						value);
				env.topFrame().operandStack.pushFp64(fp64);

			} else if (type.equals(Type.CHAR_TYPE)) {

				char value = field.getChar(receiverConcrete);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getField(className, fieldName, receiverConcrete,
								symb_receiver, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.SHORT_TYPE)) {

				short value = field.getShort(receiverConcrete);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getField(className, fieldName, receiverConcrete,
								symb_receiver, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.BOOLEAN_TYPE)) {

				boolean booleanValue = field.getBoolean(receiverConcrete);
				int value = booleanValue ? 1 : 0;
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getField(className, fieldName, receiverConcrete,
								symb_receiver, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.BYTE_TYPE)) {

				byte value = field.getByte(receiverConcrete);
				IntegerExpression intExpr = (IntegerExpression) env.heap
						.getField(className, fieldName, receiverConcrete,
								symb_receiver, (long) value);
				env.topFrame().operandStack.pushBv32(intExpr);

			} else if (type.equals(Type.getType(String.class))) {

				String value = (String) field.get(receiverConcrete);
				StringExpression strExpr = env.heap.getField(className,
						fieldName, receiverConcrete, symb_receiver, value);
				if (strExpr == null) {
					env.topFrame().operandStack.pushRef(NullReference
							.getInstance());
				} else {
					env.topFrame().operandStack.pushStringRef(strExpr);
				}

			} else {

				Object value = field.get(receiverConcrete);
				Reference ref = env.heap.getField(className, fieldName,
						receiverConcrete, symb_receiver, value);
				env.topFrame().operandStack.pushRef(ref);

			}

			if (!isAccessible) {
				field.setAccessible(false);
			}

		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Store a value in an instance field.
	 * 
	 * Before actually retrieving the value, the JVM will check if the instance
	 * is null. If the receiver instance is null, the JVM will throw a null
	 * pointer exception.
	 */
	@Override
	public void PUTFIELD(Object concrete_receiver, String className,
			String fieldName, String desc) {

		/**
		 * Prepare classes
		 */
		Field field = resolveField(classLoader.getClassForName(className),
				fieldName);
		env.ensurePrepared(field.getDeclaringClass());

		if (concrete_receiver == null) {
			return;
		}

		/**
		 * Update symbolic heap (if needed)
		 */
		Operand value_operand = env.topFrame().operandStack.popOperand();
		Reference receiver = env.topFrame().operandStack.popRef();

		NonNullReference symb_receiver = (NonNullReference) receiver;
		Expression<?> symb_value = null;
		if (value_operand instanceof IntegerOperand) {
			IntegerOperand intOp = (IntegerOperand) value_operand;
			symb_value = intOp.getIntegerExpression();
		} else if (value_operand instanceof RealOperand) {
			RealOperand realOp = (RealOperand) value_operand;
			symb_value = realOp.getRealExpression();
		} else if (value_operand instanceof Reference) {
			Reference ref = (Reference) value_operand;
			if (ref instanceof NullReference) {
				symb_value = null;
			} else if (ref instanceof StringReference) {
				StringReference strRef = (StringReference) ref;
				symb_value = strRef.getStringExpression();
			} else {
				// NonNullReference are not stored in the symbolic heap fields
				return;
			}

		}
		env.heap.putField(className, fieldName, concrete_receiver,
				symb_receiver, symb_value);
	}

	/* Arrays */

	/**
	 * Create a (one-dimensional) array of primitive componenet type, e.g., new
	 * int[3]
	 * 
	 * Allocate space on the heap and push a reference ref to it onto the stack.
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc10.html#newarray
	 */
	@Override
	public void NEWARRAY(int lengthConcrete, Class<?> componentType) {
		/**
		 * Since this callback is invoked before the actual array creation, we
		 * can only add negative index constraints.
		 * 
		 * PRE: int (length)
		 * 
		 * POST: arrayref (delayed)
		 */
		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();

		/* negative index */
		if (lengthConcrete < 0)
			return;

		String className = "[" + componentType.getName();
		NonNullReference newObjectArray = this.env.heap.newReference(className);
		env.topFrame().operandStack.pushRef(newObjectArray);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc
	 * .html#anewarray
	 */
	@Override
	public void ANEWARRAY(int lengthConcrete, String componentTypeName) {
		/**
		 * Since this callback is invoked before the actual array creation, we
		 * can only add negative index constraints.
		 * 
		 * PRE: int (length)
		 * 
		 * POST: arrayref (delayed)
		 */

		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();

		/* negative index */
		if (lengthConcrete < 0)
			return;

		String className = "[" + componentTypeName;
		NonNullReference newObjectArray = this.env.heap.newReference(className);
		env.topFrame().operandStack.pushRef(newObjectArray);
	}

	/**
	 * MULTIANEWARRAY
	 * 
	 * <pre>
	 * boolean[] b1 = new boolean[1]; // NEWARRAY T_BOOLEAN
	 * Boolean[] B1 = new Boolean[1]; // ANEWARRAY java/lang/Boolean
	 * boolean[][] b2 = new boolean[1][2]; // MULTIANEWARRAY [[Z 2
	 * Boolean[][] B2 = new Boolean[1][2]; // MULTIANEWARRAY [[Ljava/lang/Boolean; 2
	 * </pre>
	 */
	@Override
	public void MULTIANEWARRAY(String arrayTypeDesc, int nrDimensions) {
		/**
		 * Since this callback is invoked before the actual array creation, we
		 * can only add negative index constraints.
		 * 
		 * PRE: int (dimensions) | ... | int (size2) | int (size1)
		 * 
		 * POST: arrayref (delayed)
		 */

		// discard operand stack
		env.topFrame().operandStack.popBv32();
		for (int i = 1; i < nrDimensions; i++) {
			env.topFrame().operandStack.popBv32();
		}

		// push delayed object
		NonNullReference newMultiArray = this.env.heap
				.newReference(arrayTypeDesc); // @FIXME
		env.topFrame().operandStack.pushRef(newMultiArray);
	}

	@Override
	public void ARRAYLENGTH(Object referenceConcrete) {
		// discard symbolic array reference
		env.topFrame().operandStack.popRef();

		if (referenceConcrete != null) {
			int lengthConcrete = Array.getLength(referenceConcrete);

			// replace symbolic array reference with length
			IntegerConstant literalLength = ExpressionFactory
					.buildNewIntegerConstant(lengthConcrete);
			env.topFrame().operandStack.pushBv32(literalLength);
		}
	}

	/**
	 * Load an int value from an array and push it on the stack
	 * 
	 * ..., arrayref, index ==> ..., value
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#iaload
	 */
	@Override
	public void IALOAD(Object referenceConcrete, int indexConcrete) {
		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

		/* null-check */
		if (referenceConcrete == null)
			return;

		/* negative index */
		if (indexConcrete < 0)
			return;

		/* out of bound index */
		if (indexConcrete >= Array.getLength(referenceConcrete))
			return;

		int bv32 = Array.getInt(referenceConcrete, indexConcrete);
		IntegerConstant c = ExpressionFactory.buildNewIntegerConstant(bv32);
		env.topFrame().operandStack.pushBv32(c);

	}

	@Override
	public void LALOAD(Object referenceConcrete, int indexConcrete) {
		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

		/* null-check */
		if (referenceConcrete == null)
			return;

		/* negative index */
		if (indexConcrete < 0)
			return;

		/* out of bound index */
		if (indexConcrete >= Array.getLength(referenceConcrete))
			return;

		long bv64 = Array.getLong(referenceConcrete, indexConcrete);
		IntegerConstant c = ExpressionFactory.buildNewIntegerConstant(bv64);
		env.topFrame().operandStack.pushBv64(c);

	}

	@Override
	public void FALOAD(Object referenceConcrete, int indexConcrete) {
		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

		/* null-check */
		if (referenceConcrete == null)
			return;

		/* negative index */
		if (indexConcrete < 0)
			return;

		/* out of bound index */
		if (indexConcrete >= Array.getLength(referenceConcrete))
			return;

		float fp32 = Array.getFloat(referenceConcrete, indexConcrete);
		RealConstant c = ExpressionFactory.buildNewRealConstant(fp32);
		env.topFrame().operandStack.pushFp32(c);

	}

	/**
	 * Load double from array
	 * 
	 * ..., arrayref, index ==> ..., value
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3.html#daload
	 */
	@Override
	public void DALOAD(Object referenceConcrete, int indexConcrete) {
		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

		/* null-check */
		if (referenceConcrete == null)
			return;

		/* negative index */
		if (indexConcrete < 0)
			return;

		/* out of bound index */
		if (indexConcrete >= Array.getLength(referenceConcrete))
			return;

		double fp64 = Array.getDouble(referenceConcrete, indexConcrete);
		RealConstant c = ExpressionFactory.buildNewRealConstant(fp64);
		env.topFrame().operandStack.pushFp64(c);

	}

	@Override
	public void AALOAD(Object referenceConcrete, int indexConcrete) {
		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

		/* null-check */
		if (referenceConcrete == null)
			return;

		/* negative index */
		if (indexConcrete < 0)
			return;

		/* out of bound index */
		if (indexConcrete >= Array.getLength(referenceConcrete))
			return;

		Object value = Array.get(referenceConcrete, indexConcrete);
		Reference ref = env.heap.getReference(value);
		env.topFrame().operandStack.pushRef(ref);
	}

	/**
	 * retrieve byte/boolean from array
	 */
	@Override
	public void BALOAD(Object referenceConcrete, int indexConcrete) {
		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

		/* null-check */
		if (referenceConcrete == null)
			return;

		/* negative index */
		if (indexConcrete < 0)
			return;

		/* out of bound index */
		if (indexConcrete >= Array.getLength(referenceConcrete))
			return;

		Object object = Array.get(referenceConcrete, indexConcrete);
		int intValue;
		if (object instanceof Boolean) {
			boolean booleanValue = ((Boolean) object).booleanValue();
			intValue = booleanValue ? 1 : 0;
		} else {
			assert object instanceof Byte;
			intValue = ((Byte) object).shortValue();
		}
		IntegerConstant c = ExpressionFactory.buildNewIntegerConstant(intValue);
		env.topFrame().operandStack.pushBv32(c);

	}

	@Override
	public void CALOAD(Object referenceConcrete, int indexConcrete) {
		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

		/* null-check */
		if (referenceConcrete == null)
			return;

		/* negative index */
		if (indexConcrete < 0)
			return;

		/* out of bound index */
		if (indexConcrete >= Array.getLength(referenceConcrete))
			return;

		char bv32 = Array.getChar(referenceConcrete, indexConcrete);
		IntegerConstant c = ExpressionFactory.buildNewIntegerConstant(bv32);
		env.topFrame().operandStack.pushBv32(c);

	}

	@Override
	public void SALOAD(Object referenceConcrete, int indexConcrete) {
		// discard symbolic arguments
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

		/* null-check */
		if (referenceConcrete == null)
			return;

		/* negative index */
		if (indexConcrete < 0)
			return;

		/* out of bound index */
		if (indexConcrete >= Array.getLength(referenceConcrete))
			return;

		short bv32 = Array.getShort(referenceConcrete, indexConcrete);
		IntegerConstant c = ExpressionFactory.buildNewIntegerConstant(bv32);
		env.topFrame().operandStack.pushBv32(c);

	}

	/**
	 * Store the top operand stack value into an array
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#iastore
	 */
	@Override
	public void IASTORE(Object referenceConcrete, int indexConcrete) {
		/**
		 * Discard all information flowing through arrays. We will catch this
		 * later from the JVM execution.
		 */
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();
	}

	@Override
	public void LASTORE(Object referenceConcrete, int indexConcrete) {
		/**
		 * Discard all information flowing through arrays. We will catch this
		 * later from the JVM execution.
		 */
		env.topFrame().operandStack.popBv64();
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

	}

	@Override
	public void FASTORE(Object referenceConcrete, int indexConcrete) {
		/**
		 * Discard all information flowing through arrays. We will catch this
		 * later from the JVM execution.
		 */
		env.topFrame().operandStack.popFp32();
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();

	}

	@Override
	public void DASTORE(Object referenceConcrete, int indexConcrete) {
		/**
		 * Discard all information flowing through arrays. We will catch this
		 * later from the JVM execution.
		 */
		env.topFrame().operandStack.popFp64();
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc
	 * .html#aastore
	 */
	@Override
	public void AASTORE(Object referenceConcrete, int indexConcrete) {
		/**
		 * Discard all information flowing through arrays. We will catch this
		 * later from the JVM execution.
		 */
		env.topFrame().operandStack.popRef();
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();
	}

	@Override
	public void BASTORE(Object referenceConcrete, int indexConcrete) {
		/**
		 * Discard all information flowing through arrays. We will catch this
		 * later from the JVM execution.
		 */
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();
	}

	@Override
	public void CASTORE(Object referenceConcrete, int indexConcrete) {
		/**
		 * Discard all information flowing through arrays. We will catch this
		 * later from the JVM execution.
		 */
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();
	}

	@Override
	public void SASTORE(Object referenceConcrete, int indexConcrete) {
		/**
		 * Discard all information flowing through arrays. We will catch this
		 * later from the JVM execution.
		 */
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.popRef();
	}

	/**
	 * Explicit type cast:
	 * 
	 * <pre>
	 * RefTypeX x = (RefTypeX) ref;
	 * </pre>
	 * 
	 * null is treated as (can be cast to) any reference type. This is
	 * consistent with the null type being a subtype of every reference type.
	 * Note the different treatment in {@link #INSTANCEOF}.
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc2.html#checkcast
	 */
	@Override
	public void CHECKCAST(Object referenceConcrete, String typeName) {
		/**
		 * Ignore check cast constraints
		 */
	}

	/**
	 * Dynamic type check:
	 * 
	 * <pre>
	 * (variable instanceof TypeName)
	 * </pre>
	 * 
	 * null is not treated as (is not an instance of) any reference type. This
	 * requires non-standard treatment of null. Note the different treatment in
	 * {@link #CHECKCAST}.
	 * 
	 * <p>
	 * If the jvm has not loaded the class/interface named TypeName before, then
	 * we load it. TODO: Is this a problem?
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#instanceof
	 */
	@Override
	public void INSTANCEOF(Object referenceConcrete, String typeName) {
		env.topFrame().operandStack.popRef(); // discard symbolic reference
		Class<?> myClazz = classLoader.getClassForName(typeName);
		boolean instanceOf = myClazz.isInstance(referenceConcrete);

		IntegerConstant ret;
		if (instanceOf) {
			ret = ExpressionFactory.ICONST_1;
		} else {
			ret = ExpressionFactory.ICONST_0;
		}

		env.topFrame().operandStack.pushBv32(ret);
	}
}
