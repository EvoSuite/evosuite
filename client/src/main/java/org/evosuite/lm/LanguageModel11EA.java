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

import org.evosuite.testcase.ValueMinimizer;
import org.evosuite.testcase.variable.ConstantValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by mat on 07/04/2014.
 */
public class LanguageModel11EA extends LanguageModelSearch {
    protected static Logger logger = LoggerFactory.getLogger(LanguageModel11EA.class);


    public LanguageModel11EA(ConstantValue statement, ValueMinimizer.Minimization objective) {
        super(objective, statement);
    }


    @Override
    public String optimise() {
        if (startPoint == null || startPoint.isEmpty()) {
            logger.info("Not trying to optimise null or empty string");
            return startPoint;
        }

        resetEvaluationCounter();

        Chromosome best = new Chromosome(startPoint);
        best.setFitness(evaluate(best));
        double originalStringScore = best.getFitness();

        for (int generation = 0; generation < GENERATIONS && !isBudgetExpended(); generation++) {
            double currentStringScore = best.getFitness();

            Chromosome mutant = mutate(best);
            if (!mutant.isEvaluated())
                mutant.setFitness(evaluate(mutant));

            if (mutant.compareTo(best) > 0) {
                best = mutant;
            }
        }
        double thisStringScore = best.getFitness();



/*        logger.info(String.format("LanguageModel: Produced a new string '%s' with score %f, old string was '%s' with score %f",
                best.getValue(),
                thisStringScore,
                startPoint,
                originalStringScore));
*/
        return best.getValue();
    }

}
