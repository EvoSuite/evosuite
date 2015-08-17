package org.evosuite.jee;

import com.examples.with.different.packagename.jee.db.SimpleDBInteraction;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 17/08/15.
 */
public class DBSystemTest extends SystemTest {

    @Test
    public void testSimpleDBInteraction(){
        Properties.JEE = true;
        do100percentLineTest(SimpleDBInteraction.class);
    }
}
