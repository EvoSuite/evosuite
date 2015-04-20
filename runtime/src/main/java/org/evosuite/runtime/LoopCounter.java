package org.evosuite.runtime;

import java.util.LinkedList;
import java.util.List;

/**
 * In some cases, we can end up with infinite loops: eg due to a bug, a seeded mutation,
 * a very large input parameter, or simply it is the expected behavior of the CUT.
 * In such cases, we will every time hit the test timeout, which will hinder the search.
 *
 * <p>
 * Therefore, for each loop in the instrumented CUTs, we can have a limit, and throw an exception
 * if too many iterations have occurred
 *
 * Created by Andrea Arcuri on 29/03/15.
 */
public class LoopCounter {

    private static final LoopCounter singleton = new LoopCounter();

    /**
     * Number of iterations so far
     */
    private List<Long> counters;


    private LoopCounter(){
        counters = new LinkedList<>();
    }

    public static LoopCounter getInstance(){
        return singleton;
    }

    public void reset(){
        counters.clear();
    }

    /**
     * This is called during bytecode instrumentation to determine which index
     * to assign to a new parsed loop
     *
     * @return the next valid index for a new loop
     */
    public int getNewIndex(){
        int index = counters.size();
        counters.add(0l);
        return index;
    }


    /**
     * This is added directly in the instrumented CUT after each loop statement
     *
     * @param index
     * @throws TooManyResourcesException if this loop has executed too many iterations
     * @throws IllegalArgumentException
     */
    public void checkLoop(int index) throws TooManyResourcesException, IllegalArgumentException{
        if(index < 0){
            throw new IllegalArgumentException("Loop index cannot be negative");
        }

        if(RuntimeSettings.maxNumberOfIterationsPerLoop < 0){
            return; //do nothing, no check
        }

        //first check initialization
        int size = counters.size();
        if(index >= size){
            for(int i=0; i < 1 + (index - size); i++){
                counters.add(0l);
            }
        }

        //do increment
        long value = counters.get(index) + 1l;
        counters.set(index , value);

        if(value >= RuntimeSettings.maxNumberOfIterationsPerLoop){
            throw new TooManyResourcesException("Loop has been executed more times than the allowed " +
                    RuntimeSettings.maxNumberOfIterationsPerLoop);
        }
    }
}
