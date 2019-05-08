package org.evosuite.basic;

import com.examples.with.different.packagename.*;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.util.List;

import static org.evosuite.Properties.SelectionFunction.NOVELTY_RANK_TOURNAMENT;
import static org.evosuite.Properties.SelectionFunction.RANK_CROWD_DISTANCE_TOURNAMENT;
import static org.evosuite.statistics.backend.CSVStatisticsBackend.getReportDir;

public class NoveltySearchSystemTest extends SystemTestBase {
    @Test
    public void testNoveltySearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = LoginValidator.class.getName();
        Properties.TARGET_CLASS = targetClass;

        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 2000;
        Properties.MAX_FEATURE_DISTANCE = true;
        Properties.NOVELTY_THRESHOLD = 0.7;
        Properties.NOVELTY_THRESHOLD_PERCENTAGE = 0.002;
        Properties.STRATEGY = Properties.Strategy.NOVELTY;
        Properties.SELECTION_FUNCTION = NOVELTY_RANK_TOURNAMENT;

        Properties.RANK_AND_NOVELTY_SELECTION = false;
        Properties.RANK_AND_DISTANCE_SELECTION = false; Properties.IS_EXPERIMENT=false;
        Properties.NOVELTY_SELECTION = true;
        Properties.SWITCH_NOVELTY_FITNESS = false;
        Properties.DISTANCE_FOR_NOVELTY = false;

        Properties.SWITCH_ITERATIONS = 15;

        Properties.SEARCH_BUDGET = 200;
        Properties.STOPPING_CONDITION = Properties.STOPPING_CONDITION.MAXGENERATIONS;
        Properties.GLOBAL_TIMEOUT = 2000;

        Properties.RANKING_TYPE = Properties.RankingType.PREFERENCE_SORTING;
        Properties.ALGORITHM = Properties.Algorithm.NOVELTY;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        NoveltySearch<?> ga = (NoveltySearch)getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual2();

        System.out.println("Generations : "+ga.getAge());
        System.out.print(best.toString());
        System.out.println("Feature values : "+ FeatureFactory.getFeatures());
        //writeFeatureData("NOVELTY_SELECTION", FeatureFactory.getTrueList(), FeatureFactory.getFalseList());


        //Assert.assertEquals(1,Archive.getArchiveInstance().getNumberOfCoveredTargets());

    }

    public void writeFeatureData(String config, List<Integer> data1, List<Integer> data2) {
        // Write to evosuite-report/statistics.csv
        try {
            File outputDir = getReportDir();
            File f = new File(outputDir.getAbsolutePath() + File.separator + "statistics_feature.csv");
            BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
            if (f.length() == 0L) {
                out.write("Configuration_id"+",True,False" + "\n");
            }
            out.write(getCSVData1(config, data1, data2) + "\n");
            out.close();

        } catch (IOException e) {
            //logger.warn("Error while writing statistics: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    private String getCSVData1(String config, List<Integer> data1, List<Integer> data2) {
        StringBuilder r = new StringBuilder();
        int length = data1.size() < data2.size() ? data1.size() : data2.size();
        for(int i=0; i < length; i++){
            r.append(config).append(",").append(data1.get(i)).append(",").append(data2.get(i)).append("\n");
        }
        for(int i=length; i < data2.size(); i++){
            r.append(config).append(",").append(0).append(",").append(data2.get(i)).append("\n");
        }
        return r.toString();
    }
    @Test
    public void testNoveltySearch1() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DataUtils1.class.getCanonicalName();
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
        String[] command = new String[]{"-generateSuite", "-projectCP", "C:\\Users\\Prathmesh\\Downloads\\subjects-icst15\\subjects\\commons-math3-3.2",  "-prefix", "org.apache.commons.math3.analysis.function"};

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

        String targetClass = DataUtils1.class.getCanonicalName();
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
