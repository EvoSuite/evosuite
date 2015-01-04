package org.evosuite.runtime.thread;

import org.evosuite.runtime.RuntimeSettings;

/**
 * In a JUnit test, we do not want to start hundreds/thousands of
 * threads. This could happen if bug in SUT, or if we call it with
 * some specific parameters.
 * So, if a test starts too many threads, just throw an exception.
 * This is particularly useful when running tests in parallel,
 * or in a CI server.
 *
 *
 * Created by arcuri on 9/25/14.
 */
public class ThreadCounter {

    private static final ThreadCounter singleton = new ThreadCounter();

    private volatile int counter;

    private ThreadCounter(){
        resetSingleton();
    }

    public static ThreadCounter getInstance(){
        return singleton;
    }

    public synchronized  void resetSingleton(){
        counter = 0;
    }

    public synchronized void checkIfCanStartNewThread() throws RuntimeException{
        if(counter == RuntimeSettings.maxNumberOfThreads){
            throw new RuntimeException("This test case has tried to start too many threads. "+
                "Maximum allowed per test is "+RuntimeSettings.maxNumberOfThreads+" threads.");
        }
        counter++;
    }
}
