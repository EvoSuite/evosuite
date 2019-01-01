package org.evosuite.ga.metaheuristics;

import com.thoughtworks.xstream.XStream;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureNovelty implements NoveltyMetric {

    private XmlPullParser xpp1 = getInstance();
    private XmlPullParser xpp2 = getInstance();
    private XStream xstream = new XStream();

    private XmlPullParser getInstance(){
        try {

            XmlPullParserFactory factory  = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            return xpp;
        } catch (XmlPullParserException e) {
            System.out.println("Problem while trying to creating a instance of XmlPullParserFactory");
            e.printStackTrace();
        }
        return null;
    }


    private ExecutionResult runTest(TestCase test) {
        return TestCaseExecutor.runTest(test);
    }

    private ExecutionResult getExecutionResult(TestChromosome individual) {
        ExecutionResult origResult = individual.getLastExecutionResult();
        if(origResult == null||individual.isChanged()) {
            origResult = runTest(individual.getTestCase());
            individual.setLastExecutionResult(origResult);
            individual.setChanged(false);
        }
        return individual.getLastExecutionResult();
    }

    @Override
    public double calculateDistance(TestChromosome a, TestChromosome b) {

        ExecutionResult result1 =  getExecutionResult((TestChromosome)a);
        ExecutionTrace trace1 = result1.getTrace();
        Map<Integer, Feature> featureMap1  = trace1.getVisitedFeaturesMap();
        trace1.updateFeatureObjectLink(a.getTestCase().getID(), featureMap1);

        ExecutionResult result2 = getExecutionResult((TestChromosome)b);
        ExecutionTrace trace2 = result2.getTrace();
        Map<Integer, Feature> featureMap2  = trace2.getVisitedFeaturesMap();
        trace2.updateFeatureObjectLink(b.getTestCase().getID(), featureMap2);
        double difference = 0.0;


        /**
         * This should create a MAP of featureName, DistanceValue. I.e. Each individual will have a vector of distances
         * for each of the feature.
         * Distance should be calculated for TestChromosome 'a' w.r.t the other individuals.
         */
        Map<Integer, Double> distanceVector = null;

        if(a.getDistanceVector().isEmpty()){
            distanceVector = new HashMap<>();
        }else{
            // do nothing as of now
            distanceVector = a.getDistanceVector();
        }
        double distanceSum = 0; // sum of all the feature wise distance. TODO; find a way to normalize it. Or do we even need to do it?
        for (Map.Entry<Integer, Feature> entry : featureMap1.entrySet()) {
            /*System.out.println(entry.getKey() + ":" + entry.getValue());*/
            System.out.println("FeatureNovelty.java : Feature2 is : "+ featureMap2);
            if(featureMap2.isEmpty()){
                // rare case. But happens when the test case contains statements which throw exception while execution.
                // recently seen, linkedList0.add(int0, integer1); where 'integer1' is the index whose value was greater
                // than the size of the linkedList.
                continue;
            }
            double value = getDistance(featureMap1.get(entry.getKey()), featureMap2.get(entry.getKey()));
            if (distanceVector.get(entry.getKey()) == null) {
                // adding distance for the first time
                distanceVector.put(entry.getKey(), value);
            } else {
                // fetch the value first
                double valueTemp = distanceVector.get(entry.getKey());
                value += valueTemp;
                distanceVector.put(entry.getKey(), value);
            }
            distanceSum +=value;
            // update the original vector map in the testChromosome 'a'
            a.setDistanceVector(distanceVector);
        }

        // find a way to iterate through the feature distanceVector and get a normalized distance between 0-1
        return distanceSum;
    }

    @Override
    public void sortPopulation(List<TestChromosome> population) {

    }

    /**
     * Assuming that both the feature has values of same type
     * @param feature1
     * @param feature2
     * @return
     */
    private double getDistance(Feature feature1, Feature feature2){

        // value can be of type INT, Lon, Float, Double,String Object are converted to string xml format
        Object val = feature1.getValue();
        if(feature2 == null)
            System.out.println("Achtung!!!!!");
        Object val1 = feature2.getValue();
        if(val instanceof Integer){
            return Math.abs((Integer)feature1.getValue() - (Integer)feature2.getValue());
        }else if(val instanceof Float){
            return Math.abs((Float)feature1.getValue() - (Float) feature2.getValue());
        }else if(val instanceof Long){
            return Math.abs((Long) feature1.getValue() - (Long)feature2.getValue());
        }else if(val instanceof Double){
            return Math.abs((Double) feature1.getValue() - (Double)feature2.getValue());
        }else if(val instanceof String){
            // handle String value type
            // All Objects of type other than above ones are handled here.
            // TODO: act according to xml string or simple String
            try {
               Map<String, Double> diffMap = FeatureDiffCalculator.getDifferenceMap((String)val, (String)val1);
               if(diffMap.isEmpty()){
                   // something went wrong. Diff cannot be calculated for this feature
                   System.out.println("something went wrong. Diff cannot be calculated for this feature");
                   // fail safe. At the mst we wouldn't consider this feature for difference
                   return 0;
               }else{
                   // iterate the MAP and fetch the individual components
                   double valDiff = diffMap.get(FeatureDiffCalculator.VALUE_DIFF);
                   double structDiff = diffMap.get(FeatureDiffCalculator.STRUCT_DIFF);
                   double totalTags = diffMap.get(FeatureDiffCalculator.TOTAL_TAGS);

                   // As of now returning the sum of both the component
                   // TODO: handle it in a better way. Maybe the product of them.
                   return valDiff+FeatureDiffCalculator.getNormalizedStructDiff(structDiff, totalTags);
               }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }

        }
        return 0;
    }

    /**
     * Uses identifyObject
     * @param xml1
     * @param xml2
     * @return
     */
    private double getDistanceForObjectType(String xml1, String xml2){
        return 0;
    }

    /**
     * Tries to identify the Object Type. Currently trying to handle Collection types e.g. List, Map etc.
     * @param xml1 - xml representation of the object
     * @return
     */
    private Object identifyObject(String xml1, String xml2){
        double diff = 0;
        try {
            xpp1.setInput( new StringReader( xml1 ) );
            xpp2.setInput( new StringReader( xml2 ) );
            int eventType1 = xpp1.getEventType();
            int eventType2 = xpp2.getEventType();
            while (eventType1 != XmlPullParser.END_DOCUMENT && eventType2 != XmlPullParser.END_DOCUMENT) {
                if(eventType1 == eventType2) {
                    if(xpp1.getName().equals(xpp2.getName())){
                        if(eventType1 == XmlPullParser.TEXT){
                            if(xpp1.getText().equals(xpp2.getText())){
                                eventType1 = xpp1.next();
                                eventType2 = xpp2.next();
                                continue;
                            }
                        }
                    }
                }
                diff++;
                eventType1 = xpp1.next();
                eventType2 = xpp2.next();
            }

        } catch (XmlPullParserException  | IOException e) {
            System.out.println("Trouble while finding out Object type");
            e.printStackTrace();
        }
        return null;
    }


}
