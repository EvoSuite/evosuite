package org.evosuite.statistics;

import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.statistics.MultiThreads;
import com.examples.with.different.packagename.statistics.NoThreads;

public class SearchStatisticsSystemTest extends SystemTest{

	@Test
	public void testHandlingOfNoThreads(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = NoThreads.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.OUTPUT_VARIABLES=""+RuntimeVariable.Threads;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
		Assert.assertNotNull(map);
		OutputVariable threads = map.get(RuntimeVariable.Threads.toString());
		Assert.assertNotNull(threads);
		Assert.assertEquals(1, threads.getValue());
	}

	@Test
	public void testHandlingOfMultiThreads(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = MultiThreads.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.OUTPUT_VARIABLES=""+RuntimeVariable.Threads;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
		Assert.assertNotNull(map);
		OutputVariable threads = map.get(RuntimeVariable.Threads.toString());
		Assert.assertNotNull(threads);
		Assert.assertEquals(3, threads.getValue());
	}

}
