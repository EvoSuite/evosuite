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
package org.evosuite.symbolic.vm.instructionlogger.implementations;

import org.evosuite.symbolic.vm.instructionlogger.IInstructionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Central log.
 * <p>
 * Offers convenience methods for logging short lists of strings that
 * do not use the String "+" operator.
 * <p>
 * FIXME: very primitive
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
public abstract class AbstractInstructionLogger implements IInstructionLogger {

    private static final transient Logger logger = LoggerFactory.getLogger(AbstractInstructionLogger.class);
    static List<String> instructionsExecuted = new ArrayList<>();
    static StringBuilder buffer = new StringBuilder();

    public final static String NL = System.getProperty("line.separator");
    public final static String FS = System.getProperty("file.separator");

    /**
     * Log parameter as p.
     */
    public abstract void log(String p);

    /**
     * Log newline.
     */
    public abstract void logln();

    public abstract void cleanUp();

    /**
     * Log parameter as p.
     */
    public void log(int p) {
        log(String.valueOf(p));
    }

    /**
     * Log parameters as ab.
     */
    public void log(String a, String b) {
        log(a);
        log(b);
    }

    /**
     * Log parameters as abc.
     */
    public void log(String a, String b, String c) {
        log(a);
        log(b);
        log(c);
    }

    /**
     * Log parameters as abcd.
     */
    public void log(String a, String b, String c, String d) {
        log(a);
        log(b);
        log(c);
        log(d);
    }

    /**
     * Log parameters as abcde.
     */
    public void log(String a, String b, String c, String d, String e) {
        log(a);
        log(b);
        log(c);
        log(d);
        log(e);
    }

    /**
     * Log parameter as p followed by newline.
     */
    public void logln(int p) {
        log(p);
        logln();
    }

    /**
     * Log stack trace of exception. If it is an exception thrown by the user
     * program, omit lower part of stack trace that shows Dsc invocation
     * machinery.
     */
    public void logln(Throwable e) {
        if (e == null)
            return;

        log("Aborted with: ");
        logln(e.toString());

        StackTraceElement[] trace = e.getStackTrace();
        if (trace == null)
            return;

        for (StackTraceElement ste : trace) {
            if (ste == null)
                continue;

            String className = ste.getClassName();
            if (className != null && className.startsWith("edu.uta.cse.dsc.vm.MethodExploration")) {
                logln("\t.. invoked by Dsc.");
                break;
            }

            log("\tat ");
            logln(ste.toString());
        }
    }

    /**
     * Log parameter as p followed by newline.
     */
    public void logln(Object p) {
        logln(p.toString());
    }

    /**
     * Log parameter as p followed by newline.
     */
    public void logln(String p) {
        logger.info(p);
        logln();
    }

    /**
     * Log parameters as ab followed by newline.
     */
    public void logln(String a, String b) {
        log(a, b);
        logln();
    }

    /**
     * Log parameters as abc followed by newline.
     */
    public void logln(String a, String b, String c) {
        log(a, b, c);
        logln();
    }

    /**
     * Log parameters as abcd followed by newline.
     */
    public void logln(String a, String b, String c, String d) {
        log(a, b, c, d);
        logln();
    }

    /**
     * Log parameters as abcde followed by newline.
     */
    public void logln(String a, String b, String c, String d, String e) {
        log(a, b, c, d, e);
        logln();
    }

    public void logfileIf(boolean doLog, Object o, String fileName) {
        if (!doLog)    // src-util should not depend on src-vm
            return;

        try (FileWriter fstream = new FileWriter(fileName)) {
            final BufferedWriter writer = new BufferedWriter(fstream);
            writer.write(o.toString());
            writer.close();
        } catch (Exception e) { //Catch exception if any
            System.err.println("File error: " + e.getMessage());
        }
    }


    /**
     * Logs parameter, if doLog.
     */
    public void logIf(boolean doLog, String s) {
        if (doLog)
            log(s);
    }


    /**
     * Logs newline, if doLog.
     */
    public void loglnIf(boolean doLog) {
        if (doLog)
            logln();
    }

    /**
     * Logs parameter followed by newline, if doLog.
     */
    public void loglnIf(boolean doLog, String s) {
        if (doLog)
            logln(s);
    }

    /**
     * Logs parameters as ab followed by newline, if doLog.
     */
    public void loglnIf(boolean doLog, String a, String b) {
        if (doLog)
            logln(a, b);
    }

    /**
     * Logs parameters as abc followed by newline, if doLog.
     */
    public void loglnIf(boolean doLog, String a, String b, String c) {
        if (doLog)
            logln(a, b, c);
    }

    /**
     * Logs parameters as abcd followed by newline, if doLog.
     */
    public void loglnIf(boolean doLog, String a, String b, String c, String d) {
        if (doLog)
            logln(a, b, c, d);
    }

    /**
     * Logs parameters as abcde followed by newline, if doLog.
     */
    public void loglnIf(boolean doLog, String a, String b, String c, String d, String e) {
        if (doLog)
            logln(a, b, c, d, e);
    }
}