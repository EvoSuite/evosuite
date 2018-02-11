package org.evosuite.ga.problems.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class MetricsTest {

  @Test
  public void testGetMaximumValue() {
    double front[][] = new double [5][2];
    for (int i = 0; i < front.length; i++) {
      front[i][0] = 0.5;
      front[i][1] = 0.75;
    }

    Spacing spacingMetric = new Spacing();
    double[] max = spacingMetric.getMaximumValues(front);
    assertEquals(0.50, max[0], 0.0);
    assertEquals(0.75, max[1], 0.0);
  }

  @Test
  public void testGetMinimumValue() {
    double front[][] = new double [5][2];
    for (int i = 0; i < front.length; i++) {
      front[i][0] = 0.5;
      front[i][1] = 0.75;
    }

    Spacing spacingMetric = new Spacing();
    double[] min = spacingMetric.getMinimumValues(front);
    assertEquals(0.50, min[0], 0.0);
    assertEquals(0.75, min[1], 0.0);
  }

  @Test
  public void testFrontWithIndividualEquidistanlySpaced() {
    double front[][] = new double [5][2];
    for (int i = 0; i < front.length; i++) {
      front[i][0] = 0.5;
      front[i][1] = 0.75;
    }

    Spacing sp = new Spacing();
    double[] max = sp.getMaximumValues(front);
    double[] min = sp.getMinimumValues(front);

    double normalizedFront[][] = sp.getNormalizedFront(front, max, min);
    assertEquals(0.0, sp.evaluate(normalizedFront), 0.0);
  }

  @Test
  public void testFrontWithIndividualNotEquidistanlySpaced() {
    double front[][] = new double [5][2];
    front[0][0] = 0.05;
    front[0][1] = 0.10;
    front[1][0] = 0.15;
    front[2][1] = 0.20;
    front[3][0] = 0.25;
    front[3][1] = 0.30;
    front[4][0] = 0.35;
    front[4][1] = 0.40;

    Spacing sp = new Spacing();
    double[] max = sp.getMaximumValues(front);
    double[] min = sp.getMinimumValues(front);

    double normalizedFront[][] = sp.getNormalizedFront(front, max, min);
    assertNotEquals(0.0, sp.evaluate(normalizedFront), 0.0);
  }
}
