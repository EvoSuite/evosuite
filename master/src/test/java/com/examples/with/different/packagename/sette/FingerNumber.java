/*
 * SETTE - Symbolic Execution based Test Tool Evaluator
 *
 * SETTE is a tool to help the evaluation and comparison of symbolic execution based test input 
 * generator tools.
 *
 * Budapest University of Technology and Economics (BME)
 *
 * Authors: Lajos Cseppentő <lajos.cseppento@inf.mit.bme.hu>, Zoltán Micskei <micskeiz@mit.bme.hu>
 *
 * Copyright 2014-2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the 
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.examples.with.different.packagename.sette;


/**
 * Simple class implementing the Number interface. This class can only represent between 1 to 10.
 */
public final class FingerNumber extends Number {
    private static final long serialVersionUID = 4280286901518300224L;
    private final int value;

    public FingerNumber(int v) {
        if (0 > v || v > 10) {
            throw new IllegalArgumentException();
        }
        value = v;
    }

    public FingerNumber add(FingerNumber o) {
        int r = value + o.value;
        if (r > 10) {
            throw new RuntimeException("Out of range");
        }
        return new FingerNumber(r);
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }
}
