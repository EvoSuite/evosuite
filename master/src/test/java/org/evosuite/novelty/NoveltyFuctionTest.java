package org.evosuite.novelty;

import com.thoughtworks.xstream.XStream;
import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.contracts.ContractViolation;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.coverage.dataflow.FeatureFactory;
import org.evosuite.coverage.dataflow.FeatureKey;
import org.evosuite.feature.converters.StaticFieldConverter;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.TestVisitor;
import org.evosuite.testcase.execution.*;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.environment.AccessedEnvironment;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Listener;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;

public class NoveltyFuctionTest {

    public static XStream xstream = new XStream(new StaticFieldConverter());


    public static List<Map<Integer, Feature>> getFeature1(){
        Boo boo = new Boo(true, 2, 5);
        Foo foo = new Foo(5, new int[]{2,3,4}, boo);
        String feat = xstream.toXML(foo);

        Feature feature = new Feature();
        feature.setVariableName("x");
        feature.setMethodName("foo");
        feature.setValue(feat);
        Feature feature1 = new Feature();
        feature1.setVariableName("y");
        feature1.setMethodName("boo");
        feature1.setValue(0);
        Map<Integer, Feature> featureMap = new HashMap<>();
        featureMap.put(1, feature);
        //featureMap.put(2, feature1);
        List<Map<Integer, Feature>> list = new ArrayList<>();
        list.add(featureMap);

        /*Feature feature2 = new Feature();
        feature2.setVariableName("x");
        feature2.setMethodName("foo");
        feature2.setValue(4);
        Feature feature3 = new Feature();
        feature3.setVariableName("y");
        feature3.setMethodName("foo");
        feature3.setValue(7);
        Map<Integer, Feature> featureMap1 = new HashMap<>();
        featureMap1.put(1, feature2);
        featureMap1.put(2, feature3);
        list.add(featureMap1);*/


        return list;
    }
    public static List<Map<Integer, Feature>> getFeature2(){

        Boo boo1 = new Boo(true, 2, 8);
        Foo foo1 = new Foo(5, new int[]{2,3,4}, boo1);
        String feat1 = xstream.toXML(foo1);

        Feature feature = new Feature();
        feature.setVariableName("x");
        feature.setMethodName("foo");
        feature.setValue(feat1);
        Feature feature1 = new Feature();
        feature1.setVariableName("y");
        feature1.setMethodName("foo");
        feature1.setValue(7);
        Map<Integer, Feature> featureMap = new HashMap<>();
        featureMap.put(1, feature);
        //featureMap.put(2, feature1);
        List<Map<Integer, Feature>> list = new ArrayList<>();
        list.add(featureMap);
        return list;
    }
    public static List<Map<Integer, Feature>> getFeature3(){
        Boo boo2 = new Boo(true, 2, 9);
        Foo foo2 = new Foo(5, new int[]{2,3,4}, boo2);
        String feat2 = xstream.toXML(foo2);

        Feature feature = new Feature();
        feature.setVariableName("x");
        feature.setMethodName("foo");
        feature.setValue(feat2);
        Feature feature1 = new Feature();
        feature1.setVariableName("y");
        feature1.setMethodName("foo");
        feature1.setValue(7);
        Map<Integer, Feature> featureMap = new HashMap<>();
        featureMap.put(1, feature);
        //featureMap.put(2, feature1);
        List<Map<Integer, Feature>> list = new ArrayList<>();
        list.add(featureMap);
        return list;
    }
    public static List<Map<Integer, Feature>> getFeature4(){

        Boo boo3 = new Boo(true, 2, 10);
        Foo foo3 = new Foo(5, new int[]{2,3,4}, boo3);
        String feat3 = xstream.toXML(foo3);

        Feature feature = new Feature();
        feature.setVariableName("x");
        feature.setMethodName("foo");
        feature.setValue(feat3);
        Feature feature1 = new Feature();
        feature1.setVariableName("y");
        feature1.setMethodName("foo");
        feature1.setValue(9);
        Map<Integer, Feature> featureMap = new HashMap<>();
        featureMap.put(1, feature);
        //featureMap.put(2, feature1);
        List<Map<Integer, Feature>> list = new ArrayList<>();
        list.add(featureMap);
        return list;
    }
    public static List<Map<Integer, Feature>> getFeature5(){
        Boo boo4 = new Boo(true, 2, 15);
        Foo foo4 = new Foo(5, new int[]{2,3,4}, boo4);
        String feat4 = xstream.toXML(foo4);

        Feature feature = new Feature();
        feature.setVariableName("x");
        feature.setMethodName("foo");
        feature.setValue(feat4);
        Feature feature1 = new Feature();
        feature1.setVariableName("y");
        feature1.setMethodName("foo");
        feature1.setValue(7);
        Map<Integer, Feature> featureMap = new HashMap<>();
        featureMap.put(1, feature);
        //featureMap.put(2, feature1);
        List<Map<Integer, Feature>> list = new ArrayList<>();
        list.add(featureMap);
        return list;
    }

    @Test()
    public void testNoveltyCalculation(){

        // setup
        TestChromosome.class.getClassLoader().setClassAssertionStatus(TestChromosome.class.getName(), false);

        FeatureNoveltyFunction<TestChromosome> noveltyFunction = new FeatureNoveltyFunction();

        //Case 1: Calculating novelty on numeric values
        TestChromosome testChromosome = new TestChromosome();
        ExecutionResult origResult = new ExecutionResult(new TestCase() {
            @Override
            public int getID() {
                return 1;
            }

            @Override
            public void accept(TestVisitor visitor) {

            }

            @Override
            public void addAssertions(TestCase other) {

            }

            @Override
            public void addCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void removeCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void addContractViolation(ContractViolation violation) {

            }

            @Override
            public VariableReference addStatement(Statement statement) {
                return null;
            }

            @Override
            public VariableReference addStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void addStatements(List<? extends Statement> statements) {

            }

            @Override
            public void chop(int length) {

            }

            @Override
            public int sliceFor(VariableReference var) {
                return 0;
            }

            @Override
            public void clearCoveredGoals() {

            }

            @Override
            public boolean contains(Statement statement) {
                return false;
            }

            @Override
            public TestCase clone() {
                return null;
            }

            @Override
            public Set<Class<?>> getAccessedClasses() {
                return null;
            }

            @Override
            public AccessedEnvironment getAccessedEnvironment() {
                return null;
            }

            @Override
            public List<Assertion> getAssertions() {
                return null;
            }

            @Override
            public Set<ContractViolation> getContractViolations() {
                return null;
            }

            @Override
            public Set<TestFitnessFunction> getCoveredGoals() {
                return null;
            }

            @Override
            public Set<Class<?>> getDeclaredExceptions() {
                return null;
            }

            @Override
            public Set<VariableReference> getDependencies(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Object getObject(VariableReference reference, Scope scope) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(int position) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(Type type, int position) {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject() {
                return null;
            }

            @Override
            public VariableReference getRandomObject(int position) {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Set<VariableReference> getReferences(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getReturnValue(int position) {
                return null;
            }

            @Override
            public Statement getStatement(int position) {
                return null;
            }

            @Override
            public boolean hasStatement(int position) {
                return false;
            }

            @Override
            public boolean hasAssertions() {
                return false;
            }

            @Override
            public boolean hasCastableObject(Type type) {
                return false;
            }

            @Override
            public boolean hasObject(Type type, int position) {
                return false;
            }

            @Override
            public boolean hasReferences(VariableReference var) {
                return false;
            }

            @Override
            public boolean isAccessible() {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean isFailing() {
                return false;
            }

            @Override
            public void setFailing() {

            }

            @Override
            public boolean isGoalCovered(TestFitnessFunction goal) {
                return false;
            }

            @Override
            public boolean isPrefix(TestCase t) {
                return false;
            }

            @Override
            public boolean isUnstable() {
                return false;
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public void remove(int position) {

            }

            @Override
            public void removeAssertion(Assertion assertion) {

            }

            @Override
            public void removeAssertions() {

            }

            @Override
            public void replace(VariableReference var1, VariableReference var2) {

            }

            @Override
            public VariableReference setStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void setUnstable(boolean unstable) {

            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public int sizeWithAssertions() {
                return 0;
            }

            @Override
            public String toCode() {
                return null;
            }

            @Override
            public String toCode(Map<Integer, Throwable> exceptions) {
                return null;
            }

            @Override
            public Iterator<Statement> iterator() {
                return null;
            }

            @Override
            public void addListener(Listener<Void> listener) {

            }

            @Override
            public void deleteListener(Listener<Void> listener) {

            }
        });
        ExecutionTraceImpl executionTraceImpl = new ExecutionTraceImpl();
        executionTraceImpl.setListOfFeatureMap(getFeature1());
        ExecutionTrace executionTrace = new ExecutionTraceProxy(executionTraceImpl);
        origResult.setTrace(executionTrace);
        testChromosome.setLastExecutionResult(origResult);
        testChromosome.setChanged(false);


        TestChromosome testChromosome1 = new TestChromosome();
        ExecutionResult origResult1 = new ExecutionResult(new TestCase() {
            @Override
            public int getID() {
                return 1;
            }

            @Override
            public void accept(TestVisitor visitor) {

            }

            @Override
            public void addAssertions(TestCase other) {

            }

            @Override
            public void addCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void removeCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void addContractViolation(ContractViolation violation) {

            }

            @Override
            public VariableReference addStatement(Statement statement) {
                return null;
            }

            @Override
            public VariableReference addStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void addStatements(List<? extends Statement> statements) {

            }

            @Override
            public void chop(int length) {

            }

            @Override
            public int sliceFor(VariableReference var) {
                return 0;
            }

            @Override
            public void clearCoveredGoals() {

            }

            @Override
            public boolean contains(Statement statement) {
                return false;
            }

            @Override
            public TestCase clone() {
                return null;
            }

            @Override
            public Set<Class<?>> getAccessedClasses() {
                return null;
            }

            @Override
            public AccessedEnvironment getAccessedEnvironment() {
                return null;
            }

            @Override
            public List<Assertion> getAssertions() {
                return null;
            }

            @Override
            public Set<ContractViolation> getContractViolations() {
                return null;
            }

            @Override
            public Set<TestFitnessFunction> getCoveredGoals() {
                return null;
            }

            @Override
            public Set<Class<?>> getDeclaredExceptions() {
                return null;
            }

            @Override
            public Set<VariableReference> getDependencies(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Object getObject(VariableReference reference, Scope scope) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(int position) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(Type type, int position) {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject() {
                return null;
            }

            @Override
            public VariableReference getRandomObject(int position) {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Set<VariableReference> getReferences(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getReturnValue(int position) {
                return null;
            }

            @Override
            public Statement getStatement(int position) {
                return null;
            }

            @Override
            public boolean hasStatement(int position) {
                return false;
            }

            @Override
            public boolean hasAssertions() {
                return false;
            }

            @Override
            public boolean hasCastableObject(Type type) {
                return false;
            }

            @Override
            public boolean hasObject(Type type, int position) {
                return false;
            }

            @Override
            public boolean hasReferences(VariableReference var) {
                return false;
            }

            @Override
            public boolean isAccessible() {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean isFailing() {
                return false;
            }

            @Override
            public void setFailing() {

            }

            @Override
            public boolean isGoalCovered(TestFitnessFunction goal) {
                return false;
            }

            @Override
            public boolean isPrefix(TestCase t) {
                return false;
            }

            @Override
            public boolean isUnstable() {
                return false;
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public void remove(int position) {

            }

            @Override
            public void removeAssertion(Assertion assertion) {

            }

            @Override
            public void removeAssertions() {

            }

            @Override
            public void replace(VariableReference var1, VariableReference var2) {

            }

            @Override
            public VariableReference setStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void setUnstable(boolean unstable) {

            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public int sizeWithAssertions() {
                return 0;
            }

            @Override
            public String toCode() {
                return null;
            }

            @Override
            public String toCode(Map<Integer, Throwable> exceptions) {
                return null;
            }

            @Override
            public Iterator<Statement> iterator() {
                return null;
            }

            @Override
            public void addListener(Listener<Void> listener) {

            }

            @Override
            public void deleteListener(Listener<Void> listener) {

            }
        });
        ExecutionTraceImpl executionTraceImpl1 = new ExecutionTraceImpl();
        executionTraceImpl1.setListOfFeatureMap(getFeature2());
        ExecutionTrace executionTrace1 = new ExecutionTraceProxy(executionTraceImpl1);
        origResult1.setTrace(executionTrace1);
        testChromosome1.setLastExecutionResult(origResult1);
        testChromosome1.setChanged(false);

        TestChromosome testChromosome2 = new TestChromosome();
        ExecutionResult origResult2 = new ExecutionResult(new TestCase() {
            @Override
            public int getID() {
                return 1;
            }

            @Override
            public void accept(TestVisitor visitor) {

            }

            @Override
            public void addAssertions(TestCase other) {

            }

            @Override
            public void addCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void removeCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void addContractViolation(ContractViolation violation) {

            }

            @Override
            public VariableReference addStatement(Statement statement) {
                return null;
            }

            @Override
            public VariableReference addStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void addStatements(List<? extends Statement> statements) {

            }

            @Override
            public void chop(int length) {

            }

            @Override
            public int sliceFor(VariableReference var) {
                return 0;
            }

            @Override
            public void clearCoveredGoals() {

            }

            @Override
            public boolean contains(Statement statement) {
                return false;
            }

            @Override
            public TestCase clone() {
                return null;
            }

            @Override
            public Set<Class<?>> getAccessedClasses() {
                return null;
            }

            @Override
            public AccessedEnvironment getAccessedEnvironment() {
                return null;
            }

            @Override
            public List<Assertion> getAssertions() {
                return null;
            }

            @Override
            public Set<ContractViolation> getContractViolations() {
                return null;
            }

            @Override
            public Set<TestFitnessFunction> getCoveredGoals() {
                return null;
            }

            @Override
            public Set<Class<?>> getDeclaredExceptions() {
                return null;
            }

            @Override
            public Set<VariableReference> getDependencies(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Object getObject(VariableReference reference, Scope scope) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(int position) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(Type type, int position) {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject() {
                return null;
            }

            @Override
            public VariableReference getRandomObject(int position) {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Set<VariableReference> getReferences(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getReturnValue(int position) {
                return null;
            }

            @Override
            public Statement getStatement(int position) {
                return null;
            }

            @Override
            public boolean hasStatement(int position) {
                return false;
            }

            @Override
            public boolean hasAssertions() {
                return false;
            }

            @Override
            public boolean hasCastableObject(Type type) {
                return false;
            }

            @Override
            public boolean hasObject(Type type, int position) {
                return false;
            }

            @Override
            public boolean hasReferences(VariableReference var) {
                return false;
            }

            @Override
            public boolean isAccessible() {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean isFailing() {
                return false;
            }

            @Override
            public void setFailing() {

            }

            @Override
            public boolean isGoalCovered(TestFitnessFunction goal) {
                return false;
            }

            @Override
            public boolean isPrefix(TestCase t) {
                return false;
            }

            @Override
            public boolean isUnstable() {
                return false;
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public void remove(int position) {

            }

            @Override
            public void removeAssertion(Assertion assertion) {

            }

            @Override
            public void removeAssertions() {

            }

            @Override
            public void replace(VariableReference var1, VariableReference var2) {

            }

            @Override
            public VariableReference setStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void setUnstable(boolean unstable) {

            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public int sizeWithAssertions() {
                return 0;
            }

            @Override
            public String toCode() {
                return null;
            }

            @Override
            public String toCode(Map<Integer, Throwable> exceptions) {
                return null;
            }

            @Override
            public Iterator<Statement> iterator() {
                return null;
            }

            @Override
            public void addListener(Listener<Void> listener) {

            }

            @Override
            public void deleteListener(Listener<Void> listener) {

            }
        });
        ExecutionTraceImpl executionTraceImpl2 = new ExecutionTraceImpl();
        executionTraceImpl2.setListOfFeatureMap(getFeature3());
        ExecutionTrace executionTrace2 = new ExecutionTraceProxy(executionTraceImpl2);
        origResult2.setTrace(executionTrace2);
        testChromosome2.setLastExecutionResult(origResult2);
        testChromosome2.setChanged(false);

        TestChromosome testChromosome3 = new TestChromosome();
        ExecutionResult origResult3 = new ExecutionResult(new TestCase() {
            @Override
            public int getID() {
                return 1;
            }

            @Override
            public void accept(TestVisitor visitor) {

            }

            @Override
            public void addAssertions(TestCase other) {

            }

            @Override
            public void addCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void removeCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void addContractViolation(ContractViolation violation) {

            }

            @Override
            public VariableReference addStatement(Statement statement) {
                return null;
            }

            @Override
            public VariableReference addStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void addStatements(List<? extends Statement> statements) {

            }

            @Override
            public void chop(int length) {

            }

            @Override
            public int sliceFor(VariableReference var) {
                return 0;
            }

            @Override
            public void clearCoveredGoals() {

            }

            @Override
            public boolean contains(Statement statement) {
                return false;
            }

            @Override
            public TestCase clone() {
                return null;
            }

            @Override
            public Set<Class<?>> getAccessedClasses() {
                return null;
            }

            @Override
            public AccessedEnvironment getAccessedEnvironment() {
                return null;
            }

            @Override
            public List<Assertion> getAssertions() {
                return null;
            }

            @Override
            public Set<ContractViolation> getContractViolations() {
                return null;
            }

            @Override
            public Set<TestFitnessFunction> getCoveredGoals() {
                return null;
            }

            @Override
            public Set<Class<?>> getDeclaredExceptions() {
                return null;
            }

            @Override
            public Set<VariableReference> getDependencies(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Object getObject(VariableReference reference, Scope scope) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(int position) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(Type type, int position) {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject() {
                return null;
            }

            @Override
            public VariableReference getRandomObject(int position) {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Set<VariableReference> getReferences(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getReturnValue(int position) {
                return null;
            }

            @Override
            public Statement getStatement(int position) {
                return null;
            }

            @Override
            public boolean hasStatement(int position) {
                return false;
            }

            @Override
            public boolean hasAssertions() {
                return false;
            }

            @Override
            public boolean hasCastableObject(Type type) {
                return false;
            }

            @Override
            public boolean hasObject(Type type, int position) {
                return false;
            }

            @Override
            public boolean hasReferences(VariableReference var) {
                return false;
            }

            @Override
            public boolean isAccessible() {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean isFailing() {
                return false;
            }

            @Override
            public void setFailing() {

            }

            @Override
            public boolean isGoalCovered(TestFitnessFunction goal) {
                return false;
            }

            @Override
            public boolean isPrefix(TestCase t) {
                return false;
            }

            @Override
            public boolean isUnstable() {
                return false;
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public void remove(int position) {

            }

            @Override
            public void removeAssertion(Assertion assertion) {

            }

            @Override
            public void removeAssertions() {

            }

            @Override
            public void replace(VariableReference var1, VariableReference var2) {

            }

            @Override
            public VariableReference setStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void setUnstable(boolean unstable) {

            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public int sizeWithAssertions() {
                return 0;
            }

            @Override
            public String toCode() {
                return null;
            }

            @Override
            public String toCode(Map<Integer, Throwable> exceptions) {
                return null;
            }

            @Override
            public Iterator<Statement> iterator() {
                return null;
            }

            @Override
            public void addListener(Listener<Void> listener) {

            }

            @Override
            public void deleteListener(Listener<Void> listener) {

            }
        });
        ExecutionTraceImpl executionTraceImpl3 = new ExecutionTraceImpl();
        executionTraceImpl3.setListOfFeatureMap(getFeature4());
        ExecutionTrace executionTrace3 = new ExecutionTraceProxy(executionTraceImpl3);
        origResult3.setTrace(executionTrace3);
        testChromosome3.setLastExecutionResult(origResult3);
        testChromosome3.setChanged(false);

        TestChromosome testChromosome4 = new TestChromosome();
        ExecutionResult origResult4 = new ExecutionResult(new TestCase() {
            @Override
            public int getID() {
                return 1;
            }

            @Override
            public void accept(TestVisitor visitor) {

            }

            @Override
            public void addAssertions(TestCase other) {

            }

            @Override
            public void addCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void removeCoveredGoal(TestFitnessFunction goal) {

            }

            @Override
            public void addContractViolation(ContractViolation violation) {

            }

            @Override
            public VariableReference addStatement(Statement statement) {
                return null;
            }

            @Override
            public VariableReference addStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void addStatements(List<? extends Statement> statements) {

            }

            @Override
            public void chop(int length) {

            }

            @Override
            public int sliceFor(VariableReference var) {
                return 0;
            }

            @Override
            public void clearCoveredGoals() {

            }

            @Override
            public boolean contains(Statement statement) {
                return false;
            }

            @Override
            public TestCase clone() {
                return null;
            }

            @Override
            public Set<Class<?>> getAccessedClasses() {
                return null;
            }

            @Override
            public AccessedEnvironment getAccessedEnvironment() {
                return null;
            }

            @Override
            public List<Assertion> getAssertions() {
                return null;
            }

            @Override
            public Set<ContractViolation> getContractViolations() {
                return null;
            }

            @Override
            public Set<TestFitnessFunction> getCoveredGoals() {
                return null;
            }

            @Override
            public Set<Class<?>> getDeclaredExceptions() {
                return null;
            }

            @Override
            public Set<VariableReference> getDependencies(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getLastObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Object getObject(VariableReference reference, Scope scope) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(int position) {
                return null;
            }

            @Override
            public List<VariableReference> getObjects(Type type, int position) {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomNonNullObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject() {
                return null;
            }

            @Override
            public VariableReference getRandomObject(int position) {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type) throws ConstructionFailedException {
                return null;
            }

            @Override
            public VariableReference getRandomObject(Type type, int position) throws ConstructionFailedException {
                return null;
            }

            @Override
            public Set<VariableReference> getReferences(VariableReference var) {
                return null;
            }

            @Override
            public VariableReference getReturnValue(int position) {
                return null;
            }

            @Override
            public Statement getStatement(int position) {
                return null;
            }

            @Override
            public boolean hasStatement(int position) {
                return false;
            }

            @Override
            public boolean hasAssertions() {
                return false;
            }

            @Override
            public boolean hasCastableObject(Type type) {
                return false;
            }

            @Override
            public boolean hasObject(Type type, int position) {
                return false;
            }

            @Override
            public boolean hasReferences(VariableReference var) {
                return false;
            }

            @Override
            public boolean isAccessible() {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean isFailing() {
                return false;
            }

            @Override
            public void setFailing() {

            }

            @Override
            public boolean isGoalCovered(TestFitnessFunction goal) {
                return false;
            }

            @Override
            public boolean isPrefix(TestCase t) {
                return false;
            }

            @Override
            public boolean isUnstable() {
                return false;
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public void remove(int position) {

            }

            @Override
            public void removeAssertion(Assertion assertion) {

            }

            @Override
            public void removeAssertions() {

            }

            @Override
            public void replace(VariableReference var1, VariableReference var2) {

            }

            @Override
            public VariableReference setStatement(Statement statement, int position) {
                return null;
            }

            @Override
            public void setUnstable(boolean unstable) {

            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public int sizeWithAssertions() {
                return 0;
            }

            @Override
            public String toCode() {
                return null;
            }

            @Override
            public String toCode(Map<Integer, Throwable> exceptions) {
                return null;
            }

            @Override
            public Iterator<Statement> iterator() {
                return null;
            }

            @Override
            public void addListener(Listener<Void> listener) {

            }

            @Override
            public void deleteListener(Listener<Void> listener) {

            }
        });
        ExecutionTraceImpl executionTraceImpl4 = new ExecutionTraceImpl();
        executionTraceImpl4.setListOfFeatureMap(getFeature5());
        ExecutionTrace executionTrace4 = new ExecutionTraceProxy(executionTraceImpl4);
        origResult4.setTrace(executionTrace4);
        testChromosome4.setLastExecutionResult(origResult4);
        testChromosome4.setChanged(false);

        // setup
        //FeatureFactory.registerAsFeature("x", "foo");
        FeatureFactory.registerAsFeature("y", "foo");


        Collection<TestChromosome> population = new ArrayList<>();
        population.add(testChromosome);
        population.add(testChromosome1);
        population.add(testChromosome2);
        population.add(testChromosome3);
        population.add(testChromosome4);

        List<String> uncoveredMethod = new ArrayList<>();
        uncoveredMethod.add("foo");uncoveredMethod.add("boo");
        Properties.NOVELTY_THRESHOLD =2;// diabling the archive
        Properties.MAX_NOVELTY_ARCHIVE_SIZE = 2;
        noveltyFunction.calculateNovelty(population, new ArrayDeque<TestChromosome>(), uncoveredMethod, true);

        for(TestChromosome c : population){
            System.out.println("Novelty Score : "+c.getNoveltyScore());
        }

        System.out.println(" --- Features --- ");
        for(TestChromosome c : population){
            for(Map<Integer, Feature> map : c.getLastExecutionResult().getTrace().getListOfFeatureMap()){
                for (Map.Entry<Integer, Feature> entry : map.entrySet()){
                    System.out.println("Feature : "+ entry.getValue().getVariableName() + " Normalized Value : " + entry.getValue().getNormalizedValue());
                }
            }
            System.out.println(" --- ");
        }
    }
}

class Foo {
    int j;
    private static int special = 12;
    long someL = 12l;
    private Boo boo;
    int[] someArr;
    private boolean someFlag;

    public Foo(int num, int[] arr, Boo boo) {
        j = num;
        this.someArr = arr;
        this.boo = boo;
    }

    public Foo() {

    }

    boolean getSomething() {
        if (boo.checkEquals() && someArr[4]==4) {
            return true;
        } else {
            return false;
        }
    }
}

class Boo {
    private boolean result;
    private int input;
    private int input2;

    Boo(boolean res, int i, int j) {
        this.result = res;
        input = i;
        input2 = j;
    }

    public boolean checkEquals() {
        if (input == 2) {
            return true;
        } else
            return false;
    }
}
