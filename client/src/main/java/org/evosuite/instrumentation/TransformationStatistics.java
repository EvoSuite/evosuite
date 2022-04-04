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

package org.evosuite.instrumentation;

import org.evosuite.Properties;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * <p>TransformationStatistics class.</p>
 *
 * @author Gordon Fraser
 */
public class TransformationStatistics {

    private static final Logger logger = LoggerFactory.getLogger(TransformationStatistics.class);

    /**
     * Constant <code>transformedBooleanComparison=0</code>
     */
    public static int transformedBooleanComparison = 0;

    /**
     * Constant <code>insertedGet=0</code>
     */
    public static int insertedGet = 0;

    /**
     * Constant <code>insertedPushInt0=0</code>
     */
    public static int insertedPushInt0 = 0;

    /**
     * Constant <code>insertedPushInt1=0</code>
     */
    public static int insertedPushInt1 = 0;

    /**
     * Constant <code>insertedPushIntRef=0</code>
     */
    public static int insertedPushIntRef = 0;

    /**
     * Constant <code>insertedPushIntNull=0</code>
     */
    public static int insertedPushIntNull = 0;

    /**
     * Constant <code>transformedComparison=0</code>
     */
    public static int transformedComparison = 0;

    /**
     * Constant <code>transformedImplicitElse=0</code>
     */
    public static int transformedImplicitElse = 0;

    /**
     * Constant <code>transformedInstanceOf=0</code>
     */
    public static int transformedInstanceOf = 0;

    /**
     * Constant <code>transformedBooleanReturn=0</code>
     */
    public static int transformedBooleanReturn = 0;

    /**
     * Constant <code>transformedBooleanParameter=0</code>
     */
    public static int transformedBooleanParameter = 0;

    /**
     * Constant <code>transformedBooleanField=0</code>
     */
    public static int transformedBooleanField = 0;

    /**
     * Constant <code>transformedBackToBooleanParameter=0</code>
     */
    public static int transformedBackToBooleanParameter = 0;

    /**
     * Constant <code>transformedBackToBooleanField=0</code>
     */
    public static int transformedBackToBooleanField = 0;

    /**
     * Constant <code>untransformableMethod=0</code>
     */
    public static int untransformableMethod = 0;

    /**
     * Constant <code>transformedStringComparison=0</code>
     */
    public static int transformedStringComparison = 0;

    /**
     * Constant <code>transformedContainerComparison=0</code>
     */
    public static int transformedContainerComparison = 0;

    /**
     * Constant <code>transformedBitwise=0</code>
     */
    public static int transformedBitwise = 0;

    /**
     * <p>reset</p>
     */
    public static void reset() {
        transformedBooleanComparison = 0;
        insertedGet = 0;
        insertedPushInt0 = 0;
        insertedPushInt1 = 0;
        insertedPushIntRef = 0;
        insertedPushIntNull = 0;
        transformedComparison = 0;
        transformedImplicitElse = 0;
        transformedInstanceOf = 0;
        transformedBooleanReturn = 0;
        transformedBooleanParameter = 0;
        untransformableMethod = 0;
        transformedStringComparison = 0;
        transformedContainerComparison = 0;
    }

    /**
     * IFEQ -> IFLE / IFNE -> IFGT
     */
    public static void transformedBooleanComparison() {
        transformedBooleanComparison++;
    }

    /**
     * Insertion of getPredicate
     */
    public static void insertedGet() {
        insertedGet++;
    }

    /**
     * Insertion of pushDistance
     *
     * @param opcode a int.
     */
    public static void insertPush(int opcode) {
        switch (opcode) {
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
                insertedPushInt0++;
                break;
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
                insertedPushInt1++;
                break;
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                insertedPushIntRef++;
                break;
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
                insertedPushIntNull++;
                break;
            default:
                // GOTO, JSR: Do nothing
        }
    }

    /**
     * LCMPL, DCMPL, FCMPL
     */
    public static void transformedComparison() {
        transformedComparison++;
    }

    /**
     * Added implicit else
     */
    public static void transformedImplicitElse() {
        transformedImplicitElse++;
    }

    /**
     * InstanceOf
     */
    public static void transformInstanceOf() {
        transformedInstanceOf++;
    }

    /**
     * Return value was boolean
     */
    public static void transformBooleanReturnValue() {
        transformedBooleanReturn++;
    }

    /**
     * Parameter value was boolean
     */
    public static void transformBooleanParameter() {
        transformedBooleanParameter++;
    }

    /**
     * Field was boolean
     */
    public static void transformBooleanField() {
        transformedBooleanField++;
    }

    /**
     * Parameter value was boolean
     */
    public static void transformBackToBooleanParameter() {
        transformedBackToBooleanParameter++;
    }

    /**
     * Field was boolean
     */
    public static void transformBackToBooleanField() {
        transformedBackToBooleanField++;
    }

    /**
     * Method contains boolean in signature, but can't be transformed
     */
    public static void foundUntransformableMethod() {
        untransformableMethod++;
    }

    /**
     * String.equals or similar
     */
    public static void transformedStringComparison() {
        transformedStringComparison++;
    }

    /**
     * Container.isEmpty or similar
     */
    public static void transformedContainerComparison() {
        transformedContainerComparison++;
    }

    /**
     * Bitwise AND, OR, XOR
     */
    public static void transformedBitwise() {
        transformedBitwise++;
    }

    /**
     * <p>writeStatistics</p>
     *
     * @param className a {@link java.lang.String} object.
     */
    public static void writeStatistics(String className) {
        try {
            String filename = Properties.REPORT_DIR + "/transformation.csv";
            File logfile = new File(filename);
            boolean needHeader = !logfile.exists();
            BufferedWriter out = new BufferedWriter(new FileWriter(logfile, true));

            if (needHeader)
                out.write("ClassName,BooleanComparison,Get,Push0,Push1,PushRef,PushNull,Comparison,ImplicitElse,InstanceOf,BooleanReturn,BooleanParameter,BooleanField,BackToBooleanParameter,BackToBooleanField,UntransformableMethod,StringComparison,ContainerComparison\n");

            out.write(className);
            out.write(",");
            out.write(transformedBooleanComparison + ",");
            out.write(insertedGet + ",");
            out.write(insertedPushInt0 + ",");
            out.write(insertedPushInt1 + ",");
            out.write(insertedPushIntRef + ",");
            out.write(insertedPushIntNull + ",");
            out.write(transformedComparison + ",");
            out.write(transformedImplicitElse + ",");
            out.write(transformedInstanceOf + ",");
            out.write(transformedBooleanReturn + ",");
            out.write(transformedBooleanParameter + ",");
            out.write(transformedBooleanField + ",");
            out.write(transformedBackToBooleanParameter + ",");
            out.write(transformedBackToBooleanField + ",");
            out.write(transformedStringComparison + ",");
            out.write(transformedContainerComparison + ",");
            out.write("\n");
            out.close();
        } catch (IOException e) {
            logger.info("Exception while writing CSV data: " + e);
        }
    }
}
