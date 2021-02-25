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
package org.evosuite.utils;

import java.io.File;
import java.util.StringJoiner;

/**
 * Utils for files/directories paths creation.
 *
 * @author Ignacio Lebrero
 */
public class SystemPathUtil {

    /**
     * Exceptions messages
     */
    public static final String ELEMENTS_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE = "Elements must not be null.";
    public static final String DELIMITER_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE = "Delimiter must not be null.";

    /**
     * Different file extensions
     */
    public enum FileExtension {
        TXT("txt"),
        CSV("csv");

        String name;

        FileExtension(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    /**
     * Simple Delimiter for file creation
     */
    public static final String FILE_NAME_DELIMITER = "_";

    /**
     * Simple Delimiter for file extension
     */
    public static final String FILE_EXTENSION_DELIMITER = ".";


    /**
     * Creates a file name from a series of given Strings.
     *
     * @param params
     * @return
     */
    public static String buildFileName(FileExtension extension, String... params) {
        return joinWithDelimiter(FILE_EXTENSION_DELIMITER, joinWithDelimiter(FILE_NAME_DELIMITER, params), extension.getName());
    }

    /**
     * Creates a path from a series of given Strings.
     *
     * @param pathElements
     * @return
     */
    public static String buildPath(String... pathElements) {
        return joinWithDelimiter(File.separator, pathElements);
    }

    /**
     * Joins a series of String using a delimiter.
     *
     * @param delimiter
     * @param elements
     * @return
     */
    public static String joinWithDelimiter(String delimiter, String... elements) {
        if (delimiter == null) throw new IllegalArgumentException(DELIMITER_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE);
        if (elements == null) throw new IllegalArgumentException(ELEMENTS_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE);

        StringJoiner joiner = new StringJoiner(delimiter);

        for (String element : elements) {
            joiner.add(element);
        }

        return joiner.toString();
    }
}
