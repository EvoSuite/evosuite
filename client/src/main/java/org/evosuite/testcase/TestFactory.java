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

import com.googlecode.gentyref.CaptureType;
import com.googlecode.gentyref.GenericTypeReflector;
import org.apache.commons.lang3.ClassUtils;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.seeding.CastClassManager;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testcase.mutation.RandomInsertion;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.statements.environment.EnvironmentStatements;
import org.evosuite.testcase.statements.reflection.PrivateFieldStatement;
import org.evosuite.testcase.statements.reflection.PrivateMethodStatement;
import org.evosuite.testcase.statements.reflection.ReflectionFactory;
import org.evosuite.testcase.variable.*;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/*
 * A note about terminology: this class currently uses the term "object" or
 * "Object" with ambiguous meanings. Depending on the context, it may refer to
 * one of the following:
 *  - objects in the OOP sense as instances of classes (i.e., complex data
 *    types that extend java.lang.Object). For example: method createObject
 *    creates an object for a given class
 *  - object referring to the class java.lang.Object, e.g.,
 *    attemptInstantiationOfObjectClass tries to find objects of some data
 *    type T that can be assigned to Object and safely downcast to T
 *  - object referring to the object-representation of a reflected field,
 *    method or constructor of a class, i.e., GenericAccessibleObject
 */

/**
 * @author Gordon Fraser
 */
public class TestFactory {

    private static final Logger logger = LoggerFactory.getLogger(TestFactory.class);

    /**
     * Keep track of objects we are already trying to generate to avoid cycles
     */
    private final transient Set<GenericAccessibleObject<?>> currentRecursion = new LinkedHashSet<>();

    /**
     * Singleton instance
     */
    private static TestFactory instance = null;

    private ReflectionFactory reflectionFactory;

    private TestFactory() {
        reset();
    }

    /**
     * We keep track of calls already attempted to avoid infinite recursion
     */
    public void reset() {
        currentRecursion.clear();
        reflectionFactory = null;
    }

    public static TestFactory getInstance() {
        if (instance == null)
            instance = new TestFactory();
        return instance;
    }

    /**
     * Adds a call of the field or method represented by {@code call} to the
     * test case
     * {@code test} at the given {@code position} with {@code callee} as the callee of {@code call}.
     * Note that constructor calls are <em>not</em> supported
     * Returns {@code true} if the operation was successful, {@code false} otherwise.
     *
     * @param test     the test case the call should be added to
     * @param callee   reference to the owning object of {@code call}
     * @param call     the {@code GenericAccessibleObject}
     * @param position the position within {@code test} at which to add the call
     * @return {@code true} if successful, {@code false} otherwise
     */
    private boolean addCallFor(TestCase test, VariableReference callee,
                               GenericAccessibleObject<?> call, int position) {

        logger.trace("addCallFor {}", callee.getName());

        int previousLength = test.size(); // length of the test case before inserting new statements
        currentRecursion.clear();

        try {
            if (call.isMethod()) {
                GenericMethod method = (GenericMethod) call;
                if (call.isStatic() || !method.getDeclaringClass().isAssignableFrom(callee.getVariableClass())) {
                    // Static methods / methods in other classes can be modifiers of the SUT if the SUT depends on static fields
                    addMethod(test, method, position, 0);
                } else {
                    addMethodFor(test,
                            callee,
                            (GenericMethod) call.copyWithNewOwner(callee.getGenericClass()),
                            position);
                }
            } else if (call.isField()) {
                // A modifier for the SUT could also be a static field in another class
                if (call.isStatic()) {
                    addFieldAssignment(test, (GenericField) call, position, 0);
                } else {
                    addFieldFor(test,
                            callee,
                            (GenericField) call.copyWithNewOwner(callee.getGenericClass()),
                            position);
                }
            }
            return true;
        } catch (ConstructionFailedException e) {
            // TODO: Check this!
            logger.debug("Inserting call {} has failed: {} Removing statements", call, e);
            // TODO: Doesn't work if position != test.size()
            int lengthDifference = test.size() - previousLength;

            // Undo the changes made to the test case by removing the statements inserted so far.
            // We need to remove them in order, so that the test case is at all time consistent.
            for (int i = lengthDifference - 1; i >= 0; i--) {
                if (logger.isDebugEnabled()) {
                    logger.debug("  Removing statement: " + test.getStatement(position + i).getCode());
                }
                test.remove(position + i);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Test after removal: " + test.toCode());
            }
            return false;
        }
    }


    public VariableReference addFunctionalMock(TestCase test, Type type, int position, int recursionDepth)
            throws ConstructionFailedException, IllegalArgumentException {

        Inputs.checkNull(test, type);

        if (recursionDepth > Properties.MAX_RECURSION) {
            logger.debug("Max recursion depth reached");
            throw new ConstructionFailedException("Max recursion depth reached");
        }

        //TODO this needs to be fixed once we handle Generics in mocks
        FunctionalMockStatement fms = new FunctionalMockStatement(test, type, GenericClassFactory.get(type));
        VariableReference ref = test.addStatement(fms, position);

        //note: when we add a new mock, by default it will have no parameter at the beginning

        return ref;
    }

    public VariableReference addFunctionalMockForAbstractClass(TestCase test, Type type, int position, int recursionDepth)
            throws ConstructionFailedException, IllegalArgumentException {

        Inputs.checkNull(test, type);

        if (recursionDepth > Properties.MAX_RECURSION) {
            logger.debug("Max recursion depth reached");
            throw new ConstructionFailedException("Max recursion depth reached");
        }

        //TODO this needs to be fixed once we handle Generics in mocks
        FunctionalMockForAbstractClassStatement fms = new FunctionalMockForAbstractClassStatement(test,
                type,
                GenericClassFactory.get(type));
        VariableReference ref = test.addStatement(fms, position);

        //note: when we add a new mock, by default it will have no parameter at the beginning

        return ref;
    }

    /**
     * Inserts a call to the given {@code constructor} into the {@code test} case at the specified
     * {@code position}.
     * <p>
     * Callers of this method have to supply the current recursion depth. This
     * allows for better management of test generation resources. If this method is called from
     * another method that already has a recursion depth as formal parameter, passing that
     * recursion depth + 1 is appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the return value of the constructor call. If the
     * {@link Properties#MAX_RECURSION maximum recursion depth} has been reached a
     * {@code ConstructionFailedException} is thrown.
     *
     * @param test           the test case in which to insert
     * @param constructor    the constructor for which to add the call
     * @param position       the position at which to insert
     * @param recursionDepth the current recursion depth
     * @return a reference to the result of the constructor call
     * @throws ConstructionFailedException if the maximum recursion depth has been reached
     */
    public VariableReference addConstructor(TestCase test,
                                            GenericConstructor constructor, int position, int recursionDepth)
            throws ConstructionFailedException {
        return addConstructor(test, constructor, null, position, recursionDepth);
    }

    /**
     * Inserts a call to the given {@code constructor} into the {@code test} case at the specified
     * {@code position}.
     * <p>
     * Callers of this method have to supply the current recursion depth. This
     * allows for better management of test generation resources. If this method is called from
     * another method that already has a recursion depth as formal parameter, passing that
     * recursion depth + 1 is appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the return value of the constructor call. If the
     * {@link Properties#MAX_RECURSION maximum recursion depth} has been reached a
     * {@code ConstructionFailedException} is thrown.
     *
     * @param test           the test case in which to insert
     * @param constructor    the constructor for which to add the call
     * @param exactType
     * @param position       the position at which to insert
     * @param recursionDepth the current recursion depth
     * @return a reference to the result of the constructor call
     * @throws ConstructionFailedException if the maximum recursion depth has been reached
     */
    public VariableReference addConstructor(TestCase test,
                                            GenericConstructor constructor, Type exactType, int position,
                                            int recursionDepth) throws ConstructionFailedException {

        if (recursionDepth > Properties.MAX_RECURSION) {
            logger.debug("Max recursion depth reached");
            throw new ConstructionFailedException("Max recursion depth reached");
        }

        Class<?> klass = constructor.getRawGeneratedType();

        int length = test.size();

        try {
            //first be sure if parameters can be satisfied
            List<VariableReference> parameters = satisfyParameters(test,
                    null,
                    Arrays.asList(constructor.getParameterTypes()),
                    Arrays.asList(constructor.getConstructor().getParameters()),
                    position,
                    recursionDepth + 1,
                    true, false, true);
            int newLength = test.size();
            position += (newLength - length);

            //create a statement for the constructor
            Statement st = new ConstructorStatement(test, constructor, parameters);
            VariableReference ref = test.addStatement(st, position);

            return ref;
        } catch (Exception e) {
            throw new ConstructionFailedException("Failed to add constructor for " + klass.getName() +
                    " due to " + e.getClass().getCanonicalName() + ": " + e.getMessage());
        }
    }

    /**
     * Adds the given {@code field} to the {@code test} case at the given {@code position}.
     * <p>
     * Callers of this method have to supply the current recursion depth. This
     * allows for better management of test generation resources. If this method is called from
     * another method that already has a recursion depth as formal parameter, passing that
     * recursion depth + 1 is appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the inserted field. If the {@link Properties#MAX_RECURSION maximum
     * recursion depth} has been reached a {@code ConstructionFailedException} is thrown.
     *
     * @param test           the test case to which to add
     * @param field          the field to add
     * @param position       the position at which to add the field
     * @param recursionDepth the current recursion depth
     * @return a reference to the inserted field
     * @throws ConstructionFailedException if the maximum recursion depth has been reached
     */
    public VariableReference addField(TestCase test, GenericField field, int position,
                                      int recursionDepth) throws ConstructionFailedException {

        logger.debug("Adding field {}", field);
        if (recursionDepth > Properties.MAX_RECURSION) {
            logger.debug("Max recursion depth reached");
            throw new ConstructionFailedException("Max recursion depth reached");
        }

        VariableReference callee = null;
        int length = test.size();

        if (!field.isStatic()) {
            callee = createOrReuseVariable(test, field.getOwnerType(), position,
                    recursionDepth, null, false, false, false);
            position += test.size() - length;

            if (!TestUsageChecker.canUse(field.getField(), callee.getVariableClass())) {
                logger.debug("Cannot call field {} with callee of type {}", field, callee.getClassName());
                throw new ConstructionFailedException("Cannot apply field to this callee");
            }

            // TODO: Check if field is still accessible in subclass
            if (!field.getOwnerClass().equals(callee.getGenericClass())) {
                try {
                    if (!TestUsageChecker.canUse(callee.getVariableClass().getField(field.getName()))) {
                        throw new ConstructionFailedException("Cannot access field in subclass");
                    }
                } catch (NoSuchFieldException fe) {
                    throw new ConstructionFailedException("Cannot access field in subclass");
                }
            }
        }

        Statement st = new FieldStatement(test, field, callee);

        return test.addStatement(st, position);
    }

    /**
     * Add method at given position if max recursion depth has not been reached
     *
     * @param test
     * @param field
     * @param position
     * @param recursionDepth
     * @return
     * @throws ConstructionFailedException
     */
    public VariableReference addFieldAssignment(TestCase test, GenericField field,
                                                int position, int recursionDepth) throws ConstructionFailedException {
        logger.debug("Recursion depth: " + recursionDepth);
        if (recursionDepth > Properties.MAX_RECURSION) {
            logger.debug("Max recursion depth reached");
            throw new ConstructionFailedException("Max recursion depth reached");
        }
        logger.debug("Adding field " + field);

        int length = test.size();
        VariableReference callee = null;
        if (!field.isStatic()) {
            callee = createOrReuseVariable(test, field.getOwnerType(), position,
                    recursionDepth, null, false, false, false);
            position += test.size() - length;
            length = test.size();
            if (!TestUsageChecker.canUse(field.getField(), callee.getVariableClass())) {
                logger.debug("Cannot call field " + field + " with callee of type "
                        + callee.getClassName());
                throw new ConstructionFailedException("Cannot apply field to this callee");
            }

        }

        VariableReference var = createOrReuseVariable(test, field.getFieldType(),
                position, recursionDepth, callee, true, false, false);
        int newLength = test.size();
        position += (newLength - length);

        FieldReference f = new FieldReference(test, field, callee);
        if (f.equals(var))
            throw new ConstructionFailedException("Self assignment");

        Statement st = new AssignmentStatement(test, f, var);
        VariableReference ret = test.addStatement(st, position);
        // logger.info("FIeld assignment: " + st.getCode());
        assert (test.isValid());
        return ret;
    }

    /**
     * Add reference to a field of variable "callee"
     *
     * @param test
     * @param callee
     * @param field
     * @param position
     * @return
     * @throws ConstructionFailedException
     */
    public VariableReference addFieldFor(TestCase test, VariableReference callee,
                                         GenericField field, int position) throws ConstructionFailedException {
        logger.debug("Adding field " + field + " for variable " + callee);
        if (position <= callee.getStPosition())
            throw new ConstructionFailedException("Cannot insert call on object before the object is defined");

        currentRecursion.clear();

        FieldReference fieldVar = new FieldReference(test, field, callee);
        int length = test.size();
        VariableReference value = createOrReuseVariable(test, fieldVar.getType(),
                position, 0, callee, true, false, true);

        int newLength = test.size();
        position += (newLength - length);

        Statement st = new AssignmentStatement(test, fieldVar, value);
        VariableReference ret = test.addStatement(st, position);
        ret.setDistance(callee.getDistance() + 1);

        assert (test.isValid());

        return ret;
    }

    /**
     * Adds the given {@code method} call to the {@code test} at the specified {@code position}.
     * For non-static methods, the callee object of the method is chosen at random.
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the return value of the inserted method call. If the
     * {@link Properties#MAX_RECURSION maximum  recursion depth} has been reached a
     * {@code ConstructionFailedException} is thrown.
     *
     * @param test           the test case in which to insert
     * @param method         the method call to insert
     * @param position       the position at which to add the call
     * @param recursionDepth the current recursion depth (see above)
     * @return a reference to the return value of the inserted method call
     * @throws ConstructionFailedException if the maximum recursion depth has been reached
     */
    public VariableReference addMethod(TestCase test, GenericMethod method, int position,
                                       int recursionDepth) throws ConstructionFailedException {

        logger.debug("Recursion depth: " + recursionDepth);
        if (recursionDepth > Properties.MAX_RECURSION) {
            logger.debug("Max recursion depth reached");
            throw new ConstructionFailedException("Max recursion depth reached");
        }

        logger.debug("Adding method " + method);
        int length = test.size();
        VariableReference callee = null;
        List<VariableReference> parameters = null;

        try {
            if (!method.isStatic()) {
                callee = createOrReuseVariable(test, method.getOwnerType(), position,
                        recursionDepth, null, false, false, false);
                //a functional mock can never be a callee
                assert !(test.getStatement(callee.getStPosition()) instanceof FunctionalMockStatement);

                position += test.size() - length;
                length = test.size();

                logger.debug("Found callee of type " + method.getOwnerType() + ": "
                        + callee.getName());
                if (!TestUsageChecker.canUse(method.getMethod(),
                        callee.getVariableClass())) {
                    logger.debug("Cannot call method " + method
                            + " with callee of type " + callee.getClassName());
                    throw new ConstructionFailedException("Cannot apply method to this callee");
                }
            }

            // Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
            parameters = satisfyParameters(test, callee,
                    Arrays.asList(method.getParameterTypes()),
                    Arrays.asList(method.getMethod().getParameters()),
                    position, recursionDepth + 1, true, false, true);

        } catch (ConstructionFailedException e) {
            // TODO: Re-insert in new test cluster
            // TestCluster.getInstance().checkDependencies(method);
            throw e;
        }

        int newLength = test.size();
        position += (newLength - length);

        Statement st = new MethodStatement(test, method, callee, parameters);
        VariableReference ret = test.addStatement(st, position);
        if (callee != null)
            ret.setDistance(callee.getDistance() + 1);
        return ret;
    }

    /**
     * Adds the given {@code method} call to the {@code test} at the specified {@code position},
     * using the supplied {@code VariableReference} as {@code callee} object of the {@code method}.
     * Only intended to be used for <em>non-static</em> methods! If a static {@code method} is
     * supplied, the behavior is undefined.
     * <p>
     * Returns a reference to the return value of the inserted method call. Throws a
     * {@code ConstructionFailedException} if the given {@code position} is invalid, i.e., if
     * {@code callee} is undefined (or has not been defined yet) at {@code position}.
     *
     * @param test     the test case in which to insert
     * @param callee   reference to the object on which to call the {@code method}
     * @param method   the method call to insert
     * @param position the position at which to add the call
     * @return a reference to the return value of the inserted method call
     * @throws ConstructionFailedException if the given position is invalid (see above)
     */
    public VariableReference addMethodFor(TestCase test, VariableReference callee,
                                          GenericMethod method, int position) throws ConstructionFailedException {

        logger.debug("Adding method {} for {} (Generating {})", method, callee, method.getGeneratedClass());

        if (position <= callee.getStPosition()) {
            throw new ConstructionFailedException("Cannot insert call on object before the object is defined");
        }

        currentRecursion.clear();
        int length = test.size();

        boolean allowNull = true;

        // Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
        List<VariableReference> parameters = satisfyParameters(
                test, callee,
                Arrays.asList(method.getParameterTypes()),
                Arrays.asList(method.getMethod().getParameters()), position, 1, allowNull, false, true);

        int newLength = test.size();
        position += (newLength - length);

        Statement st = new MethodStatement(test, method, callee, parameters);
        VariableReference ret = test.addStatement(st, position);
        ret.setDistance(callee.getDistance() + 1);

        logger.debug("Success: Adding method {}", method);
        return ret;
    }

    /**
     * Adds the given primitive statement {@code old} at the specified {@code
     * position} to the test case {@code test}.
     *
     * @param test     the test case to which to add the statement
     * @param old      the primitive statement to add
     * @param position the position in {@code test} at which to add the statement
     * @return a reference to the return value of the added statement
     */
    private VariableReference addPrimitive(TestCase test, PrimitiveStatement<?> old,
                                           int position) throws ConstructionFailedException {
        logger.debug("Adding primitive");
        Statement st = old.clone(test);
        return test.addStatement(st, position);
    }

    /**
     * Appends the given {@code statement} at the end of the test case {@code test}, trying to
     * satisfy parameters.
     * <p>
     * Called from TestChromosome when doing crossover
     *
     * @param test
     * @param statement
     */
    public void appendStatement(TestCase test, Statement statement)
            throws ConstructionFailedException {
        currentRecursion.clear();

        if (statement instanceof ConstructorStatement) {
            addConstructor(test, ((ConstructorStatement) statement).getConstructor(),
                    test.size(), 0);
        } else if (statement instanceof MethodStatement) {
            GenericMethod method = ((MethodStatement) statement).getMethod();
            addMethod(test, method, test.size(), 0);
        } else if (statement instanceof PrimitiveStatement<?>) {
            addPrimitive(test, (PrimitiveStatement<?>) statement, test.size());
            // test.statements.add((PrimitiveStatement) statement);
        } else if (statement instanceof FieldStatement) {
            addField(test, ((FieldStatement) statement).getField(), test.size(), 0);
        }
    }

    /**
     * Assign a value to an array index
     *
     * @param test
     * @param array
     * @param arrayIndex
     * @param position
     * @throws ConstructionFailedException
     */
    public void assignArray(TestCase test, VariableReference array, int arrayIndex,
                            int position) throws ConstructionFailedException {
        List<VariableReference> objects = test.getObjects(array.getComponentType(),
                position);
        Iterator<VariableReference> iterator = objects.iterator();
        GenericClass<?> componentClass = GenericClassFactory.get(array.getComponentType());
        // Remove assignments from the same array
        while (iterator.hasNext()) {
            VariableReference var = iterator.next();
            if (var instanceof ArrayIndex) {
                if (((ArrayIndex) var).getArray().equals(array))
                    iterator.remove();
                    // Do not assign values of same type as array to elements
                    // This may e.g. happen if we have Object[], we could otherwise assign Object[] as values
                else if (((ArrayIndex) var).getArray().getType().equals(array.getType()))
                    iterator.remove();
            }
            if (componentClass.isWrapperType()) {
                Class<?> rawClass = ClassUtils.wrapperToPrimitive(componentClass.getRawClass());
                if (!var.getVariableClass().equals(rawClass)
                        && !var.getVariableClass().equals(componentClass.getRawClass())) {
                    iterator.remove();
                }
            }

        }
        logger.debug("Reusable objects: " + objects);
        assignArray(test, array, arrayIndex, position, objects);
    }

    /**
     * Assign a value to an array index for a given set of objects
     *
     * @param test
     * @param array
     * @param arrayIndex
     * @param position
     * @param objects
     * @throws ConstructionFailedException
     */
    protected void assignArray(TestCase test, VariableReference array, int arrayIndex,
                               int position, List<VariableReference> objects)
            throws ConstructionFailedException {
        assert (array instanceof ArrayReference);
        ArrayReference arrRef = (ArrayReference) array;

        if (!objects.isEmpty()
                && Randomness.nextDouble() <= Properties.OBJECT_REUSE_PROBABILITY) {
            // Assign an existing value
            // TODO:
            // Do we need a special "[Array]AssignmentStatement"?
            VariableReference choice = Randomness.choice(objects);
            logger.debug("Reusing value: " + choice);

            ArrayIndex index = new ArrayIndex(test, arrRef, arrayIndex);
            Statement st = new AssignmentStatement(test, index, choice);
            test.addStatement(st, position);
        } else {
            // Assign a new value
            // Need a primitive, method, constructor, or field statement where
            // retval is set to index
            // Need a version of attemptGeneration that takes retval as
            // parameter

            // OR: Create a new variablereference and then assign it to array
            // (better!)
            int oldLength = test.size();
            logger.debug("Attempting generation of object of type "
                    + array.getComponentType());
            VariableReference var = attemptGeneration(test, array.getComponentType(),
                    position);
            // Generics instantiation may lead to invalid types, so better double check
            if (!var.isAssignableTo(arrRef.getComponentType())) {
                throw new ConstructionFailedException("Error");
            }

            position += test.size() - oldLength;
            ArrayIndex index = new ArrayIndex(test, arrRef, arrayIndex);
            Statement st = new AssignmentStatement(test, index, var);
            test.addStatement(st, position);
        }
    }

    /**
     * Attempt to generate a non-null object; initialize recursion level to 0
     */
    public VariableReference attemptGeneration(TestCase test, Type type, int position)
            throws ConstructionFailedException {
        return attemptGeneration(test, type, position, 0, false, null, true, true);
    }


    /**
     * Try to generate an object of a given type
     *
     * @param test
     * @param type
     * @param position
     * @param recursionDepth
     * @param allowNull
     * @return
     * @throws ConstructionFailedException
     */
    protected VariableReference attemptGeneration(TestCase test, Type type, int position,
                                                  int recursionDepth, boolean allowNull, VariableReference generatorRefToExclude,
                                                  boolean canUseMocks, boolean canReuseExistingVariables)
            throws ConstructionFailedException {

        GenericClass<?> clazz = GenericClassFactory.get(type);

        if (clazz.isEnum()) {

            if (!TestUsageChecker.canUse(clazz.getRawClass()))
                throw new ConstructionFailedException("Cannot generate unaccessible enum " + clazz);
            return createPrimitive(test, clazz, position, recursionDepth);

        } else if (clazz.isPrimitive() || clazz.isClass()
                || EnvironmentStatements.isEnvironmentData(clazz.getRawClass())) {

            return createPrimitive(test, clazz, position, recursionDepth);

        } else if (clazz.isString()) {

            if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
                logger.debug("Using a null reference to satisfy the type: {}", type);
                return createNull(test, type, position, recursionDepth);
            } else {
                return createPrimitive(test, clazz, position, recursionDepth);
            }

        } else if (clazz.isArray()) {

            if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
                logger.debug("Using a null reference to satisfy the type: {}", type);
                return createNull(test, type, position, recursionDepth);
            } else {
                return createArray(test, clazz, position, recursionDepth);
            }

        } else {

            if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
                logger.debug("Using a null reference to satisfy the type: {}", type);
                return createNull(test, type, position, recursionDepth);
            }

            ObjectPoolManager objectPool = ObjectPoolManager.getInstance();
            if (Randomness.nextDouble() <= Properties.P_OBJECT_POOL
                    && objectPool.hasSequence(clazz)) {

                TestCase sequence = objectPool.getRandomSequence(clazz);
                logger.debug("Using a sequence from the object pool to satisfy the type: {}", type);
                VariableReference targetObject = sequence.getLastObject(type);
                int returnPos = position + targetObject.getStPosition();

                for (int i = 0; i < sequence.size(); i++) {
                    Statement s = sequence.getStatement(i);
                    test.addStatement(s.copy(test, position), position + i);
                }

                logger.debug("Return type of object sequence: {}",
                        test.getStatement(returnPos).getReturnValue().getClassName());

                return test.getStatement(returnPos).getReturnValue();
            }

            logger.debug("Creating new object for type {}", type);
            return createObject(test, type, position, recursionDepth,
                    generatorRefToExclude, allowNull, canUseMocks, canReuseExistingVariables);
        }
    }

    /**
     * In the given test case {@code test}, tries to generate an object at the specified {@code
     * position} suitable to serve as instance for the class {@code java.lang.Object}. This might
     * be useful when generating tests for "legacy code" before the advent of generics in Java.
     * Such code is likely to use (unsafe) down-casts from {@code Object} to some other subclass.
     * Since {@code Object} is at the root of the type hierarchy the information that something is
     * of type {@code Object} is essentially as valuable as no type information at all. For this
     * reason, this method scans the byte code of the UUT for subsequent down-casts and tries to
     * generate an instance of the subclass being cast to. If {@code allowNull} is {@code true} it
     * is also possible to assign the {@code null} reference.
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the created object of type {@code java.lang.Object}, or throws a
     * {@code ConstructionFailedException} if an error occurred.
     *
     * @param test           the test case in which to insert
     * @param position       the position at which to insert
     * @param recursionDepth the current recursion depth (see above)
     * @param allowNull      whether to allow the creation of  the {@code null} reference
     * @return a reference to the created object
     * @throws ConstructionFailedException if creation fails
     */
    protected VariableReference attemptObjectGeneration(TestCase test, int position,
                                                        int recursionDepth, boolean allowNull) throws ConstructionFailedException {

        if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
            logger.debug("Using a null reference to satisfy the type: {}", Object.class);
            return createNull(test, Object.class, position, recursionDepth);
        }

        Set<GenericClass<?>> castClasses = new LinkedHashSet<>(CastClassManager.getInstance().getCastClasses());
        //needed a copy because hasGenerator(c) does modify that set...
        List<GenericClass<?>> classes = castClasses.stream()
                .filter(c -> TestCluster.getInstance().hasGenerator(c) || c.isString())
                .collect(Collectors.toList());
        classes.add(GenericClassFactory.get(Object.class));

        //TODO if classes is empty, should we use FM here?

        GenericClass<?> choice = Randomness.choice(classes);
        logger.debug("Chosen class for Object: {}", choice);
        if (choice.isString()) {
            return createOrReuseVariable(test, String.class, position,
                    recursionDepth, null, allowNull, false, false);
        }

        GenericAccessibleObject<?> o = TestCluster.getInstance().getRandomGenerator(choice);
        currentRecursion.add(o);

        if (o == null) {

            if (!TestCluster.getInstance().hasGenerator(Object.class)) {
                logger.debug("We have no generator for Object.class ");
            }
            throw new ConstructionFailedException("Generator is null");

        } else if (o.isField()) {

            logger.debug("Attempting generating of Object.class via field of type Object.class");
            VariableReference ret = addField(test, (GenericField) o, position, recursionDepth + 1);
            ret.setDistance(recursionDepth + 1);
            logger.debug("Success in generating type Object.class");
            return ret;

        } else if (o.isMethod()) {

            logger.debug("Attempting generating of Object.class via method {} of type Object.class", o);
            VariableReference ret = addMethod(test, (GenericMethod) o, position, recursionDepth + 1);
            logger.debug("Success in generating type Object.class");
            ret.setDistance(recursionDepth + 1);
            return ret;

        } else if (o.isConstructor()) {

            logger.debug("Attempting generating of Object.class via constructor {} of type Object.class", o);
            VariableReference ret = addConstructor(test, (GenericConstructor) o, position, recursionDepth + 1);
            logger.debug("Success in generating Object.class");
            ret.setDistance(recursionDepth + 1);

            return ret;

        } else {

            logger.debug("No generators found for Object.class");
            throw new ConstructionFailedException("No generator found for Object.class");
        }

    }

    /**
     * Replace the statement with a new statement using given call
     *
     * @param test
     * @param statement
     * @param call
     * @throws ConstructionFailedException
     */
    public void changeCall(TestCase test, Statement statement,
                           GenericAccessibleObject<?> call) throws ConstructionFailedException {
        int position = statement.getReturnValue().getStPosition();

        logger.debug("Changing call {} with {}", test.getStatement(position), call);

        if (call.isMethod()) {
            GenericMethod method = (GenericMethod) call;
            if (method.hasTypeParameters())
                throw new ConstructionFailedException("Cannot handle generic methods properly");

            VariableReference retval = statement.getReturnValue();
            VariableReference callee = null;
            if (!method.isStatic()) {
                callee = getRandomNonNullNonPrimitiveObject(test, method.getOwnerType(), position);
            }

            List<VariableReference> parameters = new ArrayList<>();
            for (Type type : method.getParameterTypes()) {
                parameters.add(test.getRandomObject(type, position));
            }
            MethodStatement m = new MethodStatement(test, method, callee, parameters, retval);
            test.setStatement(m, position);
            logger.debug("Using method {}", m.getCode());

        } else if (call.isConstructor()) {

            GenericConstructor constructor = (GenericConstructor) call;
            VariableReference retval = statement.getReturnValue();
            List<VariableReference> parameters = new ArrayList<>();
            for (Type type : constructor.getParameterTypes()) {
                parameters.add(test.getRandomObject(type, position));
            }
            ConstructorStatement c = new ConstructorStatement(test, constructor, retval, parameters);

            test.setStatement(c, position);
            logger.debug("Using constructor {}", c.getCode());

        } else if (call.isField()) {
            GenericField field = (GenericField) call;
            VariableReference retval = statement.getReturnValue();
            VariableReference source = null;
            if (!field.isStatic())
                source = getRandomNonNullNonPrimitiveObject(test, field.getOwnerType(), position);

            try {
                FieldStatement f = new FieldStatement(test, field, source, retval);
                test.setStatement(f, position);
                logger.debug("Using field {}", f.getCode());
            } catch (Throwable e) {
                logger.error("Error: " + e + " , Field: " + field + " , Test: " + test);
                throw new Error(e);
            }
        }
    }

    private VariableReference getRandomNonNullNonPrimitiveObject(TestCase tc, Type type, int position)
            throws ConstructionFailedException {
        Inputs.checkNull(type);

        List<VariableReference> variables = tc.getObjects(type, position);
        variables.removeIf(var -> var instanceof NullReference
                || tc.getStatement(var.getStPosition()) instanceof PrimitiveStatement
                || var.isPrimitive()
                || var.isWrapperType()
                || tc.getStatement(var.getStPosition()) instanceof FunctionalMockStatement);

        if (variables.isEmpty()) {
            throw new ConstructionFailedException("Found no variables of type " + type
                    + " at position " + position);
        }

        return Randomness.choice(variables);
    }

    public boolean changeRandomCall(TestCase test, Statement statement) {
        logger.debug("Changing statement {}", statement.getCode());

        List<VariableReference> objects = test.getObjects(statement.getReturnValue().getStPosition());
        objects.remove(statement.getReturnValue());

        Iterator<VariableReference> iter = objects.iterator();
        while (iter.hasNext()) {
            VariableReference ref = iter.next();
            //do not use FM as possible callees
            if (test.getStatement(ref.getStPosition()) instanceof FunctionalMockStatement) {
                iter.remove();
                continue;
            }
        }

        // TODO: replacing void calls with other void calls might not be the best idea
        List<GenericAccessibleObject<?>> calls = getPossibleCalls(statement.getReturnType(), objects);

        GenericAccessibleObject<?> ao = statement.getAccessibleObject();
        if (ao != null && ao.getNumParameters() > 0) {
            calls.remove(ao);
        }

        logger.debug("Got {} possible calls for {} objects", calls.size(), objects.size());

        //calls.clear();
        if (calls.isEmpty()) {
            logger.debug("No replacement calls");
            return false;
        }

        GenericAccessibleObject<?> call = Randomness.choice(calls);
        try {
            changeCall(test, statement, call);
            return true;
        } catch (ConstructionFailedException e) {
            // Ignore
            logger.info("Change failed for statement " + statement.getCode() + " -> "
                    + call + ": " + e.getMessage() + " " + test.toCode());
        }
        return false;
    }

    /**
     * In the test case {@code test}, creates a new non-null array of the component type
     * represented by the given {@code arrayClass} at the specified {@code position}.
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the created array, or throws a {@code GenerationFailedException} if
     * generation was unsuccessful.
     *
     * @param test           the test case in which to insert the array
     * @param arrayClass     the component type of the array
     * @param position       the position at which to insert the array
     * @param recursionDepth the current recursion depth (see above)
     * @return a reference to the created array
     * @throws ConstructionFailedException if creation failed
     */
    private VariableReference createArray(TestCase test, GenericClass<?> arrayClass,
                                          int position, int recursionDepth) throws ConstructionFailedException {

        logger.debug("Creating array of type " + arrayClass.getTypeName());
        if (arrayClass.hasWildcardOrTypeVariables()) {
            //if (arrayClass.getComponentClass().isClass()) {
            //	arrayClass = arrayClass.getWithWildcardTypes();
            //} else {
            arrayClass = arrayClass.getGenericInstantiation();
            logger.debug("Setting generic array to type " + arrayClass.getTypeName());
            //}
        }
        // Create array with random size
        ArrayStatement statement = new ArrayStatement(test, arrayClass.getType());
        VariableReference reference = test.addStatement(statement, position);
        position++;
        logger.debug("Array length: " + statement.size());
        logger.debug("Array component type: " + reference.getComponentType());

        // For each value of array, call attemptGeneration
        List<VariableReference> objects = test.getObjects(reference.getComponentType(),
                position);

        // Don't assign values to other values in the same array initially
        Iterator<VariableReference> iterator = objects.iterator();
        while (iterator.hasNext()) {
            VariableReference current = iterator.next();
            if (current instanceof ArrayIndex) {
                ArrayIndex index = (ArrayIndex) current;
                if (index.getArray().equals(statement.getReturnValue()))
                    iterator.remove();
                    // Do not assign values of same type as array to elements
                    // This may e.g. happen if we have Object[], we could otherwise assign Object[] as values
                else if (index.getArray().getType().equals(arrayClass.getType()))
                    iterator.remove();

            }
        }

        objects.remove(statement.getReturnValue());
        logger.debug("Found assignable objects: " + objects.size());
        Set<GenericAccessibleObject<?>> currentArrayRecursion = new LinkedHashSet<>(currentRecursion);

        for (int i = 0; i < statement.size(); i++) {
            currentRecursion.clear();
            currentRecursion.addAll(currentArrayRecursion);
            logger.debug("Assigning array index " + i);
            int oldLength = test.size();
            assignArray(test, reference, i, position, objects);
            position += test.size() - oldLength;
        }
        reference.setDistance(recursionDepth);
        return reference;
    }

    /**
     * In the given test case {@code test} at the specified {@code position}, creates and returns a
     * new variable of the primitive or "simple data object" data type represented by {@code clazz}.
     * In detail, the following data types are accepted:
     * <ul>
     *     <li>all primitive data types ({@code byte}, {@code short}, {@code int}, {@code long},
     *     {@code float}, {@code double}, {@code boolean}, {@code char}),</li>
     *     <li>{@code String}s,</li>
     *     <li>enumeration types ("enums"),</li>
     *     <li>EvoSuite environment data types as defined in
     *     {@link org.evosuite.runtime.testdata.EnvironmentDataList EnvironmentDataList}, and</li>
     *     <li>class primitives ({@code Class.class}).</li>
     * </ul>
     * The {@code null} reference and arrays receive special treatment by their own dedicated
     * methods, {@code createNull} and {@code createArray}.
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the created primitive value, or throws a
     * {@code ConstructionFailedException} if creation is not possible.
     *
     * @param test           the test case for which to create the variable
     * @param clazz          the primitive data type of the variable to create (see above)
     * @param position       the position at which to insert the created variable
     * @param recursionDepth the current recursion depth (see above)
     * @return a reference to the created variable
     * @throws ConstructionFailedException if variable creation is not possible
     */
    private VariableReference createPrimitive(TestCase test, GenericClass<?> clazz,
                                              int position, int recursionDepth) throws ConstructionFailedException {
        // Special case: we cannot instantiate Class<Class<?>>
        if (clazz.isClass()) {
            if (clazz.hasWildcardOrTypeVariables()) {
                logger.debug("Getting generic instantiation of class");
                clazz = clazz.getGenericInstantiation();
                logger.debug("Chosen: " + clazz);
            }
            Type parameterType = clazz.getParameterTypes().get(0);
            if (!(parameterType instanceof WildcardType) && GenericTypeReflector.erase(parameterType).equals(Class.class)) {
                throw new ConstructionFailedException(
                        "Cannot instantiate a class with a class");
            }
        }
        Statement st = PrimitiveStatement.getRandomStatement(test, clazz, position);
        VariableReference ret = test.addStatement(st, position);
        ret.setDistance(recursionDepth);
        return ret;
    }

    /**
     * Creates a new {@code null} variable of the given {@code type} at the given {@code position}
     * in the {@code test} case.
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the inserted {@code null} variable. If the creation of the variable
     * fails a {@code ConstructionFailedException} is thrown.
     *
     * @param test           the test case for which to create the {@code null} variable
     * @param type           represents the type of the variable to create
     * @param position       the position in {@code test} at which to insert
     * @param recursionDepth the current recursion depth
     * @return a reference to the inserted {@code null} variable
     * @throws ConstructionFailedException if the creation of the variable fails
     */
    private VariableReference createNull(TestCase test, Type type, int position,
                                         int recursionDepth) throws ConstructionFailedException {
        GenericClass<?> genericType = GenericClassFactory.get(type);

        // For example, HashBasedTable.Factory in Guava is private but used as a parameter
        // in a public method. This would lead to compile errors
        if (!TestUsageChecker.canUse(genericType.getRawClass())) {
            throw new ConstructionFailedException("Cannot use class " + type);
        }
        if (genericType.hasWildcardOrTypeVariables()) {
            type = genericType.getGenericInstantiation().getType();
        }
        Statement st = new NullStatement(test, type);
        test.addStatement(st, position);
        VariableReference ret = test.getStatement(position).getReturnValue();
        ret.setDistance(recursionDepth);
        return ret;
    }


    /**
     * Creates a new object of the given complex (i.e. non-primitive) {@code type} and adds it to
     * the {@code test} case at the desired {@code position}. If the test case already contains an
     * object of the specified type, this method might simply return a reference to the already
     * existing object. Also, the insertion of a {@code null} reference is possible. The decision
     * about which action to take is made probabilistically.
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the created object or throws a {@code ConstructionFailedException} if
     * generation was not possible.
     *
     * @param test                  the test case for which to create the object
     * @param type                  represents the type of the object to create
     * @param position              the position in {@code test} at which to insert the reference to the object
     * @param recursionDepth        the current recursion depth (see above)
     * @param generatorRefToExclude
     * @return a reference to the generated object
     * @throws ConstructionFailedException if generation was not possible
     */
    public VariableReference createObject(TestCase test, Type type, int position,
                                          int recursionDepth, VariableReference generatorRefToExclude) throws ConstructionFailedException {
        return createObject(test, type, position, recursionDepth, generatorRefToExclude, true, true, true);
    }

    /**
     * Creates a new object of the given complex data type {@code type} (i.e., extending
     * {@code java.lang.Object}) and adds it to the {@code test} case at the desired {@code
     * position}. The following parameters allow clients to tweak the generation process:
     * <ul>
     *     <li>If {@code allowNull} is set to {@code true} the creation of {@code null} references
     *     is possible.</li>
     *     <li>If {@code canUseFunctionalMocks} is set to {@code true} the creation of mocks is
     *     permitted.</li>
     *     <li>If {@code canReuseVariables} is set to {@code true} the method is allowed to
     *     return a reference to an already existing object of matching {@code type}.</li>
     * </ul>
     * If one wants to create {@code null} references specifically, the corresponding method
     * {@code createNull} should be used instead. If one wants to create arrays, the corresponding
     * method {@code createArray} should be used.
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns the reference to the created object or throws a {@code ConstructionFailedException}
     * if creation was not possible.
     *
     * @param test                  the test case in which to insert
     * @param type                  the type of the object to create
     * @param position              the position at which to insert the created object
     * @param recursionDepth        the current recursion depth (see above)
     * @param generatorRefToExclude
     * @param allowNull             whether to allow the creation of {@code null} objects
     * @param canUseFunctionalMocks whether to allow the creation of mocks
     * @param canReuseVariables     whether to allow the reuse of already existing objects of
     *                              matching {@code type}
     * @return a reference to the created object
     * @throws ConstructionFailedException if creation failed
     */
    public VariableReference createObject(TestCase test, Type type, int position,
                                          int recursionDepth, VariableReference generatorRefToExclude,
                                          boolean allowNull, boolean canUseFunctionalMocks,
                                          boolean canReuseVariables) throws ConstructionFailedException {
        GenericClass<?> clazz = GenericClassFactory.get(type);

        logger.debug("Going to create object for type {}", type);
        VariableReference ret = null;

        if (canUseFunctionalMocks && TimeController.getInstance().getPhasePercentage() >= Properties.FUNCTIONAL_MOCKING_PERCENT &&
                Randomness.nextDouble() < Properties.P_FUNCTIONAL_MOCKING &&
                FunctionalMockStatement.canBeFunctionalMocked(type)) {

            //mock creation
            logger.debug("Creating functional mock for {}", type);
            ret = addFunctionalMock(test, type, position, recursionDepth + 1);

        } else {

            //regular creation

            GenericAccessibleObject<?> o = TestCluster.getInstance().getRandomGenerator(
                    clazz, currentRecursion, test, position, generatorRefToExclude, recursionDepth);
            currentRecursion.add(o);

            if (o == null) {
                if (canReuseVariables) {
//					throw new ConstructionFailedException("Cannot currently instantiate type "+type);

				/*
					It could happen that there is no current valid generator for 'position', but valid
					generators were usable before. This is for example the case when the only generator
					has an "atMostOnce" constraint, and so can only be used once.
					In such case, we should just re-use an existing variable if it exists, as long as
					it is not a functional mock (which can be used only once)
				 */
                    for (int i = position - 1; i >= 0; i--) {
                        Statement statement = test.getStatement(i);
                        VariableReference var = statement.getReturnValue();

                        if (!allowNull && ConstraintHelper.isNull(var, test)) {
                            continue;
                        }

                        if (var.isAssignableTo(type) && !(statement instanceof FunctionalMockStatement)) {

                            // Workaround for https://issues.apache.org/jira/browse/LANG-1420
                            if (!clazz.getRawClass().isAssignableFrom(var.getGenericClass().getRawClass())) {
                                continue;
                            }
                            logger.debug("Reusing variable at position {}", var.getStPosition());
                            return var;
                        }
                    }
                }

                if (canUseFunctionalMocks && (Properties.MOCK_IF_NO_GENERATOR || Properties.P_FUNCTIONAL_MOCKING > 0)) {
					/*
						Even if mocking is not active yet in this phase, if we have
						no generator for a type, we use mocking directly
				 	*/
                    if (FunctionalMockStatement.canBeFunctionalMocked(type)) {
                        logger.debug("Using mock for type {}", type);
                        ret = addFunctionalMock(test, type, position, recursionDepth + 1);
                    } else if (clazz.isAbstract() && FunctionalMockStatement.canBeFunctionalMockedIncludingSUT(type)) {
                        {
                            logger.debug("Using mock for abstract type {}", type);
                            ret = addFunctionalMockForAbstractClass(test, type, position, recursionDepth + 1);
                        }
                    }
                }
                if (ret == null) {
                    logger.debug("No mock solution found: {}, {}, {}, {}", canUseFunctionalMocks, Properties.MOCK_IF_NO_GENERATOR, FunctionalMockStatement.canBeFunctionalMocked(type), FunctionalMockStatement.canBeFunctionalMockedIncludingSUT(type));

                    if (!TestCluster.getInstance().hasGenerator(type)) {
                        logger.debug("No generators found for {}, attempting to resolve dependencies", type);
                        TestClusterGenerator clusterGenerator = TestGenerationContext.getInstance().getTestClusterGenerator();
                        Class<?> mock = MockList.getMockClass(clazz.getRawClass().getCanonicalName());
                        if (mock != null) {
                            clusterGenerator.addNewDependencies(Collections.singletonList(mock));
                        } else {
                            clusterGenerator.addNewDependencies(Collections.singletonList(clazz.getRawClass()));
                        }

                        if (TestCluster.getInstance().hasGenerator(type)) {
                            logger.debug("Found new generators for {}", type);
                            return createObject(test, type, position, recursionDepth + 1, generatorRefToExclude, allowNull, canUseFunctionalMocks, canReuseVariables);
                        } else {
                            logger.debug("Found no new generators for {}", type);
                        }
                    }
                    throw new ConstructionFailedException("Have no generator for " + type + " canUseFunctionalMocks=" + canUseFunctionalMocks + ", canBeMocked: " + FunctionalMockStatement.canBeFunctionalMocked(type));
                }

            } else if (o.isField()) {
                logger.debug("Attempting generating of {} via field of type {}", type, type);
                ret = addField(test, (GenericField) o, position, recursionDepth + 1);
            } else if (o.isMethod()) {
                logger.debug("Attempting generating of " + type + " via method " + (o) + " of type " + type);

                ret = addMethod(test, (GenericMethod) o, position, recursionDepth + 1);

                // TODO: Why are we doing this??
                //if (o.isStatic()) {
                //	ret.setType(type);
                //}
                logger.debug("Success in generating type {} using method \"{}\"", type, o);
            } else if (o.isConstructor()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting generating of " + type + " via constructor " + (o)
                            + " of type " + type + ", with constructor type " + o.getOwnerType() +
                            ", at position " + position);
                }

                ret = addConstructor(test, (GenericConstructor) o, type, position, recursionDepth + 1);
            } else {
                logger.debug("No generators found for type {}", type);
                throw new ConstructionFailedException("No generator found for type " + type);
            }
        }

        ret.setDistance(recursionDepth + 1);
        logger.debug("Success in generation of type {} at position {}", type, position);
        return ret;
    }


    /**
     * In the given {@code test} case, tries to create a new variable of type {@code parameterType}
     * at the given {@code position} or reuse an existing variable of matching type.
     *
     * @param test
     * @param parameterType
     * @param position
     * @param recursionDepth
     * @param exclude
     * @return
     * @throws ConstructionFailedException
     */
    private VariableReference createOrReuseVariable(TestCase test, Type parameterType,
                                                    int position, int recursionDepth, VariableReference exclude, boolean allowNull,
                                                    boolean excludeCalleeGenerators, boolean canUseMocks)
            throws ConstructionFailedException {

        if (Properties.SEED_TYPES && parameterType.equals(Object.class)) {
            return createOrReuseObjectVariable(test, position, recursionDepth, exclude, allowNull, canUseMocks);
        }

        double reuse = Randomness.nextDouble();

        List<VariableReference> objects = getCandidatesForReuse(test, parameterType, position, exclude, allowNull, canUseMocks);

        GenericClass<?> clazz = GenericClassFactory.get(parameterType);
        boolean isPrimitiveOrSimilar = clazz.isPrimitive() || clazz.isWrapperType() || clazz.isEnum() || clazz.isClass() || clazz.isString();

        if (isPrimitiveOrSimilar && !objects.isEmpty() && reuse <= Properties.PRIMITIVE_REUSE_PROBABILITY) {
            logger.debug(" Looking for existing object of type {}", parameterType);
            VariableReference reference = Randomness.choice(objects);
            return reference;

        } else if (!isPrimitiveOrSimilar && !objects.isEmpty() && (reuse <= Properties.OBJECT_REUSE_PROBABILITY)) {

            if (logger.isDebugEnabled()) {
                logger.debug(" Choosing from {} existing objects: {}", objects.size(), Arrays.toString(objects.toArray()));
            }
            VariableReference reference = Randomness.choice(objects);
            //logger.debug(" Using existing object of type {}: {}", parameterType, reference);
            return reference;
        }

        //if chosen to not re-use existing variable, try create a new one
        VariableReference created = createVariable(test, parameterType,
                position, recursionDepth, exclude, allowNull,
                excludeCalleeGenerators, canUseMocks, true);
        if (created != null) {
            return created;
        }

        //could not create, so go back in trying to re-use an existing variable
        if (objects.isEmpty()) {
            if (allowNull) {
                return createNull(test, parameterType, position, recursionDepth);
            } else {
                throw new ConstructionFailedException("No objects and generators for type " + parameterType);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(" Choosing from {} existing objects: {}", objects.size(), Arrays.toString(objects.toArray()));
        }
        VariableReference reference = Randomness.choice(objects);
        assert canUseMocks || !(test.getStatement(reference.getStPosition()) instanceof FunctionalMockStatement);
        logger.debug(" Using existing object of type {}: {}", parameterType, reference);
        return reference;
    }

    /**
     * In the given {@code test} case, tries to create a variable of the type represented by
     * {@code parameterType} at the specified {@code position}. Clients can tweak the creation
     * process using the following parameters:
     * <ul>
     *     <li>If {@code allowNull} is set to {@code true} the generation of {@code null} objects
     *     is possible. Only applies if {@code parameterType} represents a non-primitive type.</li>
     *     <li>If {@code canUseMocks} is set to {@code true} the generation of mocks for the
     *     specified {@code parameterType} is possible.</li>
     *     <li>If {@code canReuseExistingVariables} is set to {@code true} the method is
     *     allowed to return a reference to an already existing object of the given type
     *     instead of generating a new one. The given {@code position} is ignored in this case.</li>
     * </ul>
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     *
     * @param test                      the test case for which to create a new variable
     * @param parameterType             represents the type of the variable to create
     * @param position                  the desired position for the insertion of the variable
     * @param recursionDepth            the current recursion depth (see above)
     * @param exclude
     * @param allowNull                 whether to allow the generation of {@code null} variables
     * @param excludeCalleeGenerators
     * @param canUseMocks               whether to allow the generation of mocks
     * @param canReuseExistingVariables whether to allow the reuse of already existing variables
     * @return a reference to the created variable
     * @throws ConstructionFailedException if creation of the variable failed
     */
    private VariableReference createVariable(TestCase test, Type parameterType,
                                             int position, int recursionDepth, VariableReference exclude, boolean allowNull,
                                             boolean excludeCalleeGenerators, boolean canUseMocks, boolean canReuseExistingVariables)
            throws ConstructionFailedException {

        GenericClass<?> clazz = GenericClassFactory.get(parameterType);

        if (clazz.hasWildcardOrTypeVariables()) {
            logger.debug("Getting generic instantiation of {}", clazz);
            if (exclude != null)
                clazz = clazz.getGenericInstantiation(exclude.getGenericClass().getTypeVariableMap());
            else
                clazz = clazz.getGenericInstantiation();
            parameterType = clazz.getType();
        }


        if (clazz.isEnum() || clazz.isPrimitive() || clazz.isWrapperType() || clazz.isObject() ||
                clazz.isClass() || EnvironmentStatements.isEnvironmentData(clazz.getRawClass()) ||
                clazz.isString() || clazz.isArray() || TestCluster.getInstance().hasGenerator(parameterType) ||
                Properties.P_FUNCTIONAL_MOCKING > 0 || Properties.MOCK_IF_NO_GENERATOR) {

            logger.debug(" Generating new object of type {}", parameterType);

            //FIXME exclude methods
            VariableReference generatorRefToExclude = null;
            if (excludeCalleeGenerators) {
                generatorRefToExclude = exclude;
            }
            VariableReference reference = attemptGeneration(test, parameterType,
                    position, recursionDepth,
                    allowNull, generatorRefToExclude, canUseMocks, canReuseExistingVariables);

            assert !(!allowNull && ConstraintHelper.isNull(reference, test));
            assert canUseMocks || !(test.getStatement(reference.getStPosition()) instanceof FunctionalMockStatement);

            return reference;
        }

        return null;
    }

    private List<VariableReference> getCandidatesForReuse(TestCase test, Type parameterType, int position, VariableReference exclude,
                                                          boolean allowNull, boolean canUseMocks) {

        //look at all vars defined before pos
        List<VariableReference> objects = test.getObjects(parameterType, position);

        //if an exclude var was specified, then remove it
        if (exclude != null) {
            objects.remove(exclude);
            if (exclude.getAdditionalVariableReference() != null)
                objects.remove(exclude.getAdditionalVariableReference());

            objects.removeIf(v -> exclude.equals(v.getAdditionalVariableReference()));
        }

        List<VariableReference> additionalToRemove = new ArrayList<>();

        //no mock should be used more than once
        Iterator<VariableReference> iter = objects.iterator();
        while (iter.hasNext()) {
            VariableReference ref = iter.next();
            if (!(test.getStatement(ref.getStPosition()) instanceof FunctionalMockStatement)) {
                continue;
            }

            //check if current mock var is used anywhere: if so, then we cannot choose it
            for (int i = ref.getStPosition() + 1; i < test.size(); i++) {
                Statement st = test.getStatement(i);
                if (st.getVariableReferences().contains(ref)) {
                    iter.remove();
                    additionalToRemove.add(ref);
                    break;
                }
            }
        }

        //check for null
        if (!allowNull) {
            iter = objects.iterator();
            while (iter.hasNext()) {
                VariableReference ref = iter.next();

                if (ConstraintHelper.isNull(ref, test)) {
                    iter.remove();
                    additionalToRemove.add(ref);
                }
            }
        }

        //check for mocks
        if (!canUseMocks) {
            iter = objects.iterator();
            while (iter.hasNext()) {
                VariableReference ref = iter.next();

                if (test.getStatement(ref.getStPosition()) instanceof FunctionalMockStatement) {
                    iter.remove();
                    additionalToRemove.add(ref);
                }
            }
        }

        //further remove all other vars that have the deleted ones as additionals
        iter = objects.iterator();
        while (iter.hasNext()) {
            VariableReference ref = iter.next();
            VariableReference additional = ref.getAdditionalVariableReference();
            if (additional == null) {
                continue;
            }
            if (additionalToRemove.contains(additional)) {
                iter.remove();
            }
        }

        //avoid using characters as values for numeric types arguments
        iter = objects.iterator();
        String parCls = parameterType.getTypeName();
        if (Integer.TYPE.getTypeName().equals(parCls) || Long.TYPE.getTypeName().equals(parCls)
                || Float.TYPE.getTypeName().equals(parCls) || Double.TYPE.getTypeName().equals(parCls)) {
            while (iter.hasNext()) {
                VariableReference ref = iter.next();
                String cls = ref.getType().getTypeName();
                if ((Character.TYPE.getTypeName().equals(cls)))
                    iter.remove();
            }
        }

        return objects;
    }

    /**
     * In the given test case {@code test}, tries to insert a reference to an object compatible with
     * {@code java.lang.Object} at the desired {@code position}. This method is specifically
     * intended to create or reuse a variable that can be assigned to {@code java.lang.Object}.
     * For any other type, {@code createOrReuseVariable} should be used instead.
     * <p>
     * Source code using {@code Object} often dates back to pre-generic versions of Java. As such,
     * it was necessary to specify {@code Object} as data type for parameters or variables and use
     * (unsafe) downcasts if polymorphism was desired. The inherent drawback was the circumvention
     * of the type system and thus the loss of static type information, among others. This poses a
     * great challenge for test generation. In an attempt to tackle this challenge, this method
     * scans the byte code for subsequent downcasts, and only returns references to objects of the
     * type being downcast to. This is more likely to yield tests that don't fail at runtime due to
     * casting errors.
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the created variable, or throws a {@code ConstructionFailedException}
     * if creation failed.
     *
     * @param test           the test in which to insert
     * @param position       the position at which to insert
     * @param recursionDepth the current recursion depth (see above)
     * @param exclude
     * @param allowNull      whether to allow the assignment of {@code null} to the created variable
     * @param canUseMocks    whether to allow mocks on the right-hand side for the created variable
     * @return a reference to the created variable
     * @throws ConstructionFailedException if creation fails
     */
    private VariableReference createOrReuseObjectVariable(TestCase test, int position,
                                                          int recursionDepth, VariableReference exclude, boolean allowNull, boolean canUseMocks)
            throws ConstructionFailedException {
        final boolean reuse = Randomness.nextDouble() <= Properties.PRIMITIVE_REUSE_PROBABILITY;
        if (reuse) { // Only reuse objects if they are related to a target call
            List<VariableReference> candidates = getCandidatesForReuse(test, Object.class, position, exclude, allowNull, canUseMocks);
            //List<VariableReference> candidates = test.getObjects(Object.class, position);
            filterVariablesByCastClasses(candidates);
            //filterVariablesByClass(candidates, Object.class);
            logger.debug("Choosing object from: {}", candidates);
            if (!candidates.isEmpty())
                return Randomness.choice(candidates);
        }
        logger.debug("Attempting object generation");

        return attemptObjectGeneration(test, position, recursionDepth, allowNull);

    }

    /**
     * Delete the statement at position from the test case and remove all
     * references to it
     *
     * @param test
     * @param position
     * @return false if it was not possible to delete the statement
     * @throws ConstructionFailedException
     */
    public boolean deleteStatement(TestCase test, int position) {

        logger.debug("Deleting target statement - {}", position);

        Set<Integer> toDelete = new LinkedHashSet<>();
        recursiveDeleteInclusion(test, toDelete, position);

        List<Integer> pos = new ArrayList<>(toDelete);
        pos.sort(Collections.reverseOrder());

        for (int i : pos) {
            logger.debug("Deleting statement: {}", i);
            test.remove(i);
        }

        return true;
    }

    private void recursiveDeleteInclusion(TestCase test, Set<Integer> toDelete, int position) {

        if (toDelete.contains(position)) {
            return; //end of recursion
        }

        toDelete.add(position);

        Set<Integer> references = getReferencePositions(test, position);

		/*
			it can happen that we can delete the target statements but, when we look at
			the other statements using it, then we could not delete them :(
			in those cases, we have to recursively look at all their dependencies.
		 */

        for (int i : references) {
            recursiveDeleteInclusion(test, toDelete, i);
        }
    }

    private Set<Integer> getReferencePositions(TestCase test, int position) {
        Set<VariableReference> references = new LinkedHashSet<>();
        Set<Integer> positions = new LinkedHashSet<>();
        references.add(test.getReturnValue(position));

        for (int i = position; i < test.size(); i++) {
            Set<VariableReference> temp = new LinkedHashSet<>();
            for (VariableReference v : references) {
                if (test.getStatement(i).references(v)) {
                    temp.add(test.getStatement(i).getReturnValue());
                    positions.add(i);
                }
            }
            references.addAll(temp);
        }
        return positions;
    }

    private static void filterVariablesByCastClasses(Collection<VariableReference> variables) {
        // Remove invalid classes if this is an Object.class reference
        Set<GenericClass<?>> castClasses = CastClassManager.getInstance().getCastClasses();
        Iterator<VariableReference> replacement = variables.iterator();
        while (replacement.hasNext()) {
            VariableReference r = replacement.next();
            boolean isAssignable = false;
            for (GenericClass<?> clazz : castClasses) {
                if (r.isPrimitive())
                    continue;
                if (clazz.isAssignableFrom(r.getVariableClass())) {
                    isAssignable = true;
                    break;
                }
            }
            if (!isAssignable && !r.getVariableClass().equals(Object.class))
                replacement.remove();
        }
    }


    private static void filterVariablesByClass(Collection<VariableReference> variables, Class<?> clazz) {
        // Remove invalid classes if this is an Object.class reference
        variables.removeIf(r -> !r.getVariableClass().equals(clazz));
    }


    /**
     * @param test
     * @param position
     * @return true if statements was deleted or any dependency was modified
     * @throws ConstructionFailedException
     */
    public boolean deleteStatementGracefully(TestCase test, int position)
            throws ConstructionFailedException {
        VariableReference var = test.getReturnValue(position);

        if (var instanceof ArrayIndex) {
            return deleteStatement(test, position);
        }

        boolean changed = false;

        boolean replacingPrimitive = test.getStatement(position) instanceof PrimitiveStatement;

        // Get possible replacements
        List<VariableReference> alternatives = test.getObjects(var.getType(), position);

        int maxIndex = 0;
        if (var instanceof ArrayReference) {
            maxIndex = ((ArrayReference) var).getMaximumIndex();
        }

        // Remove invalid classes if this is an Object.class reference
        if (test.getStatement(position) instanceof MethodStatement) {
            MethodStatement ms = (MethodStatement) test.getStatement(position);
            if (ms.getReturnType().equals(Object.class)) {
                //				filterVariablesByClass(alternatives, var.getVariableClass());
                filterVariablesByClass(alternatives, Object.class);
            }
        } else if (test.getStatement(position) instanceof ConstructorStatement) {
            ConstructorStatement cs = (ConstructorStatement) test.getStatement(position);
            if (cs.getReturnType().equals(Object.class)) {
                filterVariablesByClass(alternatives, Object.class);
            }
        }

        // Remove self, and all field or array references to self
        alternatives.remove(var);
        Iterator<VariableReference> replacement = alternatives.iterator();
        while (replacement.hasNext()) {
            VariableReference r = replacement.next();
            if (test.getStatement(r.getStPosition()) instanceof FunctionalMockStatement) {
                // we should ensure that a FM should never be a callee
                replacement.remove();
            } else if (var.equals(r.getAdditionalVariableReference())) {
                replacement.remove();
            } else if (var.isFieldReference()) {
                FieldReference fref = (FieldReference) var;
                if (fref.getField().isFinal()) {
                    replacement.remove();
                }
            } else if (r instanceof ArrayReference) {
                if (maxIndex >= ((ArrayReference) r).getArrayLength())
                    replacement.remove();
            } else if (!replacingPrimitive) {
                if (test.getStatement(r.getStPosition()) instanceof PrimitiveStatement) {
                    replacement.remove();
                }
            }
        }

        if (!alternatives.isEmpty()) {
            // Change all references to return value at position to something else
            for (int i = position + 1; i < test.size(); i++) {
                Statement s = test.getStatement(i);
                if (s.references(var)) {
                    if (s.isAssignmentStatement()) {
                        AssignmentStatement assignment = (AssignmentStatement) s;
                        if (assignment.getValue() == var) {
                            VariableReference replacementVar = Randomness.choice(alternatives);
                            if (assignment.getReturnValue().isAssignableFrom(replacementVar)) {
                                s.replace(var, replacementVar);
                                changed = true;
                            }
                        } else if (assignment.getReturnValue() == var) {
                            VariableReference replacementVar = Randomness.choice(alternatives);
                            if (replacementVar.isAssignableFrom(assignment.getValue())) {
                                s.replace(var, replacementVar);
                                changed = true;
                            }
                        }
                    } else {
                        s.replace(var, Randomness.choice(alternatives));
                        changed = true;
                    }
                }
            }
        }

        if (var instanceof ArrayReference) {
            alternatives = test.getObjects(var.getComponentType(), position);
            // Remove self, and all field or array references to self
            alternatives.remove(var);
            replacement = alternatives.iterator();
            while (replacement.hasNext()) {
                VariableReference r = replacement.next();
                if (var.equals(r.getAdditionalVariableReference()))
                    replacement.remove();
                else if (r instanceof ArrayReference) {
                    if (maxIndex >= ((ArrayReference) r).getArrayLength())
                        replacement.remove();
                }
            }
            if (!alternatives.isEmpty()) {
                // Change all references to return value at position to something else
                for (int i = position; i < test.size(); i++) {
                    Statement s = test.getStatement(i);
                    for (VariableReference var2 : s.getVariableReferences()) {
                        if (var2 instanceof ArrayIndex) {
                            ArrayIndex ai = (ArrayIndex) var2;
                            if (ai.getArray().equals(var)) {
                                s.replace(var2, Randomness.choice(alternatives));
                                changed = true;
                            }
                        }
                    }
                }
            }
        }

        // Remove everything else
        boolean deleted = deleteStatement(test, position);
        return deleted || changed;
    }

    /**
     * Determine if the set of objects is sufficient to satisfy the set of
     * dependencies
     *
     * @param dependencies
     * @param objects
     * @return
     */
    private static boolean dependenciesSatisfied(Set<Type> dependencies,
                                                 List<VariableReference> objects) {
        for (Type type : dependencies) {
            boolean found = false;
            for (VariableReference var : objects) {
                if (var.getType().equals(type)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                return false;
        }
        return true;
    }

    /**
     * Retrieve the dependencies for a constructor
     *
     * @param constructor
     * @return
     */
    private static Set<Type> getDependencies(GenericConstructor constructor) {
        return new LinkedHashSet<>(Arrays.asList(constructor.getParameterTypes()));
    }

    /**
     * Retrieve the dependencies for a field
     *
     * @param field
     * @return
     */
    private static Set<Type> getDependencies(GenericField field) {
        Set<Type> dependencies = new LinkedHashSet<>();
        if (!field.isStatic()) {
            dependencies.add(field.getOwnerType());
        }

        return dependencies;
    }

    /**
     * Retrieve the dependencies for a method
     *
     * @param method
     * @return
     */
    private static Set<Type> getDependencies(GenericMethod method) {
        Set<Type> dependencies = new LinkedHashSet<>();
        if (!method.isStatic()) {
            dependencies.add(method.getOwnerType());
        }
        dependencies.addAll(Arrays.asList(method.getParameterTypes()));

        return dependencies;
    }

    /**
     * Retrieve all the replacement calls that can be inserted at this position
     * without changing the length
     *
     * @param returnType
     * @param objects
     * @return
     */
    private List<GenericAccessibleObject<?>> getPossibleCalls(Type returnType,
                                                              List<VariableReference> objects) {
        List<GenericAccessibleObject<?>> calls = new ArrayList<>();
        Set<GenericAccessibleObject<?>> allCalls;

        try {
            allCalls = TestCluster.getInstance().getGenerators(GenericClassFactory.get(
                    returnType));
        } catch (ConstructionFailedException e) {
            return calls;
        }

        for (GenericAccessibleObject<?> call : allCalls) {
            Set<Type> dependencies = null;
            if (call.isMethod()) {
                GenericMethod method = (GenericMethod) call;
                if (method.hasTypeParameters()) {
                    try {
                        call = method.getGenericInstantiation(GenericClassFactory.get(returnType));
                    } catch (ConstructionFailedException e) {
                        continue;
                    }
                }
                if (!((GenericMethod) call).getReturnType().equals(returnType))
                    continue;
                dependencies = getDependencies((GenericMethod) call);
            } else if (call.isConstructor()) {
                dependencies = getDependencies((GenericConstructor) call);
            } else if (call.isField()) {
                if (!((GenericField) call).getFieldType().equals(returnType))
                    continue;
                dependencies = getDependencies((GenericField) call);
            } else {
                assert (false);
            }
            if (dependenciesSatisfied(dependencies, objects)) {
                calls.add(call);
            }
        }

        // TODO: What if primitive?

        return calls;
    }


    private boolean insertRandomReflectionCall(TestCase test, int position, int recursionDepth)
            throws ConstructionFailedException {

        logger.debug("Recursion depth: " + recursionDepth);
        if (recursionDepth > Properties.MAX_RECURSION) {
            logger.debug("Max recursion depth reached");
            throw new ConstructionFailedException("Max recursion depth reached");
        }

        int length = test.size();
        List<VariableReference> parameters = null;
        Statement st = null;

        if (reflectionFactory.nextUseField()) {
            Field field = reflectionFactory.nextField();
            parameters = satisfyParameters(test, null,
                    //we need a reference to the SUT, and one to a variable of same type of chosen field
                    Arrays.asList(reflectionFactory.getReflectedClass(), field.getType()), null,
                    position, recursionDepth + 1, true, false, true);

            try {
                st = new PrivateFieldStatement(test, reflectionFactory.getReflectedClass(), field.getName(),
                        parameters.get(0), parameters.get(1));
            } catch (NoSuchFieldException e) {
                logger.error("Reflection problem: " + e, e);
                throw new ConstructionFailedException("Reflection problem");
            }
        } else {
            //method
            Method method = reflectionFactory.nextMethod();
            List<Type> list = new ArrayList<>();
            list.add(reflectionFactory.getReflectedClass());
            list.addAll(Arrays.asList(method.getGenericParameterTypes()));

            // Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
            parameters = satisfyParameters(test, null, list, null, position, recursionDepth + 1, true, false, true);
            VariableReference callee = parameters.remove(0);

            st = new PrivateMethodStatement(test, reflectionFactory.getReflectedClass(), method,
                    callee, parameters, Modifier.isStatic(method.getModifiers()));
        }

        int newLength = test.size();
        position += (newLength - length);

        test.addStatement(st, position);
        return true;
    }

    private boolean insertRandomReflectionCallOnObject(TestCase test, VariableReference callee, int position, int recursionDepth)
            throws ConstructionFailedException {

        logger.debug("Recursion depth: " + recursionDepth);
        if (recursionDepth > Properties.MAX_RECURSION) {
            logger.debug("Max recursion depth reached");
            throw new ConstructionFailedException("Max recursion depth reached");
        }

        if (!reflectionFactory.getReflectedClass().isAssignableFrom(callee.getVariableClass())) {
            logger.debug("Reflection not performed on class {}", callee.getVariableClass());
            return false;
        }

        int length = test.size();
        List<VariableReference> parameters = null;
        Statement st = null;

        if (reflectionFactory.nextUseField()) {
            Field field = reflectionFactory.nextField();

			/*
				In theory, there might be cases in which using null in PA might help increasing
				coverage. However, likely most of the time we ll end up in useless tests throwing
				NPE on the private fields. As we maximize the number of methods throwing exceptions,
				we could end up with a lot of useless tests
			 */
            boolean allowNull = false;

            // Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
            parameters = satisfyParameters(test, callee,
                    //we need a reference to the SUT, and one to a variable of same type of chosen field
                    Collections.singletonList(field.getType()), null, position, recursionDepth + 1, allowNull, false, true);

            try {
                st = new PrivateFieldStatement(test, reflectionFactory.getReflectedClass(), field.getName(),
                        callee, parameters.get(0));
            } catch (NoSuchFieldException e) {
                logger.error("Reflection problem: " + e, e);
                throw new ConstructionFailedException("Reflection problem");
            }
        } else {
            //method
            Method method = reflectionFactory.nextMethod();
            List<Type> list = new ArrayList<>(Arrays.asList(method.getParameterTypes()));
            // Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
            parameters = satisfyParameters(test, callee, list, null, position, recursionDepth + 1, true, false, true);

            st = new PrivateMethodStatement(test, reflectionFactory.getReflectedClass(), method,
                    callee, parameters, Modifier.isStatic(method.getModifiers()));
        }

        int newLength = test.size();
        position += (newLength - length);

        test.addStatement(st, position);
        return true;
    }

    /**
     * Tries to insert a random call on the environment the UUT interacts with, e.g., the file
     * system or network connections. Callers have to specify the position of the last valid
     * statement of {@code test} before the insertion. Returns the updated position of the last
     * valid statement after a successful insertion, or a negative value if there was an error.
     *
     * @param test
     * @param lastValidPosition
     * @return the position where the insertion happened, or a negative value otherwise
     */
    public int insertRandomCallOnEnvironment(TestCase test, int lastValidPosition) {

        int previousLength = test.size();
        currentRecursion.clear();

        List<GenericAccessibleObject<?>> shuffledOptions = TestCluster.getInstance().getRandomizedCallsToEnvironment();
        if (shuffledOptions == null || shuffledOptions.isEmpty()) {
            return -1;
        }

        //iterate (in random order) over all possible environment methods till we find one that can be inserted
        for (GenericAccessibleObject<?> o : shuffledOptions) {
            try {
                int position;
                if (lastValidPosition <= 0) {
                    position = 0;
                } else {
                    position = Randomness.nextInt(0, lastValidPosition);
                }

                if (o.isConstructor()) {
                    GenericConstructor c = (GenericConstructor) o;
                    addConstructor(test, c, position, 0);
                    return position;
                } else if (o.isMethod()) {
                    GenericMethod m = (GenericMethod) o;
                    if (!m.isStatic()) {

                        VariableReference callee = null;
                        Type target = m.getOwnerType();

                        if (!test.hasObject(target, position)) {
                            callee = createObject(test, target, position, 0, null);
                            position += test.size() - previousLength;
                            previousLength = test.size();
                        } else {
                            callee = test.getRandomNonNullObject(target, position);
                        }
                        if (!TestUsageChecker.canUse(m.getMethod(), callee.getVariableClass())) {
                            logger.error("Cannot call method " + m + " with callee of type " + callee.getClassName());
                        }

                        addMethodFor(test, callee, m.copyWithNewOwner(callee.getGenericClass()), position);
                        return position;
                    } else {
                        addMethod(test, m, position, 0);
                        return position;
                    }
                } else {
                    throw new RuntimeException("Unrecognized type for environment: " + o);
                }
            } catch (ConstructionFailedException e) {
                //TODO what to do here?
                AtMostOnceLogger.warn(logger, "Failed environment insertion: " + e);
            }
        }

        //note: due to the constraints, it could well be that no environment method could be added

        return -1;
    }


    /**
     * Inserts a random call for the UUT into the given {@code test} at the specified {@code
     * position}. Returns {@code true} on success, {@code false} otherwise.
     *
     * @param test     the test case in which to insert
     * @param position the position at which to insert
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean insertRandomCall(TestCase test, int position) {
        int previousLength = test.size();
        String name = "";
        currentRecursion.clear();
        logger.debug("Inserting random call at position {}", position);
        try {
            if (reflectionFactory == null) {
                final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
                reflectionFactory = new ReflectionFactory(targetClass);
            }

            if (reflectionFactory.hasPrivateFieldsOrMethods() &&
                    TimeController.getInstance().getPhasePercentage() >= Properties.REFLECTION_START_PERCENT &&
                    (Randomness.nextDouble() < Properties.P_REFLECTION_ON_PRIVATE || TestCluster.getInstance().getNumTestCalls() == 0)) {
                logger.debug("Going to insert random reflection call");
                return insertRandomReflectionCall(test, position, 0);
            }
            GenericAccessibleObject<?> o = TestCluster.getInstance().getRandomTestCall(test);
            if (o == null) {
                logger.warn("Have no target methods to test");
                return false;
            } else if (o.isConstructor()) {

                GenericConstructor c = (GenericConstructor) o;
                logger.debug("Adding constructor call {}", c.getName());
                name = c.getName();
                addConstructor(test, c, position, 0);
            } else if (o.isMethod()) {
                GenericMethod m = (GenericMethod) o;
                logger.debug("Adding method call {}", m.getName());
                name = m.getName();

                if (!m.isStatic()) {
                    logger.debug("Getting callee of type {}", m.getOwnerClass().getTypeName());
                    VariableReference callee = null;
                    Type target = m.getOwnerType();

                    if (!test.hasObject(target, position)) {
                        callee = createObject(test, target, position, 0, null, false, false, true); //no FM for SUT
                        position += test.size() - previousLength;
                        previousLength = test.size();
                    } else {
                        callee = test.getRandomNonNullObject(target, position);
                        // This may also be an inner class, in this case we can't use a SUT instance
                        //if (!callee.isAssignableTo(m.getDeclaringClass())) {
                        //	callee = test.getRandomNonNullObject(m.getDeclaringClass(), position);
                        //}
                    }
                    logger.debug("Got callee of type {}", callee.getGenericClass().getTypeName());
                    if (!TestUsageChecker.canUse(m.getMethod(), callee.getVariableClass())) {
                        logger.debug("Cannot call method {} with callee of type {}", m, callee.getClassName());
                        throw new ConstructionFailedException("Cannot apply method to this callee");
                    }

                    addMethodFor(test, callee, m.copyWithNewOwner(callee.getGenericClass()), position);
                } else {
                    // We only use this for static methods to avoid using wrong constructors (?)
                    addMethod(test, m, position, 0);
                }
            } else if (o.isField()) {
                GenericField f = (GenericField) o;
                name = f.getName();
                logger.debug("Adding field {}", f.getName());
                if (Randomness.nextBoolean()) {
                    addFieldAssignment(test, f, position, 0);
                } else {
                    addField(test, f, position, 0);
                }
            } else {
                logger.error("Got type other than method or constructor!");
                return false;
            }

            return true;
        } catch (ConstructionFailedException e) {
            // TODO: Check this! - TestCluster replaced
            // TestCluster.getInstance().checkDependencies(o);
            logger.debug("Inserting statement {} has failed. Removing statements: {}", name, e);

            // TODO: Doesn't work if position != test.size()
            int lengthDifference = test.size() - previousLength;
            for (int i = lengthDifference - 1; i >= 0; i--) {
                //we need to remove them in order, so that the testcase is at all time consistent
                if (logger.isDebugEnabled()) {
                    logger.debug("  Removing statement: " + test.getStatement(position + i).getCode());
                }
                test.remove(position + i);
            }
            return false;
        }
    }

    /**
     * Within the given {@code test} case, inserts a random call at the specified {@code position}
     * on the object referenced by {@code var}. Returns {@code true} if the operation was successful
     * and {@code false} otherwise.
     * <p>
     * This method is especially useful if someone wants to insert a random call to a variable
     * that is subsequently used as a parameter for the method under test (MUT). The idea is to
     * mutate the parameter so that new program states can be reached in the MUT.
     *
     * @param test     the test case in which to insert
     * @param var      the reference to the object on which to perform the random method call
     * @param position the position at which to insert the call
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean insertRandomCallOnObjectAt(TestCase test, VariableReference var, int position) {

        // Select a random variable
        logger.debug("Chosen object: {}", var.getName());

        if (var instanceof ArrayReference) {
            logger.debug("Chosen object is array ");

            ArrayReference array = (ArrayReference) var;
            if (array.getArrayLength() > 0) {
                for (int i = 0; i < array.getArrayLength(); i++) {
                    logger.debug("Assigning array index " + i);
                    int old_len = test.size();
                    try {
                        assignArray(test, array, i, position);
                        position += test.size() - old_len;
                    } catch (ConstructionFailedException e) {
                    }
                }
                return true;
            }
        } else if (var.getGenericClass().hasWildcardOrTypeVariables()) {
            // TODO: If the object is of type Foo<?> then only
            //       methods that don't return / need a type ?
            //       should be called. For now, we just don't call
            //       any methods at all.
            logger.debug("Cannot add calls on unknown type");
        } else {
            logger.debug("Getting calls for object {}", var);
            try {
                if (reflectionFactory == null) {
                    final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
                    reflectionFactory = new ReflectionFactory(targetClass);
                }

                if (reflectionFactory.hasPrivateFieldsOrMethods() &&
                        TimeController.getInstance().getPhasePercentage() >= Properties.REFLECTION_START_PERCENT &&
                        Randomness.nextDouble() < Properties.P_REFLECTION_ON_PRIVATE) {
                    return insertRandomReflectionCallOnObject(test, var, position, 0);
                }

                // For the specified object "var" (that is being used as a parameter in a
                // subsequent but here unrelated call to the MUT), randomly choose a method that we
                // can call so as to change the state of "var". This tactic makes it more likely
                // that new program states will be reached and thus more code will be covered.
                GenericAccessibleObject<?> call = TestCluster.getInstance().getRandomCallFor(var.getGenericClass(), test, position);
                logger.debug("Chosen call {}", call);
                return addCallFor(test, var, call, position);
            } catch (ConstructionFailedException e) {
                logger.debug("Found no modifier: {}", e.getMessage());
            }
        }

        return false;
    }


    /**
     * Inserts one or perhaps multiple random statements into the given {@code test}. Callers
     * have to specify the position of the last valid statement of {@code test} by supplying an
     * appropriate index {@code lastPosition}. After a successful insertion, returns the updated
     * position of the last valid statement (which is always non-negative), or if there was an error
     * the constant {@link org.evosuite.testcase.mutation.InsertionStrategy#INSERTION_ERROR
     * INSERTION_ERROR}.
     *
     * @param test         the test case in which to insert
     * @param lastPosition the position of the last valid statement of {@code test} before insertion
     * @return the position of the last valid statement after insertion, or {@code INSERTION_ERROR}
     * (see above)
     */
    public int insertRandomStatement(TestCase test, int lastPosition) {
        RandomInsertion rs = new RandomInsertion();
        return rs.insertStatement(test, lastPosition);
    }

    /**
     * Satisfies a list of parameters by reusing or creating variables. Returns a list of references
     * to the objects or values . If there are no parameters, simply returns the empty list. If
     * there was an error, throws a {@code ConstructionFailedException}.
     *
     * @param test
     * @param parameterTypes
     * @param parameterList
     * @param position
     * @param recursionDepth
     * @return
     * @throws ConstructionFailedException
     */
    public List<VariableReference> satisfyParameters(TestCase test, VariableReference callee, List<Type> parameterTypes,
                                                     List<Parameter> parameterList, int position, int recursionDepth, boolean allowNull,
                                                     boolean excludeCalleeGenerators, boolean canReuseExistingVariables) throws ConstructionFailedException {

        if (callee == null && excludeCalleeGenerators) {
            throw new IllegalArgumentException("Exclude generators on null callee");
        }

        List<VariableReference> parameters = new ArrayList<>();
        logger.debug("Trying to satisfy {} parameters at position {}", parameterTypes.size(), position);

        for (int i = 0; i < parameterTypes.size(); i++) {
            Type parameterType = parameterTypes.get(i);
            Parameter parameter = null;
            boolean allowNullForParameter = allowNull;
            if (parameterList != null)
                parameter = parameterList.get(i);

            logger.debug("Current parameter type: {}", parameterType);

            if (parameterType instanceof CaptureType) {
                // TODO: This should not really happen in the first place
                throw new ConstructionFailedException("Cannot satisfy capture type");
            }

            GenericClass<?> parameterClass = GenericClassFactory.get(parameterType);
            if (parameterClass.hasTypeVariables()) {
                logger.debug("Parameter has type variables, replacing with wildcard");
                parameterType = parameterClass.getWithWildcardTypes().getType();
            }
            int previousLength = test.size();

            VariableReference var = null;

            if (Properties.HONOUR_DATA_ANNOTATIONS && (parameterList != null)) {

                if (GenericUtils.isAnnotationTypePresent(parameter.getAnnotations(), GenericUtils.NONNULL)) {
                    allowNullForParameter = false;
                }
            }

            if (canReuseExistingVariables) {
                logger.debug("Can re-use variables");
                var = createOrReuseVariable(test, parameterType, position, recursionDepth, callee, allowNullForParameter,
                        excludeCalleeGenerators, true);
            } else {
                logger.debug("Cannot re-use variables: attempt at creating new one");
                var = createVariable(test, parameterType, position, recursionDepth, callee, allowNullForParameter,
                        excludeCalleeGenerators, true, false);
                if (var == null) {
                    throw new ConstructionFailedException(
                            "Failed to create variable for type " + parameterType + " at position " + position);
                }
            }

            assert !(!allowNullForParameter && ConstraintHelper.isNull(var, test));

            // Generics instantiation may lead to invalid types, so better
            // double check
            if (!var.isAssignableTo(parameterType)) {
                throw new ConstructionFailedException("Error: " + var + " is not assignable to " + parameterType);
            }
            parameters.add(var);

            int currentLength = test.size();
            position += currentLength - previousLength;
        }
        logger.debug("Satisfied {} parameters", parameterTypes.size());
        return parameters;
    }


}
