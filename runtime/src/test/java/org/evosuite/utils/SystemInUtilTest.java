package org.evosuite.utils;

import java.util.Scanner;

import org.junit.Assert;
import org.junit.After;
import org.junit.Test;

public class SystemInUtilTest {

	@After
	public void tearDown(){
		SystemInUtil.resetSingleton();
	}
	
	@Test
	public void testDoubleExecution(){
		
		String data = "Hello World!";
		SystemInUtil.getInstance().initForTestCase(); 
		SystemInUtil.addInputLine(data);
		Scanner scanner = new Scanner(System.in);
		String first = scanner.nextLine();
		scanner.close();
		Assert.assertEquals(data, first);
		
		//now add the same again
		SystemInUtil.addInputLine(data);
		scanner = new Scanner(System.in);
		String second = scanner.nextLine();
		scanner.close();
		Assert.assertEquals(data, second);
	}

}
