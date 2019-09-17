package org.evosuite.ga.metaheuristics.mapelites;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

public class FitnessFunctionWrapper {
  /**
   * Counter for Feedback-Directed Sampling
   * @see https://arxiv.org/pdf/1901.01541.pdf
   */
  private final Counter counter;
  private final TestFitnessFunction fitnessFunction;

  public FitnessFunctionWrapper(TestFitnessFunction fitnessFunction) {
    super();
    this.fitnessFunction = fitnessFunction;
    this.counter = new Counter();
  }

  public double getFitness(TestChromosome individual) {
    return this.fitnessFunction.getFitness(individual);
  }

  public boolean isCovered(TestChromosome individual) {
    return this.fitnessFunction.isCovered(individual);
  }

  public Counter getCounter() {
    return this.counter;
  }
}
