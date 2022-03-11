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
/**
 * <p>MethodCall class.</p>
 *
 * @author Gordon Fraser
 */
package org.evosuite.testcase.execution;

import java.util.ArrayList;
import java.util.List;

public class MethodCall implements Cloneable {
    public String className;
    public String methodName;
    public List<Integer> lineTrace;
    public List<Integer> branchTrace;
    public List<Double> trueDistanceTrace;
    public List<Double> falseDistanceTrace;
    public List<Integer> defuseCounterTrace;
    public int methodId;
    public int callingObjectID;
    public int callDepth;

    /**
     * <p>Constructor for MethodCall.</p>
     *
     * @param className       a {@link java.lang.String} object.
     * @param methodName      a {@link java.lang.String} object.
     * @param methodId        a int.
     * @param callingObjectID a int.
     * @param callDepth       a int.
     */
    public MethodCall(String className, String methodName, int methodId,
                      int callingObjectID, int callDepth) {
        this.className = className;
        this.methodName = methodName;
        lineTrace = new ArrayList<>();
        branchTrace = new ArrayList<>();
        trueDistanceTrace = new ArrayList<>();
        falseDistanceTrace = new ArrayList<>();
        defuseCounterTrace = new ArrayList<>();
        this.methodId = methodId;
        this.callingObjectID = callingObjectID;
        this.callDepth = callDepth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(className);
        ret.append(":");
        ret.append(methodName);
        ret.append("\n");
        // ret.append("Lines: ");
        // for(Integer line : line_trace) {
        // ret.append(" "+line);
        // }
        // ret.append("\n");
        ret.append("Branches: ");
        for (Integer branch : branchTrace) {
            ret.append(" " + branch);
        }
        ret.append("\n");
        ret.append("True Distances: ");
        for (Double distance : trueDistanceTrace) {
            ret.append(" " + distance);
        }
        ret.append("\nFalse Distances: ");
        for (Double distance : falseDistanceTrace) {
            ret.append(" " + distance);
        }
        ret.append("\n");
        return ret.toString();
    }

    /**
     * <p>explain</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String explain() {
        // TODO StringBuilder-explain() functions to construct string templates like explainList()
        StringBuffer r = new StringBuffer();
        r.append(className);
        r.append(":");
        r.append(methodName);
        r.append("\n");
        r.append("Lines: ");
        if (lineTrace == null) {
            r.append("null");
        } else {
            for (Integer line : lineTrace) {
                r.append("\t" + line);
            }
            r.append("\n");
        }
        r.append("Branches: ");
        if (branchTrace == null) {
            r.append("null");
        } else {
            for (Integer branch : branchTrace) {
                r.append("\t" + branch);
            }
            r.append("\n");
        }
        r.append("True Distances: ");
        if (trueDistanceTrace == null) {
            r.append("null");
        } else {
            for (Double distance : trueDistanceTrace) {
                r.append("\t" + distance);
            }
            r.append("\n");
        }
        r.append("False Distances: ");
        if (falseDistanceTrace == null) {
            r.append("null");
        } else {
            for (Double distance : falseDistanceTrace) {
                r.append("\t" + distance);
            }
            r.append("\n");
        }
        r.append("DefUse Trace:");
        if (defuseCounterTrace == null) {
            r.append("null");
        } else {
            for (Integer duCounter : defuseCounterTrace) {
                r.append("\t" + duCounter);
            }
            r.append("\n");
        }
        return r.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodCall clone() {
        MethodCall copy = new MethodCall(className, methodName, methodId,
                callingObjectID, callDepth);
        copy.lineTrace = new ArrayList<>(lineTrace);
        copy.branchTrace = new ArrayList<>(branchTrace);
        copy.trueDistanceTrace = new ArrayList<>(trueDistanceTrace);
        copy.falseDistanceTrace = new ArrayList<>(falseDistanceTrace);
        copy.defuseCounterTrace = new ArrayList<>(defuseCounterTrace);
        return copy;
    }
}
