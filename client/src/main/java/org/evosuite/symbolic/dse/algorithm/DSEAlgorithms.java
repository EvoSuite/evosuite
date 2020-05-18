/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.dse.algorithm;

import org.evosuite.Properties;

/**
 * DSE Algorithms implemented.
 * Please, add your algorithm as well as a short description when you add a new one.
 *
 * @author ignacio lebrero
 */
public enum DSEAlgorithms {
    SAGE(
            "SAGE",
            "Default implementation based on: Automated Whitebox Fuzz Testing, Godefroid, Levin, Molnar",
            new Properties.Criterion[]{Properties.Criterion.BRANCH});

    private String name;
    private String description;
    private Properties.Criterion[] criteria;

    DSEAlgorithms(String name, String description, Properties.Criterion[] criteria) {
        this.name = name;
        this.description = description;
        this.criteria = criteria;
    }

    public String getName() {
        return this.name;
    }
    public String getDescription() {
        return description;
    }
    public Properties.Criterion[] getCriteria() {
        return criteria;
    }
}
