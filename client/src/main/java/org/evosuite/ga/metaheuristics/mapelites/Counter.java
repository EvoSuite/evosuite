package org.evosuite.ga.metaheuristics.mapelites;

public class Counter implements Comparable<Counter> {
  private int value;
  
  public int getValue() {
    return this.value;
  }
  
  public void increment() {
    ++this.value;
  }
  
  public void reset() {
    this.value = 0;
  }

  @Override
  public int compareTo(Counter o) {
    return Integer.compare(this.value, o.value);
  }
}
