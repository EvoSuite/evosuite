package org.evosuite.statistics;

import org.junit.Test;

public class SearchStatisticsTest {

    @Test
    public void test_setOutputVariable() {
        SearchStatistics statistics = SearchStatistics.getInstance();
        statistics.setOutputVariable(RuntimeVariable.DiversityTimeline, 0.42);
    }
}