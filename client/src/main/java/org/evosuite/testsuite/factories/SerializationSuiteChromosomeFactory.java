package org.evosuite.testsuite.factories;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.DebuggingObjectOutputStream;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationSuiteChromosomeFactory
    implements ChromosomeFactory<TestSuiteChromosome>
{
    private static final long serialVersionUID = -569338946355072318L;

    private static final Logger logger = LoggerFactory.getLogger(SerializationSuiteChromosomeFactory.class);

    private static TestSuiteChromosome previousSuite = null;

    private ChromosomeFactory<TestChromosome> defaultFactory;

    /**
     * The carved test cases are used only with a certain probability P. So, with probability 1-P the 'default' factory
     * is rather used.
     * 
     * @param defaultFactory
     * @throws IllegalStateException if Properties are not properly set
     */
    public SerializationSuiteChromosomeFactory(ChromosomeFactory<TestChromosome> defaultFactory)
        throws IllegalStateException
    {
        this.defaultFactory = defaultFactory;

        previousSuite = new TestSuiteChromosome(this.defaultFactory);
        previousSuite.clearTests();

        this.loadPreviousTests();
    }

    /**
     * Deserialize previous tests
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
                new ObjectInputStream(new FileInputStream(Properties.SEED_DIR + File.separator + Properties.TARGET_CLASS));

            while (true)
            {
                try
                {
                    TestChromosome tc = (TestChromosome) in.readObject();
                    previousSuite.addTest(tc);
                }
                catch (EOFException e)
                {
                    break;
                }
                catch (ClassNotFoundException | IllegalStateException e)
                {
                    logger.error("DESERIALIZATION ERROR: " + e);
                }
            }

            in.close();
        }
        catch (IOException e)
        {
            // ok, maybe we don't have a previous test suite yet
        }
    }

    public static void saveTests(List<TestSuiteChromosome> bestSuites)
    {
    	try
    	{
    		ObjectOutputStream out =
    				new DebuggingObjectOutputStream(new FileOutputStream(Properties.SEED_DIR + File.separator
    						+ Properties.TARGET_CLASS));

        	for(TestSuiteChromosome suite : bestSuites)
        		saveTests(suite, out);
        	
    		out.flush();
    		out.close();
    	}
    	catch (IOException e)
    	{
    		logger.error(e.getMessage());
    	}

    }

    public static void saveTests(TestSuiteChromosome testSuite, ObjectOutputStream out) throws IOException {
    	for (TestChromosome tc : testSuite.getTestChromosomes()) {
    		tc.getTestCase().removeAssertions();
    		out.writeObject(tc);
    	}
    }
    /**
     * Serialize tests
     */
    public static void saveTests(TestSuiteChromosome testSuite)
    {
    	try
    	{
    		ObjectOutputStream out =
    				new DebuggingObjectOutputStream(new FileOutputStream(Properties.SEED_DIR + File.separator
    						+ Properties.TARGET_CLASS));

    		saveTests(testSuite, out);
    		out.flush();
    		out.close();
    	}
    	catch (IOException e)
    	{
    		logger.error(e.getMessage());
    	}
    }

    /**
     * 
     */
    @Override
    public TestSuiteChromosome getChromosome()
    {
        final int previousSuiteSize = previousSuite.getTestChromosomes().size();

        if (Randomness.nextDouble() <= Properties.SEED_CLONE && previousSuiteSize > 0)
            return previousSuite.clone();

        TestSuiteChromosome tsc = new TestSuiteChromosome(this.defaultFactory);
        tsc.clearTests();

        int numTests = Randomness.nextInt(Properties.MIN_INITIAL_TESTS,
                                          Properties.MAX_INITIAL_TESTS + 1);

        for (int i = 0; i < numTests; i++)
        {
            TestChromosome tc = (TestChromosome) this.defaultFactory.getChromosome().clone();
            tsc.addTest(tc);
        }

        return tsc;
    }
}
