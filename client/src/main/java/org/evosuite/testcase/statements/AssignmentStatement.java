/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testcase.statements;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.evosuite.Properties;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.setup.TestClusterUtils;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericField;
import org.evosuite.utils.Randomness;

/**
 * An assignment statement assigns values to variables. This is only used to assign to array indices
 * or to public member variables of objects, e.g., {@code var1[0] = 42} or {@code var2.maxSize =
 * 1337}. Assignment statements do not define new values.
 *
 * @author Gordon Fraser
 */
public class AssignmentStatement extends AbstractStatement {

	private static final long serialVersionUID = 2051431241124468349L;

	protected VariableReference value;

	/**
	 * <p>
	 * Constructor for AssignmentStatement.
	 * </p>
	 *
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param var
	 *            a {@link org.evosuite.testcase.variable.VariableReference} object.
	 * @param value
	 *            a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public AssignmentStatement(TestCase tc, VariableReference var, VariableReference value) {
		super(tc, var);
		this.value = value;

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
	 * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public VariableReference getValue() {
		return this.value;
	}

	/** {@inheritDoc} */
	@Override
	public AbstractStatement copy(TestCase newTestCase, int offset) {
		try {
			VariableReference newParam = value.copy(newTestCase, offset);
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
			        InstantiationException, CodeUnderTestException {
				try {
					final Object value = AssignmentStatement.this.value.getObject(scope);

					if (checkNullDereference(scope)) {
						throw new CodeUnderTestException(new NullPointerException());
					}

					retval.setObject(scope, value);
					//} catch (CodeUnderTestException e) {
					//	throw CodeUnderTestException.throwException(e.getCause());
				} catch (IllegalArgumentException e) {
					logger.error("Error assigning value of type "
					        + value.getSimpleClassName() + " defined at statement "
					        + tc.getStatement(value.getStPosition()).getCode()
					        + ", assignment statement: "
					        + tc.getStatement(retval.getStPosition()).getCode()
					        + "; SUT=" + Properties.TARGET_CLASS);

					// FIXXME: IllegalArgumentException may happen when we only have generators
					// for an abstract supertype and not the concrete type that we need!
					throw e;
				} catch (CodeUnderTestException e) {
					throw e;
				} catch (Throwable e) {
					throw new EvosuiteError(e);
				}
			}

			/**
			 * Returns true of the retval of the assignment is a field reference (i.e. expr.f)
			 * such that expr==null
			 *
			 * @param scope
			 * @return
			 * @throws CodeUnderTestException (cause is NullPointerException)
			 */
			private boolean checkNullDereference(final Scope scope) throws CodeUnderTestException {
				if (retval instanceof FieldReference) {
					FieldReference fieldRef = (FieldReference)retval;

					if (fieldRef.getField().isStatic()) {
						return false;
					}

					VariableReference source = fieldRef.getSource();
					Object sourceValue = source.getObject(scope);
					return sourceValue == null;
				}
				return false;
			}

			@Override
			public Set<Class<? extends Throwable>> throwableExceptions() {
				Set<Class<? extends Throwable>> t = new LinkedHashSet<Class<? extends Throwable>>();
				t.add(AssertionError.class);
				return t;
			}
		});

	}

	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> vars = new LinkedHashSet<VariableReference>();
		vars.add(retval);
		vars.add(value);

		if (retval.getAdditionalVariableReference() != null)
			vars.add(retval.getAdditionalVariableReference());
		if (value.getAdditionalVariableReference() != null)
			vars.add(value.getAdditionalVariableReference());
		vars.addAll(getAssertionReferences());

		return vars;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if (value.equals(oldVar))
			value = newVar;
		//else if (retval.equals(var1))
		//	retval = var2;
		else
			value.replaceAdditionalVariableReference(oldVar, newVar);
		//else if (var1.equals(retval.getAdditionalVariableReference()))
		//	retval.setAdditionalVariableReference(var2);

	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + retval.hashCode()
		        + +((value == null) ? 0 : value.hashCode());
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
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (retval == null) {
			return other.retval == null;
		} else return retval.equals(other.retval);
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
		value.getStPosition();
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
	public boolean same(Statement s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		AssignmentStatement other = (AssignmentStatement) s;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.same(other.value))
			return false;
		if (retval == null) {
			return other.retval == null;
		} else return retval.same(other.retval);
	}

	/**
	 * Retrieve the set of FieldReference and ArrayIndex variables that can
	 * serve as a replacement for retval
	 *
	 * @return
	 */
	private Set<VariableReference> getSourceReplacements() {
		Set<VariableReference> variables = new LinkedHashSet<VariableReference>();
		for (int i = 0; i < retval.getStPosition() && i < tc.size(); i++) {
			VariableReference value = tc.getReturnValue(i);
			if (value == null)
				continue;
			if (value instanceof ArrayReference) {
				if (GenericClass.isAssignable(value.getComponentType(),
				                              this.value.getType())) {
					for (int index = 0; index < ((ArrayReference) value).getArrayLength(); index++) {
						variables.add(new ArrayIndex(tc, (ArrayReference) value, index));
					}
				}
			} else if (value instanceof ArrayIndex) {
				// Don't need to add this because array indices are created for array statement?
				if (value.isAssignableFrom(this.value.getType())) {
					variables.add(value);
				}
			} else {
				if (!value.isPrimitive() && !(value instanceof NullReference)) {
					// add fields of this object to list
					for (Field field : TestClusterUtils.getAccessibleFields(value.getVariableClass())) {
						if(TestClusterGenerator.isFinalField(field))
							continue;
						FieldReference f = new FieldReference(tc, new GenericField(field,
						        value.getGenericClass()), value);
						if (f.getDepth() <= 2) {
							if (f.isAssignableFrom(this.value.getType())) {
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
	protected boolean mutationImpl(TestCase test) {
		assert (isValid());

		// Either mutate parameter, or source
		if (Randomness.nextDouble() < 0.5) {
			Set<VariableReference> objects = getSourceReplacements();
			objects.remove(retval);
			objects.remove(value);

			if (!objects.isEmpty()) {
				VariableReference newRetVal = Randomness.choice(objects);
				// Need to double check, because we might try to replace e.g.
				// a long with an int, which is assignable
				// but if the long is assigned to a Long field, then it is not!
				if(value.isAssignableTo(newRetVal)) {

					// Need to check array status because commons lang
					// is sometimes confused about what is assignable
					if(value.isArray() == newRetVal.isArray()) {
						retval = newRetVal;
						assert (isValid());
						return true;
					}
				}
			}

		} else {

			List<VariableReference> objects = test.getObjects(value.getType(),
			                                                  retval.getStPosition());
			objects.remove(retval);
			objects.remove(value);
			if (!objects.isEmpty()) {
				VariableReference choice = Randomness.choice(objects);
				if(choice.isAssignableTo(retval)) {

					// Need special care if it is a wrapper class
					if(retval.getGenericClass().isWrapperType()) {
						Class<?> rawClass = ClassUtils.wrapperToPrimitive(retval.getVariableClass());
						if(!retval.getVariableClass().equals(rawClass) && !retval.getVariableClass().equals(choice.getVariableClass())) {
							return false;
						}
					}

					value = choice;
					assert (isValid());

					return true;
				}
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public GenericAccessibleObject<?> getAccessibleObject() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssignmentStatement() {
		return true;
	}

    @Override
    public int getPosition() {
        int pos = 0;
        for(Statement s : getTestCase()) {
            if(this == s)
                return pos;
            pos++;
        }
        throw new RuntimeException("Could not find position of assignment statement");
    }
}
