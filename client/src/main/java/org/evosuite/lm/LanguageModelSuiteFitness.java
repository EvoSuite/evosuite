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
package org.evosuite.lm;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.io.IOException;

/**
 * Created by mat on 20/03/2014.
 */
public class LanguageModelSuiteFitness extends TestSuiteFitnessFunction {

    private static final long serialVersionUID = 8985543347957256453L;
    private final BranchCoverageSuiteFitness backingFitness;
    private final LangModel languageModel;


    public LanguageModelSuiteFitness() {
        backingFitness = new BranchCoverageSuiteFitness();
        try {
            languageModel = new LangModel(Properties.LM_SRC);
        } catch (IOException e) {
            //TODO: what's the policy for showstopper exceptions?
            throw new RuntimeException("Language Model failed to initialise");
        }
    }

    @Override
    public double getFitness(TestSuiteChromosome individual) {
        double fitness = backingFitness.getFitness(individual);


        //TODO: replace this ugly code with a visitor; will TestVisitor work?
//        for(TestChromosome test : individual.getTestChromosomes()){
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
//
//        }

        return fitness;
    }
}
