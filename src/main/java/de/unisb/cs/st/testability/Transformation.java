package de.unisb.cs.st.testability;

/**
 * Created by Yanchuan Li Date: 2/11/11 Time: 11:00 PM
 */
public class Transformation {
	private String classname;
	private String methodname;
	private String event;
	private int count;

	public Transformation(String classname, String methodname, String event, int count) {
		this.classname = classname;
		this.methodname = methodname;
		this.event = event;
		this.count = count;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getMethodname() {
		return methodname;
	}

	public void setMethodname(String methodname) {
		this.methodname = methodname;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
