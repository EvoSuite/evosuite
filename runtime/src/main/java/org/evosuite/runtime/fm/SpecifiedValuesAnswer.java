package org.evosuite.runtime.fm;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Specified a list of values, which will be returned in the order they are
 * specified each time an answer is queried.
 * If there are more queries than values, the last value will be returned
 *
 * Created by Andrea Arcuri on 27/07/15.
 */
public class SpecifiedValuesAnswer<T> implements Answer<T>{

    private final T[] values;
    private int counter;

    public SpecifiedValuesAnswer(T... values){
        this.values = values;
        counter = 0;
    }

    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {

        if(values==null || values.length==0){
            /*
                return default value.
                we use 0 instead of null to avoid possible problems with
                unboxing of primitive values (eg int)
             */
            if(values instanceof Integer[]){
                return (T) new Integer(0);
            } else if(values instanceof Double[]){
                return (T) new Double(0);
            } else if(values instanceof Float[]){
                return (T) new Float(0);
            } else if(values instanceof Long[]){
                return (T) new Long(0);
            } else if(values instanceof Short[]){
                return (T) new Short((short)0);
            } else {
                return null;
            }
        }

        if(counter >= values.length){
            //return the last element
            return values[values.length-1];
        }

        return values[counter++];
    }
}
