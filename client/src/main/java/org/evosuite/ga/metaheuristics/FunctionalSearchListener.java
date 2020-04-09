package org.evosuite.ga.metaheuristics;

import java.util.function.Consumer;

import org.evosuite.ga.Chromosome;

public class FunctionalSearchListener<T extends Chromosome> implements SearchListener<T> {

  private final Consumer<GeneticAlgorithm<T,?>> searchStartedHandler;
  private final Consumer<GeneticAlgorithm<T,?>> searchFinishedHandler;
  private final Consumer<GeneticAlgorithm<T,?>> iterationHandler;
  private final Consumer<Chromosome> fitnessEvaluationHandler;
  private final Consumer<Chromosome> modificationHandler;

  public FunctionalSearchListener(Consumer<GeneticAlgorithm<T, ?>> searchStartedHandler,
      Consumer<GeneticAlgorithm<T,?>> searchFinishedHandler,
      Consumer<GeneticAlgorithm<T,?>> iterationHandler,
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
  public void searchStarted(GeneticAlgorithm<T,?> algorithm) {
    this.searchStartedHandler.accept(algorithm);
  }

  @Override
  public void iteration(GeneticAlgorithm<T,?> algorithm) {
    this.iterationHandler.accept(algorithm);
  }

  @Override
  public void searchFinished(GeneticAlgorithm<T,?> algorithm) {
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
