/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.evosuite.jenkins;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 *
 * @author Riki
 */
public class EvosuiteStats{
    private int NumberOfClasses;
    private int totalNumberOfTestableClasses;
    private double averageBranchCoverage;
    public Set <GeneratedTestSuite> TestSuites = new  HashSet<GeneratedTestSuite>();
    public int buildNumber;
    
    public List <GeneratedTestSuite> getTestSuites(){
        return new ArrayList<GeneratedTestSuite> (TestSuites);
    }
    public double getaverageBranchCoverage(){
        return averageBranchCoverage;
    }
    public Integer getNumberOfClasses(){
        return NumberOfClasses;
    }
    public Integer gettotalNumberOfTestableClasses(){
        return totalNumberOfTestableClasses;
    }
    public EvosuiteStats(File fXmlFile, int buildnumber){
        try {
            this.buildNumber = buildnumber;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList nList = doc.getElementsByTagName("ProjectInfo");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                
                
                
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    NumberOfClasses = Integer.parseInt(eElement.getElementsByTagName("totalNumberOfClasses").item(0).getTextContent());
                    totalNumberOfTestableClasses = Integer.parseInt(eElement.getElementsByTagName("totalNumberOfTestableClasses").item(0).getTextContent());
                    averageBranchCoverage = Double.parseDouble(eElement.getElementsByTagName("averageBranchCoverage").item(0).getTextContent());
                }
                NodeList suiteList = doc.getElementsByTagName("generatedTestSuites");
                //nodelist containing all generated testsuites for a particular module
                for (int j = 0; j < suiteList.getLength(); j++){
                    //for each suite list, create a generated test suite to hold the data, this class contains an array of generated test suites
                    Node jNode = suiteList.item(j);
                    NodeList newlist  = suiteList.item(j).getChildNodes();
                    GeneratedTestSuite tem = new GeneratedTestSuite(newlist);
                    TestSuites.add(tem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
