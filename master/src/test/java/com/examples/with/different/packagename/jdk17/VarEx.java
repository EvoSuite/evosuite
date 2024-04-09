package com.examples.with.different.packagename.jdk17;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class VarEx {
    public boolean setPrimitive() {
        var strData = "Jayden";
        var doubleData = 1.5;
        var intData = 100;

        return true;
    }

    public List getCollection(){
        var strData = "Jayden";
        var doubleData = 1.5;
        var intData = 100;

        var datas = List.of(strData, doubleData, intData);

        return datas;
    }

    public boolean makeLoo(){
        var datas = getCollection();

        for (var data : datas) {
            System.out.println(data);
        }

        return true;
    }


    public boolean getConnection() throws Exception{
        var url = new URL("http://www.oracle.com/");
        var conn = url.openConnection();
        var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        if(reader!=null){
            reader.close();
        }

        return true;
    }

}
