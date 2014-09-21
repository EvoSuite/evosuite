/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.evosuite.jenkins;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Riki
 * 
 */
public class GeneratedTestSuite {
    private final String NameOfTargetClass;
    private final String NameOfTestSuite;
    private final Double branchCoverage;
    private final int numberOfTests;
    private final int totalNumberOfStatements;
    private final int totalEffortInSeconds;
    private final int effortFromLastModificationInSeconds;
    private final Element DataList;
    
    public GeneratedTestSuite(NodeList data){
        DataList = (Element) data;
        NameOfTargetClass = DataList.getElementsByTagName("fullNameOfTargetClass").item(0).getTextContent();
        NameOfTestSuite = DataList.getElementsByTagName("fullNameOfTestSuite").item(0).getTextContent();
        branchCoverage = Double.parseDouble(DataList.getElementsByTagName("branchCoverage").item(0).getTextContent());
        numberOfTests = Integer.parseInt(DataList.getElementsByTagName("numberOfTests").item(0).getTextContent());
        totalNumberOfStatements = Integer.parseInt(DataList.getElementsByTagName("totalNumberOfStatements").item(0).getTextContent());
        totalEffortInSeconds = Integer.parseInt(DataList.getElementsByTagName("totalEffortInSeconds").item(0).getTextContent());
        effortFromLastModificationInSeconds = Integer.parseInt(DataList.getElementsByTagName("numberOfTests").item(0).getTextContent());
    }
    
    public String getFullNameOfTargetClass(){
        return NameOfTargetClass;
    }
    public String getFullNameOfTestSuite(){
        return NameOfTestSuite;
    }
    public double getBranchCoverage(){
        return branchCoverage;
    }
    public int getNumberOfTests(){
        return numberOfTests;
    }
    public int getTotalNumberOfStatements(){
        return totalNumberOfStatements;
    }
    public int getTotalEffortInSeconds(){
        return totalEffortInSeconds;
    }
    public int getEffortFromLastModificationInSeconds(){
        return effortFromLastModificationInSeconds;
    }
    
}
