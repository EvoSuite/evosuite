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
package org.evosuite;

/**
 * Package information should never be hardcoded, as packages can be refactored or shaded.
 * All access should be through reflection or .class variables
 * <p>
 * Created by Andrea Arcuri on 22/10/15.
 */
public class PackageInfo {

    private static final String SHADED = "shaded"; //WARN: do not modify it, as it is used in xml files as well

    public static String getEvoSuitePackage() {
        return PackageInfo.class.getPackage().getName();
    }

    public static String getEvoSuitePackageWithSlash() {
        return getEvoSuitePackage().replace('.', '/');
    }

    /**
     * The package were third-party libraries are shaded into
     *
     * @return
     */
    public static String getShadedPackageForThirdPartyLibraries() {
        return getEvoSuitePackage() + "." + SHADED;
    }

    public static String getNameWithSlash(Class<?> klass) {
        return klass.getName().replace('.', '/');
    }


    /**
     * The package name of the shaded EvoSutie. Used only for when testing EvoSuite
     *
     * @return
     */
    public static String getShadedEvoSuitePackage() {
        String shaded = SHADED + ".";
        String evo = getEvoSuitePackage();
        if (evo.startsWith(shaded)) {
            return evo;
        } else {
            return shaded + evo;
        }
    }


    public static boolean isCurrentlyShaded() {
        return getEvoSuitePackage().startsWith(SHADED);
    }
}
