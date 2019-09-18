package org.evosuite.testcase.mutation.change;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.ConstraintHelper;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;

public class RandomModification implements ModificationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RandomModification.class);

    private RandomModification() {
    }

    public static RandomModification getInstance() {
        return SingletonContainer.instance;
    }

    @Override
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

            int boundPosition = ConstraintHelper.getLastPositionOfBounded(ref, test);
            if (boundPosition >= 0 && boundPosition >= statement.getPosition()) {
                // if bounded variable, cannot add methods before its initialization, and so cannot be
                // used as a callee
                iter.remove();
            }
        }

        // TODO: replacing void calls with other void calls might not be the best idea
        List<GenericAccessibleObject<?>> calls = getPossibleCalls(statement.getReturnType(), objects);

        GenericAccessibleObject<?> ao = statement.getAccessibleObject();
        if (ao != null && ao.getNumParameters() > 0) {
            calls.remove(ao);
        }

        if (ConstraintHelper.getLastPositionOfBounded(statement.getReturnValue(), test) >= 0) {
            //if the return variable is bounded, we can only use a constructor on the right hand-side
            calls.removeIf(k -> !(k instanceof GenericConstructor));
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
                || tc.getStatement(var.getStPosition()) instanceof FunctionalMockStatement
                || ConstraintHelper.getLastPositionOfBounded(var, tc) >= position);

        if (variables.isEmpty()) {
            throw new ConstructionFailedException("Found no variables of type " + type
                    + " at position " + position);
        }

        return Randomness.choice(variables);
    }

    /**
     * Retrieve all the replacement calls that can be inserted at this position without changing the
     * length
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
            allCalls = TestCluster.getInstance().getGenerators(new GenericClass(
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
                        call = method.getGenericInstantiation(new GenericClass(returnType));
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

    /**
     * Retrieve the dependencies for a method
     *
     * @param method
     * @return
     */
    private Set<Type> getDependencies(GenericMethod method) {
        Set<Type> dependencies = new LinkedHashSet<>();
        if (!method.isStatic()) {
            dependencies.add(method.getOwnerType());
        }
        dependencies.addAll(Arrays.asList(method.getParameterTypes()));

        return dependencies;
    }

    /**
     * Retrieve the dependencies for a constructor
     *
     * @param constructor
     * @return
     */
    private Set<Type> getDependencies(GenericConstructor constructor) {
        return new LinkedHashSet<>(Arrays.asList(constructor.getParameterTypes()));
    }

    /**
     * Retrieve the dependencies for a field
     *
     * @param field
     * @return
     */
    private Set<Type> getDependencies(GenericField field) {
        Set<Type> dependencies = new LinkedHashSet<>();
        if (!field.isStatic()) {
            dependencies.add(field.getOwnerType());
        }

        return dependencies;
    }

    /**
     * Determine if the set of objects is sufficient to satisfy the set of dependencies
     *
     * @param dependencies
     * @param objects
     * @return
     */
    private boolean dependenciesSatisfied(Set<Type> dependencies,
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

    private static final class SingletonContainer {
        private static final RandomModification instance = new RandomModification();
    }
}
