/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.Criterion;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.examples.with.different.packagename.epa.MyBoundedStack;

/**
 * @author Juan Galeotti
 */
public class CoverageAnalysisOfEPATransitionSystemTest extends SystemTestBase {

	@Test
	public void testLineBranchEPATransitionFitnessFunction() {

		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "..", "client", "src",
				"test", "resources", "epas", "MyBoundedStack.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		EvoSuite evosuite = new EvoSuite();

		String targetClass = MyBoundedStack.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.CRITERION = new Criterion[] { /*Criterion.LINE, Criterion.BRANCH,*/ Criterion.EPATRANSITION };

		String[] command = new String[] { "-class", targetClass, "-Djunit=" + targetClass + Properties.JUNIT_SUFFIX,
				"-measureCoverage", "-Depa_xml_path=" + epaXMLFile.getAbsolutePath() };

		SearchStatistics statistics = (SearchStatistics) evosuite.parseCommandLine(command);
		Assert.assertNotNull(statistics);

		Map<String, OutputVariable<?>> data = statistics.getOutputVariables();
		assertEquals(7, (Integer) data.get("Total_Goals").getValue(), 0.0);
		assertEquals(5, (Integer) data.get("Covered_Goals").getValue(), 0.0);
	}
}
