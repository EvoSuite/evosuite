package org.evosuite.testcase;

import org.evosuite.runtime.util.Inputs;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.utils.GenericMethod;
import org.junit.Test;

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
                if(cs.getConstructor().getDeclaringClass().equals(klass)){
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
}
