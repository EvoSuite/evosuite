/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.papers;

import com.examples.with.different.packagename.papers.pafm.PAFM;
import com.examples.with.different.packagename.papers.pafm.PAFM_old;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.Before;
import org.junit.Test;

public class PAFM_SystemTest extends SystemTestBase {

    @Test
    public void testPAFM_old(){
        Properties.SEARCH_BUDGET = 100_000;
        do100percentLineTest(PAFM_old.class);
    }


    @Test
    public void testPAFM(){
        Properties.SEARCH_BUDGET = 100_000;
        do100percentLineTest(PAFM.class);
    }
}
