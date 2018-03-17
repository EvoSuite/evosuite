package org.evosuite.regression;

import static org.evosuite.regression.ObjectDistanceCalculator.getObjectDistance;
import static org.junit.Assert.assertEquals;

import com.examples.with.different.packagename.ClassWithPublicField;
import java.util.HashMap;
import java.util.Map;
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

  @Test
  public void objectDiff_Basic() {
    ClassWithPublicField p = new ClassWithPublicField();
    ClassWithPublicField q = new ClassWithPublicField();
    // they are equal
    assertEquals(0.0, getDistance(p, q), Double.MIN_VALUE);

    p.x = 43;
    q.x = 42;
    assertEquals(0.666, getDistance(p, q), 0.001);

    p.y = HELLO;
    q.y = "hell";
    // there are two different vars now
    assertEquals(1, getDistance(p, q), 0.001);

    q.x = 43; // make q.x equal to p.x
    assertEquals(0.666, getDistance(p, q), 0.001);

    q.z = new Integer(43); // there are now two different vars
    assertEquals(1, getDistance(p, q), 0.001);

    q.z = new Integer(43000); // larger diff
    assertEquals(1.166, getDistance(p, q), 0.001);
  }

  @Test
  public void objectDiff_Nested() {
    NestedBasicObjectWithPublicFields p = constructNestedObject();
    NestedBasicObjectWithPublicFields q = constructNestedObject();

    assertEquals(0, getDistance(p, q), 0.001);

    q.foo.x = 5000;
    assertEquals(0.583, getDistance(p, q), 0.001);

    q.self.foo.x = 5000;
    assertEquals(0.770, getDistance(p, q), 0.001);

    q.self.foo.y = "bFoo";
    assertEquals(0.868, getDistance(p, q), 0.001);
  }

  @Test
  public void objectDiff_DifferentObjects() {
    NestedBasicObjectWithPublicFields p = constructNestedObject();
    ClassWithPublicField q = new ClassWithPublicField();
    // different classes
    assertEquals(0.5, getDistance(p, q), 0.001);
  }

  @Test
  public void mapDiff() {
    Map<String, Object> p = new HashMap<>();
    Map<String, Object> q = new HashMap<>();

    assertEquals(0.0, getDistance(p, q), 0.001);

    p.put("foo", 42);
    q.put("foo", 42);
    assertEquals(0.0, getDistance(p, q), 0.001);

    q.put("foo", 43);
    assertEquals(0.833, getDistance(p, q), 0.001);

    p.put("bar", constructNestedObject());
    q.put("bar", 43);
    assertEquals(1.1, getDistance(p, q), 0.001);

    q.put("bar", constructNestedObject());
    assertEquals(0.833, getDistance(p, q), 0.001);

    ((NestedBasicObjectWithPublicFields) q.get("bar")).foo.x = 5000;
    assertEquals(1.035, getDistance(p, q), 0.001);

    ((NestedBasicObjectWithPublicFields) q.get("bar")).bar.y = "bFoo"; // should be "aFoo"
    assertEquals(1.134, getDistance(p, q), 0.001);

    q.put("buzz", 1);
    assertEquals(1.329, getDistance(p, q), 0.001);
  }

  @Test
  public void mapDiff_NonStringObject() {
    Map<Integer, String> p = new HashMap<>();
    Map<Integer, String> q = new HashMap<>();

    assertEquals(0.0, getDistance(p, q), 0.001);

    p.put(42, "foo");
    q.put(42, "foo");
    assertEquals(0.0, getDistance(p, q), 0.001);

    q.put(42, "bar");
    //FIXME: this shouldn't be 0.0
    assertEquals(0.0, getDistance(p, q), 0.001);
  }

  private NestedBasicObjectWithPublicFields constructNestedObject() {
    NestedBasicObjectWithPublicFields constructed = new NestedBasicObjectWithPublicFields(
        new ClassWithPublicField(),
        new ClassWithPublicField()
    );
    constructed.foo.x = 10;
    constructed.foo.y = "aFoo";
    constructed.foo.z = 100;
    return constructed;
  }

  class NestedBasicObjectWithPublicFields {

    ClassWithPublicField foo;
    ClassWithPublicField bar;
    NestedBasicObjectWithPublicFields self;

    NestedBasicObjectWithPublicFields() {
      foo = new ClassWithPublicField();
      bar = new ClassWithPublicField();
    }

    NestedBasicObjectWithPublicFields(ClassWithPublicField foo, ClassWithPublicField bar) {
      this.foo = foo;
      this.bar = bar;

      self = new NestedBasicObjectWithPublicFields();

      self.foo.x++;
      self.foo.y += "o";
      self.foo.z++;

      self.bar.x += 2;
      self.bar.y += "oo";
      self.bar.z += 2;
    }
  }

}
