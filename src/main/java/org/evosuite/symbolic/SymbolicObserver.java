package org.evosuite.symbolic;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.IntegerVariable;
import org.evosuite.symbolic.expr.IntegerVariableFactory;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealVariable;
import org.evosuite.symbolic.expr.RealVariableFactory;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringVariable;
import org.evosuite.symbolic.expr.StringVariableFactory;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.IntegerOperand;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.NullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.RealOperand;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.ReferenceOperand;
import org.evosuite.symbolic.vm.StringReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
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
import gnu.trove.map.hash.THashMap;

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
		pushParameterList(stmt.parameters, scope);
		String owner = className.replace(".", "/");
		String desc = Type.getConstructorDescriptor(stmt.getConstructor());
		/* indicates if the following code is instrumented or not */
		VM.INVOKESPECIAL(owner, INIT, desc);
		boolean needThis = true;
		call_vm_caller_stack_params(needThis, stmt.parameters, scope);
	}

	private void after(ConstructorStatement stmt, Scope scope) {
		String className = stmt.getConstructor().getDeclaringClass().getName();
		String desc = Type.getConstructorDescriptor(stmt.getConstructor());
		/* pops operands if previous code was not instrumented */
		// constructor return type is always VOID
		String onwer = className.replace(".", "/");
		VM.CALL_RESULT(onwer, INIT, desc);
		VariableReference varRef = stmt.getReturnValue();

		NonNullReference nonNullRef = (NonNullReference) env.topFrame().operandStack
				.popRef();
		String varName = varRef.getName();
		symb_references.put(varName, nonNullRef);

	}

	@Override
	public void beforeStatement(StatementInterface s, Scope scope) {
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
				IntegerConstant lengthExpr = ExpressionFactory
						.buildNewIntegerConstant(length);
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
					String componentTypeName = component_class.getName()
							.replace(".", "/");
					VM.ANEWARRAY(length, componentTypeName);
				}
			} else {
				// push dimensions
				for (int i = 0; i < arrayRef.getArrayDimensions(); i++) {
					int length = arrayRef.getLengths()[i];
					IntegerConstant lengthExpr = ExpressionFactory
							.buildNewIntegerConstant(length);
					env.topFrame().operandStack.pushBv32(lengthExpr);
				}
				String arrayTypeDesc = Type
						.getDescriptor(conc_array.getClass());
				VM.MULTIANEWARRAY(arrayTypeDesc, arrayRef.getArrayDimensions());

			}
			NonNullReference symb_array = (NonNullReference) env.topFrame().operandStack
					.popRef();
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

	private enum RESULT_TYPE {
		REFERENCE, EXPRESSION
	}

	private static class ExpressionOrReferenceResult {
		private final RESULT_TYPE resultType;
		private final Reference ref;
		private final Expression<?> expr;

		public ExpressionOrReferenceResult(Expression<?> expr) {
			this.resultType = RESULT_TYPE.EXPRESSION;
			this.ref = null;
			this.expr = expr;
		}

		public ExpressionOrReferenceResult(Reference ref) {
			this.resultType = RESULT_TYPE.REFERENCE;
			this.ref = ref;
			this.expr = null;
		}

		public Reference getReference() {
			return ref;
		}

		public Expression<?> getExpression() {
			return expr;
		}

		public boolean isExpression() {
			return resultType == RESULT_TYPE.EXPRESSION;
		}

		public boolean isReference() {
			return resultType == RESULT_TYPE.REFERENCE;
		}

	}

	private void after(AssignmentStatement s, Scope scope) {
		VariableReference lhs = s.getReturnValue();
		VariableReference rhs = s.getValue();

		ExpressionOrReferenceResult readResult = read(rhs, scope);

		if (lhs instanceof FieldReference) {
			writeField((FieldReference) lhs, readResult, scope);
		} else if (lhs instanceof ArrayIndex) {
			writeArray((ArrayIndex) lhs, readResult, scope);
		} else {
			writeVariable(lhs, readResult);
		}
	}

	private ExpressionOrReferenceResult read(VariableReference rhs, Scope scope) {
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
	private ExpressionOrReferenceResult readVariable(VariableReference rhs,
			Scope scope) {
		String rhs_name = rhs.getName();
		if (this.symb_references.containsKey(rhs_name)) {
			Reference symb_ref = symb_references.get(rhs_name);
			return new ExpressionOrReferenceResult(symb_ref);
		} else if (symb_expressions.containsKey(rhs_name)) {
			Expression<?> symb_expr = symb_expressions.get(rhs_name);
			return new ExpressionOrReferenceResult(symb_expr);
		} else {
			throw new IllegalArgumentException(
					"Cannot find symbolic value for " + rhs_name);
		}

	}

	private ExpressionOrReferenceResult readArray(ArrayIndex rhs, Scope scope) {
		ArrayReference arrayReference = rhs.getArray();
		NonNullReference symb_array = (NonNullReference) symb_references
				.get(arrayReference.getName());
		int conc_index = rhs.getArrayIndex();
		Class<?> componentClass = arrayReference.getComponentClass();

		try {
			Object conc_array = arrayReference.getObject(scope);

			if (componentClass.equals(int.class)) {
				int conc_value = Array.getInt(conc_array, conc_index);
				IntegerExpression expr = env.heap.array_load(symb_array,
						conc_index, (long) conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (componentClass.equals(char.class)) {
				char conc_value = Array.getChar(conc_array, conc_index);
				IntegerExpression expr = env.heap.array_load(symb_array,
						conc_index, (long) conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (componentClass.equals(boolean.class)) {
				boolean conc_value = Array.getBoolean(conc_array, conc_index);
				IntegerExpression expr = env.heap.array_load(symb_array,
						conc_index, (long) (conc_value ? 1 : 0));
				return new ExpressionOrReferenceResult(expr);
			} else if (componentClass.equals(byte.class)) {
				byte conc_value = Array.getByte(conc_array, conc_index);
				IntegerExpression expr = env.heap.array_load(symb_array,
						conc_index, (long) conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (componentClass.equals(short.class)) {
				short conc_value = Array.getShort(conc_array, conc_index);
				IntegerExpression expr = env.heap.array_load(symb_array,
						conc_index, (long) conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (componentClass.equals(long.class)) {
				long conc_value = Array.getLong(conc_array, conc_index);
				IntegerExpression expr = env.heap.array_load(symb_array,
						conc_index, conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (componentClass.equals(float.class)) {
				float conc_value = Array.getFloat(conc_array, conc_index);
				RealExpression expr = env.heap.array_load(symb_array,
						conc_index, (double) conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (componentClass.equals(double.class)) {
				double conc_value = Array.getDouble(conc_array, conc_index);
				RealExpression expr = env.heap.array_load(symb_array,
						conc_index, conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else {
				Object conc_value = Array.get(conc_array, conc_index);
				if (conc_value instanceof String) {
					StringExpression expr = env.heap.array_load(symb_array,
							conc_index, (String) conc_value);
					return new ExpressionOrReferenceResult(expr);
				} else {
					Reference ref = env.heap.getReference(conc_value);
					return new ExpressionOrReferenceResult(ref);
				}
			}
		} catch (CodeUnderTestException e) {
			throw new RuntimeException(e);
		}
	}

	private ExpressionOrReferenceResult readField(FieldReference rhs,
			Scope scope) {

		if (rhs.getSource() != null) {
			/* instance field */
			return readInstanceField(rhs.getSource(), rhs.getField(), scope);
		} else {
			/* static field */
			return readStaticField(rhs.getField());
		}

	}

	private ExpressionOrReferenceResult readStaticField(Field field) {

		String owner = field.getDeclaringClass().getName().replace(".", "/");
		String name = field.getName();

		Class<?> fieldClazz = field.getType();

		try {

			if (fieldClazz.equals(int.class)) {
				int conc_value = field.getInt(null);
				Expression<?> expr = env.heap.getStaticField(owner, name,
						conc_value);
				return new ExpressionOrReferenceResult(expr);

			} else if (fieldClazz.equals(char.class)) {
				char conc_value = field.getChar(null);
				Expression<?> expr = env.heap.getStaticField(owner, name,
						conc_value);
				return new ExpressionOrReferenceResult(expr);

			} else if (fieldClazz.equals(long.class)) {
				long conc_value = field.getLong(null);
				Expression<?> expr = env.heap.getStaticField(owner, name,
						conc_value);
				return new ExpressionOrReferenceResult(expr);

			} else if (fieldClazz.equals(short.class)) {
				short conc_value = field.getShort(null);
				Expression<?> expr = env.heap.getStaticField(owner, name,
						conc_value);
				return new ExpressionOrReferenceResult(expr);

			} else if (fieldClazz.equals(byte.class)) {
				byte conc_value = field.getByte(null);
				Expression<?> expr = env.heap.getStaticField(owner, name,
						conc_value);
				return new ExpressionOrReferenceResult(expr);

			} else if (fieldClazz.equals(boolean.class)) {
				boolean conc_value = field.getBoolean(null);
				Expression<?> expr = env.heap.getStaticField(owner, name,
						conc_value ? 1 : 0);
				return new ExpressionOrReferenceResult(expr);

			} else if (fieldClazz.equals(float.class)) {
				float conc_value = field.getFloat(null);
				Expression<?> expr = env.heap.getStaticField(owner, name,
						conc_value);
				return new ExpressionOrReferenceResult(expr);

			} else if (fieldClazz.equals(double.class)) {
				double conc_value = field.getDouble(null);
				Expression<?> expr = env.heap.getStaticField(owner, name,
						conc_value);
				return new ExpressionOrReferenceResult(expr);

			} else {
				Object conc_value = field.get(null);
				if (conc_value instanceof String) {
					String string = (String) conc_value;
					Expression<?> expr = env.heap.getStaticField(owner, name,
							string);
					return new ExpressionOrReferenceResult(expr);
				} else {
					Reference ref = env.heap.getReference(conc_value);
					return new ExpressionOrReferenceResult(ref);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private ExpressionOrReferenceResult readInstanceField(
			VariableReference source, Field field, Scope scope) {

		String owner = field.getDeclaringClass().getName().replace(".", "/");
		String name = field.getName();

		Class<?> fieldClazz = field.getType();

		String source_name = source.getName();
		NonNullReference symb_receiver = (NonNullReference) symb_references
				.get(source_name);

		try {
			Object conc_receiver = source.getObject(scope);

			if (fieldClazz.equals(int.class)) {
				int conc_value = field.getInt(conc_receiver);
				Expression<?> expr = env.heap.getField(owner, name,
						conc_receiver, symb_receiver, conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (fieldClazz.equals(char.class)) {
				char conc_value = field.getChar(conc_receiver);
				Expression<?> expr = env.heap.getField(owner, name,
						conc_receiver, symb_receiver, conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (fieldClazz.equals(long.class)) {
				long conc_value = field.getLong(conc_receiver);
				Expression<?> expr = env.heap.getField(owner, name,
						conc_receiver, symb_receiver, conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (fieldClazz.equals(short.class)) {
				short conc_value = field.getShort(conc_receiver);
				Expression<?> expr = env.heap.getField(owner, name,
						conc_receiver, symb_receiver, conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (fieldClazz.equals(byte.class)) {
				byte conc_value = field.getByte(conc_receiver);
				Expression<?> expr = env.heap.getField(owner, name,
						conc_receiver, symb_receiver, conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (fieldClazz.equals(boolean.class)) {
				boolean conc_value = field.getBoolean(conc_receiver);
				Expression<?> expr = env.heap.getField(owner, name,
						conc_receiver, symb_receiver, conc_value ? 1 : 0);
				return new ExpressionOrReferenceResult(expr);
			} else if (fieldClazz.equals(float.class)) {
				float conc_value = field.getFloat(conc_receiver);
				Expression<?> expr = env.heap.getField(owner, name,
						conc_receiver, symb_receiver, conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else if (fieldClazz.equals(double.class)) {
				double conc_value = field.getDouble(conc_receiver);
				Expression<?> expr = env.heap.getField(owner, name,
						conc_receiver, symb_receiver, conc_value);
				return new ExpressionOrReferenceResult(expr);
			} else {
				Object conc_value = field.get(conc_receiver);
				if (conc_value instanceof String) {
					String string = (String) conc_value;
					Expression<?> expr = env.heap.getField(owner, name,
							conc_receiver, symb_receiver, string);
					return new ExpressionOrReferenceResult(expr);
				} else {
					Reference ref = env.heap.getReference(conc_value);
					return new ExpressionOrReferenceResult(ref);
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

	private void writeVariable(VariableReference lhs,
			ExpressionOrReferenceResult readResult) {
		String lhs_name = lhs.getName();
		if (readResult.isExpression()) {
			Expression<?> expr = readResult.getExpression();
			symb_expressions.put(lhs_name, expr);
		} else {
			Reference ref = readResult.getReference();
			symb_references.put(lhs_name, ref);
		}
	}

	private void writeArray(ArrayIndex lhs,
			ExpressionOrReferenceResult readResult, Scope scope) {

		if (readResult.isExpression()) {
			ArrayReference arrayReference = lhs.getArray();
			int conc_index = lhs.getArrayIndex();
			Expression<?> symb_value = readResult.getExpression();

			Object conc_array;
			try {
				conc_array = arrayReference.getObject(scope);
			} catch (CodeUnderTestException e) {
				throw new RuntimeException(e);
			}
			String array_name = arrayReference.getName();
			Reference symb_ref = symb_references.get(array_name);
			NonNullReference symb_array = (NonNullReference) symb_ref;
			env.heap.array_store(conc_array, symb_array, conc_index, symb_value);

		} else {
			/* ignore storing references (we use objects to find them) */
		}

	}

	private void writeField(FieldReference lhs,
			ExpressionOrReferenceResult readResult, Scope scope) {
		Field field = lhs.getField();
		String className = field.getDeclaringClass().getName()
				.replace(".", "/");
		String fieldName = field.getName();

		if (readResult.isExpression()) {
			Expression<?> symb_value = readResult.getExpression();

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
				NonNullReference symb_receiver = (NonNullReference) symb_references
						.get(source_name);
				env.heap.putField(className, fieldName, conc_receiver,
						symb_receiver, symb_value);
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
		Method method = statement.getMethod();

		String owner = method.getDeclaringClass().getName().replace(".", "/");
		String name = method.getName();
		String desc = Type.getMethodDescriptor(method);

		boolean needThis = statement.getCallee() != null;

		if (needThis) {
			VariableReference callee = statement.getCallee();
			String calleeVarName = callee.getName();

			Reference ref;
			if (symb_references.containsKey(calleeVarName)) {
				ref = symb_references.get(calleeVarName);
			} else {
				StringExpression strExpr = (StringExpression) symb_expressions
						.get(calleeVarName);
				ref = new StringReference(strExpr);
			}
			this.env.topFrame().operandStack.pushRef(ref);
		}

		List<VariableReference> parameters = statement.getParameterReferences();
		pushParameterList(parameters, scope);

		if (needThis) {
			VariableReference callee = statement.getCallee();
			Object receiver;
			try {
				if (parameters.size() < 3) {
					receiver = callee.getObject(scope);
					VM.INVOKEVIRTUAL(receiver, owner, name, desc);
				} else {
					VM.INVOKEVIRTUAL(owner, name, desc);
				}
			} catch (CodeUnderTestException e) {
				throw new RuntimeException(e);
			}
		} else {
			VM.INVOKESTATIC(owner, name, desc);
		}

		call_vm_caller_stack_params(needThis, parameters, scope);

	}

	private void call_vm_caller_stack_params(boolean needThis,
			List<VariableReference> parameters, Scope scope) {
		int calleeLocalsIndex = 0;
		if (needThis)
			calleeLocalsIndex++;

		for (int i = 0; i < parameters.size(); i++) {
			VariableReference p = parameters.get(i);
			calleeLocalsIndex += getSize(p.getType());
		}

		for (int i = parameters.size() - 1; i >= 0; i--) {
			VariableReference p = parameters.get(i);
			try {
				Object param_object = p.getObject(scope);
				calleeLocalsIndex -= getSize(p.getType());
				if (p.getType().equals(int.class)) {
					int intValue = ((Integer) param_object).intValue();
					VM.CALLER_STACK_PARAM(intValue, i, calleeLocalsIndex);
				} else if (p.getType().equals(char.class)) {
					char charValue = ((Character) param_object).charValue();
					VM.CALLER_STACK_PARAM(charValue, i, calleeLocalsIndex);
				} else if (p.getType().equals(byte.class)) {
					byte byteValue = ((Byte) param_object).byteValue();
					VM.CALLER_STACK_PARAM(byteValue, i, calleeLocalsIndex);
				} else if (p.getType().equals(boolean.class)) {
					boolean booleanValue = ((Boolean) param_object)
							.booleanValue();
					VM.CALLER_STACK_PARAM(booleanValue, i, calleeLocalsIndex);
				} else if (p.getType().equals(short.class)) {
					short shortValue = ((Short) param_object).shortValue();
					VM.CALLER_STACK_PARAM(shortValue, i, calleeLocalsIndex);
				} else if (p.getType().equals(long.class)) {
					long longValue = ((Long) param_object).longValue();
					VM.CALLER_STACK_PARAM(longValue, i, calleeLocalsIndex);
				} else if (p.getType().equals(float.class)) {
					float floatValue = ((Float) param_object).floatValue();
					VM.CALLER_STACK_PARAM(floatValue, i, calleeLocalsIndex);
				} else if (p.getType().equals(double.class)) {
					double doubleValue = ((Double) param_object).doubleValue();
					VM.CALLER_STACK_PARAM(doubleValue, i, calleeLocalsIndex);
				} else {
					VM.CALLER_STACK_PARAM(param_object, i, calleeLocalsIndex);
				}
			} catch (CodeUnderTestException e) {
				new RuntimeException(e);
			}
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

	private void pushParameterList(List<VariableReference> parameters,
			Scope scope) {
		for (VariableReference varRef : parameters) {
			ExpressionOrReferenceResult readResult = this.read(varRef, scope);

			if (readResult.isExpression()) {

				Expression<?> symb_expr = readResult.getExpression();

				if (symb_expr instanceof StringExpression) {
					StringExpression strExpr = (StringExpression) symb_expr;
					env.topFrame().operandStack.pushStringRef(strExpr);
				} else if (symb_expr instanceof RealExpression) {
					RealExpression realExpr = (RealExpression) symb_expr;
					if (isFp32(varRef.getVariableClass())) {
						env.topFrame().operandStack.pushFp32(realExpr);
					} else {
						env.topFrame().operandStack.pushFp64(realExpr);
					}
				} else if (symb_expr instanceof IntegerExpression) {
					IntegerExpression integerExpr = (IntegerExpression) symb_expr;
					if (isBv32(varRef.getVariableClass())) {
						env.topFrame().operandStack.pushBv32(integerExpr);
					} else {
						env.topFrame().operandStack.pushBv64(integerExpr);
					}

				}
			} else {
				Reference ref = readResult.getReference();
				env.topFrame().operandStack.pushRef(ref);
			}

		}
	}

	private static boolean isFp32(Class<?> clazz) {
		return clazz.equals(float.class);
	}

	private void before(StringPrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	private void before(IntPrimitiveStatement statement, Scope scope) {
		/* do nothing */
	}

	@Override
	public void afterStatement(StatementInterface s, Scope scope,
			Throwable exception) {

		if (exception != null) {
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
		ExpressionOrReferenceResult readResult;
		if (s.getSource() != null) {
			readResult = readInstanceField(s.getSource(), s.getField(), scope);
		} else {
			readResult = readStaticField(s.getField());
		}

		String lhs_name = s.getReturnValue().getName();

		if (readResult.isExpression()) {
			Expression<?> expr = readResult.getExpression();
			symb_expressions.put(lhs_name, expr);
		} else {
			Reference ref = readResult.getReference();
			symb_references.put(lhs_name, ref);
		}
	}

	private void after(ShortPrimitiveStatement statement, Scope scope) {
		short valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = IntegerVariableFactory
				.buildIntegerVariable(varRefName, valueOf, Short.MIN_VALUE,
						Short.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);
	}

	private void after(LongPrimitiveStatement statement, Scope scope) {
		long valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = IntegerVariableFactory
				.buildIntegerVariable(varRefName, valueOf, Long.MIN_VALUE,
						Long.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);
	}

	private void after(FloatPrimitiveStatement statement, Scope scope) {
		float valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		RealVariable integerVariable = RealVariableFactory.buildRealVariable(
				varRefName, valueOf, Float.MIN_VALUE, Float.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);
	}

	private void after(CharPrimitiveStatement statement, Scope scope) {
		char valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = IntegerVariableFactory
				.buildIntegerVariable(varRefName, valueOf, Character.MIN_VALUE,
						Character.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);
	}

	private void after(BytePrimitiveStatement statement, Scope scope) {
		byte valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = IntegerVariableFactory
				.buildIntegerVariable(varRefName, valueOf, Byte.MIN_VALUE,
						Byte.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);
	}

	private void after(BooleanPrimitiveStatement statement, Scope scope) {
		boolean valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = IntegerVariableFactory
				.buildIntegerVariable(varRefName, valueOf ? 1 : 0, 0, 1);
		symb_expressions.put(varRefName, integerVariable);
	}

	private void after(DoublePrimitiveStatement statement, Scope scope) {
		double valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		RealVariable integerVariable = RealVariableFactory.buildRealVariable(
				varRefName, valueOf, Double.MIN_VALUE, Double.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);

	}

	private void after(MethodStatement statement, Scope scope) {
		String owner = statement.getMethod().getDeclaringClass().getName()
				.replace(".", "/");
		String name = statement.getMethod().getName();
		String desc = Type.getMethodDescriptor(statement.getMethod());

		/* update DSE symbolic state */
		VariableReference varRef = statement.getReturnValue();
		try {
			if (varRef.getType().equals(void.class)) {
				VM.CALL_RESULT(owner, name, desc);
			} else if (varRef.getType().equals(int.class)) {
				Integer res = (Integer) varRef.getObject(scope);
				VM.CALL_RESULT(res.intValue(), owner, name, desc);
			} else if (varRef.getType().equals(boolean.class)) {
				Boolean res = (Boolean) varRef.getObject(scope);
				VM.CALL_RESULT(res.booleanValue(), owner, name, desc);
			} else if (varRef.getType().equals(double.class)) {
				Double res = (Double) varRef.getObject(scope);
				VM.CALL_RESULT(res.doubleValue(), owner, name, desc);
			} else if (varRef.getType().equals(float.class)) {
				Float res = (Float) varRef.getObject(scope);
				VM.CALL_RESULT(res.floatValue(), owner, name, desc);
			} else if (varRef.getType().equals(long.class)) {
				Long res = (Long) varRef.getObject(scope);
				VM.CALL_RESULT(res.longValue(), owner, name, desc);
			} else if (varRef.getType().equals(short.class)) {
				Short res = (Short) varRef.getObject(scope);
				VM.CALL_RESULT(res.shortValue(), owner, name, desc);
			} else if (varRef.getType().equals(byte.class)) {
				Byte res = (Byte) varRef.getObject(scope);
				VM.CALL_RESULT(res.byteValue(), owner, name, desc);
			} else if (varRef.getType().equals(char.class)) {
				Character res = (Character) varRef.getObject(scope);
				VM.CALL_RESULT(res.charValue(), owner, name, desc);
			} else if (varRef.getType().equals(void.class)) {
				VM.CALL_RESULT(owner, name, desc);
			} else {
				Object res = varRef.getObject(scope);
				VM.CALL_RESULT(res, owner, name, desc);
			}
		} catch (CodeUnderTestException e) {
			throw new RuntimeException(e);
		}

		if (varRef.getType().equals(void.class)) {
			return;
		}

		/* update our symbolic state */
		String varName = varRef.getName();
		Operand stackOperand = env.topFrame().operandStack.popOperand();

		if (stackOperand instanceof IntegerOperand) {
			IntegerOperand integerOperand = (IntegerOperand) stackOperand;
			IntegerExpression integerExpression = integerOperand
					.getIntegerExpression();
			symb_expressions.put(varName, integerExpression);

		} else if (stackOperand instanceof RealOperand) {
			RealOperand realOperand = (RealOperand) stackOperand;
			RealExpression realExpression = realOperand.getRealExpression();
			symb_expressions.put(varName, realExpression);

		} else if (stackOperand instanceof ReferenceOperand) {
			ReferenceOperand ref_operand = (ReferenceOperand) stackOperand;
			Reference ref = ref_operand.getReference();
			if (ref instanceof StringReference) {
				StringReference str_ref = (StringReference) ref;
				StringExpression str_expr = str_ref.getStringExpression();
				symb_expressions.put(varName, str_expr);
			} else {
				symb_references.put(varName, ref);
			}
		}

		// dispose all other arguments
		env.topFrame().operandStack.clearOperands();

	}

	private static boolean isBv32(Class<?> clazz) {
		return clazz.equals(int.class) || clazz.equals(char.class)
				|| clazz.equals(byte.class) || clazz.equals(short.class)
				|| clazz.equals(boolean.class);
	}

	private void after(StringPrimitiveStatement statement, Scope scope) {
		String valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		StringVariable stringVariable = StringVariableFactory
				.buildStringVariable(varRefName, valueOf, valueOf, valueOf);
		symb_expressions.put(varRefName, stringVariable);
	}

	private final Map<String, Expression<?>> symb_expressions = new THashMap<String, Expression<?>>();
	private final Map<String, Reference> symb_references = new THashMap<String, Reference>();

	private void after(IntPrimitiveStatement statement, Scope scope) {
		int valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = IntegerVariableFactory
				.buildIntegerVariable(varRefName, valueOf, Integer.MIN_VALUE,
						Integer.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);
	}

	@Override
	public void clear() {
		symb_expressions.clear();
		symb_references.clear();
	}

}
