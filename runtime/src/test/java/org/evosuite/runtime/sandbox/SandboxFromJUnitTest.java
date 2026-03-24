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
package org.evosuite.runtime.sandbox;

import org.junit.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SandboxFromJUnitTest {

    private static ExecutorService executor;
    private static boolean securityManagerSupported;

    @BeforeClass
    public static void initEvoSuiteFramework() {
        securityManagerSupported = MSecurityManager.isSecurityManagerSupported();
        Assume.assumeTrue(securityManagerSupported);
        Assert.assertNull(System.getSecurityManager());

        Sandbox.initializeSecurityManagerForSUT();
        Assert.assertEquals(Sandbox.SandboxStatus.LEGACY_ENFORCEMENT, Sandbox.getSandboxStatus());
        Assert.assertTrue(Sandbox.isEnforcingPermissions());
        executor = Executors.newCachedThreadPool();

    }

    @AfterClass
    public static void clearEvoSuiteFramework() {
        if (!securityManagerSupported) {
            return;
        }
        Assert.assertNotNull(System.getSecurityManager());

        executor.shutdownNow();
        Sandbox.resetDefaultSecurityManager();

        Assert.assertNull(System.getSecurityManager());
    }

    @Before
    public void initTest() {
        Assume.assumeTrue(securityManagerSupported);
        Sandbox.goingToExecuteSUTCode();
        //TestGenerationContext.getInstance().goingToExecuteSUTCode();
    }

    @After
    public void doneWithTestCase() {
        if (!securityManagerSupported) {
            return;
        }
        Sandbox.doneWithExecutingSUTCode();
    }

    @Test
    public void testLegacySandboxStatus() {
        Assert.assertTrue(Sandbox.isSandboxInitialized());
        Assert.assertTrue(Sandbox.isEnforcingPermissions());
        Assert.assertEquals(Sandbox.SandboxStatus.LEGACY_ENFORCEMENT, Sandbox.getSandboxStatus());
        Assert.assertEquals(Sandbox.EnforcementCapability.LEGACY_SECURITY_MANAGER,
                Sandbox.getEnforcementCapability());
    }


    @Test
    public void testExit() throws Exception {

        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                //-------
                Foo foo = new Foo();
                try {
                    foo.tryToExit();
                    Assert.fail();
                } catch (SecurityException e) {
                    //expected
                }
                //-------
            }
        });
        future.get(5000, TimeUnit.MILLISECONDS);

    }

}


class Foo {

    public void tryToExit() {
        System.exit(0);
    }
}
