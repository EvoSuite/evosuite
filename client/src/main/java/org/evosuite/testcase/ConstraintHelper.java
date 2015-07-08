package org.evosuite.testcase;

import org.evosuite.runtime.annotation.Constraints;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to help the verification and proper use of constraints
 *
 * Created by Andrea Arcuri on 29/06/15.
 */
public class ConstraintHelper {


    public static int countNumberOfNewInstances(TestCase test, Class<?> klass) throws IllegalArgumentException{
        Inputs.checkNull(test,klass);

        int counter = 0;

        for(int i=0; i<test.size(); i++){
            Statement st = test.getStatement(i);
            if(st instanceof ConstructorStatement){
                ConstructorStatement cs = (ConstructorStatement) st;
                if(klass.isAssignableFrom(cs.getConstructor().getDeclaringClass())){
                    counter++;
                }
            }
        }

        return counter;
    }

    /**
     * This ignores the input parameters
     *
     * @param test
     * @param klass
     * @param methodName
     * @return
     * @throws IllegalArgumentException
     */
    public static int countNumberOfMethodCalls(TestCase test, Class<?> klass, String methodName) throws IllegalArgumentException {
        Inputs.checkNull(test, klass);
        int counter = 0;
        for (int i = 0; i < test.size(); i++) {
            Statement st = test.getStatement(i);
            if (st instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) st;
                GenericMethod gm = ms.getMethod();
                if(gm.getDeclaringClass().equals(klass) && gm.getName().equals(methodName)){
                    counter++;
                }
            }
        }

        return counter;
    }

    /**
     *
     * @param test
     * @param className
     * @param methodName
     * @return a negative value if it is not present
     */
    public static int getLastPositionOfMethodCall(TestCase test, String className, String methodName, int lastPosition){
        Inputs.checkNull(test,className,methodName);

        int pos = -1;
        for (int i = 0; i < lastPosition; i++) {
            Statement st = test.getStatement(i);
            if (st instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) st;
                GenericMethod gm = ms.getMethod();
                if(gm.getDeclaringClass().getCanonicalName().equals(className) && gm.getName().equals(methodName)){
                    pos = i;
                }
            }
        }

        return pos;
    }

    /**
     *
     * @param s
     * @param c
     * @return an array of size 2
     */
    public static String[] getClassAndMethod(String s, Class<?> c) {
        String klassName = null;
        String methodName = null;
        if (s.contains("#")) {
            int pos = s.indexOf('#');
            klassName = s.substring(0, pos);
            methodName = s.substring(pos + 1, s.length());
        } else {
            klassName = c.getCanonicalName();
            methodName = s;
        }
        return new String[]{klassName, methodName};
    }

    /**
     * Retrieve all blacklisted methods in the test.
     * This is based on the 'excludeOthers' constraint
     *
     * @param tc
     * @return
     * @throws IllegalArgumentException
     */
    public static List<String[]> getExcludedMethods(TestCase tc) throws IllegalArgumentException{
        Inputs.checkNull(tc);

        List<String[]> list = new ArrayList<>();
        for(int i=0; i<tc.size(); i++){
            Statement st = tc.getStatement(i);
            Constraints constraints = getConstraints(st);
            if(constraints==null){
                continue;
            }

            Class<?> declaringClass = st.getAccessibleObject().getDeclaringClass();

            for(String excluded : constraints.excludeOthers()) {
                String[] klassAndMethod = getClassAndMethod(excluded, declaringClass);
                list.add(klassAndMethod);
            }
        }

        return list;
    }

    public static Constraints getConstraints(Statement st){
        if(st instanceof MethodStatement){
            return ((MethodStatement) st).getMethod().getMethod().getAnnotation(Constraints.class);
        } else if(st instanceof ConstructorStatement){
            return ((ConstructorStatement)st).getConstructor().getConstructor().getAnnotation(Constraints.class);
        } else {
            return null;
        }
    }
}
