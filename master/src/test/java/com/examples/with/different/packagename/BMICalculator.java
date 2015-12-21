package com.examples.with.different.packagename;

public class BMICalculator {

	public static String calculateBMICategory(double height, double weight) {
		double bmi = weight / (height * height);
		if (bmi < 18.5) {
			return "underweight";
		} else if (bmi < 25) {
			return "healthy";
		} else if (bmi < 30) {
			return "overweight";
		} else if (bmi < 40) {
			return "obese";
		} else {
			return "very obese";
		}
	}
}
