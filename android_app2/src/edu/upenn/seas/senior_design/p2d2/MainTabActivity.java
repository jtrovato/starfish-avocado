package edu.upenn.seas.senior_design.p2d2;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

public class MainTabActivity extends FragmentActivity implements CvCameraViewListener2, OnTouchListener, InstructionsFragment.InstructionDialogListener {
	
	//static initalizer block  (runs when class is loaded)
	static{
		if(!OpenCVLoader.initDebug()){
			Log.e("openCV", "error initializing OpenCV");
		}
	}
	
	//Action Bar stuff
	ViewPager Tab;
    TabPagerAdapter TabAdapter;
	ActionBar actionBar;
	//image stuff
	public static String TAG="P2D2 MainTabAcitivity";
	//image control stuff
	public SeekBar isoBar;
	public SeekBar zoomBar;
	public SeekBar wbBar;
	//timer stuff
	public Button startButton;
	public Button stopButton;
	public TextView timer_value;
	public long startTime = 0L;
	public Handler customHandler = new Handler();
	public long timeInMilliseconds = 0L;
	public long timeSwapBuff = 0L;
	public long updatedTime = 0L;
	public long ref_time;
	//scheduler stuff
	public boolean testInProgress = false;
	public ScheduledExecutorService  scheduleTaskExecutor;
	//openCV stuff
	public Mat mRgba;
	public Mat mGray;
	public Mat mRgbaT;
	public CustomView mOpenCvCameraView;
	public Rect ROI;
	public Point[] points = new Point[8];
	public MatOfPoint cal_points;
	public int x;
	public int y;
	public int touch_count = -1;
	public boolean cal = false;
	public boolean boxup = false;
	public ArrayList<Rect> channels = new ArrayList<Rect>();
	ArrayList<Mat> rgb_channels = new ArrayList<Mat>();
	//store the fluo data
	public ArrayList<int[]> fluo_data = new ArrayList<int[]>();
	public ArrayList<Long> time_data = new ArrayList<Long>(); //in milliseconds
	public ArrayList<GraphViewData> graph_data1 = new ArrayList<GraphViewData>();
	//graph stuff
	public GraphViewData[] fluo_graph_data;// = new GraphViewData[]{};
	public GraphViewSeries fluo_series1;// = new GraphViewSeries("Channel 1", new GraphViewSeriesStyle(Color.rgb(200, 50, 00),2), fluo_graph_data);
	public GraphViewSeries fluo_series2;// = new GraphViewSeries("Channel 2", new GraphViewSeriesStyle(Color.rgb(90, 250, 00),2), fluo_graph_data);
	public GraphViewSeries fluo_series3;// = new GraphViewSeries("Channel 3", new GraphViewSeriesStyle(Color.rgb(0, 50, 250),2), fluo_graph_data);
	public GraphView fluo_graph;// = new LineGraphView(this, "Channel Fluorescence");

	private int maxDataLen = 50000;
	
	int graph_frag_id;
	GraphTab graph_fragment = (GraphTab)getSupportFragmentManager().findFragmentById(graph_frag_id);
	// Device stuff
	protected boolean actuated=false;
	DialogFragment actuationInstAlert;
	
	//Bluetooth commands
	byte[] turnLEDsOn = { (byte) 0xB8, (byte) 0xD3, (byte) 0x01, (byte) 0x3C,
			(byte) 0xFF };
	byte[] turnLEDsOff = { (byte) 0xB8, (byte) 0xD3, (byte) 0x01, (byte) 0x3C,
			(byte) 0x00 };
	
	
	//constructor, necessary?
	public MainTabActivity(){
		Log.i(TAG, "Instantiated new "+this.getClass());
	}
	
	//display view on screen
	public BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
		@Override
		public void onManagerConnected(int status){
			switch(status){
			case LoaderCallbackInterface.SUCCESS:
			{
				mRgba = new Mat();
				mGray = new Mat();
				mRgbaT = new Mat();
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(MainTabActivity.this);
			} break;
			default:
			{
				super.onManagerConnected(status);
				Log.d(TAG, "OpenCv was not loaded correctly");
			}break;
			}
		}
	};
	
	/**************************************************************************
	 * Bluetooth Initializations, service/broadcast reciever definitions
	 **************************************************************************/
	
	public BTConnectionService mBTService;
	private LocalBroadcastManager manager;

	// Defines callbacks for service binding, passed to bindService()
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBTService = ((BTConnectionService.LocalBinder) service)
					.getService();

			if (mBTService == null) {
				btNotConnected();
			} else if (!mBTService.isConnected()) {
				btNotConnected();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// unused
		}

	};

	private final BroadcastReceiver mBTDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String stringExtra = intent
					.getStringExtra(getString(R.string.bt_data_type));
			if (stringExtra == null) {
				Log.e(BLUETOOTH_SERVICE,
						"Null data type broadcast from BT service");
				return;
			}
			if (stringExtra.equals(getString(R.string.bt_error))) {
				processError(intent);
			} else if (stringExtra.equals(getString(R.string.bt_fluid_state))) {
				processFluidActuationState(intent);
			} else if (stringExtra.equals(getString(R.string.bt_heating_state))) {
				processHeatingState(intent);
			} else if (stringExtra.equals(getString(R.string.bt_led_state))) {
				processLEDState(intent);
			} else if (stringExtra.equals(getString(R.string.bt_temp_data))) {
				processTempData(intent);
			}
		}
	};

	private final BroadcastReceiver mBTStopReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean shutdown = intent.getBooleanExtra(
					getString(R.string.bt_disconnect_broadcast), false);
			if (shutdown) {
				unbindService(mConnection);
			}
		}
	};
	
	private final BroadcastReceiver mBTDisconnectReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			btNotConnected();
		}
	};
	
	private void btNotConnected() {
		new AlertDialogFragmentBT();
		DialogFragment btAlertFragment = AlertDialogFragmentBT
				.newInstance();
		btAlertFragment.show(getFragmentManager(), "no_BT");
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////   The Standard Activity Functions  /////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        
        //Action Bar and Tabs
        TabAdapter = new TabPagerAdapter(getSupportFragmentManager());
        
        Tab = (ViewPager)findViewById(R.id.pager);
        Tab.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                       
                    	actionBar = getActionBar();
                    	actionBar.setSelectedNavigationItem(position);                    }
                });
        Tab.setAdapter(TabAdapter);
        
        actionBar = getActionBar();
        //Enable Tabs on Action Bar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener(){

			@Override
			
			public void onTabReselected(android.app.ActionBar.Tab tab,
					FragmentTransaction ft) {
								
			}

			@Override
			 public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
	          
	            Tab.setCurrentItem(tab.getPosition());
	        }

			@Override
			public void onTabUnselected(android.app.ActionBar.Tab tab,
					FragmentTransaction ft) {
				
			}};
			//Add New Tab
			actionBar.addTab(actionBar.newTab().setText("Test").setTabListener(tabListener));
			actionBar.addTab(actionBar.newTab().setText("Graph").setTabListener(tabListener));
			actionBar.addTab(actionBar.newTab().setText("Logs").setTabListener(tabListener));
			
			//opencv camera view is implemented in TestTab()
			
			ref_time = updatedTime;
			//scheduled executor to take pictures at a certain rate.
			scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
			scheduleTaskExecutor.scheduleAtFixedRate(new Runnable(){
				@Override
				public void run(){
					//the task
					if(testInProgress)//causes unpredictable delays, but not an issue
					{
						Log.i(TAG, "taking picture now");
						mOpenCvCameraView.takePicture();
						//update the UI if necessary
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								//update the UI component here
							}
						});
					}
				}
			}, 10, 10, TimeUnit.SECONDS);

			// Bind to BT Connection
			Intent intent = new Intent(this, BTConnectionService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			
			// Register Broadcast receivers for BT Service
			manager = LocalBroadcastManager.getInstance(getApplicationContext());
			// Receiver for data
			IntentFilter dataFilter = new IntentFilter(
					BTConnectionService.ACTION_BT_RECIEVED);
			manager.registerReceiver(mBTDataReceiver, dataFilter);
			// Receiver for stopping service
			IntentFilter stopFilter = new IntentFilter(
					BTConnectionService.ACTION_BT_STOP);
			manager.registerReceiver(mBTStopReceiver, stopFilter);
			
			// Receiver for BT Disconnect
			IntentFilter disconnectFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
			disconnectFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			registerReceiver(mBTDisconnectReceiver, disconnectFilter);
			
			actuationInstruction();
			if(mBTService==null)
			{
				Log.d(TAG, "mBTService is null");
			}
			testInstruction();
			
    }
    
    @Override
	protected void onPause()
	{
    	Log.d(TAG, "onPause");
		super.onPause();

	}
	@Override
	protected void onResume()
	{
		Log.d(TAG, "onResume");
		super.onResume();
		
		
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(mConnection);
		manager.unregisterReceiver(mBTDataReceiver);
		manager.unregisterReceiver(mBTStopReceiver);
		unregisterReceiver(mBTDisconnectReceiver);
		//mOpenCvCameraView.releaseCam();
		if(mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////   Misc. Functions           ////////////////////////////////////////////////////////
	
	//this is a worker thread for the timer
	public Runnable updateTimerThread = new Runnable(){
		@Override
		public void run(){
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			updatedTime = timeSwapBuff + timeInMilliseconds; //in ms

			int secs = (int)(updatedTime/1000);
			int mins = secs/60;
			secs = secs%60;
			int milliseconds = (int)(updatedTime%1000);
			timer_value.setText("" + mins + ":" 
					+ String.format("%02d",  secs) + ":"
					+ String.format("%03d", milliseconds));
			customHandler.postDelayed(this, 0);

		}
	};

	//instructions
	private void testInstruction() {
		DialogFragment testInstAlert = new InstructionsFragment().newInstance();
		Bundle bundle = new Bundle();
		bundle.putInt("inst", 1); //1 corresponds to test instructions
		testInstAlert.setArguments(bundle);
		testInstAlert.show(getFragmentManager(), "test_inst");
		//mBTService.writeToBT(turnLEDsOn);
		}
	
	private void imageCalInstruction() {
		DialogFragment testInstAlert = new InstructionsFragment().newInstance();
		Bundle bundle = new Bundle();
		bundle.putInt("inst", 2); //2 corresponds to image cal
		testInstAlert.setArguments(bundle);
		testInstAlert.show(getFragmentManager(), "cal_inst");
		}
	
	private void actuationInstruction() {
		actuationInstAlert = new InstructionsFragment().newInstance();
		Bundle bundle = new Bundle();
		bundle.putInt("inst", 5); //5 corresponds to actuation
		actuationInstAlert.setArguments(bundle);
		actuationInstAlert.show(getFragmentManager(), "act_inst");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "actuated is true");
		actuated = true;
		actuationInstAlert.dismiss();
		//testInstruction();
		}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////   Camera View Functions           /////////////////////////////////////////////////
  	
  	@Override
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
		
		
	}

	@Override
	public void onCameraViewStopped() {
		
	}

	@Override
	/* this method should only be used for displaying real time images */
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		//note: Mat.t() and Core.split() has a memory leak (causes the app to crash in some other way)
		//UPDATE: fixed the memory leaks by releasing the Mats used in this function but the app still crashes after a few seconds.	
		mRgba.release();
		mRgbaT.release();
		for(Mat m : rgb_channels)
		{
			m.release();
		}
		mRgba = inputFrame.rgba();
		//rotate view
		//mRgbaT = mRgba.t();
		//Core.flip(mRgba.t(), mRgbaT, 1);
		//Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
		//Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGRA2GRAY);
		Core.split(mRgba, rgb_channels);
		Mat ch_g = rgb_channels.get(1);
		
		
		if(touch_count == -1)
		{
			mBTService.writeToBT(turnLEDsOn);
			touch_count = 0;
		}
		
		if(touch_count > 8) //if the calibration routine is complete
		{
			
			if(cal == false && boxup == false)
			{
				boxup = true;
				imageCalInstruction();
				
			}
			int[] fluo = ImageProc.getFluorescence(mRgba, channels);
			if(updatedTime > ref_time+1000)
			{
				Log.d(TAG, "writing data");
				//store data, graph prep
				fluo_data.add(fluo);
				time_data.add(updatedTime);
				//graph_data1.add(new GraphViewData((double)time_data.get(time_data.size()-1),fluo_data.get(fluo_data.size()-1)[0]));
				fluo_series1.appendData(new GraphViewData((double)updatedTime/1000, fluo[0]), false, maxDataLen);
				fluo_series2.appendData(new GraphViewData((double)updatedTime/1000, fluo[1]), false, maxDataLen);
				fluo_series3.appendData(new GraphViewData((double)updatedTime/1000, fluo[2]), false, maxDataLen);
				//graph_fragment.redrawAll(); //need this to plot the graph

				Log.i("fluorescence values", Double.toString(fluo[0]) + " " + Double.toString(fluo[1]) + " " + Double.toString(fluo[2]));
				ref_time=updatedTime;
				
			}
			//outline ROI and Channels
			Core.rectangle(mRgba, ROI.tl(),ROI.br(),new Scalar( 255, 0, 0 ),4,8, 0 );
			int i =0;
			for(Rect c : channels)
			{
				Core.rectangle(mRgba, c.tl(), c.br(), new Scalar( 0, 255, 0 ),2,8, 0 );
				Core.putText(mRgba, Integer.toString(fluo[i]), new Point(c.x - c.width*(0.09/(2*0.04))  , c.y + c.height + 20), 
					    Core.FONT_HERSHEY_COMPLEX, 0.8, new Scalar(200,200,250), 1);
				i++;
			}
		}
		
		//return rgb_channels.get(1); //display the green channel
		return mRgba;
	}
	
	
	
	@Override
	/* this method is executed every time the user touches the screen.camera view */
	public boolean onTouch(View arg0, MotionEvent event) {
		double cols = mRgba.cols();
		double rows = mRgba.rows();

		double xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
		double yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
		x = (int)((event).getX() - xOffset);
		y = (int)((event).getY() - yOffset);
		
		
		if(touch_count > 8)
		{
			
		}
		else if(touch_count == 8)
		{
			cal_points = new MatOfPoint();
			cal_points.fromArray(points);
			ROI = Imgproc.boundingRect(cal_points);
			channels = ImageProc.findChannels(ROI);
			
		} else {
			points[touch_count] = new Point(x,y);
		}
		
		x = (int)((event).getX() - xOffset);
		y = (int)((event).getY() - yOffset);
		
		Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

		
		touch_count++;
		
		return false;
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		cal = true;
		boxup = false;
		
	}



	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		touch_count = 0;
		boxup=false;
		
	}
	
	
	/***************************************************************************
	 * Methods for processing BT Data
	 **************************************************************************/
	private void processTempData(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFF);
		if (dataOne == (byte) 0xFF) {
			Log.e(BLUETOOTH_SERVICE, "Bad temp data reached processTempData()");
			return;
		}
		byte dataTwo = intent.getByteExtra(getString(R.string.bt_data_2),
				(byte) 0xFF);
		int temp = dataOne * 256 + dataTwo;
		String tempDataString = Integer.toString(temp);
		Log.i(BLUETOOTH_SERVICE, "Temperature = " + tempDataString + " Celcius");
	}

	private void processHeatingState(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFA);
		switch (dataOne) {
		case (byte) 0x00:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Stopped");
			break;
		case (byte) 0x31:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heating to Temp 1");
			break;
		case (byte) 0x33:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heated to Temp 1");
			break;
		case (byte) 0x51:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heating to Temp 2");
			break;
		case (byte) 0x55:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heated to Temp 2");
			break;
		case (byte) 0x62:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heating to Temp 3");
			break;
		case (byte) 0x66:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heated to Temp 3");
			break;
		case (byte) 0xF7:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heating to Temp 4");
			break;
		case (byte) 0xFF:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heated to Temp 4");
			break;
		default:
			Log.e(BLUETOOTH_SERVICE,
					"Bad fluid actuation state reached processFluidActuationState()");
			break;
		}
	}

	private void processLEDState(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFA);
		switch (dataOne) {
		case (byte) 0x00:
			Log.i(BLUETOOTH_SERVICE, "LEDS: OFF");
			break;
		case (byte) 0xFF:
			Log.i(BLUETOOTH_SERVICE, "LEDS: ON");
			break;
		default:
			Log.e(BLUETOOTH_SERVICE, "Bad LED state reached processLEDState()");
			break;
		}
	}

	private void processFluidActuationState(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFA);
		switch (dataOne) {
		case (byte) 0x00:
			Log.i(BLUETOOTH_SERVICE, "Fluids: not yet actuated");
			break;
		case (byte) 0x44:
			Log.i(BLUETOOTH_SERVICE, "Fluids: currently actuating");
			break;
		case (byte) 0xFF:
			Log.i(BLUETOOTH_SERVICE, "Fluids: already actuated");
			actuated = true;
			actuationInstAlert.dismiss();
			testInstruction();
			break;
		default:
			Log.e(BLUETOOTH_SERVICE,
					"Bad fluid actuation state reached processFluidActuationState()");
			break;
		}
	}

	private void processError(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFF);
		switch (dataOne) {
		case (byte) 0x11:
			Log.e(BLUETOOTH_SERVICE, "Temperature out of range!");
			break;
		default:
			Log.e(BLUETOOTH_SERVICE, "Bad error intent reached processError()");
			break;
		}
	}
	
	private class ActuatedListener implements Runnable {

		@Override
		public void run() {
			byte[] check = {(byte)0xB8, (byte)0xD3, (byte)0x01, (byte)0xFF, (byte)0x8F};
			while(!actuated){
				mBTService.writeToBT(check);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}
