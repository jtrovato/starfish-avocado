package edu.upenn.seas.senior_design.p2d2;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainTabActivity extends FragmentActivity implements CvCameraViewListener2, OnTouchListener {
	
	//Action Bar stuff
	ViewPager Tab;
    TabPagerAdapter TabAdapter;
	ActionBar actionBar;
	//image stuff
	public static String TAG="P2D2 MainTabAcitivity";
	//image control stuff
	private SeekBar isoBar;
	private SeekBar zoomBar;
	private SeekBar wbBar;
	//timer stuff
	private Button startButton;
	private Button stopButton;
	private TextView timer_value;
	private long startTime = 0L;
	private Handler customHandler = new Handler();
	long timeInMilliseconds = 0L;
	long timeSwapBuff = 0L;
	long updatedTime = 0L;
	//scheduler stuff
	private boolean testInProgress = false;
	private ScheduledExecutorService  scheduleTaskExecutor;
	//openCV stuff
	private Mat mRgba;
	private Mat mGray;
	private Mat mRgbaT;
	private CustomView mOpenCvCameraView;
	private Rect ROI;
	private Point[] points = new Point[8];
	private MatOfPoint cal_points;
	private int x;
	private int y;
	private int touch_count = 0;
	private ArrayList<Rect> channels = new ArrayList<Rect>();
	ArrayList<Mat> rgb_channels = new ArrayList<Mat>();
	//store the fluo data
	public ArrayList<int[]> fluo_data;
	
	//constructor, necessary?
	public MainTabActivity(){
		Log.i(TAG, "Instantiated new "+this.getClass());
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
				// TODO Auto-generated method stub
				
			}

			@Override
			 public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
	          
	            Tab.setCurrentItem(tab.getPosition());
	        }

			@Override
			public void onTabUnselected(android.app.ActionBar.Tab tab,
					FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}};
			//Add New Tab
			actionBar.addTab(actionBar.newTab().setText("Test").setTabListener(tabListener));
			actionBar.addTab(actionBar.newTab().setText("Graph").setTabListener(tabListener));
			actionBar.addTab(actionBar.newTab().setText("Logs").setTabListener(tabListener));
			
			//The Camera View is implemented in the fragment

			

    }
    
    @Override
	protected void onPause()
	{
		super.onPause();
		if(mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		//OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5,  this, mLoaderCallback);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
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
	/////////////////////   Camera View Functions           /////////////////////////////////////////////////
	/*
    //display view on screen
  	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
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
  			}break;
  			}
  		}
  	};*/
/*
	@Override
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();		
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	// this method should only be used for displaying real time images //
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
		
		
		if(touch_count > 8) //if the calibration routine is complete
		{
			int[] fluo = ImageProc.getFluorescence(mRgba, channels);
			fluo_data.add(fluo);
			int i =0;
			Log.i("fluorescence values", Double.toString(fluo[0]) + " " + Double.toString(fluo[1]) + " " + Double.toString(fluo[2]));
			//outline ROI and Channels
			Core.rectangle(mRgba, ROI.tl(),ROI.br(),new Scalar( 255, 0, 0 ),4,8, 0 );
			for(Rect c : channels)
			{
				Core.rectangle(mRgba, c.tl(), c.br(), new Scalar( 0, 255, 0 ),2,8, 0 );
				Core.putText(mRgba, Integer.toString(fluo[i]), new Point(c.x - c.width*(0.09/(2*0.04))  , c.y + c.height + 20), 
					    Core.FONT_HERSHEY_COMPLEX, 0.8, new Scalar(200,200,250), 1);
				i++;
			}
			//store the data
			
			
			
			
		}
		
		//return rgb_channels.get(1); //display the green channel
		return mRgba;
	}
	


	
	//@Override
	//this method is executed every time the user touches the screen.camera view 
	public boolean onTouch(View arg0, MotionEvent event) 
	{
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
 */

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		return null;
	}
    
}