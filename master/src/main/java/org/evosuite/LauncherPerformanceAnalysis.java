package org.evosuite;

/**
 * @author Giovanni Grano
 * Utility class to lunch and test the implemented performance indicators
 */
public class LauncherPerformanceAnalysis {

    public static void main(String[] args) {

        String[] command = {
                "-generateMOSuite",
                "-Dcriterion=BRANCH",
                "-Dconfiguration_id=test",
                "-Dperformance_indicators=LOOP_COUNTER:METHOD_CALL:COVERED_METHOD_CALL:STATEMENTS_COUNTER:STATEMENTS_COVERED:OBJECTS_INSTANTIATIONS",
                // parameter for the strategy used for combining all the indicators
//                "-Dperformance_strategy=PREFERENCE_CRITERION",
                "-Dperformance_strategy=CROWDING_DISTANCE",
                "-Dperformance_combination_strategy=MIN_MAX", //MIN_MAX, SUM
                "-Djunit_check=false",
                "-Dminimize=FALSE",
                "-Dpopulation=50",
                "-Dalgorithm=PDMOSA",
//                "-Dalgorithm=DYNAMOSA",
                "-Dcoverage=TRUE",
                "-Dsandbox=TRUE",
                "-Dassertions=FALSE",
                "-Dsearch_budget=60",
                // if true, allows you to debug the client part (executed on the same JVM)
                "-Dclient_on_thread=false",
                "-Doutput_variables=TARGET_CLASS,criterion,configuration_id,algorithm,Total_Goals,Covered_Goals," +
                        "Generations,Statements_Executed,Fitness_Evaluations,Tests_Executed,Generations,Total_Time," +
                        "Size,Length,BranchCoverage,MethodCall,CoveredMethodCall," +
                        "ObjectsInstantiations,StatementCounter,StatementCovered,LoopCounter,TestExecutionTime",
                "-projectCP",
                "/Users/grano/IdeaProjects/evo_performance/Dataset/projects/3_gson/gson-2.8.1-SNAPSHOT.jar",
                "-class",
                "com.google.gson.stream.JsonReader"
        };

        EvoSuite.main(command);
    }
}