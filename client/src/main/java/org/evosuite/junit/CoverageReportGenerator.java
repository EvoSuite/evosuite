/**
 * 
 */
package org.evosuite.junit;

import java.io.File;

import org.evosuite.Properties;
import org.evosuite.utils.Utils;

/**
 * <p>
 * CoverageReportGenerator class
 * </p>
 * 
 * @author José Campos
 */
public class CoverageReportGenerator {

	public static void writeCoverage(boolean[][] coverage) {

		StringBuilder suite = new StringBuilder();
		for (int i = 0; i < coverage.length; i++) {
			StringBuilder test = new StringBuilder();

			for (int j = 0; j < coverage[i].length - 1; j++) {
				if (coverage[i][j])
					test.append("1 ");
				else
					test.append("0 ");
			}

			if (!test.toString().contains("1")) // if a test case does not contains a "1", means it does not coverage anything
				continue ;

			if (coverage[i][coverage[i].length - 1])
				test.append("+\n");
			else
				test.append("-\n");

			suite.append(test);
		}

		Utils.writeFile(suite.toString(), new File(getReportDir().getAbsolutePath() +
				File.separator + "data" + File.separator +
				Properties.TARGET_CLASS + ".matrix"));
	}

	/**
     * Return the folder of where reports should be generated.
     * If the folder does not exist, try to create it
     * 
     * @return
     * @throws RuntimeException if folder does not exist, and we cannot create it
     */
    private static File getReportDir() throws RuntimeException{
        File dir = new File(Properties.REPORT_DIR);

        if (!dir.exists()) {
            if (!dir.mkdirs())
                throw new RuntimeException("Cannot create report dir: " + Properties.REPORT_DIR);
        }

        return dir;
    }
}
