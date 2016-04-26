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
package org.evosuite.xsd;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * 
 * @author José Campos
 */
public class ProjectUtilTest {

  @Test
  public void testNumberTestedClasses() {
    Project project = new Project();
    assertEquals(0, ProjectUtil.getTotalNumberTestedClasses(project));
    assertEquals(0, ProjectUtil.getNumberLatestTestedClasses(project));
    project.getCut().add(new CUT());
    assertEquals(1, ProjectUtil.getTotalNumberTestedClasses(project));
  }

  @Test
  public void testNumberTestableClasses() {
    Project project = new Project();
    project.setTotalNumberOfTestableClasses(XSDUtils.convert(1));
    assertEquals(1, ProjectUtil.getNumberTestableClasses(project));
  }

  @Test
  public void testEmptyProject() {
    Project project = new Project();

    assertEquals(0, ProjectUtil.getTotalEffort(project));
    assertEquals(0, ProjectUtil.getTimeBudget(project));
    assertEquals(0, ProjectUtil.getNumberGeneratedTestSuites(project));
    Assert.assertTrue(ProjectUtil.getUnionCriteria(project).isEmpty());
    assertEquals(0.0, ProjectUtil.getAverageNumberStatements(project), 0.0);
    assertEquals(0.0, ProjectUtil.getOverallCoverage(project), 0.0);
    assertEquals(0.0, ProjectUtil.getAverageCriterionCoverage(project, ""), 0.0);
    assertEquals(0.0, ProjectUtil.getAverageNumberTests(project), 0.0);
  }

  @Test
  public void testNonEmptyProject() {
    CUT c1 = new CUT();
    c1.setFullNameOfTargetClass("foo.Bar");
    CUT c2 = new CUT();
    c2.setFullNameOfTargetClass("bar.Foo");

    Project project = new Project();
    project.setTotalNumberOfTestableClasses(XSDUtils.convert(2));
    project.getCut().add(c1);
    project.getCut().add(c2);

    Coverage branch = new Coverage();
    branch.setCriterion("Branch");
    branch.setCoverageValue(0.8);

    Coverage exception = new Coverage();
    exception.setCriterion("Exception");
    exception.setCoverageValue(0.3);

    TestSuite suite_1 = new TestSuite();
    suite_1.setNumberOfTests(XSDUtils.convert(7));
    suite_1.setTotalEffortInSeconds(XSDUtils.convert(120));
    suite_1.setTotalNumberOfStatements(XSDUtils.convert(35));
    suite_1.getCoverage().add(branch);
    suite_1.getCoverage().add(exception);

    TestSuite suite_2 = new TestSuite();
    suite_2.setNumberOfTests(XSDUtils.convert(5));
    suite_2.setTotalEffortInSeconds(XSDUtils.convert(40));
    suite_2.setTotalNumberOfStatements(XSDUtils.convert(25));
    suite_2.getCoverage().add(branch);
    suite_2.getCoverage().add(exception);

    Generation g0_1 = new Generation();
    g0_1.setId(XSDUtils.convert(0));
    g0_1.setFailed(false);
    g0_1.setModified(true);
    g0_1.setTimeBudgetInSeconds(XSDUtils.convert(120));
    g0_1.setSuite(suite_1);

    Generation g0_2 = new Generation();
    g0_2.setId(XSDUtils.convert(0));
    g0_2.setFailed(false);
    g0_2.setModified(true);
    g0_2.setTimeBudgetInSeconds(XSDUtils.convert(60));
    g0_2.setSuite(suite_2);

    c1.getGeneration().add(g0_1);
    c2.getGeneration().add(g0_2);

    assertEquals(2, ProjectUtil.getNumberLatestTestedClasses(project));

    // 120 seconds from c1 + 40 seconds from c2 = 160 seconds ~ 3minute
    assertEquals(3, ProjectUtil.getTotalEffort(project));
    // 120 seconds from c1 + 60 seconds from c2 = 180 seconds = 3minute
    assertEquals(3, ProjectUtil.getTimeBudget(project));
    assertEquals(2, ProjectUtil.getNumberGeneratedTestSuites(project));

    Set<String> criteria = ProjectUtil.getUnionCriteria(project);
    assertEquals(2, criteria.size());
    Assert.assertTrue(criteria.contains("Branch"));
    Assert.assertTrue(criteria.contains("Exception"));
    assertEquals(30, ProjectUtil.getAverageNumberStatements(project), 0.0);
    assertEquals(0.55, ProjectUtil.getOverallCoverage(project), 0.0);
    assertEquals(0.0, ProjectUtil.getAverageCriterionCoverage(project, ""), 0.0);
    assertEquals(0.8, ProjectUtil.getAverageCriterionCoverage(project, "Branch"), 0.0);
    assertEquals(0.3, ProjectUtil.getAverageCriterionCoverage(project, "Exception"), 0.0);
    assertEquals(6, ProjectUtil.getAverageNumberTests(project), 0.0);

    // now trying to make it a bit more difficult

    // new generation for c1
    Generation g1 = new Generation();
    g1.setId(XSDUtils.convert(1));
    g1.setFailed(false);
    g1.setModified(false);
    g1.setTimeBudgetInSeconds(XSDUtils.convert(120));
    g1.setSuite(suite_1);
    c1.getGeneration().add(g1);

    // and simulation of a skipped generation for c2
    Generation g2 = new Generation();
    g2.setId(XSDUtils.convert(1));
    g2.setFailed(false);
    g2.setModified(false);
    g2.setTimeBudgetInSeconds(XSDUtils.convert(0));
    g2.setSuite(null);
    c2.getGeneration().add(g2);

    assertEquals(1, ProjectUtil.getNumberLatestTestedClasses(project));

    // this time only c1 has been tested, therefore the time settled by
    // the scheduler and actually spent is lower
    assertEquals(2, ProjectUtil.getTotalEffort(project));
    assertEquals(2, ProjectUtil.getTimeBudget(project));
    // however, the coverage, number of test cases, etc must be
    // the same as the coverage is cumulative
    assertEquals(2, ProjectUtil.getNumberGeneratedTestSuites(project));
    assertEquals(30, ProjectUtil.getAverageNumberStatements(project), 0.0);
    assertEquals(0.55, ProjectUtil.getOverallCoverage(project), 0.0);
    assertEquals(6, ProjectUtil.getAverageNumberTests(project), 0.0);
  }

  @Test
  public void testCUT() {
    CUT cut = new CUT();
    cut.setFullNameOfTargetClass("foo.Bar");

    Project project = new Project();
    project.getCut().add(cut);

    Assert.assertNull(ProjectUtil.getCUT(project, "invalid.ClassName"));
    Assert.assertNotNull(ProjectUtil.getCUT(project, "foo.Bar"));
  }

  @Test
  public void testAllSuccessfulGenerations() {
    CUT cut = new CUT();

    Project project = new Project();
    // no CUTs
    Assert.assertTrue(ProjectUtil.getAllSuccessfulGenerations(project).isEmpty());

    // one generation that failed
    Generation g0 = new Generation();
    g0.setId(XSDUtils.convert(0));
    g0.setFailed(true);
    g0.setSuite(null);

    cut.getGeneration().add(g0);
    project.getCut().add(cut);
    assertEquals(0, ProjectUtil.getAllSuccessfulGenerations(project).size());

    // one successful generation
    Generation g1 = new Generation();
    g1.setId(XSDUtils.convert(1));
    g1.setFailed(false);
    g1.setSuite(new TestSuite());

    cut.getGeneration().add(g1);
    assertEquals(1, ProjectUtil.getAllSuccessfulGenerations(project).size());
  }
}
