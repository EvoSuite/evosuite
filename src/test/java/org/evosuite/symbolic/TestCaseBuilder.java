package org.evosuite.symbolic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.evosuite.testcase.ArrayIndex;
import org.evosuite.testcase.ArrayReference;
import org.evosuite.testcase.ArrayStatement;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.BooleanPrimitiveStatement;
import org.evosuite.testcase.BytePrimitiveStatement;
import org.evosuite.testcase.CharPrimitiveStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.DoublePrimitiveStatement;
import org.evosuite.testcase.EnumPrimitiveStatement;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.FieldStatement;
import org.evosuite.testcase.FloatPrimitiveStatement;
import org.evosuite.testcase.IntPrimitiveStatement;
import org.evosuite.testcase.LongPrimitiveStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.ShortPrimitiveStatement;
import org.evosuite.testcase.StringPrimitiveStatement;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.GenericMethod;

public class TestCaseBuilder {

	private final DefaultTestCase tc = new DefaultTestCase();

	public VariableReference appendConstructor(Constructor<?> constructor,
			VariableReference... parameters) {

		List<VariableReference> parameter_list = Arrays.asList(parameters);
		ConstructorStatement constructorStmt = new ConstructorStatement(tc,
				new GenericConstructor(constructor,
						constructor.getDeclaringClass()), parameter_list);
		tc.addStatement(constructorStmt);

		return constructorStmt.getReturnValue();
	}

	public VariableReference appendIntPrimitive(int intValue) {
		IntPrimitiveStatement primitiveStmt = new IntPrimitiveStatement(tc,
				intValue);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	public String toCode() {
		return tc.toCode();
	}

	/**
	 * 
	 * @param callee
	 *            <code>null</code> for state methods
	 * @param method
	 * @param parameters
	 * @return <code>void reference</code> for void methods
	 */
	public VariableReference appendMethod(VariableReference callee,
			Method method, VariableReference... parameters) {
		List<VariableReference> parameter_list = Arrays.asList(parameters);
		MethodStatement methodStmt = null;
		if (callee == null) {
			methodStmt = new MethodStatement(tc, new GenericMethod(method,
					method.getDeclaringClass()), callee, parameter_list);
		} else {
			methodStmt = new MethodStatement(tc, new GenericMethod(method,
					callee.getType()), callee, parameter_list);
		}
		tc.addStatement(methodStmt);
		return methodStmt.getReturnValue();
	}

	public DefaultTestCase getDefaultTestCase() {
		return tc;
	}

	public VariableReference appendStringPrimitive(String string) {
		StringPrimitiveStatement primitiveStmt = new StringPrimitiveStatement(
				tc, string);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	public VariableReference appendBooleanPrimitive(boolean b) {
		BooleanPrimitiveStatement primitiveStmt = new BooleanPrimitiveStatement(
				tc, b);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	public VariableReference appendDoublePrimitive(double d) {
		DoublePrimitiveStatement primitiveStmt = new DoublePrimitiveStatement(
				tc, d);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	public void appendAssignment(VariableReference receiver, Field field,
			VariableReference value) {
		FieldReference fieldReference;

		if (receiver == null) {
			fieldReference = new FieldReference(tc, new GenericField(field,
					field.getDeclaringClass()));
		} else {
			fieldReference = new FieldReference(tc, new GenericField(field,
					receiver.getType()), receiver);
		}
		AssignmentStatement stmt = new AssignmentStatement(tc, fieldReference,
				value);
		tc.addStatement(stmt);
	}

	public VariableReference appendStaticFieldStmt(Field field) {
		Class<?> declaringClass = field.getDeclaringClass();
		final GenericField genericField = new GenericField(field,
				declaringClass);
		// static field (receiver is null)
		FieldStatement stmt = new FieldStatement(tc, genericField, null);
		tc.addStatement(stmt);
		return stmt.getReturnValue();
	}

	public VariableReference appendFieldStmt(VariableReference receiver,
			Field field) {

		if (receiver == null) {
			throw new NullPointerException(
					"Receiver object for a non-static field cannot be null");
		}
		FieldStatement stmt = new FieldStatement(tc, new GenericField(field,
				receiver.getType()), receiver);
		tc.addStatement(stmt);
		return stmt.getReturnValue();
	}

	public VariableReference appendNull(Type type) {
		NullStatement stmt = new NullStatement(tc, type);
		tc.addStatement(stmt);
		return stmt.getReturnValue();
	}

	public VariableReference appendEnumPrimitive(Enum<?> value) {
		EnumPrimitiveStatement primitiveStmt = new EnumPrimitiveStatement(tc,
				value);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	public ArrayReference appendArrayStmt(Type type, int... length) {
		ArrayStatement arrayStmt = new ArrayStatement(tc, type, length);
		tc.addStatement(arrayStmt);
		return (ArrayReference) arrayStmt.getReturnValue();
	}

	/**
	 * array[index] := var
	 * 
	 * @param array
	 * @param index
	 * @param var
	 */
	public void appendAssignment(ArrayReference array, int index,
			VariableReference var) {

		ArrayIndex arrayIndex = new ArrayIndex(tc, array, index);
		AssignmentStatement stmt = new AssignmentStatement(tc, arrayIndex, var);
		tc.addStatement(stmt);
	}

	/**
	 * var := array[index]
	 * 
	 * @param var
	 * @param array
	 * @param index
	 */
	public void appendAssignment(VariableReference var, ArrayReference array,
			int index) {
		ArrayIndex arrayIndex = new ArrayIndex(tc, array, index);
		AssignmentStatement stmt = new AssignmentStatement(tc, var, arrayIndex);
		tc.addStatement(stmt);
	}

	public VariableReference appendLongPrimitive(long l) {
		LongPrimitiveStatement primitiveStmt = new LongPrimitiveStatement(tc, l);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	public VariableReference appendFloatPrimitive(float f) {
		FloatPrimitiveStatement primitiveStmt = new FloatPrimitiveStatement(tc,
				f);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	public VariableReference appendShortPrimitive(short s) {
		ShortPrimitiveStatement primitiveStmt = new ShortPrimitiveStatement(tc,
				s);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	public VariableReference appendBytePrimitive(byte b) {
		BytePrimitiveStatement primitiveStmt = new BytePrimitiveStatement(tc, b);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	public VariableReference appendCharPrimitive(char c) {
		CharPrimitiveStatement primitiveStmt = new CharPrimitiveStatement(tc, c);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}
}
