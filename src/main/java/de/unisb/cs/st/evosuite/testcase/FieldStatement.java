/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Statement that accesses an instance/class field
 * 
 * @author Gordon Fraser
 * 
 */
public class FieldStatement extends AbstractStatement {

	private static final long serialVersionUID = -4944610139232763790L;

	transient Field field;
	VariableReference source;
	// VariableReference ret_val;

	private final String className;
	private final String fieldName;

	public FieldStatement(TestCase tc, Field field, VariableReference source,
	        java.lang.reflect.Type type) {
		super(tc, new VariableReferenceImpl(tc, type));
		this.field = field;
		this.className = field.getDeclaringClass().getName();
		this.fieldName = field.getName();
		this.source = source;
	}

	/**
	 * This constructor allows you to use an already existing VariableReference
	 * as retvar. This should only be done, iff an old statement is replaced
	 * with this statement. And already existing objects should in the future
	 * reference this object.
	 * 
	 * @param tc
	 * @param field
	 * @param source
	 * @param ret_var
	 */
	public FieldStatement(TestCase tc, Field field, VariableReference source,
	        VariableReference ret_var) {
		super(tc, ret_var);
		assert (tc.size() > ret_var.getStPosition()); //as an old statement should be replaced by this statement
		this.field = field;
		this.className = field.getDeclaringClass().getName();
		this.fieldName = field.getName();
		this.source = source;
	}

	private Object readResolve() {
		try {
			Class<?> clazz = Class.forName(className);
			this.field = clazz.getField(fieldName);
		} catch (ClassNotFoundException e) {
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;
	}

	public VariableReference getSource() {
		return source;
	}

	public void setSource(VariableReference source) {
		this.source = source;
	}

	public boolean isStatic() {
		return Modifier.isStatic(field.getModifiers());
	}

	@Override
	public String getCode(Throwable exception) {
		String cast_str = "  ";
		String result = "";
		if (!retval.getVariableClass().isAssignableFrom(field.getType())) {
			cast_str += "(" + retval.getSimpleClassName() + ")";
		}

		if (exception != null) {
			result = retval.getSimpleClassName() + " " + retval.getName() + " = null;\n";
			result += "try {\n  ";
		} else {
			result = retval.getSimpleClassName() + " ";
		}
		if (!Modifier.isStatic(field.getModifiers()))
			result += retval.getName() + " = " + cast_str + source.getName() + "."
			        + field.getName() + ";";
		else
			result += retval.getName() + " = " + cast_str
			        + field.getDeclaringClass().getSimpleName() + "." + field.getName()
			        + ";";
		if (exception != null) {
			Class<?> ex = exception.getClass();
			while (!Modifier.isPublic(ex.getModifiers()))
				ex = ex.getSuperclass();
			result += "\n} catch(" + ClassUtils.getShortClassName(ex) + " e) {}";
		}

		return result;
	}

	@Override
	public StatementInterface clone(TestCase newTestCase) {
		if (Modifier.isStatic(field.getModifiers()))
			return new FieldStatement(newTestCase, field, null, retval.getType());
		else {
			if (source instanceof ArrayIndex
			        && tc.getStatement(source.getStPosition()) instanceof ArrayStatement) {
				ArrayReference otherArray = (ArrayReference) newTestCase.getStatement(source.getStPosition()).getReturnValue(); //must be set as we only use this to clone whole testcases
				VariableReference newSource = new ArrayIndex(newTestCase, otherArray,
				        ((ArrayIndex) source).getArrayIndex());
				return new FieldStatement(newTestCase, field, newSource, retval.getType());
			} else {
				return new FieldStatement(
				        newTestCase,
				        field,
				        newTestCase.getStatement(source.getStPosition()).getReturnValue(),
				        retval.getType());
			}
		}
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {
		Object source_object = null;
		try {
			if (!Modifier.isStatic(field.getModifiers())) {
				source_object = scope.get(source);
				if (source_object == null) {
					scope.set(retval, null);
					return new NullPointerException();
				}

			}
			Object ret = field.get(source_object);
			scope.set(retval, ret);
		} catch (Throwable e) {
			if (e instanceof java.lang.reflect.InvocationTargetException) {
				e = e.getCause();
				logger.debug("Exception thrown in field: " + e);
			} else
				logger.debug("Exception thrown in field: " + e);
			exceptionThrown = e;
		}
		return exceptionThrown;
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		if (!Modifier.isStatic(field.getModifiers())) {
			references.add(source);
			if (source instanceof ArrayIndex)
				references.add(((ArrayIndex) source).getArray());
		}
		return references;

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#replace(de.unisb.cs.st.evosuite.testcase.VariableReference, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		if (!Modifier.isStatic(field.getModifiers())) {
			if (source.equals(var1))
				source = var2;
			//else if (source instanceof ArrayIndex && var2 instanceof ArrayIndex)
			// TODO: FIXXME
		}
	}

	@Override
	public boolean equals(Object s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		FieldStatement fs = (FieldStatement) s;
		if (!Modifier.isStatic(field.getModifiers()))
			return source.equals(fs.source) && retval.equals(fs.retval)
			        && field.equals(fs.field);
		else
			return retval.equals(fs.retval) && field.equals(fs.field);
	}

	@Override
	public int hashCode() {
		final int prime = 51;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	public Field getField() {
		return field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getBytecode(org.objectweb.
	 * asm.commons.GeneratorAdapter)
	 */
	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        Throwable exception) {

		Label start = mg.newLabel();
		Label end = mg.newLabel();

		// if(exception != null)
		mg.mark(start);

		if (!isStatic()) {
			source.loadBytecode(mg, locals);
		}
		if (isStatic())
			mg.getStatic(Type.getType(field.getDeclaringClass()), field.getName(),
			             Type.getType(field.getType()));
		else {
			if (!source.getVariableClass().isInterface()) {
				mg.getField(Type.getType(source.getVariableClass()), field.getName(),
				            Type.getType(field.getType()));
			} else {
				mg.getField(Type.getType(field.getDeclaringClass()), field.getName(),
				            Type.getType(field.getType()));
			}
		}

		retval.storeBytecode(mg, locals);

		// if(exception != null) {
		mg.mark(end);
		Label l = mg.newLabel();
		mg.goTo(l);
		// mg.catchException(start, end, Type.getType(exception.getClass()));
		mg.catchException(start, end, Type.getType(Throwable.class));
		mg.pop(); // Pop exception from stack
		if (!retval.isVoid()) {
			Class<?> clazz = retval.getVariableClass();
			if (clazz.equals(boolean.class))
				mg.push(false);
			else if (clazz.equals(char.class))
				mg.push(0);
			else if (clazz.equals(int.class))
				mg.push(0);
			else if (clazz.equals(short.class))
				mg.push(0);
			else if (clazz.equals(long.class))
				mg.push(0L);
			else if (clazz.equals(float.class))
				mg.push(0.0F);
			else if (clazz.equals(double.class))
				mg.push(0.0);
			else if (clazz.equals(byte.class))
				mg.push(0);
			else if (clazz.equals(String.class))
				mg.push("");
			else
				mg.visitInsn(Opcodes.ACONST_NULL);

			retval.storeBytecode(mg, locals);
		}
		mg.mark(l);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getUniqueVariableReferences()
	 */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return new ArrayList<VariableReference>(getVariableReferences());
	}

	@Override
	public boolean same(StatementInterface s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		FieldStatement fs = (FieldStatement) s;
		if (!Modifier.isStatic(field.getModifiers()))
			return source.same(fs.source) && retval.same(fs.retval)
			        && field.equals(fs.field);
		else
			return retval.same(fs.retval) && field.equals(fs.field);
	}

}
