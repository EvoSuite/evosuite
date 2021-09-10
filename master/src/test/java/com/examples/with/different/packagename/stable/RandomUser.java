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
package com.examples.with.different.packagename.stable;

import java.util.Random;

public class RandomUser {

    private final Random random;

    public RandomUser() {
        random = new Random();
    }

    public String toString() {
        return random.toString();
    }

    public int nextInt() {
        return random.nextInt();
    }

    public long nextLong() {
        return random.nextLong();
    }

    public int nextInt(int n) {
        return random.nextInt(n);
    }

    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    public double nextDouble() {
        return random.nextDouble();
    }

    public void nextBytes(byte[] bytes) {
        random.nextBytes(bytes);
    }

    public float nextFloat() {
        return random.nextFloat();
    }

    public double nextGaussian() {
        return random.nextGaussian();
    }

}
