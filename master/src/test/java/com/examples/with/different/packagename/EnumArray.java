package com.examples.with.different.packagename;

public class EnumArray {

    public boolean doSomething(Level[] arr) throws RuntimeException{
        if(Level.HIGH.equals(arr[15])){
            return true;
        }else{
            return false;
        }
    }

}
/**
 * ref : http://tutorials.jenkov.com/java/enums.html
 *
 *
 * */

enum Level {
    HIGH  (3),  //calls constructor with value 3
    MEDIUM(2),  //calls constructor with value 2
    LOW   (1)   //calls constructor with value 1
    ; // semicolon needed when fields / methods follow


    private final int levelCode;

    Level(int levelCode) {
        this.levelCode = levelCode;
    }

    public int getLevelCode() {
        return this.levelCode;
    }

}

/*class Simple {
    boolean checkGreater(int x, int y) {
        if (x > y)
            return true;
        else
            return false;
    }
}*/
