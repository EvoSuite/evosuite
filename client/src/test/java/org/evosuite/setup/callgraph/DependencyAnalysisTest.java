package org.evosuite.setup.callgraph;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.setup.DependencyAnalysis;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DependencyAnalysisTest {

	@BeforeClass
	public static void initialize() {
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.context.EntryPointsClass";
		Properties.CRITERION = new Criterion[1];
		Properties.CRITERION[0]=Criterion.IBRANCH;
		List<String> classpath = new ArrayList<>();
		classpath.add(System.getProperty("user.dir") + "/target/test-classes");
		try {
			DependencyAnalysis
					.analyze(
							"com.examples.with.different.packagename.context.EntryPointsClass",
							classpath);
		} catch (ClassNotFoundException | RuntimeException e) {
			Assert.fail();
		}
	}

	@Test
	public void test1levelContext() {
		String context1 = DependencyAnalysis
				.getCallGraph()
				.getAllContextsFromTargetClass(
						"com.examples.with.different.packagename.context.SubClass",
						"checkFiftneen(I)Z").toString();
		assertEquals(
				context1,
				"[com.examples.with.different.packagename.context.EntryPointsClass:dosmt(I)V com.examples.with.different.packagename.context.SubClass:checkFiftneen(I)Z]");
	}
	
	/**
	 * test level 2 context masked by an abtract class
	 */
	@Test
	public void test2levelContext() {
		String context2 = DependencyAnalysis
				.getCallGraph()
				.getAllContextsFromTargetClass(
						"com.examples.with.different.packagename.context.SubClass",
						"bla(I)Z").toString();
		System.out.println(DependencyAnalysis
				.getCallGraph().getViewOfCurrentMethods());
		assertEquals(
				context2,
				"[com.examples.with.different.packagename.context.EntryPointsClass:dosmt(I)V com.examples.with.different.packagename.context.SubClass:checkFiftneen(I)Z com.examples.with.different.packagename.context.SubClass:bla(I)Z]");
	}

	/**
	 * test level 3 context masked by an abstract class and an interface
	 */
	@Test
	public void test3levelContext() {
		String context2 = DependencyAnalysis
				.getCallGraph()
				.getAllContextsFromTargetClass(
						"com.examples.with.different.packagename.context.SubSubClass",
						"innermethod(I)Z").toString();
		System.out.println(DependencyAnalysis
				.getCallGraph().getViewOfCurrentMethods());
		assertEquals(
				context2,
				"[com.examples.with.different.packagename.context.EntryPointsClass:dosmt(I)V com.examples.with.different.packagename.context.SubClass:checkFiftneen(I)Z com.examples.with.different.packagename.context.SubClass:bla(I)Z com.examples.with.different.packagename.context.SubSubClass:innermethod(I)Z]");
	}
	
	//

}