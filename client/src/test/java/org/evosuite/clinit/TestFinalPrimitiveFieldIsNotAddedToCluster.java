package org.evosuite.clinit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.InheritanceTree;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.junit.Test;

import com.examples.with.different.packagename.clinit.FinalPrimitiveField;

public class TestFinalPrimitiveFieldIsNotAddedToCluster {

	/**
	 * As RESET_STATIC_FINAL_FIELDS=true removes the <code>final</code> modifier
	 * of static fields in the target class, the purpose of this test case is to
	 * check that the TestClusterGenerator indeed does not include these fields.
	 * 
	 * 
	 * @throws ClassNotFoundException
	 * @throws RuntimeException
	 */
	@Test
	public void test() throws ClassNotFoundException, RuntimeException {
		Properties.TARGET_CLASS = FinalPrimitiveField.class.getCanonicalName();
		Properties.RESET_STATIC_FINAL_FIELDS = true;

		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
		String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
		DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS, Arrays.asList(cp.split(File.pathSeparator)));
		InheritanceTree tree = DependencyAnalysis.getInheritanceTree();
		TestClusterGenerator gen = new TestClusterGenerator(tree);
		assertNotNull(gen);
		TestCluster cluster = TestCluster.getInstance();
		List<GenericAccessibleObject<?>> testCalls = cluster.getTestCalls();
		assertEquals("Unexpected number of TestCalls", 2, testCalls.size());
	}

}
