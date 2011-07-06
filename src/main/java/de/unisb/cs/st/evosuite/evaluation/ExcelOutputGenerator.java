package de.unisb.cs.st.evosuite.evaluation;

import java.io.File;
import java.io.IOException;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Strategy;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJPool;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

import jxl.*;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import jxl.read.biff.BiffException;

public class ExcelOutputGenerator {
	
	private static WritableWorkbook writtenWorkbook;
	
	public static void createNewExcelWorkbook(String dirName){
		
		try {
			String wholeFilename = dirName + File.separator + Properties.CRITERION;
			
			if (Properties.STRATEGY == Strategy.EVOSUITE)
				wholeFilename += "-EvoStats.xls";
			else
				wholeFilename +="-Stats.xls";
				
			File f = new File(wholeFilename);
			
			File dir = new File(dirName);
			
			if (!dir.exists()){
				if (dir.mkdir())
					if (f.createNewFile())
						writtenWorkbook =  Workbook.createWorkbook(f);
			}
			else if (!f.exists()){
				if (f.createNewFile())
					writtenWorkbook =  Workbook.createWorkbook(f);
			}
			else {
				if (f.exists()){
					Workbook workbook = Workbook.getWorkbook(f);
					writtenWorkbook =  Workbook.createWorkbook(f ,workbook);
				}
			}
				
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public static void writeToCurrentWorkbook(){
		try {		
			writtenWorkbook.write();
			writtenWorkbook.close();
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void writeLCSAJStatistics(String className, int totalBranches, int infeasableBranches,TestSuiteChromosome LCSAJsuite,TestSuiteChromosome LCSAJbranchSuite){
		
		WritableSheet currentSheet;
		System.out.println(writtenWorkbook);
		System.out.println(Properties.PROJECT_PREFIX);
		
		if (writtenWorkbook.getSheet(Properties.PROJECT_PREFIX) == null){
			currentSheet = setupSheetForLCSAJStatistics();
		}
		else {
			currentSheet = writtenWorkbook.getSheet(Properties.PROJECT_PREFIX);
		}
		Label l = new Label(0,currentSheet.getRows(), className);

		Number n1 = new Number(1, currentSheet.getRows(), totalBranches);
		Number n2 = new Number(2, currentSheet.getRows(), LCSAJPool.getLCSAJsPerClass(className));
		Number n3 = new Number(3, currentSheet.getRows(), LCSAJsuite.getCoverage());
		Number n4 = new Number(4, currentSheet.getRows(), LCSAJbranchSuite.getCoverage());
		Number n5 = new Number(5, currentSheet.getRows(), LCSAJsuite.getFitness());
		Number n6 = new Number(6, currentSheet.getRows(), LCSAJbranchSuite.getFitness());
		Number n7 = new Number(7, currentSheet.getRows(), LCSAJPool.getMinLCSAJlength(className));
		Number n8 = new Number(8, currentSheet.getRows(), LCSAJPool.getAvgLCSAJlength(className));
		Number n9 = new Number(9, currentSheet.getRows(), LCSAJPool.getMaxLCSAJlength(className));
		Number n10 = new Number(10, currentSheet.getRows(), LCSAJPool.getInfeasableLCSAJs(className));
		Number n11 = new Number(11, currentSheet.getRows(), LCSAJPool.getUnfinishedLCSAJs(className));
		Number n12 = new Number(12, currentSheet.getRows(), infeasableBranches);
		Number n13 = new Number(13, currentSheet.getRows(), LCSAJsuite.size());
		Number n14 = new Number(14, currentSheet.getRows(), LCSAJsuite.totalLengthOfTestCases());
		
		try {
			currentSheet.addCell(l);
			currentSheet.addCell(n1);
			currentSheet.addCell(n2);
			currentSheet.addCell(n3);
			currentSheet.addCell(n4);
			currentSheet.addCell(n5);
			currentSheet.addCell(n6);
			currentSheet.addCell(n7);
			currentSheet.addCell(n8);
			currentSheet.addCell(n9);
			currentSheet.addCell(n10);
			currentSheet.addCell(n11);
			currentSheet.addCell(n12);
			currentSheet.addCell(n13);
			currentSheet.addCell(n14);
			
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void wirteBranchStatistics(String className, int branchTests, int branchTestsLength, double branchCoverage,double branchCoverageFitness){
		
		WritableSheet currentSheet;

		if (writtenWorkbook.getNumberOfSheets() == 0 || writtenWorkbook.getSheet(Properties.PROJECT_PREFIX) == null){
			currentSheet = setupSheetForBranchStatistics();
		}
		else {
			currentSheet = writtenWorkbook.getSheet(Properties.PROJECT_PREFIX);
		}
		Label l = new Label(0,currentSheet.getRows(), className);
		Number n1 = new Number(1, currentSheet.getRows(), branchTests);
		Number n2 = new Number(2, currentSheet.getRows(), branchTestsLength);
		Number n3 = new Number(3, currentSheet.getRows(), branchCoverage);
		Number n4 = new Number(4, currentSheet.getRows(), branchCoverageFitness);

		try {
			
			currentSheet.addCell(l);
			currentSheet.addCell(n1);
			currentSheet.addCell(n2);
			currentSheet.addCell(n3);
			currentSheet.addCell(n4);
			
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static WritableSheet setupSheetForLCSAJStatistics(){
		
		writtenWorkbook.createSheet(Properties.PROJECT_PREFIX, writtenWorkbook.getNumberOfSheets());
		WritableSheet currentSheet = writtenWorkbook.getSheet(Properties.PROJECT_PREFIX);
		try {
			currentSheet.addCell(new Label(0, 0, "Classname"));
			currentSheet.addCell(new Label(1, 0, "Number of Branches"));	
			currentSheet.addCell(new Label(2, 0, "Number of LCSAJs"));
			currentSheet.addCell(new Label(3, 0, "LCSAJ coverage"));
			currentSheet.addCell(new Label(4, 0, "Branch coverage of LCSAJ Tests"));
			currentSheet.addCell(new Label(5, 0, "LCSAJ fitness"));
			currentSheet.addCell(new Label(6, 0, "Branch fitness"));
			currentSheet.addCell(new Label(7, 0, "LCSAJ length min"));
			currentSheet.addCell(new Label(8, 0, "LCSAJ length avg"));
			currentSheet.addCell(new Label(9, 0, "LCSAJ length max"));
			currentSheet.addCell(new Label(10, 0, "Infeasable LCSAJs"));
			currentSheet.addCell(new Label(11, 0, "Unfinished LCSAJs"));
			currentSheet.addCell(new Label(12, 0, "Infeasable Branches"));
			currentSheet.addCell(new Label(13, 0, "LCSAJ Test cases"));
			currentSheet.addCell(new Label(14, 0, "LCSAJ Test length"));
			
			
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	return currentSheet;
	}
private static WritableSheet setupSheetForBranchStatistics(){
		
		writtenWorkbook.createSheet(Properties.PROJECT_PREFIX, writtenWorkbook.getNumberOfSheets());
		WritableSheet currentSheet = writtenWorkbook.getSheet(Properties.PROJECT_PREFIX);
		try {
			
			currentSheet.addCell(new Label(0, 0, "Classname"));
			currentSheet.addCell(new Label(1, 0, "Branch Test cases"));
			currentSheet.addCell(new Label(2, 0, "Branch Test length"));
			currentSheet.addCell(new Label(3, 0, "Branch coverage"));
			currentSheet.addCell(new Label(4, 0, "Branch Fitness"));
			
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	return currentSheet;
	}
}
