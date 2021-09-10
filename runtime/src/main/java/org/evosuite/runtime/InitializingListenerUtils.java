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
package org.evosuite.runtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Utils methods needed in their own class with no external dependency.
 * Required due to constraints on dependencies in the various modules using them
 */
public class InitializingListenerUtils {

    public static List<String> scanClassesToInit(File dir) throws IllegalArgumentException {
        Objects.requireNonNull(dir);
        if (!dir.exists()) {
            throw new IllegalArgumentException("Invalid compiled test folder: " + dir.getAbsolutePath());
        }

        List<String> list = new ArrayList<>();


        try {
            Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith("scaffolding.class")) {
                        String resource = file.toFile().getAbsolutePath()
                                .substring(dir.getAbsolutePath().length() + 1);
                        String className = getClassNameFromResourcePath(resource);
                        list.add(className);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan compiled test folder " + dir.getAbsolutePath() + " : " + e.getMessage(), e);
        }

        return list;
    }

    public static String getClassNameFromResourcePath(String resource) {
        if (resource == null || resource.isEmpty()) {
            return resource;
        }

        // check file ending
        final String CLASS = ".class";
        if (resource.endsWith(CLASS)) {
            resource = resource.substring(0, resource.length() - CLASS.length());
        }

        //in Jar it is always '/'
        resource = resource.replace('/', '.');

        if (File.separatorChar != '/') {
            //this would happen on a Windows machine for example
            resource = resource.replace(File.separatorChar, '.');
        }

        return resource;
    }
}
