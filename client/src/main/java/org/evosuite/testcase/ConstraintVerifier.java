/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.runtime.annotation.*;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Class used to verify that the constraints on the usage of the external
 * resources (mock environment and JavaEE) are properly satisfied.
 *
 * <p>The checks are mainly for debugging EvoSuite, as constraints should
 * always hold by construction. Here we are just interested to see if any
 * mutation does break the constraints </p>
 *
 * <p>Note: checks here are lightweight (ie not 100% precise), and only
 * check what is in the test, not what should had been there </p>
 *
 * Created by Andrea Arcuri on 06/06/15.
 */
public class ConstraintVerifier {

    private static final Logger logger = LoggerFactory.getLogger(ConstraintVerifier.class);

    /**
     * During the search, assertions have not been generated yet.
     * Here, do check if any method, that is only for assertions, was
     * included in the test
     *
     * @param tc
     * @return
     */
    public static boolean hasAnyOnlyForAssertionMethod(TestCase tc){

        for(int i=0; i<tc.size(); i++) {
            Statement st = tc.getStatement(i);

            if (! canStatementHaveConstraints(st)){
                continue;
            }

            AccessibleObject ao = null;
            if(st instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) st;
                ao = ms.getMethod().getMethod();
            } else if(st instanceof ConstructorStatement){
                ConstructorStatement cs = (ConstructorStatement) st;
                ao = cs.getConstructor().getConstructor();
            }

            for(Annotation annotation : ao.getDeclaredAnnotations()){
                if(annotation instanceof EvoSuiteAssertionOnly){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Can the statement at the given position be deleted?
     * A case in which it is not possible is for example if it is
     * using an existing bounded variable
     *
     * @param tc
     * @param pos
     * @return
     */
    public static boolean canDelete(TestCase tc, int pos) throws IllegalArgumentException {
        return dependentPositions(tc,pos).isEmpty();
    }

    public static Set<Integer> dependentPositions(TestCase tc, int pos) throws IllegalArgumentException{
        Inputs.checkNull(tc);

        Set<Integer> dep = new LinkedHashSet<>();

        Statement st = tc.getStatement(pos);

        if(! canStatementHaveConstraints(st)){
            /*
                the statement itself has no constraints.
                however, others might have dependencies on it.
                An example is methods with "noNullInput" using it
             */
            VariableReference ret = st.getReturnValue();

            for (int i = pos + 1; i < tc.size(); i++) {
                Statement toCheck = tc.getStatement(i);
                Constraints constraint = ConstraintHelper.getConstraints(toCheck);
                if(constraint==null || !constraint.noNullInputs()){
                    continue;
                }
                if(! (toCheck instanceof EntityWithParametersStatement)){
                    continue;
                }
                EntityWithParametersStatement entity = (EntityWithParametersStatement) toCheck;
                for(VariableReference input : entity.getParameterReferences()){
                    if(input.same(ret)){
                        //var is used as input in a method that accepts no null input, so cannot be deleted
                        dep.add(i);
                    }
                }
            }

            return dep;
        }

        //first look at bounded variables
        for(Annotation[] array : getParameterAnnotations(st)){
            for(int i=0; i<array.length; i++){
                Annotation an = array[i];
                if(an instanceof BoundInputVariable){

                    EntityWithParametersStatement e = (EntityWithParametersStatement) st;
                    int boundingVarPos = e.getParameterReferences().get(i).getStPosition();
                    dep.add(boundingVarPos);
                }
            }
        }

        //check if there is an 'after' constraint
        if(st instanceof MethodStatement) {
            MethodStatement current = (MethodStatement) st;
            String currentKlassName = current.getMethod().getDeclaringClass().getCanonicalName();
            String currentMethodName = current.getMethod().getName();

            for (int i = pos + 1; i < tc.size(); i++) {
                Statement toCheck = tc.getStatement(i);
                Constraints constraints = ConstraintHelper.getConstraints(toCheck);
                if (constraints == null) {
                    continue;
                }
                String after = constraints.after();
                if (after == null || after.trim().isEmpty()) {
                    continue;
                }

                MethodStatement ms = (MethodStatement) toCheck;
                String[] klassAndMethod = ConstraintHelper.getClassAndMethod(after, ms.getMethod().getDeclaringClass());
                String afterKlassName = klassAndMethod[0];
                String afterMethodName = klassAndMethod[1];

                if(afterKlassName.equals(currentKlassName) && afterMethodName.equals(currentMethodName)){
                    dep.add(i);
                }
            }
        }
        return dep;
    }

    public static boolean isValidPositionForInsertion(GenericAccessibleObject<?> obj, TestCase tc, int pos)
        throws IllegalArgumentException{

        Inputs.checkNull(obj,tc);

        /*
            if the given 'obj' (a method/constructor) belongs to a class for which there is an instance
            before "pos" which is bounded after "pos", then we cannot add it, as could break bounding
            constraints if such instance is chosen as callee for "obj". Note: we could force to never
            use such instance (ie use another one if exists, or create it), but that would complicate
            a lot all the algorithms in the test factory :(
         */
        List<VariableReference> possibleCallees = tc.getObjects(obj.getOwnerType(), pos);
        for(VariableReference ref : possibleCallees){
            int boundPos = ConstraintHelper.getLastPositionOfBounded(ref, tc);
            if(boundPos >= pos){
                return false;
            }
        }

        Constraints constraints = obj.getAccessibleObject().getAnnotation(Constraints.class);
        if(constraints == null){
            return true;
        }

        if(! canBeInsertedRegardlessOfPosition(obj, tc)){
            return false;
        }

        int minPos = getMinPosForAfter(obj,tc,tc.size());
        if(minPos < 0 || pos < minPos){
            return false;
        }

        return true;
    }

    /**
     *
     * @param obj
     * @param tc
     * @param lastValid
     * @return position where the object can be inserted, otherwise a negative value if no insertion is possible
     * @throws IllegalArgumentException
     */
    public static int getAValidPositionForInsertion(GenericAccessibleObject<?> obj, TestCase tc, int lastValid) throws IllegalArgumentException{
        Inputs.checkNull(obj,tc);

        Constraints constraints = obj.getAccessibleObject().getAnnotation(Constraints.class);
        if(constraints == null){
            if(lastValid <= 0){
                return 0;
            }
            return Randomness.nextInt(0,lastValid);
        }

        if(! canBeInsertedRegardlessOfPosition(obj, tc)){
            return -1;
        }

        //TODO
        //bounded

        int minPos = getMinPosForAfter(obj, tc, lastValid);

        if(minPos < 0){
            return -1;
        } else if(minPos > 0) {
            return minPos; //try to add immediately 'after' the constraining method
        } else {
            assert minPos==0;
            if(lastValid<=0){
                return 0;
            }
            return Randomness.nextInt(0,lastValid);
        }
    }

    private static int getMinPosForAfter(GenericAccessibleObject<?> obj, TestCase tc, int lastValid){

        Constraints constraints = obj.getAccessibleObject().getAnnotation(Constraints.class);
        Class<?> declaringClass = obj.getDeclaringClass();

        //after
        int minPos = 0;
        String after = constraints.after();
        if(after!=null && !after.isEmpty()){
            String[] pair = ConstraintHelper.getClassAndMethod(after,declaringClass);

            int afterPos = ConstraintHelper.getLastPositionOfMethodCall(tc,pair[0],pair[1],lastValid);
            if(afterPos < 0){
                /*
                    The current method cannot be inserted, because it has to be 'after' X, but X is not in the test
                 */
                return -1;
            }
            minPos = afterPos+1;
        }

        return minPos;
    }

    private static boolean canBeInsertedRegardlessOfPosition(GenericAccessibleObject<?> obj, TestCase tc){

        Constraints constraints = obj.getAccessibleObject().getAnnotation(Constraints.class);
        if(constraints == null){
            return true;
        }

        if(constraints.noDirectInsertion()){
            return false;
        }

        Class<?> declaringClass = obj.getDeclaringClass();
        String declaringClassName = declaringClass.getCanonicalName();
        String name = obj.getName();

        //check atMostOnce
        if(constraints.atMostOnce()){
            int counter = ConstraintHelper.countNumberOfMethodCalls(tc,declaringClass,name);
            if(counter == 1){
                //cannot insert it again
                return false;
            } else if(counter > 1){
                throw new RuntimeException("Violated 'atMostOnce' constraint for "+obj.getName());
            }
        }

        //excludeOthers
        List<String[]> othersExcluded = ConstraintHelper.getExcludedMethods(tc);
        if(othersExcluded != null && othersExcluded.size() > 0){
            for(String[] pair : othersExcluded){
                if(pair[0].equals(declaringClassName) && pair[1].equals(name)){
                    //this method/constructor cannot be added
                    return false;
                }
            }
        }

        //dependOnProperties
        String[] properties = constraints.dependOnProperties();
        if(properties!=null && properties.length>0){
            for(String property : properties){
                if(! tc.getAccessedEnvironment().hasProperty(property)){
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean verifyTest(TestChromosome tc){
        return verifyTest(tc.getTestCase());
    }

    /**
     *
     * @param tc
     * @return true if the test case does satisfy all the constraints
     */
    public static boolean verifyTest(TestCase tc) throws IllegalArgumentException{
        Inputs.checkNull(tc);

        Set<Object> seenAtMostOnce = new LinkedHashSet<>();

        //look at each statement in the test case, one at a time
        for(int i=0; i<tc.size(); i++){
            Statement st = tc.getStatement(i);

            if (! canStatementHaveConstraints(st)){
                continue;
            }

            if(! checkFunctionalMockUsage(st, tc)){
                return false;
            }

            //data we need for calculations
            Object reflectionRef = null;
            List<VariableReference> inputs = null;
            List<VariableReference> boundedInitializingInputs = null;
            Annotation[] methodAnnotations = null;
            Annotation[][] parameterAnnotations = null;
            Class<?> declaringClass = null;

            //init data based on whether current statement is a constructor or regular method
            if(st instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) st;
                inputs = ms.getParameterReferences();

                Method m = ms.getMethod().getMethod();
                reflectionRef = m;
                methodAnnotations = m.getDeclaredAnnotations();
                declaringClass = m.getDeclaringClass();
                parameterAnnotations = m.getParameterAnnotations();

                boundedInitializingInputs = getBoundedInitializingVariables(inputs, parameterAnnotations);

                if(! checkBoundedVariableAtMostOnce(tc,i,ms)){
                    return false;
                }

            } else if(st instanceof ConstructorStatement){
                ConstructorStatement cs = (ConstructorStatement) st;
                inputs = cs.getParameterReferences();

                Constructor c = cs.getConstructor().getConstructor();
                reflectionRef = c;
                methodAnnotations = c.getDeclaredAnnotations();
                declaringClass = c.getDeclaringClass();
                parameterAnnotations = c.getParameterAnnotations();

                boundedInitializingInputs = getBoundedInitializingVariables(inputs, parameterAnnotations);
            }


            //should the method had been directly excluded from the tests?
            boolean declaringClassExcluded = isDeclaringExcluded(declaringClass);
            if(declaringClassExcluded){
                if(! hasIncludeAnnotation(methodAnnotations)){
                    logger.error("'excludeClass' constraint violated at position "+i+" in test case:\n"+tc.toCode());
                    return false;
                }
            }

            //the method is an initializer for some bounded variable
            if(! boundedInitializingInputs.isEmpty()){
                for(VariableReference vr : boundedInitializingInputs){
                    if(!checkInitializingBoundedVariable(tc,i,vr)){
                        return false;
                    }
                }
            }

            //look at each annotation on the method
            for(Annotation annotation : methodAnnotations){
                if(annotation instanceof EvoSuiteExclude  && declaringClassExcluded){
                    logger.error("Wrong constraints: class "+declaringClass.getName()+" is a " +
                            EvoSuiteClassExclude.class.getSimpleName() + " but uses " + EvoSuiteExclude.class.getSimpleName() +
                            " on the method" + reflectionRef.toString());
                    return false;
                }
                if(annotation instanceof EvoSuiteInclude  && !declaringClassExcluded){
                    logger.error("Wrong constraints: class "+declaringClass.getName()+" is not a " +
                            EvoSuiteClassExclude.class.getSimpleName() + " but uses " + EvoSuiteInclude.class.getSimpleName() +
                            " on the method" + reflectionRef.toString());
                    return false;
                }

                if(annotation instanceof EvoSuiteExclude){
                    logger.error("'excludeMethod' constraint violated at position "+i+" in test case:\n"+tc.toCode());
                    return false;
                }

                //found an annotation defining one or more constraints
                if(annotation instanceof Constraints){
                    Constraints c = (Constraints) annotation;

                    //check for methods that should appear only once
                    if(c.atMostOnce()){
                        if(seenAtMostOnce.contains(reflectionRef)){
                            logger.error("'atMostOne' constraint violated at position "+i+" in test case:\n"+tc.toCode());
                            return false;
                        }
                        seenAtMostOnce.add(reflectionRef);
                    }

                    //check for methods that should have no null inputs
                    if(c.noNullInputs()){
                        for(VariableReference vr : inputs){
                            boolean invalid = ConstraintHelper.isNull(vr,tc);

                            if(invalid){
                                logger.error("'noNullInputs' constraint violated at position "+i+" in test case:\n"+tc.toCode());
                                return false;
                            }
                        }
                    }

                    //'excludeOthers' constraint check
                    if(c.excludeOthers() != null && c.excludeOthers().length > 0){
                        if (! checkExcludeOthers(tc, i, declaringClass, c)){
                            return false;
                        }
                    }

                    //'after' constraint check
                    if(c.after() != null && !c.after().trim().isEmpty()){
                        if (! checkAfter(tc, i, declaringClass, c)){
                            return false;
                        }
                    }

                    break;
                }
            }
        }

        return true; //everything was OK
    }

    /**
     * No method should be called on the return value of a mock creation.
     * This is because a functional mock object should only be used as an input value.
     * Calling any method on it would make no sense
     *
     * @param st
     */
    private static boolean checkFunctionalMockUsage(Statement st, TestCase tc) {

        if(! (st instanceof MethodStatement)){
            return true;
        }

        MethodStatement ms = (MethodStatement) st;
        VariableReference callee = ms.getCallee();
        if(callee==null){
            //ie, static method
            return true;
        }

        Statement source = tc.getStatement(callee.getStPosition());
        if(source instanceof FunctionalMockStatement){
            logger.error("Mock object created at position "+source.getPosition()+" has a method called in position "+
                st.getPosition());
            return false;
        }

        return true;
    }

    private static Annotation[][] getParameterAnnotations(Statement st){
        if(st instanceof MethodStatement){
            return ((MethodStatement) st).getMethod().getMethod().getParameterAnnotations();
        } else if(st instanceof ConstructorStatement){
            return ((ConstructorStatement)st).getConstructor().getConstructor().getParameterAnnotations();
        } else {
            return null;
        }
    }


    private static AccessibleObject getAccessibleObject(Statement st){
        if(st instanceof MethodStatement){
            return ((MethodStatement) st).getMethod().getMethod();
        } else if(st instanceof ConstructorStatement){
            return ((ConstructorStatement)st).getConstructor().getConstructor();
        } else {
            return null;
        }
    }

    private static boolean canStatementHaveConstraints(Statement st) {
        //constraints are defined only on methods and constructors (eg, no primitive variable field declarations)
        if(! (st instanceof MethodStatement) && ! (st instanceof ConstructorStatement)){
            return false;
        }
        return true;
    }

    private static boolean checkBoundedVariableAtMostOnce(TestCase tc, int i, MethodStatement ms) {

        Annotation[][] annotations = ms.getMethod().getMethod().getParameterAnnotations();
        List<VariableReference> inputs = ms.getParameterReferences();
        List<VariableReference> atMostOnce = new ArrayList<>();

        //check if input method has any bounded variable declared as atMostOnce
        outer : for(int j=0; j<annotations.length; j++){
            Annotation[] array = annotations[j];
            for(Annotation annotation : array){
                if(annotation instanceof BoundInputVariable){
                    BoundInputVariable biv = (BoundInputVariable) annotation;
                    if(biv.atMostOnce()){
                        atMostOnce.add(inputs.get(j));
                        continue outer;
                    }
                }
            }
        }

        if(atMostOnce.isEmpty()){
            return true;
        }

        for(int j=i-1; j>=0; j--){
            Statement st = tc.getStatement(j);
            if(! (st instanceof MethodStatement)){
                continue;
            }
            MethodStatement other = (MethodStatement) st;

            if(! other.getMethod().getMethod().equals( ms.getMethod().getMethod())){
                continue;
            }

            //ok, same method. but is it called with the same bounded variables?
            for(VariableReference ref : other.getParameterReferences()){
                for(VariableReference bounded : atMostOnce){
                    if(ref.same(bounded)){
                        logger.error("Bounded variable declared in "+ref.getStPosition()+" can only be used once as input for the " +
                                "method "+other.getMethod().getName()+" : it is wrongly used both at position "+j+" and "+i);
                        return false;
                    }
                }
            }
        }

        return true;
    }


    private static boolean checkInitializingBoundedVariable(TestCase tc, int i, VariableReference vr) {

        for(int j = i-1; j>vr.getStPosition() ; j--){
            Statement st = tc.getStatement(j);

            MethodStatement ms = null;
            ConstructorStatement cs = null;
            Annotation[][] annotations = null;
            List<VariableReference> inputs = null;

            if(st instanceof MethodStatement){
                ms = (MethodStatement) st;
                annotations = ms.getMethod().getMethod().getParameterAnnotations();
                inputs = ms.getParameterReferences();

                //is any other method of the bounded variable been called?
                VariableReference callee = ms.getCallee();
                if(vr.same(callee)){
                    logger.error("Invalid method call at position "+j+
                            " on bounded variable created in "+vr.getStPosition()+" " +
                            "and initialized in "+i +
                            "\nTest case code:\n" + tc.toCode());
                    return false;
                }
            }

            if(st instanceof ConstructorStatement){
                cs = (ConstructorStatement) st;
                annotations = cs.getConstructor().getConstructor().getParameterAnnotations();
                inputs = cs.getParameterReferences();
            }

            if(inputs==null || inputs.isEmpty()){
                continue;
            }

            //is the bounded variable used as input in another method?
            outer : for(int k=0; k<inputs.size(); k++){

                VariableReference input = inputs.get(k);
                Annotation[] varAnns = annotations[k];
                for(Annotation ann : varAnns){
                    if(ann instanceof BoundInputVariable){
                        continue outer;  // it is fine if bounded variable is used several methods (eg injectors for each field)
                    }
                }

                if (vr.same(input)) {
                    logger.error("Bounded variable of type " + vr.getType() +
                            " created at position " + vr.getStPosition() + " is used as input in " +
                            j + " before its bounding initializer at position " + i +
                            ". Statement at position "+j+" is:\n"+tc.getStatement(j).getCode() +
                            "\nTest case code:\n" + tc.toCode());
                    return false;
                }
            }
        }

        Statement declaration = tc.getStatement(vr.getStPosition());
        if(! (declaration instanceof ConstructorStatement)){
            logger.error("Bounded variable is declared in "+vr.getStPosition()+" but not with a 'new' constructor." +
            "Statement:\n"+declaration+"\nTest code:\n"+tc.toCode());
            return false;
        }

        return true;
    }

    /**
     * This method assumes the two data structures are aligned
     *
     * @param inputs
     * @param annotations
     * @return
     */
    private static List<VariableReference> getBoundedInitializingVariables(List<VariableReference> inputs, Annotation[][] annotations) {

        List<VariableReference> bounded = new ArrayList<>();

        outer : for(int i=0; i<inputs.size(); i++){
            Annotation[] array = annotations[i];
            for(Annotation annotation : array){
                if(annotation instanceof BoundInputVariable){
                    BoundInputVariable biv = (BoundInputVariable) annotation;
                    if(biv.initializer()){
                        bounded.add(inputs.get(i));
                        continue  outer;
                    }
                }
            }
        }

        return bounded;
    }

    private static boolean checkAfter(TestCase tc, int i, Class<?> declaringClass, Constraints c) {
        String after = c.after();

        String[] klassAndMethod = ConstraintHelper.getClassAndMethod(after, declaringClass);
        String afterKlassName = klassAndMethod[0];
        String afterMethodName = klassAndMethod[1];

        for(int j=i-1; j>=0 ; j--){
            Statement previous = tc.getStatement(j);
            if(! (previous instanceof MethodStatement)){
                continue;
            }

            MethodStatement ms = (MethodStatement) previous;
            if(ms.getMethod().getName().equals(afterMethodName) &&
                    ms.getMethod().getDeclaringClass().getName().equals(afterKlassName)){
                //found it. it is in the test before the statement.
                return true;
            }
        }

        logger.error("'after' constraint violated at position "+i+". Not found previous call to '"+
                    after + "' in test case:\n"+tc.toCode());
        return false;
    }


    private static boolean hasIncludeAnnotation(Annotation[] methodAnnotations) {
        for(Annotation annotation : methodAnnotations){
            if(annotation instanceof EvoSuiteInclude){
                return true;
            }
        }
        return false;
    }


    private static boolean isDeclaringExcluded(Class<?> declaringClass) {
        Annotation ann = declaringClass.getAnnotation(EvoSuiteClassExclude.class);
        return ann != null;
    }


    private static boolean checkExcludeOthers(TestCase tc, int i, Class<?> declaringClass, Constraints c) {

        Statement st = tc.getStatement(i);

        for(String excluded : c.excludeOthers()){
            String[] klassAndMethod = ConstraintHelper.getClassAndMethod(excluded, declaringClass);
            String klassName = klassAndMethod[0];
            String excludedName = klassAndMethod[1];

            //check if it exists, ie whether the constraint is valid
            try {
                Class<?> klass = Class.forName(klassName);
                boolean found = false;
                for(Method k : klass.getDeclaredMethods()){
                    if(k.getName().equals(excludedName)){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    logger.error("Invalid constraint definition for " +
                            declaringClass.getCanonicalName()+". The excluded method "
                            +excludedName+" does not exist.");
                    return false;
                }
            } catch (ClassNotFoundException e) {
                logger.error("Invalid constraint definition for " +
                        declaringClass.getCanonicalName()+". The excluded method in class "
                        +klassName+" does not exist.");
                return false;
            }

            //look at all the other statements
            for(int j=0; j<tc.size(); j++) {
                Statement other = tc.getStatement(j);
                if (j==i || !(other instanceof MethodStatement)) {
                    continue;
                }
                MethodStatement oms = (MethodStatement) other;
                if(oms.getMethod().getName().equals(excludedName) &&
                        oms.getMethod().getDeclaringClass().getName().equals(klassName)){
                    logger.error("'excludeOthers' constraint violated at position "+i+" in test case:\n"+tc.toCode());
                    return false;
                }
            }
        }
        return true;
    }
}
