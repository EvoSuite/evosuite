package com.examples.with.different.packagename;

public class DataUtils {
    public void verify2DArray(int[][] data) {
        for (int r = 0; r < data.length; r++) {
            for (int c = 0; c < data[r].length; c++) {
                int i = data[r][c];
                System.out.print(i);
            }
        }
    }
}


