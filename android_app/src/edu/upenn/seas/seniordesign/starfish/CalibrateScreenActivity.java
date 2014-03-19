package edu.upenn.seas.seniordesign.starfish;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * @author Martin
 * Main activity of the calibration screen
 */

public class CalibrateScreenActivity extends Activity{

	ActionArrayAdapter adapter;
	ListView listview;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibrate_screen);
		
		//Listview that displays the calibration tests to be done. The behavior of listview is
		//determined by the ActionArrayAdapter adapter
		listview = (ListView) findViewById(R.id.calibration_tests);
		
		//String values for the calibration tests that are done
		//Note: Find a way to move string values to string res
		String[] values = new String[]{"Test LEDs", "Test Pump", "Test Heat", "Test Valves"};
		
		//constructing the list of ListItems that hold the name, boolean result, and test method
		//for each calibration test
		//ListItems are processed inside ActionArrayAdapter
		final ArrayList<ListItem> list = new ArrayList<ListItem>();
		for(int i = 0; i < values.length; ++i){
			list.add(new ListItem(values[i]));
		}
		
		adapter = new ActionArrayAdapter(this, list);
		listview.setAdapter(adapter);
	}
	
	/**
	 * Calls all the testing methods from one location
	 * @param view
	 */
	public void testAll(View view)
	{

	}
	
	public void ledTest(View view)
	{
		
	}
	
	public void pumpTest(View view)
	{
		
	}
	
	public void heatTest(View view)
	{
		
	}
	
	public void valvesTest(View view)
	{
		
	}
}
