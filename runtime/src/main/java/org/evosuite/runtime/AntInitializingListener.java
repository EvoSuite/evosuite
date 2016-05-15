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
            throw new BuildException("Failed to run EvoSuite initializing listener: "+e.getMessage(), e);
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
