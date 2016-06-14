/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with EvoSuite. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.regression;


/*
 * [GA] ALL_MEASURES: Coverage old + Coverage new + State difference + branch distance (only if REGRESSION_BRANCH_DISTANCE is true), 
 * [GA] STATE_DIFFERENCE, 
 * [GA] BRANCH_DISTANCE, 
 * [GA] COVERAGE: Coverage old + Coverage new, 
 * [GA] COVERAGE_OLD, 
 * [GA] COVERAGE_NEW, 
 * [Random Search] RANDOM
 */
public enum RegressionMeasure {
  ALL_MEASURES, 
  STATE_DIFFERENCE, 
  BRANCH_DISTANCE, 
  COVERAGE, 
  COVERAGE_OLD, 
  COVERAGE_NEW, 
  RANDOM
}
