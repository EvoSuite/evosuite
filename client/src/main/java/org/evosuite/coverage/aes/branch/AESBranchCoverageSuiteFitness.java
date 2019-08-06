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

    private Map<Integer, Integer> trueMap;
    private Map<Integer, Integer> falseMap;
    private Map<String, Integer> branchlessMethodsMap;
    //    private Map<Integer, BranchDetails> branchToMethodMap;  //mycode Integer=component_number
    //private Map<String, Double> suspiciousnesScores;         //mycode String=classname+"."+method_name, Double = Suspiciousness_score, contains the liklihood values
    //private Map<Integer, Double> weights;                    //mycode Integer=component_number, Double=Suspiciousness, contains the liklihood values of the components
    //private boolean mode;               //mode = false => uniform distribution
    private int numberOfGoals = 0;
    //private static int count = 0;
    //private double sumWeights = -1d;
    //private static double otherWeight = 0d;

    public AESBranchCoverageSuiteFitness(Metric metric) {
        super(metric);
    }

    public AESBranchCoverageSuiteFitness() {
        this(Metric.AES);
    }

    //modified
    private void determineCoverageGoals() {

        if (this.branchlessMethodsMap == null || this.trueMap == null || this.falseMap == null) {
            this.branchlessMethodsMap = new HashMap<String, Integer>();
            this.trueMap = new HashMap<Integer, Integer>();
            this.falseMap = new HashMap<Integer, Integer>();
//            this.branchToMethodMap = new HashMap<Integer, BranchDetails>();

            List<TestFitnessFunction> goals = new AESBranchCoverageFactory().getCoverageGoals();
            this.numberOfGoals = goals.size() - 1;

            for (int g = 0; g < this.numberOfGoals; g++) {
                TestFitnessFunction ff = goals.get(g);

                if (ff instanceof BranchCoverageTestFitness) {
                    BranchCoverageTestFitness goal = (BranchCoverageTestFitness) ff;

                    if (goal.getBranch() == null) { // branchless method
                        branchlessMethodsMap.put(goal.getClassName() + "." + goal.getMethod(), g);
                    } else if (goal.getBranchExpressionValue()) { // true branch
                        trueMap.put(goal.getBranch().getActualBranchId(), g);
                    } else { // false branch
                        falseMap.put(goal.getBranch().getActualBranchId(), g);
                    }
                    //map it to the prior
                    /*branchToSuspiciousnessMap(goal, g);
                    if (goal.getBranch() == null) { // branchless method{
                        branchlessMethodsMap.put(goal.getClassName() + "." + goal.getMethod(), g);
//                        branchToMethodMap.put(g, new BranchDetails(goal.getClassName() + "." + goal.getMethod(), -1, true, -1));
                    } else if (goal.getBranchExpressionValue()) { // true branch
                        trueMap.put(goal.getBranch().getActualBranchId(), g);
//                        branchToMethodMap.put(g, new BranchDetails(goal.getClassName() + "." + goal.getMethod(), goal.getBranch().getActualBranchId(), true, goal.getBranch().getInstruction().getLineNumber()));
                    } else { // false branch
                        falseMap.put(goal.getBranch().getActualBranchId(), g);
//                        branchToMethodMap.put(g, new BranchDetails(goal.getClassName() + "." + goal.getMethod(), goal.getBranch().getActualBranchId(), false, goal.getBranch().getInstruction().getLineNumber()));
                    }*/
                }
            }
        }
    }

    /*protected void readOtherWeight(String filepath)
    {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(filepath));
            otherWeight = Double.valueOf(reader.readLine());
        }catch (IOException e){
            otherWeight = 0d;
        }
    }*/
    @Override
    protected Spectrum getSpectrum(List<ExecutionResult> results) {

        //get the likelihoods
        /*if (count == 0) {
            extract_data(System.getenv("PRIOR_VAL"));
            readOtherWeight(System.getenv("OTHER_PRIOR_VAL"));

        }
        count++;*/
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

    /*private void extract_data(String filename) {
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


    private void branchToSuspiciousnessMap(BranchCoverageTestFitness A, int component_no) {
        //uniform distribution
        if (!mode)
            return;

        //initialise weight map
        if (weights == null)
            weights = new HashMap<>();                    //mycode Integer=component_number, Double=Suspiciousness


        String method_name = A.getMethod();
        String class_name = A.getClassName();

        String method_final = method_name.substring(0, method_name.indexOf('('));
        String class_final = class_name.substring(class_name.lastIndexOf('.') + 1);
        if (method_final.contains("<init>"))
            method_final = class_final;

        Double temp = suspiciousnesScores.get(class_final + "." + method_final);
        if (temp == null) {
            weights.put(component_no, otherWeight);
            sumWeights = sumWeights + otherWeight;
        } else {
            weights.put(component_no, temp);
            sumWeights = sumWeights + temp;
        }

    }*/

    protected Map<Integer, Double> getWeights() {
        //return weights;
        return null;
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


