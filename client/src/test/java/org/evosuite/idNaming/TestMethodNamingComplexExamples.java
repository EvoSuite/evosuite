package org.evosuite.idNaming;

import static org.evosuite.coverage.io.IOCoverageConstants.ARRAY_EMPTY;
import static org.evosuite.coverage.io.IOCoverageConstants.ARRAY_NONEMPTY;
import static org.evosuite.coverage.io.IOCoverageConstants.NUM_NEGATIVE;
import static org.evosuite.coverage.io.IOCoverageConstants.NUM_POSITIVE;
import static org.evosuite.coverage.io.IOCoverageConstants.NUM_ZERO;
import static org.evosuite.coverage.io.IOCoverageConstants.REF_NONNULL;
import static org.evosuite.coverage.io.IOCoverageConstants.REF_NULL;
import static org.evosuite.coverage.io.IOCoverageConstants.STRING_NONEMPTY;
import static org.junit.Assert.assertEquals;

import com.examples.with.different.packagename.generic.GenericClassTwoParameters;
import com.googlecode.gentyref.TypeToken;
import org.evosuite.TestGenerationContext;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.io.input.InputCoverageGoal;
import org.evosuite.coverage.io.input.InputCoverageTestFitness;
import org.evosuite.coverage.io.output.OutputCoverageGoal;
import org.evosuite.coverage.io.output.OutputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.JumpInsnNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmr on 31/07/15.
 *
 * TODO: These tests need more work, this is currently rather a mess.
 *
 */
public class TestMethodNamingComplexExamples {

    @Test
    public void testIDNamingStandalone() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

        // Branch goals for branchless methods (root branches)
        TestFitnessFunction goal1 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","keys()[I", -1));
        TestFitnessFunction goal2 = new BranchCoverageTestFitness(new BranchCoverageGoal(null, true, "com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V", -1));
      //  TestFitnessFunction goal3 = new BranchCoverageTestFitness(new BranchCoverageGoal(null, true, "com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values()[B", -1));

        // Branch instruction in method isPositive(I)Z
        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);

       
         // Output goals for method keys()I
        TestFitnessFunction goal8 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()", Type.INT_TYPE, NUM_POSITIVE));
        TestFitnessFunction goal9 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()", Type.INT_TYPE, NUM_NEGATIVE));

        // Method goals for all methods in the class
        TestFitnessFunction goal10 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V");
        TestFitnessFunction goal11 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()");
        TestFitnessFunction goal12 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "mist(I)V");
      //  TestFitnessFunction goal13 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "bar(I)V");
     //   TestFitnessFunction goal14 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "foo(I)V");

        // Overloaded methods become different method goals (difference in the number (or type!) of arguments)
       // TestFitnessFunction goal15 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "foo(Z)V");
       // TestFitnessFunction goal16 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "foo(IIII)V");
  //      TestFitnessFunction goal3 = new InputCoverageTestFitness(new InputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()",-1,"I","nonempty"));
        TestFitnessFunction goal4 = new InputCoverageTestFitness(new InputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V",0,Type.getType("Ljava.lang.Object;"), REF_NONNULL));

        
        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,42)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal10);
        test1.addCoveredGoal(goal11);
        test1.addCoveredGoal(goal12);
        test1.addCoveredGoal(goal4);
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);


        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"foo")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal10);
        test2.addCoveredGoal(goal11);
        test2.addCoveredGoal(goal8);
        test2.addCoveredGoal(goal4);
        test2.addCoveredGoal(goal1);
        test2.addCoveredGoal(goal2);
        
        DefaultTestCase test3 = new DefaultTestCase();
        test3.addStatement(new StringPrimitiveStatement(test3,"foo1")); // any statement to fool hashcode function
        test3.addCoveredGoal(goal10);
        test3.addCoveredGoal(goal11);
        test3.addCoveredGoal(goal9);
        test3.addCoveredGoal(goal4);
        test3.addCoveredGoal(goal1);
        test3.addCoveredGoal(goal2);

        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.add(test1);
        testCases.add(test2);
        testCases.add(test3);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
        String nameTest1 = naming.getName(test1);
        String nameTest2 = naming.getName(test2);
        String nameTest3 = naming.getName(test3);

        assertEquals("Generated test name differs from expected", "testMist", nameTest1);
        assertEquals("Generated test name differs from expected", "testKeysReturningPositive", nameTest2);
        assertEquals("Generated test name differs from expected", "testKeysReturningNegative", nameTest3);
    }

    @Test
    public void testIDNamingWithDifferentArguments() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
        TestFitnessFunction goal1 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","values([B)[B");
        TestFitnessFunction goal2 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V");
        TestFitnessFunction goal3 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","values([I)[B");

        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);
        TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values([B)",Type.getType("[B"), ARRAY_EMPTY));
        TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values([I)",Type.getType("[B"),ARRAY_NONEMPTY));


        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,11)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);
        test1.addCoveredGoal(goal4);

        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"barfoo")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal3);
        test2.addCoveredGoal(goal2);
        test2.addCoveredGoal(goal5);
        ArrayList<TestCase> testCases = new ArrayList<>();
        testCases.add(test1);
        testCases.add(test2);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
        String nameTest1 = naming.getName(test1);
        String nameTest2 = naming.getName(test2);
        assertEquals("Generated test name differs from expected", "testValuesTakingByteArray", nameTest1);
        assertEquals("Generated test name differs from expected", "testValuesTakingIntArray", nameTest2);
    }

    @Test
    public void testIDNamingWithDifferentArguments2() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
        TestFitnessFunction goal1 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","values([B)[B");
        TestFitnessFunction goal2 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V");
        TestFitnessFunction goal3 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","values()[B");

        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);
        TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values([B)",Type.getType("[B"), ARRAY_EMPTY));
        TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values()",Type.getType("[B"), ARRAY_NONEMPTY));


        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,11)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);
        test1.addCoveredGoal(goal4);

        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"barfoo")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal3);
        test2.addCoveredGoal(goal2);
        test2.addCoveredGoal(goal5);
        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.add(test1);
        testCases.add(test2);


        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
        String nameTest1 = naming.getName(test1);
        String nameTest2 = naming.getName(test2);
        assertEquals("Generated test name differs from expected", "testValuesTakingByteArray", nameTest1);
        assertEquals("Generated test name differs from expected", "testValuesTakingNoArguments", nameTest2);
    }

    @Test
    public void testIdNamingWithSameMethodGoals() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
        TestFitnessFunction goal1 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","keys()[I");
        TestFitnessFunction goal2 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V");
        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);
        TestFitnessFunction goal3 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()",Type.INT_TYPE, NUM_NEGATIVE));
        TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()",Type.INT_TYPE, NUM_POSITIVE));

        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,42)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);
        test1.addCoveredGoal(goal3);

        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"foo")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal1);
        test2.addCoveredGoal(goal2);
        test2.addCoveredGoal(goal4);


        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.add(test1);
        testCases.add(test2);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
        String nameTest1 = naming.getName(test1);
        String nameTest2 = naming.getName(test2);

        assertEquals("Generated test name differs from expected", "testKeysTakingNoArgumentsReturningNegative", nameTest1);
        assertEquals("Generated test name differs from expected", "testKeysTakingNoArgumentsReturningPositive", nameTest2);
    }

    @Test
    public void testIDNamingWithConstructor() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
        TestFitnessFunction goal2 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V");

        TestFitnessFunction goal1 = new ExceptionCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V", ArrayIndexOutOfBoundsException.class, ExceptionCoverageTestFitness.ExceptionType.IMPLICIT);

        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);
        TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values([B)",Type.getType("[B"), ARRAY_EMPTY));
        TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values()",Type.getType("[B"), ARRAY_NONEMPTY));


        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,11)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal2);

        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"bar")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal2);
        test2.addCoveredGoal(goal1);

        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.add(test1);
        testCases.add(test2);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
        String nameTest1 = naming.getName(test1);
        String nameTest2 = naming.getName(test2);

        assertEquals("Generated test name differs from expected", "testCreatesTUnmodifiableIntByteMap", nameTest1);
        assertEquals("Generated test name differs from expected", "testFailsToCreateTUnmodifiableIntByteMapThrowsArrayIndexOutOfBoundsException", nameTest2);
    }

    @Ignore // Not implemented yet
    @Test
    public void testIDNamingWithAssertions() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
        TestFitnessFunction goal1 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","values([B)[B");

        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);
        TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values([B)",Type.getType("[B"), ARRAY_EMPTY));
        TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values()",Type.getType("[B"), ARRAY_NONEMPTY));

        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,11)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal4);
        test1.addCoveredGoal(goal5);

        Class<?> targetClass = com.examples.with.different.packagename.generic.GenericClassTwoParameters.class;
        Method creatorMethod = targetClass.getMethod("create", new Class<?>[] {});
        Method targetMethod = targetClass.getMethod("get", new Class<?>[] { Object.class });
        Method inspectorMethod = targetClass.getMethod("testMe", new Class<?>[] {});
        Constructor<?> intConst = Integer.class.getConstructor(new Class<?>[] { int.class });

        GenericClass listOfInteger = new GenericClass(new TypeToken<GenericClassTwoParameters<Integer, Integer>>() {}.getType());
        GenericMethod genericCreatorMethod = new GenericMethod(creatorMethod, targetClass).getGenericInstantiationFromReturnValue(listOfInteger);
        System.out.println(genericCreatorMethod.getGeneratedClass().toString());
        GenericMethod genericMethod = new GenericMethod(targetMethod, targetClass).copyWithNewOwner(genericCreatorMethod.getGeneratedClass());
        System.out.println(genericMethod.getGeneratedClass().toString());

        DefaultTestCase test2 = new DefaultTestCase();
        MethodStatement ms1 = new MethodStatement(test2, genericCreatorMethod, (VariableReference) null, new ArrayList<VariableReference>());
        test2.addStatement(ms1);

        IntPrimitiveStatement ps1 = (IntPrimitiveStatement) PrimitiveStatement.getPrimitiveStatement(test2, int.class);
        test2.addStatement(ps1);

        GenericConstructor intConstructor = new GenericConstructor(intConst, Integer.class);
        List<VariableReference> constParam = new ArrayList<VariableReference>();
        constParam.add(ps1.getReturnValue());
        ConstructorStatement cs1 = new ConstructorStatement(test2, intConstructor, constParam);
        //test.addStatement(cs1);

        List<VariableReference> callParam = new ArrayList<VariableReference>();
        callParam.add(ps1.getReturnValue());

        MethodStatement ms2 = new MethodStatement(test2, genericMethod, ms1.getReturnValue(), callParam);
        test2.addStatement(ms2);

        Inspector inspector = new Inspector(targetClass, inspectorMethod);
        Assertion assertion = new InspectorAssertion(inspector, ms2, ms1.getReturnValue(), 0);
        ms2.addAssertion(assertion);
        test2.addCoveredGoal(goal1);
        test2.addCoveredGoal(goal4);
        test2.addCoveredGoal(goal5);

        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.add(test1);
        testCases.add(test2);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
        String nameTest1 = naming.getName(test1);
        String nameTest2 = naming.getName(test2);
        System.out.println(nameTest1+nameTest2);
        assertEquals("Generated test name differs from expected", "test_Values", nameTest1);
        assertEquals("Generated test name differs from expected", "test_checksTestMe", nameTest2);
    }

    @Test
    public void testIDNamingWithDifferentArguments3() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
        TestFitnessFunction goal2 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V");

        TestFitnessFunction goal1 = new ExceptionCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V", ArrayIndexOutOfBoundsException.class ,ExceptionCoverageTestFitness.ExceptionType.IMPLICIT);

        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);
        TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values([B)",Type.getType("[B"), ARRAY_EMPTY));
        TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values()",Type.getType("[B"), ARRAY_NONEMPTY));


        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,11)); // any statement to fool hashcode function
        //test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);
        test1.addCoveredGoal(goal4);
        test1.addCoveredGoal(goal5);

        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"bar")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal1);
        test2.addCoveredGoal(goal2);
        test2.addCoveredGoal(goal4);
        test2.addCoveredGoal(goal5);
        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.add(test1);
        testCases.add(test2);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
        String nameTest1 = naming.getName(test1);
        String nameTest2 = naming.getName(test2);
        System.out.println(nameTest1+nameTest2);
        assertEquals("Generated test name differs from expected", "testCreatesTUnmodifiableIntByteMap", nameTest1);
        assertEquals("Generated test name differs from expected", "testFailsToCreateTUnmodifiableIntByteMapThrowsArrayIndexOutOfBoundsException", nameTest2);
    }

    @Test
    public void testIDNamingWithSameMethodGoals() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
        TestFitnessFunction goal1 = new MethodCoverageTestFitness("org.jdom2.DocType","<init>()V");
        TestFitnessFunction goal2 = new MethodCoverageTestFitness("org.jdom2.DocType","getPublicID()Ljava/lang/String");
        TestFitnessFunction goal3 = new MethodCoverageTestFitness("org.jdom2.DocType","setPublicID(Ljava/lang/String;)Lorg/jdom2/DocType");

        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);
        TestFitnessFunction goal4 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","getPublicID()Ljava/lang/String;", -1));
        TestFitnessFunction goal5 = new BranchCoverageTestFitness(new BranchCoverageGoal(br1,true,"org.jdom2.DocType","<init>()V", -1));
        TestFitnessFunction goal6 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","setPublicID(Ljava/lang/String;)Lorg/jdom2/DocType;", -1));

        TestFitnessFunction goal7 = new OutputCoverageTestFitness(new OutputCoverageGoal("org.jdom2.DocType", "getPublicID()",Type.getType("Ljava.lang.String;"), REF_NONNULL));
        TestFitnessFunction goal8 = new OutputCoverageTestFitness(new OutputCoverageGoal("org.jdom2.DocType", "setPublicID(Ljava/lang/String;)",Type.getType("Lorg/jdom2/DocType;"), REF_NONNULL));

        TestFitnessFunction goal9 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "setPublicID(Ljava/lang/String;)", 0, Type.getType("Ljava.lang.String;"), STRING_NONEMPTY));
        TestFitnessFunction goal10 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "setPublicID(Ljava/lang/String;)", 0, Type.getType("Ljava.lang.String;"), REF_NULL));

        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,42)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal5);
        test1.addCoveredGoal(goal2);


        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"foo")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal1);
        test2.addCoveredGoal(goal3);
        test2.addCoveredGoal(goal5);
        test2.addCoveredGoal(goal6);
        test2.addCoveredGoal(goal8);
        test2.addCoveredGoal(goal10);




        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.add(test1);
        testCases.add(test2);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
        String nameTest1 = naming.getName(test1);
        String nameTest2 = naming.getName(test2);

        assertEquals("Generated test name differs from expected", "testGetPublicID", nameTest1);
        assertEquals("Generated test name differs from expected", "testCreatesDocTypeAndCallsSetPublicIDTakingString", nameTest2);
    }

    @Test
    public void testIDNamingWithSameMethodGoals2() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
        TestFitnessFunction goal1 = new MethodCoverageTestFitness("org.jdom2.DocType","<init>(Lorg/apache/commons/math/Field;II)V");
        TestFitnessFunction goal2 = new MethodCoverageTestFitness("org.jdom2.DocType","<init>(Lorg/apache/commons/math/linear/FieldMatrix;)");

        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);
        TestFitnessFunction goal4 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/Field;II)V", -1));

        TestFitnessFunction goal7 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)V", 0, Type.getType("Ljava.lang.Object;"), REF_NONNULL));
        TestFitnessFunction goal8 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)V", 1, Type.INT_TYPE, NUM_ZERO));
        TestFitnessFunction goal9 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)V", 2, Type.INT_TYPE, NUM_ZERO));
        TestFitnessFunction goal3 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/linear/FieldMatrix;)", 0, Type.getType("Ljava.lang.Object;"), REF_NULL));
        TestFitnessFunction goal10 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)", 1, Type.INT_TYPE, NUM_NEGATIVE));
        TestFitnessFunction goal11 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)", 2, Type.INT_TYPE, NUM_POSITIVE));
        TestFitnessFunction goal12 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/linear/FieldMatrix;)",0, Type.getType("Ljava.lang.Object;"), REF_NONNULL));

        TestFitnessFunction goal5 = new ExceptionCoverageTestFitness("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)V", ArrayIndexOutOfBoundsException.class ,ExceptionCoverageTestFitness.ExceptionType.IMPLICIT);

        TestFitnessFunction goal31 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","getColumnDimension()I", -1));
        TestFitnessFunction goal32= new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","getRowDimension()I", -1));
        TestFitnessFunction goal33 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/Field;II)V", -1));
        TestFitnessFunction goal34 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","getEntry(II)Lorg/apache/commons/math/FieldElement;", -1));
        TestFitnessFunction goal35 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","computeKey(II)I", -1));
        TestFitnessFunction goal36 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/linear/FieldMatrix;)V", -1));
        TestFitnessFunction goal37 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/linear/FieldMatrix;)V", -1));
        TestFitnessFunction goal38 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/linear/FieldMatrix;)V", -1));
        TestFitnessFunction goal39 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","setEntry(IILorg/apache/commons/math/FieldElement;)V", -1));


        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,42)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal4);
        test1.addCoveredGoal(goal7);
        test1.addCoveredGoal(goal8);
        test1.addCoveredGoal(goal9);
        test1.addCoveredGoal(goal5);

        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"foo")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal2);
        test2.addCoveredGoal(goal3);
        test2.addCoveredGoal(goal5);

        DefaultTestCase test3 = new DefaultTestCase();
        test3.addStatement(new StringPrimitiveStatement(test3,"bar")); // any statement to fool hashcode function
        test3.addCoveredGoal(goal1);
        test3.addCoveredGoal(goal2);
        test3.addCoveredGoal(goal31);
        test3.addCoveredGoal(goal32);
        test3.addCoveredGoal(goal33);
        test3.addCoveredGoal(goal34);
        test3.addCoveredGoal(goal35);
        test3.addCoveredGoal(goal36);
        test3.addCoveredGoal(goal37);
        test3.addCoveredGoal(goal38);
        test3.addCoveredGoal(goal39);
        test3.addCoveredGoal(goal7);
        test3.addCoveredGoal(goal10);
        test3.addCoveredGoal(goal11);
        test3.addCoveredGoal(goal12);
        test3.addCoveredGoal(goal5);

        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.add(test1);
        testCases.add(test2);
        testCases.add(test3);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
        String nameTest1 = naming.getName(test1);
        String nameTest2 = naming.getName(test2);
        String nameTest3 = naming.getName(test3);

        assertEquals("Generated test name differs from expected", "testCreatesDocTypeTaking3ArgumentsWithZeroAndZero", nameTest1);
        assertEquals("Generated test name differs from expected", "testCreatesDocTypeTakingFieldMatrixWithNull", nameTest2);
        // TODO: Why is Positive vs nonnull nondeterministically chosen?
        assertEquals("Generated test name differs from expected", "testCreatesDocTypeTaking3ArgumentsWithNegative", nameTest3);
    }
}
