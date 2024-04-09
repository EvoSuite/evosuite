package com.examples.with.different.packagename.jdk17;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class SmallEx {
    public boolean makeTryCatch() {
        try (
                BufferedReader reader = new BufferedReader(new StringReader("Hello, Java 9!"));
                Writer writer = new StringWriter();
        ) {
            String line = reader.readLine();
            writer.write(line);
            System.out.println("Read from resource: " + line);
            System.out.println("Write to resource: " + writer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
    public boolean setDiamondOperator(){
        List<String> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        Map<Integer, String> map = new HashMap<>();

        return true;
    }
    public boolean setInterClassDiamondOperator(){
        Addition<Integer> integerAddition = new Addition<>() {
            @Override
            void add(Integer t1, Integer t2) {
                System.out.println(t1+t2);
            }
        };

        return true;
    }
    public boolean getConnection() throws Exception{
        class MyClass implements MyInterface {
            @Override
            public void instanceMethod() {
                System.out.println("Implementation of instanceMethod");
            }
        }

        return true;
    }

}

abstract class Addition<T> {
    abstract void add(Integer t1, Integer t2);
}

interface MyInterface {
    void instanceMethod();

    private static String staticMethod() {
        return "static private";
    }

    default void defaultMethod() {
        String result = staticMethod();
    }
}
