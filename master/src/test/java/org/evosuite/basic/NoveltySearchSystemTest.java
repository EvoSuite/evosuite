package org.evosuite.basic;

import com.examples.with.different.packagename.DataUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.coverage.dataflow.FeatureFactory;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.NoveltySearch;
import org.evosuite.ga.metaheuristics.mosa.MOSA;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import static org.evosuite.Properties.SelectionFunction.NOVELTY_RANK_TOURNAMENT;

public class NoveltySearchSystemTest extends SystemTestBase {
    @Test
    public void testNoveltySearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DataUtils.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.NOVELTY, Properties.Criterion.BRANCH};
        //Properties.STRATEGY = Properties.Strategy.MOSUITE;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;
        //Properties.TEST_ARCHIVE = false;
        //Properties.ALGORITHM = Properties.Algorithm.MOSA;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch)getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual2();
        System.out.println("Generations : "+ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : "+ FeatureFactory.getFeatures());

        //Assert.assertEquals(1,Archive.getArchiveInstance().getNumberOfCoveredTargets());

    }
    @Test
    public void testMOSA() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DataUtils.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.STRATEGY = Properties.Strategy.MOSUITE;

        //Properties.TEST_ARCHIVE = false;
        Properties.ALGORITHM = Properties.Algorithm.MOSA;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        MOSA<?> ga = (MOSA)getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.print(best.toString());
        /*System.out.println("Feature values : "+ FeatureFactory.getFeatures());
        */
        Assert.assertEquals(1,Archive.getArchiveInstance().getNumberOfCoveredTargets());
    }
}
