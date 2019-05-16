package org.evosuite.basic;

import com.examples.with.different.packagename.DataUtils1;

import com.examples.with.different.packagename.LoginValidator;
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

        String targetClass = LoginValidator.class.getName();
        Properties.TARGET_CLASS = targetClass;

        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 20;
        Properties.MAX_FEATURE_DISTANCE = true;
        Properties.NOVELTY_THRESHOLD = 0.7;
        Properties.NOVELTY_THRESHOLD_PERCENTAGE = 0.002;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;

        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.RANK_AND_DISTANCE_SELECTION = false;
        Properties.IS_EXPERIMENT = false;
        Properties.NOVELTY_SELECTION = true;
        Properties.SWITCH_NOVELTY_FITNESS = false;
        Properties.DISTANCE_FOR_NOVELTY = false;

        Properties.SWITCH_ITERATIONS = 15;

        Properties.SEARCH_BUDGET = 5;
        Properties.STOPPING_CONDITION = Properties.STOPPING_CONDITION.MAXGENERATIONS;
        Properties.GLOBAL_TIMEOUT = 20;

        Properties.RANKING_TYPE = Properties.RankingType.PREFERENCE_SORTING;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        NoveltySearch<?> ga = (NoveltySearch) getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        System.out.println("Generations : " + ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : " + FeatureFactory.getFeatures());
        Assert.assertEquals("Features not derived correctly",6, FeatureFactory.getFeatures().size());
        Assert.assertEquals("Non-optimal coverage: ", 1.0d, best.getFitness(), 0.001);

    }

    @Test
    public void testCacheBuilderSpec() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = "com.google.common.cache.CacheBuilderSpec";
        Properties.TARGET_CLASS = targetClass;
        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 2550;
        Properties.MAX_FEATURE_DISTANCE = true;
        Properties.NOVELTY_THRESHOLD = 0.4;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;

        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.RANK_AND_DISTANCE_SELECTION = false;
        Properties.NOVELTY_SELECTION = true;

        Properties.SWITCH_NOVELTY_FITNESS = false;
        Properties.SWITCH_ITERATIONS = 5;

        Properties.RANKING_TYPE = Properties.RankingType.PREFERENCE_SORTING;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\guava-18.0", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch) getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("Generations : " + ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : " + FeatureFactory.getFeatures());

        //Assert.assertEquals(1,Archive.getArchiveInstance().getNumberOfCoveredTargets());

    }

    @Test
    public void testBigIntegerMath() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = "com.google.common.math.BigIntegerMath";
        Properties.TARGET_CLASS = targetClass;
        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 2550;
        Properties.MAX_FEATURE_DISTANCE = true;
        Properties.NOVELTY_THRESHOLD = 0.4;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;

        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.RANK_AND_DISTANCE_SELECTION = false;
        Properties.NOVELTY_SELECTION = true;

        Properties.SWITCH_NOVELTY_FITNESS = false;
        Properties.SWITCH_ITERATIONS = 5;

        Properties.RANKING_TYPE = Properties.RankingType.PREFERENCE_SORTING;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\guava-18.0", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch) getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("Generations : " + ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : " + FeatureFactory.getFeatures());

    }

    @Test
    public void testMonitor() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = "com.google.common.util.concurrent.Monitor";
        Properties.TARGET_CLASS = targetClass;
        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 255;
        Properties.MAX_FEATURE_DISTANCE = true;
        Properties.NOVELTY_THRESHOLD = 0.4;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;

        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.RANK_AND_DISTANCE_SELECTION = false;
        Properties.NOVELTY_SELECTION = true;

        Properties.SWITCH_NOVELTY_FITNESS = false;
        Properties.SWITCH_ITERATIONS = 5;

        Properties.RANKING_TYPE = Properties.RankingType.PREFERENCE_SORTING;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\guava-18.0", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch) getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("Generations : " + ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : " + FeatureFactory.getFeatures());

    }

    @Test
    public void testEReader() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = "com.ib.client.EReader";
        Properties.TARGET_CLASS = targetClass;
        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 255;
        Properties.MAX_FEATURE_DISTANCE = true;
        Properties.NOVELTY_THRESHOLD = 0.4;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;

        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.RANK_AND_DISTANCE_SELECTION = false;
        Properties.NOVELTY_SELECTION = true;

        Properties.SWITCH_NOVELTY_FITNESS = false;
        Properties.SWITCH_ITERATIONS = 5;

        Properties.RANKING_TYPE = Properties.RankingType.PREFERENCE_SORTING;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\tullibee", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch) getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("Generations : " + ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : " + FeatureFactory.getFeatures());

    }

    @Test
    public void testEWrapperMsgGenerator() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = "com.ib.client.EWrapperMsgGenerator";
        Properties.TARGET_CLASS = targetClass;
        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 255;
        Properties.MAX_FEATURE_DISTANCE = true;
        Properties.NOVELTY_THRESHOLD = 0.4;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;

        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.RANK_AND_DISTANCE_SELECTION = false;
        Properties.NOVELTY_SELECTION = true;

        Properties.SWITCH_NOVELTY_FITNESS = false;
        Properties.SWITCH_ITERATIONS = 5;

        Properties.RANKING_TYPE = Properties.RankingType.PREFERENCE_SORTING;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\tullibee", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch) getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("Generations : " + ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : " + FeatureFactory.getFeatures());

    }

    @Test
    public void testCommonsMathFunctionPAckage() {
        EvoSuite evosuite = new EvoSuite();

        /*String targetClass = "com.ib.client.EWrapperMsgGenerator";
        Properties.TARGET_CLASS = targetClass;*/
        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 255;
        Properties.MAX_FEATURE_DISTANCE = true;
        Properties.NOVELTY_THRESHOLD = 0.4;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;

        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.RANK_AND_DISTANCE_SELECTION = false;
        Properties.NOVELTY_SELECTION = true;

        Properties.SWITCH_NOVELTY_FITNESS = false;
        Properties.SWITCH_ITERATIONS = 5;

        Properties.RANKING_TYPE = Properties.RankingType.PREFERENCE_SORTING;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\commons-math3-3.2", "-prefix", "org.apache.commons.math3.analysis.function"};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch) getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("Generations : " + ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : " + FeatureFactory.getFeatures());

    }

    @Test
    public void testMOSA() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DataUtils1.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.STRATEGY = Properties.Strategy.MOSUITE;

        //Properties.TEST_ARCHIVE = false;
        Properties.ALGORITHM = Properties.Algorithm.MOSA;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        MOSA<?> ga = (MOSA) getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.print(best.toString());
        /*System.out.println("Feature values : "+ FeatureFactory.getFeatures());
         */
        Assert.assertEquals(1, Archive.getArchiveInstance().getNumberOfCoveredTargets());
    }
}
