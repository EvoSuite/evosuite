package com.examples.with.different.packagename.fm;

/**
 * Created by foo on 08/12/15.
 */
public class SimpleFM_NullString {

    public interface Foo{
        String get(Object obj);
    }

    public void bar(Foo foo){
        String a = foo.get(new Object());
        if(a!=null){

            String b = foo.get(new Object());
            if(b==null){
                System.out.println("target");
            }
        }
    }
}
