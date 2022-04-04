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
package org.evosuite.runtime.classhandling;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.EvoClassLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by arcuri on 1/20/15.
 */
public class ClassStateSupportTest {

    @Test
    public void testInitializeClasses() {


        EvoClassLoader loader = new EvoClassLoader();
        String className = "com.examples.with.different.packagename.classhandling.TimeA";
        //no mocking
        RuntimeSettings.deactivateAllMocking();
        boolean problem = ClassStateSupport.initializeClasses(loader, className);
        Assert.assertFalse(problem);

        //with mocking
        RuntimeSettings.mockJVMNonDeterminism = true;
        className = "com.examples.with.different.packagename.classhandling.TimeB";
        problem = ClassStateSupport.initializeClasses(loader, className);
        Assert.assertFalse(problem);
    }
}
