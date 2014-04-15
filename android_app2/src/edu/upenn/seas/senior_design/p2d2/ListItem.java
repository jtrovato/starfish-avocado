package edu.upenn.seas.senior_design.p2d2;

public class ListItem {
	
	//fields for the name of a test, the result of a test, and the method
	//that should be run when the item is pressed
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
	
	public boolean doWork()
	{
		try{
			testResult = !testResult;
			Thread.sleep(2000);
		}catch (InterruptedException e){
			e.printStackTrace();
		}
		
		return testResult;
	}
}
