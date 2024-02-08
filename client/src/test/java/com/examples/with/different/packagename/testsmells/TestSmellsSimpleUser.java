package com.examples.with.different.packagename.testsmells;

public class TestSmellsSimpleUser {

    private String name;

    public TestSmellsSimpleUser (String name){
        this.name = name;
    }

    /**
     * Change the current name
     * @param name The new name
     */
    public void setName (String name){
        this.name = name;
    }

    /**
     * Get the current name
     * @return String with the current name
     */
    public String getName (){
        return name;
    }

    @Override
    public String toString() {
        return "TestSmellsSimpleUser{" +
                "name='" + name + '\'' +
                '}';
    }
}
