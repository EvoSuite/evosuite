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
package org.evosuite.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public enum Java9InvisiblePackage {

    instance;
    private static boolean classesLoaded = false;
    private static final List<String> excludedClasses = new ArrayList<>();
    private static final String FILENAME = "/java9_invisible_packages.txt";

    private void loadExcludedClassNames() {
        if (classesLoaded)
            return;
        classesLoaded = true;
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
                FILENAME)))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                excludedClasses.add(strLine);
            }
        } catch (IOException e) {
            System.err.println("Wrong filename/path/file is missing");
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * getClassesToBeIgnored
     * </p>
     *
     * @return the names of packages invisible in Java9
     */
    public List<String> getClassesToBeIgnored() {
        loadExcludedClassNames();
        return excludedClasses;
    }
}
