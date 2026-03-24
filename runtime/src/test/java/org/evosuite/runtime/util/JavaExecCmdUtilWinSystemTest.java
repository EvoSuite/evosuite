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
import org.hamcrest.core.StringStartsWith;
import org.junit.*;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaExecCmdUtilWinSystemTest {

    private static final String SEPARATOR = "\\";
    private static final String JAVA_HOME_SYSTEM = System.getenv("JAVA_HOME");
    private static final String JAVA_HOME_MOCK_PATH =
            "c:" + SEPARATOR + "sample" + SEPARATOR + "windows path" + SEPARATOR + "jdk_8";
    private static final String WIN_MOCK_OS = "Windows 10";
    private static final String ORIG_OS = System.getProperty("os.name");

    @Test
    public void winNeverNull() {
        assertNotNull(JavaExecCmdUtil.getJavaBinExecutablePath());
        assertNotNull(JavaExecCmdUtil.getJavaBinExecutablePath(true));
        assertNotNull(JavaExecCmdUtil.getJavaBinExecutablePath(false));
    }

    @Test
    public void winMockEnvIsOk() {
        // run test only on windows build
        Assume.assumeThat(ORIG_OS.toLowerCase(), StringStartsWith.startsWith("win"));

        String resolved = JavaExecCmdUtil.getJavaBinExecutablePath(
                JAVA_HOME_MOCK_PATH,
                WIN_MOCK_OS,
                JAVA_HOME_MOCK_PATH,
                true
        );

        assertThat(resolved, IsEqual.equalTo(
                JAVA_HOME_MOCK_PATH + SEPARATOR + "bin" + SEPARATOR + "java.exe"));
        assertFalse(StringUtils.isEmpty(JAVA_HOME_SYSTEM));
    }

    @Test
    public void winOldBehaviorJava() {
        // return "java" value
        assertThat(JavaExecCmdUtil.getJavaBinExecutablePath(
                        null,
                        WIN_MOCK_OS,
                        JAVA_HOME_MOCK_PATH,
                        false),
                IsEqual.equalTo("java"));
    }

    @Test
    public void winOldBehaviorJavaCmd() {
        // return JAVA_CMD value
        assertThat(JavaExecCmdUtil.getJavaBinExecutablePath(
                        null,
                        WIN_MOCK_OS,
                        JAVA_HOME_MOCK_PATH,
                        true),
                IsEqual.equalTo(
                        JAVA_HOME_MOCK_PATH + SEPARATOR + "bin" + SEPARATOR + "java"));
    }

    @Test
    public void winNewBehavior() {
        // run test only on windows build
        Assume.assumeThat(ORIG_OS.toLowerCase(), StringStartsWith.startsWith("win"));

        JavaExecCmdUtil.getOsName().filter(osName -> osName.startsWith("Windows")).ifPresent(os ->
                {
                    assertThat(JavaExecCmdUtil.getJavaBinExecutablePath(
                                    JAVA_HOME_SYSTEM,
                                    WIN_MOCK_OS,
                                    JAVA_HOME_MOCK_PATH,
                                    false),
                            IsEqual.equalTo(
                                    JAVA_HOME_SYSTEM + SEPARATOR + "bin" + SEPARATOR + "java.exe"));
                }
        );
    }
}
