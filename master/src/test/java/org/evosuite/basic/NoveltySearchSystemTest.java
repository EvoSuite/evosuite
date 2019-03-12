package org.evosuite.basic;

import com.examples.with.different.packagename.DataUtils;
import com.examples.with.different.packagename.DataUtils1;
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
import static org.evosuite.Properties.SelectionFunction.RANK_CROWD_DISTANCE_TOURNAMENT;

public class NoveltySearchSystemTest extends SystemTestBase {
    @Test
    public void testNoveltySearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DataUtils.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 2550;
        Properties.MAX_FEATURE_DISTANCE = true;
        Properties.NOVELTY_THRESHOLD = 0.4;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;

        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.RANK_AND_DISTANCE_SELECTION = false;
        Properties.NOVELTY_SELECTION = false;

        Properties.SWITCH_NOVELTY_FITNESS = true;
        Properties.SWITCH_ITERATIONS = 5;

        Properties.RANKING_TYPE = Properties.RankingType.PREFERENCE_SORTING;
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
    public void testNoveltySearch1() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DataUtils.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        /*Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH};
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;
        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.NOVELTY_SELECTION = true;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};*/

        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH};
        Properties.SELECTION_FUNCTION = RANK_CROWD_DISTANCE_TOURNAMENT;
        Properties.ALGORITHM = Properties.Algorithm.MOSA;
        Properties.STRATEGY = Properties.Strategy.MOSUITE;
        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        MOSA<?> ga = (MOSA<?>) getGAFromResult(result);
        /*NoveltySearch<?> ga = (NoveltySearch<?>) getGAFromResult(result);*/
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("Generations : "+ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : "+ FeatureFactory.getFeatures());

        //Assert.assertEquals(1,Archive.getArchiveInstance().getNumberOfCoveredTargets());

    }

    @Test
    public void testNoveltySearchUtf8() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = "com.google.common.base.Utf8";
        Properties.TARGET_CLASS = targetClass;
        //Properties.INSTRUMENT_ONLY_FIELD = true;
      /*  Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.NOVELTY, Properties.Criterion.BRANCH};
        //Properties.STRATEGY = Properties.Strategy.MOSUITE;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;
        //Properties.TEST_ARCHIVE = false;
        //Properties.ALGORITHM = Properties.Algorithm.MOSA;
        //Properties.MAX_FEATURE_DISTANCE = false;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;*/

        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH};
        Properties.SELECTION_FUNCTION = RANK_CROWD_DISTANCE_TOURNAMENT;
        Properties.ALGORITHM = Properties.Algorithm.MOSA;
        Properties.STRATEGY = Properties.Strategy.MOSUITE;


        String[] command = new String[]{"-generateMOSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\guava-18.0",  "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        /*NoveltySearch<?> ga = (NoveltySearch)getGAFromResult(result);*/
        MOSA<?> ga = (MOSA<?>) getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("Generations : "+ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : "+ FeatureFactory.getFeatures());

        //Assert.assertEquals(1,Archive.getArchiveInstance().getNumberOfCoveredTargets());

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
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\guava-18.0",  "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch)getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual1();
        System.out.println("Generations : "+ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : "+ FeatureFactory.getFeatures());

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
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\guava-18.0",  "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch)getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual1();
        System.out.println("Generations : "+ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : "+ FeatureFactory.getFeatures());

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
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\guava-18.0",  "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch)getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual1();
        System.out.println("Generations : "+ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : "+ FeatureFactory.getFeatures());

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
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\tullibee",  "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch)getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual1();
        System.out.println("Generations : "+ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : "+ FeatureFactory.getFeatures());

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
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\tullibee",  "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        NoveltySearch<?> ga = (NoveltySearch)getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual1();
        System.out.println("Generations : "+ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : "+ FeatureFactory.getFeatures());

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
