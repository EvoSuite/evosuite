package org.evosuite.assertion;

import org.evosuite.SystemTest;
import org.evosuite.TestGenerationContext;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ExampleEnum;

public class TestAssertionClassLoader extends SystemTest {

	@Test
	public void testLoaderOfEnumsAreChanged() throws NoSuchMethodException, SecurityException {
		InspectorAssertion assertion = new InspectorAssertion();
		assertion.inspector = new Inspector(ExampleEnum.class, ExampleEnum.class.getMethod("testMe", new Class<?>[] {}));
		assertion.value = ExampleEnum.VALUE1;
		Assert.assertEquals(ExampleEnum.VALUE1, assertion.value);
		
		ClassLoader loader = TestGenerationContext.getInstance().getClassLoaderForSUT();
		assertion.changeClassLoader(loader);
		
		Assert.assertNotEquals(ExampleEnum.VALUE1, assertion.value);
	}
}
