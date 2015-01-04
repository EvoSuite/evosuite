package org.evosuite.runtime.mock;

import java.util.List;

import org.junit.Assert;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.java.lang.MockException;
import org.evosuite.runtime.mock.java.lang.MockRuntime;
import org.junit.After;
import org.junit.Test;

public class MockListTest {

	private static final boolean DEFAULT_JVM = RuntimeSettings.mockJVMNonDeterminism;
	
	@After
	public void tearDown(){
		RuntimeSettings.mockJVMNonDeterminism = DEFAULT_JVM;
	}
	
	@Test
	public void checkGetJVMMocks(){				
		
		RuntimeSettings.mockJVMNonDeterminism = false;		
		List<Class<? extends EvoSuiteMock>> list = MockList.getList();
		Assert.assertFalse(list.contains(MockRuntime.class));
		Assert.assertFalse(list.contains(MockException.class));
		
		RuntimeSettings.mockJVMNonDeterminism = true;
		list = MockList.getList();
		Assert.assertTrue(list.contains(MockRuntime.class));
		Assert.assertTrue(list.contains(MockException.class));
	}
	
	@Test
	public void testShouldBeMocked(){
		RuntimeSettings.mockJVMNonDeterminism = true;
		
		//first try with a override mock
		Assert.assertTrue(new MockException() instanceof OverrideMock);
		Assert.assertTrue(MockList.shouldBeMocked(Exception.class.getName()));
		
		//then try a static replacement one
		Assert.assertTrue(new MockRuntime() instanceof StaticReplacementMock);
		Assert.assertTrue(MockList.shouldBeMocked(java.lang.Runtime.class.getName()));
	}
}
