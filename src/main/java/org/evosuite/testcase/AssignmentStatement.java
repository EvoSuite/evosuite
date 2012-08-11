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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.utils.Randomness;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * An assignment statement assigns a variable to another variable. This is only
 * used to assign to array indices
 * 
 * @author Gordon Fraser
 */
public class AssignmentStatement extends AbstractStatement {

	private static final long serialVersionUID = 2051431241124468349L;

	protected VariableReference parameter;

	/**
	 * <p>
	 * Constructor for AssignmentStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param var
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @param value
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public AssignmentStatement(TestCase tc, VariableReference var, VariableReference value) {
		super(tc, var);
		this.parameter = value;

		// TODO:
		// Assignment of an "unassignable" type may happen if we have no generator for
		// the target class, as we then attempt generating a superclass and try to case
		// down to the actual class
		//
	}

	/**
	 * <p>
	 * getValue
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public VariableReference getValue() {
		return this.parameter;
	}

	/** {@inheritDoc} */
	@Override
	public StatementInterface copy(TestCase newTestCase, int offset) {
		try {
			VariableReference newParam = parameter.copy(newTestCase, offset);
			VariableReference newTarget;

			// FIXXME: Return value should always be an existing variable
			//if (retval.getAdditionalVariableReference() != null)
			newTarget = retval.copy(newTestCase, offset);
			//else
			//	newTarget = retval.copy(newTestCase, offset);
			//newTarget = new VariableReferenceImpl(newTestCase, retval.getType());
			AssignmentStatement copy = new AssignmentStatement(newTestCase, newTarget,
			        newParam);
			// copy.assertions = copyAssertions(newTestCase, offset);

			//logger.info("Copy of statement is: " + copy.getCode());
			return copy;
		} catch (Exception e) {
			logger.info("Error cloning statement " + getCode());
			logger.info("In test: " + this.tc.toCode());
			logger.info("New test: " + newTestCase.toCode());
			e.printStackTrace();
			assert (false) : e.toString();
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Throwable execute(final Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {

		return super.exceptionHandler(new Executer() {

			@Override
			public void execute() throws InvocationTargetException,
			        IllegalArgumentException, IllegalAccessException,
			        InstantiationException {
				try {
					final Object value = parameter.getObject(scope);
					retval.setObject(scope, value);
					//} catch (CodeUnderTestException e) {
					//	throw CodeUnderTestException.throwException(e.getCause());
				} catch (IllegalArgumentException e) {
					// FIXXME: IllegalArgumentException may happen when we only have generators
					// for an abstract supertype and not the concrete type that we need!
					throw e;
				} catch (Throwable e) {
					throw new EvosuiteError(e);
				}
			}

			@Override
			public Set<Class<? extends Throwable>> throwableExceptions() {
				Set<Class<? extends Throwable>> t = new HashSet<Class<? extends Throwable>>();
				t.add(AssertionError.class);
				return t;
			}
		});

	}

	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> vars = new HashSet<VariableReference>();
		vars.add(retval);
		vars.add(parameter);

		if (retval.getAdditionalVariableReference() != null)
			vars.add(retval.getAdditionalVariableReference());
		if (parameter.getAdditionalVariableReference() != null)
			vars.add(parameter.getAdditionalVariableReference());
		vars.addAll(getAssertionReferences());

		return vars;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		if (parameter.equals(var1))
			parameter = var2;
		//else if (retval.equals(var1))
		//	retval = var2;
		else
			parameter.replaceAdditionalVariableReference(var1, var2);
		//else if (var1.equals(retval.getAdditionalVariableReference()))
		//	retval.setAdditionalVariableReference(var2);

	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + retval.hashCode()
		        + +((parameter == null) ? 0 : parameter.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssignmentStatement other = (AssignmentStatement) obj;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		if (retval == null) {
			if (other.retval != null)
				return false;
		} else if (!retval.equals(other.retval))
			return false;
		return true;
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
		parameter.loadBytecode(mg, locals);

		Class<?> clazz = parameter.getVariableClass();
		if (parameter.isPrimitive() && !retval.isPrimitive()) {
			mg.box(Type.getType(parameter.getVariableClass()));
			clazz = parameter.getGenericClass().getBoxedType();
		}

		if (!clazz.equals(retval.getVariableClass())) {
			mg.cast(org.objectweb.asm.Type.getType(clazz),
			        org.objectweb.asm.Type.getType(retval.getVariableClass()));
		}

		retval.storeBytecode(mg, locals);
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

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#isValid()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isValid() {
		assert (super.isValid());
		parameter.getStPosition();
		//if (!retval.getVariableClass().isAssignableFrom(parameter.getVariableClass())) {
		//	logger.error("Type mismatch: " + retval.getVariableClass() + " and "
		//	        + parameter.getVariableClass());
		//	logger.error(tc.toCode());
		//}

		//assert (retval.getVariableClass().isAssignableFrom(parameter.getVariableClass()));
		return true;
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

		AssignmentStatement other = (AssignmentStatement) s;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.same(other.parameter))
			return false;
		if (retval == null) {
			if (other.retval != null)
				return false;
		} else if (!retval.same(other.retval))
			return false;
		return true;
	}

	/**
	 * Retrieve the set of FieldReference and ArrayIndex variables that can
	 * serve as a replacement for retval
	 * 
	 * @return
	 */
	private Set<VariableReference> getSourceReplacements() {
		Set<VariableReference> variables = new HashSet<VariableReference>();
		for (int i = 0; i < retval.getStPosition() && i < tc.size(); i++) {
			VariableReference value = tc.getReturnValue(i);
			if (value == null)
				continue;
			if (value instanceof ArrayReference) {
				if (GenericClass.isAssignable(value.getComponentType(),
				                              parameter.getType())) {
					for (int index = 0; index < ((ArrayReference) value).getArrayLength(); index++) {
						variables.add(new ArrayIndex(tc, (ArrayReference) value, index));
					}
				}
			} else if (value instanceof ArrayIndex) {
				// Don't need to add this because array indices are created for array statement?
				if (value.isAssignableFrom(parameter.getType())) {
					variables.add(value);
				}
			} else {
				if (!value.isPrimitive() && !(value instanceof NullReference)) {
					// add fields of this object to list
					for (Field field : StaticTestCluster.getAccessibleFields(value.getVariableClass())) {
						FieldReference f = new FieldReference(tc, field, value);
						if (f.getDepth() <= 2) {
							if (f.isAssignableFrom(parameter.getType())) {
								variables.add(f);
							}
						}
					}
				}
			}
		}
		return variables;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#mutate(org.evosuite.testcase.TestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean mutate(TestCase test, AbstractTestFactory factory) {
		assert (isValid());

		// Either mutate parameter, or source
		if (Randomness.nextDouble() < 0.5) {
			Set<VariableReference> objects = getSourceReplacements();
			objects.remove(retval);
			objects.remove(parameter);

			if (!objects.isEmpty()) {
				VariableReference newRetVal = Randomness.choice(objects);
				retval = newRetVal;
				assert (isValid());
				return true;
			}

		} else {

			List<VariableReference> objects = test.getObjects(parameter.getType(),
			                                                  retval.getStPosition());
			objects.remove(retval);
			objects.remove(parameter);
			if (!objects.isEmpty()) {
				parameter = Randomness.choice(objects);
				assert (isValid());
				return true;
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public AccessibleObject getAccessibleObject() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssignmentStatement() {
		return true;
	}
}
