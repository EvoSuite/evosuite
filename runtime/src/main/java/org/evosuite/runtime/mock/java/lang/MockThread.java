package org.evosuite.runtime.mock.java.lang;

import org.evosuite.runtime.mock.OverrideMock;
import sun.nio.ch.Interruptible;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by arcuri on 9/23/14.
 */
public class MockThread extends Thread implements OverrideMock{

    public final static int MIN_PRIORITY = 1;
    public final static int NORM_PRIORITY = 5;
    public final static int MAX_PRIORITY = 10;

    public static Thread currentThread(){
        return null; //TODO
    }

    public static  void yield(){
        //TODO
    }


    public static void sleep(long millis) throws InterruptedException{
        //TODO
    }

    public static void sleep(long millis, int nanos)
            throws InterruptedException {
        //TODO
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // -------- constructors ---------

    public MockThread() {
          super();
    }

    public MockThread(Runnable target) {
        super(target);
    }

    public MockThread(ThreadGroup group, Runnable target) {
        super(group,target);
    }

    public MockThread(String name) {
        super(name);
    }

    public MockThread(ThreadGroup group, String name) {
        super(group,name);
    }

    public MockThread(Runnable target, String name) {
        super(target,name);
    }

    public MockThread(ThreadGroup group, Runnable target, String name) {
        super(group,target,name);
    }

    public MockThread(ThreadGroup group, Runnable target, String name,
                  long stackSize) {
        super(group,target,name,stackSize);
    }

    // ---------------

    @Override
    public synchronized void start() {
        //TODO
    }

    @Override
    public void run() {
        super.run();
    }

    // they are final
    /*public final void stop() {
        stop(new ThreadDeath());
    }
    public final synchronized void stop(Throwable obj) {
    ...
    }
    */

    @Override
    public void interrupt() {
        super.interrupt();
    }

    //TODO
    public static boolean interrupted() {
        return false;
        //return currentThread().isInterrupted(true);
    }

    @Override
    public boolean isInterrupted() {
        return super.isInterrupted();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    //TODO
    //public final native boolean isAlive();

    /*
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

    public final void setName(String name) {   }

    public final String getName() {
        return String.valueOf(name);
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


    public static int activeCount() {
        return currentThread().getThreadGroup().activeCount();
    }

    public static int enumerate(Thread tarray[]) {
        return currentThread().getThreadGroup().enumerate(tarray);
    }

    public static void dumpStack() {
        new Exception("Stack trace").printStackTrace();
    }


    @Override
    public String toString() {
        //TODO
        ThreadGroup group = getThreadGroup();
        if (group != null) {
            return "Thread[" + getName() + "," + getPriority() + "," +
                    group.getName() + "]";
        } else {
            return "Thread[" + getName() + "," + getPriority() + "," +
                    "" + "]";
        }
    }

    @Override
    public ClassLoader getContextClassLoader() {
        return super.getContextClassLoader();
    }

    @Override
    public void setContextClassLoader(ClassLoader cl) {
        super.setContextClassLoader(cl);
    }


    public static native boolean holdsLock(Object obj);

    public StackTraceElement[] getStackTrace() {
        return null; //TODO
    }

    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        // check for getStackTrace permission
        /*
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(
                    SecurityConstants.GET_STACK_TRACE_PERMISSION);
            security.checkPermission(
                    SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        }

        // Get a snapshot of the list of all threads
        Thread[] threads = getThreads();
        StackTraceElement[][] traces = dumpThreads(threads);
        Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.length);
        for (int i = 0; i < threads.length; i++) {
            StackTraceElement[] stackTrace = traces[i];
            if (stackTrace != null) {
                m.put(threads[i], stackTrace);
            }
            // else terminated so we don't put it in the map
        }
        return m;
        */
        return null; //TODO
    }


    @Override
    public long getId() {
        return 0; //TODO
        //return tid;
    }

    @Override
    public State getState() {
        return super.getState();
    }


    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        Thread.setDefaultUncaughtExceptionHandler(eh);
    }

    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler(){
        return Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return super.getUncaughtExceptionHandler();
    }

    @Override
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        super.setUncaughtExceptionHandler(eh);
    }


}
