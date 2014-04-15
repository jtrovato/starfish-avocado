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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity implements CvCameraViewListener2, OnTouchListener{
	
	//static initalizer block  (runs when class is loaded)
	static{
		if(!OpenCVLoader.initDebug()){
			Log.e("openCV", "error initializing OpenCV");
		}
	}

	//image stuff

	public static String TAG="P2D2 camera";
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
	
	//constructor, necessary?
	public TestActivity(){
		Log.i(TAG, "Instantiated new "+this.getClass());
	}
	
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
				mOpenCvCameraView.setOnTouchListener(TestActivity.this);
			} break;
			default:
			{
				super.onManagerConnected(status);
			}break;
			}
		}
	};
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_test);
		mOpenCvCameraView = (CustomView)findViewById(R.id.test_activity_java_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		//set up timer
		timer_value = (TextView) findViewById(R.id.timer_value);
		//start button
		startButton = (Button) findViewById(R.id.button_start);
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startTime = SystemClock.uptimeMillis();
				customHandler.postDelayed(updateTimerThread, 0);
			    testInProgress = true;
				
			}
		});
		//stopButton
		stopButton = (Button)findViewById(R.id.button_stop);
		stopButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				timeSwapBuff += timeInMilliseconds;
				customHandler.removeCallbacks(updateTimerThread);
				testInProgress = false;
				//start the Results Activity using an intent
				Intent resultsIntent = new Intent(TestActivity.this, ResultsActivity.class);
				//myIntent.putExtra("key", value); //to pass info if needed
				TestActivity.this.startActivity(resultsIntent);
			}
		});

		//zoom settings
		//not really necessary because it is all digital zoom, but easier to select channels
		zoomBar = (SeekBar) findViewById(R.id.seekbar_zoom);
		zoomBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int zoom;
			int maxZoom;
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				maxZoom = mOpenCvCameraView.getMaxZoom();
				zoom = (int)((maxZoom/30)*progress);
				mOpenCvCameraView.setZoom(zoom);
			}
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				//TODO
			}
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				Toast.makeText(TestActivity.this, "zoom:" + Integer.toString(zoom),
						Toast.LENGTH_SHORT).show();
			}
		});
		//exposure settings
		isoBar = (SeekBar) findViewById(R.id.seekbar_iso);
		isoBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			String iso;
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				mOpenCvCameraView.setExposureCompensation(progress);
			}
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				//TODO
			}
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				Toast.makeText(TestActivity.this, "iso:" + iso,
						Toast.LENGTH_SHORT).show();
			}
		});
		//white balance settings
		wbBar = (SeekBar) findViewById(R.id.seekbar_wb);
		wbBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			String wb;
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				switch(progress){
					case 0: wb=Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT;
						break;
					case 1: wb=Camera.Parameters.WHITE_BALANCE_DAYLIGHT;
						break;
					case 2: wb=Camera.Parameters.WHITE_BALANCE_FLUORESCENT;
						break;
					case 3: wb=Camera.Parameters.WHITE_BALANCE_INCANDESCENT;
						break;
					case 4: wb=Camera.Parameters.WHITE_BALANCE_SHADE;
						break;
					case 5: wb=Camera.Parameters.WHITE_BALANCE_TWILIGHT;
						break;
					case 6: wb=Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT;
						break;
					default: wb=Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT;
						break;
				}
				mOpenCvCameraView.setWhiteBalance(wb);
			}
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				//TODO
			}
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				Toast.makeText(TestActivity.this, "wb:" + wb,
						Toast.LENGTH_SHORT).show();
			}
		});

		//scheduled executor to take pictures at a certain rate.
		scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		scheduleTaskExecutor.scheduleAtFixedRate(new Runnable(){
			public void run(){
				//the task
				if(testInProgress)//causes unpredictable delays, but not an issue
				{
					mOpenCvCameraView.takePicture();
					//update the UI if necessary
					runOnUiThread(new Runnable() {
						public void run() {
							//update the UI component here
						}
					});
				}
			}
		}, 10, 10, TimeUnit.SECONDS);
		
		Button captureButton = (Button)findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mOpenCvCameraView.takePicture();
				
			}
		});
	} 

	//this is a worker thread for the timer
	private Runnable updateTimerThread = new Runnable(){
		public void run(){
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			updatedTime = timeSwapBuff + timeInMilliseconds;
			
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
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5,  this, mLoaderCallback);
	}
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
	
	/*
	 * detect if this device has a camera
	 */
	private boolean checkCameraHardware(Context context)
	{
		if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
		{
			//the device has a camera
			return true;
		} else
		{
			//device does not have a camera
			return false;
		}
	}

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
		
		
		if(touch_count > 8)
		{
			double[] fluo = ImageProc.getFluorescence(mRgba, channels);
			int i =0;
			Log.i("fluorescence values", Double.toString(fluo[0]) + " " + Double.toString(fluo[1]) + " " + Double.toString(fluo[2]));
			//outline ROI and Channels
			Core.rectangle(mRgba, ROI.tl(),ROI.br(),new Scalar( 255, 0, 0 ),4,8, 0 );
			for(Rect c : channels)
			{
				Core.rectangle(mRgba, c.tl(), c.br(), new Scalar( 0, 255, 0 ),2,8, 0 );
				Core.putText(mRgba, Integer.toString((int)fluo[i]), new Point(c.x - c.width*(0.09/(2*0.04))  , c.y + c.height + 20), 
					    Core.FONT_HERSHEY_COMPLEX, 0.8, new Scalar(200,200,250), 1);
				i++;
			}
			//display fluo text
			
			
			
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
	
}
