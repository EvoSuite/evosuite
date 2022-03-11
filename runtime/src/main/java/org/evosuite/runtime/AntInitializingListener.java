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
package org.evosuite.runtime;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

import java.io.OutputStream;

public class AntInitializingListener implements JUnitResultFormatter {

    @Override
    public void startTestSuite(JUnitTest suite) throws BuildException {

        InitializingListener listener = new InitializingListener();
        try {
            listener.testRunStarted(null);
        } catch (Exception e) {
            throw new BuildException("Failed to run EvoSuite initializing listener: " + e.getMessage(), e);
        }

    }

    @Override
    public void startTest(Test test) {

    }

    //-----------------------------------------------------------------------------------------------

    @Override
    public void endTestSuite(JUnitTest suite) throws BuildException {

    }

    @Override
    public void setOutput(OutputStream out) {

    }

    @Override
    public void setSystemOutput(String out) {

    }

    @Override
    public void setSystemError(String err) {

    }

    @Override
    public void addError(Test test, Throwable e) {

    }

    @Override
    public void addFailure(Test test, AssertionFailedError e) {

    }

    @Override
    public void endTest(Test test) {

    }

}
