package de.unisb.cs.st.testability;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yanchuan Li
 * Date: 2/10/11
 * Time: 10:19 PM
 */
public class StatisticUtil {

    private static Logger log = Logger.getLogger(StatisticUtil.class);

    public static final String BooleanAsFlag = "BooleanAsFlag";
    public static final String BooleansBySimpleTrueAndFalse = "BooleansBySimpleTrueAndFalse";
    public static final String BooleansByVariable = "BooleansByVariable";
    public static final String BooleansBySimpleMethod = "BooleansBySimpleMethod";
    public static final String BooleansByArithmeticOperation = "BooleansByArithmeticOperation";
    public static final String BooleansByBooleanOrMethodPredicate = "BooleansByBooleanOrMethodPredicate";
    public static final String BooleansByEqualityComparison = "BooleansByEqualityComparison";
    public static final String BooleansByNullityComparison = "BooleansByNullityComparison";
    public static final String BooleansByInstanceof = "BooleansByInstanceof";
    public static final String MethodDuplicated = "MethodDuplicated";
    public static final String MethodSignatureUpdated = "MethodSignatureUpdated";
    public static final String FieldRetyped = "FieldRetyped";
    public static final String LocalVariableRetyped = "LocalVariableRetyped";

    public static Map<String, Integer> logbook = new HashMap<String, Integer>();



    public static void clear() {
        logbook.clear();
    }

    public static int numOfBooleanUsages() {
        if (logbook.containsKey(BooleanAsFlag)) {
            return logbook.get(BooleanAsFlag);
        } else {
            return 0;
        }
    }

    public static int numOfBooleanDefinitions() {
        int result = 0;
        for (String s : logbook.keySet()) {
            if (!s.equals(BooleanAsFlag)) {
                result = result + logbook.get(s);
            }
        }
        return result;
    }

    public static void registerTransformation(String s) {
        if (logbook.containsKey(s)) {
            int i = logbook.get(s);
            i++;
            logbook.put(s, i);
        } else {
            logbook.put(s, 1);
        }
    }


}

