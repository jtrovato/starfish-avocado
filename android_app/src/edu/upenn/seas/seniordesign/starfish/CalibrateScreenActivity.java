package edu.upenn.seas.seniordesign.starfish;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CalibrateScreenActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibrate_screen);
		
		final ListView listview = (ListView) findViewById(R.id.calibration_tests);
		String[] values = new String[]{"Test LEDs", "Test Pump", "Test Heat", "Test Valves"};
		
		final ArrayList<ListItem> list = new ArrayList<ListItem>();
		for(int i = 0; i < values.length; ++i){
			list.add(get(values[i]));
		}
		
		final ActionArrayAdapter adapter = new ActionArrayAdapter(this, list);
		listview.setAdapter(adapter);
	}
	
	private ListItem get(String s){
		return new ListItem(s);
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
