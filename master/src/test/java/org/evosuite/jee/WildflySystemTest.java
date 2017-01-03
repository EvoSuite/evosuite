package org.evosuite.jee;

import com.examples.with.different.packagename.jee.injection.wildfly.TransactionServlet;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.utils.Randomness;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class WildflySystemTest extends SystemTestBase{


    @Test(timeout = 60_000) //@Ignore
    public void testTransactionServlet(){
        Properties.JEE = true;
        Properties.SEARCH_BUDGET = 100_000;
        do100percentLineTestOnStandardCriteria(TransactionServlet.class);
    }
}
