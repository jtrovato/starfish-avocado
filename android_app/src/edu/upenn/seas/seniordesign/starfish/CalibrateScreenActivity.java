package edu.upenn.seas.seniordesign.starfish;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

/**
 * 
 * @author Martin
 * Main activity of the calibration screen
 */

public class CalibrateScreenActivity extends Activity{

	ActionArrayAdapter adapter;
	ListView listview;
	
	ArrayList<ListItem> list;
	
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
		list = new ArrayList<ListItem>();
		for(int i = 0; i < values.length; ++i){
			list.add(new ListItem(values[i]));
		}
		
		adapter = new ActionArrayAdapter(this, list);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int pos,
                    long id) {
                
                final ImageView icon = (ImageView)view.findViewById(R.id.passed_test_icon);
                final ProgressBar bar = (ProgressBar)view.findViewById(R.id.progress_wheel_icon);
                             
                class Task extends AsyncTask<Integer, Void, Boolean>{

                    protected void onPreExecute()
                    {
                        icon.setVisibility(View.GONE);
                        bar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Boolean doInBackground(Integer... params) {
                        boolean result = false;
                        switch(params[0]){
                            case 0: result = ledTest();
                                    break;
                            case 1: result = pumpTest();
                                    break;
                            case 2: result = heatTest();
                                    break;
                            case 3: result = valvesTest();
                                    break;
                            default: break;
                        }
                        return result;
                    }
                    
                    @Override
                    protected void onPostExecute(Boolean result){
                        
                        if(result)
                            icon.setImageResource(R.drawable.checkmark);
                        else
                            icon.setImageResource(R.drawable.crossout);
                        
                        icon.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.GONE);
                    }
                }
                
                new Task().execute(pos);
            }	    
		});
		
		Button testAll = (Button)findViewById(R.id.test_all_button);
		testAll.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                testAll();
            }
		});
	}
	
	
	
	/**
	 * Calls all the testing methods from one location
	 * @param view
	 */
	public void testAll()
	{
	    class Task extends AsyncTask<Integer, Void, Boolean>{

	        View v;
	        ImageView icon;
	        ProgressBar bar;
	        int index;
	        
	        public Task(int i){
	            index = i;
	            v = listview.getChildAt(index);
	            
	            if(v != null){
	                icon = (ImageView) v.findViewById(R.id.passed_test_icon);
	                bar = (ProgressBar) v.findViewById(R.id.progress_wheel_icon);
	            }
	        }
	        
	        protected void onPreExecute()
            {
                icon.setVisibility(View.GONE);
                bar.setVisibility(View.VISIBLE);
            }
	        
            @Override
            protected Boolean doInBackground(Integer... params) {
                boolean result = false;
                switch(params[0]){
                    case 0: result = ledTest();
                            break;
                    case 1: result = pumpTest();
                            break;
                    case 2: result = heatTest();
                            break;
                    case 3: result = valvesTest();
                            break;
                    default: break;
                }
                return result;
            }
	        
            @Override
            protected void onPostExecute(Boolean result){
                
                if(result)
                    icon.setImageResource(R.drawable.checkmark);
                else
                    icon.setImageResource(R.drawable.crossout);
                
                icon.setVisibility(View.VISIBLE);
                bar.setVisibility(View.GONE);
                
                if(listview.getChildAt(index+1) != null)
                    new Task(index+1).execute(index+1);
            }
	    }
	    
	    new Task(0).execute(0);
	}
	
	public boolean ledTest()
	{
	    Log.i("Calibrate", "ledTest");
	    try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    
	    list.get(0).setTestResult(!list.get(0).testPassed());
	    
	    return list.get(0).testPassed();
	}
	
	public boolean pumpTest()
	{
	    Log.i("Calibrate", "pumpTest");
	    try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    
	    list.get(1).setTestResult(!list.get(1).testPassed());
        
        return list.get(1).testPassed();
	}
	
	public boolean heatTest()
	{
	    Log.i("Calibrate", "heatTest");
	    try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    
	    list.get(2).setTestResult(!list.get(2).testPassed());
        
        return list.get(2).testPassed();
	}
	
	public boolean valvesTest()
	{
	    Log.i("Calibrate", "valvesTest");
	    try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    
	    list.get(3).setTestResult(!list.get(3).testPassed());
        
        return list.get(3).testPassed();
	}
}
