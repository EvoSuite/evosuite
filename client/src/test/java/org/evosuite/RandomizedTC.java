package org.evosuite;

import org.evosuite.runtime.Random;
import org.evosuite.utils.Randomness;
import org.junit.BeforeClass;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Specify that the given unit test is using some randomized component.
 * This will enforce the seed to be constant and not at random.
 * However, the seed could be changed deterministically (eg each new month)
 *
 * Created by Andrea Arcuri on 22/10/15.
 */
public abstract class RandomizedTC {


    @BeforeClass
    public static void forceADeterministicSeed(){
        //change seed every month
        long seed = new GregorianCalendar().get(Calendar.MONTH);
        Randomness.setSeed(seed);
    }
}
