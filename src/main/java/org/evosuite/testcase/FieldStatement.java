/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Statement that accesses an instance/class field
 * 
 * @author Gordon Fraser
 */
public class FieldStatement extends AbstractStatement {

	private static final long serialVersionUID = -4944610139232763790L;

	protected GenericField field;
	protected VariableReference source;

	/**
	 * <p>
	 * Constructor for FieldStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param field
	 *            a {@link java.lang.reflect.Field} object.
	 * @param source
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @param type
	 *            a {@link java.lang.reflect.Type} object.
	 */
	public FieldStatement(TestCase tc, GenericField field, VariableReference source) {
		super(tc, new VariableReferenceImpl(tc, field.getFieldType()));
		this.field = field;
		this.source = source;
		if (retval.getComponentType() != null) {
			retval = new ArrayReference(tc, retval.getGenericClass(), 0);
		}
	}

	/**
	 * This constructor allows you to use an already existing VariableReference
	 * as retvar. This should only be done, iff an old statement is replaced
	 * with this statement. And already existing objects should in the future
	 * reference this object.
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param field
	 *            a {@link java.lang.reflect.Field} object.
	 * @param source
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @param ret_var
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public FieldStatement(TestCase tc, GenericField field, VariableReference source,
	        VariableReference ret_var) {
		super(tc, ret_var);
		assert (tc.size() > ret_var.getStPosition()); //as an old statement should be replaced by this statement
		this.field = field;
		this.source = source;
	}

	/**
	 * <p>
	 * Getter for the field <code>source</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public VariableReference getSource() {
		return source;
	}

	/**
	 * Try to replace source of field with all possible choices
	 * 
	 * @param test
	 * @param statement
	 * @param objective
	 */
	/* (non-Javadoc)
	 * @see org.evosuite.testcase.AbstractStatement#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.TestFactory)
	 */
	@Override
	public boolean mutate(TestCase test, TestFactory factory) {

		if (Randomness.nextDouble() >= Properties.P_CHANGE_PARAMETER)
			return false;

		if (!isStatic()) {
			VariableReference source = getSource();
			List<VariableReference> objects = test.getObjects(source.getType(),
			                                                  getPosition());
			objects.remove(source);

			if (!objects.isEmpty()) {
				setSource(Randomness.choice(objects));
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>
	 * Setter for the field <code>source</code>.
	 * </p>
	 * 
	 * @param source
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public void setSource(VariableReference source) {
		this.source = source;
	}

	@Override
	public boolean isAccessible() {
		if(!field.isAccessible())
			return false;
		
		return super.isAccessible();
	}
	
	/**
	 * <p>
	 * isStatic
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isStatic() {
		return field.isStatic();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getNumParameters()
	 */
	@Override
	public int getNumParameters() {
		if (isStatic())
			return 0;
		else
			return 1;
	}

	/** {@inheritDoc} */
	@Override
	public StatementInterface copy(TestCase newTestCase, int offset) {
		if (field.isStatic()) {
			FieldStatement s = new FieldStatement(newTestCase, field.copy(), null);
			// s.assertions = copyAssertions(newTestCase, offset);
			return s;
		} else {
			VariableReference newSource = source.copy(newTestCase, offset);
			FieldStatement s = new FieldStatement(newTestCase, field.copy(), newSource);
			// s.assertions = copyAssertions(newTestCase, offset);
			return s;
		}

	}

	/** {@inheritDoc} */
	@Override
	public Throwable execute(final Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {
		Throwable exceptionThrown = null;

		try {
			return super.exceptionHandler(new Executer() {

				@Override
				public void execute() throws InvocationTargetException,
				        IllegalArgumentException, IllegalAccessException,
				        InstantiationException, CodeUnderTestException {
					Object source_object;
					try {
						source_object = (field.isStatic()) ? null
						        : source.getObject(scope);

						if (!field.isStatic() && source_object == null) {
							retval.setObject(scope, null);
							throw new CodeUnderTestException(new NullPointerException());
						}
						//} catch (CodeUnderTestException e) {
						//	throw CodeUnderTestException.throwException(e.getCause());
					} catch (CodeUnderTestException e) {
						throw e;
					} catch (Throwable e) {
						throw new EvosuiteError(e);
					}

					Object ret = field.getField().get(source_object);
					if(!retval.isAssignableFrom(ret.getClass())) {
						throw new CodeUnderTestException(new ClassCastException());
					}
					try {
						// FIXXME: isAssignableFrom int <- Integer does not return true 
						//assert(ret==null || retval.getVariableClass().isAssignableFrom(ret.getClass())) : "we want an " + retval.getVariableClass() + " but got an " + ret.getClass();
						retval.setObject(scope, ret);
					} catch (CodeUnderTestException e) {
						throw e;
						// throw CodeUnderTestException.throwException(e);
					} catch (Throwable e) {
						throw new EvosuiteError(e);
					}
				}

				@Override
				public Set<Class<? extends Throwable>> throwableExceptions() {
					Set<Class<? extends Throwable>> t = new HashSet<Class<? extends Throwable>>();
					t.add(InvocationTargetException.class);
					return t;
				}
			});

		} catch (InvocationTargetException e) {
			exceptionThrown = e.getCause();
		}
		return exceptionThrown;
	}

	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new LinkedHashSet<VariableReference>();
		references.add(retval);
		if (!isStatic()) {
			references.add(source);
			if (source.getAdditionalVariableReference() != null)
				references.add(source.getAdditionalVariableReference());
		}
		return references;

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		if (!field.isStatic()) {
			if (source.equals(var1))
				source = var2;
			else
				source.replaceAdditionalVariableReference(var1, var2);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		FieldStatement fs = (FieldStatement) s;
		if (!field.isStatic())
			return source.equals(fs.source) && retval.equals(fs.retval)
			        && field.equals(fs.field);
		else
			return retval.equals(fs.retval) && field.equals(fs.field);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 51;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	/**
	 * <p>
	 * Getter for the field <code>field</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.reflect.Field} object.
	 */
	public GenericField getField() {
		return field;
	}

	/**
	 * <p>
	 * Setter for the field <code>field</code>.
	 * </p>
	 * 
	 * @param field
	 *            a {@link java.lang.reflect.Field} object.
	 */
	public void setField(GenericField field) {
		// assert (this.field.getType().equals(field.getType()));
		this.field = field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.Statement#getBytecode(org.objectweb.
	 * asm.commons.GeneratorAdapter)
	 */
	/** {@inheritDoc} */
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
			mg.getStatic(Type.getType(field.getField().getDeclaringClass()),
			             field.getName(), Type.getType(field.getField().getType()));
		else {
			if (!source.getVariableClass().isInterface()) {
				mg.getField(Type.getType(source.getVariableClass()), field.getName(),
				            Type.getType(field.getField().getType()));
			} else {
				mg.getField(Type.getType(field.getField().getDeclaringClass()),
				            field.getName(), Type.getType(field.getField().getType()));
			}
		}

		if (!retval.getVariableClass().equals(field.getField().getType())) {
			if (!retval.getVariableClass().isPrimitive()) {
				if (field.getField().getType().isPrimitive()) {
					mg.box(Type.getType(field.getField().getType()));
				}
				mg.checkCast(Type.getType(retval.getVariableClass()));
			} else {
				mg.cast(Type.getType(field.getField().getType()),
				        Type.getType(retval.getVariableClass()));
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
	 * org.evosuite.testcase.Statement#getUniqueVariableReferences()
	 */
	/** {@inheritDoc} */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return new ArrayList<VariableReference>(getVariableReferences());
	}

	/** {@inheritDoc} */
	@Override
	public boolean same(StatementInterface s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		FieldStatement fs = (FieldStatement) s;
		if (!field.isStatic())
			return source.same(fs.source) && retval.same(fs.retval)
			        && field.equals(fs.field);
		else
			return retval.same(fs.retval) && field.equals(fs.field);
	}

	/** {@inheritDoc} */
	@Override
	public GenericField getAccessibleObject() {
		return field;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssignmentStatement() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#changeClassLoader(java.lang.ClassLoader)
	 */
	/** {@inheritDoc} */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		field.changeClassLoader(loader);
		super.changeClassLoader(loader);
	}
}
