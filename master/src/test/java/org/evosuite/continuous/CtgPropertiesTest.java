package org.evosuite.continuous;

import org.evosuite.Properties;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Andrea Arcuri on 21/11/15.
 */
public class CtgPropertiesTest {

    @Test
    public void testNeverEverDefinePropertiesBasedOnOthersOtherwiseChangingInFormerWillNotAffectTheLatter(){

        Properties.getInstance().resetToDefaults();
        assertTrue(! Properties.CTG_BESTS_DIR_NAME.startsWith(Properties.CTG_DIR));
        assertTrue(! Properties.CTG_PROJECT_INFO.startsWith(Properties.CTG_DIR));
    }
}
