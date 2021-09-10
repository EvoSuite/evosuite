/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
///*
// * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
// * contributors
// *
// * This file is part of EvoSuite.
// *
// * EvoSuite is free software: you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as published
// * by the Free Software Foundation, either version 3.0 of the License, or
// * (at your option) any later version.
// *
// * EvoSuite is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * Lesser Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
// */
//package com.examples.with.different.packagename.dse.interfaces;
//
///**
// * Simple interface private method using a default method on top of it.
// *
// * @author Ignacio Lebrero
// */
//public class InterfacePrivateMethodExample {
//
//    private interface testInterface {
//        default int apply(int val) {
//            return isTwenty(val);
//        }
//
//        private int isTwenty(int val) {
//            if (val == 20) {
//                return 1;
//            } else {
//                return 2;
//            }
//        }
//    }
//
//    private static class testClass implements testInterface {}
//
//    public static int test(int val) {
//        testInterface instance = new testClass();
//        return instance.apply(val);
//    }
//
//}
