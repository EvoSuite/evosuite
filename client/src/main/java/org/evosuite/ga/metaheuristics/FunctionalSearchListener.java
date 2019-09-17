package org.evosuite.ga.metaheuristics;

import java.util.function.Consumer;

import org.evosuite.ga.Chromosome;

public class FunctionalSearchListener implements SearchListener {

  private final Consumer<GeneticAlgorithm<?>> searchStartedHandler;
  private final Consumer<GeneticAlgorithm<?>> searchFinishedHandler;
  private final Consumer<GeneticAlgorithm<?>> iterationHandler;
  private final Consumer<Chromosome> fitnessEvaluationHandler;
  private final Consumer<Chromosome> modificationHandler;

  public FunctionalSearchListener(Consumer<GeneticAlgorithm<?>> searchStartedHandler,
      Consumer<GeneticAlgorithm<?>> searchFinishedHandler,
      Consumer<GeneticAlgorithm<?>> iterationHandler,
      Consumer<Chromosome> fitnessEvaluationHandler,
      Consumer<Chromosome> modificationHandler) {
    super();
    this.searchStartedHandler = orDefault(searchStartedHandler);
    this.searchFinishedHandler = orDefault(searchFinishedHandler);
    this.iterationHandler = orDefault(iterationHandler);
    this.fitnessEvaluationHandler = orDefault(fitnessEvaluationHandler);
    this.modificationHandler = orDefault(modificationHandler);
  }
  
  private <T> Consumer<T> orDefault(Consumer<T> handler) {
      return handler == null ? (T arg) -> {} : handler;
  }

  @Override
  public void searchStarted(GeneticAlgorithm<?> algorithm) {
    this.searchStartedHandler.accept(algorithm);
  }

  @Override
  public void iteration(GeneticAlgorithm<?> algorithm) {
    this.iterationHandler.accept(algorithm);
  }

  @Override
  public void searchFinished(GeneticAlgorithm<?> algorithm) {
    this.searchFinishedHandler.accept(algorithm);
  }

  @Override
  public void fitnessEvaluation(Chromosome individual) {
    this.fitnessEvaluationHandler.accept(individual);
  }

  @Override
  public void modification(Chromosome individual) {
    this.modificationHandler.accept(individual);
  }

}
