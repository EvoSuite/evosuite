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
        } catch (FileNotFoundException e) {
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
