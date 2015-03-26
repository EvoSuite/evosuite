package org.evosuite.instrumentation.testability;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by Andrea Arcuri on 26/03/15.
 */
public class TestContainerReplacementMethods {

    @Test
    public void testContainsTransformation() {
        Set<Integer> firstSet = new HashSet<Integer>();
        firstSet.add(17);
        firstSet.add(626);
        Assert.assertEquals(1, BooleanHelper.collectionContains(firstSet, 17));
        Assert.assertEquals(1, BooleanHelper.collectionContains(firstSet, 626));
        Assert.assertTrue(BooleanHelper.collectionContains(firstSet, 100) < 0);
    }

    @Test
    public void testContainsTransformationList() {
        List<Integer> list = new LinkedList<Integer>();
        list.add(17);
        list.add(626);
        list.add(17);
        Assert.assertEquals(2, BooleanHelper.collectionContains(list, 17));
        Assert.assertEquals(1, BooleanHelper.collectionContains(list, 626));
        Assert.assertTrue(BooleanHelper.collectionContains(list, 100) < 0);
    }

    @Test
    public void testContainsStringTransformation() {
        Set<String> firstSet = new HashSet<String>();
        firstSet.add("foo");
        firstSet.add("bar");
        Assert.assertEquals(1, BooleanHelper.collectionContains(firstSet, "foo"));
        Assert.assertEquals(1, BooleanHelper.collectionContains(firstSet, "bar"));
        Assert.assertTrue(BooleanHelper.collectionContains(firstSet, "zoo") < 0);
    }

    @Test
    public void testContainsStringTransformationList() {
        List<String> list = new LinkedList<String>();
        list.add("foo");
        list.add("bar");
        list.add("foo");
        Assert.assertEquals(2, BooleanHelper.collectionContains(list, "foo"));
        Assert.assertEquals(1, BooleanHelper.collectionContains(list, "bar"));
        Assert.assertTrue(BooleanHelper.collectionContains(list, "zoo") < 0);
    }

    @Test
    public void testContainsAllTransformation() {
        Set<Integer> firstSet = new HashSet<Integer>();
        firstSet.add(17);
        firstSet.add(626);
        Assert.assertEquals(1, BooleanHelper.collectionContains(firstSet, 17));
        Assert.assertEquals(1, BooleanHelper.collectionContains(firstSet, 626));
        Assert.assertTrue(BooleanHelper.collectionContains(firstSet, 100) < 0);
    }

    @Test
    public void testContainsAllTransformationList() {
        List<Integer> list = new LinkedList<Integer>();
        list.add(17);
        list.add(626);
        list.add(17);
        Assert.assertEquals(2, BooleanHelper.collectionContains(list, 17));
        Assert.assertEquals(1, BooleanHelper.collectionContains(list, 626));
        Assert.assertTrue(BooleanHelper.collectionContains(list, 100) < 0);
    }

    @Test
    public void testCollectionEmptyTransformation2() {
        Set<Integer> firstSet = new HashSet<Integer>();
        firstSet.add(17);
        firstSet.add(626);
        Assert.assertEquals(-2, BooleanHelper.collectionIsEmpty(firstSet));
    }

    @Test
    public void testCollectionEmptyTransformation1() {
        List<String> list = new LinkedList<String>();
        list.add("test");
        Assert.assertEquals(-1, BooleanHelper.collectionIsEmpty(list));
    }

    @Test
    public void testCollectionEmptyTransformation0() {
        Set<Integer> firstSet = new HashSet<Integer>();
        Assert.assertEquals(Integer.MAX_VALUE - 2, BooleanHelper.collectionIsEmpty(firstSet));
    }

    @Test
    public void testMapContainsKeyTransformation() {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(17, 235235);
        map.put(23, 233);
        Assert.assertEquals(1, BooleanHelper.mapContainsKey(map, 17));
        Assert.assertEquals(1, BooleanHelper.mapContainsKey(map, 23));
        Assert.assertTrue(BooleanHelper.mapContainsKey(map, 24) < 0);
    }

    @Test
    public void testMapContainsValueTransformation() {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(17, 235235);
        map.put(23, 233);
        Assert.assertEquals(1, BooleanHelper.mapContainsValue(map, 235235));
        Assert.assertEquals(1, BooleanHelper.mapContainsValue(map, 233));
        Assert.assertTrue(BooleanHelper.mapContainsValue(map, 24) < 0);
    }
}
