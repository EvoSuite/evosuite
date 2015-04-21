package com.examples.with.different.packagename;

import org.apache.commons.cli.AlreadySelectedException;

/**
 * Created by Andrea Arcuri on 21/04/15.
 */
public class DependencyLibrary {

    public void foo(){
        //here, at compile time we use the one in EvoSuite dependency, but not at runtime
        AlreadySelectedException e = new AlreadySelectedException(null);
        if(e.toString().equals("foo")){
            System.out.println("Only executed if SUT version is used, and not the one in EvoSuite's dependencies");
        }
    }
}

/*
package org.apache.commons.cli;

public class AlreadySelectedException {

    public AlreadySelectedException(String msg){
    }

    @Override
    public String toString(){
        //this cannot be returned by the original class in commons-cli
        return "foo";
    }
}


 */
