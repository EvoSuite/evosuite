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
package org.evosuite.symbolic.vm.instructionlogger;

/**
 * Interface for instruction loggers
 *
 * @author Ignacio Lebrero
 */
public interface IInstructionLogger {

    /**
     * Log parameter as p.
     */
    void log(int p);

    /**
     * Log parameter as p.
     */
    void log(String p);

    /**
     * Log newline.
     */
    void logln();

    /**
     * Log parameter as p followed by newline.
     */
    void logln(int p);

    /**
     * Log parameters as ab.
     */
    void log(String a, String b);

    /**
     * Log parameters as abc.
     */
    void log(String a, String b, String c);

    /**
     * Log parameters as abcd.
     */
    void log(String a, String b, String c, String d);

    /**
     * Log parameters as abcde.
     */
    void log(String a, String b, String c, String d, String e);

    /**
     * Log stack trace of exception. If it is an exception thrown by the user
     * program, omit lower part of stack trace that shows Dsc invocation
     * machinery.
     */
    void logln(Throwable e);

    /**
     * Log parameter as p followed by newline.
     */
    void logln(Object p);

    /**
     * Log parameter as p followed by newline.
     */
    void logln(String p);

    /**
     * Log parameters as ab followed by newline.
     */
    void logln(String a, String b);

    /**
     * Log parameters as abc followed by newline.
     */
    void logln(String a, String b, String c);

    /**
     * Log parameters as abcd followed by newline.
     */
    void logln(String a, String b, String c, String d);

    /**
     * Log parameters as abcde followed by newline.
     */
    void logln(String a, String b, String c, String d, String e);

    void logfileIf(boolean doLog, Object o, String fileName);

    /**
     * Logs parameter, if doLog.
     */
    void logIf(boolean doLog, String s);

    /**
     * Logs newline, if doLog.
     */
    void loglnIf(boolean doLog);

    /**
     * Logs parameter followed by newline, if doLog.
     */
    void loglnIf(boolean doLog, String s);

    /**
     * Logs parameters as ab followed by newline, if doLog.
     */
    void loglnIf(boolean doLog, String a, String b);

    /**
     * Logs parameters as abc followed by newline, if doLog.
     */
    void loglnIf(boolean doLog, String a, String b, String c);

    /**
     * Logs parameters as abcd followed by newline, if doLog.
     */
    void loglnIf(boolean doLog, String a, String b, String c, String d);

    /**
     * Logs parameters as abcde followed by newline, if doLog.
     */
    void loglnIf(boolean doLog, String a, String b, String c, String d, String e);

    /**
     * Cleans up any internal state
     */
    void cleanUp();
}
