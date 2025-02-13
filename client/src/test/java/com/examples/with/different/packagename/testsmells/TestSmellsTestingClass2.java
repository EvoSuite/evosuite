package com.examples.with.different.packagename.testsmells;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class TestSmellsTestingClass2 {

    private HashMap<String, File> sharedFiles;

    public TestSmellsTestingClass2(){
        sharedFiles  = new HashMap<>();
    }

    public boolean addFile (File file){
        if(!this.sharedFiles.containsKey(file.getName())){
            this.sharedFiles.put(file.getName(), file);
            return true;
        }
        return false;
    }

    public boolean removeFile (String fileName){
        if(this.sharedFiles.containsKey(fileName)){
            this.sharedFiles.remove(fileName);
            return true;
        }
        return false;
    }

    public boolean editFile (String name, String content){
        try {
            FileWriter myWriter = new FileWriter(name);
            myWriter.write(content);
            myWriter.close();
            return true;

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;
    }
}
