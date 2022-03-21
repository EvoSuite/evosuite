package com.examples.with.different.packagename.testsmells;

import java.util.Objects;
import java.util.UUID;

public class TestSmellsTestingClass1 {

    private String name;
    private int number;
    private final String randomID;
    private String something;

    public TestSmellsTestingClass1 (String name){
        this.name = name;
        this.randomID = UUID.randomUUID().toString();
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

    /**
     * Set the number
     * @param number The new number
     */
    public void setNumber(int number){
        this.number = number;
    }

    /**
     * Get the number
     * @return int with the current number
     */
    public int getNumber(){
        return number;
    }

    /**
     * Verify if the number is positive
     * @return true if the number is positive
     */
    public boolean isPositive (){
        return number > 0;
    }

    /**
     * A getter without a setter
     * @return String with a random value
     */
    public String getRandomID (){
        return randomID;
    }

    /**
     * A setter without a getter
     * @param something A random String that is never used
     */
    public void setSomething (String something){
        this.something = something;
    }

    /**
     * It is just an empty method
     */
    public void pointlessMethod (){

    }

    /**
     * Returns null
     * @return String is null
     */
    public String returnNull (){
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestSmellsTestingClass1 that = (TestSmellsTestingClass1) o;
        return Objects.equals(randomID, that.randomID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(randomID);
    }
}
