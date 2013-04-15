package org.evosuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@SuiteClasses({ TestSUTWithSimpleSingleMethod_v2.class, TestImplicitExplicitExceptions.class })
public class SimpleFollowedByExceptionTest {

}
