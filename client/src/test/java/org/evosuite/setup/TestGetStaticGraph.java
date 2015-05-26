package org.evosuite.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.classpath.ClassPathHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.staticusage.Bar1;
import com.examples.with.different.packagename.staticusage.Bar4;
import com.examples.with.different.packagename.staticusage.Bar3;
import com.examples.with.different.packagename.staticusage.Bar2;
import com.examples.with.different.packagename.staticusage.Bar5;
import com.examples.with.different.packagename.staticusage.Bar6;
import com.examples.with.different.packagename.staticusage.Bar7;
import com.examples.with.different.packagename.staticusage.BarBar1;
import com.examples.with.different.packagename.staticusage.Cycle1;
import com.examples.with.different.packagename.staticusage.Cycle2;
import com.examples.with.different.packagename.staticusage.Foo;


public class TestGetStaticGraph {


	@BeforeClass
	public static void init(){
		String cp = System.getProperty("user.dir") + "/target/test-classes";
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
	}

	@Test
	public void testFoo() {
		String targetClassName = Foo.class.getCanonicalName();
		GetStaticGraph graph = GetStaticGraphGenerator
				.generate(targetClassName);

		Set<String> expectedClasses = new HashSet<String>(Arrays.asList(
				Foo.class.getCanonicalName(), Bar1.class.getCanonicalName(),
				Bar2.class.getCanonicalName(), Bar3.class.getCanonicalName(),
				Bar4.class.getCanonicalName(), Bar5.class.getCanonicalName(),
				Bar6.class.getCanonicalName(), Bar7.class.getCanonicalName(),
				BarBar1.class.getCanonicalName()));

		Set<String> allClasses = new HashSet<String>();
		allClasses.addAll(graph.getSourceClasses());
		allClasses.addAll(graph.getTargetClasses());

		assertEquals(expectedClasses, allClasses);

		Map<String, Set<String>> expectedStaticFields = new HashMap<String, Set<String>>();
		expectedStaticFields.put(Bar2.class.getCanonicalName(),
				new HashSet<String>(Arrays.asList("fieldBar2")));
		expectedStaticFields.put(Bar6.class.getCanonicalName(),
				new HashSet<String>(Arrays.asList("fieldBar6")));
		expectedStaticFields.put(Bar7.class.getCanonicalName(),
				new HashSet<String>(Arrays.asList("fieldBar7")));

		Map<String, Set<String>> staticFields = graph.getStaticFields();
		assertEquals(expectedStaticFields, staticFields);
	}

	@Test
	public void testCycle() {
		String targetClassName = Cycle1.class.getCanonicalName();
		GetStaticGraph graph = GetStaticGraphGenerator
				.generate(targetClassName);

		Set<String> expectedSourceClasses = new HashSet<String>(Arrays.asList(
				Cycle1.class.getCanonicalName(),
				Cycle2.class.getCanonicalName()));

		assertEquals(expectedSourceClasses, graph.getSourceClasses());

		Set<String> expectedTargetClasses = expectedSourceClasses;

		assertEquals(expectedTargetClasses, graph.getTargetClasses());

		Map<String, Set<String>> staticFields = graph.getStaticFields();
		assertTrue(staticFields.isEmpty());

	}
}
