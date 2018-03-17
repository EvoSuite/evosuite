package org.evosuite.regression;

import static org.evosuite.regression.ObjectDistanceCalculator.getObjectDistance;
import static org.junit.Assert.assertEquals;

import org.evosuite.Properties.Algorithm;
import org.junit.Test;

public class ObjectDistanceCalculatorTest {

  private static final String HELLO = "hello";

  /**
   * Helper to test in both directions
   */
  private double getDistance(Object p, Object q) {
    double distance = getObjectDistance(p, q);
    double distanceReverse = getObjectDistance(q, p);
    assertEquals(distance, distanceReverse, Double.MIN_VALUE);
    return distance;
  }

  @Test
  public void differentNanDouble() {
    double distance = getDistance(Double.NaN, 42.0);
    assertEquals(1, distance, 0.001);
  }

  @Test
  public void differentNanFloat() {
    double distance = getDistance(Float.NaN, 42f);
    assertEquals(1, distance, 0.001);
  }

  @Test
  public void differentNanDoubleFloat() {
    double distance = getDistance(Double.NaN, Float.NaN);
    // they're equal, but different variables
    assertEquals(0.5, distance, 0.001);
  }

  @Test
  public void differentNanFloatDouble() {
    double distance = getDistance(42f, Double.NaN);
    assertEquals(1, distance, 0.001);
  }

  @Test
  public void differentInfinity() {
    double distance = getDistance(Float.POSITIVE_INFINITY, Double.NaN);
    assertEquals(1, distance, 0.001);
  }

  @Test
  public void differentInfinity_sameType() {
    double distance = getDistance(Float.POSITIVE_INFINITY, Float.NaN);
    assertEquals(1, distance, 0.001);
  }

  @Test
  public void differentInfinity_positiveInfinity() {
    double distance = getDistance(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    assertEquals(1, distance, 0.001);
  }

  @Test
  public void differentInfinity_double() {
    double distance = getDistance(Double.POSITIVE_INFINITY, Double.NaN);
    assertEquals(1, distance, 0.001);
  }

  @Test
  public void infinityVsNumeric() {
    double distance = getDistance(Double.POSITIVE_INFINITY, 42.0);
    assertEquals(1, distance, 0.001);
  }

  @Test
  public void enumVsDouble() {
    double distance = getDistance(Algorithm.MONOTONIC_GA, 42.0);
    // one is number, the other isn't
    assertEquals(1, distance, 0.001);
  }

  @Test
  public void enumVsEnum() {
    double distance = getDistance(Algorithm.MONOTONIC_GA, Algorithm.STANDARD_GA);
    // levenshtein
    assertEquals(1.4, distance, 0.001);
  }

  @Test
  public void stringVsString() {
    // levenshtein
    assertEquals(0, getDistance("hello", HELLO), 0.001);
    assertEquals(1, getDistance("helloo", HELLO), 0.001);
    assertEquals(1.166, getDistance("hellooo", HELLO), 0.001);
    assertEquals(1.25, getDistance("helloooo", HELLO), 0.001);
    assertEquals(1.333, getDistance("helloooooo", HELLO), 0.001);
    assertEquals(1.357, getDistance("hAlloooooo", HELLO), 0.001);
    assertEquals(1.388, getDistance("hellooooooooo", HELLO), 0.001);
    assertEquals(1.437, getDistance("helloooooooooooooooo", HELLO), 0.001);
  }

  @Test
  public void charDiff() {
    assertEquals(1.0, getDistance('a', 'b'), 0.001);
    assertEquals(1.461, getDistance('a', 'z'), 0.001);
    assertEquals(1.469, getDistance('a', 'A'), 0.001);
  }

  @Test
  public void boolDiff() {
    assertEquals(1.0, getDistance(true, false), 0.001);
    assertEquals(1.0, getDistance(Boolean.TRUE, false), 0.001);
    assertEquals(1.0, getDistance(Boolean.FALSE, true), 0.001);
  }

  @Test
  public void longDiff() {
    // same but different variables
    assertEquals(0.5, getDistance(42L, 42), 0.001);
    assertEquals(0.5, getDistance(42L, 42.0), 0.001);
    assertEquals(0.5, getDistance(Long.MAX_VALUE, Double.MAX_VALUE), 0.001);

    // use longValue for long comparisons
    assertEquals(1.0, getDistance(Long.MAX_VALUE, Long.MAX_VALUE - 1), 0.001);
    assertEquals(1.166, getDistance(Long.MAX_VALUE, Long.MAX_VALUE - 2), 0.001);
    assertEquals(1.409, getDistance(Long.MAX_VALUE, Long.MAX_VALUE - 10), 0.001);
  }

  @Test
  public void doubleDiff() {
    // one is int, other is double
    assertEquals(0.5, getDistance(42, 42.0), 0.001);
    assertEquals(1.5, getDistance(Double.MAX_VALUE, Double.MIN_VALUE), 0.001);
    // different vars
    assertEquals(0.5, getDistance(Double.MAX_VALUE, Float.MAX_VALUE), 0.001);
  }
}