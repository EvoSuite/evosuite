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

import org.junit.Test;

/**
 * 
 * @author José Campos
 */
public class GenerationUtilTest {

  @Test
  public void testNullGeneration() {
    assertEquals(0, GenerationUtil.getNumberStatements(null));
    assertEquals(0, GenerationUtil.getTotalEffort(null));
    assertEquals(0, GenerationUtil.getTimeBudget(null));
    assertEquals(0, GenerationUtil.getNumberTests(null));
    assertEquals(0, GenerationUtil.getCriteria(null).size());
    assertEquals(0.0, GenerationUtil.getCriterionCoverage(null, ""), 0.0);
    assertEquals(0.0, GenerationUtil.getOverallCoverage(null), 0.0);
  }

  @Test
  public void testFailGeneration() {
    Generation generation = new Generation();
    generation.setFailed(true);
    generation.setTimeBudgetInSeconds(XSDUtils.convert(66));

    assertEquals(0, GenerationUtil.getNumberStatements(generation));
    assertEquals(0, GenerationUtil.getTotalEffort(generation));
    assertEquals(2, GenerationUtil.getTimeBudget(generation));
    assertEquals(0, GenerationUtil.getNumberTests(generation));
    assertEquals(0, GenerationUtil.getCriteria(generation).size());
    assertEquals(0.0, GenerationUtil.getCriterionCoverage(generation, ""), 0.0);
    assertEquals(0.0, GenerationUtil.getOverallCoverage(generation), 0.0);
  }

  @Test
  public void testNoTestSuite() {
    Generation generation = new Generation();
    generation.setFailed(false);
    generation.setSuite(null);
    generation.setTimeBudgetInSeconds(XSDUtils.convert(66));

    assertEquals(0, GenerationUtil.getNumberStatements(generation));
    assertEquals(0, GenerationUtil.getTotalEffort(generation));
    assertEquals(2, GenerationUtil.getTimeBudget(generation));
    assertEquals(0, GenerationUtil.getNumberTests(generation));
    assertEquals(0, GenerationUtil.getCriteria(generation).size());
    assertEquals(0.0, GenerationUtil.getCriterionCoverage(generation, ""), 0.0);
    assertEquals(0.0, GenerationUtil.getOverallCoverage(generation), 0.0);
  }

  @Test
  public void testSuccessfulGeneration() {
    TestSuite suite = new TestSuite();
    suite.setTotalNumberOfStatements(XSDUtils.convert(15));
    suite.setTotalEffortInSeconds(XSDUtils.convert(150));
    suite.setNumberOfTests(XSDUtils.convert(7));

    Coverage branch = new Coverage();
    branch.setCriterion("Branch");
    branch.setCoverageValue(0.8);

    Coverage exception = new Coverage();
    exception.setCriterion("Exception");
    exception.setCoverageValue(0.3);

    suite.getCoverage().add(branch);
    suite.getCoverage().add(exception);

    Generation generation = new Generation();
    generation.setFailed(false);
    generation.setSuite(suite);
    generation.setTimeBudgetInSeconds(XSDUtils.convert(66));

    assertEquals(15, GenerationUtil.getNumberStatements(generation));
    assertEquals(3, GenerationUtil.getTotalEffort(generation));
    assertEquals(7, GenerationUtil.getNumberTests(generation));
    assertEquals(2, GenerationUtil.getCriteria(generation).size());
    assertEquals(0.0, GenerationUtil.getCriterionCoverage(generation, ""), 0.0);
    assertEquals(0.8, GenerationUtil.getCriterionCoverage(generation, "Branch"), 0.0);
    assertEquals(0.3, GenerationUtil.getCriterionCoverage(generation, "Exception"), 0.0);
    assertEquals(0.55, GenerationUtil.getOverallCoverage(generation), 0.0);
  }
}
