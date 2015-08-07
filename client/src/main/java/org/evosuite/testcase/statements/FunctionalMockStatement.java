package org.evosuite.testcase.statements;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.testcase.fm.EvoInvocationListener;
import org.evosuite.testcase.fm.MethodDescriptor;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.execution.UncompilableCodeException;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
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
 * TODO: need to handle Generics, eg <br>
 * Foo&lt;Bar&gt; foo = (Foo&lt;Bar&gt;) mock(Foo.class);
 *
 * <p>
 * Created by Andrea Arcuri on 01/08/15.
 */
public class FunctionalMockStatement extends EntityWithParametersStatement{

    /**
     * This list needs to be kept sorted
     */
    private final List<MethodDescriptor> mockedMethods;

    /**
     * key -> MethodDescriptor id
     */
    private final Map<String, List<VariableReference>> methodParameters;

    private final Class<?> targetClass;

    private volatile EvoInvocationListener listener;


    public FunctionalMockStatement(TestCase tc, VariableReference retval, Class<?> targetClass) throws IllegalArgumentException{
        super(tc, retval);
        Inputs.checkNull(targetClass);
        this.targetClass = targetClass;
        mockedMethods = new ArrayList<>();
        methodParameters = new LinkedHashMap<>();
    }


    /**
     * Check if the last execution of the test case has led a change in the usage of the mock.
     * This will result in adding/removing variable references
     *
     * @return
     */
    public boolean doesNeedToUpdateInputs(){
        if(listener==null){
            assert mockedMethods.isEmpty();
            return false; //no execution yet, so default is empty
        }

        List<MethodDescriptor> executed = listener.getCopyOfMethodDescriptors();
        if(executed.size() != mockedMethods.size()){
            return true;
        }

        for(int i=0; i<executed.size(); i++){
            MethodDescriptor previous = mockedMethods.get(i);
            MethodDescriptor now = executed.get(i);

            if(previous.compareTo(now) != 0){
                return true;
            }
        }

        return false;
    }


    /**
     * Based on most recent test execution, update the mocking configuration.
     * After calling this method, it is <b>necessary</b> to provide the missing
     * VariableReferences, if any, using addMissingInputs.
     *
     * @return a ordered, non-null list of types of missing new inputs that will need to be provided
     *
     */
    public List<Type> updateMockedMethods(){

        List<Type> list = new ArrayList<>();

        super.parameters.clear();
        mockedMethods.clear(); //important to remove all the no longer used calls

        List<MethodDescriptor> executed = listener.getCopyOfMethodDescriptors();

        for(MethodDescriptor md : executed){
            List<VariableReference> vars = methodParameters.get(md.getID());
            if(vars==null){
                vars = new ArrayList<>();
                methodParameters.put(md.getID(),vars);
            }

            //check if less calls
            while(vars.size() > md.getCounter()){
                //now the method has been called less times,
                //so remove the last calls
                vars.remove(vars.size()-1);
            }

            for(int i=0; i<vars.size(); i++){
                //align super class data structure
                super.parameters.add(vars.get(i));
            }

            //check if rather more calls
            if(vars.size() < md.getCounter()){
                Class<?> returnType = md.getMethod().getReturnType();
                if(! returnType.equals(Void.TYPE)) {
                    for (int i = vars.size(); i < md.getCounter(); i++) {
                        list.add(returnType);

                        super.parameters.add(null); //important place holder for following update
                    }
                }
            }

            mockedMethods.add(md);
        }

        return list;
    }

    public void addMissingInputs(List<VariableReference> inputs) throws IllegalArgumentException{
        Inputs.checkNull(inputs);

        if(inputs.isEmpty()){
            return; //nothing to add
        }

        if(inputs.size() > parameters.size()){
            //first quick check
            throw new IllegalArgumentException("Not enough parameter place holders");
        }

        int index = 0;
        for(VariableReference ref : inputs){
            while(parameters.get(index) != null){
                index++;
                if(index >= parameters.size()){
                    throw new IllegalArgumentException("Not enough parameter place holders");
                }
            }

            parameters.set(index, ref);
        }
    }


    //------------ override methods ---------------

    @Override
    public Statement copy(TestCase newTestCase, int offset) {


        FunctionalMockStatement copy = new FunctionalMockStatement(
                //TODO: handle generics in the type of VarRef
                newTestCase,new VariableReferenceImpl(newTestCase,targetClass),targetClass);

        for (VariableReference r : this.parameters) {
            copy.parameters.add(r.copy(newTestCase, offset));
        }

        copy.listener = this.listener; //no need to clone, as only read, and created new instance at each new execution

        for(MethodDescriptor md : this.mockedMethods){
            copy.mockedMethods.add(md.getCopy());
        }

        for(Map.Entry<String,List<VariableReference>> entry : methodParameters.entrySet()){
            List<VariableReference> list = new ArrayList<>();
            for(VariableReference var : entry.getValue()){
                list.add(var.copy(newTestCase,offset));
            }
            copy.methodParameters.put(entry.getKey() , list);
        }

        return copy;
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
                        Object[] thenReturnInputs = new Object[md.getCounter()];
                        for(int i = index; i<thenReturnInputs.length; i++){

                            VariableReference parameterVar = parameters.get(i);
                            thenReturnInputs[i] = parameterVar.getObject(scope);

                            if(thenReturnInputs[i] == null && method.getReturnType().isPrimitive()) {
                                throw new CodeUnderTestException(new NullPointerException());
                            }

                            if (thenReturnInputs[i] != null && !TypeUtils.isAssignable(thenReturnInputs[i].getClass(), method.getReturnType())) {
                                throw new CodeUnderTestException(
                                        new UncompilableCodeException("Cannot assign "+parameterVar.getVariableClass().getName()
                                                +" to "+method.getReturnType()));
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
        return null; //not defined for FM
    }

    @Override
    public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals, Throwable exception) {
        //deprecated
    }

    @Override
    public boolean isAssignmentStatement() {
        return false;
    }

    @Override
    public boolean same(Statement s) {
        if (this == s)
            return true;
        if (s == null)
            return false;
        if (getClass() != s.getClass())
            return false;

        FunctionalMockStatement fms = (FunctionalMockStatement) s;

        if (fms.parameters.size() != parameters.size())
            return false;

        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).same(fms.parameters.get(i)))
                return false;
        }

        if (!retval.same(fms.retval))
            return false;

        if(!targetClass.equals(fms.targetClass)){
            return false;
        }

        if(fms.mockedMethods.size() != mockedMethods.size()){
            return false;
        }

        for(int i=0; i<mockedMethods.size(); i++){
            if(! mockedMethods.get(i).getID().equals(fms.mockedMethods.get(i).getID())){
                return false;
            }
        }

        return true;
    }
}
