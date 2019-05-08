package com.examples.with.different.packagename;

public class MatchingChars {

    public boolean doSomething(String target){
        if(target.equals(LOGINVALIDATOR.PASSWORD.getEncodedValue())){
            return true;
        }else{
            return false;
        }
    }
}

enum LOGINVALIDATOR {
    PASSWORD("$~?"),  //calls constructor with value 3
    ; // semicolon needed when fields / methods follow


    private final String encodedValue;

    LOGINVALIDATOR(String encodedValue) {
        this.encodedValue = encodedValue;
    }

    public String getEncodedValue() {
        return this.encodedValue;
    }

}
