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
package org.evosuite.setup;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.setup.PutStaticMethodCollector.MethodIdentifier;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.examples.with.different.packagename.staticusage.FooBar1;
import com.examples.with.different.packagename.staticusage.FooBar2;

public class TestPutStaticCollector {

	@BeforeClass
	public static void init(){
		String cp = System.getProperty("user.dir") + "/target/test-classes";
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
	}

	@Test
	public void testFooBar1() {
		String className = FooBar1.class.getName();
		PutStaticMethodCollector collector = new PutStaticMethodCollector(className);

		MethodIdentifier expected_method_id = new MethodIdentifier(
				FooBar2.class.getName(), "init_used_int_field",
				Type.getMethodDescriptor(Type.VOID_TYPE));
		Set<MethodIdentifier> expected_methods = Collections
				.singleton(expected_method_id);

		Set<MethodIdentifier> methods = collector.collectMethods();
		assertEquals(expected_methods, methods);
	}
}
