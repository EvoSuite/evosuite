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
package org.evosuite.runtime.mock.java.lang;

import org.apache.commons.lang3.SystemUtils;
import org.evosuite.runtime.annotation.EvoSuiteExclude;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.thread.ThreadCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Threads are very complex to handle.
 * For the moment, we mock only certain aspects: TODO
 *
 * Created by arcuri on 9/23/14.
 */
public class MockThread extends Thread implements OverrideMock {

    static {
        final Integer javaVersion = Integer.valueOf(SystemUtils.JAVA_VERSION.split("\\.")[0]);
        if(javaVersion < 11){
            try {
                Method destroy = MockThread.class.getMethod("destroy");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    // ----- mock internals -------

    private static final Logger logger = LoggerFactory.getLogger(MockThread.class);

    private static final Map<Integer, Long> threadMap = new ConcurrentHashMap<>();

    public static void reset() {
        threadMap.clear();
    }

    private boolean isSutRelated() {
        String sut = RuntimeSettings.className;
        String threadName = this.getClass().getName();
        String targetName = target==null ? null : target.getClass().getName();

        /*
            Note: this check would not recognize code like:

            Thread t = new Thread(); t.start();

            however, as it does nothing, no point in starting it anyway
         */

        return  match(sut,threadName) || match(sut,targetName);
    }

    private boolean match(String sut, String other) {
        if(other==null || other.length() < sut.length()) {
            return false;
        }
        if(other.length() == sut.length()) {
            //is the thread the SUT itself?
            return other.equals(sut);
        } else {
            //anonymous or internal class of the SUT
            return other.startsWith(sut+"$");
        }
    }

    /**
     * a copy of the private field in superclass
     */
    private Runnable target;

    // ------ public static fields --------

    public final static int MIN_PRIORITY = 1;
    public final static int NORM_PRIORITY = 5;
    public final static int MAX_PRIORITY = 10;


    // ------ static  methods  --------

    public static Thread currentThread() {
        return Thread.currentThread();
    }

    @EvoSuiteExclude
    public static void yield() {
        Thread.yield();
    }

    @EvoSuiteExclude
    public static void sleep(long millis) throws InterruptedException {
        //no point in doing any sleep
        //MockThread.yield(); //just in case to change thread //FIXME quite a few side effects
        Thread.sleep(Math.min(millis,50)); //TODO maybe should be a parameter
    }

    @EvoSuiteExclude
    public static void sleep(long millis, int nanos)
            throws InterruptedException {
        MockThread.sleep(millis);
    }

    public static boolean interrupted() {
        return Thread.interrupted();
    }

    public static int activeCount() {
        return Thread.activeCount();
    }

    public static int enumerate(Thread[] tarray) {
        return Thread.enumerate(tarray);
    }

    public static void dumpStack() {
        if(!MockFramework.isEnabled()) {
            Thread.dumpStack();
        } else {
            new MockException("Stack trace").printStackTrace();
        }
    }

    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        if(!MockFramework.isEnabled()) {
            return Thread.getAllStackTraces();
        }
        //get actual running threads, and then replace stack traces

        //this will ask for permissions, but we grant it anyway
        Set<Thread> threads =  Thread.getAllStackTraces().keySet();
        Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.size());
        for(Thread t : threads) {
            m.put(t,MockThrowable.getDefaultStackTrace());
        }

        return m;
    }

    public static boolean holdsLock(Object obj) {
        return Thread.holdsLock(obj);
    }

    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        Thread.setDefaultUncaughtExceptionHandler(eh);
    }

    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        return Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // -------- constructors ---------

    public MockThread() {
        super();
        mockSetup(null);
    }

    public MockThread(Runnable target) {
        super(target);
        this.target = target;
        mockSetup(null);
    }

    public MockThread(ThreadGroup group, Runnable target) {
        super(group, target);
        this.target = target;
        mockSetup(null);
    }

    public MockThread(String name) {
        super(name);
        mockSetup(name);
    }

    public MockThread(ThreadGroup group, String name) {
        super(group, name);
        mockSetup(name);
    }

    public MockThread(Runnable target, String name) {
        super(target, name);
        this.target = target;
        mockSetup(name);
    }

    public MockThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        this.target = target;
        mockSetup(name);
    }

    public MockThread(ThreadGroup group, Runnable target, String name,
                      long stackSize) {
        super(group, target, name, stackSize);
        this.target = target;
        mockSetup(name);
    }

    private void mockSetup(String name) {
        if(!MockFramework.isEnabled()) {
            return;
        }

        if(name == null) {
            /*
                If SUT did not specify any name, we need
                to change the one automatically given by the JVM,
                as it could be non-deterministic
             */
            setName("MockThread-"+getId());
        }
    }

    // ---------------

    @Override
    @EvoSuiteExclude
    public synchronized void start() {

        if(!MockFramework.isEnabled()) {
            super.start();
            return;
        }

        if(!isSutRelated()) {
            //no point in starting those 3rd party threads
            return;
        }

        ThreadCounter.getInstance().checkIfCanStartNewThread();

        /*
            TODO: we could rather buffer them like we did for hooks, and execute them at the end.
            We could even have methods in the test-cluster to execute some of those threads till they yield
            within the test case
         */
        super.start();
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    @EvoSuiteExclude
    public void interrupt() {
        super.interrupt();
    }


    @Override
    public boolean isInterrupted() {
        return super.isInterrupted();
    }

    // @Override
    // No @Override to guarantee Java 11 compatibility
    @EvoSuiteExclude
    public void destroy() {
        // inlined super.destroy()
        throw new NoSuchMethodError();
    }



    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public ClassLoader getContextClassLoader() {
        return super.getContextClassLoader();
    }

    @Override
    public void setContextClassLoader(ClassLoader cl) {
        super.setContextClassLoader(cl);
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        if(!MockFramework.isEnabled()) {
            return super.getStackTrace();
        }
        return MockThrowable.getDefaultStackTrace();
    }


    @Override
    public long getId() {
        if(!MockFramework.isEnabled()) {
            return super.getId();
        }

        synchronized (threadMap) {
            int identity = java.lang.System.identityHashCode(this);
            if (!threadMap.containsKey(identity)) {
                threadMap.put(identity, (long) threadMap.size());
            }
            return threadMap.get(identity);
        }
    }

    @Override
    public State getState() {
        return super.getState();
    }


    @Override
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return super.getUncaughtExceptionHandler();
    }

    @Override
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        super.setUncaughtExceptionHandler(eh);
    }

    // ----- StaticReplacementMethods  ---------


    // Handled in the constructors
    // public final void setName(String name) {   }
    // public final String getName() {}


    //TODO
    // they are final
    /*public final void stop() {
        stop(new ThreadDeath());
    }
    public final synchronized void stop(Throwable obj) {
    ...
    }

    //public final native boolean isAlive();

    @Deprecated
    public final void suspend() {
        checkAccess();
        suspend0();
    }

    @Deprecated
    public final void resume() {
        checkAccess();
        resume0();
    }


    public final void setPriority(int newPriority) {
    }

    public final int getPriority() {
        return priority;
    }



    public final ThreadGroup getThreadGroup() {
        return group;
    }

    @Deprecated
    public native int countStackFrames();

    public final synchronized void join(long millis)
            throws InterruptedException {
    }

    public final synchronized void join(long millis, int nanos)
            throws InterruptedException {

    }

    public final void join() throws InterruptedException {
        join(0);
    }


    public final void setDaemon(boolean on) {
    }

    public final boolean isDaemon() {
        return daemon;
    }

    public final void checkAccess() {
    }
*/


}
