package org.evosuite.statistics;

import java.util.Map;

import com.examples.with.different.packagename.Compositional;
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

    @Test
    public void testCompositionalFitnessTimelineVariables(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = Compositional.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.COMPOSITIONAL_FITNESS = true;
        Properties.CRITERION = new Properties.Criterion[4];
        Properties.CRITERION[0] = Properties.Criterion.ONLYBRANCH;
        Properties.CRITERION[1] = Properties.Criterion.METHODNOEXCEPTION;
        Properties.CRITERION[2] = Properties.Criterion.METHOD;
        Properties.CRITERION[3] = Properties.Criterion.OUTPUT;


        StringBuilder s = new StringBuilder();
        s.append(RuntimeVariable.CoverageTimeline); s.append(",");
        s.append(RuntimeVariable.OnlyBranchCoverageTimeline); s.append(",");
        s.append(RuntimeVariable.MethodCoverageTimeline); s.append(",");
        s.append(RuntimeVariable.MethodNoExceptionCoverageTimeline); s.append(",");
        s.append(RuntimeVariable.OutputCoverageTimeline);
        Properties.OUTPUT_VARIABLES = s.toString();

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        evosuite.parseCommandLine(command);

        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable method = getLastTimelineVariable(map, RuntimeVariable.MethodCoverageTimeline.toString());

        Assert.assertNotNull(method);
        Assert.assertEquals(1.0, method.getValue());
        OutputVariable methodNE = getLastTimelineVariable(map, RuntimeVariable.MethodNoExceptionCoverageTimeline.toString());
        Assert.assertNotNull(methodNE);
        Assert.assertEquals(1.0, methodNE.getValue());
        OutputVariable output = getLastTimelineVariable(map, RuntimeVariable.OutputCoverageTimeline.toString());
        Assert.assertNotNull(output);
        Assert.assertEquals(1.0, output.getValue());
    }

    @Test
    public void testCompositionalFitnessAllTimelineVariables(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = Compositional.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.COMPOSITIONAL_FITNESS = true;
        Properties.MINIMIZE = true;
        Properties.CRITERION = new Properties.Criterion[3];
        Properties.CRITERION[0] = Properties.Criterion.ONLYBRANCH;
        Properties.CRITERION[1] = Properties.Criterion.METHODNOEXCEPTION;
        Properties.CRITERION[2] = Properties.Criterion.OUTPUT;

        StringBuilder analysisCriteria = new StringBuilder();
        analysisCriteria.append(Properties.Criterion.LINE); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.ONLYBRANCH); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.METHODTRACE); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.METHOD); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.METHODNOEXCEPTION); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.OUTPUT); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.WEAKMUTATION); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.EXCEPTION);
        Properties.ANALYSIS_CRITERIA = analysisCriteria.toString();
        
        StringBuilder outputVariables = new StringBuilder();
        outputVariables.append(RuntimeVariable.CoverageTimeline); outputVariables.append(",");
        outputVariables.append(RuntimeVariable.OnlyBranchCoverageTimeline); outputVariables.append(",");
        outputVariables.append(RuntimeVariable.MethodNoExceptionCoverageTimeline); outputVariables.append(",");
        outputVariables.append(RuntimeVariable.OutputCoverageTimeline);
        Properties.OUTPUT_VARIABLES = outputVariables.toString();

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        evosuite.parseCommandLine(command);
        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable cov = getLastTimelineVariable(map, RuntimeVariable.CoverageTimeline.toString());
        Assert.assertNotNull(cov);
        Assert.assertEquals(1.0, cov.getValue());
        OutputVariable method = getLastTimelineVariable(map, RuntimeVariable.OnlyBranchCoverageTimeline.toString());
        Assert.assertNotNull(method);
        Assert.assertEquals(1.0, method.getValue());
        OutputVariable methodNE = getLastTimelineVariable(map, RuntimeVariable.MethodNoExceptionCoverageTimeline.toString());
        Assert.assertNotNull(methodNE);
        Assert.assertEquals(1.0, methodNE.getValue());
        OutputVariable output = getLastTimelineVariable(map, RuntimeVariable.OutputCoverageTimeline.toString());
        Assert.assertNotNull(output);
        Assert.assertEquals(1.0, output.getValue());
    }

    private OutputVariable getLastTimelineVariable(Map<String, OutputVariable<?>> map, String name) {
        OutputVariable timelineVar = null;
        int max = -1;
        for (Map.Entry<String, OutputVariable<?>> e : map.entrySet()) {
            if (e.getKey().startsWith(name)) {
                int index = Integer.parseInt( (e.getKey().split("_T"))[1] );
                if (index > max) {
                    max = index;
                    timelineVar = e.getValue();
                }
            }
        }
        return timelineVar;
    }
}
