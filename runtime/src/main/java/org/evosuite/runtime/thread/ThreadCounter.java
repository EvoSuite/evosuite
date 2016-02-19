/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.thread;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.TooManyResourcesException;

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

    public synchronized void checkIfCanStartNewThread() throws TooManyResourcesException{
        if(counter >= RuntimeSettings.maxNumberOfThreads){
            throw new TooManyResourcesException("This test case has tried to start too many threads. "+
                "Maximum allowed per test is "+RuntimeSettings.maxNumberOfThreads+" threads.");
        }
        counter++;
    }
}
