package de.unisb.cs.st.evosuite;

import org.junit.*;

import com.examples.with.different.packagename.InfiniteLoops;

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

public class TestSUTInfiniteLoops extends SystemTest{

	public static final int defaultTimeout = Properties.TIMEOUT;

	@After
	public void resetProperties(){
		Properties.TIMEOUT = defaultTimeout;
	}

	
	@Test
	public void testInfiniteLoops() throws InterruptedException{
		EvoSuite evosuite = new EvoSuite();

		String targetClass = InfiniteLoops.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		Properties.TIMEOUT = 50;

		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :"+result.getClass(), result instanceof GeneticAlgorithm);

		GeneticAlgorithm ga = (GeneticAlgorithm) result;

		Thread.sleep(100);
		
		int stalled = TestCaseExecutor.getInstance().getNumStalledThreads();
		
		Assert.assertEquals(0, stalled);
	}
}