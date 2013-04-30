package org.evosuite.symbolic;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.bv.RealToIntegerCast;
import org.evosuite.symbolic.expr.fp.IntegerToRealCast;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.NullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.wrappers.Types;
import org.evosuite.testcase.ArrayIndex;
import org.evosuite.testcase.ArrayReference;
import org.evosuite.testcase.ArrayStatement;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.BooleanPrimitiveStatement;
import org.evosuite.testcase.BytePrimitiveStatement;
import org.evosuite.testcase.CharPrimitiveStatement;
import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DoublePrimitiveStatement;
import org.evosuite.testcase.EnumPrimitiveStatement;
import org.evosuite.testcase.EvosuiteError;
import org.evosuite.testcase.ExecutionObserver;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.FieldStatement;
import org.evosuite.testcase.FloatPrimitiveStatement;
import org.evosuite.testcase.IntPrimitiveStatement;
import org.evosuite.testcase.LongPrimitiveStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.ShortPrimitiveStatement;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.StringPrimitiveStatement;
import org.evosuite.testcase.VariableReference;
import org.objectweb.asm.Type;

import edu.uta.cse.dsc.VM;

public class SymbolicObserver extends ExecutionObserver {

	private static final String INIT = "<init>";
	private final SymbolicEnvironment env;

	public SymbolicObserver(SymbolicEnvironment env) {
		this.env = env;
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	private void before(ConstructorStatement stmt, Scope scope) {
		String className = stmt.getConstructor().getDeclaringClass().getName();
		VM.NEW(className);
		VM.DUP();
		String desc = Type.getConstructorDescriptor(stmt.getConstructor().getConstructor());
		pushParameterList(stmt.parameters, scope, desc);
		String owner = className.replace(".", "/");
		/* indicates if the following code is instrumented or not */
		VM.INVOKESPECIAL(owner, INIT, desc);
		boolean needThis = true;
		call_vm_caller_stack_params(needThis, stmt.parameters, scope, desc);
	}

	private void after(ConstructorStatement stmt, Scope scope) {
		String className = stmt.getConstructor().getDeclaringClass().getName();
		String desc = Type.getConstructorDescriptor(stmt.getConstructor().getConstructor());
		/* pops operands if previous code was not instrumented */
		// constructor return type is always VOID
		String onwer = className.replace(".", "/");
		VM.CALL_RESULT(onwer, INIT, desc);
		VariableReference varRef = stmt.getReturnValue();

		NonNullReference nonNullRef = (NonNullReference) env.topFrame().operandStack.popRef();
		String varName = varRef.getName();
		symb_references.put(varName, nonNullRef);

	}

	@Override
	public void beforeStatement(StatementInterface s, Scope scope) {
		if (VM.vm.isStopped()) {
			return;
		}
		VM.setIgnoreCallBack(false);

		try {
			if (env.isEmpty()) {
				env.prepareStack(null);
			}

			if (s instanceof NullStatement) {
				before((NullStatement) s, scope);
			} else if (s instanceof AssignmentStatement) {
				before((AssignmentStatement) s, scope);

			} else if (s instanceof EnumPrimitiveStatement<?>) {
				before((EnumPrimitiveStatement<?>) s, scope);

			} else if (s instanceof ArrayStatement) {
				before((ArrayStatement) s, scope);

			} else if (s instanceof FieldStatement) {
				before((FieldStatement) s, scope);

			} else if (s instanceof ConstructorStatement) {
				before((ConstructorStatement) s, scope);

			}

			/* primitive statements */
			else if (s instanceof BooleanPrimitiveStatement) {
				before((BooleanPrimitiveStatement) s, scope);

			} else if (s instanceof MethodStatement) {
				before((MethodStatement) s, scope);

			} else if (s instanceof BytePrimitiveStatement) {
				before((BytePrimitiveStatement) s, scope);

			} else if (s instanceof CharPrimitiveStatement) {
				before((CharPrimitiveStatement) s, scope);

			} else if (s instanceof DoublePrimitiveStatement) {
				before((DoublePrimitiveStatement) s, scope);

			} else if (s instanceof FloatPrimitiveStatement) {
				before((FloatPrimitiveStatement) s, scope);

			} else if (s instanceof IntPrimitiveStatement) {
				before((IntPrimitiveStatement) s, scope);

			} else if (s instanceof LongPrimitiveStatement) {
				before((LongPrimitiveStatement) s, scope);

			} else if (s instanceof ShortPrimitiveStatement) {
				before((ShortPrimitiveStatement) s, scope);

			} else if (s instanceof StringPrimitiveStatement) {
				before((StringPrimitiveStatement) s, scope);
			} else {
				throw new UnsupportedOperationException();
			}
		} catch (Throwable t) {
			throw new EvosuiteError(t);
		}

	}

	private static final int COMPONENT_TYPE_BOOLEAN = 4;
	private static final int COMPONENT_TYPE_CHAR = 5;
	private static final int COMPONENT_TYPE_FLOAT = 6;
	private static final int COMPONENT_TYPE_DOUBLE = 7;
	private static final int COMPONENT_TYPE_BYTE = 8;
	private static final int COMPONENT_TYPE_SHORT = 9;
	private static final int COMPONENT_TYPE_INT = 10;
	private static final int COMPONENT_TYPE_LONG = 11;

	private void after(ArrayStatement s, Scope scope) {
		try {
			ArrayReference arrayRef = (ArrayReference) s.getReturnValue();
			Object conc_array;
			conc_array = arrayRef.getObject(scope);

			if (arrayRef.getArrayDimensions() == 1) {
				int length = arrayRef.getArrayLength();
				IntegerConstant lengthExpr = ExpressionFactory.buildNewIntegerConstant(length);
				Class<?> component_class = arrayRef.getComponentClass();
				env.topFrame().operandStack.pushBv32(lengthExpr);
				if (component_class.equals(int.class)) {
					VM.NEWARRAY(length, COMPONENT_TYPE_INT);
				} else if (component_class.equals(char.class)) {
					VM.NEWARRAY(length, COMPONENT_TYPE_CHAR);
				} else if (component_class.equals(short.class)) {
					VM.NEWARRAY(length, COMPONENT_TYPE_SHORT);
				} else if (component_class.equals(byte.class)) {
					VM.NEWARRAY(length, COMPONENT_TYPE_BYTE);
				} else if (component_class.equals(float.class)) {
					VM.NEWARRAY(length, COMPONENT_TYPE_FLOAT);
				} else if (component_class.equals(long.class)) {
					VM.NEWARRAY(length, COMPONENT_TYPE_LONG);
				} else if (component_class.equals(boolean.class)) {
					VM.NEWARRAY(length, COMPONENT_TYPE_BOOLEAN);
				} else if (component_class.equals(double.class)) {
					VM.NEWARRAY(length, COMPONENT_TYPE_DOUBLE);
				} else {
					// push arguments
					String componentTypeName = component_class.getName().replace(".", "/");
					VM.ANEWARRAY(length, componentTypeName);
				}
			} else {
				// push dimensions
				List<Integer> dimensions = arrayRef.getLengths();
				for (int i = 0; i < arrayRef.getArrayDimensions(); i++) {
					int length = dimensions.get(i);
					IntegerConstant lengthExpr = ExpressionFactory.buildNewIntegerConstant(length);
					env.topFrame().operandStack.pushBv32(lengthExpr);
				}
				String arrayTypeDesc = Type.getDescriptor(conc_array.getClass());
				VM.MULTIANEWARRAY(arrayTypeDesc, arrayRef.getArrayDimensions());

			}
			NonNullReference symb_array = (NonNullReference) env.topFrame().operandStack.popRef();
			env.heap.initializeReference(conc_array, symb_array);

			String varRef_name = arrayRef.getName();
			symb_references.put(varRef_name, symb_array);

		} catch (CodeUnderTestException e) {
			throw new RuntimeException(e);
		}

	}

	private void before(EnumPrimitiveStatement<?> s, Scope scope) {
		/* do nothing */
	}

	private void before(NullStatement s, Scope scope) {
		/* do nothing */
	}

	private void before(FieldStatement s, Scope scope) {
		/* do nothing */
	}

	private static class ReferenceExpressionPair {
		private final Reference ref;
		private final Expression<?> expr;

		public ReferenceExpressionPair(Reference ref, Expression<?> expr) {
			this.ref = ref;
			this.expr = expr;
		}

		public Reference getReference() {
			return ref;
		}

		public Expression<?> getExpression() {
			return expr;
		}

	}

	private void after(AssignmentStatement s, Scope scope) {
		VariableReference lhs = s.getReturnValue();
		VariableReference rhs = s.getValue();

		ReferenceExpressionPair readResult = read(rhs, scope);

		if (lhs instanceof FieldReference) {
			writeField((FieldReference) lhs, readResult, scope);
		} else if (lhs instanceof ArrayIndex) {
			writeArray((ArrayIndex) lhs, readResult, scope);
		} else {
			writeVariable(lhs, readResult);
		}
	}

	private ReferenceExpressionPair read(VariableReference rhs, Scope scope) {
		if (rhs instanceof FieldReference) {
			return readField((FieldReference) rhs, scope);
		} else if (rhs instanceof ArrayIndex) {
			return readArray((ArrayIndex) rhs, scope);
		} else {
			return readVariable(rhs, scope);
		}
	}

	/**
	 * Reads a VariableReference from the stored symbolic references and
	 * symbolic expressions.
	 * 
	 * @throws IllegalArgumentException
	 *             if no value was found
	 * 
	 * @param rhs
	 * @param scope
	 * @return
	 */
	private ReferenceExpressionPair readVariable(VariableReference rhs, Scope scope) {
		String rhs_name = rhs.getName();
		Reference symb_ref = symb_references.get(rhs_name);
		Expression<?> symb_expr = symb_expressions.get(rhs_name);
		return new ReferenceExpressionPair(symb_ref, symb_expr);

	}

	private ReferenceExpressionPair readArray(ArrayIndex rhs, Scope scope) {
		ArrayReference arrayReference = rhs.getArray();
		NonNullReference symb_array = (NonNullReference) symb_references.get(arrayReference.getName());
		int conc_index = rhs.getArrayIndex();
		Class<?> componentClass = arrayReference.getComponentClass();

		try {
			Object conc_array = arrayReference.getObject(scope);

			if (componentClass.equals(int.class)) {
				int conc_value = Array.getInt(conc_array, conc_index);
				IntegerValue expr = env.heap.array_load(symb_array, conc_index,
				                                        conc_value);
				NonNullReference newIntegerRef = newIntegerReference(conc_value, expr);
				return new ReferenceExpressionPair(newIntegerRef, expr);
			} else if (componentClass.equals(char.class)) {
				char conc_value = Array.getChar(conc_array, conc_index);
				IntegerValue expr = env.heap.array_load(symb_array, conc_index,
				                                        conc_value);
				NonNullReference newCharacterRef = newCharacterReference(conc_value, expr);
				return new ReferenceExpressionPair(newCharacterRef, expr);
			} else if (componentClass.equals(boolean.class)) {
				boolean conc_value = Array.getBoolean(conc_array, conc_index);
				IntegerValue expr = env.heap.array_load(symb_array, conc_index,
				                                        conc_value ? 1 : 0);
				NonNullReference newBooleanRef = newBooleanReference(conc_value, expr);
				return new ReferenceExpressionPair(newBooleanRef, expr);
			} else if (componentClass.equals(byte.class)) {
				byte conc_value = Array.getByte(conc_array, conc_index);
				IntegerValue expr = env.heap.array_load(symb_array, conc_index,
				                                        conc_value);
				NonNullReference newByteRef = newByteReference(conc_value, expr);
				return new ReferenceExpressionPair(newByteRef, expr);
			} else if (componentClass.equals(short.class)) {
				short conc_value = Array.getShort(conc_array, conc_index);
				IntegerValue expr = env.heap.array_load(symb_array, conc_index,
				                                        conc_value);
				NonNullReference newShortRef = newShortReference(conc_value, expr);
				return new ReferenceExpressionPair(newShortRef, expr);
			} else if (componentClass.equals(long.class)) {
				long conc_value = Array.getLong(conc_array, conc_index);
				IntegerValue expr = env.heap.array_load(symb_array, conc_index,
				                                        conc_value);
				NonNullReference newLongRef = newLongReference(conc_value, expr);
				return new ReferenceExpressionPair(newLongRef, expr);
			} else if (componentClass.equals(float.class)) {
				float conc_value = Array.getFloat(conc_array, conc_index);
				RealValue expr = env.heap.array_load(symb_array, conc_index, conc_value);
				NonNullReference newFloatRef = newFloatReference(conc_value, expr);
				return new ReferenceExpressionPair(newFloatRef, expr);
			} else if (componentClass.equals(double.class)) {
				double conc_value = Array.getDouble(conc_array, conc_index);
				RealValue expr = env.heap.array_load(symb_array, conc_index, conc_value);
				NonNullReference newDoubleRef = newDoubleReference(conc_value, expr);
				return new ReferenceExpressionPair(newDoubleRef, expr);
			} else {
				Object conc_value = Array.get(conc_array, conc_index);
				if (conc_value instanceof String) {
					StringValue expr = env.heap.array_load(symb_array, conc_index,
					                                       (String) conc_value);
					NonNullReference newStringRef = newStringReference((String) conc_value,
					                                                   expr);
					return new ReferenceExpressionPair(newStringRef, expr);
				} else {
					Reference ref = env.heap.getReference(conc_value);

					if (conc_value != null && isWrapper(conc_value)) {
						NonNullReference nonNullRef = (NonNullReference) ref;
						Expression<?> expr = findOrCreate(conc_value, nonNullRef);
						return new ReferenceExpressionPair(ref, expr);
					} else {
						return new ReferenceExpressionPair(ref, null);
					}
				}
			}
		} catch (CodeUnderTestException e) {
			throw new RuntimeException(e);
		}
	}

	private ReferenceExpressionPair readField(FieldReference rhs, Scope scope) {

		if (rhs.getSource() != null) {
			/* instance field */
			return readInstanceField(rhs.getSource(), rhs.getField().getField(), scope);
		} else {
			/* static field */
			return readStaticField(rhs.getField().getField());
		}

	}

	private ReferenceExpressionPair readStaticField(Field field) {

		String owner = field.getDeclaringClass().getName().replace(".", "/");
		String name = field.getName();

		Class<?> fieldClazz = field.getType();

		try {

			if (fieldClazz.equals(int.class)) {
				int conc_value = field.getInt(null);
				IntegerValue expr = env.heap.getStaticField(owner, name, conc_value);
				NonNullReference newIntegerRef = newIntegerReference(conc_value, expr);
				return new ReferenceExpressionPair(newIntegerRef, expr);

			} else if (fieldClazz.equals(char.class)) {
				char conc_value = field.getChar(null);
				IntegerValue expr = env.heap.getStaticField(owner, name, conc_value);
				NonNullReference newCharacterRef = newCharacterReference(conc_value, expr);
				return new ReferenceExpressionPair(newCharacterRef, expr);

			} else if (fieldClazz.equals(long.class)) {
				long conc_value = field.getLong(null);
				IntegerValue expr = env.heap.getStaticField(owner, name, conc_value);
				NonNullReference newLongRef = newLongReference(conc_value, expr);
				return new ReferenceExpressionPair(newLongRef, expr);

			} else if (fieldClazz.equals(short.class)) {
				short conc_value = field.getShort(null);
				IntegerValue expr = env.heap.getStaticField(owner, name, conc_value);
				NonNullReference newShortRef = newShortReference(conc_value, expr);
				return new ReferenceExpressionPair(newShortRef, expr);

			} else if (fieldClazz.equals(byte.class)) {
				byte conc_value = field.getByte(null);
				IntegerValue expr = env.heap.getStaticField(owner, name, conc_value);
				NonNullReference newByteRef = newByteReference(conc_value, expr);
				return new ReferenceExpressionPair(newByteRef, expr);

			} else if (fieldClazz.equals(boolean.class)) {
				boolean conc_value = field.getBoolean(null);
				IntegerValue expr = env.heap.getStaticField(owner, name, conc_value ? 1
				        : 0);
				NonNullReference newBooleanRef = newBooleanReference(conc_value, expr);
				return new ReferenceExpressionPair(newBooleanRef, expr);

			} else if (fieldClazz.equals(float.class)) {
				float conc_value = field.getFloat(null);
				RealValue expr = env.heap.getStaticField(owner, name, conc_value);
				NonNullReference newFloatRef = newFloatReference(conc_value, expr);
				return new ReferenceExpressionPair(newFloatRef, expr);

			} else if (fieldClazz.equals(double.class)) {
				double conc_value = field.getDouble(null);
				RealValue expr = env.heap.getStaticField(owner, name, conc_value);
				NonNullReference newDoubleRef = newDoubleReference(conc_value, expr);
				return new ReferenceExpressionPair(newDoubleRef, expr);

			} else {
				Object conc_value = field.get(null);
				if (conc_value instanceof String) {
					String string = (String) conc_value;
					StringValue expr = env.heap.getStaticField(owner, name, string);
					NonNullReference newStringRef = newStringReference(string, expr);
					return new ReferenceExpressionPair(newStringRef, expr);
				} else {
					Reference ref = env.heap.getReference(conc_value);
					if (conc_value != null && isWrapper(conc_value)) {
						NonNullReference nonNullRef = (NonNullReference) ref;
						Expression<?> expr = findOrCreate(conc_value, nonNullRef);
						return new ReferenceExpressionPair(ref, expr);
					} else {
						return new ReferenceExpressionPair(ref, null);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private ReferenceExpressionPair readInstanceField(VariableReference source,
	        Field field, Scope scope) {

		String owner = field.getDeclaringClass().getName().replace(".", "/");
		String name = field.getName();

		Class<?> fieldClazz = field.getType();

		String source_name = source.getName();
		NonNullReference symb_receiver = (NonNullReference) symb_references.get(source_name);

		try {
			Object conc_receiver = source.getObject(scope);

			if (fieldClazz.equals(int.class)) {
				int conc_value = field.getInt(conc_receiver);
				IntegerValue expr = env.heap.getField(owner, name, conc_receiver,
				                                      symb_receiver, conc_value);
				NonNullReference newIntegerRef = newIntegerReference(conc_value, expr);
				return new ReferenceExpressionPair(newIntegerRef, expr);
			} else if (fieldClazz.equals(char.class)) {
				char conc_value = field.getChar(conc_receiver);
				IntegerValue expr = env.heap.getField(owner, name, conc_receiver,
				                                      symb_receiver, conc_value);
				NonNullReference newCharacterRef = newCharacterReference(conc_value, expr);
				return new ReferenceExpressionPair(newCharacterRef, expr);
			} else if (fieldClazz.equals(long.class)) {
				long conc_value = field.getLong(conc_receiver);
				IntegerValue expr = env.heap.getField(owner, name, conc_receiver,
				                                      symb_receiver, conc_value);
				NonNullReference newLongRef = newLongReference(conc_value, expr);
				return new ReferenceExpressionPair(newLongRef, expr);
			} else if (fieldClazz.equals(short.class)) {
				short conc_value = field.getShort(conc_receiver);
				IntegerValue expr = env.heap.getField(owner, name, conc_receiver,
				                                      symb_receiver, conc_value);
				NonNullReference newShortRef = newShortReference(conc_value, expr);
				return new ReferenceExpressionPair(newShortRef, expr);
			} else if (fieldClazz.equals(byte.class)) {
				byte conc_value = field.getByte(conc_receiver);
				IntegerValue expr = env.heap.getField(owner, name, conc_receiver,
				                                      symb_receiver, conc_value);
				NonNullReference newByteRef = newByteReference(conc_value, expr);
				return new ReferenceExpressionPair(newByteRef, expr);
			} else if (fieldClazz.equals(boolean.class)) {
				boolean conc_value = field.getBoolean(conc_receiver);
				IntegerValue expr = env.heap.getField(owner, name, conc_receiver,
				                                      symb_receiver, conc_value ? 1 : 0);
				NonNullReference newBooleanRef = newBooleanReference(conc_value, expr);
				return new ReferenceExpressionPair(newBooleanRef, expr);
			} else if (fieldClazz.equals(float.class)) {
				float conc_value = field.getFloat(conc_receiver);
				RealValue expr = env.heap.getField(owner, name, conc_receiver,
				                                   symb_receiver, conc_value);
				NonNullReference newFloatRef = newFloatReference(conc_value, expr);
				return new ReferenceExpressionPair(newFloatRef, expr);
			} else if (fieldClazz.equals(double.class)) {
				double conc_value = field.getDouble(conc_receiver);
				RealValue expr = env.heap.getField(owner, name, conc_receiver,
				                                   symb_receiver, conc_value);
				NonNullReference newDoubleRef = newDoubleReference(conc_value, expr);
				return new ReferenceExpressionPair(newDoubleRef, expr);
			} else {
				Object conc_value = field.get(conc_receiver);
				if (conc_value instanceof String) {
					String string = (String) conc_value;
					StringValue expr = env.heap.getField(owner, name, conc_receiver,
					                                     symb_receiver, string);
					NonNullReference newStringRef = newStringReference(string, expr);
					return new ReferenceExpressionPair(newStringRef, expr);
				} else {
					Reference ref = env.heap.getReference(conc_value);

					if (conc_value != null && isWrapper(conc_value)) {
						NonNullReference nonNullRef = (NonNullReference) ref;
						Expression<?> expr = findOrCreate(conc_value, nonNullRef);
						return new ReferenceExpressionPair(ref, expr);
					} else {
						return new ReferenceExpressionPair(ref, null);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (CodeUnderTestException e) {
			throw new RuntimeException(e);
		}
	}

	private void before(AssignmentStatement s, Scope scope) {
		/* do nothing */
	}

	private void writeVariable(VariableReference lhs, ReferenceExpressionPair readResult) {
		String lhs_name = lhs.getName();
		Expression<?> expr = readResult.getExpression();
		if (expr != null)
			symb_expressions.put(lhs_name, expr);

		Reference ref = readResult.getReference();
		if (ref != null)
			symb_references.put(lhs_name, ref);

	}

	private void writeArray(ArrayIndex lhs, ReferenceExpressionPair readResult,
	        Scope scope) {

		ArrayReference arrayReference = lhs.getArray();
		int conc_index = lhs.getArrayIndex();

		Object conc_array;
		try {
			conc_array = arrayReference.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}

		Type arrayType = Type.getType(conc_array.getClass());
		Type elementType = arrayType.getElementType();
		if (isValue(elementType) || elementType.equals(Type.getType(String.class))) {
			Expression<?> symb_value = readResult.getExpression();
			symb_value = castIfNeeded(elementType, symb_value);

			String array_name = arrayReference.getName();
			Reference symb_ref = symb_references.get(array_name);
			NonNullReference symb_array = (NonNullReference) symb_ref;
			env.heap.array_store(conc_array, symb_array, conc_index, symb_value);

		} else {
			/* ignore storing references (we use objects to find them) */
		}

	}

	private Expression<?> castIfNeeded(Type elementType, Expression<?> symb_value) {
		// cast integer to real if needed
		if ((isFp32(elementType) || isFp64(elementType))
		        && symb_value instanceof IntegerValue) {
			IntegerValue intExpr = (IntegerValue) symb_value;
			double concValue = intExpr.getConcreteValue().doubleValue();
			symb_value = new IntegerToRealCast(intExpr, concValue);
		} else if ((isBv32(elementType) || isBv64(elementType))
		        && symb_value instanceof RealValue) {
			RealValue realExpr = (RealValue) symb_value;
			long concValue = realExpr.getConcreteValue().longValue();
			symb_value = new RealToIntegerCast(realExpr, concValue);
		}
		return symb_value;
	}

	private void writeField(FieldReference lhs, ReferenceExpressionPair readResult,
	        Scope scope) {
		Field field = lhs.getField().getField();
		String className = field.getDeclaringClass().getName().replace(".", "/");
		String fieldName = field.getName();

		Class<?> fieldClass = field.getType();

		Type fieldType = Type.getType(fieldClass);
		if (isValue(fieldType) || fieldType.equals(Type.getType(String.class))) {
			Expression<?> symb_value = readResult.getExpression();
			symb_value = castIfNeeded(fieldType, symb_value);

			VariableReference source = lhs.getSource();
			if (source != null) {
				/* write symbolic expression to instance field */
				String source_name = source.getName();
				Object conc_receiver;
				try {
					conc_receiver = source.getObject(scope);
				} catch (CodeUnderTestException e) {
					throw new RuntimeException(e);
				}
				NonNullReference symb_receiver = (NonNullReference) symb_references.get(source_name);
				env.heap.putField(className, fieldName, conc_receiver, symb_receiver,
				                  symb_value);
			} else {
				/* write symbolic expression to static field */
				env.heap.putStaticField(className, fieldName, symb_value);
			}
		} else {
			/*
			 * ignore writing of references (DSE does not store Reference
			 * fields)
			 */
		}
	}

	private void before(ShortPrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	private void before(LongPrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	private void before(FloatPrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	private void before(CharPrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	private void before(BytePrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	private void before(BooleanPrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	private void before(DoublePrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	private void before(MethodStatement statement, Scope scope) {
		Method method = statement.getMethod().getMethod();

		String owner = method.getDeclaringClass().getName().replace(".", "/");
		String name = method.getName();
		String desc = Type.getMethodDescriptor(method);

		boolean needThis = statement.getCallee() != null;

		if (needThis) {
			VariableReference callee = statement.getCallee();
			ReferenceExpressionPair refExprPair = read(callee, scope);

			Reference ref = refExprPair.getReference();
			this.env.topFrame().operandStack.pushRef(ref);
		}

		List<VariableReference> parameters = statement.getParameterReferences();
		pushParameterList(parameters, scope, desc);

		if (needThis) {
			VariableReference callee = statement.getCallee();
			Object receiver;
			try {
				receiver = callee.getObject(scope);
			} catch (CodeUnderTestException e) {
				throw new RuntimeException(e);
			}

			Class<?> ownerClass = env.ensurePrepared(owner);
			if (ownerClass.isInterface()) {
				VM.INVOKEINTERFACE(receiver, owner, name, desc);

			} else {
				VM.INVOKEVIRTUAL(receiver, owner, name, desc);

			}
		} else {
			VM.INVOKESTATIC(owner, name, desc);
		}

		call_vm_caller_stack_params(needThis, parameters, scope, desc);

	}

	private void call_vm_caller_stack_params(boolean needThis,
	        List<VariableReference> parameters, Scope scope, String desc) {
		int calleeLocalsIndex = 0;
		if (needThis)
			calleeLocalsIndex++;

		for (int i = 0; i < parameters.size(); i++) {
			VariableReference p = parameters.get(i);
			calleeLocalsIndex += getSize(p.getType());
		}

		Type[] argTypes = Type.getArgumentTypes(desc);

		for (int i = parameters.size() - 1; i >= 0; i--) {
			Type argType = argTypes[i];
			VariableReference p = parameters.get(i);
			try {
				Object param_object = p.getObject(scope);
				calleeLocalsIndex -= getSize(p.getType());
				if (argType.equals(Type.INT_TYPE)) {
					int intValue = getIntValue(param_object);
					VM.CALLER_STACK_PARAM(intValue, i, calleeLocalsIndex);
				} else if (argType.equals(Type.CHAR_TYPE)) {
					char charValue = getCharValue(param_object);
					VM.CALLER_STACK_PARAM(charValue, i, calleeLocalsIndex);
				} else if (argType.equals(Type.BYTE_TYPE)) {
					byte byteValue = getByteValue(param_object);
					VM.CALLER_STACK_PARAM(byteValue, i, calleeLocalsIndex);
				} else if (argType.equals(Type.BOOLEAN_TYPE)) {
					boolean booleanValue = getBooleanValue(param_object);
					VM.CALLER_STACK_PARAM(booleanValue, i, calleeLocalsIndex);
				} else if (argType.equals(Type.SHORT_TYPE)) {
					short shortValue = getShortValue(param_object);
					VM.CALLER_STACK_PARAM(shortValue, i, calleeLocalsIndex);
				} else if (argType.equals(Type.LONG_TYPE)) {
					long longValue = getLongValue(param_object);
					VM.CALLER_STACK_PARAM(longValue, i, calleeLocalsIndex);
				} else if (argType.equals(Type.FLOAT_TYPE)) {
					float floatValue = getFloatValue(param_object);
					VM.CALLER_STACK_PARAM(floatValue, i, calleeLocalsIndex);
				} else if (argType.equals(Type.DOUBLE_TYPE)) {
					double doubleValue = getDoubleValue(param_object);
					VM.CALLER_STACK_PARAM(doubleValue, i, calleeLocalsIndex);
				} else {
					VM.CALLER_STACK_PARAM(param_object, i, calleeLocalsIndex);
				}
			} catch (CodeUnderTestException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static double getDoubleValue(Object o) {
		if (o == null) {
			return 0;
		}
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1 : 0;
		} else if (o instanceof Short) {
			return ((Short) o).shortValue();
		} else if (o instanceof Byte) {
			return ((Byte) o).byteValue();
		} else if (o instanceof Character) {
			return ((Character) o).charValue();
		} else if (o instanceof Integer) {
			return ((Integer) o).intValue();
		} else if (o instanceof Long) {
			return ((Long) o).longValue();
		} else if (o instanceof Float) {
			return ((Float) o).floatValue();
		} else if (o instanceof Double) {
			return ((Double) o).doubleValue();
		} else {
			throw new EvosuiteError("Unreachable code!");
		}
	}

	private static float getFloatValue(Object o) {
		if (o == null) {
			return 0;
		}
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1 : 0;
		} else if (o instanceof Short) {
			return ((Short) o).shortValue();
		} else if (o instanceof Byte) {
			return ((Byte) o).byteValue();
		} else if (o instanceof Character) {
			return ((Character) o).charValue();
		} else if (o instanceof Integer) {
			return ((Integer) o).intValue();
		} else if (o instanceof Long) {
			return ((Long) o).longValue();
		} else if (o instanceof Float) {
			return ((Float) o).floatValue();
		} else if (o instanceof Double) {
			return (float) ((Double) o).doubleValue();
		} else {
			throw new EvosuiteError("Unreachable code!");
		}
	}

	private static long getLongValue(Object o) {
		if (o == null) {
			return 0;
		}
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1 : 0;
		} else if (o instanceof Short) {
			return ((Short) o).shortValue();
		} else if (o instanceof Byte) {
			return ((Byte) o).byteValue();
		} else if (o instanceof Character) {
			return ((Character) o).charValue();
		} else if (o instanceof Integer) {
			return ((Integer) o).intValue();
		} else if (o instanceof Long) {
			return ((Long) o).longValue();
		} else if (o instanceof Float) {
			return (long) ((Float) o).floatValue();
		} else if (o instanceof Double) {
			return (long) ((Double) o).doubleValue();
		} else {
			throw new EvosuiteError("Unreachable code!");
		}
	}

	private static int getIntValue(Object o) {
		if (o == null) {
			return 0;
		}
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1 : 0;
		} else if (o instanceof Short) {
			return ((Short) o).shortValue();
		} else if (o instanceof Byte) {
			return ((Byte) o).byteValue();
		} else if (o instanceof Character) {
			return ((Character) o).charValue();
		} else if (o instanceof Integer) {
			return ((Integer) o).intValue();
		} else if (o instanceof Long) {
			return (int) ((Long) o).longValue();
		} else if (o instanceof Float) {
			return (int) ((Float) o).floatValue();
		} else if (o instanceof Double) {
			return (int) ((Double) o).doubleValue();
		} else {
			throw new EvosuiteError("Unreachable code!");
		}
	}

	private static short getShortValue(Object o) {
		if (o == null) {
			return 0;
		}
		if (o instanceof Boolean) {
			return (short) (((Boolean) o).booleanValue() ? 1 : 0);
		} else if (o instanceof Short) {
			return ((Short) o).shortValue();
		} else if (o instanceof Byte) {
			return ((Byte) o).byteValue();
		} else if (o instanceof Character) {
			return (short) ((Character) o).charValue();
		} else if (o instanceof Integer) {
			return (short) ((Integer) o).intValue();
		} else if (o instanceof Long) {
			return (short) ((Long) o).longValue();
		} else if (o instanceof Float) {
			return (short) ((Float) o).floatValue();
		} else if (o instanceof Double) {
			return (short) ((Double) o).doubleValue();
		} else {
			throw new EvosuiteError("Unreachable code!");
		}
	}

	private static byte getByteValue(Object o) {
		if (o == null) {
			return 0;
		}
		if (o instanceof Boolean) {
			return (byte) (((Boolean) o).booleanValue() ? 1 : 0);
		} else if (o instanceof Short) {
			return (byte) ((Short) o).shortValue();
		} else if (o instanceof Byte) {
			return ((Byte) o).byteValue();
		} else if (o instanceof Character) {
			return (byte) ((Character) o).charValue();
		} else if (o instanceof Integer) {
			return (byte) ((Integer) o).intValue();
		} else if (o instanceof Long) {
			return (byte) ((Long) o).longValue();
		} else if (o instanceof Float) {
			return (byte) ((Float) o).floatValue();
		} else if (o instanceof Double) {
			return (byte) ((Double) o).doubleValue();
		} else {
			throw new EvosuiteError("Unreachable code!");
		}
	}

	private static char getCharValue(Object o) {
		if (o == null) {
			return 0;
		}
		if (o instanceof Boolean) {
			return (char) (((Boolean) o).booleanValue() ? 1 : 0);
		} else if (o instanceof Short) {
			return (char) ((Short) o).shortValue();
		} else if (o instanceof Byte) {
			return (char) ((Byte) o).byteValue();
		} else if (o instanceof Character) {
			return ((Character) o).charValue();
		} else if (o instanceof Integer) {
			return (char) ((Integer) o).intValue();
		} else if (o instanceof Long) {
			return (char) ((Long) o).longValue();
		} else if (o instanceof Float) {
			return (char) ((Float) o).floatValue();
		} else if (o instanceof Double) {
			return (char) ((Double) o).doubleValue();
		} else {
			throw new EvosuiteError("Unreachable code!");
		}
	}

	private static boolean getBooleanValue(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue();
		} else if (o instanceof Short) {
			return ((Short) o).shortValue() == 1;
		} else if (o instanceof Byte) {
			return ((Byte) o).byteValue() == 1;
		} else if (o instanceof Character) {
			return ((Character) o).charValue() == 1;
		} else if (o instanceof Integer) {
			return ((Integer) o).intValue() == 1;
		} else if (o instanceof Long) {
			return ((Long) o).longValue() == 1;
		} else if (o instanceof Float) {
			return ((Float) o).floatValue() == 1;
		} else if (o instanceof Double) {
			return ((Double) o).doubleValue() == 1;
		} else {
			throw new EvosuiteError("Unreachable code!");
		}
	}

	private int getSize(java.lang.reflect.Type type) {
		if (type.equals(double.class))
			return 2;
		else if (type.equals(long.class))
			return 2;
		else
			return 1;
	}

	private void pushParameterList(List<VariableReference> parameters, Scope scope,
	        String desc) {

		Type[] argTypes = Type.getArgumentTypes(desc);

		for (int i = 0; i < parameters.size(); i++) {

			VariableReference varRef = parameters.get(i);
			Type argType = argTypes[i];
			ReferenceExpressionPair readResult = this.read(varRef, scope);
			Expression<?> symb_expr = readResult.getExpression();
			Reference symb_ref = readResult.getReference();

			if (isValue(argType)) {

				if (symb_expr instanceof RealValue) {
					RealValue realExpr = (RealValue) symb_expr;
					if (isFp32(argType)) {
						env.topFrame().operandStack.pushFp32(realExpr);
					} else if (isFp64(argType)) {
						env.topFrame().operandStack.pushFp64(realExpr);
					} else if (isBv32(argType)) {
						int concV = realExpr.getConcreteValue().intValue();
						RealToIntegerCast castExpr = new RealToIntegerCast(realExpr,
						        (long) concV);
						env.topFrame().operandStack.pushBv32(castExpr);
					} else if (isBv64(argType)) {
						long concV = realExpr.getConcreteValue().longValue();
						RealToIntegerCast castExpr = new RealToIntegerCast(realExpr,
						        concV);
						env.topFrame().operandStack.pushBv64(castExpr);
					} else {
						/* unreachable code */
					}
				} else if (symb_expr instanceof IntegerValue) {
					IntegerValue integerExpr = (IntegerValue) symb_expr;
					if (isBv32(argType)) {
						env.topFrame().operandStack.pushBv32(integerExpr);
					} else if (isBv64(argType)) {
						env.topFrame().operandStack.pushBv64(integerExpr);
					} else if (isFp32(argType)) {
						float concV = integerExpr.getConcreteValue().floatValue();
						IntegerToRealCast castExpr = new IntegerToRealCast(integerExpr,
						        (double) concV);
						env.topFrame().operandStack.pushFp32(castExpr);
					} else if (isFp64(argType)) {
						double concV = integerExpr.getConcreteValue().doubleValue();
						IntegerToRealCast castExpr = new IntegerToRealCast(integerExpr,
						        concV);
						env.topFrame().operandStack.pushFp64(castExpr);
					} else {
						/* unreachable code */
					}

				} else {

					if (symb_ref instanceof NullReference) {
						// although this will lead in the JVM to a NPE, we push
						// a dummy
						// value to prevent the DSE VM from crashing
						pushDummyValue(argType);
						return;
					} else {
						throw new EvosuiteError("no expression for value!");
					}
				}
			} else {

				Reference ref = readResult.getReference();
				env.topFrame().operandStack.pushRef(ref);
			}

		}
	}

	private void pushDummyValue(Type argType) {
		if (isBv32(argType)) {
			IntegerValue integerExpr = ExpressionFactory.buildNewIntegerConstant(0);
			env.topFrame().operandStack.pushBv32(integerExpr);
		} else if (isBv64(argType)) {
			IntegerValue integerExpr = ExpressionFactory.buildNewIntegerConstant(0);
			env.topFrame().operandStack.pushBv64(integerExpr);
		} else if (isFp32(argType)) {
			RealValue realExpr = ExpressionFactory.buildNewRealConstant(0);
			env.topFrame().operandStack.pushFp32(realExpr);
		} else if (isFp64(argType)) {
			RealValue realExpr = ExpressionFactory.buildNewRealConstant(0);
			env.topFrame().operandStack.pushFp64(realExpr);
		} else {
			throw new EvosuiteError(argType.toString() + " is not a value type!");
		}
	}

	private static boolean isValue(Type t) {
		return isBv32(t) || isBv64(t) || isFp32(t) || isFp64(t);
	}

	private static boolean isFp64(Type t) {
		return t.equals(Type.DOUBLE_TYPE);
	}

	private static boolean isFp32(Type t) {
		return t.equals(Type.FLOAT_TYPE);
	}

	private static boolean isBv64(Type t) {
		return t.equals(Type.LONG_TYPE);
	}

	private static boolean isBv32(Type t) {
		return t.equals(Type.CHAR_TYPE) || t.equals(Type.BOOLEAN_TYPE)
		        || t.equals(Type.SHORT_TYPE) || t.equals(Type.BYTE_TYPE)
		        || t.equals(Type.INT_TYPE);
	}

	/**
	 * This method forbids using the same interning String in two separate
	 * string primitive statements.
	 * 
	 * @param statement
	 * @param scope
	 */
	private void before(StringPrimitiveStatement statement, Scope scope) {
		/* do nothing */ 

	}

	private void before(IntPrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	@Override
	public void afterStatement(StatementInterface s, Scope scope, Throwable exception) {

		if (exception != null) {
			return;
		}

		if (VM.vm.isStopped()) {
			return;
		}

		try {
			if (s instanceof NullStatement) {
				after((NullStatement) s, scope);

			} else if (s instanceof EnumPrimitiveStatement<?>) {
				after((EnumPrimitiveStatement<?>) s, scope);

			} else if (s instanceof ArrayStatement) {
				after((ArrayStatement) s, scope);

			} else if (s instanceof AssignmentStatement) {
				after((AssignmentStatement) s, scope);

			} else if (s instanceof FieldStatement) {
				after((FieldStatement) s, scope);

			} else if (s instanceof ConstructorStatement) {
				after((ConstructorStatement) s, scope);
			}
			/* primitive statements */
			else if (s instanceof BooleanPrimitiveStatement) {
				after((BooleanPrimitiveStatement) s, scope);

			} else if (s instanceof MethodStatement) {
				after((MethodStatement) s, scope);

			} else if (s instanceof BytePrimitiveStatement) {
				after((BytePrimitiveStatement) s, scope);

			} else if (s instanceof CharPrimitiveStatement) {
				after((CharPrimitiveStatement) s, scope);

			} else if (s instanceof DoublePrimitiveStatement) {
				after((DoublePrimitiveStatement) s, scope);

			} else if (s instanceof FloatPrimitiveStatement) {
				after((FloatPrimitiveStatement) s, scope);

			} else if (s instanceof IntPrimitiveStatement) {
				after((IntPrimitiveStatement) s, scope);

			} else if (s instanceof LongPrimitiveStatement) {
				after((LongPrimitiveStatement) s, scope);

			} else if (s instanceof ShortPrimitiveStatement) {
				after((ShortPrimitiveStatement) s, scope);

			} else if (s instanceof StringPrimitiveStatement) {
				after((StringPrimitiveStatement) s, scope);
			} else {
				throw new UnsupportedOperationException();
			}
		} catch (Throwable t) {
			throw new EvosuiteError(t);
		}
	}

	private void before(ArrayStatement s, Scope scope) {
		/* do nothing */
	}

	private void after(EnumPrimitiveStatement<?> s, Scope scope) {
		VariableReference varRef = s.getReturnValue();
		String varName = varRef.getName();
		Object conc_value = s.getValue();
		Reference symb_value = env.heap.getReference(conc_value);
		symb_references.put(varName, symb_value);
	}

	private void after(NullStatement s, Scope scope) {
		VariableReference lhs = s.getReturnValue();
		String lhs_name = lhs.getName();

		symb_references.put(lhs_name, NullReference.getInstance());
	}

	private void after(FieldStatement s, Scope scope) {
		ReferenceExpressionPair readResult;
		if (s.getSource() != null) {
			readResult = readInstanceField(s.getSource(), s.getField().getField(), scope);
		} else {
			readResult = readStaticField(s.getField().getField());
		}

		String lhs_name = s.getReturnValue().getName();

		Expression<?> expr = readResult.getExpression();
		Reference ref = readResult.getReference();

		if (expr != null)
			symb_expressions.put(lhs_name, expr);

		if (ref != null)
			symb_references.put(lhs_name, ref);
	}

	private void after(ShortPrimitiveStatement statement, Scope scope) {
		short valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = buildIntegerVariable(varRefName, valueOf,
		                                                       Short.MIN_VALUE,
		                                                       Short.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);

		Short short_instance;
		try {
			short_instance = (Short) varRef.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}
		NonNullReference shortRef = newShortReference(short_instance, integerVariable);
		symb_references.put(varRefName, shortRef);
	}

	private NonNullReference newShortReference(Short conc_short, IntegerValue symb_value) {
		NonNullReference shortRef = (NonNullReference) env.heap.getReference(conc_short);
		env.heap.putField(Types.JAVA_LANG_SHORT, SymbolicHeap.$SHORT_VALUE, conc_short,
		                  shortRef, symb_value);
		return shortRef;
	}

	private void after(LongPrimitiveStatement statement, Scope scope) {
		long valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = buildIntegerVariable(varRefName, valueOf,
		                                                       Long.MIN_VALUE,
		                                                       Long.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);

		Long long_instance;
		try {
			long_instance = (Long) varRef.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}
		NonNullReference longRef = newLongReference(long_instance, integerVariable);
		symb_references.put(varRefName, longRef);
	}

	private NonNullReference newLongReference(Long conc_long, IntegerValue symb_value) {
		NonNullReference longRef = (NonNullReference) env.heap.getReference(conc_long);
		env.heap.putField(Types.JAVA_LANG_LONG, SymbolicHeap.$LONG_VALUE, conc_long,
		                  longRef, symb_value);
		return longRef;
	}

	private void after(FloatPrimitiveStatement statement, Scope scope) {
		float valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		RealVariable realVariable = buildRealVariable(varRefName, valueOf,
		                                              -Float.MAX_VALUE, Float.MAX_VALUE);
		symb_expressions.put(varRefName, realVariable);

		Float float_instance;
		try {
			float_instance = (Float) varRef.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}
		NonNullReference floatRef = newFloatReference(float_instance, realVariable);
		symb_references.put(varRefName, floatRef);
	}

	private NonNullReference newFloatReference(Float conc_float, RealValue symb_value) {
		NonNullReference floatRef = (NonNullReference) env.heap.getReference(conc_float);
		env.heap.putField(Types.JAVA_LANG_FLOAT, SymbolicHeap.$FLOAT_VALUE, conc_float,
		                  floatRef, symb_value);
		return floatRef;
	}

	private void after(CharPrimitiveStatement statement, Scope scope) {
		char valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = buildIntegerVariable(varRefName, valueOf,
		                                                       Character.MIN_VALUE,
		                                                       Character.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);

		Character character0;
		try {
			character0 = (Character) varRef.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}
		NonNullReference charRef = newCharacterReference(character0, integerVariable);
		symb_references.put(varRefName, charRef);
	}

	private NonNullReference newCharacterReference(Character conc_char,
	        IntegerValue symb_value) {
		NonNullReference charRef = (NonNullReference) env.heap.getReference(conc_char);
		env.heap.putField(Types.JAVA_LANG_CHARACTER, SymbolicHeap.$CHAR_VALUE, conc_char,
		                  charRef, symb_value);
		return charRef;
	}

	private void after(BytePrimitiveStatement statement, Scope scope) {
		byte valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = buildIntegerVariable(varRefName, valueOf,
		                                                       Byte.MIN_VALUE,
		                                                       Byte.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);
		Byte byte_instance;
		try {
			byte_instance = (Byte) varRef.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}

		NonNullReference byteRef = newByteReference(byte_instance, integerVariable);

		symb_references.put(varRefName, byteRef);
	}

	private NonNullReference newByteReference(Byte conc_byte, IntegerValue symb_value) {
		NonNullReference byteRef = (NonNullReference) env.heap.getReference(conc_byte);
		env.heap.putField(Types.JAVA_LANG_BYTE, SymbolicHeap.$BYTE_VALUE, conc_byte,
		                  byteRef, symb_value);
		return byteRef;
	}

	private void after(BooleanPrimitiveStatement statement, Scope scope) {
		boolean valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = buildIntegerVariable(varRefName, valueOf ? 1
		        : 0, 0, 1);
		Boolean boolean_instance;
		try {
			boolean_instance = (Boolean) varRef.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}

		symb_expressions.put(varRefName, integerVariable);
		NonNullReference booleanRef = newBooleanReference(boolean_instance,
		                                                  integerVariable);
		symb_references.put(varRefName, booleanRef);
	}

	private NonNullReference newBooleanReference(Boolean conc_boolean,
	        IntegerValue symb_value) {
		NonNullReference booleanRef = (NonNullReference) env.heap.getReference(conc_boolean);
		env.heap.putField(Types.JAVA_LANG_BOOLEAN, SymbolicHeap.$BOOLEAN_VALUE,
		                  conc_boolean, booleanRef, symb_value);
		return booleanRef;
	}

	private void after(DoublePrimitiveStatement statement, Scope scope) {
		double valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		RealVariable realVariable = buildRealVariable(varRefName, valueOf,
		                                              -Double.MAX_VALUE, Double.MAX_VALUE);
		symb_expressions.put(varRefName, realVariable);

		Double double_instance;
		try {
			double_instance = (Double) varRef.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}
		NonNullReference doubleRef = newDoubleReference(double_instance, realVariable);
		symb_references.put(varRefName, doubleRef);
	}

	private NonNullReference newDoubleReference(Double conc_double, RealValue symb_value) {
		NonNullReference doubleRef = (NonNullReference) env.heap.getReference(conc_double);
		env.heap.putField(Types.JAVA_LANG_DOUBLE, SymbolicHeap.$DOUBLE_VALUE,
		                  conc_double, doubleRef, symb_value);
		return doubleRef;
	}

	private void after(MethodStatement statement, Scope scope) {
		String owner = statement.getMethod().getDeclaringClass().getName().replace(".",
		                                                                           "/");
		String name = statement.getMethod().getName();
		String desc = Type.getMethodDescriptor(statement.getMethod().getMethod());

		Type returnType = Type.getReturnType(statement.getMethod().getMethod());

		VariableReference varRef = statement.getReturnValue();
		String varName = varRef.getName();
		try {
			if (varRef.getType().equals(void.class)) {
				VM.CALL_RESULT(owner, name, desc);

			} else if (returnType.equals(Type.INT_TYPE)) {
				Integer res = (Integer) varRef.getObject(scope);
				VM.CALL_RESULT(res.intValue(), owner, name, desc);
				IntegerValue intExpr = env.topFrame().operandStack.popBv32();
				NonNullReference newIntegerRef = newIntegerReference(res, intExpr);
				symb_references.put(varName, newIntegerRef);
				symb_expressions.put(varName, intExpr);

			} else if (returnType.equals(Type.BOOLEAN_TYPE)) {
				Boolean res = (Boolean) varRef.getObject(scope);
				VM.CALL_RESULT(res.booleanValue(), owner, name, desc);
				IntegerValue intExpr = env.topFrame().operandStack.popBv32();
				NonNullReference newBooleanRef = newBooleanReference(res, intExpr);
				symb_references.put(varName, newBooleanRef);
				symb_expressions.put(varName, intExpr);

			} else if (returnType.equals(Type.DOUBLE_TYPE)) {
				Double res = (Double) varRef.getObject(scope);
				VM.CALL_RESULT(res.doubleValue(), owner, name, desc);
				RealValue realExpr = env.topFrame().operandStack.popFp64();
				NonNullReference newDoubleRef = newDoubleReference(res, realExpr);
				symb_references.put(varName, newDoubleRef);
				symb_expressions.put(varName, realExpr);

			} else if (returnType.equals(Type.FLOAT_TYPE)) {
				Float res = (Float) varRef.getObject(scope);
				VM.CALL_RESULT(res.floatValue(), owner, name, desc);
				RealValue realExpr = env.topFrame().operandStack.popFp32();
				NonNullReference newFloatRef = newFloatReference(res, realExpr);
				symb_references.put(varName, newFloatRef);
				symb_expressions.put(varName, realExpr);

			} else if (returnType.equals(Type.LONG_TYPE)) {
				Long res = (Long) varRef.getObject(scope);
				VM.CALL_RESULT(res.longValue(), owner, name, desc);
				IntegerValue intExpr = env.topFrame().operandStack.popBv64();
				NonNullReference newBooleanRef = newLongReference(res, intExpr);
				symb_references.put(varName, newBooleanRef);
				symb_expressions.put(varName, intExpr);

			} else if (returnType.equals(Type.SHORT_TYPE)) {
				Short res = (Short) varRef.getObject(scope);
				VM.CALL_RESULT(res.shortValue(), owner, name, desc);
				IntegerValue intExpr = env.topFrame().operandStack.popBv32();
				NonNullReference newShortRef = newShortReference(res, intExpr);
				symb_references.put(varName, newShortRef);
				symb_expressions.put(varName, intExpr);

			} else if (returnType.equals(Type.BYTE_TYPE)) {
				Byte res = (Byte) varRef.getObject(scope);
				VM.CALL_RESULT(res.byteValue(), owner, name, desc);
				IntegerValue intExpr = env.topFrame().operandStack.popBv32();
				NonNullReference newByteRef = newByteReference(res, intExpr);
				symb_references.put(varName, newByteRef);
				symb_expressions.put(varName, intExpr);

			} else if (returnType.equals(Type.CHAR_TYPE)) {
				Character res = (Character) varRef.getObject(scope);
				VM.CALL_RESULT(res.charValue(), owner, name, desc);
				IntegerValue intExpr = env.topFrame().operandStack.popBv32();
				NonNullReference newCharacterRef = newCharacterReference(res, intExpr);
				symb_references.put(varName, newCharacterRef);
				symb_expressions.put(varName, intExpr);

			} else {
				Object res = varRef.getObject(scope);
				VM.CALL_RESULT(res, owner, name, desc);

				Reference ref = env.topFrame().operandStack.peekRef();

				if (res != null && res instanceof String) {

					String string = (String) res;
					NonNullReference newStringRef = (NonNullReference) env.heap.getReference(string);
					StringValue str_expr = env.heap.getField(Types.JAVA_LANG_STRING,
					                                         SymbolicHeap.$STRING_VALUE,
					                                         string, newStringRef, string);
					symb_references.put(varName, newStringRef);
					symb_expressions.put(varName, str_expr);
				} else {
					symb_references.put(varName, ref);
					if (res != null && isWrapper(res)) {
						NonNullReference nonNullRef = (NonNullReference) ref;
						Expression<?> expr = findOrCreate(res, nonNullRef);
						symb_expressions.put(varName, expr);
					}
				}

			}
		} catch (CodeUnderTestException e) {
			throw new RuntimeException(e);
		}
		// dispose all other arguments
		env.topFrame().operandStack.clearOperands();

	}

	private Expression<?> findOrCreate(Object conc_ref, NonNullReference symb_ref) {
		if (conc_ref instanceof Boolean) {
			Boolean boolean0 = (Boolean) conc_ref;
			int conc_val = boolean0.booleanValue() ? 1 : 0;
			return env.heap.getField(Types.JAVA_LANG_BOOLEAN,
			                         SymbolicHeap.$BOOLEAN_VALUE, boolean0, symb_ref,
			                         conc_val);
		} else if (conc_ref instanceof Byte) {
			Byte byte0 = (Byte) conc_ref;
			byte conc_val = byte0.byteValue();
			return env.heap.getField(Types.JAVA_LANG_BYTE, SymbolicHeap.$BYTE_VALUE,
			                         byte0, symb_ref, conc_val);
		} else if (conc_ref instanceof Short) {
			Short short0 = (Short) conc_ref;
			short conc_val = short0.shortValue();
			return env.heap.getField(Types.JAVA_LANG_SHORT, SymbolicHeap.$SHORT_VALUE,
			                         short0, symb_ref, conc_val);
		} else if (conc_ref instanceof Character) {
			Character character0 = (Character) conc_ref;
			char conc_val = character0.charValue();
			return env.heap.getField(Types.JAVA_LANG_CHARACTER, SymbolicHeap.$CHAR_VALUE,
			                         character0, symb_ref, conc_val);
		} else if (conc_ref instanceof Integer) {
			Integer integer0 = (Integer) conc_ref;
			int conc_val = integer0.intValue();
			return env.heap.getField(Types.JAVA_LANG_INTEGER, SymbolicHeap.$INT_VALUE,
			                         integer0, symb_ref, conc_val);
		} else if (conc_ref instanceof Long) {
			Long long0 = (Long) conc_ref;
			long conc_val = long0.longValue();
			return env.heap.getField(Types.JAVA_LANG_LONG, SymbolicHeap.$LONG_VALUE,
			                         long0, symb_ref, conc_val);
		} else if (conc_ref instanceof Float) {
			Float float0 = (Float) conc_ref;
			float conc_val = float0.floatValue();
			return env.heap.getField(Types.JAVA_LANG_FLOAT, SymbolicHeap.$FLOAT_VALUE,
			                         float0, symb_ref, conc_val);
		} else if (conc_ref instanceof Double) {
			Double double0 = (Double) conc_ref;
			double conc_val = double0.doubleValue();
			return env.heap.getField(Types.JAVA_LANG_FLOAT, SymbolicHeap.$DOUBLE_VALUE,
			                         double0, symb_ref, conc_val);
		} else {
			throw new EvosuiteError("Unreachable code!");
		}
	}

	private static boolean isWrapper(Object res) {
		return res instanceof Boolean || res instanceof Short || res instanceof Byte
		        || res instanceof Integer || res instanceof Character
		        || res instanceof Long || res instanceof Float || res instanceof Double;
	}

	private void after(StringPrimitiveStatement statement, Scope scope) {
		String valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		StringVariable stringVariable = buildStringVariable(varRefName, valueOf);
		symb_expressions.put(varRefName, stringVariable);

		String string_instance;
		try {
			String string_interned = (String) varRef.getObject(scope);
			string_instance =new String(string_interned);
			scope.setObject(varRef, string_instance);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}
		NonNullReference stringRef = newStringReference(string_instance, stringVariable);
		symb_references.put(varRefName, stringRef);
	}

	private NonNullReference newStringReference(String conc_string, StringValue str_expr) {
		NonNullReference stringRef = (NonNullReference) env.heap.getReference(conc_string);
		env.heap.putField(Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
		                  conc_string, stringRef, str_expr);
		return stringRef;
	}

	private final Map<String, Expression<?>> symb_expressions = new HashMap<String, Expression<?>>();
	private final Map<String, Reference> symb_references = new HashMap<String, Reference>();
	private final Map<String, IntegerVariable> integerVariables = new HashMap<String, IntegerVariable>();
	private final Map<String, RealVariable> realVariables = new HashMap<String, RealVariable>();
	private final Map<String, StringVariable> stringVariables = new HashMap<String, StringVariable>();

	private void after(IntPrimitiveStatement statement, Scope scope) {
		int valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = buildIntegerVariable(varRefName, valueOf,
		                                                       Integer.MIN_VALUE,
		                                                       Integer.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);

		Integer integer_instance;
		try {
			integer_instance = (Integer) varRef.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new EvosuiteError(e);
		}
		NonNullReference integerRef = newIntegerReference(integer_instance,
		                                                  integerVariable);
		symb_references.put(varRefName, integerRef);
	}

	private NonNullReference newIntegerReference(Integer conc_integer,
	        IntegerValue symb_value) {
		NonNullReference integerRef = (NonNullReference) env.heap.getReference(conc_integer);
		env.heap.putField(Types.JAVA_LANG_INTEGER, SymbolicHeap.$INT_VALUE, conc_integer,
		                  integerRef, symb_value);
		return integerRef;
	}

	@Override
	public void clear() {
		symb_expressions.clear();
		symb_references.clear();
	}

	private IntegerVariable buildIntegerVariable(String name, long conV, long minValue,
	        long maxValue) {

		IntegerVariable integerVariable;
		if (integerVariables.containsKey(name)) {
			integerVariable = integerVariables.get(name);
			integerVariable.setConcreteValue(conV);
			assert minValue == integerVariable.getMinValue();
			assert maxValue == integerVariable.getMaxValue();
		} else {
			integerVariable = new IntegerVariable(name, conV, minValue, maxValue);
			integerVariables.put(name, integerVariable);
		}
		return integerVariable;
	}

	private RealVariable buildRealVariable(String name, double conV, double minValue,
	        double maxValue) {

		RealVariable realVariable;
		if (realVariables.containsKey(name)) {
			realVariable = realVariables.get(name);
			realVariable.setConcreteValue(conV);
			assert minValue == realVariable.getMinValue();
			assert maxValue == realVariable.getMaxValue();
		} else {
			realVariable = new RealVariable(name, conV, minValue, maxValue);
			realVariables.put(name, realVariable);
		}
		return realVariable;
	}

	private StringVariable buildStringVariable(String name, String concVal) {

		StringVariable stringVariable;
		if (stringVariables.containsKey(name)) {
			stringVariable = stringVariables.get(name);
			stringVariable.setConcreteValue(concVal);
		} else {
			stringVariable = new StringVariable(name, concVal);
			stringVariables.put(name, stringVariable);
		}
		return stringVariable;
	}

}
