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
package org.evosuite.testcase;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.assertion.PrimitiveFieldAssertion;
import org.evosuite.contracts.ContractViolation;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.setup.TestClusterUtils;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.statements.environment.AccessedEnvironment;
import org.evosuite.testcase.variable.*;
import org.evosuite.utils.ListenableList;
import org.evosuite.utils.Listener;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericClassUtils;
import org.evosuite.utils.generic.GenericField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A test case is a list of statements
 *
 * @author Gordon Fraser
 */
public class DefaultTestCase implements TestCase, Serializable {

    private static final long serialVersionUID = -689512549778944250L;

    private static final Logger logger = LoggerFactory.getLogger(DefaultTestCase.class);

    protected static final AtomicInteger idGenerator = new AtomicInteger(0);

    private final AccessedEnvironment accessedEnvironment = new AccessedEnvironment();

    /**
     * The statements
     */
    protected final ListenableList<Statement> statements;

    /**
     * Coverage goals this test covers
     */
    private transient Set<TestFitnessFunction> coveredGoals = new LinkedHashSet<>();

    /**
     * Violations revealed by this test
     */
    private transient Set<ContractViolation> contractViolations = new LinkedHashSet<>();

    private boolean isFailing = false;

    private boolean unstable = false;

    private int id;

    /**
     * Constructs an empty test case, i.e., initially containing no statements.
     */
    public DefaultTestCase() {
        statements = new ListenableList<>(new ArrayList<>());
        id = idGenerator.getAndIncrement();
    }

    public int getID() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#accept(org.evosuite.testcase.TestVisitor)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(TestVisitor visitor) {
        visitor.visitTestCase(this);

        for (Statement s : statements) {
            logger.trace("Visiting statement " + s.getCode());
            visitor.visitStatement(s);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#addAssertions(org.evosuite.testcase.DefaultTestCase)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAssertions(TestCase other) {
        for (int i = 0; i < statements.size() && i < other.size(); i++) {
            for (Assertion a : other.getStatement(i).getAssertions()) {
                if (!statements.get(i).getAssertions().contains(a) && a != null)
                    statements.get(i).getAssertions().add(a.clone(this));
            }
        }
    }

    @Override
    public void addContractViolation(ContractViolation violation) {
        contractViolations.add(violation);
    }

    @Override
    public Set<ContractViolation> getContractViolations() {
        return contractViolations;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#addCoveredGoal(org.evosuite.testcase.TestFitnessFunction)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCoveredGoal(TestFitnessFunction goal) {
        coveredGoals.add(goal);
        // TODO: somehow adds the same goal more than once (fitnessfunction.equals()?)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCoveredGoal(TestFitnessFunction goal) {
        coveredGoals.remove(goal);
    }

    @Override
    public boolean isGoalCovered(TestFitnessFunction goal) {
        return coveredGoals.contains(goal);
    }


    private void addFields(List<VariableReference> variables, VariableReference var,
                           Type type) {

        if (!var.isPrimitive() && !(var instanceof NullReference)) {
            // add fields of this object to list
            for (Field field : TestClusterUtils.getAccessibleFields(var.getVariableClass())) {
                Type fieldType = field.getType();
                try {
                    fieldType = field.getGenericType();
                } catch (java.lang.reflect.GenericSignatureFormatError e) {
                    // Ignore
                    fieldType = field.getType();
                }
                FieldReference f = new FieldReference(this, new GenericField(field,
                        var.getGenericClass()), fieldType, var);
                if (f.getDepth() <= 2) {
                    if (type != null) {
                        if (f.isAssignableTo(type) && !variables.contains(f)) {
                            variables.add(f);
                        }
                    } else if (!variables.contains(f)) {
                        variables.add(f);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(Listener<Void> listener) {
        statements.addListener(listener);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#addStatement(org.evosuite.testcase.Statement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference addStatement(Statement statement) {
        statements.add(statement);
        try {
            assert (isValid());
        } catch (AssertionError e) {
            logger.info("Is not valid: ");
            for (Statement s : statements) {
                try {
                    logger.info(s.getCode());
                } catch (AssertionError e2) {
                    logger.info("Found error in: " + s);
                    if (s instanceof MethodStatement) {
                        MethodStatement ms = (MethodStatement) s;
                        if (!ms.isStatic()) {
                            logger.info("Callee: ");
                            logger.info(ms.getCallee().toString());
                        }
                        int num = 0;
                        for (VariableReference v : ms.getParameterReferences()) {
                            logger.info("Parameter " + num);
                            logger.info(v.getVariableClass().toString());
                            logger.info(v.getClass().toString());
                            logger.info(v.toString());
                        }
                    }
                }
            }
            assert (false);
        }
        return statement.getReturnValue();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#addStatement(org.evosuite.testcase.Statement, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference addStatement(Statement statement, int position) {
        statements.add(position, statement);
        assert (isValid());
        return statement.getReturnValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addStatements(List<? extends Statement> statements) {
        this.statements.addAll(statements);
    }

    /**
     * <p>
     * changeClassLoader
     * </p>
     *
     * @param loader a {@link java.lang.ClassLoader} object.
     */
    public void changeClassLoader(ClassLoader loader) {
        changedClassLoader = loader;
        for (Statement s : statements) {
            s.changeClassLoader(loader);
        }
    }

    private transient ClassLoader changedClassLoader = null;

    public ClassLoader getChangedClassLoader() {
        return changedClassLoader;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#chop(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void chop(int length) {
        while (statements.size() > length) {
            statements.remove(length);
        }
    }

    @Override
    public int sliceFor(VariableReference var) {

        Set<Statement> dependentStatements = new LinkedHashSet<>();
        dependentStatements.add(statements.get(var.getStPosition()));

        int lastPosition = var.getStPosition();
        // Add all statements that use this var
        for (VariableReference ref : getReferences(var)) {
            if (ref.getStPosition() > lastPosition)
                lastPosition = ref.getStPosition();
            dependentStatements.add(statements.get(ref.getStPosition()));
        }

        for (int i = lastPosition; i >= 0; i--) {
            Set<Statement> newStatements = new LinkedHashSet<>();
            for (Statement s : dependentStatements) {
                if (s.references(statements.get(i).getReturnValue()) ||
                        s.references(statements.get(i).getReturnValue().getAdditionalVariableReference())) {
                    newStatements.add(statements.get(i));
                    break;
                }
            }
            dependentStatements.addAll(newStatements);
        }
        List<Integer> dependentPositions = new ArrayList<>();
        for (Statement s : dependentStatements) {
            dependentPositions.add(s.getPosition());
        }
        dependentPositions.sort(Collections.reverseOrder());
        for (int pos = size(); pos >= 0; pos--) {
            if (!dependentPositions.contains(pos)) {
                remove(pos);
            }
        }
        return var.getStPosition();
    }

    public boolean contains(Statement statement) {
        return statements.contains(statement);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#clearCoveredGoals()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCoveredGoals() {
        coveredGoals.clear();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Create a copy of the test case
     */
    @Override
    public DefaultTestCase clone() {
        DefaultTestCase t = null;
        t = new DefaultTestCase(); //Note: cannot use super.clone() due to final fields :(
		/*
		try {
			t = (DefaultTestCase) super.clone();
		} catch (CloneNotSupportedException e) {
			//shouldn't really happen
			logger.error("Failed clone: "+e);
			return null;
		}
		*/

        for (Statement s : statements) {
            Statement copy = s.clone(t);
            t.statements.add(copy);
            copy.setRetval(s.getReturnValue().clone(t));
            copy.setAssertions(s.copyAssertions(t, 0));
        }
        t.coveredGoals.addAll(coveredGoals);
        t.accessedEnvironment.copyFrom(accessedEnvironment);
        t.isFailing = isFailing;
        t.id = idGenerator.getAndIncrement(); //always create new ID when making a clone
        //t.exception_statement = exception_statement;
        //t.exceptionThrown = exceptionThrown;
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteListener(Listener<Void> listener) {
        statements.deleteListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultTestCase other = (DefaultTestCase) obj;

        if (statements == null) {
            return other.statements == null;
        } else {
            if (statements.size() != other.statements.size())
                return false;
            // if (!statements.equals(other.statements))
            for (int i = 0; i < statements.size(); i++) {
                if (!statements.get(i).equals(other.statements.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getAccessedClasses()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<?>> getAccessedClasses() {
        Set<Class<?>> accessedClasses = new LinkedHashSet<>();
        for (Statement s : statements) {
            for (VariableReference var : s.getVariableReferences()) {
                if (var != null && !var.isPrimitive()) {
                    Class<?> clazz = var.getVariableClass();
                    while (clazz.isMemberClass()) {
                        //accessed_classes.add(clazz);
                        clazz = clazz.getEnclosingClass();
                    }
                    while (clazz.isArray())
                        clazz = clazz.getComponentType();
                    accessedClasses.add(clazz);
                }
            }
            if (s instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) s;
                accessedClasses.addAll(Arrays.asList(ms.getMethod().getMethod().getExceptionTypes()));
                accessedClasses.add(ms.getMethod().getMethod().getDeclaringClass());
                accessedClasses.add(ms.getMethod().getMethod().getReturnType());
                accessedClasses.addAll(Arrays.asList(ms.getMethod().getMethod().getParameterTypes()));
            } else if (s instanceof FieldStatement) {
                FieldStatement fs = (FieldStatement) s;
                accessedClasses.add(fs.getField().getField().getDeclaringClass());
                accessedClasses.add(fs.getField().getField().getType());
            } else if (s instanceof ConstructorStatement) {
                ConstructorStatement cs = (ConstructorStatement) s;
                accessedClasses.add(cs.getConstructor().getConstructor().getDeclaringClass());
                accessedClasses.addAll(Arrays.asList(cs.getConstructor().getConstructor().getExceptionTypes()));
                accessedClasses.addAll(Arrays.asList(cs.getConstructor().getConstructor().getParameterTypes()));
            }
        }
        return accessedClasses;
    }


    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getAssertions()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Assertion> getAssertions() {
        List<Assertion> assertions = new ArrayList<>();
        for (Statement s : statements) {
            assertions.addAll(s.getAssertions());
        }
        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getCoveredGoals()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TestFitnessFunction> getCoveredGoals() {
        return coveredGoals;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getDeclaredExceptions()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<?>> getDeclaredExceptions() {
        Set<Class<?>> exceptions = new LinkedHashSet<>();
        for (Statement statement : statements) {
            exceptions.addAll(statement.getDeclaredExceptions());
        }
        return exceptions;
    }

    @Override
    public AccessedEnvironment getAccessedEnvironment() {
        return accessedEnvironment;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getDependencies(org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<VariableReference> getDependencies(VariableReference var) {
        Set<VariableReference> dependencies = new LinkedHashSet<>();

        if (var == null || var.getStPosition() == -1)
            return dependencies;

        Set<Statement> dependentStatements = new LinkedHashSet<>();
        if (statements.size() > var.getStPosition())
            dependentStatements.add(statements.get(var.getStPosition()));

        for (int i = var.getStPosition(); i >= 0; i--) {
            Set<Statement> newStatements = new LinkedHashSet<>();
            for (Statement s : dependentStatements) {
                if (s.references(statements.get(i).getReturnValue())) {
                    newStatements.add(statements.get(i));
                    dependencies.add(statements.get(i).getReturnValue());
                    break;
                }
            }
            dependentStatements.addAll(newStatements);
        }

        return dependencies;
    }

    @Override
    public VariableReference getLastObject(Type type) throws ConstructionFailedException {
        return getLastObject(type, 0);
    }

    @Override
    public VariableReference getLastObject(Type type, int position)
            throws ConstructionFailedException {
        for (int i = statements.size() - 1; i >= position; i--) {
            Statement statement = statements.get(i);
            VariableReference var = statement.getReturnValue();
            if (var.isAssignableTo(type))
                return var;
        }
        throw new ConstructionFailedException("Found no variables of type " + type);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getObject(org.evosuite.testcase.VariableReference, org.evosuite.testcase.Scope)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject(VariableReference reference, Scope scope) {
        try {
            return reference.getObject(scope);
        } catch (CodeUnderTestException e) {
            throw new AssertionError("This case isn't handled yet");
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getObjects(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VariableReference> getObjects(int position) {
        List<VariableReference> variables = new LinkedList<>();

        for (int i = 0; i < position && i < statements.size(); i++) {
            VariableReference value = statements.get(i).getReturnValue();

            if (value == null)
                continue;
            // TODO: Need to support arrays that were not self-created
            if (value instanceof ArrayReference) { // &&
                for (int index = 0; index < ((ArrayReference) value).getArrayLength(); index++) {
                    variables.add(new ArrayIndex(this, (ArrayReference) value, index));
                }
            } else if (!(value instanceof ArrayIndex)) {
                variables.add(value);
                addFields(variables, value, null);
            }
            // logger.trace(statements.get(i).retval.getSimpleClassName());
        }

        return variables;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getObjects(java.lang.reflect.Type, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VariableReference> getObjects(Type type, int position) {
        List<VariableReference> variables = new LinkedList<>();

        GenericClass<?> genericClass = GenericClassFactory.get(type);
        Class<?> rawClass = genericClass.getRawClass();
        for (int i = 0; i < position && i < size(); i++) {
            Statement statement = statements.get(i);
            if (statement instanceof MethodStatement) {
                if (((MethodStatement) statement).getMethod().getName().equals("hashCode"))
                    continue;
            }
            VariableReference value = statement.getReturnValue();

            if (value == null)
                continue;
            if (value instanceof ArrayReference) {

                // For some reason, TypeUtils/ClassUtils sometimes claims
                // that an array is assignable to its component type
                // TODO: Fix
                boolean isClassUtilsBug = false;
                if (value.isArray()) {
                    Class<?> arrayClass = value.getVariableClass();
                    isClassUtilsBug = isClassUtilsBug(rawClass, arrayClass);
                }
                if (rawClass.isArray() && !isClassUtilsBug) {
                    isClassUtilsBug = isClassUtilsBug(value.getVariableClass(), rawClass);
                }

                if (value.isAssignableTo(type) && !isClassUtilsBug && value.isArray() == rawClass.isArray()) {
                    logger.debug("Array is assignable: " + value.getType() + " to "
                            + type + ", " + value.isArray() + ", " + rawClass.isArray());
                    variables.add(value);
                } else if (GenericClassUtils.isAssignable(type, value.getComponentType())) {
                    Class<?> arrayClass = value.getComponentClass();
                    if (isClassUtilsBug(rawClass, arrayClass)) {
                        continue;
                    }

                    for (int index = 0; index < ((ArrayReference) value).getArrayLength(); index++) {
                        if (((ArrayReference) value).isInitialized(index, position))
                            variables.add(new ArrayIndex(this, (ArrayReference) value,
                                    index));
                    }
                }
            } else if (value instanceof ArrayIndex) {
                // Don't need to add this because array indices are created for array statement
            } else if (value.isAssignableTo(type) && value.isPrimitive() == rawClass.isPrimitive() &&
                    value.isArray() == rawClass.isArray()) {
                variables.add(value);
            } else {
                addFields(variables, value, type);
            }
        }

        return variables;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getRandomObject(java.lang.reflect.Type, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position)
            throws ConstructionFailedException {
        Inputs.checkNull(type);

        List<VariableReference> variables = getObjects(type, position);
        Iterator<VariableReference> iterator = variables.iterator();
        while (iterator.hasNext()) {
            VariableReference var = iterator.next();
            if (var instanceof NullReference)
                iterator.remove();
            else if (getStatement(var.getStPosition()) instanceof PrimitiveStatement)
                iterator.remove();
            else if (var.isPrimitive() || var.isWrapperType())
                iterator.remove();
            else if (this.getStatement(var.getStPosition()) instanceof FunctionalMockStatement && !(this.getStatement(var.getStPosition()) instanceof FunctionalMockForAbstractClassStatement))
                iterator.remove();
        }
        if (variables.isEmpty())
            throw new ConstructionFailedException("Found no variables of type " + type
                    + " at position " + position);

        return Randomness.choice(variables);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getRandomObject(java.lang.reflect.Type, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference getRandomNonNullObject(Type type, int position)
            throws ConstructionFailedException {
        Inputs.checkNull(type);

        List<VariableReference> variables = getObjects(type, position);
        variables.removeIf(ref -> {
            final Statement statement = this.getStatement(ref.getStPosition());
            return ref instanceof NullReference || statement instanceof FunctionalMockStatement;
        });
        if (variables.isEmpty())
            throw new ConstructionFailedException("Found no variables of type " + type
                    + " at position " + position);

        return Randomness.choice(variables);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getRandomObject()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference getRandomObject() {
        return getRandomObject(statements.size());
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getRandomObject(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference getRandomObject(int position) {
        List<VariableReference> variables = getObjects(position);
        if (variables.isEmpty())
            return null;

        return Randomness.choice(variables);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getRandomObject(java.lang.reflect.Type)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference getRandomObject(Type type)
            throws ConstructionFailedException {
        return getRandomObject(type, statements.size());
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getRandomObject(java.lang.reflect.Type, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference getRandomObject(Type type, int position)
            throws ConstructionFailedException {
        assert (type != null);
        List<VariableReference> variables = getObjects(type, position);
        if (variables.isEmpty())
            throw new ConstructionFailedException("Found no variables of type " + type
                    + " at position " + position);

        return Randomness.choice(variables);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getReferences(org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<VariableReference> getReferences(VariableReference var) {
        Set<VariableReference> references = new LinkedHashSet<>();

        if (var == null || var.getStPosition() == -1)
            return references;

        // references.add(var);

        for (int i = var.getStPosition() + 1; i < statements.size(); i++) {
            Set<VariableReference> temp = new LinkedHashSet<>();
            if (statements.get(i).references(var))
                temp.add(statements.get(i).getReturnValue());
            else if (statements.get(i).references(var.getAdditionalVariableReference()))
                temp.add(statements.get(i).getReturnValue());
            for (VariableReference v : references) {
                if (statements.get(i).references(v))
                    temp.add(statements.get(i).getReturnValue());
                else if (statements.get(i).references(v.getAdditionalVariableReference()))
                    temp.add(statements.get(i).getReturnValue());
            }
            references.addAll(temp);
        }

        return references;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getReturnValue(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference getReturnValue(int position) {
        return getStatement(position).getReturnValue();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#getStatement(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement getStatement(int position) {
        if (position < 0 || position >= statements.size()) {
            throw new IllegalArgumentException("Cannot access statement due to wrong position "
                    + position + ", where total number of statements is " + statements.size());
        }
        return statements.get(position);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#hasStatement(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStatement(int position) {
        return statements.size() > position && position >= 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#hasAssertions()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAssertions() {
        return statements.stream().anyMatch(Statement::hasAssertions);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#hasCastableObject(java.lang.reflect.Type)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCastableObject(Type type) {
        return statements.stream().anyMatch(s -> s.getReturnValue().isAssignableFrom(type));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Equality check
     */
    // public boolean equals(TestCase t) {
    // return statements.size() == t.statements.size() && isPrefix(t);
    // }
    @Override
    public int hashCode() {
        return statements.hashCode();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#hasObject(java.lang.reflect.Type, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasObject(Type type, int position) {
        for (int i = 0; i < position && i < size(); i++) {
            Statement st = statements.get(i);
            if (st.getReturnValue() == null)
                continue; // Nop
            if (st.getReturnValue().isAssignableTo(type)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#hasReferences(org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasReferences(VariableReference var) {
        if (var == null || var.getStPosition() == -1)
            return false;

        for (int i = var.getStPosition() + 1; i < statements.size(); i++) {
            if (statements.get(i).references(var))
                return true;
        }
        for (Assertion assertion : statements.get(var.getStPosition()).getAssertions()) {
            if (assertion.getReferencedVariables().contains(var))
                return true;
        }
        return false;
    }

    private boolean isClassUtilsBug(Class<?> rawClass, Class<?> arrayClass) {
        while (arrayClass != null && arrayClass.isArray()) {
            if (rawClass.isAssignableFrom(arrayClass.getComponentType())) {
                //			if (arrayClass.getComponentType().equals(rawClass)) {
                return true;
            }
            arrayClass = arrayClass.getComponentType();
        }
        return false;
    }

    @Override
    public boolean isAccessible() {
        return statements.stream().allMatch(Statement::isAccessible);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#isEmpty()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return statements.isEmpty();
    }

    @Override
    public boolean isFailing() {
        return isFailing;
    }

    @Override
    public void setFailing() {
        isFailing = true;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#isPrefix(org.evosuite.testcase.DefaultTestCase)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrefix(TestCase t) {
        if (statements.size() > t.size())
            return false;

        for (int i = 0; i < statements.size(); i++) {
            if (!statements.get(i).same(t.getStatement(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isUnstable() {
        return unstable;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#isValid()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        for (Statement s : statements) {
            assert (s.isValid()) : toCode();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Statement> iterator() {
        return statements.iterator();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#remove(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(int position) {
        logger.debug("Removing statement {}", position);
        if (position >= size()) {
            return;
        }
        statements.remove(position);
        assert (isValid());
        // for(Statement s : statements) {
        // for(Asss.assertions)
        // }
    }

    @Override
    public void removeAssertion(Assertion assertion) {
        statements.forEach(s -> s.removeAssertion(assertion));
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#removeAssertions()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAssertions() {
        statements.forEach(Statement::removeAssertions);
    }

    private boolean methodNeedsDownCast(MethodStatement methodStatement, VariableReference var, Class<?> abstractClass) {

        if (!methodStatement.isStatic() && methodStatement.getCallee().equals(var)) {

            if (MethodUtils.getAccessibleMethod(abstractClass, methodStatement.getMethodName(), methodStatement.getMethod().getRawParameterTypes()) == null) {
                // Need downcast for real
                return true;
            } else {
                Method superClassMethod = MethodUtils.getMatchingMethod(abstractClass, methodStatement.getMethodName(), methodStatement.getMethod().getRawParameterTypes());
                if (superClassMethod != null && !methodStatement.getMethod().getRawGeneratedType().equals(superClassMethod.getReturnType())) {
                    // Overriding can also change return value, in which case we need to keep the downcast
                    return true;
                }
            }
        }
        List<VariableReference> parameters = methodStatement.getParameterReferences();
        Class<?>[] parameterTypes = methodStatement.getMethod().getRawParameterTypes();
        for (int i = 0; i < parameters.size(); i++) {
            VariableReference param = parameters.get(i);
            if (param.equals(var) && !parameterTypes[i].isAssignableFrom(abstractClass)) {
                // Need downcast for real
                return true;
            }
        }
        return false;
    }

    private boolean constructorNeedsDownCast(ConstructorStatement constructorStatement, VariableReference var, Class<?> abstractClass) {
        List<VariableReference> parameters = constructorStatement.getParameterReferences();
        Class<?>[] parameterTypes = constructorStatement.getConstructor().getConstructor().getParameterTypes();
        for (int i = 0; i < parameters.size(); i++) {
            VariableReference param = parameters.get(i);
            if (param.equals(var) && !parameterTypes[i].isAssignableFrom(abstractClass)) {
                // Need downcast for real
                return true;
            }
        }
        return false;
    }

    private boolean fieldNeedsDownCast(FieldReference fieldReference, VariableReference var, Class<?> abstractClass) {
        if (fieldReference.getSource() != null && fieldReference.getSource().equals(var)) {
            // Need downcast for real
            return !fieldReference.getField().getDeclaringClass().isAssignableFrom(abstractClass);
        }
        return false;
    }

    private boolean fieldNeedsDownCast(FieldStatement fieldStatement, VariableReference var, Class<?> abstractClass) {
        if (!fieldStatement.isStatic() && fieldStatement.getSource().equals(var)) {
            // Need downcast for real
            return !fieldStatement.getField().getDeclaringClass().isAssignableFrom(abstractClass);
        }
        return false;
    }

    private boolean assertionsNeedDownCast(Statement s, VariableReference var, Class<?> abstractClass) {
        for (Assertion assertion : s.getAssertions()) {
            if (assertion instanceof InspectorAssertion && assertion.getSource().equals(var)) {
                InspectorAssertion inspectorAssertion = (InspectorAssertion) assertion;
                Method inspectorMethod = inspectorAssertion.getInspector().getMethod();
                if (MethodUtils.getAccessibleMethod(abstractClass, inspectorMethod.getName(), inspectorMethod.getParameterTypes()) == null) {
                    return true;
                }
            } else if (assertion instanceof PrimitiveFieldAssertion && assertion.getSource().equals(var)) {
                PrimitiveFieldAssertion fieldAssertion = (PrimitiveFieldAssertion) assertion;
                if (!fieldAssertion.getField().getDeclaringClass().isAssignableFrom(abstractClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeDownCasts() {
        for (Statement s : statements) {
            if (s instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) s;
                VariableReference retVal = s.getReturnValue();
                Class<?> variableClass = retVal.getVariableClass();
                Class<?> methodReturnClass = ms.getMethod().getRawGeneratedType();
                if (!variableClass.equals(methodReturnClass) && methodReturnClass.isAssignableFrom(variableClass)) {
                    logger.debug("Found downcast from {} to {}", methodReturnClass.getName(), variableClass);
                    if (assertionsNeedDownCast(ms, retVal, methodReturnClass)) {
                        return;
                    }
                    for (VariableReference ref : getReferences(retVal)) {
                        Statement usageStatement = statements.get(ref.getStPosition());
                        if (assertionsNeedDownCast(usageStatement, retVal, methodReturnClass)) {
                            return;
                        }
                        if (usageStatement instanceof MethodStatement) {
                            if (methodNeedsDownCast((MethodStatement) usageStatement, retVal, methodReturnClass)) {
                                return;
                            }
                        } else if (usageStatement instanceof ConstructorStatement) {
                            if (constructorNeedsDownCast((ConstructorStatement) usageStatement, retVal, methodReturnClass)) {
                                return;
                            }

                        } else if (usageStatement instanceof FieldStatement) {
                            if (fieldNeedsDownCast((FieldStatement) usageStatement, retVal, methodReturnClass)) {
                                return;
                            }
                        }
                        if (ref.isFieldReference()) {
                            if (fieldNeedsDownCast((FieldReference) ref, retVal, methodReturnClass)) {
                                return;
                            }
                        }
                    }
                    logger.debug("Downcast not needed, replacing with {}", ms.getMethod().getReturnType());
                    retVal.setType(ms.getMethod().getReturnType());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(VariableReference var1, VariableReference var2) {
        statements.forEach(s -> s.replace(var1, var2));
    }


    private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
            IOException {
        ois.defaultReadObject();

        coveredGoals = new LinkedHashSet<>();
        contractViolations = new LinkedHashSet<>();
    }

    public void setFailing(boolean failing) {
        isFailing = failing;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#setStatement(org.evosuite.testcase.Statement, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference setStatement(Statement statement, int position) {
        statements.set(position, statement);
        assert (isValid());
        return statement.getReturnValue(); // TODO:
        // -1?
    }

    @Override
    public void setUnstable(boolean unstable) {
        this.unstable = unstable;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#size()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return statements.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sizeWithAssertions() {
        return this.size() + this.getAssertions().size();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#toCode()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String toCode() {
        TestCodeVisitor visitor = new TestCodeVisitor();
        accept(visitor);
        return visitor.getCode();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestCase#toCode(java.util.Map)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String toCode(Map<Integer, Throwable> exceptions) {
        TestCodeVisitor visitor = new TestCodeVisitor();
        visitor.setExceptions(exceptions);
        accept(visitor);
        return visitor.getCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toCode();
    }
}
