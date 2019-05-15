package org.evosuite.ga.metaheuristics;

import com.sun.org.apache.xerces.internal.dom.DeferredTextImpl;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.coverage.dataflow.FeatureKey;
import org.evosuite.testcase.TestChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureValueAnalyser {

    private final static Logger logger = LoggerFactory.getLogger(FeatureValueAnalyser.class);

    private static Map<String, List<Double>> nodeAnalysisMap = new HashMap<String, List<Double>>();

    private static void updateMap(String nodeName, String val) {
        // if the nodeName is already not present in the map then make an entry
        if (!nodeAnalysisMap.containsKey(nodeName)) {
            List<Double> listOfCountAndVal = new ArrayList<Double>();
            listOfCountAndVal.add(1.0);
            double value = readDoubleValue(val);
            listOfCountAndVal.add(value);
            nodeAnalysisMap.put(nodeName, listOfCountAndVal);
        } else {
            // update the count and value
            List<Double> listOfCountAndVal = nodeAnalysisMap.get(nodeName);
            double count = listOfCountAndVal.get(0);
            count++;
            double value = readDoubleValue(val);
            double newVal = listOfCountAndVal.get(1) + value;
            listOfCountAndVal.clear();
            listOfCountAndVal.add(0, count);
            listOfCountAndVal.add(1, newVal);
            nodeAnalysisMap.put(nodeName, listOfCountAndVal);
            // the same map entry
        }
    }

    private static void iterateNodes(NodeList nodeList) {

        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                if (tempNode.hasChildNodes()) {
                    // loop again if has child nodes
                    iterateNodes(tempNode.getChildNodes());
                }
            } else {
                if (tempNode.getPreviousSibling() != null && !tempNode.getPreviousSibling().getNodeName().equals("null") && !tempNode.getPreviousSibling().getNodeName().equals("serialVersionUID")
                        ) {
                    updateMap((((DeferredTextImpl) tempNode).getParentNode()).getNodeName() + "_" + tempNode.getPreviousSibling().getNodeName(), tempNode.getPreviousSibling().getTextContent());
                }

            }
        }
    }

    /**
     * This method traverses the nodes of the xml representation recursively and returns a MAP with
     * all the distinct members/children of the parent node. Each distinct entry contains 'ParentName_ChildName'
     * as the key and the value is a List containing at index (0) -> Occurrence of the same member in the xml
     * representation and at index (1) -> total value of such member elements
     * <p>
     * For e.g. if we have the String representation of the following MAP
     * Map<String, Integer> map = new HashMap<String, Integer>();
     * map.put("First", 1);
     * map.put("Second", 2);
     * map.put("Third", 3);
     * map.put("Fourth", 4);
     * as
     * <map>
     * <entry>
     * <string>Second</string>
     * <int>2</int>
     * </entry>
     * <entry>
     * <string>Third</string>
     * <int>3</int>
     * </entry>
     * <entry>
     * <string>First</string>
     * <int>1</int>
     * </entry>
     * <entry>
     * <string>Fourth</string>
     * <int>4</int>
     * </entry>
     * </map>
     * then this method will return :
     * {entry_int=[4.0, 10.0], entry_string=[4.0, 477.0], map_entry=[4.0, 455.0]}
     *
     * @param xm11
     * @return
     */
    public static Map<String, List<Double>> getAnalysisFromStringRepresentationUsingDomParser(String xm11) {
        nodeAnalysisMap.clear();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            //fix TODO: FeatureInstrumentation is yet not intelligent enough to not serialize certain classes.
            if (xm11.contains("&#")) {
                xm11 = xm11.replace("&#", "");
            }

            Document doc = dBuilder.parse(new InputSource(new StringReader(xm11)));
            doc.getDocumentElement().normalize();

            if (doc.hasChildNodes()) {
                iterateNodes(doc.getChildNodes());
            }
        } catch (Exception e) {
            logger.error("Error while parsing xml string. Empty map will be returned");
        }
        return nodeAnalysisMap;
    }

    /**
     * Tries to convert the String representation of values to Int, Float, Long, Double, Boolean
     * to a double value.
     * <p>
     * For String values of type other than the above types, -9999999 is returned.
     *
     * @param objValue1
     * @return
     */
    private static double getNumericValue(String objValue1) {
        try {
            return Integer.valueOf(objValue1);

        } catch (NumberFormatException e) {
            // log the exception
            // try for float
            try {
                return Float.valueOf(objValue1);

            } catch (NumberFormatException e1) {
                // log the exception
                // try for double
                try {
                    return Double.valueOf(objValue1);

                } catch (NumberFormatException e2) {
                    // log the exception
                    // try for long
                    try {
                        return Long.valueOf(objValue1);

                    } catch (NumberFormatException e3) {
                        //try for Boolean

                        if (objValue1 != null) {
                            if (objValue1.equalsIgnoreCase("true"))
                                return 1;
                            else if (objValue1.equalsIgnoreCase("false"))
                                return 0;
                            else
                                return -9999999;
                        } else {
                            //"Object value is a String"
                            // log the exception
                            // invalid format or possibly non parsable String
                            // This might be the case when
                            // 1) the values are truely two different String or
                            // 2) the values are String but of the form '/n' and ' /n' which happens as a result of Structural difference in the xml TEXT tags.
                            // as a default fallback - we always consider
                            // As of now returning 1 for All String differences.
                            // TODO: handle in a better way to get an accurate difference
                            // TODO: Maybe we can use length of the string or good old way of char by char comparision?
                            return -9999999;
                        }
                    }

                }

            }
        }
    }


    static int count = 0;

    public static void updateNormalizedFeatureValues(TestChromosome t, Map<FeatureKey, List<Double>> featureValueRangeList) {
        List<Map<Integer, Feature>> featureMapList = t.getLastExecutionResult().getTrace().getListOfFeatureMap();

        if (featureMapList == null || featureMapList.isEmpty()) {
            // no need to process
            count++;
            return;
        }

        for (Map<Integer, Feature> map : featureMapList) {
            for (Map.Entry<Integer, Feature> entry : map.entrySet()) {
                FeatureKey featureKey = new FeatureKey(entry.getValue().getVariableName(), entry.getValue().getMethodName());
                List<Double> valueRange = featureValueRangeList.get(featureKey);
                Feature feature = entry.getValue();
                double normalizedVal = Double.parseDouble(String.format("%.3f", getNormalizedValue(readDoubleValue(feature.getValue()), valueRange)));
                feature.setNormalizedValue(normalizedVal);
            }
        }

    }

    public static double getNormalizedValue(double value, List<Double> valueRange) {
        double minVal = valueRange.get(0);
        double maxVal = valueRange.get(1);
        double range = (maxVal - minVal) == 0 ? 1 : (maxVal - minVal);
        // the value should be in the range of 0-1
        return (maxVal - value) / range;
    }

    public static double readDoubleValue(Object val) {
        if (val instanceof Integer) {
            return ((Integer) val);
        } else if (val instanceof Float) {
            return ((Float) val);
        } else if (val instanceof Long) {
            return ((Long) val);
        } else if (val instanceof Double) {
            return ((Double) val);
        } else if (val instanceof String) {

            return getCharValueAsIntFromString((String) val);
        }
        // default case
        return 0.1;
    }

    /**
     * This method returns the sum of all the int value of the Unicode characters
     * of the input String.
     *
     * @param input
     * @return
     */
    private static double getCharValueAsIntFromString(String input) {

        double val = getNumericValue(input);
        if (Double.compare(val, -9999999) != 0) {
            return val;
        }
        // this is definitely a String
        char[] arr = input.toCharArray();
        int sum = 0;
        for (char c : arr)
            sum += Character.getNumericValue(c);
        return sum;
    }

    /**
     * This method returns the squared difference between feature1 and feature2.
     * If any one of the feature is null then max difference 1 is returned.
     *
     * @param feature1
     * @param feature2
     * @return
     */

    public static double getFeatureDistance(Feature feature1, Feature feature2) {
        if ((feature1 != null) && (feature2 != null)) {
            // to limit the precision value
            double diff = feature1.getNormalizedValue() - feature2.getNormalizedValue();
            diff = Double.parseDouble(String.format("%.3f", diff));
            double sqrdDiff = Double.parseDouble(String.format("%.3f", (diff * diff)));
            return sqrdDiff;
        } else {
            //TODO: decide what to do
            // returning max distance as of now
            return 1;
        }
    }


}

