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

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.FalsePositiveException;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.InstrumentedClass;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.execution.UncompilableCodeException;
import org.evosuite.testcase.fm.EvoInvocationListener;
import org.evosuite.testcase.fm.MethodDescriptor;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericClassUtils;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
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
 *
 * <p>
 * Created by Andrea Arcuri on 01/08/15.
 */
public class FunctionalMockStatement extends EntityWithParametersStatement {

    private static final long serialVersionUID = -8177814473724093381L;

    private static final Logger logger = LoggerFactory.getLogger(FunctionalMockStatement.class);

    /**
     * This list needs to be kept sorted
     */
    protected final List<MethodDescriptor> mockedMethods;

    /**
     * key -> MethodDescriptor id,
     * Value -> min,max  inclusive of indices on super.parameters
     */
    protected final Map<String, int[]> methodParameters;

    protected GenericClass<?> targetClass;

    protected transient volatile EvoInvocationListener listener;

    protected transient Method mockCreator;


    public FunctionalMockStatement(TestCase tc, VariableReference retval, GenericClass<?> targetClass) throws IllegalArgumentException {
        super(tc, retval);
        Inputs.checkNull(targetClass);
        this.targetClass = targetClass;
        mockedMethods = new ArrayList<>();
        methodParameters = new LinkedHashMap<>();
        checkTarget();
        assert parameters.isEmpty();
        //setUpMockCreator();
    }


    public FunctionalMockStatement(TestCase tc, Type retvalType, GenericClass<?> targetClass) throws IllegalArgumentException {
        super(tc, retvalType);
        Inputs.checkNull(targetClass);

        Class<?> rawType = GenericClassFactory.get(retvalType).getRawClass();
        if (!targetClass.getRawClass().equals(rawType)) {
            throw new IllegalArgumentException("Mismatch between raw type " + rawType + " and target class " + targetClass);
        }

        this.targetClass = targetClass;
        mockedMethods = new ArrayList<>();
        methodParameters = new LinkedHashMap<>();
        checkTarget();
        assert parameters.isEmpty();
        //setUpMockCreator();
    }

    private void setUpMockCreator() {
        ClassLoader loader = targetClass.getRawClass().getClassLoader();
        try {
            Class<?> mockito = loader.loadClass(Mockito.class.getName());
            mockCreator = mockito.getDeclaredMethod("mock",
                    loader.loadClass(Class.class.getName()), loader.loadClass(MockSettings.class.getName()));

        } catch (Exception e) {
            logger.error("Failed to setup mock creator: " + e.getMessage());
        }
    }

    @Override
    public void changeClassLoader(ClassLoader loader) {

        targetClass.changeClassLoader(loader);
        for (MethodDescriptor descriptor : mockedMethods) {
            if (descriptor != null) {
                descriptor.changeClassLoader(loader);
            }
        }
        if (listener != null) {
            listener.changeClassLoader(loader);
        }
        super.changeClassLoader(loader);
    }

    protected void checkTarget() {
        if (!canBeFunctionalMocked(targetClass.getRawClass())) {
            throw new IllegalArgumentException("Cannot create a basic functional mock for class " + targetClass);
        }
    }

    public static boolean canBeFunctionalMockedIncludingSUT(Type type) {

        Class<?> rawClass = GenericClassFactory.get(type).getRawClass();

        if (EvoSuiteMock.class.isAssignableFrom(rawClass) ||
                MockList.isAMockClass(rawClass.getName()) ||
                rawClass.equals(Class.class) ||
                rawClass.isArray() || rawClass.isPrimitive() || rawClass.isAnonymousClass() ||
                rawClass.isEnum() ||
                //note: Mockito can handle package-level classes, but we get all kinds of weird exceptions with instrumentation :(
                !Modifier.isPublic(rawClass.getModifiers())) {
            return false;
        }

        if (!InstrumentedClass.class.isAssignableFrom(rawClass) &&
                Modifier.isFinal(rawClass.getModifiers())) {
            /*
                if a class has not been instrumented (eg because belonging to javax.*),
                then if it is final we cannot mock it :(
                recall that instrumentation does remove the final modifiers
             */
            return false;
        }

        if (InetSocketAddress.class.equals(rawClass)) {
            /*
             InetSocketAddress declares hashCode as final and thus cannot be mocked:
             https://github.com/mockito/mockito/issues/310
             */
            return false;
        }

        try {
            // If dependencies are missing, this may throw a NoClassDefFoundException
            rawClass.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            AtMostOnceLogger.warn(logger, "Problem with class " + rawClass.getName() + ": " + e);
            return false;
        }

        //avoid cases of infinite recursions
        boolean onlySelfReturns = true;
        for (Method m : rawClass.getDeclaredMethods()) {
            if (!rawClass.equals(m.getReturnType())) {
                onlySelfReturns = false;
                break;
            }
        }

        if (onlySelfReturns && rawClass.getDeclaredMethods().length > 0) {
            //avoid weird cases like java.lang.Appendable
            return false;
        }

        //ad-hoc list of classes we should not really mock
        List<Class<?>> avoid = Arrays.asList(
                //add here if needed
        );

        return !avoid.contains(rawClass);
    }

    public static boolean canBeFunctionalMocked(Type type) {

        Class<?> rawClass = GenericClassFactory.get(type).getRawClass();
        final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

        if (Properties.hasTargetClassBeenLoaded()
                && GenericClassUtils.isAssignable(targetClass, rawClass)) {
            return false;
        }

        return canBeFunctionalMockedIncludingSUT(type);
    }


    public Class<?> getTargetClass() {
        return targetClass.getRawClass();
    }

    public List<MethodDescriptor> getMockedMethods() {
        return mockedMethods;
    }

    public List<VariableReference> getParameters(String id) throws IllegalArgumentException {
        Inputs.checkNull(id);

        int[] minMax = methodParameters.get(id);
        if (minMax == null) {
            return null;
        }

        List<VariableReference> list = new ArrayList<>();
        for (int i = minMax[0]; i <= minMax[1]; i++) {
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
    public boolean doesNeedToUpdateInputs() {
        if (listener == null) {
            /*
                Tricky case: if no execution yet, then there should be no mocked method yet.
                However, this method is also executed when JUnit source code is generated.
                If this is done in a system test for debugging, then it would be a problem,
                as serialized tests sent from Client to Master have no listener (it has to be
                transient). So, we can just skip it, as info used only for debugging.
             */

            assert mockedMethods.isEmpty() || RuntimeSettings.isRunningASystemTest;

            return false;
        }

        List<MethodDescriptor> executed = listener.getCopyOfMethodDescriptors();
        if (executed.size() != mockedMethods.size()) {
            return true;
        }

        for (int i = 0; i < executed.size(); i++) {
            MethodDescriptor previous = mockedMethods.get(i);
            MethodDescriptor now = executed.get(i);

            if (!previous.getID().equals(now.getID())) {
                return true;
            }

            if (!now.shouldBeMocked()) {
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

            if (now.getCounter() != previous.getCounter() &&
                    (now.getCounter() < Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT) ||
                    previous.getCounter() < Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT
            ) {
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
     */
    public List<Type> updateMockedMethods() throws ConstructionFailedException {

        logger.debug("Executing updateMockedMethods. Parameter size: " + parameters.size());

        List<Type> list = new ArrayList<>();

        assert !super.parameters.contains(null);
        assert mockedMethods.size() == methodParameters.size();

        List<VariableReference> copy = new ArrayList<>(super.parameters);
        assert copy.size() == super.parameters.size();

        super.parameters.clear();
        mockedMethods.clear(); //important to remove all the no longer used calls

        Map<String, int[]> mpCopy = new LinkedHashMap<>();

        List<MethodDescriptor> executed = listener.getCopyOfMethodDescriptors();

        int mdIndex = 0;

        for (MethodDescriptor md : executed) {
            mockedMethods.add(md);

            if (!md.shouldBeMocked() || md.getCounter() == 0) {
                //void method or not called, so no parameter needed for it
                mpCopy.put(md.getID(), null);
                continue;
            }

            int added = 0;

            logger.debug("Method called on mock object: " + md.getMethod());

            //infer parameter mapping of current vars from previous execution, if any
            int[] minMax = methodParameters.get(md.getID());
            int existingParameters; //total number of existing parameters
            if (minMax == null) {
                //before it was not called
                minMax = new int[]{-1, -1};
                existingParameters = 0;
            } else {
                assert minMax[1] >= minMax[0] && minMax[0] >= 0;
                assert minMax[1] < copy.size() : "Max=" + minMax[1] + " but n=" + copy.size();
                existingParameters = 1 + (minMax[1] - minMax[0]);
            }

            assert existingParameters <= Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT;

            //check if less calls
            if (existingParameters > md.getCounter()) {
                //now the method has been called less times,
                //so remove the last calls, ie decrease counter
                minMax[1] -= (existingParameters - md.getCounter());
            }

            if (existingParameters > 0) {
                for (int i = minMax[0]; i <= minMax[1]; i++) {
                    //align super class data structure
                    super.parameters.add(copy.get(i));
                    added++;
                }
            }

            //check if rather more calls
            if (existingParameters < md.getCounter()) {
                for (int i = existingParameters; i < md.getCounter() && i < Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT; i++) {
                    // Create a copy as the typemap is stored in the class during generic instantiation
                    // but we might want to have a different type for each call of the same method invocation
                    GenericClass<?> calleeClass = GenericClassFactory.get(retval.getGenericClass());
                    Type returnType = md.getGenericMethodFor(calleeClass).getGeneratedType();
                    assert !returnType.equals(Void.TYPE);
                    logger.debug("Return type: " + returnType + " for retval " + retval.getGenericClass());
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
        for (MethodDescriptor md : mockedMethods) {
            if (!methodParameters.containsKey(md.getID())) {
                methodParameters.put(md.getID(), null);
            }
        }

        return list;
    }

    public void addMissingInputs(List<VariableReference> inputs) throws IllegalArgumentException {
        Inputs.checkNull(inputs);

        logger.debug("Adding {} missing values", inputs.size());

        if (!inputs.isEmpty()) {

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
                logger.debug("Current input: " + ref + " for expected type " + getExpectedParameterType(index));

                assert ref.isAssignableTo(getExpectedParameterType(index));

                parameters.set(index, ref);
            }
        } //else, nothing to add

        //check if all "holes" have been filled
        for (VariableReference ref : parameters) {
            if (ref == null) {
                throw new IllegalArgumentException("Functional mock not fully set with all needed missing inputs");
            }
        }
    }

    public void fillWithNullRefs() {
        for (int i = 0; i < parameters.size(); i++) {
            VariableReference ref = parameters.get(i);
            if (ref == null) {
                Class<?> expected = getExpectedParameterType(i);
                Object value = null;
                if (expected.isPrimitive()) {
                    //can't fill a primitive with null
                    if (expected.equals(Integer.TYPE)) {
                        value = 0;
                    } else if (expected.equals(Float.TYPE)) {
                        value = 0f;
                    } else if (expected.equals(Double.TYPE)) {
                        value = 0d;
                    } else if (expected.equals(Long.TYPE)) {
                        value = 0L;
                    } else if (expected.equals(Boolean.TYPE)) {
                        value = false;
                    } else if (expected.equals(Short.TYPE)) {
                        value = Short.valueOf("0");
                    } else if (expected.equals(Character.TYPE)) {
                        value = 'a';
                    }
                }
                parameters.set(i, new ConstantValue(tc, GenericClassFactory.get(expected), value));
            }
        }
    }


    private Class<?> getExpectedParameterType(int i) {

        for (MethodDescriptor md : mockedMethods) {
            int[] bounds = methodParameters.get(md.getID());
            if (bounds != null && i >= bounds[0] && i <= bounds[1]) {
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
                newTestCase, retval.getType(), targetClass);

        for (VariableReference r : this.parameters) {
            copy.parameters.add(r.copy(newTestCase, offset));
        }

        copy.listener = this.listener; //no need to clone, as only read, and created new instance at each new execution

        for (MethodDescriptor md : this.mockedMethods) {
            copy.mockedMethods.add(md.getCopy());
        }

        for (Map.Entry<String, int[]> entry : methodParameters.entrySet()) {
            int[] array = entry.getValue();
            int[] copiedArray = array == null ? null : new int[]{array[0], array[1]};
            copy.methodParameters.put(entry.getKey(), copiedArray);
        }

        return copy;
    }

    protected EvoInvocationListener createInvocationListener() {
        return new EvoInvocationListener(retval.getGenericClass());
    }

    protected MockSettings createMockSettings() {
        return withSettings().invocationListeners(listener);
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
                    listener = createInvocationListener();

                    //then create the mock
                    Object ret;
                    try {
                        logger.debug("Mockito: create mock for {}", targetClass);

                        ret = mock(targetClass.getRawClass(), createMockSettings());
                        //ret = mockCreator.invoke(null,targetClass,withSettings().invocationListeners(listener));

                        //execute all "when" statements
                        int index = 0;

                        logger.debug("Mockito: going to mock {} different methods", mockedMethods.size());
                        for (MethodDescriptor md : mockedMethods) {

                            if (!md.shouldBeMocked()) {
                                //no need to mock a method that returns void
                                logger.debug("Mockito: method {} cannot be mocked", md.getMethodName());
                                continue;
                            }

                            Method method = md.getMethod(); //target method, eg foo.aMethod(...)

                            // this is needed if method is protected: it couldn't be called here, although fine in
                            // the generated JUnit tests
                            method.setAccessible(true);

                            //target inputs
                            Object[] targetInputs = new Object[md.getNumberOfInputParameters()];
                            for (int i = 0; i < targetInputs.length; i++) {
                                logger.debug("Mockito: executing matcher {}/{}", (1 + i), targetInputs.length);
                                targetInputs[i] = md.executeMatcher(i);
                            }

                            logger.debug("Mockito: going to invoke method {} with {} matchers",
                                    method.getName(), targetInputs.length);

                            if (!method.getDeclaringClass().isAssignableFrom(ret.getClass())) {

                                String msg = "Mismatch between callee's class " + ret.getClass() + " and method's class " +
                                        method.getDeclaringClass();
                                msg += "\nTarget class classloader " + targetClass.getRawClass().getClassLoader() +
                                        " vs method's classloader " + method.getDeclaringClass().getClassLoader();
                                throw new EvosuiteError(msg);
                            }

                            //actual call foo.aMethod(...)
                            Object targetMethodResult;

                            try {
                                if (targetInputs.length == 0) {
                                    targetMethodResult = method.invoke(ret);
                                } else {
                                    targetMethodResult = method.invoke(ret, targetInputs);
                                }
                            } catch (InvocationTargetException e) {
                                logger.error("Invocation of mocked {}.{}() threw an exception. " +
                                        "This means the method was not mocked", targetClass.getClassName(), method.getName());
                                throw e;
                            } catch (IllegalArgumentException | IllegalAccessError e) {
                                // FIXME: Happens for reasons I don't understand. By throwing a CodeUnderTestException EvoSuite
                                // will just ignore that mocking statement and continue, instead of crashing
                                logger.error("IAE on <" + method + "> when called with " + Arrays.toString(targetInputs));
                                throw new CodeUnderTestException(e);
                            }

                            //when(...)
                            logger.debug("Mockito: call 'when'");
                            OngoingStubbing<Object> retForThen = Mockito.when(targetMethodResult);

                            //thenReturn(...)
                            Object[] thenReturnInputs = null;
                            try {
                                int size = Math.min(md.getCounter(), Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT);

                                thenReturnInputs = new Object[size];

                                for (int i = 0; i < thenReturnInputs.length; i++) {

                                    int k = i + index; //the position in flat parameter list
                                    if (k >= parameters.size()) {
                                        //throw new RuntimeException("EvoSuite ERROR: index " + k + " out of " + parameters.size());
                                        throw new CodeUnderTestException(new FalsePositiveException("EvoSuite ERROR: index " + k + " out of " + parameters.size()));
                                    }

                                    VariableReference parameterVar = parameters.get(i + index);
                                    thenReturnInputs[i] = parameterVar.getObject(scope);

                                    CodeUnderTestException codeUnderTestException = null;

                                    if (thenReturnInputs[i] == null && method.getReturnType().isPrimitive()) {
                                        codeUnderTestException = new CodeUnderTestException(new NullPointerException());

                                    } else if (thenReturnInputs[i] != null && !TypeUtils.isAssignable(thenReturnInputs[i].getClass(), method.getReturnType())) {
                                        codeUnderTestException = new CodeUnderTestException(
                                                new UncompilableCodeException("Cannot assign " + parameterVar.getVariableClass().getName()
                                                        + " to " + method.getReturnType()));
                                    }

                                    if (codeUnderTestException != null) {
                                        throw codeUnderTestException;
                                    }

                                    thenReturnInputs[i] = fixBoxing(thenReturnInputs[i], method.getReturnType());
                                }
                            } catch (Exception e) {
                                //be sure "then" is always called after a "when", otherwise Mockito might end up in
                                //a inconsistent state
                                retForThen.thenThrow(new RuntimeException("Failed to setup mock: " + e.getMessage()));
                                throw e;
                            }


                            //final call when(...).thenReturn(...)
                            logger.debug("Mockito: executing 'thenReturn'");
                            if (thenReturnInputs == null || thenReturnInputs.length == 0) {
                                retForThen.thenThrow(new RuntimeException("No valid return value"));
                            } else if (thenReturnInputs.length == 1) {
                                retForThen.thenReturn(thenReturnInputs[0]);
                            } else {
                                Object[] values = Arrays.copyOfRange(thenReturnInputs, 1, thenReturnInputs.length);
                                retForThen.thenReturn(thenReturnInputs[0], values);
                            }

                            index += thenReturnInputs == null ? 0 : thenReturnInputs.length;
                        }

                    } catch (CodeUnderTestException e) {
                        throw e;
                    } catch (java.lang.NoClassDefFoundError e) {
                        AtMostOnceLogger.error(logger, "Cannot use Mockito on " + targetClass + " due to failed class initialization: " + e.getMessage());
                        return; //or should throw an exception?
                    } catch (MockitoException | IllegalAccessException | IllegalAccessError | IllegalArgumentException e) {
                        // FIXME: Happens for reasons I don't understand. By throwing a CodeUnderTestException EvoSuite
                        // will just ignore that mocking statement and continue, instead of crashing
                        AtMostOnceLogger.error(logger, "Cannot use Mockito on " + targetClass + " due to IAE: " + e.getMessage());
                        throw new CodeUnderTestException(e); //or should throw an exception?
                    } catch (Throwable t) {
                        AtMostOnceLogger.error(logger, "Failed to use Mockito on " + targetClass + ": " + t.getMessage());
                        throw new EvosuiteError(t);
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

                /**
                 * a "char" can be used for a "int". But problem is that Mockito takes as input
                 * Object, and so those get boxed. However, a Character cannot be used for a "int",
                 * so we need to be sure to convert it here
                 *
                 * @param value
                 * @param expectedType
                 * @return
                 */
                private Object fixBoxing(Object value, Class<?> expectedType) {

                    if (!expectedType.isPrimitive()) {
                        return value;
                    }

                    Class<?> valuesClass = value.getClass();
                    assert !valuesClass.isPrimitive();

                    if (expectedType.equals(Integer.TYPE)) {
                        if (valuesClass.equals(Character.class)) {
                            value = (int) (Character) value;
                        } else if (valuesClass.equals(Byte.class)) {
                            value = ((Byte) value).intValue();
                        } else if (valuesClass.equals(Short.class)) {
                            value = ((Short) value).intValue();
                        }
                    }

                    if (expectedType.equals(Double.TYPE)) {
                        if (valuesClass.equals(Integer.class)) {
                            value = (double) (Integer) value;
                        } else if (valuesClass.equals(Byte.class)) {
                            value = (double) ((Byte) value).intValue();
                        } else if (valuesClass.equals(Character.class)) {
                            value = (double) (Character) value;
                        } else if (valuesClass.equals(Short.class)) {
                            value = (double) ((Short) value).intValue();
                        } else if (valuesClass.equals(Long.class)) {
                            value = (double) (Long) value;
                        } else if (valuesClass.equals(Float.class)) {
                            value = (double) (Float) value;
                        }
                    }

                    if (expectedType.equals(Float.TYPE)) {
                        if (valuesClass.equals(Integer.class)) {
                            value = (float) (Integer) value;
                        } else if (valuesClass.equals(Byte.class)) {
                            value = (float) ((Byte) value).intValue();
                        } else if (valuesClass.equals(Character.class)) {
                            value = (float) (Character) value;
                        } else if (valuesClass.equals(Short.class)) {
                            value = (float) ((Short) value).intValue();
                        } else if (valuesClass.equals(Long.class)) {
                            value = (float) (Long) value;
                        }
                    }

                    if (expectedType.equals(Long.TYPE)) {
                        if (valuesClass.equals(Integer.class)) {
                            value = (long) (Integer) value;
                        } else if (valuesClass.equals(Byte.class)) {
                            value = (long) ((Byte) value).intValue();
                        } else if (valuesClass.equals(Character.class)) {
                            value = (long) (Character) value;
                        } else if (valuesClass.equals(Short.class)) {
                            value = (long) ((Short) value).intValue();
                        }
                    }

                    if (expectedType.equals(Short.TYPE)) {
                        if (valuesClass.equals(Integer.class)) {
                            value = (short) ((Integer) value).intValue();
                        } else if (valuesClass.equals(Byte.class)) {
                            value = (short) ((Byte) value).intValue();
                        } else if (valuesClass.equals(Short.class)) {
                            value = (short) ((Short) value).intValue();
                        } else if (valuesClass.equals(Character.class)) {
                            value = (short) ((Character) value).charValue();
                        } else if (valuesClass.equals(Long.class)) {
                            value = (short) ((Long) value).intValue();
                        }
                    }

                    if (expectedType.equals(Byte.TYPE)) {
                        if (valuesClass.equals(Integer.class)) {
                            value = (byte) ((Integer) value).intValue();
                        } else if (valuesClass.equals(Short.class)) {
                            value = (byte) ((Short) value).intValue();
                        } else if (valuesClass.equals(Byte.class)) {
                            value = (byte) ((Byte) value).intValue();
                        } else if (valuesClass.equals(Character.class)) {
                            value = (byte) ((Character) value).charValue();
                        } else if (valuesClass.equals(Long.class)) {
                            value = (byte) ((Long) value).intValue();
                        }
                    }

                    return value;
                }


                @Override
                public Set<Class<? extends Throwable>> throwableExceptions() {
                    Set<Class<? extends Throwable>> t = new LinkedHashSet<>();
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

        if (!targetClass.equals(fms.targetClass)) {
            return false;
        }

        if (fms.mockedMethods.size() != mockedMethods.size()) {
            return false;
        }

        for (int i = 0; i < mockedMethods.size(); i++) {
            if (!mockedMethods.get(i).getID().equals(fms.mockedMethods.get(i).getID())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "mock(" + retval.getType() + ")";
    }

    @Override
    public String getDescriptor() {
        return "()L" + PackageInfo.getNameWithSlash(retval.getVariableClass()) + ";";
    }

    @Override
    public String getDeclaringClassName() {
        return retval.getClassName();
    }

    @Override
    public String getMethodName() {
        return "mock";
    }
}
