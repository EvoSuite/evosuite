/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Utility class used to keep track of the execution time needed to reach the maximum coverage
 *
 * @author Annibale Panichella
 */
public class BudgetConsumptionMonitor implements Serializable {

    private static final long serialVersionUID = -4282519578535413645L;

    private static final Logger logger = LoggerFactory.getLogger(BudgetConsumptionMonitor.class);
    /**
     * Coverage achieved in the previous generation
     */
    private double past_coverage;

    /**
     * To keep track when the overall search started
     */
    private final long startGlobalSearch;

    /**
     * Time required to achieve the maximum coverage
     */
    private long time2MaxCoverage;

    /**
     * Constructor that initialises the counters
     */
    public BudgetConsumptionMonitor() {
        past_coverage = 0;
        startGlobalSearch = System.currentTimeMillis();
        time2MaxCoverage = 0;
    }

    /**
     * This method updates the time needed to reach the maximum coverage if
     * the new coverage is greater than the previous one stored in "past_coverage"
     *
     * @param coverage new coverage value
     */
    public void checkMaxCoverage(double coverage) {
        if (coverage > past_coverage) {
            past_coverage = coverage;
            time2MaxCoverage = System.currentTimeMillis() - startGlobalSearch;
            logger.debug("Time to reach max coverage updated to {}", time2MaxCoverage);
        }
    }

    public long getTime2MaxCoverage() {
        return this.time2MaxCoverage;
    }

}
