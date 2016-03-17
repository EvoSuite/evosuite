package com.examples.with.different.packagename.exception;

import java.sql.SQLException;

/**
 * Created by gordon on 17/03/2016.
 */
public class Rethrow2Exceptions {


    // # branches == 0
    // # branchless methods == 2 -> 1 (<init>, foo)
    // # additional branches: 6 (IllegalArgumentException true/false, SQLException true/false, RuntimeException true/false)
    public boolean foo(int x) throws IllegalArgumentException, SQLException {
        return MethodsWithExceptions.twoExceptions(x);
    }

//    public boolean foo_instrumented(int x) throws IllegalArgumentException, SQLException {
//        Throwable ok = null;
//        try {
//            MethodsWithExceptions.twoExceptions(x);
//        } catch(Throwable t) {
//            ok = t;
//        }
//        if(ok instanceof IllegalArgumentException)
//            throw (IllegalArgumentException)ok;
//        else if(ok instanceof SQLException)
//            throw (SQLException)ok;
//        else if(ok instanceof RuntimeException)
//            throw (RuntimeException)ok;
//
//        return true;
//    }
}
