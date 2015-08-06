package org.evosuite.testcase.statements;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.runtime.fm.EvoInvocationListener;
import org.evosuite.runtime.fm.MethodDescriptor;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.execution.UncompilableCodeException;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * Statement representing the creation and setup of a functional mock.
 * Recall: a functional mock is different from an environment one (eg for file IO and CPU time).
 * A functional mock instantiation can look like:
 *
 * <p>
 * EvoInvocationListener listener = new EvoInvocationListener(); <br>
 * Foo foo = mock(Foo.class, withSettings().invocationListeners(listener)); <br>
 * when(foo.aMethod(any() ...)).thenReturn( v0, v1, ...); <br>
 * when(foo.anotherMethod(...)).thenReturn( k0, k1, ...); <br>
 * ... <br>
 * listener.activate();
 *
 * <p>
 * All these statements will be represented with a single one, where the return
 * value is the instantiated mock "foo", and the input parameters are all the input
 * parameters of all mocked methods (eg v0, k0), in order.
 *
 * <p>
 * Calls to "listener" are essential during the search (eg when the statement is executed),
 * but will not be part of the final generated JUnit tests (ie not part of toCode())
 *
 * <p>
 * Initially, a functional mock will have 0 input parameters, and no "when" call.
 * After a test is executed, the input parameter lists will be updated based on what
 * "listener" does report. The number of input parameters might vary several times
 * throughout the lifespan of a test during the search (can both increase and decrease).
 *
 * <p>
 * Created by Andrea Arcuri on 01/08/15.
 */
public class FunctionalMockStatement extends EntityWithParametersStatement{

    /**
     * This list needs to be kept sorted
     */
    private final List<MethodDescriptor> mockedMethods;

    private final Class<?> targetClass;

    private volatile EvoInvocationListener listener;



    public FunctionalMockStatement(TestCase tc, VariableReference retval, Class<?> targetClass) throws IllegalArgumentException{
        super(tc, retval);
        Inputs.checkNull(targetClass);
        this.targetClass = targetClass;
        mockedMethods = new ArrayList<>();
    }


    //------------ override methods ---------------

    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        return null; //TODO
    }

    @Override
    public Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException {

        Throwable exceptionThrown = null;

        try {
            return super.exceptionHandler(new Executer() {

                @Override
                public void execute() throws InvocationTargetException,
                        IllegalArgumentException, IllegalAccessException,
                        InstantiationException, CodeUnderTestException {

                    // First create the listener
                    listener = new EvoInvocationListener();

                    //then create the mock
                    Object ret = mock(targetClass, withSettings().invocationListeners(listener));


                    //execute all "when" statements
                    int index = 0;
                    for(MethodDescriptor md : mockedMethods){

                        Method method = md.getMethod(); //target method, eg foo.aMethod(...)
                        if(method.getReturnType().equals(Void.TYPE)){
                            //no need to mock a method that returns void
                            continue;
                        }

                        Type[] parameterTypes = method.getParameterTypes();

                        //target inputs
                        Object[] targetInputs = new Object[md.getNumberOfInputParameters()];
                        for(int i=0; i<targetInputs.length; i++){
                            targetInputs[i] = md.executeMatcher(i);
                        }

                        //actual call foo.aMethod(...)
                        Object targetMethodResult = method.invoke(ret, targetInputs);

                        //when(...)
                        OngoingStubbing<Object> retForThen = Mockito.when(targetMethodResult);

                        //thenReturn(...)
                        Object[] thenReturnInputs = new Object[parameterTypes.length];
                        for(int i = index; i<md.getCounter(); i++){
                            VariableReference parameterVar = parameters.get(i);
                            thenReturnInputs[i] = parameterVar.getObject(scope);
                            if(thenReturnInputs[i] == null && method.getParameterTypes()[i].isPrimitive()) {
                                throw new CodeUnderTestException(new NullPointerException());
                            }
                            if (thenReturnInputs[i] != null && !TypeUtils.isAssignable(thenReturnInputs[i].getClass(), parameterTypes[i])) {
                                throw new CodeUnderTestException(
                                        new UncompilableCodeException("Cannot assign "+parameterVar.getVariableClass().getName()
                                                +" to "+parameterTypes[i]));
                            }
                        }

                        //final call when(...).thenReturn(...)
                        if(thenReturnInputs.length==1) {
                            retForThen.thenReturn(thenReturnInputs[0]);
                        } else {
                            Object[] values = Arrays.copyOfRange(thenReturnInputs, 1 , thenReturnInputs.length);
                            retForThen.thenReturn(thenReturnInputs[0], values);
                        }

                        index += md.getCounter();
                    }

                    //finally, activate the listener
                    listener.activate();

                    try {
                        retval.setObject(scope, ret);
                    } catch (CodeUnderTestException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new EvosuiteError(e);
                    }
                }

                @Override
                public Set<Class<? extends Throwable>> throwableExceptions() {
                    Set<Class<? extends Throwable>> t = new HashSet<>();
                    t.add(InvocationTargetException.class);
                    return t;
                }
            });

        } catch (InvocationTargetException e) {
            exceptionThrown = e.getCause();
        }
        return exceptionThrown;
    }

    @Override
    public GenericAccessibleObject<?> getAccessibleObject() {
        return null; //TODO
    }

    @Override
    public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals, Throwable exception) {
        //TODO
    }

    @Override
    public List<VariableReference> getUniqueVariableReferences() {
        return null; //TODO
    }

    @Override
    public Set<VariableReference> getVariableReferences() {
        return null; //TODO
    }

    @Override
    public boolean isAssignmentStatement() {
        return false; //TODO
    }

    @Override
    public void replace(VariableReference var1, VariableReference var2) {
        //TODO
    }

    @Override
    public boolean same(Statement s) {
        return false; //TODO
    }
}
