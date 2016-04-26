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

import org.evosuite.xsd.CUT;
import org.evosuite.xsd.Generation;
import org.evosuite.xsd.Project;
import org.evosuite.xsd.TestSuite;
import org.evosuite.xsd.XSDUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author José Campos
 */
public class ProjectStaticDataTest {

  @Test
  public void testIsToTest_NoData() {

    Project project = new Project();
    project.setTotalNumberOfTestableClasses(XSDUtils.convert(0));

    // there is not any data at all
    Assert.assertTrue(new ProjectStaticData().isToTest("foo.Bar", 3));
  }

  @Test
  public void testIsToTest_NoCUTdata() {

    Project project = new Project();
    project.setTotalNumberOfTestableClasses(XSDUtils.convert(0));

    ProjectStaticData data = new ProjectStaticData();
    data.setProject(project);

    // there is not any generation for CUT,
    // so re-test it
    Assert.assertTrue(data.isToTest("foo.Bar", 3));
  }

  @Test
  public void testIsToTest_LastestGenerationFailed() {

    Project project = new Project();
    project.setTotalNumberOfTestableClasses(XSDUtils.convert(1));

    CUT cut = new CUT();
    cut.setFullNameOfTargetClass("foo.Bar");
    cut.setFullNameOfTestSuite("foo.BarTest");

    Generation generation = new Generation();
    generation.setId(XSDUtils.convert(0));
    generation.setFailed(true);

    cut.getGeneration().add(generation);

    project.getCut().add(cut);

    ProjectStaticData data = new ProjectStaticData();
    data.setProject(project);

    // if EvoSuite failed to generate a new test suite
    // for CUT and because we need to get N passing
    // generations to know whether the coverage improved
    // or not, this CUT has to be re-tested
    Assert.assertTrue(data.isToTest("foo.Bar", 3));
  }

  @Test
  public void testIsToTest_NotEnoughGenerations() {

    Project project = new Project();
    project.setTotalNumberOfTestableClasses(XSDUtils.convert(1));

    CUT cut = new CUT();
    cut.setFullNameOfTargetClass("foo.Bar");
    cut.setFullNameOfTestSuite("foo.BarTest");

    Generation generation = new Generation();
    cut.getGeneration().add(generation);

    project.getCut().add(cut);

    ProjectStaticData data = new ProjectStaticData();
    data.setProject(project);

    // not enough data to compare, re-test it
    Assert.assertTrue(data.isToTest("foo.Bar", 3));
  }

  @Test
  public void testIsToTest_OneLastNGenerationsFailed() {

    Project project = new Project();
    project.setTotalNumberOfTestableClasses(XSDUtils.convert(1));

    CUT cut = new CUT();
    cut.setFullNameOfTargetClass("foo.Bar");
    cut.setFullNameOfTestSuite("foo.BarTest");

    for (int i = 0; i < 5; i++) {
      Generation generation = new Generation();
      generation.setId(XSDUtils.convert(i));
      generation.setFailed(i % 2 != 0);
      generation.setModified(false);
      generation.setTimeBudgetInSeconds(XSDUtils.convert(60));

      cut.getGeneration().add(generation);
    }

    project.getCut().add(cut);

    ProjectStaticData data = new ProjectStaticData();
    data.setProject(project);

    // to be considered has improved, all N generations
    // have to end successfully
    Assert.assertTrue(data.isToTest("foo.Bar", 3));
  }

  @Test
  public void testIsToTest_SkippedClass() {

    Project project = new Project();
    project.setTotalNumberOfTestableClasses(XSDUtils.convert(1));

    CUT cut = new CUT();
    cut.setFullNameOfTargetClass("foo.Bar");
    cut.setFullNameOfTestSuite("foo.BarTest");

    Generation g0 = new Generation();
    g0.setId(XSDUtils.convert(0));
    g0.setFailed(false);
    g0.setModified(true);
    g0.setTimeBudgetInSeconds(XSDUtils.convert(60));
    g0.setSuite(new TestSuite()); // empty dummy test suite

    // and add first generation
    cut.getGeneration().add(g0);

    for (int i = 1; i <= 3; i++) {
      Generation g = new Generation();
      g.setId(XSDUtils.convert(i));
      g.setFailed(false);
      g.setModified(false);
      g.setTimeBudgetInSeconds(XSDUtils.convert(60));
      // and no test suite

      cut.getGeneration().add(g);
    }

    Generation g4 = new Generation();
    g4.setId(XSDUtils.convert(4));
    g4.setFailed(false);
    g4.setModified(false);
    g4.setTimeBudgetInSeconds(XSDUtils.convert(0)); // EvoSuite skipped this one

    cut.getGeneration().add(g4);
    project.getCut().add(cut);

    ProjectStaticData data = new ProjectStaticData();
    data.setProject(project);

    // coverage has not improved
    Assert.assertFalse(data.isToTest("foo.Bar", 3));
  }

  @Test
  public void testIsToTest_ActuallyImproved() {
    Project project = new Project();
    project.setTotalNumberOfTestableClasses(XSDUtils.convert(1));

    CUT cut = new CUT();
    cut.setFullNameOfTargetClass("foo.Bar");
    cut.setFullNameOfTestSuite("foo.BarTest");

    Generation g0 = new Generation();
    g0.setId(XSDUtils.convert(0));
    g0.setFailed(false);
    g0.setModified(true);
    g0.setTimeBudgetInSeconds(XSDUtils.convert(60));
    g0.setSuite(new TestSuite()); // empty dummy test suite

    // add first generation
    cut.getGeneration().add(g0);

    for (int i = 1; i <= 3; i++) {
      Generation g = new Generation();
      g.setId(XSDUtils.convert(i));
      g.setFailed(false);
      g.setModified(false);
      g.setTimeBudgetInSeconds(XSDUtils.convert(60));
      g.setSuite(new TestSuite());

      cut.getGeneration().add(g);
    }

    project.getCut().add(cut);

    ProjectStaticData data = new ProjectStaticData();
    data.setProject(project);

    // coverage has improved
    Assert.assertTrue(data.isToTest("foo.Bar", 3));
  }
}
