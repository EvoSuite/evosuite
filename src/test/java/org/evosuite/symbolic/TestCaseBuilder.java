package org.evosuite.symbolic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.evosuite.testcase.ArrayIndex;
import org.evosuite.testcase.ArrayReference;
import org.evosuite.testcase.ArrayStatement;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.BooleanPrimitiveStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.DoublePrimitiveStatement;
import org.evosuite.testcase.EnumPrimitiveStatement;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.FieldStatement;
import org.evosuite.testcase.IntPrimitiveStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.StringPrimitiveStatement;
import org.evosuite.testcase.VariableReference;

public class TestCaseBuilder {

	private DefaultTestCase tc = new DefaultTestCase();

	public VariableReference appendConstructor(Constructor<?> constructor,
			VariableReference... parameters) {

		List<VariableReference> parameter_list = Arrays.asList(parameters);
		ConstructorStatement constructorStmt = new ConstructorStatement(tc,
				constructor, constructor.getDeclaringClass(), parameter_list);
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
		MethodStatement methodStmt = new MethodStatement(tc, method, callee,
				method.getReturnType(), parameter_list);
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
			fieldReference = new FieldReference(tc, field);
		} else {
			fieldReference = new FieldReference(tc, field, receiver);
		}
		AssignmentStatement stmt = new AssignmentStatement(tc, fieldReference,
				value);
		tc.addStatement(stmt);
	}

	public VariableReference appendFieldStmt(VariableReference receiver,
			Field field) {
		FieldStatement stmt = new FieldStatement(tc, field, receiver,
				field.getGenericType());
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
}
