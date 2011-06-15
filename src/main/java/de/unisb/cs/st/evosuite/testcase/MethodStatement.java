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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class MethodStatement extends AbstractStatement {

	private static final long serialVersionUID = 6134126797102983073L;

	private final Method method;

	VariableReference callee;

	public List<VariableReference> parameters;

	public MethodStatement(TestCase tc, Method method, VariableReference callee, java.lang.reflect.Type type,
			List<VariableReference> parameters) {
		super(tc, new VariableReferenceImpl(tc, type));
		assert (Modifier.isStatic(method.getModifiers()) || (callee != null));
		assert (parameters != null);
		assert (method.getParameterTypes().length == parameters.size());
		this.method = method;
		this.callee = callee;
		this.parameters = parameters;
	}

	/**
	 * This constructor allows you to use an already existing VariableReference
	 * as retvar. This should only be done, iff an old statement is replaced
	 * with this statement. And already existing objects should in the future
	 * reference this object.
	 * 
	 * @param tc
	 * @param method
	 * @param callee
	 * @param retvar
	 * @param parameters
	 */
	public MethodStatement(TestCase tc, Method method, VariableReference callee, VariableReference retvar,
			List<VariableReference> parameters) {
		super(tc, retvar);
		assert (tc.size() > retvar.getStPosition()); // as an old statement
														// should be replaced by
														// this statement
		assert (Modifier.isStatic(method.getModifiers()) || (callee != null));
		assert (parameters != null);
		assert (method.getParameterTypes().length == parameters.size());
		this.method = method;
		this.callee = callee;
		this.parameters = parameters;
	}

	@Override
	public StatementInterface clone(TestCase newTestCase) {
		ArrayList<VariableReference> new_params = new ArrayList<VariableReference>();
		for (VariableReference r : parameters) {
			if (r instanceof ConstantValue) {
				new_params.add(((ConstantValue) r).clone(newTestCase));
			} else {
				new_params.add(newTestCase.getStatement(r.getStPosition()).getReturnValue());
			}
		}

		MethodStatement m;
		if (Modifier.isStatic(method.getModifiers())) {
			// FIXXME: If callee is an array index, this will return an invalid
			// copy of the cloned variable!
			m = new MethodStatement(newTestCase, method, null, retval.getType(), new_params);
		} else {
			m = new MethodStatement(newTestCase, method, newTestCase.getStatement(callee.getStPosition())
					.getReturnValue(), retval.getType(), new_params);

		}

		m.assertions = cloneAssertions(newTestCase);

		return m;
	}

	@Override
	public boolean equals(Object s) {
		if (this == s) {
			return true;
		}
		if (s == null) {
			return false;
		}
		if (getClass() != s.getClass()) {
			return false;
		}

		MethodStatement ms = (MethodStatement) s;
		if (ms.parameters.size() != parameters.size()) {
			return false;
		}

		if (!this.method.equals(ms.method)) {
			return false;
		}

		for (int i = 0; i < parameters.size(); i++) {
			if (!parameters.get(i).equals(ms.parameters.get(i))) {
				return false;
			}
		}

		if (!retval.equals(ms.retval)) {
			return false;
		}

		if (((callee == null) && (ms.callee != null)) || ((callee != null) && (ms.callee == null))) {
			return false;
		} else {
			if (callee == null) {
				return true;
			} else {
				return (callee.equals(ms.callee));
			}
		}
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException {
		logger.trace("Executing method " + method.getName());
		exceptionThrown = null;
		Object[] inputs = new Object[parameters.size()];
		PrintStream old_out = System.out;
		PrintStream old_err = System.err;
		System.setOut(out);
		System.setErr(out);

		try {
			for (int i = 0; i < parameters.size(); i++) {
				inputs[i] = scope.get(parameters.get(i));
			}

			Object callee_object = null;
			if (!Modifier.isStatic(method.getModifiers())) {
				callee_object = scope.get(callee);
			}

			Object ret = this.method.invoke(callee_object, inputs);
			scope.set(retval, ret);
		} catch (Throwable e) {
			if (e instanceof java.lang.reflect.InvocationTargetException) {
				e = e.getCause();
			}
			if (e instanceof EvosuiteError) {
				throw (EvosuiteError) e;
			}
			logger.debug("Exception thrown in method " + method.getName() + ": " + e);
			exceptionThrown = e;
		} finally {
			System.setOut(old_out);
			System.setErr(old_err);
		}
		return exceptionThrown;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getBytecode(org.objectweb.
	 * asm.commons.GeneratorAdapter)
	 */
	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals, Throwable exception) {
		Label start = mg.newLabel();
		Label end = mg.newLabel();

		// if(exception != null)
		mg.mark(start);

		if (!isStatic()) {
			callee.loadBytecode(mg, locals);
		}
		int num = 0;
		for (VariableReference parameter : parameters) {
			parameter.loadBytecode(mg, locals);
			if (method.getParameterTypes()[num].isPrimitive()) {
				if (!method.getParameterTypes()[num].equals(parameter.getVariableClass())) {
					logger.debug("Types don't match - casting!");
					mg.cast(Type.getType(parameter.getVariableClass()), Type.getType(method.getParameterTypes()[num]));
				}
			}
			num++;
		}
		logger.debug("Invoking method");
		// if(exception != null) {
		//
		// mg.visitTryCatchBlock(start, end, handler,
		// exception.getClass().getName().replace('.', '/'));
		// }
		if (isStatic()) {
			mg.invokeStatic(Type.getType(method.getDeclaringClass()),
					org.objectweb.asm.commons.Method.getMethod(method));
		} else {
			if (!callee.getVariableClass().isInterface()) {
				mg.invokeVirtual(Type.getType(callee.getVariableClass()),
						org.objectweb.asm.commons.Method.getMethod(method));
			} else {
				mg.invokeInterface(Type.getType(callee.getVariableClass()),
						org.objectweb.asm.commons.Method.getMethod(method));
			}
		}

		if (!retval.isVoid()) {
			retval.storeBytecode(mg, locals);
		}

		// if(exception != null) {
		mg.mark(end);
		Label l = mg.newLabel();
		mg.goTo(l);
		// mg.catchException(start, end,
		// Type.getType(getExceptionClass(exception)));
		mg.catchException(start, end, Type.getType(Throwable.class));
		mg.pop(); // Pop exception from stack
		if (!retval.isVoid()) {
			Class<?> clazz = retval.getVariableClass();
			if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
				mg.push(false);
			} else if (clazz.equals(char.class)) {
				mg.push(0);
			} else if (clazz.equals(int.class)) {
				mg.push(0);
			} else if (clazz.equals(short.class)) {
				mg.push(0);
			} else if (clazz.equals(long.class)) {
				mg.push(0L);
			} else if (clazz.equals(float.class)) {
				mg.push(0.0F);
			} else if (clazz.equals(double.class)) {
				mg.push(0.0);
			} else if (clazz.equals(byte.class)) {
				mg.push(0);
			} else if (clazz.equals(String.class)) {
				mg.push("");
			} else {
				mg.visitInsn(Opcodes.ACONST_NULL);
			}

			retval.storeBytecode(mg, locals);
		}
		mg.mark(l);
		// }
	}

	public VariableReference getCallee() {
		return callee;
	}

	@Override
	public String getCode(Throwable exception) {

		String result = "";

		if ((exception != null) && isValidException(exception)) {
			result += "// Undeclared exception!\n";
		}

		if (retval.getType() != Void.TYPE) {
			if (exception != null) {
				result += retval.getSimpleClassName() + " " + retval.getName() + " = " + retval.getDefaultValueString()
						+ ";\n";
			} else {
				result += retval.getSimpleClassName() + " ";
			}
		}
		if (exception != null) {
			result += "try {\n  ";
		}

		String parameter_string = "";
		if (!parameters.isEmpty()) {
			if (!method.getParameterTypes()[0].equals(parameters.get(0).getVariableClass())
					&& (parameters.get(0) instanceof ArrayIndex)) {
				parameter_string += "(" + method.getParameterTypes()[0].getSimpleName() + ")";
			}
			parameter_string += parameters.get(0).getName();
			for (int i = 1; i < parameters.size(); i++) {
				if (!method.getParameterTypes()[i].equals(parameters.get(i).getVariableClass())
						&& (parameters.get(i) instanceof ArrayIndex)) {
					parameter_string += "(" + method.getParameterTypes()[i].getSimpleName() + ")";
				}
				parameter_string += ", " + parameters.get(i).getName();
			}
		}

		String callee_str = "";
		if (!retval.getVariableClass().isAssignableFrom(method.getReturnType())) {
			callee_str = "(" + retval.getSimpleClassName() + ")";
		}

		if (Modifier.isStatic(method.getModifiers())) {
			callee_str += method.getDeclaringClass().getSimpleName();
		} else {
			callee_str += callee.getName();
		}

		if (retval.getType() == Void.TYPE) {
			result += callee_str + "." + method.getName() + "(" + parameter_string + ");";
		} else {
			result += retval.getName() + " = " + callee_str + "." + method.getName() + "(" + parameter_string + ");";
		}

		if (exception != null) {
			Class<?> ex = exception.getClass();
			while (!Modifier.isPublic(ex.getModifiers())) {
				ex = ex.getSuperclass();
			}
			result += "\n} catch(" + ClassUtils.getShortClassName(ex) + " e) {}";
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.testcase.Statement#getDeclaredExceptions()
	 */
	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		Set<Class<?>> ex = super.getDeclaredExceptions();
		for (Class<?> t : method.getExceptionTypes()) {
			ex.add(t);
		}
		return ex;
	}

	public Method getMethod() {
		return method;
	}

	public List<VariableReference> getParameterReferences() {
		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getUniqueVariableReferences()
	 */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		List<VariableReference> references = new ArrayList<VariableReference>();
		references.add(retval);
		if (isInstanceMethod()) {
			references.add(callee);
			if (callee instanceof ArrayIndex) {
				references.add(((ArrayIndex) callee).getArray());
			}
		}
		references.addAll(parameters);
		for (VariableReference param : parameters) {
			if (param instanceof ArrayIndex) {
				references.add(((ArrayIndex) param).getArray());
			}
		}
		return references;
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		if (isInstanceMethod()) {
			references.add(callee);
			if (callee instanceof ArrayIndex) {
				references.add(((ArrayIndex) callee).getArray());
			}
		}
		references.addAll(parameters);
		for (VariableReference param : parameters) {
			if (param instanceof ArrayIndex) {
				references.add(((ArrayIndex) param).getArray());
			}
		}
		return references;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callee == null) ? 0 : callee.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	public boolean isStatic() {
		return Modifier.isStatic(method.getModifiers());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#isValid()
	 */
	@Override
	public boolean isValid() {
		assert (super.isValid());
		for (VariableReference v : parameters) {
			v.getStPosition();
		}
		if (callee != null) {
			callee.getStPosition();
		}
		return true;
	}

	@Override
	public boolean isValidException(Throwable t) {
		return Arrays.asList(method.getExceptionTypes()).contains(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.StatementInterface#replace(de.unisb.
	 * cs.st.evosuite.testcase.VariableReference,
	 * de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		if (isInstanceMethod()) {
			if (callee.equals(var1)) {
				callee = var2;
				// if (callee instanceof ArrayIndex)
				// references.add(((ArrayIndex) callee).getArray()); // TODO:
				// FIXXME
			}
		}
		for (int i = 0; i < parameters.size(); i++) {

			if (parameters.get(i).equals(var1)) {
				parameters.set(i, var2);
				// if (param instanceof ArrayIndex)
				// references.add(((ArrayIndex) param).getArray()); // TODO:
				// FIXXME
			}
		}
	}

	@Override
	public boolean same(StatementInterface s) {
		if (this == s) {
			return true;
		}
		if (s == null) {
			return false;
		}
		if (getClass() != s.getClass()) {
			return false;
		}

		MethodStatement ms = (MethodStatement) s;
		if (ms.parameters.size() != parameters.size()) {
			return false;
		}

		for (int i = 0; i < parameters.size(); i++) {
			if (!parameters.get(i).same(ms.parameters.get(i))) {
				return false;
			}
		}

		if (!this.method.equals(ms.method)) {
			return false;
		}

		if (!retval.same(ms.retval)) {
			return false;
		}

		if (((callee == null) && (ms.callee != null)) || ((callee != null) && (ms.callee == null))) {
			return false;
		} else {
			if (callee == null) {
				return true;
			} else {
				return (callee.same(ms.callee));
			}
		}
	}

	public void setCallee(VariableReference callee) {
		this.callee = callee;
	}

	@Override
	public String toString() {
		return method.getName() + Type.getMethodDescriptor(method);
	}

	private boolean isInstanceMethod() {
		return !Modifier.isStatic(method.getModifiers());
	}

}
