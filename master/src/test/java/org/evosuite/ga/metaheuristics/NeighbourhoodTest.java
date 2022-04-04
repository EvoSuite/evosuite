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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Neighbourhood;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

/**
 * Unit tests for Neighbourhood's functions (i.e, part of Cellular GA functions)
 * @author Nasser Albunian
 *
 */
public class NeighbourhoodTest {

    List<TestSuiteChromosome> population = new ArrayList<>();

    public void constructPopulation() {

        Properties.POPULATION = 16;

        TestSuiteChromosome[] testSuiteChromObjects = new TestSuiteChromosome[Properties.POPULATION];

        for (int i = 0; i < testSuiteChromObjects.length; i++) {
            testSuiteChromObjects[i] = new TestSuiteChromosome();
            DefaultTestCase testcase = new DefaultTestCase();
            StringPrimitiveStatement stringStatement = new StringPrimitiveStatement(testcase, "any string statatement for TS" + i);
            testcase.addStatement(stringStatement);
            testSuiteChromObjects[i].addTest(testcase);
            population.add(testSuiteChromObjects[i]);
        }

    }


    @Test
    public void testRingTopology_leftNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.ringTopology(population, 2);

        TestSuiteChromosome exepcted_individual = population.get(1);
        TestSuiteChromosome returned_individual = neighbors.get(0);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testRingTopology_rightNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.ringTopology(population, 2);

        TestSuiteChromosome exepcted_individual = population.get(3);
        TestSuiteChromosome returned_individual = neighbors.get(2);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testRingTopology_mostLeftNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.ringTopology(population, 0);

        TestSuiteChromosome exepcted_individual = population.get(15);
        TestSuiteChromosome returned_individual = neighbors.get(0);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testRingTopology_mostRightNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.ringTopology(population, 15);

        TestSuiteChromosome exepcted_individual = population.get(0);
        TestSuiteChromosome returned_individual = neighbors.get(2);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testNorthNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.linearFive(population, 5);

        TestSuiteChromosome exepcted_individual = population.get(1);
        TestSuiteChromosome returned_individual = neighbors.get(0);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testSouthNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.linearFive(population, 5);

        TestSuiteChromosome exepcted_individual = population.get(9);
        TestSuiteChromosome returned_individual = neighbors.get(1);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testEastNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.linearFive(population, 5);

        TestSuiteChromosome exepcted_individual = population.get(6);
        TestSuiteChromosome returned_individual = neighbors.get(2);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testWestNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.linearFive(population, 5);

        TestSuiteChromosome exepcted_individual = population.get(4);
        TestSuiteChromosome returned_individual = neighbors.get(3);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testNorthEastNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactNine(population, 5);

        TestSuiteChromosome exepcted_individual = population.get(2);
        TestSuiteChromosome returned_individual = neighbors.get(6);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testNorthWestNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactNine(population, 5);

        TestSuiteChromosome exepcted_individual = population.get(0);
        TestSuiteChromosome returned_individual = neighbors.get(4);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testSouthEastNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactNine(population, 5);

        TestSuiteChromosome exepcted_individual = population.get(10);
        TestSuiteChromosome returned_individual = neighbors.get(7);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testSouthWestNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactNine(population, 5);

        TestSuiteChromosome exepcted_individual = population.get(8);
        TestSuiteChromosome returned_individual = neighbors.get(5);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testNorthNorthNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactThirteen(population, 10);

        TestSuiteChromosome exepcted_individual = population.get(2);
        TestSuiteChromosome returned_individual = neighbors.get(8);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testSouthSouthNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactThirteen(population, 10);

        TestSuiteChromosome exepcted_individual = population.get(2);
        TestSuiteChromosome returned_individual = neighbors.get(9);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testEastEastNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactThirteen(population, 10);

        TestSuiteChromosome exepcted_individual = population.get(8);
        TestSuiteChromosome returned_individual = neighbors.get(10);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testWestWestNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactThirteen(population, 5);

        TestSuiteChromosome exepcted_individual = population.get(7);
        TestSuiteChromosome returned_individual = neighbors.get(11);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testMostNorthWestNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactThirteen(population, 0);

        TestSuiteChromosome exepcted_individual = population.get(15);
        TestSuiteChromosome returned_individual = neighbors.get(4);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testMostNorthEastNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactThirteen(population, 3);

        TestSuiteChromosome exepcted_individual = population.get(12);
        TestSuiteChromosome returned_individual = neighbors.get(6);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testMostSouthWestNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactThirteen(population, 12);

        TestSuiteChromosome exepcted_individual = population.get(3);
        TestSuiteChromosome returned_individual = neighbors.get(5);

        assertEquals(exepcted_individual, returned_individual);
    }

    @Test
    public void testMostSouthEastNeighbour() {

        this.constructPopulation();

        Neighbourhood<TestSuiteChromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);

        List<TestSuiteChromosome> neighbors = neighbourhood.compactThirteen(population, 15);

        TestSuiteChromosome exepcted_individual = population.get(0);
        TestSuiteChromosome returned_individual = neighbors.get(7);

        assertEquals(exepcted_individual, returned_individual);
    }

}
