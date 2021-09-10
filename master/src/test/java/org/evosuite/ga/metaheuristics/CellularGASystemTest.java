/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.ga.Chromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ClassHierarchyIncludingInterfaces;
import com.examples.with.different.packagename.XMLElement2;

/**
 * CellularGA system test
 * @author Nasser Albunian
 *
 */
public class CellularGASystemTest extends SystemTestBase {

    public List<Chromosome<?>> setup(StoppingCondition sc, int budget, String cut) {
        Properties.CRITERION = new Criterion[1];
        Properties.CRITERION[0] = Criterion.BRANCH;
        Properties.ALGORITHM = Algorithm.CELLULAR_GA;
        Properties.POPULATION = 50;
        Properties.STOPPING_CONDITION = sc;
        Properties.SEARCH_BUDGET = budget;
        Properties.MINIMIZE = false;

        EvoSuite evosuite = new EvoSuite();

        Properties.TARGET_CLASS = cut;

        String[] command = new String[]{"-generateSuite", "-class", cut};

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        return new ArrayList<>(ga.getBestIndividuals());
    }

    @Test
    public void testCellularGAWithLimitedTime() {

        List<Chromosome<?>> population = this.setup(StoppingCondition.MAXTIME, 15, XMLElement2.class.getCanonicalName());

        for (Chromosome<?> p : population) {
            Assert.assertNotEquals(p.getCoverage(), 1.0);
        }
    }

    @Test
    public void testCellularGAWithLimitedGenerations() {

        List<Chromosome<?>> population = this.setup(StoppingCondition.MAXGENERATIONS, 10, ClassHierarchyIncludingInterfaces.class.getCanonicalName());

        for (Chromosome<?> p : population) {
            Assert.assertNotEquals(p.getCoverage(), 1.0);
        }
    }
}
