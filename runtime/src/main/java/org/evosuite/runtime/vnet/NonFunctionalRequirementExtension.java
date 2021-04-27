package org.evosuite.runtime.vnet;

import org.evosuite.runtime.TooManyResourcesException;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension for JUnit 5 that avoids failing tests due to changes in non-functional requirements.
 *
 * @see org.evosuite.runtime.vnet.NonFunctionalRequirementRule
 */
public class NonFunctionalRequirementExtension implements TestExecutionExceptionHandler {
    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        if(throwable instanceof TooManyResourcesException){
            // Prevent TMRE to propagate to the JUnit runner
            return;
        }
        throw throwable;
    }
}
