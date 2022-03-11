/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.util;

import org.evosuite.runtime.RuntimeSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This singleton class is used to handle calls to System.in by
 * replacing them with a smart stub
 *
 * @author arcuri
 */
public class SystemInUtil extends InputStream {

    /**
     * Need to keep reference to original {@code System.in} for
     * when we reset this singleton
     */
    private static final InputStream defaultIn = System.in;

    private static final Logger logger = LoggerFactory.getLogger(SystemInUtil.class);

    private static final SystemInUtil singleton = new SystemInUtil();

    /**
     * Has System.in ever be used by the SUT?
     */
    private volatile boolean beingUsed;

    /**
     * The data that will be taken from System.in
     */
    private volatile List<Byte> data;

    /**
     * the position in the stream
     */
    private volatile AtomicInteger counter;


    /**
     * This is needed to simulate blocking calls when there is
     * no input
     */
    private static final Object monitor = new Object();

    private volatile boolean endReached;

    //--------------------------------

    /**
     * Hidden constructor
     */
    protected SystemInUtil() {
        super();
    }

    public static synchronized SystemInUtil getInstance() {
        return singleton;
    }

    /**
     * Reset the static state be re-instantiate the singleton
     */
    public static synchronized void resetSingleton() {
        singleton.beingUsed = false;
        singleton.data = new ArrayList<>();
        singleton.counter = new AtomicInteger(0);
        singleton.endReached = false;
        System.setIn(defaultIn);
    }

    /**
     * Setup mocked/stubbed System.in for the test case
     */
    public void initForTestCase() {
        data = new ArrayList<>();
        counter = new AtomicInteger(0);
        endReached = false;
        if (RuntimeSettings.mockSystemIn) {
            System.setIn(this);
        }
    }

    /**
     * Use given <code>input</code> string to represent the data
     * that will be provided by System.in.
     * The string will be appended to current buffer as a new line,
     * i.e. by adding "\n" to the <code>input</code> string
     *
     * @param input A string representing an input on the console
     */
    public static void addInputLine(String input) {
        if (input == null) {
            return;
        }

        /*
         * Note: this method needs to be static, as we call it directly in the test cases.
         */

        synchronized (monitor) {
            String line = input + "\n";
            for (byte b : line.getBytes()) {
                singleton.data.add(b);
            }
            singleton.endReached = false;
        }
    }

    @Override
    public int read() throws IOException {

        beingUsed = true;

        synchronized (monitor) {

            int current = counter.get();

            if (Thread.currentThread().isInterrupted()) {
                /*
                 *  if by the time this thread acquires the monitor it has been interrupted,
                 *  and the buffered data is finished, then return -1 to represent the end of
                 *  the stream.
                 *
                 *  Note: the real System.in would not return (would block). Here
                 *  we need to return, otherwise the test case thread would never end
                 */
                return -1;
            }

            while (current >= data.size()) {

                if (!endReached) {
                    endReached = true;
                    return -1;
                }

                /*
                 * instead of having the thread waiting on new input that might never come (eg
                 * if the SUT code is run on same thread as test case, or if there is no console input
                 * in the following test case statements), let's just simulate an exception.
                 */
                throw new IOException("Simulated exception in System.in");
				/*
				try {
					monitor.wait();
				} catch (InterruptedException e) {
					return -1; // simulate end of stream
				}
				*/
            }

            int i = counter.getAndIncrement();

            return (int) data.get(i);
        }
    }

    @Override
    public int available() throws IOException {
        synchronized (monitor) {
            return data.size() - counter.get();
        }
    }

    /**
     * Has there be any call to System.in.read()?
     *
     * @return
     */
    public boolean hasBeenUsed() {
        return beingUsed;
    }

}
