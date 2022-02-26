package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCodeDuplication extends AbstractTestCaseSmell {

    public TestCodeDuplication() {
        super("TestSmellTestCodeDuplication");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        String currentStatementString;

        List<String> listOfStatementStrings = new ArrayList<>();
        List<String> listOfStatementStringsCopy = new ArrayList<>();

        for(int i = 0; i < size; i++){
            currentStatementString =  chromosome.getTestCase().getStatement(i).toString();
            listOfStatementStrings.add(currentStatementString);
            listOfStatementStringsCopy.add(currentStatementString);
        }

        Collections.shuffle(listOfStatementStringsCopy);

        String chromosomeString = String.join("", listOfStatementStrings);
        String chromosomeStringCopy = String.join("", listOfStatementStringsCopy);

        int i = 0;

        while(i < chromosomeString.length()){
            if(chromosomeString.charAt(i) == chromosomeStringCopy.charAt(i)){
                count++;
            }
            i++;
        }

        return count;
    }
}
