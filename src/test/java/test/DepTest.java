package test;

public class DepTest {
  @Deprecated
  public void testDeprecated() {
    System.out.println("Deprecated");
  }
  
  public void testNormal() {
    System.out.println("Normal");
  }
}