package org.evosuite.coverage.method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by gordon on 01/01/2016.
 */
public class JUnitObserver {

    private final static Logger logger = LoggerFactory.getLogger(JUnitObserver.class);

    public static void methodCalled(Object callee, int opcode, String className, String methodName, Object[] arguments) {
        logger.info("Calling method "+className+"."+methodName+" with callee "+callee+" and arguments "+ Arrays.asList(arguments));
    }

    public static void methodReturned(Object retVal, String className, String methodName) {
        logger.info("Method "+className+"."+methodName+" returned: "+retVal);
    }
}
