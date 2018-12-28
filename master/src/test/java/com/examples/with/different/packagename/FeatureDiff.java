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

public class FeatureDiff {

    /**
     * Finds structural and behavioural difference of two xml representations
     * @param xm11
     * @param xml2
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void testDiff(String xm11 , String xml2) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp1 = factory.newPullParser();
        XmlPullParser xpp2 = factory.newPullParser();
        double diff = 0;
        int totalTags = 0; // stores the max. no of tags a particular xml representation has
        xpp1.setInput( new StringReader( xm11 ) );
        xpp2.setInput( new StringReader( xml2 ) );
        int eventType1 = xpp1.getEventType();
        int eventType2 = xpp2.getEventType();
        while (eventType1 != XmlPullParser.END_DOCUMENT && eventType2 != XmlPullParser.END_DOCUMENT) {
            totalTags++;
            if(eventType1 == eventType2) {
                if(eventType1 == XmlPullParser.START_DOCUMENT){
                    // start document tags do not have a name.
                    eventType1 = xpp1.next();
                    eventType2 = xpp2.next();
                    continue;
                }
                if(eventType1 == XmlPullParser.START_TAG){
                    // which means both the evenType are same and both of them are of type START_TAG.
                    if(xpp1.getName().equals(xpp2.getName())){
                        // checking the type of object
                        eventType1 = xpp1.next();
                        eventType2 = xpp2.next();
                        continue;
                    }

                }
                if(eventType1 == XmlPullParser.TEXT){
                    // which means both the evenType are same and both of them are of type TEXT.
                    // 1. Get both the values
                    // 2. Check for types from valid allowable types
                    // 3. If valid types for e.g int, float, String then check for difference
                    // 4. store the difference in another variable - valueDiff;

                    // check for string equality first. It will save a lot of complexity of conversion
                    // the 'else' part should check for value difference
                    if(xpp1.getText().equals(xpp2.getText())){
                        // checking the type of object
                        eventType1 = xpp1.next();
                        eventType2 = xpp2.next();
                        continue;
                    }else{
                        // check for value difference
                        // after calculating valueDiff move on
                        // diff++   // this is also a structural difference
                        // eventType1 = xpp1.next();
                        // eventType2 = xpp2.next();
                        // continue;
                    }

                }
                if(eventType1 == XmlPullParser.END_TAG){
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
        if(eventType1 == XmlPullParser.END_DOCUMENT && eventType2 != XmlPullParser.END_DOCUMENT ){
            // continue iterating xml2 to get the total tags count
            while(eventType2 != XmlPullParser.END_DOCUMENT){
                totalTags++;
                diff++;
                eventType2 = xpp2.next();
            }
        }else if(eventType1 != XmlPullParser.END_DOCUMENT && eventType2 == XmlPullParser.END_DOCUMENT ){
            // continue iterating xml1 to get the total tags count
            while(eventType1 != XmlPullParser.END_DOCUMENT){
                totalTags++;
                diff++;
                eventType1 = xpp1.next();
            }
        }

        System.out.println("Diff is : "+ diff);
        System.out.println("Total Tags : "+ totalTags);
    }


    public static void main(String args[]) throws XmlPullParserException, IOException {

        List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        List<Integer> list2 = new ArrayList<>();
        list2.add(1);
        list2.add(2);
        list2.add(2);
        list2.add(2);
        list2.add(2);
        list2.add(2);
        list2.add(2);
        list2.add(2);

        XStream xstream = new XStream();
        int[] arr = new int[2];
        SomeClass someClass = new SomeClass(2, arr);
        String dataXml1 = xstream.toXML(someClass);
        String dataXml2 = xstream.toXML(list2);
        //java.lang.Class<?> x = null;
        ClassLoader classLoader;
        Object x = null;
        try {
          x = Class.forName("com.examples.with.different.packagename.SomeClass").newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        x = xstream.fromXML(dataXml1);

        FeatureDiff featureDiff = new FeatureDiff();
//        featureDiff.testDiff(dataXml2, dataXml1);


        int[] arr1 = new int[2];

        SomeClass someClass1 = new SomeClass(100, arr1);
        String dataXml3 = xstream.toXML(someClass);
        String dataXml4 = xstream.toXML(someClass1);

        Object c = xstream.fromXML(dataXml3);

        //featureDiff.testDiff(dataXml3, dataXml4);

        /*Map<Integer, String> integerStringMap = new HashMap<>(); // astore_1
        integerStringMap.put(1, "One");
        Map<Integer, String> integerStringMap2 = new HashMap<>(); // astore_1
        integerStringMap2.put(2, "Twone");
        integerStringMap2.put(3, "Twone");
        String dataXml5 = xstream.toXML(integerStringMap);
        String dataXml6 = xstream.toXML(integerStringMap2);

        featureDiff.testDiff(dataXml5, dataXml6);*/

        System.out.println(dataXml4);

    }
}

