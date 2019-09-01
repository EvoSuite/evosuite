package org.evosuite.coverage.aes.branch;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import org.evosuite.coverage.aes.AbstractAESCoverageSuiteFitness;
import org.evosuite.coverage.aes.Spectrum;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;


public class AESBranchCoverageSuiteFitness extends AbstractAESCoverageSuiteFitness {

    private static final long serialVersionUID = 7409239464436681146L;
    /* A branch can be of 3 types: 1) true branch (stored in the "trueMap"), 2) false branch (stored in the "falseMap") and 3) a method not containing a branch
    (stored in the "branchlessMethodMap") */
    private Map<Integer, Integer> trueMap;
    private Map<Integer, Integer> falseMap;
    private Map<String, Integer> branchlessMethodsMap;

    /* This map parses the JSON object. And maps the string <classname+"."+method_name> to the suspiciousness score*/
    private Map<String, Double> suspiciousnesScores;
    /*This map stores the weights of each method (likelihood). This is done by mapping the component number of each branch to its corresponding suspiciousness score
    found in the "suspiciousnessScores" map. The idea is we first find the method to which the branch belongs to. Then we make the string in the format
    <classname+"."+method_name> and  do a look up in the "suspiciousnessScores" */
    private Map<Integer, Double> weights;
    /* mode signified whether we are using the priors or not. If set to false then all the components have equal chance of being faulty */
    private boolean mode;
    private int numberOfGoals = 0;
    /* We want to parse the JSON object once. This variable value if 0 we parse the json object and increment it by 1. So that we don't end up parsing again */
    private static int count = 0;
    private double sumWeights = -1d;
    private static double otherWeight = 0d;

    public AESBranchCoverageSuiteFitness(Metric metric) {
        super(metric);
    }

    public AESBranchCoverageSuiteFitness() {
        this(Metric.AES);
    }


    private void determineCoverageGoals() {

        if (this.branchlessMethodsMap == null || this.trueMap == null || this.falseMap == null) {
            this.branchlessMethodsMap = new HashMap<String, Integer>();
            this.trueMap = new HashMap<Integer, Integer>();
            this.falseMap = new HashMap<Integer, Integer>();

            /* list "goals" contains all the branches that has been covered by the current generation of test suites */
            List<TestFitnessFunction> goals = new AESBranchCoverageFactory().getCoverageGoals();
            this.numberOfGoals = goals.size() - 1;

            /* This variable "g" is the component number */
            for (int g = 0; g < this.numberOfGoals; g++) {
                TestFitnessFunction ff = goals.get(g);

                if (ff instanceof BranchCoverageTestFitness) {
                    BranchCoverageTestFitness goal = (BranchCoverageTestFitness) ff;

                    /* For each of the goal object, which is essentially a branch covered, we must map it to the prior.
                    "branchToSuspiciousnessMap" takes care of that.*/
                    branchToSuspiciousnessMap(goal, g);
                    if (goal.getBranch() == null) { // branchless method{
                        branchlessMethodsMap.put(goal.getClassName() + "." + goal.getMethod(), g);
                    } else if (goal.getBranchExpressionValue()) { // true branch
                        trueMap.put(goal.getBranch().getActualBranchId(), g);
                    } else { // false branch
                        falseMap.put(goal.getBranch().getActualBranchId(), g);
                    }
                }
            }
        }
    }


    @Override
    protected Spectrum getSpectrum(List<ExecutionResult> results) {

       /* If count is 0, getSpectrum is getting called for the first time. We parse the JSON object now. This is done by first extracting the location of the JSON file
       that has been stored as an environment variable "PRIOR_VAL". And then calling the "extract_data" function.*/
        if (count == 0) {
            extract_data(System.getenv("PRIOR_LOC"));
        }
        count++;
        determineCoverageGoals();
        Spectrum spectrum = new Spectrum(results.size(), this.numberOfGoals);

        for (int t = 0; t < results.size(); t++) {
            ExecutionResult result = results.get(t);

            for (String method : result.getTrace().getCoveredMethods()) {
                if (branchlessMethodsMap.containsKey(method)) {
                    spectrum.setInvolved(t, branchlessMethodsMap.get(method));
                }
            }

            for (int trueBranchId : result.getTrace().getCoveredTrueBranches()) {
                if (trueMap.containsKey(trueBranchId)) {
                    spectrum.setInvolved(t, trueMap.get(trueBranchId));
                }
            }

            for (int falseBranchId : result.getTrace().getCoveredFalseBranches()) {
                if (falseMap.containsKey(falseBranchId)) {
                    spectrum.setInvolved(t, falseMap.get(falseBranchId));
                }
            }

        }

        return spectrum;
    }

    /* extract_data uses an external library to parse the JSON object.


<dependencies>
    <dependency>
        <groupId>com.googlecode.json-simple</groupId>
        <artifactId>json-simple</artifactId>
        <version>1.1.1</version>
    </dependency>
</dependencies>

    IMPORTANT:: Above mentioned code has been added to the POM file handle the dependency. This piece of code must be placed as a direct child of <project> tag. If it is placed
    under <dependencyManagement>, it will not work.


    The libraries required have been added using "import" at the top of the file
    Please refer to online documents to resolve any queries on how the parsing is done. */

    private void extract_data(String filename) {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(filename);
            mode = true;
            Object obj = jsonParser.parse(reader);
            JSONObject jobj = (JSONObject) obj;
            parseJsonObject(jobj);

        } catch (IOException e) {
            mode = false;
            return;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void parseJsonObject(JSONObject jo) {
        if (suspiciousnesScores == null)
            suspiciousnesScores = new HashMap<>();

        JSONArray javaArray = (JSONArray) jo.get("children");
        for (int i = 0; i < javaArray.size(); i++) {
            JSONObject javaObject = (JSONObject) javaArray.get(i);
            JSONArray classArray = (JSONArray) javaObject.get("children");
            for (int j = 0; j < classArray.size(); j++) {
                JSONObject classObject = (JSONObject) classArray.get(j);
                String className = (String) classObject.get("name");


                JSONArray methodArray = (JSONArray) classObject.get("children");
                for (int k = 0; k < methodArray.size(); k++) {
                    JSONObject methodObject = (JSONObject) methodArray.get(k);
                    suspiciousnesScores.put(className + "." + (String) methodObject.get("name"), Double.valueOf((String) methodObject.get("prob")));

                }
            }
        }

    }


    /* This function maps the prior to the branches */

    private void branchToSuspiciousnessMap(BranchCoverageTestFitness A, int component_no) {
        //uniform distribution
        if (!mode)
            return;

        //initialise weight map
        if (weights == null)
            weights = new HashMap<>();                    //mycode Integer=component_number, Double=Suspiciousness


        /* We first extract the method and class name from the goal object */
        String method_name = A.getMethod();
        String class_name = A.getClassName();

        /* Modify the string such that it follows the <classname+"."+method_name> format */
        String method_final = method_name.substring(0, method_name.indexOf('('));
        String class_final = class_name.substring(class_name.lastIndexOf('.') + 1);

        /* If the method name is <init>, it is a constructor. So we rename it to the classname */
        if (method_final.contains("<init>"))
            method_final = class_final;

        /* We make the lookup in the "suspiciousnesScores" map  to get the corresponding prior value of the branch */
        Double temp = suspiciousnesScores.get(class_final + "." + method_final);
        /* If there doesn't exist an entry then we give it a very small prior val. The weights map uses <component_no, prior_val> key-value pair. */
        if (temp == null) {
            weights.put(component_no, 0.0000001);
        } else {
            weights.put(component_no, temp);
        }

    }

    /* This method that will be called during computation of FF4, to get the weights. Remember if mode is false (not using prior) then weights map is null.
    We must put a check in the calling method to know if priors are being used or not. */
    protected Map<Integer, Double> getWeights() {
        return weights;
    }

    protected double getSumWeights() {
        //return sumWeights;
        return 0d;
    }

    //temp
//    private void printmyhashmap(Map<Integer, Double> A) {
//
//        if(A == null)
//            return;
//
//        BufferedWriter out = null;
//        for (Map.Entry<Integer, Double> entry : A.entrySet()) {
//            try {
//
//                String str = String.valueOf(entry.getKey()) + "," + String.valueOf(entry.getValue()) + "," + String.valueOf(sumWeights) + "\n";
//                out = new BufferedWriter(
//                        new FileWriter("/tmp/weights.csv", true));
//                out.write(str);
//                out.close();
//            } catch (IOException e) {
//                System.out.println("exception occoured" + e);
//            }
//        }
//
//
//    }
}


