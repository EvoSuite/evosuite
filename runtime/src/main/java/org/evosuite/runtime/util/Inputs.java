package org.evosuite.runtime.util;

/**
 * Created by Andrea Arcuri on 09/06/15.
 */
public class Inputs {

    /**
     *  Check for NPE but using IAE with proper error message
     *
     * @param inputs
     * @throws IllegalArgumentException
     */
    public static void checkNull(Object... inputs) throws IllegalArgumentException{
        if(inputs == null){
            throw new IllegalArgumentException("No inputs to check");
        }
        for(Object obj : inputs){
            if(obj==null){
                throw new IllegalArgumentException("Null input");
            }
        }
    }
}
