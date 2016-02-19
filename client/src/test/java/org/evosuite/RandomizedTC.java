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
package org.evosuite;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.evosuite.utils.Randomness;
import org.junit.Before;

/**
 * Specify that the given unit test is using some randomized component.
 * This will enforce the seed to be constant and not at random.
 * However, the seed could be changed deterministically (eg each new month).
 *
 *
 * Created by Andrea Arcuri on 22/10/15.
 */
public abstract class RandomizedTC {


    @Before
    public void forceADeterministicSeed(){
        //change seed every month
        long seed = new GregorianCalendar().get(Calendar.MONTH);
        Randomness.setSeed(seed);
    }
}
