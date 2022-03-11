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

import org.evosuite.Properties;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A common superclass for statements that contain a call to an executable entity, i.e.,
 * methods, constructors and functional mocks.
 * <p>
 * Created by Andrea Arcuri on 04/07/15.
 */
public abstract class EntityWithParametersStatement extends AbstractStatement {

    private static final long serialVersionUID = 2971944785047056480L;
    protected final List<VariableReference> parameters;
    protected final Annotation[][] parameterAnnotations;
    protected final Annotation[] annotations;

    protected EntityWithParametersStatement(TestCase tc, Type type, List<VariableReference> parameters,
                                            Annotation[] annotations, Annotation[][] parameterAnnotations) throws IllegalArgumentException {
        super(tc, type);
        this.parameters = parameters;
        this.annotations = annotations;
        this.parameterAnnotations = parameterAnnotations;
        validateInputs();
    }

    protected EntityWithParametersStatement(TestCase tc, VariableReference retval, List<VariableReference> parameters,
                                            Annotation[] annotations, Annotation[][] parameterAnnotations) throws IllegalArgumentException {
        super(tc, retval);
        this.parameters = parameters;
        this.annotations = annotations;
        this.parameterAnnotations = parameterAnnotations;
        validateInputs();
    }

    /**
     * Constructor needed for Functional Mocks where the number of input parameters
     * might vary during the search, ie not constant, and starts with 0
     *
     * @param tc
     * @param retval
     */
    protected EntityWithParametersStatement(TestCase tc, VariableReference retval) {
        super(tc, retval);
        this.parameters = new ArrayList<>();
        this.annotations = null;
        this.parameterAnnotations = null;
    }

    /**
     * Constructor needed for Functional Mocks where the number of input parameters
     * might vary during the search, ie not constant, and starts with 0
     *
     * @param tc
     * @param type
     */
    protected EntityWithParametersStatement(TestCase tc, Type type) {
        super(tc, type);
        this.parameters = new ArrayList<>();
        this.annotations = null;
        this.parameterAnnotations = null;
    }

    private void validateInputs() throws IllegalArgumentException {
        Inputs.checkNull(parameters);
        for (VariableReference ref : parameters) {
            Inputs.checkNull(ref);
        }
        if (parameterAnnotations != null) {
            if (parameterAnnotations.length != parameters.size()) {
                throw new IllegalArgumentException("Size mismatched");
            }
        }
    }

    public List<VariableReference> getParameterReferences() {
        return Collections.unmodifiableList(parameters);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(VariableReference var1, VariableReference var2) {

        if (retval.equals(var1)) {
            retval = var2;
            // TODO: Notify listener?
        }

        for (int i = 0; i < parameters.size(); i++) {

            if (parameters.get(i).equals(var1))
                parameters.set(i, var2);
            else
                parameters.get(i).replaceAdditionalVariableReference(var1, var2);
        }
    }


    @Override
    public List<VariableReference> getUniqueVariableReferences() {
        List<VariableReference> references = new ArrayList<>();
        references.add(retval);
        references.addAll(parameters);
        for (VariableReference param : parameters) {
            if (param instanceof ArrayIndex)
                references.add(((ArrayIndex) param).getArray());
        }
        return references;

    }

    @Override
    public Set<VariableReference> getVariableReferences() {
        Set<VariableReference> references = new LinkedHashSet<>();
        references.add(retval);
        for (VariableReference param : parameters) {
            if (param == null) {
                /*
                    This could happen while building a functional mock, and creation
                    of its input values lead to a forward check of properties
                 */
                continue;
            }
            references.add(param);
            if (param.getAdditionalVariableReference() != null)
                references.add(param.getAdditionalVariableReference());
        }
        references.addAll(getAssertionReferences());

        return references;
    }


    @Override
    public int getNumParameters() {
        return parameters.size();
    }

    /**
     * <p>
     * replaceParameterReference
     * </p>
     *
     * @param var          a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param numParameter a int.
     */
    public void replaceParameterReference(VariableReference var, int numParameter) throws IllegalArgumentException {
        Inputs.checkNull(var);
        if (numParameter < 0 || numParameter >= parameters.size()) {
            throw new IllegalArgumentException("Out of range index " + numParameter + " from list of size " + parameters.size());
        }

        parameters.set(numParameter, var);
    }

    /**
     * Check if the given var is bounded in this method/constructor as input parameter
     *
     * @param var
     * @return
     */
    public boolean isBounded(VariableReference var) throws IllegalArgumentException {
        Inputs.checkNull(var);

        if (parameterAnnotations == null) {
            assert this instanceof FunctionalMockStatement; //for now this should be the only valid case
            return false;
        }

        return false;
    }

    protected int getNumParametersOfType(Class<?> clazz) {
        int num = 0;
        for (VariableReference var : parameters) {
            if (var.getVariableClass().equals(clazz))
                num++;
        }
        return num;
    }

    protected boolean mutateParameter(TestCase test, int numParameter) {

        // replace a parameter
        VariableReference parameter = parameters.get(numParameter);

        List<VariableReference> objects = test.getObjects(parameter.getType(), getPosition());
        objects.remove(parameter);
        objects.remove(getReturnValue());

        NullStatement nullStatement = new NullStatement(test, parameter.getType());
        Statement copy = null;

        boolean avoidNull = false;

        if (Properties.HONOUR_DATA_ANNOTATIONS && (numParameter < parameterAnnotations.length)) {
            if (GenericUtils.isAnnotationTypePresent(parameterAnnotations[numParameter], GenericUtils.NONNULL)) {
                avoidNull = true;
            }
        }
        if (avoidNull) {
            //be sure to remove all references pointing to NULL
            objects.removeIf(ref -> ref instanceof NullReference);

        } else {
            // If it's not a primitive, then changing to null is also an option
            if (!parameter.isPrimitive()) {
                objects.add(nullStatement.getReturnValue());
            }
        }


        // If there are fewer objects than parameters of that type,
        // we consider adding an instance
        if (getNumParametersOfType(parameter.getVariableClass()) + 1 > objects.size()) {
            Statement originalStatement = test.getStatement(parameter.getStPosition());
            copy = originalStatement.clone(test);
            if (originalStatement instanceof PrimitiveStatement<?>) {
                ((PrimitiveStatement<?>) copy).delta();
            }
            objects.add(copy.getReturnValue());
        }

        if (objects.isEmpty())
            return false;

        VariableReference replacement = Randomness.choice(objects);
        if (replacement == nullStatement.getReturnValue()) {
            test.addStatement(nullStatement, getPosition());
        } else if (copy != null && replacement == copy.getReturnValue()) {
            test.addStatement(copy, getPosition());
        }
        replaceParameterReference(replacement, numParameter);
        return true;
    }

    public abstract String getDeclaringClassName();

    public abstract String getMethodName();

    public abstract String getDescriptor();

}
