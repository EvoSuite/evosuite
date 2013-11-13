package org.evosuite.continuous.project;

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
		
		ProjectAnalyzer pa = new ProjectAnalyzer(cuts);
		
		ProjectStaticData data = pa.analyze();

		Assert.assertEquals(2, data.getTotalNumberOfClasses());
		
		ClassInfo simple = data.getClassInfo(Simple.class.getName());
		Assert.assertNotNull(simple);
		Assert.assertEquals(2, simple.numberOfBranches);
		
		ClassInfo trivial = data.getClassInfo(Trivial.class.getName()); 
		Assert.assertNotNull(trivial);
		Assert.assertEquals(1, trivial.numberOfBranches);		
	}
}
