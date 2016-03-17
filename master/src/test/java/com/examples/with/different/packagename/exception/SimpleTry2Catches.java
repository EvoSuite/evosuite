package com.examples.with.different.packagename.exception;

import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * Created by gordon on 17/03/2016.
 */
public class SimpleTry2Catches {

    // # branches == 0
    // # branchless methods == 2 (<init>, foo)
    // # additional branches: 6 (IllegalArgumentException true/false, SQLException true/false, RuntimeException true/false)
    public boolean foo(int x) {

        try {
            MethodsWithExceptions.twoExceptions(x);
        } catch(IllegalArgumentException e) {
            return false;
        } catch(SQLException e) {
            return false;
        }

        return true;
    }

//    public boolean foo_instrumented(int x) {
//        try {
//            Throwable ok = null;
//            try {
//                MethodsWithExceptions.twoExceptions(x);
//            } catch(Throwable t) {
//                ok = t;
//            }
//            if(ok instanceof IllegalArgumentException)
//                throw (IllegalArgumentException)ok;
//            else if(ok instanceof SQLException)
//                throw (SQLException)ok;
//            else if(ok instanceof RuntimeException)
//                throw (RuntimeException)ok;
//
//
//        } catch(IllegalArgumentException e) {
//            return false;
//        } catch(SQLException e) {
//            return false;
//        }
//
//        return true;
//    }
}
