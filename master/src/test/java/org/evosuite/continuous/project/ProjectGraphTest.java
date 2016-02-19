/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.continuous.project;

import java.io.Serializable;
import java.util.Set;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProjectGraphTest {

	@BeforeClass
	public static void initClass() {
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
	}

	@Test
	public void testGetAllCUTsParents() {

		ProjectStaticData data = new ProjectStaticData();
		data.addNewClass(new ClassInfo(A.class, 1, true));
		data.addNewClass(new ClassInfo(B.class, 1, true));
		data.addNewClass(new ClassInfo(C.class, 0, false));
		data.addNewClass(new ClassInfo(D.class, 0, false));
		data.addNewClass(new ClassInfo(E.class, 1, true));
		data.addNewClass(new ClassInfo(F.class, 0, false));
		data.addNewClass(new ClassInfo(G.class, 1, true));

		ProjectGraph graph = data.getProjectGraph();

		Set<String> forA = graph.getAllCUTsParents(A.class.getName());
		Set<String> forB = graph.getAllCUTsParents(B.class.getName());
		Set<String> forC = graph.getAllCUTsParents(C.class.getName());
		Set<String> forD = graph.getAllCUTsParents(D.class.getName());
		Set<String> forE = graph.getAllCUTsParents(E.class.getName());
		Set<String> forF = graph.getAllCUTsParents(F.class.getName());
		Set<String> forG = graph.getAllCUTsParents(G.class.getName());

		Assert.assertEquals(0, forA.size());
		Assert.assertEquals(1, forB.size());
		Assert.assertEquals(0, forC.size());
		Assert.assertEquals(0, forD.size());
		Assert.assertEquals(0, forE.size());
		Assert.assertEquals(0, forF.size());
		Assert.assertEquals(1, forG.size());
	}

	@Test
	public void testClassTypes() {
		ProjectStaticData data = new ProjectStaticData();
		data.addNewClass(new ClassInfo(A.class, 1, true));
		data.addNewClass(new ClassInfo(B.class, 1, true));
		data.addNewClass(new ClassInfo(C.class, 0, false));
		data.addNewClass(new ClassInfo(D.class, 0, false));
		data.addNewClass(new ClassInfo(E.class, 1, true));
		data.addNewClass(new ClassInfo(F.class, 0, false));
		data.addNewClass(new ClassInfo(G.class, 1, true));

		ProjectGraph graph = data.getProjectGraph();

		Assert.assertTrue(graph.isConcrete(A.class.getName()));
		Assert.assertTrue(graph.isConcrete(B.class.getName()));
		Assert.assertTrue(graph.isInterface(C.class.getName()));
		Assert.assertTrue(graph.isInterface(D.class.getName()));
		Assert.assertTrue(graph.isAbstract(E.class.getName()));
		Assert.assertTrue(graph.isInterface(F.class.getName()));
		Assert.assertTrue(graph.isAbstract(G.class.getName()));
	}

	class A {
		void foo() {
		}
	}

	class B extends A {
		@Override
		void foo() {
		}
	}

	interface C {
	}

	interface D extends Serializable {
	}

	abstract class E implements Comparable {
		void foo() {
		}
	}

	interface F extends C, D {
	}

	abstract class G extends E implements F {
		@Override
		void foo() {
		}
	}
}
