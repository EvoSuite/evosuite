package org.maven_test_project.em;

import java.io.File;

public class FileCheck {

    public static boolean check(){

        File file = new File("thisFileShouldBeMockedInVFS.txt");
        if(file.exists()){
            return true;
        } else {
            return false;
        }
    }
}
