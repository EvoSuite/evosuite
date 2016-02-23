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
package org.evosuite.continuous.project;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.continuous.Simple;
import com.examples.with.different.packagename.continuous.Trivial;

public class ProjectAnalyzerIntTest {

	@BeforeClass
	public static void initClass() {
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
	}

    @Test
    public void testActualScanWithPrefix(){
        String target = "target/test-classes";
        String prefix = "com.examples";

        ProjectAnalyzer pa = new ProjectAnalyzer(target,prefix,null);

        ProjectStaticData data = pa.analyze();

        Assert.assertTrue(data.getTotalNumberOfClasses() > 0);
    }

    @Test
    public void testActualScanWithNoPrefix(){
        String target = "target/test-classes";
        String prefix = null;

        ProjectAnalyzer pa = new ProjectAnalyzer(target,prefix,null);

        ProjectStaticData data = pa.analyze();

        Assert.assertTrue(data.getTotalNumberOfClasses() > 0);
    }


    @Test
	public void testBranches() {

		String[] cuts = new String[] { Simple.class.getName(), Trivial.class.getName() };

		ProjectAnalyzer pa = new ProjectAnalyzer(cuts);

		ProjectStaticData data = pa.analyze();

		Assert.assertEquals(2, data.getTotalNumberOfClasses());

		ClassInfo simple = data.getClassInfo(Simple.class.getName());
		Assert.assertNotNull(simple);
		Assert.assertEquals(2, simple.numberOfBranches);

		ClassInfo trivial = data.getClassInfo(Trivial.class.getName());
		Assert.assertNotNull(trivial);
		Assert.assertEquals(1, trivial.numberOfBranches);
	}
}
