package org.evosuite.testcase;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.DebuggingObjectOutputStream;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationTestsChromosomeFactory
    implements ChromosomeFactory<TestChromosome>
{
    private static final long serialVersionUID = -569338946355072318L;

    private static final Logger logger = LoggerFactory.getLogger(SerializationTestsChromosomeFactory.class);

    private static List<TestChromosome> previousTests = new ArrayList<TestChromosome>();

    private final ChromosomeFactory<TestChromosome> defaultFactory;

    /**
     * The carved test cases are used only with a certain probability P. So, with probability 1-P the 'default' factory
     * is rather used.
     * 
     * @param defaultFactory
     * @throws IllegalStateException if Properties are not properly set
     */
    public SerializationTestsChromosomeFactory(ChromosomeFactory<TestChromosome> defaultFactory)
        throws IllegalStateException
    {
        this.defaultFactory = defaultFactory;
        this.loadPreviousTests();
    }

    /**
     * Deserialize previous tests
     * 
     * @throws IllegalStateException
     */
    private void loadPreviousTests()
    {
        File dir = new File(Properties.SEED_DIR);
        if (!dir.exists()) {
            dir.mkdir();
            return ;
        }

        try
        {
            ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(Properties.SEED_DIR + "/" + Properties.TARGET_CLASS));

            while (true)
            {
                try
                {
                    TestChromosome t = (TestChromosome) in.readObject();
                    previousTests.add(t);
                }
                catch (EOFException e)
                {
                    break;
                }
                catch (ClassNotFoundException e)
                {
                    logger.error("DESERIALIZATION ERROR, Class of a serialized object cannot be found: " + e);
                }
            }

            in.close();
        }
        catch (IOException e)
        {
            logger.error(e.getMessage());
        }
    }

    /**
     * Serialize tests
     */
    public static void saveTests(Chromosome best)
    {
        if (best.size() > 0 || previousTests.size() > 0)
        {
            try
            {
                ObjectOutputStream out =
                    new DebuggingObjectOutputStream(new FileOutputStream(Properties.SEED_DIR + "/"
                        + Properties.TARGET_CLASS));

                // keep the previous suite
                for (TestChromosome tc : previousTests)
                    out.writeObject(tc);

                // and also the new one
                if (best instanceof TestChromosome)
                {
                    ((TestChromosome) best).getTestCase().removeAssertions();
                    out.writeObject(best);
                }
                else if (best instanceof TestSuiteChromosome)
                {
                    for (TestChromosome tc : ((TestSuiteChromosome) best).getTestChromosomes())
                    {
                        tc.getTestCase().removeAssertions();
                        out.writeObject(tc);
                    }
                }

                out.flush();
                out.close();
            }
            catch (IOException e)
            {
                logger.error(e.getMessage());
            }
        }
        else
        {
            logger.error("nothing to serialize");
        }
    }

    /**
     * 
     */
    @Override
    public TestChromosome getChromosome()
    {
        final int N_mutations = Properties.SEED_MUTATIONS;
        final double P_clone = Properties.SEED_CLONE;

        if (Randomness.nextDouble() >= P_clone || previousTests.isEmpty())
        {
            logger.debug("Using random test");
            return defaultFactory.getChromosome();
        }

        // Cloning
        logger.info("Cloning a previous test chromosome");
        TestChromosome chromosome = (TestChromosome) Randomness.choice(previousTests).clone();

        if (N_mutations > 0)
        {
            int numMutations = Randomness.nextInt(N_mutations);
            logger.debug("Mutations: " + numMutations);

            // Delta
            for (int i = 0; i < numMutations; i++)
                chromosome.mutate();
        }

        return chromosome;
    }
}
