package org.evosuite.performance.indicator;

import org.evosuite.Properties;
import org.evosuite.performance.AbstractIndicator;

import java.util.ArrayList;
import java.util.List;

public class IndicatorsFactory {

    /**
     * Returns a list with all the performance indicators
     * todo: please Annibale implement here you trick for the command line parameters
     * @return  a list of indicators
     */
    public static List<AbstractIndicator> getPerformanceIndicator() {
        List<AbstractIndicator> indicators = new ArrayList<>();
        Properties.PerformanceIndicators[] propIndicators = null;

        propIndicators = Properties.MOSA_SECONDARY_OBJECTIVE;

        for (int i = 0; i < propIndicators.length; i++) {
            indicators.add(getIndicator(propIndicators[i]));
        }
        return indicators;
    }

    private static AbstractIndicator getIndicator(Properties.PerformanceIndicators id) {
        switch (id) {
            case METHOD_CALL:
                return new MethodCallCounter();
            case COVERED_METHOD_CALL:
                return new CoveredMethodCallCounter();
            case OBJECTS_INSTANTIATIONS:
                return new ObjectInstantiations();
            case STATEMENTS_COUNTER:
                return new StatementsCounter();
            case STATEMENTS_COVERED:
                return new CoveredStatementsCounter();
            case LOOP_COUNTER:
                return new LoopCounter();
            case TEST_LENGTH:
                return new TestLength();
        }
        throw new RuntimeException("{} is not a supported performance indicator".format(id.toString()));
    }
}
