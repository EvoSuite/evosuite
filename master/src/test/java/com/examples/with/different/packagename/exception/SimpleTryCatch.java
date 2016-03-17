package com.examples.with.different.packagename.exception;

import java.io.FileNotFoundException;

/**
 * Created by gordon on 17/03/2016.
 */
public class SimpleTryCatch {

    // # branches == 0
    // # branchless methods == 2 (<init>, foo)
    // # additional branches: 4 (FileNotFoundException true/false, RuntimeException true/false)
    public boolean foo(int x) {

        try {
            MethodsWithExceptions.oneException(x);
        } catch(FileNotFoundException e) {
            return false;
        }

        return true;
    }

//    public boolean foo_instrumented(int x) {
//        try {
//            Throwable ok = null;
//            try {
//                MethodsWithExceptions.oneException(x);
//            } catch(Throwable t) {
//                ok = t;
//            }
//            if(ok instanceof FileNotFoundException)
//                throw (FileNotFoundException)ok;
//            else if(ok instanceof RuntimeException)
//                throw (RuntimeException)ok;
//
//
//        } catch(FileNotFoundException e) {
//            return false;
//        }
//
//        return true;
//    }
}
