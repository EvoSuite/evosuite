package org.evosuite.papers;

import com.examples.with.different.packagename.papers.vnet.Example_UDP_TCP;
import com.examples.with.different.packagename.papers.vnet.Example_URL;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.Before;
import org.junit.Test;

public class VNET_SystemTest extends SystemTestBase {

    @Test
    public void testTCP(){
        Properties.SEARCH_BUDGET = 1_000_000;
        do100percentLineTest(Example_UDP_TCP.class);
    }

    @Test
    public void testURL(){
        do100percentLineTest(Example_URL.class);
    }
}
