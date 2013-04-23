package org.evosuite.instrumentation;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.evosuite.Properties;
import org.evosuite.symbolic.search.RegexDistance;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.TrivialForDynamicSeedingRegex;

public class TestRegexInstrumentation {

	@Test
	public void testTrivialForDynamicSeedingRegex() throws Throwable{
		//first check if it works with Java directly
		String matching = "-@0.AA";
		Assert.assertTrue(TrivialForDynamicSeedingRegex.foo(matching));
		
		//now check the distance being 0
		Assert.assertEquals(0.0, org.evosuite.symbolic.search.RegexDistance.getDistance(matching, TrivialForDynamicSeedingRegex.REGEX), 0.0);
		//FIXME: is it safe to remove this distance algorithm? (ie replace with one above)
		// do we really need 2 implementations of RegexDistance?
		Assert.assertEquals(0.0, org.evosuite.instrumentation.RegexDistance.getDistance(matching, TrivialForDynamicSeedingRegex.REGEX), 0.0);
		
		//now check that what done in the instrumentation return a positive value
		int comp = BooleanHelper.StringMatches(matching, TrivialForDynamicSeedingRegex.REGEX);
		Assert.assertTrue(""+comp,comp>0);
		
		//actually load the class, and see if it works
		String targetClass = TrivialForDynamicSeedingRegex.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		InstrumentingClassLoader loader = new InstrumentingClassLoader();
		Class<?> sut = loader.loadClass(targetClass);
		Method m = sut.getMethod("foo", String.class);
		Boolean b = (Boolean) m.invoke(null, matching);
		Assert.assertTrue(b);
	}
}
