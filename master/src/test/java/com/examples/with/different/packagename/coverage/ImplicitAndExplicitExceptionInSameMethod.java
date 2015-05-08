package com.examples.with.different.packagename.coverage;

/**
 * Created by Andrea Arcuri on 08/05/15.
 */
public class ImplicitAndExplicitExceptionInSameMethod {

    public void undeclared(Integer x, Integer y){
        if(x==null){
            throw new NullPointerException();
        }
        y.toString();
    }

    public void declared(Integer x, Integer y) throws NullPointerException{
        if(x==null){
            throw new NullPointerException();
        }
        y.toString();
    }

}
