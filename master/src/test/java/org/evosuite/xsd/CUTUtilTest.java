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
package org.evosuite.xsd;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * @author José Campos
 */
public class CUTUtilTest {

    @Test
    public void testNoSuccessfulGeneration() {
        CUT cut = new CUT();

        Generation generation = new Generation();
        generation.setFailed(true);
        generation.setModified(true);

        cut.getGeneration().add(generation);
        assertEquals(0, CUTUtil.getNumberStatements(cut));
        assertEquals(0, CUTUtil.getNumberTests(cut));
        Assert.assertTrue(CUTUtil.getCriteria(cut).isEmpty());
        assertEquals(0.0, CUTUtil.getCriterionCoverage(cut, ""), 0.0);
        assertEquals(0.0, CUTUtil.getOverallCoverage(cut), 0.0);
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

        CUT cut = new CUT();
        cut.getGeneration().add(generation);

        assertEquals(15, CUTUtil.getNumberStatements(cut));
        assertEquals(3, CUTUtil.getTotalEffort(cut));
        assertEquals(3, CUTUtil.getTotalEffort(cut, 0));
        assertEquals(0, CUTUtil.getTotalEffort(cut, 1)); // invalid id
        assertEquals(2, CUTUtil.getTimeBudget(cut));
        assertEquals(2, CUTUtil.getTimeBudget(cut, 0));
        assertEquals(0, CUTUtil.getTimeBudget(cut, 1)); // invalid id
        assertEquals(7, CUTUtil.getNumberTests(cut));
        Set<String> criteria = CUTUtil.getCriteria(cut);
        assertEquals(2, criteria.size());
        Assert.assertTrue(criteria.contains("Branch"));
        Assert.assertTrue(criteria.contains("Exception"));
        assertEquals(0.8, CUTUtil.getCriterionCoverage(cut, "Branch"), 0.0);
        assertEquals(0.3, CUTUtil.getCriterionCoverage(cut, "Exception"), 0.0);
        assertEquals(0.55, CUTUtil.getOverallCoverage(cut), 0.0);
    }

    @Test
    public void testLatestGeneration() {
        CUT cut = new CUT();

        Generation g0 = new Generation();
        g0.setId(XSDUtils.convert(0));
        cut.getGeneration().add(g0);

        Generation g1 = new Generation();
        g1.setId(XSDUtils.convert(1));
        cut.getGeneration().add(g1);

        assertEquals(1, CUTUtil.getLatestGeneration(cut).getId().intValue());
    }

    @Test
    public void testLatestSuccessfulGeneration_Failed_and_Modified() {
        CUT cut = new CUT();

        Generation generation = new Generation();
        generation.setFailed(true);
        generation.setModified(true);

        cut.getGeneration().add(generation);
        Assert.assertNull(CUTUtil.getLatestSuccessfulGeneration(cut));
    }

    @Test
    public void testLatestSuccessfulGeneration_Failed_not_Modified() {
        CUT cut = new CUT();

        Generation generation = new Generation();
        generation.setFailed(true);
        generation.setModified(false);

        cut.getGeneration().add(generation);
        Assert.assertNull(CUTUtil.getLatestSuccessfulGeneration(cut));
    }

    @Test
    public void testLatestSuccessfulGeneration_not_Failed_no_Suite() {
        CUT cut = new CUT();

        Generation generation = new Generation();
        generation.setFailed(false);
        generation.setSuite(null);

        cut.getGeneration().add(generation);
        Assert.assertNull(CUTUtil.getLatestSuccessfulGeneration(cut));
    }

    @Test
    public void testLatestSuccessfulGeneration() {
        CUT cut = new CUT();

        Generation generation = new Generation();
        generation.setFailed(false);
        generation.setSuite(new TestSuite());

        cut.getGeneration().add(generation);
        Assert.assertNotNull(CUTUtil.getLatestSuccessfulGeneration(cut));
    }
}
