package org.evosuite.continuous.project;

import java.io.Serializable;
import java.util.Set;

import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;
import org.junit.Assert;
import org.junit.Test;

public class ProjectGraphTest {

	@Test
	public void testGetAllCUTsParents(){

		ProjectStaticData data = new ProjectStaticData();
		data.addNewClass(new ClassInfo(A.class,1,true));
		data.addNewClass(new ClassInfo(B.class,1,true));
		data.addNewClass(new ClassInfo(C.class,0,false));
		data.addNewClass(new ClassInfo(D.class,0,false));
		data.addNewClass(new ClassInfo(E.class,1,true));
		data.addNewClass(new ClassInfo(F.class,0,false));
		data.addNewClass(new ClassInfo(G.class,1,true));

		ProjectGraph graph = data.getProjectGraph();

		Set<String> forA = graph.getAllCUTsParents(A.class.getName());
		Set<String> forB = graph.getAllCUTsParents(B.class.getName());
		Set<String> forC = graph.getAllCUTsParents(C.class.getName());
		Set<String> forD = graph.getAllCUTsParents(D.class.getName());
		Set<String> forE = graph.getAllCUTsParents(E.class.getName());
		Set<String> forF = graph.getAllCUTsParents(F.class.getName());
		Set<String> forG = graph.getAllCUTsParents(G.class.getName());

		Assert.assertEquals(0,forA.size());
		Assert.assertEquals(1,forB.size());
		Assert.assertEquals(0,forC.size());
		Assert.assertEquals(0,forD.size());
		Assert.assertEquals(0,forE.size());
		Assert.assertEquals(0,forF.size());
		Assert.assertEquals(1,forG.size());
	}


	private class A{ void foo(){}}

	private class B extends A{ void foo(){}}

	private interface C{}

	private interface D extends Serializable{}

	private abstract class E implements Comparable{ void foo(){}}

	private interface F extends C, D{}

	private abstract class G extends E implements F{ void foo(){}} 
}
