package com.examples.with.different.packagename.exception;

import com.thoughtworks.xstream.mapper.Mapper;

import java.sql.SQLException;

/**
 * Created by gordon on 17/03/2016.
 */
public class Rethrow2ExceptionsAndUncheckedException {


    // # branches == 0
    // # branchless methods == 2 (<init>, foo)
    // # additional branches for unchecked exceptions: 2 (NPE)
    // # additional branches for checked exceptions: 6 (IllegalArgumentException true/false, SQLException true/false, RuntimeException true/false)
    public boolean foo(MethodsWithExceptions foo, int x) throws IllegalArgumentException, SQLException {
        return foo.nonStaticTwoExceptions(x);
    }

//    public boolean foo_instrumented(MethodsWithExceptions foo, int x) throws IllegalArgumentException, SQLException {
//        Throwable ok = null;
//        try {
//            if(foo == null)
//                throw new NullPointerException();
//            foo.nonStaticTwoExceptions(x);
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
