package org.evosuite.testcase.statements;

import org.evosuite.runtime.util.Inputs;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Andrea Arcuri on 04/07/15.
 */
public abstract class EntityWithParametersStatement extends AbstractStatement{

    protected final List<VariableReference> parameters;

    protected EntityWithParametersStatement(TestCase tc, Type type, List<VariableReference> parameters){
        super(tc,type);
        this.parameters = parameters;
        Inputs.checkNull(parameters);
    }

    protected EntityWithParametersStatement(TestCase tc, VariableReference retval, List<VariableReference> parameters){
        super(tc,retval);
        this.parameters = parameters;
        Inputs.checkNull(parameters);
    }

    public List<VariableReference> getParameterReferences() {
        return parameters;
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
     * @param var
     *            a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param numParameter
     *            a int.
     */
    public void replaceParameterReference(VariableReference var, int numParameter) throws IllegalArgumentException{
        Inputs.checkNull(var);
        if(numParameter<0 || numParameter>= parameters.size()){
            throw new IllegalArgumentException("Out of range index "+numParameter+" from list of size "+parameters.size());
        }

        parameters.set(numParameter, var);
    }

    protected int getNumParametersOfType(Class<?> clazz) {
        int num = 0;
        for(VariableReference var : parameters) {
            if(var.getVariableClass().equals(clazz))
                num++;
        }
        return num;
    }

    protected boolean mutateParameter(TestCase test, int numParameter) {
        // replace a parameter
        VariableReference parameter = parameters.get(numParameter);
        List<VariableReference> objects = test.getObjects(parameter.getType(),
                getPosition());
        objects.remove(parameter);
        objects.remove(getReturnValue());
        NullStatement nullStatement = new NullStatement(test, parameter.getType());
        Statement copy = null;

        // If it's not a primitive, then changing to null is also an option
        if (!parameter.isPrimitive())
            objects.add(nullStatement.getReturnValue());

        // If there are fewer objects than parameters of that type,
        // we consider adding an instance
        if(getNumParametersOfType(parameter.getVariableClass()) + 1 < objects.size()) {
            Statement originalStatement = test.getStatement(parameter.getStPosition());
            copy = originalStatement.clone(test);
            if (originalStatement instanceof PrimitiveStatement<?>) {
                ((PrimitiveStatement<?>)copy).delta();
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
}
