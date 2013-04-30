/**
 * 
 */
package org.evosuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestSUTDivisionByZero.class, TestSUTPrintingThatShouldBeMuted.class })
public class DivisionByZeroFollowByOther {

}
