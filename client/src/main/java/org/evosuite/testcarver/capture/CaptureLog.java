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
package org.evosuite.testcarver.capture;

import com.thoughtworks.xstream.XStream;
import org.evosuite.PackageInfo;
import org.evosuite.testcarver.instrument.TransformerUtil;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;

public final class CaptureLog implements Cloneable {

    //=============   static, final fields ===================================================

    private static final Logger logger = LoggerFactory.getLogger(CaptureLog.class);

    public static final Object[] NO_ARGS = new Object[0];
    public static final String OBSERVED_INIT = "<init>";
    public static final String PLAIN_INIT = CaptureLog.class.getName() + ".PLAIN";
    public static final String COLLECTION_INIT = CaptureLog.class.getName()
            + ".COLLECTION";
    public static final String MAP_INIT = CaptureLog.class.getName() + ".MAP";
    public static final String ARRAY_INIT = CaptureLog.class.getName() + ".ARRAY";

    public static final String NOT_OBSERVED_INIT = CaptureLog.class.getName() + ".XINIT";

    public static final String END_CAPTURE_PSEUDO_METHOD = CaptureLog.class.getName()
            + ".END_CAPTURE";
    public static final int PSEUDO_CAPTURE_ID = Integer.MAX_VALUE; // for internally created statement (PLAIN_INIT and NOT_OBSERVED_INIT)

    public static final String EMPTY_DESC = Type.getMethodDescriptor(Type.VOID_TYPE
    );
    public static final int NO_DEPENDENCY = -1;

    public static final String PUTFIELD = "PUTFIELD";
    public static final String PUTSTATIC = "PUTSTATIC";
    public static final String GETFIELD = "GETFIELD";
    public static final String GETSTATIC = "GETSTATIC";

    public static final Object RETURN_TYPE_VOID = CaptureLog.class.getName()
            + ".RETURN_VOID";

    private static final Set<String> NOT_OBSERVED_INIT_METHODS = Collections.synchronizedSet(new LinkedHashSet<>());

    static {
        NOT_OBSERVED_INIT_METHODS.add(NOT_OBSERVED_INIT);
        NOT_OBSERVED_INIT_METHODS.add(COLLECTION_INIT);
        NOT_OBSERVED_INIT_METHODS.add(MAP_INIT);
        NOT_OBSERVED_INIT_METHODS.add(ARRAY_INIT);
    }

    //=============   local, object fields ===================================================

    /*
     * FIXME: the design of this class breaks OO encapsulation.
     * Fields are declared 'final', but their content can be accessed/changed from outside.
     * Need re-factoring.
     *
     * For example, are these lists supposed to have same length? (ie invariant)
     */

    //--- LOG Table
    // REC_NO | OID | METHOD | PARAMS

    /*
     * FIXME: following lists seem to be aligned
     */

    // rec_no is implied by index
    public final List<Integer> objectIds;
    public final List<Integer> captureIds;
    public final List<String> methodNames;
    /**
     * FIXME: this seems always containing Integer objects, representing either
     * null or an object identifier (oid). should it be <Integer[]> ?
     */
    public final List<Object[]> params;
    public final List<Object> returnValues;
    public final List<Boolean> isStaticCallList;
    public final List<String> descList;

    //--- OID Info Table
    // OID | INIT_REC_NO | CLASS

    /*
     * FIXME: the following lists seem to be aligned.
     * Would be better to have a single list, with object
     * containing the different fields
     */
    private final List<Integer> oids;
    private final List<Integer> oidInitRecNo;
    private final List<String> oidClassNames;
    private final List<Integer> oidFirstInits;
    private final List<Integer> oidDependencies;

    /**
     * captureId -> field name
     */
    private final Map<Integer, String> oidNamesOfAccessedFields;

    /**
     * oid -> index ==> oidInitReco.get(index) + oidClassNames.get(index)
     */
    private final Map<Integer, Integer> oidRecMapping;

    private final XStream xstream;

    /**
     * Main constructor
     */
    public CaptureLog() {
        this.objectIds = new ArrayList<>();
        this.methodNames = new ArrayList<>();
        this.params = new ArrayList<>();
        this.captureIds = new ArrayList<>();
        this.returnValues = new ArrayList<>();
        this.descList = new ArrayList<>();

        this.oidRecMapping = new LinkedHashMap<>();
        this.oidInitRecNo = new ArrayList<>();
        this.oidClassNames = new ArrayList<>();
        this.oids = new ArrayList<>();
        this.oidFirstInits = new ArrayList<>();
        this.oidDependencies = new ArrayList<>();

        this.isStaticCallList = new ArrayList<>();

        this.oidNamesOfAccessedFields = new LinkedHashMap<>();

        this.xstream = new XStream();
    }

    public String getNameOfAccessedFields(final int captureId) {
        return oidNamesOfAccessedFields.get(captureId);
    }

    public int getDependencyOID(final int oid) {
        int index = getRecordIndex(oid);
        return oidDependencies.get(index);
    }

    public List<Integer> getTargetOIDs(final Set<String> observedClassNames) {
        final List<Integer> targetOIDs = new ArrayList<>();
        final int numInfoRecs = oidClassNames.size();
        for (int i = 0; i < numInfoRecs; i++) {
            if (observedClassNames.contains(oidClassNames.get(i))) {
                targetOIDs.add(getOID(i));
            }
        }
        return targetOIDs;
    }

    public String getTypeName(final int oid) throws IllegalArgumentException {
        if (!oidRecMapping.containsKey(oid)) {
            throw new IllegalArgumentException("OID " + oid + " is not recognized");
        }
        return oidClassNames.get(getRecordIndex(oid));
    }

    public int getRecordIndex(int oid) {
        return oidRecMapping.get(oid);
    }

    public int getOID(int recordIndex) {
        if (recordIndex < 0 || recordIndex >= oids.size()) {
            throw new IllegalArgumentException("index " + recordIndex
                    + " is invalid as there are " + oids.size() + " OIDs");
        }
        return oids.get(recordIndex);
    }

    public List<String> getObservedClasses() {
        return oidClassNames;
    }

    public int getRecordIndexOfWhereObjectWasInitializedFirst(int oid)
            throws IllegalArgumentException {
        if (!oidRecMapping.containsKey(oid)) {
            throw new IllegalArgumentException("OID " + oid + " is not recognized");
        }

        int pos = oidRecMapping.get(oid);
        return oidInitRecNo.get(pos);
    }

    /**
     * FIXME: this does not make sense... it seems like oidInitRecNo contains
     * integers that have different meaning depending on whether their are
     * positive or not...
     *
     * @param currentRecord
     */
    private void addNewInitRec(int currentRecord) {
        // negative log rec no indicates obj construction
        this.oidInitRecNo.add(-currentRecord);
        logger.debug("InitRecNo added " + (-currentRecord));
    }

    public void updateWhereObjectWasInitializedFirst(int oid, int recordIndex)
            throws IllegalArgumentException {
        if (!oidRecMapping.containsKey(oid)) {
            throw new IllegalArgumentException("OID " + oid + " is not recognized");
        }
        int nRec = objectIds.size();
        /*
         * FIXME: it seems negative indexes have special meaning...
         */
        if (-recordIndex <= -nRec || recordIndex >= nRec) {
            throw new IllegalArgumentException("New record index " + recordIndex
                    + " is invalid, as there are only " + nRec + " records");
        }

        logger.debug("Updating init of OID " + oid + " from pos="
                + getRecordIndexOfWhereObjectWasInitializedFirst(oid) + " to pos="
                + recordIndex);

        // Only update init record if its number is bigger than the current init record number
        // Note that record numbers indicating fist object occurrence are marked as negative number
        // For example: constructor call at record no 8 becomes -8
        final int recentInitRecord = getRecordIndexOfWhereObjectWasInitializedFirst(oid);
        if (Math.abs(recordIndex) > Math.abs(recentInitRecord)) {
            oidInitRecNo.set(oidRecMapping.get(oid), recordIndex);
        }
    }

    @Override
    public CaptureLog clone() {
        final CaptureLog log = new CaptureLog();

        log.objectIds.addAll(this.objectIds);
        log.methodNames.addAll(this.methodNames);
        log.params.addAll(this.params);
        log.captureIds.addAll(this.captureIds);
        log.returnValues.addAll(this.returnValues);
        log.descList.addAll(this.descList);

        log.oidRecMapping.putAll(this.oidRecMapping);
        log.oidInitRecNo.addAll(this.oidInitRecNo);
        log.oidClassNames.addAll(this.oidClassNames);
        log.oids.addAll(this.oids);
        log.oidNamesOfAccessedFields.putAll(this.oidNamesOfAccessedFields);
        log.isStaticCallList.addAll(this.isStaticCallList);
        log.oidDependencies.addAll(this.oidDependencies);
        log.oidFirstInits.addAll(this.oidFirstInits);

        return log;
    }

    public void clear() {
        this.objectIds.clear();
        this.methodNames.clear();
        this.params.clear();
        this.captureIds.clear();
        this.returnValues.clear();
        this.descList.clear();

        this.oidRecMapping.clear();
        this.oidInitRecNo.clear();
        this.oidClassNames.clear();
        this.oids.clear();
        this.oidFirstInits.clear();
        this.oidDependencies.clear();
        this.isStaticCallList.clear();

        this.oidNamesOfAccessedFields.clear();
    }

    private boolean updateInfoTable(final int oid, final Object receiver,
                                    final boolean replace) {
        // update oid info table, if necessary
        // -> we assume that USUALLY the first record belonging to an object belongs to its instanciation
        if (this.oidRecMapping.containsKey(oid)) {
            if (replace) {
                final int logRecNo = this.objectIds.size();
                updateWhereObjectWasInitializedFirst(oid, -logRecNo);
                return true;
            } else {
                return false;
            }
        } else {
            final int logRecNo = this.objectIds.size();
            final int infoRecNo = this.oidInitRecNo.size();

            logger.debug("Adding mapping oid->index   {} -> {}", oid, infoRecNo);
            this.oidRecMapping.put(oid, infoRecNo);
            addNewInitRec(logRecNo);

            oidFirstInits.add(logRecNo);

            oidDependencies.add(NO_DEPENDENCY);

            registerObjectsClassName(receiver);

            this.oids.add(oid);

            return true;
        }
    }

    private void registerObjectsClassName(final Object receiver) {
        if (receiver instanceof Class) //this can only happen, if there is a static method call
        {
            final Class<?> c = (Class<?>) receiver;
            this.oidClassNames.add(c.getName().replace(PackageInfo.getEvoSuitePackage() + ".testcarver.wrapper.", ""));
            //.replaceFirst("\\$\\d+$", ""));

        } else if (this.isPlain(receiver)) {
            // we don't need fully qualified name for plain types

            // TODO: I don't understand why we would want to shorten the name if it's a primitive.
            //       It makes it more difficult later to identify the classes contained in the log.
            this.oidClassNames.add(receiver.getClass().getName());//.replaceFirst("\\$\\d+$", ""));
            //	this.oidClassNames.add(receiver.getClass().getSimpleName());//.replaceFirst("\\$\\d+$", ""));
        } else if (isProxy(receiver) || isAnonymous(receiver)) {
            // TODO what if there is more than one interface?
            final Class<?> c = receiver.getClass();
            final Class<?>[] interfaces = c.getInterfaces();
            if (interfaces.length == 0) {
                // If there are no interfaces, try superclass?
                this.oidClassNames.add(c.getSuperclass().getName());
            } else {
                this.oidClassNames.add(interfaces[0].getName());
            }
        } else {
            String name = receiver.getClass().getName().replace(PackageInfo.getEvoSuitePackage() + ".testcarver.wrapper.", "");
            this.oidClassNames.add(name);//.replaceFirst("\\$\\d+$", ""));
        }
    }

    private boolean isAnonymous(final Object receiver) {
        return receiver.getClass().isAnonymousClass();
    }

    private boolean isProxy(final Object receiver) {
        return Proxy.isProxyClass(receiver.getClass());
    }

    private boolean isPlain(final Object o) {
        return //o instanceof Class   ||
                o instanceof String || o instanceof Integer || o instanceof Double
                        || o instanceof Float || o instanceof Long || o instanceof Byte
                        || o instanceof Short || o instanceof Boolean || o instanceof Character;
    }

    /**
     * if there is an return value and the return value creation has not been
     * logged before (may happen, if, for example, the constructor is private),
     * save the information that the value comes from a finished method call
     *
     * @param captureId
     * @param receiver
     * @param returnValue
     */
    public void logEnd(final int captureId, final Object receiver,
                       final Object returnValue) {
        if (returnValue != null && returnValue != RETURN_TYPE_VOID) {
            handleReturnValue(captureId, receiver, returnValue);
        }

        this.captureIds.add(captureId);
        this.objectIds.add(System.identityHashCode(receiver));
        this.methodNames.add(END_CAPTURE_PSEUDO_METHOD);
        this.descList.add(EMPTY_DESC);
        this.params.add(NO_ARGS);
        this.returnValues.add(RETURN_TYPE_VOID);
        this.isStaticCallList.add(Boolean.FALSE);
    }

    /**
     * Find start of method call statement (created by CaptureLog.log()) for
     * capture id and receiver
     *
     * @param captureId
     * @param receiver
     * @param returnValue
     */
    private int findRecordOfMethodStart(final Object receiver, final int captureId) {
        final int oid = System.identityHashCode(receiver);

        int currentRecord = captureIds.size() - 1;

        int nestedCalls = 0;
        while (true) {
            if (this.captureIds.get(currentRecord) == captureId
                    && this.objectIds.get(currentRecord) == oid) {
                if (this.methodNames.get(currentRecord).equals(END_CAPTURE_PSEUDO_METHOD)) {
                    nestedCalls++;
                } else {
                    if (nestedCalls == 0) {
                        break;
                    } else {
                        nestedCalls--;
                    }
                }
            }
            currentRecord--;
        }

        return currentRecord;
    }

    private void handleReturnValue(final int captureId, final Object receiver,
                                   final Object returnValue) {
        final int returnValueOID = System.identityHashCode(returnValue);

        boolean condition = !this.oidRecMapping.containsKey(returnValueOID);

        if (!condition) {

            // oid of the target object is already known so we have to check if we should determine the corresponding method call for the return value
            // and adjust its init meta data

            final int firstInitRecNo = this.oidFirstInits.get(this.oidRecMapping.get(returnValueOID));

            final String methodName = methodNames.get(firstInitRecNo);
            final boolean isObservedConstructionCaughtForThisObject = methodName.equals(OBSERVED_INIT);
            final boolean isUnObservedConstructionCaughtForThisObject = NOT_OBSERVED_INIT_METHODS.contains(methodNames.get(firstInitRecNo));
            final boolean noReturnValueHasBeenSet = RETURN_TYPE_VOID.equals(returnValues.get(firstInitRecNo));

            if (!isObservedConstructionCaughtForThisObject
                    && !isUnObservedConstructionCaughtForThisObject) {
                final int methodStartRecord = findRecordOfMethodStart(receiver, captureId);

                // did the method call appear before the object construction was performed?
                // this is important because this method call might be used to reconstruct object construction instead of
                // the constructor calls
                if (methodStartRecord < firstInitRecNo) {
                    condition = noReturnValueHasBeenSet;
                } else {
                    condition = false;
                }

            } else {
                condition = noReturnValueHasBeenSet;
            }
        }

        if (condition) {
            if (!isPlain(returnValue) && !(returnValue instanceof Class)) {

                final int currentRecord = findRecordOfMethodStart(receiver, captureId);

                if (this.oidRecMapping.containsKey(returnValueOID)) {
                    final int infoRecNo = this.oidRecMapping.get(returnValueOID);
                    final int initRecNo = getRecordIndexOfWhereObjectWasInitializedFirst(returnValueOID);
                    final String method = this.methodNames.get(Math.abs(initRecNo));

                    if ((!OBSERVED_INIT.equals(method) && !NOT_OBSERVED_INIT_METHODS.contains(method)))//|| currentRecord < Math.abs(initRecNo))
                    {
                        this.returnValues.set(currentRecord, returnValueOID); // oid as integer works here as we exclude plain values
                        updateWhereObjectWasInitializedFirst(returnValueOID,
                                -currentRecord);
                        this.oidFirstInits.set(infoRecNo, currentRecord);
                    } else {
                        this.returnValues.set(currentRecord, returnValueOID);
                    }

                } else {
                    final int infoRecNo = this.oidInitRecNo.size();
                    this.oidRecMapping.put(returnValueOID, infoRecNo);
                    addNewInitRec(currentRecord);
                    this.oidFirstInits.add(currentRecord);

                    this.returnValues.set(currentRecord, returnValueOID); // oid as integer works here as we exclude plain values

                    this.registerObjectsClassName(returnValue);

                    //					this.oidClassNames.add(returnValue.getClass().getName());

                    this.oids.add(returnValueOID);
                    this.oidDependencies.add(NO_DEPENDENCY);
                }
            }
        }
    }

    /**
     * For example:
     * <p>
     * public class Foo { public class Bar(){} }
     *
     * @param receiver
     */
    private void checkIfInstanceFromInnerInstanceClass(final Object receiver) {
        if (!(receiver instanceof Class)) {
            final Class<?> receiverClass = receiver.getClass();
            final Class<?> enclosingClass = receiverClass.getEnclosingClass();
            if (enclosingClass == null) {
                // do nothing
                return;
            } else {
                if (!receiverClass.isAnonymousClass()
                        && !Modifier.isStatic(receiverClass.getModifiers())) {
                    try {
                        /*
                         * The bytecode of the Outer$Inner class will contain a package-scoped field named this$0 of type Outer.
                         * That's how non-static inner classes are implemented in Java, because at bytecode level there is no concept of an inner class.
                         *
                         * see http://stackoverflow.com/questions/763543/in-java-how-do-i-access-the-outer-class-when-im-not-in-the-inner-class
                         * for further details
                         */
                        final Field this$0 = receiverClass.getDeclaredField("this$0");
                        this$0.setAccessible(true);
                        final Object outerInstance = this$0.get(receiver);

                        //if (TransformerUtil.isClassConsideredForInstrumentation(outerInstance.getClass().getName())) {
                        // FIXME
                        //}

                        // the enclosing object has to be restored first

                        final int receiverOID = System.identityHashCode(receiver);
                        final int initRecNo = this.oidRecMapping.get(receiverOID);
                        this.oidDependencies.set(initRecNo,
                                System.identityHashCode(outerInstance));
                    } catch (final Exception e) {
                        logger.info("An error occurred while obtaining the enclosing object of an inner non-static class instance. "
                                        + "FIELDS: "
                                        + Arrays.toString(receiverClass.getDeclaredFields()),
                                e);
                    }
                }
            }
        }

    }

    public void log(final int captureId, final Object receiver, final String methodName,
                    final String methodDesc, Object... methodParams) {
        final int oid = System.identityHashCode(receiver);

        final boolean isConstructor = OBSERVED_INIT.equals(methodName);

        // TODO find nicer way
        if (PUTSTATIC.equals(methodName) || PUTFIELD.equals(methodName)) {
            /*
             * The first param always specifies the name of the accessed field.
             * The second param represents the actual value.
             */
            this.oidNamesOfAccessedFields.put(captureId, (String) methodParams[0]);
            final Object assignedValue = methodParams[1];
            methodParams = new Object[1];
            methodParams[0] = assignedValue;
        } else if (GETSTATIC.equals(methodName) || GETFIELD.equals(methodName)) {
            /*
             * The param always specifies the name of the accessed field.
             */
            this.oidNamesOfAccessedFields.put(captureId, (String) methodParams[0]);
            methodParams = new Object[0];
        } else {
            // if it's not a constructor call, check if something regarding the receiver object has been logged before.
            // if this is not the case, we know that the object construction could not be observed. Due to the instrumentation
            // logic, this is most likely an error but we have to provide some information regarding the object construction nevertheless
            // --> create UNOBSERVED_INIT log entry
            if (!isConstructor && !this.oidRecMapping.containsKey(oid)
                    && !(receiver instanceof Class)) {
                logger.info("method {} was called on object {} with oid {} without foregoing (observed) init stmt --> creating unobserved init stmt",
                        methodName, receiver, oid);
                this.updateInfoTable(oid, receiver, isConstructor);
                logUnobservedInitStmt(receiver);
            }
        }

        // TODO this.updateInfoTable(oid, receiver, isConstructor);

        // save receiver class -> might be reference in later calls e.g. doSth(Person.class)
        if (receiver instanceof Class) {
            this.objectIds.add(oid);
            this.descList.add(EMPTY_DESC);
            this.methodNames.add(PLAIN_INIT);
            this.params.add(new Object[]{receiver});
            this.returnValues.add(RETURN_TYPE_VOID);
            this.captureIds.add(PSEUDO_CAPTURE_ID);
            this.isStaticCallList.add(Boolean.FALSE);
            this.logEnd(PSEUDO_CAPTURE_ID, receiver, RETURN_TYPE_VOID);
        }

        //--- handle method params
        Object param;
        int paramOID;
        for (int i = 0; i < methodParams.length; i++) {
            param = methodParams[i];

            // null and plain params have PLAIN init stmts such as
            // Integer var0 = 122
            // Float var1 = 2.3
            // String var2 = "Hello World"
            // e.g. o.myMethod(null, var0, var1, var2);
            if (param != null) {
                // we assume that all classes (besides java and sun classes) are instrumented.
                // So if there is no foregoing entry in the oid info table, the param is a new and
                // not monitored instance. That's why this param has to be serialized.
                paramOID = System.identityHashCode(param);

                if (paramOID == oid) {
                    logger.info("PARAM is 'this' reference -> are serialized version of 'this' is created and passed as param");

                    // we serialize and deserialize param in order to get a 'cloned' instance of param
                    // -> this approach is not very efficient but we can always clone an object without the
                    //    the need of the Cloneable interface
                    try {
                        String xml = xstream.toXML(param);
                        param = xstream.fromXML(xml);
                        paramOID = System.identityHashCode(param);

                        logUnobservedInitStmt(param);
                    } catch (final Exception e) {
                        logger.info("an error occurred while serializing and deserializing {} -> is handled as NULL param",
                                param, e);
                        continue;
                    }
                } else {
                    createInitLogEntries(param);
                }

                // method param  has been created before so we link to it
                // NECESSARY as the object might be modified in between
                // exemplary output in test code:
                // Object a = new Object();
                // ...
                // o.m(a);
                methodParams[i] = paramOID;
            }
        }

        // update info table if necessary
        // in case of constructor calls, we want to remember the last one
        this.updateInfoTable(oid, receiver, isConstructor);

        //--- create method call record
        this.objectIds.add(oid);
        this.methodNames.add(methodName);
        this.descList.add(methodDesc);
        this.params.add(methodParams);
        this.returnValues.add(RETURN_TYPE_VOID);
        this.captureIds.add(captureId);
        this.isStaticCallList.add(receiver instanceof Class);

        this.checkIfInstanceFromInnerInstanceClass(receiver);
    }

    @SuppressWarnings("rawtypes")
    private void createInitLogEntries(final Object param) {
        if (param == null) {
            return;
        }

        final int paramOID = System.identityHashCode(param);
        final boolean isArray = param.getClass().isArray();
        final boolean isMap = param instanceof Map;
        final boolean isCollection = param instanceof Collection;

        if (isArray || isMap || isCollection
                || this.updateInfoTable(paramOID, param, false)) {

            final boolean isInstrumented = TransformerUtil.isClassConsideredForInstrumentation(param.getClass().getName());

            if (isPlain(param) || param instanceof Class) {
                this.objectIds.add(paramOID);
                // exemplary output in test code: Integer number = 123;
                this.methodNames.add(PLAIN_INIT);
                this.params.add(new Object[]{param});
                this.descList.add(EMPTY_DESC);
                this.returnValues.add(RETURN_TYPE_VOID);
                this.captureIds.add(PSEUDO_CAPTURE_ID);
                this.isStaticCallList.add(Boolean.FALSE);
                this.logEnd(PSEUDO_CAPTURE_ID, param, RETURN_TYPE_VOID);

            } else if (isCollection && !isInstrumented) {

                final Collection c = (Collection) param;

                final Object[] valArray = new Object[c.size()];
                int index = 0;
                for (Object o : c) {
                    if (o != null) {
                        createInitLogEntries(o);
                        valArray[index] = System.identityHashCode(o);
                    }

                    index++;
                }

                if (!this.oidRecMapping.containsKey(paramOID)) {
                    this.updateInfoTable(paramOID, param, true);
                }

                this.objectIds.add(paramOID);
                this.methodNames.add(COLLECTION_INIT);
                this.params.add(valArray);
                this.descList.add(EMPTY_DESC);
                this.returnValues.add(RETURN_TYPE_VOID);
                this.captureIds.add(PSEUDO_CAPTURE_ID);
                this.isStaticCallList.add(Boolean.FALSE);
                this.logEnd(PSEUDO_CAPTURE_ID, param, RETURN_TYPE_VOID);

            } else if (isMap && !isInstrumented) {

                final Map m = (Map) param;
                final Object[] valArray = new Object[m.size() * 2];

                Map.Entry entry;
                Object v, k;
                int index = 0;
                for (Object oe : m.entrySet()) {
                    entry = (Map.Entry) oe;
                    k = entry.getKey();
                    createInitLogEntries(k);

                    valArray[index++] = System.identityHashCode(k);

                    v = entry.getValue();
                    if (v == null) {
                        valArray[index++] = null;
                    } else {
                        createInitLogEntries(v);

                        valArray[index++] = System.identityHashCode(v);
                    }
                }

                if (!this.oidRecMapping.containsKey(paramOID)) {
                    this.updateInfoTable(paramOID, param, true);
                }

                this.objectIds.add(paramOID);
                this.methodNames.add(MAP_INIT);
                this.params.add(valArray);
                this.descList.add(EMPTY_DESC);
                this.returnValues.add(RETURN_TYPE_VOID);
                this.captureIds.add(PSEUDO_CAPTURE_ID);
                this.isStaticCallList.add(Boolean.FALSE);
                this.logEnd(PSEUDO_CAPTURE_ID, param, RETURN_TYPE_VOID);

            } else if (isArray) {
                // we use Array to handle primitive and Object arrays in the same way
                final int arraySize = Array.getLength(param);

                final Object[] valArray = new Object[arraySize];

                Object o;
                for (int index = 0; index < arraySize; index++) {
                    o = Array.get(param, index);
                    if (o != null) {
                        createInitLogEntries(o);
                        valArray[index] = System.identityHashCode(o);
                    }
                }

                if (!this.oidRecMapping.containsKey(paramOID)) {
                    this.updateInfoTable(paramOID, param, true);
                }

                this.objectIds.add(paramOID);
                this.methodNames.add(ARRAY_INIT);
                this.params.add(valArray);
                this.descList.add(EMPTY_DESC);
                this.returnValues.add(RETURN_TYPE_VOID);
                this.captureIds.add(PSEUDO_CAPTURE_ID);
                this.isStaticCallList.add(Boolean.FALSE);
                this.logEnd(PSEUDO_CAPTURE_ID, param, RETURN_TYPE_VOID);

            } else {
                // we don't need to make a dump for instrumented classes because its state changes
                // are reproducible
                if (!isInstrumented) {
                    // we always need to make a dump of objects which are not instrumented
                    // because the state might have changed and we couldn't observerve it
                    logUnobservedInitStmt(param);
                }
            }

        }
    }

    private void logUnobservedInitStmt(final Object subject) {
        final int subjectOID = System.identityHashCode(subject);
        if (!this.oidRecMapping.containsKey(subjectOID)) {
            this.updateInfoTable(subjectOID, subject, true);
        }

        this.objectIds.add(subjectOID);
        // create new serialization record for first emersion
        // exemplary output in test code: Person newJoe = (Person) xstream.fromXML(xml);

        this.checkIfInstanceFromInnerInstanceClass(subject);
        this.methodNames.add(NOT_OBSERVED_INIT);

        try {
            //					this.xstream.toXML(param, sout);
            //					this.sout.flush();
            //
            //					this.params.add(new Object[]{ this.bout.toByteArray() });
            //
            //					this.bout.reset();
            // FIXME
            this.params.add(new Object[]{this.xstream.toXML(subject)});
        } catch (final Exception e) {
            logger.info("an error occurred while serializing param '{}' -> adding null as param instead",
                    subject, e);

            // param can not be serialized -> add null as param
            this.params.add(new Object[]{null});
        }

        this.descList.add(EMPTY_DESC);
        this.returnValues.add(RETURN_TYPE_VOID);
        this.captureIds.add(PSEUDO_CAPTURE_ID);
        this.isStaticCallList.add(Boolean.FALSE);
        this.logEnd(PSEUDO_CAPTURE_ID, subject, RETURN_TYPE_VOID);
    }

    @Override
    public String toString() {
        final String delimiter = "\t|\t";

        final StringBuilder builder = new StringBuilder(1000);

        builder.append("LOG:\n").append("-------------------------------------------------------------------").append('\n').append("RECNO").append(delimiter).append("OID").append(delimiter).append("CID").append(delimiter).append("METHOD").append(delimiter).append("PARAMS").append(delimiter).append("RETURN").append(delimiter).append("IS STATIC").append(delimiter).append("DESC").append(delimiter).append("ACCESSED FIELDS").append('\n').append("-------------------------------------------------------------------").append('\n');

        int captureId;

        final int numRecords = this.objectIds.size();
        for (int i = 0; i < numRecords; i++) {
            captureId = this.captureIds.get(i);

            builder.append(i).append(delimiter) // RECNO
                    .append(this.objectIds.get(i)).append(delimiter) // OID
                    .append(captureId).append(delimiter) // CID
                    .append(this.methodNames.get(i)).append(delimiter) // METHOD
                    .append(Arrays.toString(this.params.get(i))).append(delimiter) // PARAMS
                    .append(this.returnValues.get(i)).append(delimiter) // RETURN
                    .append(this.isStaticCallList.get(i)).append(delimiter) // IS STATIC
                    .append(this.descList.get(i)).append(delimiter) // DESC
                    .append(this.oidNamesOfAccessedFields.get(captureId)) // ACCESSED FIELDS
                    .append('\n');
        }

        builder.append('\n').append('\n');

        builder.append("META INF:\n").append("-------------------------------------------------------------------").append('\n').append("OID").append(delimiter).append("INIT RECNO").append(delimiter).append("OID CLASS").append(delimiter).append("ACCESSED FIELDS").append(delimiter).append("FIRST INIT").append(delimiter).append("DEPENDENCY").append('\n').append("-------------------------------------------------------------------").append('\n');

        final int numMetaInfRecords = this.oids.size();
        for (int i = 0; i < numMetaInfRecords; i++) {
            builder.append(this.oids.get(i)).append(delimiter) // OID
                    .append(this.oidInitRecNo.get(i)).append(delimiter) // INIT RECNO
                    .append(this.oidClassNames.get(i)).append(delimiter) // OID CLASS
                    .append(this.oidNamesOfAccessedFields.get(i)).append(delimiter) // ACCESSED FIELDS
                    .append(this.oidFirstInits.get(i)).append(delimiter) // FIRST INIT FIELDS
                    .append(this.oidDependencies.get(i)) // DEPENCENCY FIELDS
                    .append('\n');
        }

        return builder.toString();
    }
}
