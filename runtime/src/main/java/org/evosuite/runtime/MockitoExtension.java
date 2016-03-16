package org.evosuite.runtime;

import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;

public class MockitoExtension {


    /**
     * Extend Mockito API by allowing an arbitrary number of inputs to
     * the method {@code doReturn()}
     *
     * @param value
     * @param values
     * @return
     */
    public static Stubber doReturn(Object value, Object... values){

        Stubber stubber = Mockito.doReturn(value);
        for(Object v : values){
            stubber = stubber.doReturn(v);
        }

        return stubber;
    }
}
