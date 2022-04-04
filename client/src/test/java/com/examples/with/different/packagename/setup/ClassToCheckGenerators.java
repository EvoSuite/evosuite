/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename.setup;

/**
 * Created by Andrea Arcuri on 13/09/15.
 */
public class ClassToCheckGenerators {


    public void bar(WithGenerator foo) {
        System.out.println("WithGenerator");
    }


    public void gi(IGeneratorForItself foo) {
        System.out.println("IGeneratorForItself");
    }

    public void xi(IX foo) {
        System.out.println("IX");
    }

    public void ga(AGeneratorForItself foo) {
        System.out.println("AGeneratorForItself");
    }

    public void xa(AX foo) {
        System.out.println("AX");
    }

    public void g(GeneratorForItself foo) {
        System.out.println("GeneratorForItself");
    }

    public void x(X foo) {
        System.out.println("X");
    }

    public void forceAnalysis(GeneratorForX gx) {
    }
}
