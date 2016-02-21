package org.evosuite.papers;

import com.examples.with.different.packagename.papers.vfs.EnvExample;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.Test;

public class Env_SystemTest extends SystemTestBase{


    @Test
    public void testEnv(){
        Properties.SEARCH_BUDGET = 1_000_000;
        do100percentLineTest(EnvExample.class);
    }
}
