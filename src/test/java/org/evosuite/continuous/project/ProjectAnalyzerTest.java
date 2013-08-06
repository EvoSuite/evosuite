package org.evosuite.continuous.project;

import java.util.Collection;

import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.continuous.Simple;
import com.examples.with.different.packagename.continuous.Trivial;

public class ProjectAnalyzerTest {

	@Test
	public void testBranches(){
		
		String[] cuts = new String[]{
				Simple.class.getName(),
				Trivial.class.getName()
		};
		
		ProjectAnalyzer pa = null;//TODO
		
		ProjectStaticData data = pa.analyze();

		Assert.assertEquals(2, data.getTotalNumberOfClasses());
		
		ClassInfo simple = null ; //TODO
		Assert.assertNotNull(simple);
		Assert.assertEquals(2, simple.numberOfBranches);
		
		ClassInfo trivial = null ; //TODO
		Assert.assertNotNull(trivial);
		Assert.assertEquals(2, trivial.numberOfBranches);		
	}
}
