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
package org.evosuite.symbolic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.runtime.testdata.EvoSuiteFile;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.environment.FileNamePrimitiveStatement;
import org.evosuite.testcase.statements.numeric.BooleanPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.BytePrimitiveStatement;
import org.evosuite.testcase.statements.numeric.CharPrimitiveStatement;
import org.evosuite.testcase.statements.ClassPrimitiveStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.numeric.DoublePrimitiveStatement;
import org.evosuite.testcase.statements.EnumPrimitiveStatement;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.numeric.FloatPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.LongPrimitiveStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.testcase.statements.numeric.ShortPrimitiveStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericField;
import org.evosuite.utils.generic.GenericMethod;

public class TestCaseBuilder {

	private final DefaultTestCase tc = new DefaultTestCase();
	private final Map<Integer,Throwable> exceptions = new HashMap<Integer,Throwable>();

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

	public VariableReference appendClassPrimitive(Class<?> value) {
		ClassPrimitiveStatement stmt = new ClassPrimitiveStatement(tc, value);
		tc.addStatement(stmt);
		return stmt.getReturnValue();
	}

	public VariableReference appendFileNamePrimitive(EvoSuiteFile evosuiteFile) {
		FileNamePrimitiveStatement stmt = new FileNamePrimitiveStatement(tc, evosuiteFile);
		tc.addStatement(stmt);
		return stmt.getReturnValue();
	}

	
	/**
	 * x.f1 := y.f2
	 *  
	 * @param var
	 * @param array
	 * @param index
	 */
	public void appendAssignment(VariableReference receiver, Field field,
			VariableReference src, Field fieldSrc) {
		FieldReference dstFieldReference;
		if (receiver == null) {
			dstFieldReference = new FieldReference(tc, new GenericField(field,
					field.getDeclaringClass()));
		} else {
			dstFieldReference = new FieldReference(tc, new GenericField(field,
					receiver.getType()), receiver);
		}
		
		FieldReference srcFieldReference;
		if (src == null) {
			srcFieldReference = new FieldReference(tc, new GenericField(fieldSrc,
					fieldSrc.getDeclaringClass()));
		} else {
			srcFieldReference = new FieldReference(tc, new GenericField(fieldSrc,
					src.getType()), src);
		}
		AssignmentStatement stmt = new AssignmentStatement(tc, dstFieldReference,
				srcFieldReference);
		tc.addStatement(stmt);
	}



	public void addException(Throwable exception) {
		int currentPos = this.tc.size()-1;
		if (currentPos<0)
			throw new IllegalStateException("Cannot add exception to empty test case");
		
		if (exceptions.containsKey(currentPos)) {
			throw new IllegalStateException("Statement already contains an exception!");
		}
		
		exceptions.put(currentPos, exception);
	}
}
