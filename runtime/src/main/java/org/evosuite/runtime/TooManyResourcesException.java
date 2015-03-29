package org.evosuite.runtime;

/**
 * This exception is thrown by the EvoSuite framework when a test case uses too many resources.
 * These resources are for example number of threads and number of iterations in loops.
 * This is done to avoid very expensive test cases, although technically it does not represent
 * a bug in the class under test.
 *
 * Created by Andrea Arcuri on 29/03/15.
 */
public class TooManyResourcesException extends RuntimeException{

    public TooManyResourcesException(){
        super();
    }

    public TooManyResourcesException(String msg){
        super(msg);
    }
}
