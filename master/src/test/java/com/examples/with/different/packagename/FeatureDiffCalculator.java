package com.examples.with.different.packagename;

import com.thoughtworks.xstream.XStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureDiffCalculator {

    public static final String VALUE_DIFF = "VALUE_DIFF";
    public static final String STRUCT_DIFF = "STRUCT_DIFF";
    public static final String TOTAL_TAGS = "TOTAL_TAGS";


    /**
     * Finds out Structural and Value difference between elements of the two input xml representation.
     * Structural difference is the difference in the nodes of the xml structure.
     * For e.g if we have two instances l1 and l2 of java.util.list with 2 and 3 elements respectively
     * then the difference in the number of tags for one element will be returned. This is more like
     * size() or length() operation performed followed by difference operation.
     * Value difference is the purely the difference of the element values. Note that this difference
     * is calculated only on corresponding elements i.e. if two Integer list of size 2 and 4 then the
     * value difference will be calculated between the first two elements of list 1 and list 2 respectively.
     * Uses getDifference. See getDifference.
     *
     * @param xm11
     * @param xml2
     * @return a java.util.Map containing Key, value pair of DifferenceType and DifferenceValue.
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static Map<String, Double> getDifferenceMap(String xm11, String xml2) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp1 = factory.newPullParser();
        XmlPullParser xpp2 = factory.newPullParser();
        double diff = 0; // Stores structural difference
        double valDiff = 0; // Stores behavioral difference
        double totalTags = 0; // stores the max. no of tags a particular xml representation has
        Map<String, Double> result = new HashMap<>();
        xpp1.setInput(new StringReader(xm11));
        xpp2.setInput(new StringReader(xml2));
        int eventType1 = xpp1.getEventType();
        int eventType2 = xpp2.getEventType();
        while (eventType1 != XmlPullParser.END_DOCUMENT && eventType2 != XmlPullParser.END_DOCUMENT) {
            totalTags++;
            if (eventType1 == eventType2) {
                if (eventType1 == XmlPullParser.START_DOCUMENT) {
                    // start document tags do not have a name.
                    eventType1 = xpp1.next();
                    eventType2 = xpp2.next();
                    continue;
                }
                if (eventType1 == XmlPullParser.START_TAG) {
                    // which means both the evenType are same and both of them are of type START_TAG.
                    if (xpp1.getName().equals(xpp2.getName())) {
                        // checking the type of object
                        eventType1 = xpp1.next();
                        eventType2 = xpp2.next();
                        continue;
                    }

                }
                if (eventType1 == XmlPullParser.TEXT) {
                    // which means both the evenType are same and both of them are of type TEXT.
                    // 1. Get both the values
                    // 2. Check for types from valid allowable types
                    // 3. If valid types for e.g int, float, String then check for difference
                    // 4. store the difference in another variable - valueDiff;

                    // check for string equality first. It will save a lot of complexity of conversion
                    // the 'else' part should check for value difference
                    if (xpp1.getText().equals(xpp2.getText())) {
                        // checking the type of object
                        eventType1 = xpp1.next();
                        eventType2 = xpp2.next();
                        continue;
                    } else {
                        // check for value difference
                        // after calculating valueDiff move on
                        valDiff += getDifference(xpp1.getText(), xpp2.getText());
                        diff++;   // this is also a structural difference
                        eventType1 = xpp1.next();
                        eventType2 = xpp2.next();
                        continue;
                    }

                }
                if (eventType1 == XmlPullParser.END_TAG) {
                    eventType1 = xpp1.next();
                    eventType2 = xpp2.next();
                    continue;
                }
            }
            diff++;
            eventType1 = xpp1.next();
            eventType2 = xpp2.next();
        }

        // if xm11 has less tags than xml2
        if (eventType1 == XmlPullParser.END_DOCUMENT && eventType2 != XmlPullParser.END_DOCUMENT) {
            // continue iterating xml2 to get the total tags count
            while (eventType2 != XmlPullParser.END_DOCUMENT) {
                totalTags++;
                diff++;
                eventType2 = xpp2.next();
            }
        } else if (eventType1 != XmlPullParser.END_DOCUMENT && eventType2 == XmlPullParser.END_DOCUMENT) {
            // continue iterating xml1 to get the total tags count
            while (eventType1 != XmlPullParser.END_DOCUMENT) {
                totalTags++;
                diff++;
                eventType1 = xpp1.next();
            }
        }
        result.put(VALUE_DIFF, valDiff);
        result.put(STRUCT_DIFF, diff);
        result.put(TOTAL_TAGS, totalTags);
        System.out.println("Value Diff is : " + valDiff);
        System.out.println("Structural Diff is : " + diff);
        System.out.println("Total Tags : " + totalTags);
        return result;
    }

    /**
     * Tries to convert the String representation of values to Int, Float, Long, Double
     * and then calculates the difference between them.
     * For different Strings currently the difference returned is 1
     *
     * @param objValue1
     * @param objValue2
     * @return
     */
    private static double getDifference(String objValue1, String objValue2) {
        try {
            Integer val1 = Integer.valueOf(objValue1);
            Integer val2 = Integer.valueOf(objValue2);
            return Math.abs(val1 - val2);
        } catch (NumberFormatException e) {
            // log the exception
            // try for float
            try {
                Float val1 = Float.valueOf(objValue1);
                Float val2 = Float.valueOf(objValue2);
                return Math.abs(val1 - val2);
            } catch (NumberFormatException e1) {
                // log the exception
                // try for double
                try {
                    Double val1 = Double.valueOf(objValue1);
                    Double val2 = Double.valueOf(objValue2);
                    return Math.abs(val1 - val2);
                } catch (NumberFormatException e2) {
                    // log the exception
                    // try for long
                    try {
                        Long val1 = Long.valueOf(objValue1);
                        Long val2 = Long.valueOf(objValue2);
                        return Math.abs(val1 - val2);
                    } catch (NumberFormatException e3) {
                        // log the exception
                        // invalid format or possibly non parsable String
                        // This might be the case when
                        // 1) the values are truely two different String or
                        // 2) the values are String but of the form '/n' and ' /n' which happens as a result of Structural difference in the xml TEXT tags.
                        // as a default fallback - we always consider
                        // As of now returning 1 for All String differences.
                        // TODO: handle in a better way to get an accurate difference
                        return 1;
                    }

                }

            }
        }
    }

    /**
     * The return value should be in the range of 0-1. The difference between totalTags and structDiff is directly proportional
     * to the result value. This gives the Normalized Structural Difference. More similar the structure higher the result.
     * Less similar the structure lower the result.
     *
     * @param structDiff
     * @param totalTags
     * @return
     */
    public static double getNormalizedStructDiff(double structDiff, double totalTags){
        double result = (totalTags-structDiff)/totalTags;
        // log the result value
        System.out.println("Struct Diff val : "+ result);
        return result;
    }


    public static void main(String args[]) throws XmlPullParserException, IOException {

        List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(1);
        list1.add(2);
        list1.add(4);

        List<Integer> list2 = new ArrayList<>();
        list2.add(30);
        list2.add(1);
        list2.add(2);
        list2.add(4);


        XStream xstream = new XStream();
        int[] arr = new int[2];
        SomeClass someClass = new SomeClass(2, arr);
        String dataXml1 = xstream.toXML(list1);
        String dataXml2 = xstream.toXML(list2);


        Map<String, Double> diffMap =  FeatureDiffCalculator.getDifferenceMap(dataXml2, dataXml1);
        double valDiff = diffMap.get(FeatureDiffCalculator.VALUE_DIFF);
        double structDiff = diffMap.get(FeatureDiffCalculator.STRUCT_DIFF);
        double totalTags = diffMap.get(FeatureDiffCalculator.TOTAL_TAGS);
System.out.println("Normalized Diff : "+getNormalizedStructDiff(structDiff, totalTags));

        /*int[] arr1 = new int[2];

        SomeClass someClass1 = new SomeClass(100, arr1);
        String dataXml3 = xstream.toXML(someClass);
        String dataXml4 = xstream.toXML(someClass1);

        Object c = xstream.fromXML(dataXml3);

        featureDiff.testDiff(dataXml3, dataXml4);

        Map<Integer, String> integerStringMap = new HashMap<>(); // astore_1
        integerStringMap.put(1, "One");
        Map<Integer, String> integerStringMap2 = new HashMap<>(); // astore_1
        integerStringMap2.put(2, "Twone");
        integerStringMap2.put(3, "Twone");
        String dataXml5 = xstream.toXML(integerStringMap);
        String dataXml6 = xstream.toXML(integerStringMap2);

        featureDiff.testDiff(dataXml5, dataXml6);

        System.out.println(dataXml4);
     FeatureDiff featureDiff = new FeatureDiff();
        Long l1 = 2l;
        Long l2 = 5l;
        System.out.println(featureDiff.getDifference(l1.toString(),l2.toString()));
     XStream xstream = new XStream();
        int[] arr = new int[2];
        SomeClass someClass = new SomeClass(2, arr);
        String dataXml1 = xstream.toXML(someClass);
        System.out.println(dataXml1);*/

    }
}

