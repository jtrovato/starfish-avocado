package edu.upenn.seas.seniordesign.starfish;

public class ListItem {
	private String text;
	private boolean testResult;
	
	public ListItem(String text){
		this.text = text;
		testResult = false;
	}
	
	public String getText(){
		return text;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public boolean testPassed(){
		return testResult;
	}
	
	public void setTestResult(boolean testResult){
		this.testResult = testResult;
	}
}
