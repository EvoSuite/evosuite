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
package com.examples.with.different.packagename.concolic;


public class TestCase2 {

    /**
     * @param args
     */
    // int int0 = ConcolicMarker.mark(-756,"var1");
    // int int1 = ConcolicMarker.mark(-542,"var2");
    // int int3 = ConcolicMarker.mark(1,"var3");
    // int int8 = ConcolicMarker.mark(-1480,"var4");
    // int int11 = ConcolicMarker.mark(-1637,"var5");
    public static void test(int int0, int int1, int int3, int int8, int int11) {
        MyLinkedList linkedList0 = new MyLinkedList();
        Object object0 = linkedList0.get(int0);
        MyLinkedList linkedList1 = new MyLinkedList();
        Object object1 = linkedList1.get(int1);
        int int2 = linkedList1.size();
        linkedList1.add(int2);
        int int4 = linkedList1.size();
        linkedList1.unreacheable();
        MyLinkedList linkedList2 = new MyLinkedList();
        linkedList1.unreacheable();
        int int5 = linkedList2.size();
        int int6 = linkedList1.size();
        linkedList1.add(linkedList2);
        linkedList2.unreacheable();
        int int7 = linkedList2.size();
        MyLinkedList linkedList3 = (MyLinkedList) linkedList1.get(int5);
        linkedList2.unreacheable();
        int int9 = linkedList1.size();
        int int10 = linkedList1.size();
        MyLinkedList linkedList4 = (MyLinkedList) linkedList1.get(int2);
        int int12 = linkedList1.size();
        linkedList1.add(int9);
        linkedList1.unreacheable();
        MyLinkedList linkedList5 = new MyLinkedList();
    }

}
