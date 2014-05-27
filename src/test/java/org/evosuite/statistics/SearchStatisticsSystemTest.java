package org.evosuite.statistics;

import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.Dummy;

public class SearchStatisticsSystemTest extends SystemTest{

	@Test
	public void testHandlingOFThreads(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Dummy.class.getCanonicalName();

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
}
