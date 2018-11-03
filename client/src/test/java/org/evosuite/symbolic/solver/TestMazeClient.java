package org.evosuite.symbolic.solver;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.examples.with.different.packagename.solver.Maze;
import com.examples.with.different.packagename.solver.MazeClient;

public class TestMazeClient {

  @Test
  public void testMazeSolution() {

    int ret_val = MazeClient.walk('s', 's', 's', 's', 'd', 'd', 'd', 'd', 'w', 'w', 'a', 'a', 'w',
        'w', 'd', 'd', 'd', 'd', 's', 's', 's', 's', 'd', 'd', 'w', 'w', 'w', 'w', (char) 0,
        (char) 0, 28);
    assertEquals(1, ret_val);
  }


}
