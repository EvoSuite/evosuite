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
