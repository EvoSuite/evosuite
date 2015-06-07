package org.evosuite.testcase;

import org.evosuite.runtime.annotation.*;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
            if (!(st instanceof MethodStatement) && !(st instanceof ConstructorStatement)) {
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


    public static boolean verifyTest(TestChromosome tc){
        return verifyTest(tc.getTestCase());
    }

    /**
     *
     * @param tc
     * @return true if the test case does satisfy all the constraints
     */
    public static boolean verifyTest(TestCase tc){

        Set<Object> seenAtMostOnce = new LinkedHashSet<>();

        for(int i=0; i<tc.size(); i++){
            Statement st = tc.getStatement(i);
            if(! (st instanceof MethodStatement) && ! (st instanceof ConstructorStatement)){
                continue;
            }

            Object reflectionRef = null;
            List<VariableReference> inputs =null;
            Annotation[] methodAnnotations = null;
            Class<?> declaringClass = null;

            if(st instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) st;
                inputs = ms.getParameterReferences();

                Method m = ms.getMethod().getMethod();
                reflectionRef = m;
                methodAnnotations = m.getDeclaredAnnotations();
                declaringClass = m.getDeclaringClass();

            } else if(st instanceof ConstructorStatement){
                ConstructorStatement cs = (ConstructorStatement) st;
                inputs = cs.getParameterReferences();

                Constructor c = cs.getConstructor().getConstructor();
                reflectionRef = c;
                methodAnnotations = c.getDeclaredAnnotations();
                declaringClass = c.getDeclaringClass();
            }

            boolean declaringClassExcluded = isDeclaringExcluded(declaringClass);

            if(declaringClassExcluded){
                if(! hasIncludeAnnotation(methodAnnotations)){
                    logger.error("'excludeClass' constraint violated at position "+i+" in test case:\n"+tc.toCode());
                    return false;
                }
            }

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
                            boolean invalid = false;

                            if(vr instanceof NullReference){
                                invalid = true;
                            }
                            //FIXME check if VariableReferenceImpl

                            if(invalid){
                                logger.error("'noNullInputs' constraint violated at position "+i+" in test case:\n"+tc.toCode());
                                return false;
                            }
                        }
                    }

                    if(c.excludeOthers() != null && c.excludeOthers().length > 0){
                        if (! checkExcludeOthers(tc, i, declaringClass, c)){
                            return false;
                        }
                    }

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

    private static boolean checkAfter(TestCase tc, int i, Class<?> declaringClass, Constraints c) {
        String after = c.after();

        String[] klassAndMethod = getClassAndMethod(after,declaringClass);
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


    private static String[] getClassAndMethod(String s, Class<?> c){
        String klassName = null;
        String methodName = null;
        if(s.contains("#")){
            int pos = s.indexOf('#');
            klassName = s.substring(0,pos);
            methodName = s.substring(pos+1,s.length());
        } else {
            klassName = c.getCanonicalName();
            methodName = s;
        }
        return new String[]{klassName,methodName};
    }

    private static boolean checkExcludeOthers(TestCase tc, int i, Class<?> declaringClass, Constraints c) {

        Statement st = tc.getStatement(i);

        for(String excluded : c.excludeOthers()){
            String[] klassAndMethod = getClassAndMethod(excluded,declaringClass);
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
                if (j==i || !(st instanceof MethodStatement)) {
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
