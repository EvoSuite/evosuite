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
package org.evosuite.testcase.mutation;

import java.util.*;

import org.evosuite.Properties;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.ConstraintHelper;
import org.evosuite.testcase.ConstraintVerifier;
import org.evosuite.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An insertion strategy that allows for modification of test cases by inserting random statements.
 */
public class RandomInsertion extends AbstractInsertionStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RandomInsertion.class);

    private RandomInsertion() {
        // private constructor, use getInstance() instead
    }

    public static RandomInsertion getInstance() {
        return SingletonContainer.instance;
    }

    @Override
    protected boolean insertUUT(TestCase test, int position) {
        return insertRandomCall(test, position);
    }

    // https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
    private static final class SingletonContainer {
        private static final RandomInsertion instance = new RandomInsertion();
    }

}
