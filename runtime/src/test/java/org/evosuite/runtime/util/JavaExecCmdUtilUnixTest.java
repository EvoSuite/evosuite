/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.util;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaExecCmdUtilUnixTest {

    private static final String SEPARATOR = "/";
    private static final String JAVA_HOME_SYSTEM = System.getenv("JAVA_HOME") != null ? System.getenv("JAVA_HOME") : System.getProperty("java.home");
    private static final String JAVA_HOME_MOCK_PATH =
            SEPARATOR + "usr" + SEPARATOR + "home" + SEPARATOR + "jdk_8";
    private static final String MOCK_OS = "Mac OS X";

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void initTestEnvironment() {
        environmentVariables.set("JAVA_HOME", JAVA_HOME_MOCK_PATH);
    }

    @Test
    public void unixNeverNull() {
        assertNotNull(JavaExecCmdUtil.getJavaBinExecutablePath());
        assertNotNull(JavaExecCmdUtil.getJavaBinExecutablePath(true));
        assertNotNull(JavaExecCmdUtil.getJavaBinExecutablePath(false));
    }

    @Test
    public void unixMockEnvIsOk() {
        System.setProperty("os.name", MOCK_OS);
        System.setProperty("file.separator", SEPARATOR);
        System.setProperty("java.home", JAVA_HOME_MOCK_PATH);

        assertThat(System.getenv("JAVA_HOME"), IsEqual.equalTo(JAVA_HOME_MOCK_PATH));
        assertThat(System.getProperty("os.name"), IsEqual.equalTo(MOCK_OS));
        assertFalse(StringUtils.isEmpty(JAVA_HOME_SYSTEM));
    }

    @Test
    public void unixOldBehaviorJava() {
        // return "java" value
        assertThat(JavaExecCmdUtil.getJavaBinExecutablePath(), IsEqual.equalTo("java"));
    }

    @Test
    public void unixOldBehaviorJavaCmd() {
        System.setProperty("os.name", MOCK_OS);
        System.setProperty("file.separator", SEPARATOR);
        System.setProperty("java.home", JAVA_HOME_MOCK_PATH);

        // return JAVA_CMD value
        assertThat(JavaExecCmdUtil.getJavaBinExecutablePath(true),
                IsEqual.equalTo(
                        JAVA_HOME_MOCK_PATH + SEPARATOR + "bin" + SEPARATOR + "java"));
    }

    @Test
    public void unixNewBehavior() {
        // run test only on unix build
        JavaExecCmdUtil.getOsName().filter(osName -> !osName.startsWith("Windows"))
                .ifPresent(os ->
                        {
                            System.setProperty("os.name", MOCK_OS);
                            System.setProperty("file.separator", SEPARATOR);
                            System.setProperty("java.home", JAVA_HOME_MOCK_PATH);

                            // set correct java_home and get real path to java
                            environmentVariables.set("JAVA_HOME", JAVA_HOME_SYSTEM);
                            assertThat(JavaExecCmdUtil.getJavaBinExecutablePath(),
                                    IsEqual.equalTo(JAVA_HOME_SYSTEM + SEPARATOR + "bin" + SEPARATOR + "java"));
                        }
                );
    }
}
