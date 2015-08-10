package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 10/08/15.
 */
public class SimpleFM_Dependency {

    public static interface Bar{
        String getBar();
    }

    public static class Foo{

        private Bar bar;

        public Foo(Bar bar){
            this.bar = bar;
        }

        public boolean foo(){
            return bar.getBar().equals("bar");
        }
    }

    public static boolean bar(Foo foo){
        if(foo.foo()){
            return true;
        } else {
            return false;
        }
    }
}
