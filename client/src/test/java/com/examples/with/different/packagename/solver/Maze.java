/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename.solver;

import java.util.Arrays;

public class Maze {

    // Maze hard-coded dimensions
    private static final int H_SIZE = 7;
    private static final int W_SIZE = 11;


    private static void draw(char[][] maze) {
        int k, j;
        for (k = 0; k < H_SIZE; k++) {
            for (j = 0; j < W_SIZE; j++) {
                System.out.print(maze[k][j]);
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }

    public static int walk(char[] moves) {
        char[][] maze = new char[][]{
                // row 0
                "+-+---+---+".toCharArray(),
                // row 1
                "| |     |#|".toCharArray(),
                // row 2
                "| | --+ | |".toCharArray(),
                // row 3
                "| |   | | |".toCharArray(),
                // row 4
                "| +-- | | |".toCharArray(),
                // row 5
                "|     |   |".toCharArray(),
                // row 6
                "+-----+---+".toCharArray()};


        int x, y; // Player position
        int ox, oy; // Old player position
        int i = 0; // Iteration number

        // Initial position
        x = 1;
        y = 1;
        maze[y][y] = 'X';

        // Print some info
        System.out.print("Maze dimensions: " + W_SIZE + "," + H_SIZE + "\n");
        System.out.print("Player pos: " + x + "x" + y + "\n");
        System.out.print("Iteration no. " + i + "\n");
        System.out.print("The player moves with 'w', 's', 'a' and 'd'\n");
        System.out.print("Try to reach the price(#)!\n");

        // Draw the maze
        draw(maze);

        // Iterate and run 'program'
        while (i < moves.length) {
            ox = x;
            oy = y;
            switch (moves[i]) {
                case 'w':
                    y--;
                    break;
                case 's':
                    y++;
                    break;
                case 'a':
                    x--;
                    break;
                case 'd':
                    x++;
                    break;
                default: {
                    System.out.println("Invalid move " + moves[i]);
                    return 0;
                }
            }

            if (maze[y][x] == '#') {
                System.out.print("You win!\n");
                System.out.print("Your solution \n" + Arrays.toString(moves));
                return 1;
            }

            if (maze[y][x] != ' ' && !((y == 2 && maze[y][x] == '|' && x > 0 && x < W_SIZE))) {
                x = ox;
                y = oy;
            }

            if (ox == x && oy == y) {
                System.out.print("You lose\n");
                return 2;
            }

            maze[y][x] = 'X';
            draw(maze); // draw it

            i++;
        }

        System.out.print("Treasure not found.\n");
        return 3;
    }

}
