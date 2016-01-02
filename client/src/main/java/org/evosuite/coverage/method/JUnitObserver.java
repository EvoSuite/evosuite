package org.evosuite.coverage.method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by gordon on 01/01/2016.
 */
public class JUnitObserver {

    private final static Logger logger = LoggerFactory.getLogger(JUnitObserver.class);

    private static JUnitObserver instance = null;

    private JUnitObserver() {}

    private boolean enabled = false;

    public static JUnitObserver getInstance() {
        if(instance == null)
            instance = new JUnitObserver();

        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    public void reset() {

    }

    public static void methodCalled(Object callee, int opcode, String className, String methodName, Object[] arguments) {
        if(!getInstance().isEnabled())
            return;

        logger.info("Calling method "+className+"."+methodName+" with callee "+callee+" and arguments "+ Arrays.asList(arguments));
    }

    public static void methodReturned(Object retVal, String className, String methodName) {
        if(!getInstance().isEnabled())
            return;

        logger.info("Method "+className+"."+methodName+" returned: "+retVal);
    }
}
