package org.evosuite.statistics;

import com.examples.with.different.packagename.Compositional;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;


public class TimelineForCombinedFitness2Test extends SystemTest {

    private final Criterion[] oldCriterion = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
    
    private final String ANALYSIS_CRITERIA = Properties.ANALYSIS_CRITERIA;
    
    private final boolean ASSERTIONS = Properties.ASSERTIONS;
    
	@After
	public void afterTest() {
		Properties.CRITERION = oldCriterion;
		Properties.ANALYSIS_CRITERIA = ANALYSIS_CRITERIA;
		Properties.ASSERTIONS = ASSERTIONS;
	}	
    @Test
    public void testTimelineForCombinedFitnessAll(){
        EvoSuite evosuite = new EvoSuite();
        String targetClass = Compositional.class.getCanonicalName();
        Properties.ASSERTIONS = false;
        Properties.TARGET_CLASS = targetClass;
        Properties.MINIMIZE = true;
        Properties.CRITERION = new Properties.Criterion[4];
        Properties.CRITERION[0] = Properties.Criterion.ONLYBRANCH;
        Properties.CRITERION[1] = Properties.Criterion.METHODNOEXCEPTION;
        Properties.CRITERION[2] = Properties.Criterion.OUTPUT;
        Properties.CRITERION[3] = Properties.Criterion.ONLYMUTATION;

        StringBuilder analysisCriteria = new StringBuilder();
        analysisCriteria.append(Properties.Criterion.LINE); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.ONLYBRANCH); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.METHODTRACE); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.METHOD); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.METHODNOEXCEPTION); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.OUTPUT); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.ONLYMUTATION); analysisCriteria.append(",");
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

        String strVar1 = RuntimeVariable.CoverageTimeline.toString();
        OutputVariable cov = getLastTimelineVariable(map, strVar1);
        Assert.assertNotNull(cov);
        Assert.assertEquals("Incorrect last timeline value for " + strVar1, 1.0, cov.getValue());

        String strVar2 = RuntimeVariable.OnlyBranchCoverageTimeline.toString();
        OutputVariable method = getLastTimelineVariable(map, strVar2);
        Assert.assertNotNull(method);
        Assert.assertEquals("Incorrect last timeline value for " + strVar2, 1.0, method.getValue());

        String strVar3 = RuntimeVariable.MethodNoExceptionCoverageTimeline.toString();
        OutputVariable methodNE = getLastTimelineVariable(map, strVar3);
        Assert.assertNotNull(methodNE);
        Assert.assertEquals("Incorrect last timeline value for " + strVar3, 1.0, methodNE.getValue());

        String strVar4 = RuntimeVariable.OutputCoverageTimeline.toString();
        OutputVariable output = getLastTimelineVariable(map, strVar4);
        Assert.assertNotNull(output);
        Assert.assertEquals("Incorrect last timeline value for " + strVar4, 1.0, output.getValue());
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
