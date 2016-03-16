package com.examples.with.different.packagename.papers.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Scanner;

public class EnvExample {

    public boolean checkContent() throws Exception {

        Scanner console = new Scanner(System.in);
        String fileName = console.nextLine();
        console.close();

        File file = new File(fileName);
        if (!file.exists())
            return false;

        Scanner fromFile = new Scanner(new FileInputStream(file));
        String fileContent = fromFile.nextLine();
        fromFile.close();

        String date = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date());
        if (fileContent.equals(date))
            return true;

        return false;
    }
}
