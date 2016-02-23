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
package org.mockito.configuration;

/**
 * Class needed to be on classpath to configure Mockito, which cannot be
 * configured programmatically :(
 *
 * This is essential during search, but should NOT be part of standalone runtime.
 *
 * During search, the same SUT class can be loaded by different classloader (eg, search,
 * assertion generation, junit checks), and would lead to class cast exceptions because
 * Mockito does cache class definitions. So, we have to disable such behavior
 *
 * Created by Andrea Arcuri on 21/08/15.
 */
public class MockitoConfiguration extends DefaultMockitoConfiguration {

    @Override
    public boolean enableClassCache(){
        return false;
    }
}
