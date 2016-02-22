package org.evosuite.lm;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.testcase.*;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.io.IOException;

/**
 * Created by mat on 20/03/2014.
 */
public class LanguageModelSuiteFitness extends TestSuiteFitnessFunction {

    private BranchCoverageSuiteFitness backingFitness;
    private LangModel languageModel;


    public LanguageModelSuiteFitness(){
        backingFitness = new BranchCoverageSuiteFitness();
        try {
            languageModel = new LangModel(Properties.LM_SRC);
        } catch (IOException e) {
            //TODO: what's the policy for showstopper exceptions?
            throw new RuntimeException("Language Model failed to initialise");
        }
    }

    @Override
    public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {
        double fitness = backingFitness.getFitness(individual);


        //TODO: replace this ugly code with a visitor; will TestVisitor work?
        for(ExecutableChromosome chromosome : individual.getTestChromosomes()){
            TestChromosome test = (TestChromosome)chromosome; //TODO: HACK!
//
//            TestCase testCase = test.getTestCase();
//            for(StatementInterface statement : testCase){
//                if(statement instanceof StringPrimitiveStatement){
//                    StringPrimitiveStatement stringPrimitive = (StringPrimitiveStatement) statement;
//
//                    String value = stringPrimitive.getValue();
//                    double score = languageModel.score(value);
//
//                    fitness += 1-score;
//
//
//                    //TODO: we need to decide here how to reward good strings;
//                    //Afshan et al do this on a per-branch basis but we can only see the whole suite fitness
//                    // (unless we subclass BranchCoverageSuiteFitness rather than wrapping it)
//
//                    //At the moment this just penalises each string, rather than allowing EvoSuite to cover branches
//                    //before penalising them.
//
//
//
//                }
//            }

        }

        return fitness;
    }
}
