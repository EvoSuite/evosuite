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

import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.core.IsEqual;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaExecCmdUtilUnixTest {

    private static final String SEPARATOR = "/";
    private static final String JAVA_HOME_SYSTEM = System.getenv("JAVA_HOME") != null ? System.getenv("JAVA_HOME") : System.getProperty("java.home");
    private static final String JAVA_HOME_MOCK_PATH =
            SEPARATOR + "usr" + SEPARATOR + "home" + SEPARATOR + "jdk_8";
    private static final String MOCK_OS = "Mac OS X";

    @Test
    public void unixNeverNull() {
        assertNotNull(JavaExecCmdUtil.getJavaBinExecutablePath());
        assertNotNull(JavaExecCmdUtil.getJavaBinExecutablePath(true));
        assertNotNull(JavaExecCmdUtil.getJavaBinExecutablePath(false));
    }

    @Test
    public void unixMockEnvIsOk() {
        String resolved = JavaExecCmdUtil.getJavaBinExecutablePath(
                JAVA_HOME_MOCK_PATH,
                MOCK_OS,
                JAVA_HOME_MOCK_PATH,
                true
        );

        assertThat(resolved, IsEqual.equalTo(
                JAVA_HOME_MOCK_PATH + SEPARATOR + "bin" + SEPARATOR + "java"));
        assertFalse(StringUtils.isEmpty(JAVA_HOME_SYSTEM));
    }

    @Test
    public void unixOldBehaviorJava() {
        // return "java" value
        assertThat(JavaExecCmdUtil.getJavaBinExecutablePath(
                        null,
                        MOCK_OS,
                        JAVA_HOME_MOCK_PATH,
                        false),
                IsEqual.equalTo("java"));
    }

    @Test
    public void unixOldBehaviorJavaCmd() {
        // return JAVA_CMD value
        assertThat(JavaExecCmdUtil.getJavaBinExecutablePath(
                        null,
                        MOCK_OS,
                        JAVA_HOME_MOCK_PATH,
                        true),
                IsEqual.equalTo(
                        JAVA_HOME_MOCK_PATH + SEPARATOR + "bin" + SEPARATOR + "java"));
    }

    @Test
    public void unixNewBehavior() {
        // run test only on unix build
        JavaExecCmdUtil.getOsName().filter(osName -> !osName.startsWith("Windows"))
                .ifPresent(os ->
                        {
                            assertThat(JavaExecCmdUtil.getJavaBinExecutablePath(
                                               JAVA_HOME_SYSTEM,
                                               MOCK_OS,
                                               JAVA_HOME_MOCK_PATH,
                                               false),
                                       IsEqual.equalTo(Paths.get(JAVA_HOME_SYSTEM, "bin", "java").toString()));
                        }
                );
    }
}
