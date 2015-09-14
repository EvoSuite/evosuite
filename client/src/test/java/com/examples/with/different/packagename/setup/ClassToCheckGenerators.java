package com.examples.with.different.packagename.setup;

/**
 * Created by Andrea Arcuri on 13/09/15.
 */
public class ClassToCheckGenerators {


    public void bar(WithGenerator foo){
        System.out.println("WithGenerator");
    }


    public void gi(IGeneratorForItself foo){
        System.out.println("IGeneratorForItself");
    }

    public void xi(IX foo){
        System.out.println("IX");
    }

    public void ga(AGeneratorForItself foo){
        System.out.println("AGeneratorForItself");
    }

    public void xa(AX foo){
        System.out.println("AX");
    }

    public void g(GeneratorForItself foo){
        System.out.println("GeneratorForItself");
    }

    public void x(X foo){
        System.out.println("X");
    }

    public void forceAnalysis(GeneratorForX gx){}
}
