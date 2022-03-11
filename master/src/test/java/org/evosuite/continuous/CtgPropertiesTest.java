/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.continuous;

import org.evosuite.Properties;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Andrea Arcuri on 21/11/15.
 */
public class CtgPropertiesTest {

    @Test
    public void testNeverEverDefinePropertiesBasedOnOthersOtherwiseChangingInFormerWillNotAffectTheLatter() {

        Properties.getInstance().resetToDefaults();
        assertFalse(Properties.CTG_BESTS_DIR_NAME.startsWith(Properties.CTG_DIR));
        assertFalse(Properties.CTG_PROJECT_INFO.startsWith(Properties.CTG_DIR));
    }
}
