package org.evosuite.setup;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.evosuite.setup.PutStaticMethodCollector.MethodIdentifier;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.examples.with.different.packagename.staticusage.FooBar1;
import com.examples.with.different.packagename.staticusage.FooBar2;

public class TestPutStaticCollector {

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
