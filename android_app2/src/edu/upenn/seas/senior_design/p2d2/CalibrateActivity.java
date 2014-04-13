package edu.upenn.seas.senior_design.p2d2;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class CalibrateActivity extends Activity {
    ActionArrayAdapter adapter;    
    // adapter is passed to ListView in order to initialize it 
    
    ListView listview;
    // listview holds the items that are touched by the user to run calibration
    // tests
    
    ArrayList<ListItem> list;
    // Holds ListItems that contains data for the tests in the ListView items
    
    public static final String TAG = 
            "edu.upenn.seas.seniordesign.starfish.CalibrateScreenActivity";
    
    private BTConnectionService mBTService;
    
    // Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBTService = ((BTConnectionService.LocalBinder) service)
                    .getService();
            
            if(mBTService == null){
                Toast.makeText(getApplicationContext(), "Bluetooth not connected", Toast.LENGTH_SHORT).show();
                //close activity
            }
            else if(!mBTService.isConnected()){
                Toast.makeText(getApplicationContext(), "Bluetooth not connected", Toast.LENGTH_SHORT).show();
                //close activity
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //unused
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);
        
        //Listview that displays the calibration tests to be done.
        listview = (ListView) findViewById(R.id.calibration_tests);
        
        //String values for the calibration tests that are done
        //Note: Find a way to move string values to string res
        String[] values = new String[]
                {"Test LEDs", "Test Pump", "Test Heat"};
        
        //constructing the ArrayList of ListItems that hold data for the test
        //results. ListItems are processed inside ActionArrayAdapter
        list = new ArrayList<ListItem>();
        for(int i = 0; i < values.length; ++i){
            list.add(new ListItem(values[i]));
        }
        
        //add adapter to listview
        adapter = new ActionArrayAdapter(this, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, 
                    int pos, long id) {
                Log.i(TAG, "listview onItemClick()");
                
                //create a new TestTask, which only runs one test
                new TestTask(pos, false).startTest();              
            }       
        });
        
        //button for testing all calibration tests
        Button testAll = (Button)findViewById(R.id.test_all_button);
        testAll.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button testAll onClick()");
                
                new TestTask(0, true).startTest();
            }
        });
        
        
    }//end of onCreate()
    
    //AsyncTask allows for test to be run off of the main Thread
    //uses an integer as an input parameter (test to be run)
    //returns void to track progress (no progress tracking data)
    //returns boolean as return value (test succeeded or failed)
    private class TestTask extends AsyncTask<Integer, Void, Boolean>{

        //holds the the elements of the View corresponding to the test
        //being run
        View v;
        ImageView icon;
        ProgressBar bar;
        
        //index of the test, and whether all tests are being run
        int index;
        boolean testAll;
        
        public TestTask(int i, boolean enableTestAll){
            Log.i(TAG, "create new TestTask");
            
            index = i;
            v = listview.getChildAt(index);
            
            if(v != null){
                icon = (ImageView) v.findViewById(R.id.passed_test_icon);
                bar = (ProgressBar) v.findViewById(R.id.progress_wheel_icon);
            }
            
            testAll = enableTestAll;
        }
        
        public void startTest(){
            Log.i(TAG, "TestTask startTest()");
            
            this.execute(index);
        }
        
        //overridden AsyncTask methods
        
        @Override
        protected void onPreExecute()
        {
            Log.i(TAG, "AsyncTask onPreExecute()");
            
            icon.setVisibility(View.GONE);
            bar.setVisibility(View.VISIBLE);
        }
        
        @Override
        protected Boolean doInBackground(Integer... params) {
            Log.i(TAG, "AsyncTask doInBackground()");
            
            return selectTest(params[0]);
        }
        
        @Override
        protected void onPostExecute(Boolean result){
            Log.i(TAG, "AsyncTask onPostExecute()");
            
            if(result)
                icon.setImageResource(R.drawable.checkmark);
            else
                icon.setImageResource(R.drawable.crossout);
            
            icon.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
            
            if(listview.getChildAt(index+1) != null && testAll)
                new TestTask(index+1, true).startTest();
        }
    }

    /*
     * Testing methods 
     * ledTest()
     * pumpTest()
     * heatTest()
     * valvesTest()
     */
    
    //run different tests, select using an int value
    public boolean selectTest(int select){
        boolean result = false;
        switch(select){
            case 0: result = ledTest();
                    break;
            case 1: result = pumpTest();
                    break;
            case 2: result = heatTest();
                    break;
            default: break;
        }
        return result;
    }
    
    public boolean ledTest()
    {
        byte[] bytes = {(byte)0xB8, (byte)0xD3, (byte)0x01, (byte)0x3C, (byte)0xFF};
        mBTService.writeToBT(bytes);

        list.get(0).setTestResult(!list.get(0).testPassed());
        
        return list.get(0).testPassed();
    }
    
    public boolean pumpTest()
    {
        byte[] bytes = {(byte)0xB8, (byte)0xD3, (byte)0x01, (byte)0x8F, (byte)0xFF};
        mBTService.writeToBT(bytes);
        
        list.get(1).setTestResult(!list.get(1).testPassed());
        
        return list.get(1).testPassed();
    }
    
    public boolean heatTest()
    {
        byte[] bytes = {(byte)0xB8, (byte)0xD3, (byte)0x01, (byte)0x57, (byte)0x33};
        mBTService.writeToBT(bytes);
        
        list.get(2).setTestResult(!list.get(2).testPassed());
        
        return list.get(2).testPassed();
    }
}
