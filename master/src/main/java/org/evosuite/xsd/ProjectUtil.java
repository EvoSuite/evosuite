/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.xsd;

/**
 * 
 * @author José Campos
 */
public abstract class ProjectUtil {

  public static int getNumberTestedClasses(Project project) {
    return project.getCut().size();
  }

  public static int getNumberTestableClasses(Project project) {
    return project.getTotalNumberOfTestableClasses().intValue();
  }

  public static CUT getCUT(Project project, String className) {
    return project.getCut().parallelStream()
        .filter(p -> p.getFullNameOfTargetClass().equals(className))
        .findFirst().orElse(null);
  }

  public static double getOverallCoverage(Project project) {

    if (project.getCut().isEmpty()) {
      return 0.0;
    }

    double overallCoverage = 0.0;
    for (CUT cut : project.getCut()) {
      // search for the latest successful test generation of 'cut'
      Generation lastSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(cut);

      if (lastSuccessfulGeneration == null) {
        // for a particular 'cut', there was not a single successful test generation
        continue;
      }

      overallCoverage += GenerationUtil.getOverallCoverage(lastSuccessfulGeneration);
    }

    return overallCoverage / project.getTotalNumberOfTestableClasses().doubleValue();
  }

  public static int getNumberGeneratedTestSuites(Project project) {

    if (project.getCut().isEmpty()) {
      return 0;
    }

    int number_generated_test_suites = 0;
    for (CUT cut : project.getCut()) {
      // search for the latest successful test generation of 'cut'
      Generation lastSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(cut);

      if (lastSuccessfulGeneration == null) {
        // for a particular 'cut', there was not a single successful test generation
        continue;
      }

      number_generated_test_suites++;
    }

    return number_generated_test_suites;
  }
}
