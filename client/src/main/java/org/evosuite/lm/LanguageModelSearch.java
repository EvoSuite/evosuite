package org.evosuite.lm;

import org.evosuite.Properties;
import org.evosuite.testcase.ValueMinimizer;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mat on 07/04/2014.
 */
public abstract class LanguageModelSearch implements Comparator<Chromosome> {
    protected static final int GENERATIONS = 1000000;
    private static Logger logger = LoggerFactory.getLogger(LanguageModelSearch.class);
    protected final LangModel languageModel;
    protected final String startPoint;
    protected final ValueMinimizer.Minimization objective;
    protected final ConstantValue constantValue;

    protected static final int MAX_EVALUATIONS;

    static{
        MAX_EVALUATIONS = Properties.LM_ITERATIONS;
    }

    public int getEvaluations() {
        return evaluations;
    }

    private int evaluations = 0;


    public LanguageModelSearch(ValueMinimizer.Minimization objective, ConstantValue constantValue) {
        try {
            this.languageModel = new LangModel(Properties.LM_SRC);
        } catch (Exception e) {
            //FIXME: remove this garbage
            throw new RuntimeException("Couldn't create language model");
        }
        this.startPoint = (String)constantValue.getValue();
        this.objective = objective;
        // this.statement = statement;
        this.constantValue = constantValue;
    }

    public static String mutateRandom(final String input){

        if (input == null || input.length() == 0){
            return input;
        }

        int position = (int)(Math.abs(Randomness.nextDouble() * input.length()));

        String output = input.substring(0,position);

        int tailSize = input.length() - position;

        assert tailSize >= 0;

        int replacementSize = 1;
        if(tailSize > 1)
            replacementSize = Randomness.nextInt(1,tailSize);

        String replacementString = "";
        for(int i =0; i < replacementSize; i++)
            replacementString += (char) Randomness.nextInt(' ', '~' + 1);

        output += replacementString;

        output += input.substring(position+ replacementSize);


        return output;
    }

    public static String mutateEvoSuite(final String input){

        char[] array = input.toCharArray();


        for (int index = 0; index < input.length(); index++) {
            if (Randomness.nextDouble() < 1.0 / (0.5 * input.length() + 1)) {
                char oldValue = array[index];
                while (array[index] == oldValue) {
                    array[index] = (char) Randomness.nextInt(' ', '~' + 1);
                }
            }
        }
        assert String.valueOf(array).length() == input.length();
        return String.valueOf(array);
    }

    public String mutate(final String input){
        if(Properties.LM_MUTATION_TYPE == MutationType.LANGMODEL) {
            return mutateLangModel(input);
        }else if(Properties.LM_MUTATION_TYPE == MutationType.RANDOM){
            return mutateRandom(input);
        }else if(Properties.LM_MUTATION_TYPE == MutationType.EVOSUITE){
            return mutateEvoSuite(input);
        }else{
            throw new RuntimeException("Mutation type " + Properties.LM_MUTATION_TYPE + " not supported");
        }
    }

    /**
     * Build a random string that is at most maxStringLength characters long.
     */
    public String generateRandomStringFromModel(int maxStringLength, String previousChar){
        //Start the string with a random choice from the 10 most likely characters to start a string:


        String newString = "";


        do{

            Set<Integer> choices = new HashSet<Integer>();
            choices.addAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

            boolean foundValidChar = false;
            String nextChar;

            do{
                int currentChoice = Randomness.choice(choices);

                choices.remove(currentChoice);

                if(previousChar == null){
                    nextChar = languageModel.predict_char(currentChoice);
                }else{
                    nextChar = languageModel.predict_char(previousChar, currentChoice);
                }

                foundValidChar = nextChar != null && !languageModel.isMagicChar(nextChar);

            }while(!choices.isEmpty() && !foundValidChar);
            //We keep trying until we get an actual char, or we run out of slots. After that, we give up and
            //reuse the input.

            if(!foundValidChar){
                logger.debug("Couldn't find any bigram or unigram for " + previousChar);
                nextChar = "a"; //TODO: this is a kludge
            }


            if(!languageModel.isEndOfSentence(nextChar))
                newString += nextChar;

            previousChar = nextChar;

        }while(newString.length() < maxStringLength && !languageModel.isEndOfSentence(previousChar));

        return newString;
    }

    public String generateRandomStringFromModelWithExactLength(int targetStringLength, String previousChar){
        String newString = generateRandomStringFromModel(targetStringLength,previousChar);

        while(newString.length() < targetStringLength){
            newString += generateRandomStringFromModel(targetStringLength - newString.length(), newString.charAt(newString.length()-1) + "");
        }

        assert newString.length() == targetStringLength;

        return newString;
    }

    public String generateRandomStringFromModelWithExactLength(int targetStringLength){
        return generateRandomStringFromModelWithExactLength(targetStringLength, null);
    }

    public String generateRandomStringFromModel(int maxStringLength){
        return generateRandomStringFromModel(maxStringLength, null);
    }


    /**
     * Produce a new string similar to input.
     * Selects a random substring of the string and for each character in that substring:
     *  gets the character preceeding it
     *  gets a character the language model predicts to follow that prefix
     *  replaces the character with the predicted character
     */
    public String mutateLangModel(final String input){

        if(input.length() == 0){
            //Mutation preserves length, so we can't do anything for empty string.
            return input;
        }

        //e.g. string is "abcde"
        // point is 0..4
        //Since nextDouble never returns 1.0, taking the floor gives us values in the range 0..strlen-1:
        int startPoint = (int)(Randomness.nextDouble() * (input.length()));

        //we preserve string lengths; how many chars do we have to mutate?
        int remainingLength = (int)Math.round(
                Randomness.nextDouble() * (input.length() - startPoint)
        );

        if(remainingLength == 0){
            //Mutation size is zero, so there's nothing to do.
            return input;
        }


        //substring is start (inclusive) to end (exclusive).
        //e.g. if startPoint is 3
        // output = "abc" at this point
        String output = input.substring(0,startPoint);

        String replacementChunk = generateRandomStringFromModelWithExactLength(remainingLength, ""+ input.charAt(startPoint));


        output += replacementChunk;
        output += input.substring(startPoint + remainingLength, input.length());

        return output;

    }


    /**
     * @param individual
     * @return the language model score of the string, plus 1 if it does not affect the coverage goal.
     */
    protected double evaluate(Chromosome individual) throws EvaluationBudgetExpendedException {
        if(isBudgetExpended()){
            throw new EvaluationBudgetExpendedException();
        }
        evaluations++;
        String oldValue = (String)constantValue.getValue();
        constantValue.setValue(individual.getValue());
        boolean isNotWorse = objective.isNotWorse();

        constantValue.setValue(oldValue);

        return (isNotWorse ? 1 : 0) + languageModel.score(individual.getValue());

    }

    protected boolean isBudgetExpended() {
        return evaluations >= MAX_EVALUATIONS;
    }

    protected Chromosome mutate(Chromosome individual){
        assert individual != null;
        return new Chromosome(mutate(individual.getValue()));
    }



    public abstract String optimise();

    @Override
    public int compare(Chromosome o1, Chromosome o2) {
        if (!o1.isEvaluated()) {
            o1.setFitness(evaluate(o1));
        }
        if (!o2.isEvaluated()) {
            o2.setFitness(evaluate(o2));
        }
        return o1.compareTo(o2);
    }


    protected void resetEvaluationCounter() {
        evaluations = 0;
    }
}

class Chromosome implements Comparable<Chromosome>, Cloneable {
    private String value;
    private double fitness = -1;
    private boolean isEvaluated = false;

    public Chromosome(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (this.value.equals(value)) {
            return;
        }

        this.value = value;
        this.isEvaluated = false;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
        this.isEvaluated = true;
    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    public Chromosome clone() {
        Chromosome other = new Chromosome(this.getValue());
        other.setFitness(this.getFitness());
        other.isEvaluated = this.isEvaluated();
        return other;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chromosome that = (Chromosome) o;

        //Don't care about fitness or evaluation status:

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = value.hashCode();
        return result;
            /*temp = Double.doubleToLongBits(fitness);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + (isEvaluated ? 1 : 0);
            return result;*/
    }

    @Override
    public int compareTo(Chromosome o) {
        if (o == null) {
            return 1;
        }

        return Double.compare(this.getFitness(), o.getFitness());

    }
}