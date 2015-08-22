/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase.statements;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.testcase.fm.EvoInvocationListener;
import org.evosuite.testcase.fm.MethodDescriptor;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.execution.UncompilableCodeException;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
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
 * This statement cannot be used to mock the SUT, as it would make no sense whatsoever.
 * However, there might be special cases: eg SUT being an abstract class with no
 * concrete implementation. That would need to be handled specially.
 *
 * <p>
 * TODO: need to handle Generics, eg <br>
 * Foo&lt;Bar&gt; foo = (Foo&lt;Bar&gt;) mock(Foo.class);
 *
 * <p>
 * Created by Andrea Arcuri on 01/08/15.
 */
public class FunctionalMockStatement extends EntityWithParametersStatement{

	private static final long serialVersionUID = -8177814473724093381L;

	/**
     * This list needs to be kept sorted
     */
    private final List<MethodDescriptor> mockedMethods;

    /**
     * key -> MethodDescriptor id,
     * Value -> min,max  inclusive of indices on super.parameters
     */
    private final Map<String, int[]> methodParameters;

    private Class<?> targetClass;

    private volatile EvoInvocationListener listener;


    public FunctionalMockStatement(TestCase tc, VariableReference retval, Class<?> targetClass) throws IllegalArgumentException{
        super(tc, retval);
        Inputs.checkNull(targetClass);
        this.targetClass = targetClass;
        mockedMethods = new ArrayList<>();
        methodParameters = new LinkedHashMap<>();
        checkSUT();
        assert parameters.isEmpty();
    }

    public FunctionalMockStatement(TestCase tc, Type retvalType, Class<?> targetClass) throws IllegalArgumentException{
        super(tc, retvalType);
        Inputs.checkNull(targetClass);
        this.targetClass = targetClass;
        mockedMethods = new ArrayList<>();
        methodParameters = new LinkedHashMap<>();
        checkSUT();
        assert parameters.isEmpty();
    }

    @Override
    public void changeClassLoader(ClassLoader loader) {

        try {
            targetClass = loader.loadClass(targetClass.getCanonicalName());
        } catch (ClassNotFoundException e) {
            logger.error("Failed to update target class from new classloader: "+e.getMessage());
        }

        super.changeClassLoader(loader);
    }

    private void checkSUT(){
        if(targetClass.equals(Properties.getTargetClass())){
            throw new IllegalArgumentException("Cannot create a basic functional mock for the SUT");
        }
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public List<MethodDescriptor> getMockedMethods() {
        return mockedMethods;
    }

    public List<VariableReference> getParameters(String id) throws IllegalArgumentException{
        Inputs.checkNull(id);

        int[] minMax = methodParameters.get(id);
        if(minMax == null){
            return null;
        }

        List<VariableReference> list = new ArrayList<>();
        for(int i=minMax[0]; i<=minMax[1]; i++){
            list.add(parameters.get(i));
        }
        return list;
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

            if(! previous.getID().equals(now.getID())){
                return true;
            }

            if(!now.shouldBeMocked()){
                /*
                    Do not change in the usage of non-mockable methods, because anyway
                    we do not have any VarRef for them
                 */
                continue;
            }

            /*
                need to be a mismatch. However, even in that case, either the current should
                not have reached the limit (and so we could not increase) OR if it is reached
                then the needed number of mocked v has increased.

                For example, if limit is 5, and previous is 10, then decreasing by 5 or
                 increasing by any amount should have no impact
             */

            if(now.getCounter() != previous.getCounter() &&
                    (now.getCounter() < Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT) ||
                     previous.getCounter() < Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT
                    ){
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

        logger.debug("Executing updateMockedMethods. Parameter size: "+parameters.size());

        List<Type> list = new ArrayList<>();

        assert ! super.parameters.contains(null);
        assert mockedMethods.size() == methodParameters.size();

        List<VariableReference> copy = new ArrayList<>(super.parameters);
        assert copy.size() == super.parameters.size();

        super.parameters.clear();
        mockedMethods.clear(); //important to remove all the no longer used calls

        Map<String, int[]> mpCopy = new LinkedHashMap<>();

        List<MethodDescriptor> executed = listener.getCopyOfMethodDescriptors();

        int mdIndex = 0;

        for(MethodDescriptor md : executed){
            mockedMethods.add(md);

            if(!md.shouldBeMocked() || md.getCounter() == 0){
                //void method or not called, so no parameter needed for it
                mpCopy.put(md.getID(), null);
                continue;
            }

            int added = 0;

            //infer parameter mapping of current vars from previous execution, if any
            int[] minMax = methodParameters.get(md.getID());
            int existingParameters; //total number of existing parameters
            if(minMax==null){
                //before it was not called
                minMax = new int[]{-1,-1};
                existingParameters = 0;
            } else {
                assert  minMax[1] >= minMax[0] && minMax[0] >= 0;
                assert  minMax[1] < copy.size() : "Max="+minMax[1]+" but n="+copy.size();
                existingParameters = 1 + (minMax[1] - minMax[0]);
            }

            assert existingParameters  <= Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT;

            //check if less calls
            if(existingParameters > md.getCounter()){
                //now the method has been called less times,
                //so remove the last calls, ie decrease counter
                minMax[1] -= (existingParameters - md.getCounter());
            }

            if(existingParameters > 0 ) {
                for (int i = minMax[0]; i <= minMax[1]; i++) {
                    //align super class data structure
                    super.parameters.add(copy.get(i));
                    added++;
                }
            }

            //check if rather more calls
            if(existingParameters < md.getCounter()){
                Class<?> returnType = md.getMethod().getReturnType();
                assert ! returnType.equals(Void.TYPE);

                for (int i = existingParameters; i < md.getCounter() && i < Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT; i++) {
                    list.add(returnType);

                    super.parameters.add(null); //important place holder for following updates
                    added++;
                }
            }


            minMax[0] = mdIndex;
            minMax[1] = (mdIndex + added - 1); //max is inclusive
            assert minMax[1] >= minMax[0] && minMax[0] >= 0; //max >= min
            assert super.parameters.size() == minMax[1] + 1;

            mpCopy.put(md.getID(), minMax);
            mdIndex += added;
        }

        methodParameters.clear();
        methodParameters.putAll(mpCopy);
        for(MethodDescriptor md : mockedMethods){
            if(! methodParameters.containsKey(md.getID())){
                methodParameters.put(md.getID(), null);
            }
        }

        return list;
    }

    public void addMissingInputs(List<VariableReference> inputs) throws IllegalArgumentException{
        Inputs.checkNull(inputs);

        logger.debug("Adding "+inputs.size()+ " missing values");

        if(! inputs.isEmpty()) {

            if (inputs.size() > parameters.size()) {
                //first quick check
                throw new IllegalArgumentException("Not enough parameter place holders");
            }

            int index = 0;
            for (VariableReference ref : inputs) {
                while (parameters.get(index) != null) {
                    index++;
                    if (index >= parameters.size()) {
                        throw new IllegalArgumentException("Not enough parameter place holders");
                    }
                }

                assert  ref.isAssignableFrom(getExpectedParameterType(index));

                parameters.set(index, ref);
            }
        } //else, nothing to add

        //check if all "holes" have been filled
        for(VariableReference ref : parameters){
            if(ref == null){
                throw new IllegalArgumentException("Functional mock not fully set with all needed missing inputs");
            }
        }
    }

    public void fillWithNullRefs(){
        for(int i=0; i<parameters.size(); i++){
            VariableReference ref = parameters.get(i);
            if(ref == null){
                parameters.set(i , new ConstantValue(tc, new GenericClass(getExpectedParameterType(i)), null));
            }
        }
    }


    private Type getExpectedParameterType(int i){

        for(MethodDescriptor md : mockedMethods) {
            int[] bounds = methodParameters.get(md.getID());
            if(bounds!=null && i >= bounds[0] && i <= bounds[1]){
                return md.getMethod().getReturnType();
            }
        }

        throw new AssertionError("");
    }

    //------------ override methods ---------------


    @Override
    public void addAssertion(Assertion assertion) {
        //never add an assertion to a functional mock
    }


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

        for(Map.Entry<String,int[]> entry : methodParameters.entrySet()){
            int[] array = entry.getValue();
            int[] copiedArray = array==null ? null : new int[]{array[0],array[1]};
            copy.methodParameters.put(entry.getKey() , copiedArray);
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

                        if(! md.shouldBeMocked()){
                            //no need to mock a method that returns void
                            continue;
                        }

                        Method method = md.getMethod(); //target method, eg foo.aMethod(...)

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
                        Object[] thenReturnInputs = new Object[Math.min(md.getCounter(), Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT)];
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
