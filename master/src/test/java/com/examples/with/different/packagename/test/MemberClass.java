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
package com.examples.with.different.packagename.test;

public class MemberClass {
    public static class Member1 {
        private int x = 0;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void rubbish() {
        }
    }

    public class Member2 {
        public int x = 0;
    }

    /*
      public Member2 getMember(int x) {
        Member2 mem = new Member2();
        mem.x = x;
        return mem;
      }
     */
    public void target(Member1 member1, Member2 member2) {
        if (member1.getX() == member2.x && member1.getX() > 0)
            System.out.println("Hooray");
    }
}