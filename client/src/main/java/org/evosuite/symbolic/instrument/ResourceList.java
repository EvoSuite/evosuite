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

package org.evosuite.symbolic.instrument;

/**
 * @author fraser
 */

import org.evosuite.dse.MainConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;


/**
 * list resources available from the classpath @ *
 *
 * @author Gordon Fraser
 */
public class ResourceList {


    public static Collection<String> findResourceInClassPath(String fileName) {
        final ArrayList<String> retval = new ArrayList<>();
        final String[] classPathElements = MainConfig.get().CLASS_PATH.split(":");

        for (final String element : classPathElements) {
            String fullFileName = element + File.separator + fileName;
            File file = new File(fullFileName);
            if (file.exists()) {
                retval.add(fullFileName);
            }
        }
        return retval;
    }


}
