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
package org.evosuite.instrumentation.testability;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by Andrea Arcuri on 26/03/15.
 */
public class ContainerHelpTest {

    @Test
    public void testContainsTransformation() {
        Set<Integer> firstSet = new HashSet<Integer>();
        firstSet.add(17);
        firstSet.add(626);
        Assert.assertEquals(1, ContainerHelper.collectionContains(firstSet, 17));
        Assert.assertEquals(1, ContainerHelper.collectionContains(firstSet, 626));
        Assert.assertTrue(ContainerHelper.collectionContains(firstSet, 100) < 0);
    }

    @Test
    public void testContainsTransformationList() {
        List<Integer> list = new LinkedList<Integer>();
        list.add(17);
        list.add(626);
        list.add(17);
        Assert.assertEquals(2, ContainerHelper.collectionContains(list, 17));
        Assert.assertEquals(1, ContainerHelper.collectionContains(list, 626));
        Assert.assertTrue(ContainerHelper.collectionContains(list, 100) < 0);
    }

    @Test
    public void testContainsStringTransformation() {
        Set<String> firstSet = new HashSet<String>();
        firstSet.add("foo");
        firstSet.add("bar");
        Assert.assertEquals(1, ContainerHelper.collectionContains(firstSet, "foo"));
        Assert.assertEquals(1, ContainerHelper.collectionContains(firstSet, "bar"));
        Assert.assertTrue(ContainerHelper.collectionContains(firstSet, "zoo") < 0);
    }

    @Test
    public void testContainsStringTransformationList() {
        List<String> list = new LinkedList<String>();
        list.add("foo");
        list.add("bar");
        list.add("foo");
        Assert.assertEquals(2, ContainerHelper.collectionContains(list, "foo"));
        Assert.assertEquals(1, ContainerHelper.collectionContains(list, "bar"));
        Assert.assertTrue(ContainerHelper.collectionContains(list, "zoo") < 0);
    }

    @Test
    public void testContainsAllTransformation() {
        Set<Integer> firstSet = new HashSet<Integer>();
        firstSet.add(17);
        firstSet.add(626);
        Assert.assertEquals(1, ContainerHelper.collectionContains(firstSet, 17));
        Assert.assertEquals(1, ContainerHelper.collectionContains(firstSet, 626));
        Assert.assertTrue(ContainerHelper.collectionContains(firstSet, 100) < 0);
    }

    @Test
    public void testContainsAllTransformationList() {
        List<Integer> list = new LinkedList<Integer>();
        list.add(17);
        list.add(626);
        list.add(17);
        Assert.assertEquals(2, ContainerHelper.collectionContains(list, 17));
        Assert.assertEquals(1, ContainerHelper.collectionContains(list, 626));
        Assert.assertTrue(ContainerHelper.collectionContains(list, 100) < 0);
    }

    @Test
    public void testCollectionEmptyTransformation2() {
        Set<Integer> firstSet = new HashSet<Integer>();
        firstSet.add(17);
        firstSet.add(626);
        Assert.assertEquals(-2, ContainerHelper.collectionIsEmpty(firstSet));
    }

    @Test
    public void testCollectionEmptyTransformation1() {
        List<String> list = new LinkedList<String>();
        list.add("test");
        Assert.assertEquals(-1, ContainerHelper.collectionIsEmpty(list));
    }

    @Test
    public void testCollectionEmptyTransformation0() {
        Set<Integer> firstSet = new HashSet<Integer>();
        Assert.assertEquals(Integer.MAX_VALUE - 2, ContainerHelper.collectionIsEmpty(firstSet));
    }

    @Test
    public void testMapContainsKeyTransformation() {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(17, 235235);
        map.put(23, 233);
        Assert.assertEquals(1, ContainerHelper.mapContainsKey(map, 17));
        Assert.assertEquals(1, ContainerHelper.mapContainsKey(map, 23));
        Assert.assertTrue(ContainerHelper.mapContainsKey(map, 24) < 0);
    }

    @Test
    public void testMapContainsValueTransformation() {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(17, 235235);
        map.put(23, 233);
        Assert.assertEquals(1, ContainerHelper.mapContainsValue(map, 235235));
        Assert.assertEquals(1, ContainerHelper.mapContainsValue(map, 233));
        Assert.assertTrue(ContainerHelper.mapContainsValue(map, 24) < 0);
    }
}
