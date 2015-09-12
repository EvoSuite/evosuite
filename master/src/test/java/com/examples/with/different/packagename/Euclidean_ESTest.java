package com.examples.with.different.packagename;

import static org.junit.Assert.assertEquals;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(useVNET = true) 
public class Euclidean_ESTest extends Euclidean_ESTest_scaffolding {

  @Test
  public void test0()  throws Throwable  {
      Euclidean euclidean0 = new Euclidean();
      int int0 = euclidean0.gcd(0, 0);
      assertEquals(0, int0);
  }
}
