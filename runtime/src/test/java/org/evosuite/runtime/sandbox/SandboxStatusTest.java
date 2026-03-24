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

import org.junit.Assert;
import org.junit.Test;

public class SandboxStatusTest {

    @Test
    public void testStatusReflectsModernJdkDegradedMode() {
        Assert.assertEquals(Sandbox.SandboxStatus.OFF, Sandbox.getSandboxStatus());
        Assert.assertEquals(Sandbox.EnforcementCapability.NONE, Sandbox.getEnforcementCapability());

        Sandbox.initializeSecurityManagerForSUT();
        try {
            if (MSecurityManager.isSecurityManagerSupported()) {
                Assert.assertEquals(Sandbox.SandboxStatus.LEGACY_ENFORCEMENT, Sandbox.getSandboxStatus());
                Assert.assertTrue(Sandbox.isEnforcingPermissions());
                Assert.assertEquals(Sandbox.EnforcementCapability.LEGACY_SECURITY_MANAGER,
                        Sandbox.getEnforcementCapability());
            } else {
                Assert.assertEquals(Sandbox.SandboxStatus.COORDINATION_ONLY, Sandbox.getSandboxStatus());
                Assert.assertFalse(Sandbox.isEnforcingPermissions());
                Assert.assertEquals(Sandbox.EnforcementCapability.NONE, Sandbox.getEnforcementCapability());
            }
        } finally {
            Sandbox.resetDefaultSecurityManager();
        }

        Assert.assertEquals(Sandbox.SandboxStatus.OFF, Sandbox.getSandboxStatus());
        Assert.assertEquals(Sandbox.EnforcementCapability.NONE, Sandbox.getEnforcementCapability());
    }
}
