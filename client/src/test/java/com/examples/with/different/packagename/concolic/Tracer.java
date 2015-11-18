package com.examples.with.different.packagename.concolic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used for debugging and for putting trace printouts in code that
 * may be controlled at runtime.
 * <p>
 * The printouts are controlled by a global log level When a trace is given to
 * this class, it checks whether the trace is this log level or higher, and then
 * proceeds to print it. Otherwise the trace is simply ignored. Debug traces
 * cannot be turned off, though.
 * 
 * @author Mikael Nilsson
 * @version $Revision$
 * @deprecated Replaced by Apache Commons Logging. Supposed to be used like "Log
 *             log = LogFactory.getLog(CLASS.class);" after the class
 *             declaration.
 */
public class Tracer {

	// if we see "TRACER" in the logs we know that we have to replace the call -
	// we should not call this class as we can get the line numbers etc!
	private static Log log = LogFactory.getLog(Tracer.class);

    private Tracer() {
    }

    /**
	 * Used when a serious error has occured, from which recovery is not
	 * foreseen. Will throw an Error with the given message, as well as issue a
	 * trace with level Tracer.ERROR.
	 * 
	 * @param trace
	 *            the string to display.
	 * @exception Error
	 *                always thrown.
	 * @deprecated
	 */
    public static void error(String trace) throws Error {
        log.error(trace);
        throw new Error(trace);
    }

    /**
     * Used when a bug has been discovered. Will throw an Error with the given
     * message, as well as issue a trace with level Tracer.BUG.
     * 
     * @param trace
     *            the string to display.
     * @exception Error
     *                always thrown.
     */
    public static void bug(String trace) throws Error {
        log.error(trace);
        throw new Error(trace);
    }

    /**
     * Used to debug program action. Actually shorthand for <br>
     * <code> Tracer.trace(trace, Tracer.DEBUG) </code>
     * 
     * @param trace
     *            the string to display.
     */
    public static void debug(String trace) {
        log.debug(trace);
    }

}
