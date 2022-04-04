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

package org.evosuite.junit;

import org.evosuite.Properties;
import org.evosuite.utils.FileIOUtils;

import java.io.File;

/**
 * <p>
 * CoverageReportGenerator class
 * </p>
 *
 * @author José Campos
 */
public class CoverageReportGenerator {

    public static void writeCoverage(boolean[][] coverage, Properties.Criterion criterion) {

        StringBuilder suite = new StringBuilder();
        for (final boolean[] c : coverage) {
            StringBuilder test = new StringBuilder();

            for (int j = 0; j < c.length - 1; j++) {
                if (c[j])
                    test.append("1 ");
                else
                    test.append("0 ");
            }

            if (!test.toString().contains("1")) // if a test case does not contains a "1", means it does not coverage anything
                continue;

            if (c[c.length - 1])
                test.append("+\n");
            else
                test.append("-\n");

            suite.append(test);
        }

        FileIOUtils.writeFile(suite.toString(), new File(getReportDir().getAbsolutePath() +
                File.separator + "data" + File.separator +
                Properties.TARGET_CLASS + File.separator +
                criterion.toString() + File.separator + Properties.COVERAGE_MATRIX_FILENAME));
    }

    /**
     * Return the folder of where reports should be generated.
     * If the folder does not exist, try to create it
     *
     * @return
     * @throws RuntimeException if folder does not exist, and we cannot create it
     */
    private static File getReportDir() throws RuntimeException {
        File dir = new File(Properties.REPORT_DIR);

        if (!dir.exists()) {
            if (!dir.mkdirs())
                throw new RuntimeException("Cannot create report dir: " + Properties.REPORT_DIR);
        }

        return dir;
    }
}
