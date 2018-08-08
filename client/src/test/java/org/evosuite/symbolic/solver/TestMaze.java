package org.evosuite.symbolic.solver;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.examples.with.different.packagename.solver.Maze;

public class TestMaze {

  @Test
  public void testMazeSolution() {
    char[] solution = "ssssddddwwaawwddddssssddwwww".toCharArray();
    int ret_val = Maze.walk(solution);
    assertEquals(1, ret_val);
  }
  
  @Test
  public void testIncompleteSolution() {
    char[] solution = "ssssddddwwaawwddddssssdd".toCharArray();
    int ret_val = Maze.walk(solution);
    assertEquals(3, ret_val);
  }
  
  @Test
  public void testCrashAgainstWall() {
    char[] solution = "sssss".toCharArray();
    int ret_val = Maze.walk(solution);
    assertEquals(2, ret_val);
  }
  
  @Test
  public void testInvalidMove() {
    char[] solution = "ssssddddwwaawwddddssssdX".toCharArray();
    int ret_val = Maze.walk(solution);
    assertEquals(0, ret_val);
  }

}
