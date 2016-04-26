package com.examples.with.different.packagename.exception;

import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * Created by gordon on 17/03/2016.
 */
public class MethodsWithExceptions {


    public static boolean oneException(int x) throws FileNotFoundException {
        if(x == 5) {
            throw new FileNotFoundException();
        }

        return true;
    }

    public static boolean twoExceptions(int x) throws IllegalArgumentException, SQLException {
        if(x == 10)
            throw new IllegalArgumentException();
        else if(x == 42)
            throw new SQLException();
        else
            return true;
    }

    public boolean nonStaticTwoExceptions(int x) throws IllegalArgumentException, SQLException {
        if(x == 10)
            throw new IllegalArgumentException();
        else if(x == 42)
            throw new SQLException();
        else
            return true;
    }
}
