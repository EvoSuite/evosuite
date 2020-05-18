package org.evosuite.statistics.backend;

import org.evosuite.Properties;

/**
 * Factory of Statistics Backend
 *
 * @author Ignacio Lebrero
 */
public class StatisticsBackendFactory {

  /**
   * Builds the type of Statistics backend required
   *
   * @param backendType
   * @return
   */
  public static StatisticsBackend getStatisticsBackend(Properties.StatisticsBackend backendType) {
    switch(backendType) {
      case CONSOLE:
        return new ConsoleStatisticsBackend();
      case CSV:
        return new CSVStatisticsBackend();
      case HTML:
        return new HTMLStatisticsBackend();
      case DEBUG:
        return new DebugStatisticsBackend();
      case NONE:
      default:
        // If no backend is specified, there is no output
        return null;
    }
  }

}
