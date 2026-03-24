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
package org.evosuite.junit;

import com.examples.with.different.packagename.junit.Foo;
import com.examples.with.different.packagename.junit.PassingFooTest;
import org.evosuite.TestGenerationContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

public class DetermineSUTTest {

    @Before
    public void resetContext() {
        TestGenerationContext.getInstance().resetContext();
    }

    @Test
    public void identifiesSutFromJUnitClassLoadedFromTargetClasspath() throws Exception {
        DetermineSUT determineSUT = new DetermineSUT();

        String sut = determineSUT.getSUTName(PassingFooTest.class.getName(), getTestClassesDirectory());

        Assert.assertEquals(Foo.class.getName(), sut);
    }

    private String getTestClassesDirectory() throws URISyntaxException {
        return new File(PassingFooTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
    }
}
