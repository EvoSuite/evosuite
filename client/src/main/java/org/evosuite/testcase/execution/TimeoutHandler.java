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
package org.evosuite.testcase.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.*;

public class TimeoutHandler<T> {

    /**
     * <p>
     * Constructor for TimeoutHandler.
     * </p>
     *
     * @param <T> a T object.
     */
    public TimeoutHandler() {
        super();
        this.bean = ManagementFactory.getThreadMXBean();
    }

    protected FutureTask<T> task = null;
    private final ThreadMXBean bean;

    /**
     * Constant <code>logger</code>
     */
    protected static final Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);

    /**
     * <p>
     * getLastTask
     * </p>
     *
     * @return a {@link java.util.concurrent.FutureTask} object.
     */
    public FutureTask<T> getLastTask() {
        return task;
    }

    /**
     * <p>
     * execute
     * </p>
     *
     * @param testcase             a {@link java.util.concurrent.Callable} object.
     * @param executor             a {@link java.util.concurrent.ExecutorService} object.
     * @param timeout              a long.
     * @param timeout_based_on_cpu a boolean.
     * @return a T object.
     * @throws java.util.concurrent.TimeoutException   if any.
     * @throws java.lang.InterruptedException          if any.
     * @throws java.util.concurrent.ExecutionException if any.
     */
    public T execute(final Callable<T> testcase, ExecutorService executor, long timeout,
                     boolean timeout_based_on_cpu) throws TimeoutException, InterruptedException,
            ExecutionException {
        if (!bean.isCurrentThreadCpuTimeSupported() && timeout_based_on_cpu) {
            timeout_based_on_cpu = false;
            logger.warn("Requested to use timeout_based_on_cpu, but it is not supported by the JVM/OS");
        }

        if (!timeout_based_on_cpu) {
            return executeWithTimeout(testcase, executor, timeout);
        } else {
            return executeWithCpuBasedTimeout(testcase, executor, timeout);
        }
    }

    private T executeWithTimeout(final Callable<T> testcase, ExecutorService executor,
                                 long timeout) throws InterruptedException, ExecutionException,
            TimeoutException {
        task = new FutureTask<>(testcase);
        executor.execute(task);
        T result = task.get(timeout, TimeUnit.MILLISECONDS);
        return result;
    }

    private T executeWithCpuBasedTimeout(final Callable<T> testcase,
                                         ExecutorService executor, long timeout) throws InterruptedException,
            ExecutionException, TimeoutException {
        long[] other_thread_ids = bean.getAllThreadIds();

        task = new FutureTask<>(testcase);
        executor.execute(task);
        T result = null;

        long waiting_time = timeout;

        while (waiting_time > 0) {
            try {
                result = task.get(waiting_time, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                //executor is still running. need to check CPU usage.
                //NOTE: this is rather tricky, because ONLY the threads that are still alive are returned.
                //if a test case generates a lot of threads and those die, then their CPU usage would not be counted
                long[] all_thread_ids = bean.getAllThreadIds();
                long cpu_usage = 0;

                outer:
                for (long id : all_thread_ids) {
                    for (final long other_thread_id : other_thread_ids)
                        if (id == other_thread_id)
                            continue outer;

                    //id is "new"

                    long ns = bean.getThreadCpuTime(id);
                    long ms = (ns / 1000000);

                    cpu_usage += ms;
                }

                final double alpha = 0.9; //used to avoid possible problems with time measurement
                if (cpu_usage < alpha * timeout) {
                    //CPU has not been used enough
                    waiting_time = timeout - cpu_usage;
                } else {
                    //effectively it is a timeout
                    throw e;
                }
            } catch (ThreadDeath t) {
                throw new InterruptedException();
            }
        }
        return result;
    }
}
