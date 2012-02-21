/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Gordon Fraser
 * 
 */
public class TransformationStatistics {

	private static Logger logger = LoggerFactory.getLogger(TransformationStatistics.class);

	public static int transformedBooleanComparison = 0;

	public static int insertedGet = 0;

	public static int insertedPushInt0 = 0;

	public static int insertedPushInt1 = 0;

	public static int insertedPushIntRef = 0;

	public static int insertedPushIntNull = 0;

	public static int transformedComparison = 0;

	public static int transformedImplicitElse = 0;

	public static int transformedInstanceOf = 0;

	public static int transformedBooleanReturn = 0;

	public static int transformedBooleanParameter = 0;

	public static int transformedBooleanField = 0;

	public static int transformedBackToBooleanParameter = 0;

	public static int transformedBackToBooleanField = 0;

	public static int untransformableMethod = 0;

	public static int transformedStringComparison = 0;

	public static int transformedContainerComparison = 0;

	public static int transformedBitwise = 0;

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
