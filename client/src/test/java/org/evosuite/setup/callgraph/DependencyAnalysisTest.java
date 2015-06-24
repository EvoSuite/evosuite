package org.evosuite.setup.callgraph;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.setup.DependencyAnalysis;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DependencyAnalysisTest {

	@BeforeClass
	public static void initialize() {
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.context.complex.EntryPointsClass";
		Properties.CRITERION = new Criterion[1];
		Properties.CRITERION[0]=Criterion.IBRANCH;
		List<String> classpath = new ArrayList<>();
		String cp = System.getProperty("user.dir") + "/target/test-classes";
		classpath.add(cp);
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
		try {
			DependencyAnalysis
					.analyzeClass(
							"com.examples.with.different.packagename.context.complex.EntryPointsClass",
							classpath);
		} catch (ClassNotFoundException | RuntimeException e) {
			Assert.fail(e.toString());
		}
	}

	@Test
	public void test1levelContext() {
		String context1 = DependencyAnalysis
				.getCallGraph()
				.getAllContextsFromTargetClass(
						"com.examples.with.different.packagename.context.complex.SubClass",
						"checkFiftneen(I)Z").toString();

		assertEquals(
				context1,
				"[com.examples.with.different.packagename.context.complex.EntryPointsClass:dosmt(ILjava/lang/String;D)V com.examples.with.different.packagename.context.complex.SubClass:checkFiftneen(I)Z]");
	}
	
	/**
	 * test level 2 context masked by an abtract class
	 */
	@Test
	public void test2levelContext() {
		String context2 = DependencyAnalysis
				.getCallGraph()
				.getAllContextsFromTargetClass(
						"com.examples.with.different.packagename.context.complex.SubClass",
						"bla(I)Z").toString(); 

		assertEquals(
				context2,
				"[com.examples.with.different.packagename.context.complex.EntryPointsClass:dosmt(ILjava/lang/String;D)V com.examples.with.different.packagename.context.complex.SubClass:checkFiftneen(I)Z com.examples.with.different.packagename.context.complex.SubClass:bla(I)Z]");
	}

	/**
	 * test level 3 context masked by an abstract class and an interface
	 */
	@Test
	public void test3levelContext() {
		String context2 = DependencyAnalysis
				.getCallGraph()
				.getAllContextsFromTargetClass(
						"com.examples.with.different.packagename.context.complex.SubSubClass",
						"innermethod(I)Z").toString(); 
		assertEquals(
				context2,
				"[com.examples.with.different.packagename.context.complex.EntryPointsClass:dosmt(ILjava/lang/String;D)V com.examples.with.different.packagename.context.complex.SubClass:checkFiftneen(I)Z com.examples.with.different.packagename.context.complex.SubClass:bla(I)Z com.examples.with.different.packagename.context.complex.SubSubClass:innermethod(I)Z]");
	}
	
	@Test
	public void testContextInParamethers() {
		String context2 = DependencyAnalysis
				.getCallGraph()
				.getAllContextsFromTargetClass(
						"com.examples.with.different.packagename.context.complex.ParameterObject",
						"isEnabled()Z").toString();
		assertEquals(
				context2,
				"[com.examples.with.different.packagename.context.complex.EntryPointsClass:doObj(Lcom/examples/with/different/packagename/context/complex/AParameterObject;)V com.examples.with.different.packagename.context.complex.ParameterObject:isEnabled()Z]");
	}
	
	//

}