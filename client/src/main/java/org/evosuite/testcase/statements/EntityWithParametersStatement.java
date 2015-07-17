package org.evosuite.testcase.statements;

import org.evosuite.runtime.annotation.BoundInputVariable;
import org.evosuite.runtime.annotation.Constraints;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andrea Arcuri on 04/07/15.
 */
public abstract class EntityWithParametersStatement extends AbstractStatement{

    protected final List<VariableReference> parameters;
    protected final Annotation[][] parameterAnnotations;
    protected final Annotation[] annotations;

    protected EntityWithParametersStatement(TestCase tc, Type type, List<VariableReference> parameters,
                                            Annotation[] annotations, Annotation[][] parameterAnnotations) throws IllegalArgumentException{
        super(tc,type);
        this.parameters = parameters;
        this.annotations = annotations;
        this.parameterAnnotations = parameterAnnotations;
        validateInputs();
    }

    protected EntityWithParametersStatement(TestCase tc, VariableReference retval, List<VariableReference> parameters,
                                            Annotation[] annotations, Annotation[][] parameterAnnotations) throws IllegalArgumentException{
        super(tc,retval);
        this.parameters = parameters;
        this.annotations = annotations;
        this.parameterAnnotations = parameterAnnotations;
        validateInputs();
    }

    private void validateInputs() throws IllegalArgumentException{
        Inputs.checkNull(parameters);
        for(VariableReference ref : parameters){
            Inputs.checkNull(ref);
        }
        if(parameterAnnotations!=null){
            if(parameterAnnotations.length != parameters.size()){
                throw new IllegalArgumentException("Size mismatched");
            }
        }
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

    /**
     * Check if the given var is bounded in this method/constructor as input parameter
     * @param var
     * @return
     */
    public boolean isBounded(VariableReference var) throws IllegalArgumentException{
        Inputs.checkNull(var);

        for(int i=0; i<parameters.size(); i++){
            if(parameters.get(i).equals(var)){

                for(int j=0; j<parameterAnnotations[i].length; j++){
                    if(parameterAnnotations[i][j] instanceof BoundInputVariable){
                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }

    protected int getNumParametersOfType(Class<?> clazz) {
        int num = 0;
        for(VariableReference var : parameters) {
            if(var.getVariableClass().equals(clazz))
                num++;
        }
        return num;
    }

    protected Constraints getConstraints(){
        for(Annotation annotation : annotations){
            if(annotation instanceof Constraints){
                return (Constraints)annotation;
            }
        }
        return null;
    }

    protected boolean mutateParameter(TestCase test, int numParameter) {

        // replace a parameter
        VariableReference parameter = parameters.get(numParameter);

        List<VariableReference> objects = test.getObjects(parameter.getType(),getPosition());
        objects.remove(parameter);
        objects.remove(getReturnValue());

        NullStatement nullStatement = new NullStatement(test, parameter.getType());
        Statement copy = null;

        //check if NULL is a valid option
        Constraints constraint = getConstraints();
        boolean avoidNull =  constraint!=null && constraint.noNullInputs();

        if(avoidNull){
            //be sure to remove all references pointing to NULL
            Iterator<VariableReference> iter = objects.iterator();
            while(iter.hasNext()){
                VariableReference ref = iter.next();
                if(ref instanceof NullReference){
                    iter.remove();
                }
            }

        } else {
            // If it's not a primitive, then changing to null is also an option
            if (!parameter.isPrimitive()) {
                objects.add(nullStatement.getReturnValue());
            }
        }


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
