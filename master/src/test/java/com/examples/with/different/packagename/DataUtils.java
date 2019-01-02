package com.examples.with.different.packagename;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class DataUtils {



    public void listOperation(List<Integer> list) {
        List<Integer> list1 = list;
        List<Integer> list2 = list;
        list1.add(2);
        list2.add(0);
        if(list2.size() < 2){
            System.out.println("Negative");
        }else{
            System.out.println("Positive");
        }
    }
    /*public static void main(String args[]) throws XmlPullParserException, IOException {
        DataUtils dataUtils = new DataUtils();



        SomeClass c = new SomeClass(1, null);
        if(c!=null){
            System.out.println("do something");
        }
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        if(list.isEmpty()){
            System.out.println("do something");
        }
        Map<Integer, String> integerStringMap = new HashMap<Integer, String>(); // astore_1
        integerStringMap.put(1, "One"); // pop
        XStream xstream = new XStream();
        String s = "sasdajkhdjhdhcd";
        String dataXml = xstream.toXML(s);
        System.out.println(dataXml);
        String dataXml1 = xstream.toXML(list);
        System.out.println("Printing out list 1");
        System.out.println(dataXml1);
        list.add(2);
        list.add(3);
        String dataXml7 = xstream.toXML(list);
        System.out.println("Printing out modified list");
        System.out.println(dataXml7);





        String dataXml2 = xstream.toXML(integerStringMap);
        System.out.println(dataXml2);
        Set<Integer> integerSet = new TreeSet<>();
        integerSet.add(2);
        String dataXml3 = xstream.toXML(integerSet);
        System.out.println(dataXml3);
        int[] integerArr = new int[2];
        integerArr[0]  =1;
        String dataXml4 = xstream.toXML(integerArr);
        System.out.println(dataXml4);
        String dataXml5 = xstream.toXML(c);
        System.out.println(dataXml5);

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput( new StringReader( dataXml1 ) );

        Map<String, Integer> collectionList = new HashMap<>();
        String type ="";
        int loopCounter = 0;
        int indexTemp = 0;
        int eventType = xpp.getEventType();
        *//*while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_DOCUMENT) {
                System.out.println("Start document : "+xpp.getName());
            } else if(eventType == XmlPullParser.END_DOCUMENT) {
                System.out.println("End document : "+xpp.getName());
            } else if(eventType == XmlPullParser.START_TAG) {
                System.out.println("Start tag "+xpp.getName());
                if(xpp.getName().equals("list"))
                    collectionList.put("list", loopCounter);
                if(xpp.getName().equals("int")){
                    type = "int";
                }
                if(collectionList.get("list") == (indexTemp = loopCounter -2)){
                    List<Integer> someList = (List<Integer>)xstream.fromXML(dataXml1);
                    System.out.println(someList);
                    break;
                    // identification done
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                System.out.println("End tag "+xpp.getName());
            } else if(eventType == XmlPullParser.TEXT) {
                System.out.println("Text "+xpp.getText());
            }



            loopCounter++;
            eventType = xpp.next();
        }*//*


    }*/

}

class SomeClass{
    int j;
    private static int special = 12;
    long someL = 12l;
    int[] someArr = new int[2];
    public SomeClass(int num, int[] arr){
        j = num;
        this.someArr = arr;
    }
    public SomeClass(){

    }
}

