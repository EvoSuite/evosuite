package org.evosuite.instrumentation;

import com.examples.with.different.packagename.DataUtils;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.dataflow.FeatureFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.execution.reset.ClassReInitializer;
import org.evosuite.utils.Randomness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FeatureInstrumentationTest {
    private static java.util.Properties currentProperties;

    @Before
    public void setUp()
    {
        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        Properties.getInstance().resetToDefaults();
        Randomness.setSeed(42);
        TestGenerationContext.getInstance().resetContext();
        ClassReInitializer.resetSingleton();
        Randomness.setSeed(42);
        currentProperties = (java.util.Properties) System.getProperties().clone();
    }
    @After
    public void tearDown()
    {
        TestGenerationContext.getInstance().resetContext();
        System.setProperties(currentProperties);
        Properties.getInstance().resetToDefaults();
    }

    @Test
    public void minimizeSuiteHalfCoverage() throws ClassNotFoundException, NoSuchFieldException, SecurityException, ConstructionFailedException, NoSuchMethodException {
        Properties.TARGET_CLASS = DataUtils.class.getCanonicalName();
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.NOVELTY, Properties.Criterion.BRANCH};
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
        assertEquals(3, FeatureFactory.getFeatures().size());
    }
}
