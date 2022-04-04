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

import java.io.File;
import java.util.Optional;

/**
 * Created by wildfly.ua@gmail.com for EvoSuite project (https://github.com/EvoSuite/evosuite)
 */
public class JavaExecCmdUtil {

    /**
     * Utility class, no public constructors
     */
    private JavaExecCmdUtil() {
    }

    /**
     * There is java environment configuration gap - ${JAVA_HOME}/bin/java != `which java` so just
     * start child process as "java" executable command drives to unexpected results. On generic Lunix
     * environment 'which java' may show not Oracle hotspot 8th jdk binary and evoSuite
     * master<->client communication will have no results.
     *
     * @param isFullOriginalJavaExecRequired - original behavior switch: "java" or JAVA_CMD from
     * (@link org.evosuite.EvoSuite#JAVA_CMD)
     * @return current runtime java executable path based on $JAVA_HOME environment variable
     * @apiNote under maven java.home property is ${JAVA_HOME}/jre/bin/java
     */
    public static String getJavaBinExecutablePath(final boolean isFullOriginalJavaExecRequired) {
        final String separator = System.getProperty("file.separator");
        final String JAVA_CMD =
                System.getProperty("java.home") + separator + "bin" + separator + "java";

        return getJavaHomeEnv()
                .map(javaHomeEnvVar ->
                        new File(
                                javaHomeEnvVar + File.separatorChar + "bin" + File.separatorChar + "java" +
                                        getOsName()
                                                .filter(osName -> osName.toLowerCase().contains("windows"))
                                                .map(osName -> ".exe")
                                                .orElse("")
                        )
                )
                .filter(File::exists)
                .map(File::getPath)
                .orElse(isFullOriginalJavaExecRequired ? JAVA_CMD : "java");
    }

    /**
     * @see #getJavaBinExecutablePath(boolean)
     */
    public static String getJavaBinExecutablePath() {
        return getJavaBinExecutablePath(false);
    }

    private static Optional<String> getJavaHomeEnv() {
        return Optional.ofNullable(System.getenv("JAVA_HOME"));
    }

    static Optional<String> getOsName() {
        return Optional.ofNullable(System.getProperty("os.name"));
    }
}
