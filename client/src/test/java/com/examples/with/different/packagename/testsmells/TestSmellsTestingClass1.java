package com.examples.with.different.packagename.testsmells;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
     * Division operation
     * @param x Divisor
     * @return int with the result of the operation
     */
    public int divideNumber (int x){
        return number / x;
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

    @Override
    public String toString() {
        return "TestSmellsTestingClass1{" +
                "name='" + name + '\'' +
                ", number=" + number +
                ", something='" + something + '\'' +
                '}';
    }

    public static int[] changeAndReturnArray (int[] listOfIntegers){

        int size = listOfIntegers.length;
        int[] res = new int[size];

        if(size > 3){
            for (int i = 0; i < size; i++){
                listOfIntegers[i] = listOfIntegers[i] + 2;
                res[i] = listOfIntegers[i];
            }
        } else {
            for (int i = 0; i < size; i++){
                listOfIntegers[i] = i;
                res[i] = i;
            }
        }

        return res;
    }

    public static void wasteTime () throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
    }
}
