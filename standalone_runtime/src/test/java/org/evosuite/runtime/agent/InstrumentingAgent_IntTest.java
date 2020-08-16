/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;

import com.examples.with.different.packagename.agent.*;
import org.evosuite.runtime.instrumentation.InstrumentedClass;
import org.evosuite.runtime.instrumentation.MethodCallReplacementCache;
import org.evosuite.runtime.mock.java.net.EvoURLStreamHandler;
import org.evosuite.runtime.mock.java.net.URLUtil;
import org.junit.*;

import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.agent.InstrumentingAgent;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.java.io.MockFile;

/**
 * Note: this needs be run as an integration test (IT), as it requires
 * the creation of the jar file first.
 * This is automatically set up in the pom file, but the test might fail
 * if run directly from an IDE
 * 
 * @author arcuri
 *
 */
public class InstrumentingAgent_IntTest {

	private final boolean replaceCalls = RuntimeSettings.mockJVMNonDeterminism;
	private final boolean vfs = RuntimeSettings.useVFS;
    private final boolean vnet = RuntimeSettings.useVNET;
	
	@BeforeClass
	public static void initClass(){
		InstrumentingAgent.initialize();
	}
	
	@Before
	public void storeValues() {
		RuntimeSettings.mockJVMNonDeterminism = true;
		RuntimeSettings.useVFS = true;
        RuntimeSettings.useVNET = true;
        MethodCallReplacementCache.resetSingleton();
		Runtime.getInstance().resetRuntime();
	}

	@After
	public void resetValues() {
		RuntimeSettings.mockJVMNonDeterminism = replaceCalls;
		RuntimeSettings.useVFS = vfs;
        RuntimeSettings.useVNET = vnet;
	}


	@Test
	public void testTransformationInClassExtendingAbstract() throws Exception{
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			InstrumentingAgent.activate();
			//even if re-instrument, they should be fine
			InstrumentingAgent.getInstrumentation().retransformClasses(AbstractTime.class,ConcreteTime.class);
			ConcreteTime time = new ConcreteTime();
			/*
			 * Using abstract class here would fail without retransformClasses, as it would be loaded 
			 * by JUnit before any method (static, BeforeClass) of this test
			 * suite is executed, and so it would not get instrumented
			 */
			//AbstractTime time = new ConcreteTime();
			Assert.assertEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}

	@Test
	public void checkRetransformIsSupported(){
		Assert.assertTrue(InstrumentingAgent.getInstrumentation().isRetransformClassesSupported());
	}
	
	@Test
	public void testFailingTransformation() throws UnmodifiableClassException{
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);

		try{
			InstrumentingAgent.activate();			
			InstrumentingAgent.getInstrumentation().retransformClasses(SecondAbstractTime.class,SecondConcreteTime.class);
			Assert.fail(); 
		} catch(UnsupportedOperationException e){ 
			/*
			 * this is expected, as default instrumentation adds methods (eg hashCode in this case), and
			 * that is currently not permitted in Java.
			 * 
			 * Note: once we change instrumentation to do not add any method, or Java will support this kind
			 * of re-transformation, then this check should be changed
			 */
		}finally {
			InstrumentingAgent.deactivate();
		} 
		
		try{
			InstrumentingAgent.activate();			
			SecondAbstractTime time = new SecondConcreteTime();
			/*
			 * Using abstract class here fails, as it would be loaded 
			 * by JUnit before any method (static, BeforeClass) of this test
			 * suite is executed, and so it is not instrumented
			 */			
			Assert.assertNotEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}


        /*
            Note: following check does not apply any more, as we now are adding an interface to each
            instrumented class, which makes retransformation not possible any more.
            anyway, retransformation is kind of deprecated, as we do not really use it anymore (but
            left code if in the future Java ll have better support)
         */

        /*
		//to do re-instrumentation without adding new methods, we need to set it up with setRetransformingMode
		try{
			InstrumentingAgent.activate();
			InstrumentingAgent.setRetransformingMode(true);
			InstrumentingAgent.getInstumentation().retransformClasses(SecondAbstractTime.class,SecondConcreteTime.class);

			//finally it should work
			SecondAbstractTime time = new SecondConcreteTime();
			Assert.assertEquals(expected, time.getTime());

		} finally {
			InstrumentingAgent.setRetransformingMode(false);
			InstrumentingAgent.deactivate();
		} 
		*/
	}


	@Test
	public void testTime(){

		long now = System.currentTimeMillis();
		Assert.assertTrue("",TimeB.getTime() >= now);
		
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);

		try{
			InstrumentingAgent.activate();
			Assert.assertEquals(expected, TimeA.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}
	
	
	@Test
	public void testTransformationInAbstractClass(){
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			/*
			 * Note: this does not work, but we found a work around
			 * by forcing loading before JUnit test execution
			 * with a customized Runner
			 */
			InstrumentingAgent.activate();
			//com.examples.with.different.packagename.agent.AbstractTime time = new com.examples.with.different.packagename.agent.ConcreteTime();
			//Assert.assertEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}

	
	
	@Test
	public void testTransformation(){
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			InstrumentingAgent.activate();
			TimeC time = new TimeC();
			Assert.assertEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}

	@Test
	public void testTransformationInExtendingClass(){
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			InstrumentingAgent.activate();
			ExtendingTimeC time = new ExtendingTimeC();
			Assert.assertEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}


	
	@Test
	public void testInstrumentation() throws Exception{
	
		try{
			InstrumentingAgent.activate();
			
			Instrumentation inst = InstrumentingAgent.getInstrumentation();
			Assert.assertNotNull(inst);
			ClassLoader loader = this.getClass().getClassLoader();
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(TimeA.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(TimeB.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(TimeC.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(ExtendingTimeC.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(ConcreteTime.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(AbstractTime.class.getName())));
			
		} finally{
			InstrumentingAgent.deactivate();
		}
	}
	
	@Test
	public void testMockFramework_OverrideMock(){
		Object obj = null;
		try{
			InstrumentingAgent.activate();
			obj =  new GetFile();
		} finally {
			InstrumentingAgent.deactivate();
		}
		
		GetFile gf = (GetFile) obj;
		MockFramework.enable();
		Assert.assertTrue(gf.get() instanceof MockFile);
		
		//now disable
		MockFramework.disable();
		//even if GetFile is instrumented, should not return a mock now
		Assert.assertFalse(gf.get() instanceof MockFile);
	}
	
	@Test
	public void testMockFramework_StaticReplacementMock(){
		
		try{
			InstrumentingAgent.activate();
			new SumRuntime();
		} finally {
			InstrumentingAgent.deactivate();
		}
		MockFramework.enable();
		Assert.assertEquals(1101, SumRuntime.getSum());
		
		//now disable
		MockFramework.disable();
		//even if SumRuntime is instrumented, should not return a mock sum now.
		// note: it should be _extremely_ unlikely that original code returns such value by chance
        Assert.assertNotEquals(1101, SumRuntime.getSum());
	}

    @Test
    public void testMockFramework_StaticReplacementMock_ofConstructors() throws MalformedURLException {

        try{
            InstrumentingAgent.activate();
            new GetURL();
        } finally {
            InstrumentingAgent.deactivate();
        }
        //first disable
        MockFramework.disable();
        String url = "http://www.evosuite.org";
        URL res = GetURL.get(url);
        URLStreamHandler handler = URLUtil.getHandler(res);
        Assert.assertFalse(handler instanceof EvoURLStreamHandler);

        //now enable
        MockFramework.enable();
        res = GetURL.get(url);
        handler = URLUtil.getHandler(res);
        Assert.assertTrue(handler instanceof EvoURLStreamHandler);
    }

    @Test
    public void testMockFramework_StaticReplacementMock_2() throws Exception{
        try{
            InstrumentingAgent.activate();
            new GetURL();
        } finally {
            InstrumentingAgent.deactivate();
        }
        //first disable
        MockFramework.disable();
        String url = "http://www.evosuite.org";
        URL res = GetURL.getFromUri(url);
        URLStreamHandler handler = URLUtil.getHandler(res);
        Assert.assertFalse(handler instanceof EvoURLStreamHandler);

        //now enable
        MockFramework.enable();
        res = GetURL.getFromUri(url);
        handler = URLUtil.getHandler(res);
        Assert.assertTrue(handler instanceof EvoURLStreamHandler);
    }

    @Test
    public void testAddingInstrumentedClassInterface(){
        Object obj = null;
        try{
            InstrumentingAgent.activate();
            obj = new GetURL();
        } finally {
            InstrumentingAgent.deactivate();
        }

        Assert.assertTrue(obj instanceof InstrumentedClass);
    }
	
	@Test
	public void testMockFramework_noAgent(){
		/*
		 * OverrideMocks should default even if called
		 * directly. 
		 */
		MockFramework.enable();
		MockFile file = new MockFile("bar/foo");
		File parent = file.getParentFile();
		Assert.assertTrue(parent instanceof MockFile);
		
		//now, disable
		MockFramework.disable();
		parent = file.getParentFile();
		//should rollback to original behavior
		Assert.assertFalse(parent instanceof MockFile);
	}
}

