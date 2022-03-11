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
 * log = LogFactory.getLog(CLASS.class);" after the class
 * declaration.
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
     * @param trace the string to display.
     * @throws Error always thrown.
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
     * @param trace the string to display.
     * @throws Error always thrown.
     */
    public static void bug(String trace) throws Error {
        log.error(trace);
        throw new Error(trace);
    }

    /**
     * Used to debug program action. Actually shorthand for <br>
     * <code> Tracer.trace(trace, Tracer.DEBUG) </code>
     *
     * @param trace the string to display.
     */
    public static void debug(String trace) {
        log.debug(trace);
    }

}
