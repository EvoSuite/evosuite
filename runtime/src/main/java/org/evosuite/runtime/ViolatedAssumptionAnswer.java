package org.evosuite.runtime;

import org.evosuite.runtime.FalsePositiveException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * If a new method is called on mocked object that is different from what was used
 * when the test was generated, then ignore the test, as likely it will be a false positive
 *
 * @param <T>
 */
public class ViolatedAssumptionAnswer<T> implements Answer<T> {

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {

        if(invocation.getMethod().getReturnType().equals(Void.TYPE)) {
            //no need of exception, as no return value will be used in the CUT anyway which could affect the test
            return null;
        } else {
            throw new FalsePositiveException("Mock call to "+invocation.getMethod().getName()+
                    " which was not presented when the test was generated");
        }
    }
}
