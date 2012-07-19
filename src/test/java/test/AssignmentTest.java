package test;

public class AssignmentTest {

  protected class Foo {
    public int x = 0;
  }

  public Foo foo = new Foo();
  
  public void foo(AssignmentTest other) {
    if(other.foo.x != 0 && foo.x != 0) {
      if(other.foo.x == 3 * (foo.x + 1) + 17) {
        // Target
      }
    }
  }

  public void bar(AssignmentTest other) {
    if(other.foo.x != 0 && foo.x != 0 && other != this) {
      if(other.foo.x * 2 == 4 * (foo.x + 2) + 20) {
        // Target
      }
    }
  }

  public void zoo(AssignmentTest other, int[] y) {
    if(other.foo.x != 0 && foo.x != 0 && y.length == 2 && other != this) {
      if(y[0] == foo.x && y[1] == other.foo.x && y[0] != y[1]) {
        // Target
      }
    }
  }
}
