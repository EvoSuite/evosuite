package com.examples.with.different.packagename.fm;

/**
 * Created by foo on 08/12/15.
 */
public class SimpleFM_GenericsNullString {

    public interface Foo<T>{
        T get(Object obj);
    }

    public void bar(Foo<String> foo){
        String a = foo.get(new Object());
        if(a!=null){

            String b = foo.get(new Object());
            if(b==null){
                System.out.println("target");
            }
        }
    }
}
