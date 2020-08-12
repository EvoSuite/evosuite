package org.evosuite.ga.metaheuristics;

import java.util.function.Consumer;

import org.evosuite.ga.Chromosome;

public class FunctionalSearchListener<T extends Chromosome<T>> implements SearchListener<T> {

  private final Consumer<GeneticAlgorithm<T>> searchStartedHandler;
  private final Consumer<GeneticAlgorithm<T>> searchFinishedHandler;
  private final Consumer<GeneticAlgorithm<T>> iterationHandler;
  private final Consumer<Chromosome<T>> fitnessEvaluationHandler;
  private final Consumer<Chromosome<T>> modificationHandler;

  public FunctionalSearchListener(Consumer<GeneticAlgorithm<T>> searchStartedHandler,
      Consumer<GeneticAlgorithm<T>> searchFinishedHandler,
      Consumer<GeneticAlgorithm<T>> iterationHandler,
      Consumer<Chromosome<T>> fitnessEvaluationHandler,
      Consumer<Chromosome<T>> modificationHandler) {
    super();
    this.searchStartedHandler = orDefault(searchStartedHandler);
    this.searchFinishedHandler = orDefault(searchFinishedHandler);
    this.iterationHandler = orDefault(iterationHandler);
    this.fitnessEvaluationHandler = orDefault(fitnessEvaluationHandler);
    this.modificationHandler = orDefault(modificationHandler);
  }

  public FunctionalSearchListener(FunctionalSearchListener<T> that) {
    // no deep copies
    this.searchStartedHandler= that.searchStartedHandler;
    this.searchFinishedHandler = that.searchFinishedHandler;
    this.iterationHandler = that.iterationHandler;
    this.fitnessEvaluationHandler = that.fitnessEvaluationHandler;
    this.modificationHandler = that.modificationHandler;
  }

  private <U> Consumer<U> orDefault(Consumer<U> handler) {
      return handler == null ? (U arg) -> {} : handler;
  }

  @Override
  public void searchStarted(GeneticAlgorithm<T> algorithm) {
    this.searchStartedHandler.accept(algorithm);
  }

  @Override
  public void iteration(GeneticAlgorithm<T> algorithm) {
    this.iterationHandler.accept(algorithm);
  }

  @Override
  public void searchFinished(GeneticAlgorithm<T> algorithm) {
    this.searchFinishedHandler.accept(algorithm);
  }

  @Override
  public void fitnessEvaluation(T individual) {
    this.fitnessEvaluationHandler.accept(individual);
  }

  @Override
  public void modification(T individual) {
    this.modificationHandler.accept(individual);
  }

}
