package org.evosuite.ga.metaheuristics.mapelites;

import java.io.Serializable;
import java.util.Arrays;

import org.evosuite.assertion.Inspector;

/**
 * 
 * @author Felix Prasse
 *
 */
public class FeatureVector implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private class Entry {
    private final String name;
    private final Object value;
    
    public Entry(Inspector inspector, Object instance) {
      try {
        this.value = inspector.getValue(instance);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
      this.name = inspector.getMethodCall();
    }

    public String getName() {
      return name;
    }

    public Object getValue() {
      return value;
    }

    @Override
    public String toString() {
      return "Entry [name=" + name + ", value=" + value + "]";
    }
  }
  
  private final Entry[] features;
  
  public FeatureVector(final Inspector[] inspectors, final Object instance) {
    this.features = new Entry[inspectors.length];
    
    for(int i = 0; i < inspectors.length; ++i) {
        this.features[i] =  new Entry(inspectors[i], instance);
    }
  }

  @Override
  public String toString() {
    return "FeatureVector [features=" + Arrays.toString(features) + "]";
  }

}
