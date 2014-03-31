package edu.upenn.seas.senior_design.p2d2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity implements CvCameraViewListener2{
	
	//static initalizer block  (runs when class is loaded)
	static{
		if(!OpenCVLoader.initDebug()){
			Log.e("openCV", "error initializing OpenCV");
		}
	}

	//image stuff
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static String TAG="P2D2 camera";
	private Camera mCamera;
	private CameraPreview mPreview;
	//image control stuff
	Camera.Parameters parameters;
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
	private CameraBridgeViewBase mOpenCvCameraView;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		//set up timer
		timer_value = (TextView) findViewById(R.id.timer_value);
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
		
		//create an instance of Camera
		mCamera = getCameraInstance();
		
		//edit camera parameters (focus and zoom)
		parameters = mCamera.getParameters(); //need a parameters object to change anything
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    // Running on something older than API level 14, so cannot lock WB or AE
			if(!parameters.isAutoExposureLockSupported() || !parameters.isAutoWhiteBalanceLockSupported())
			{
				Log.w("camera API", "camera cannot be locked into manual mode");
			}
		}
		//parameters to be set when setting are optimized
		/*parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_SHADE);
		parameters.setZoom(ideal_zoom);
		*/
		List<String> focusModes = parameters.getSupportedFocusModes(); //set focus to MACRO (close-up)
		if(focusModes.contains(Parameters.FOCUS_MODE_MACRO))
		{
			parameters.setFocusMode(Parameters.FOCUS_MODE_MACRO);
		}
		parameters.setZoom(0);
		mCamera.setParameters(parameters);
		mCamera.setDisplayOrientation(90); //this seems to do nothing
		
		//zoom settings
		zoomBar = (SeekBar) findViewById(R.id.seekbar_zoom);
		zoomBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int zoom;
			int maxZoom;
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				maxZoom = parameters.getMaxZoom();
				zoom = (int)((maxZoom/30)*progress);
				parameters.setZoom(zoom);
				mCamera.setParameters(parameters);
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
		//iso settings
		//String supportedIsoValues = parameters.get("iso-values"); //this returns a null value on some devices
		isoBar = (SeekBar) findViewById(R.id.seekbar_iso);
		isoBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			String iso;
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				switch(progress){
					case 0: iso="100";
						break;
					case 1: iso="200";
						break;
					case 2: iso="400";
						break;
					case 3: iso="800";
						break;
					case 4: iso="1600";
						break;
					default: iso="800";
						break;
				}
				parameters.set("iso", iso);
				mCamera.setParameters(parameters);
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
				parameters.setWhiteBalance(wb);
				mCamera.setParameters(parameters);
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

		
		
		
		
		//create our preview view and set it as the content of the activity
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		//scheduled executor to take pictures at a certain rate.
		scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		scheduleTaskExecutor.scheduleAtFixedRate(new Runnable(){
			public void run(){
				//the task
				if(testInProgress)//causes unpredictable delays, but not an issue
				{
					mCamera.takePicture(null, null, mPicture);
					//update the UI if necessary
					runOnUiThread(new Runnable() {
						public void run() {
							//update the UI component here
						}
					});
				}
			}
		}, 10, 10, TimeUnit.SECONDS);
		//a wakelock will keep the phone from going to sleep
		//PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
		//wl.acquire(7200000); //keep the wake lock on for 2 hours (max test time)
		
		
		Button captureButton = (Button)findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mCamera.takePicture(null, null, mPicture);
				
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
		releaseCamera();
	}
	
	private void releaseCamera()
	{
		if(mCamera != null)
		{
			mCamera.release();
			mCamera = null;
		}
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
	/*
	 * Access the camera in a safe way
	 */
	public static Camera getCameraInstance()
	{
		Camera c = null;
		try {
			c = Camera.open(); //attempt to get a camera instance
		} catch(Exception e){
			//camera is not available
		}
		return c;
	}
	class myCallback implements PictureCallback
	{
		public void onPictureTaken(byte[] data, Camera camera)
		{
			camera.startPreview(); //restarts the preview after taking a picture
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "file not found" + e.getMessage());
				//e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG, "error accessing file" + e.getMessage());
				//e.printStackTrace();
			}
		}
	}
	private myCallback mPicture = new myCallback();
	
	/*
	private PictureCallback mPicture = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if(pictureFile == null)
			{
				Log.d(TAG, "Error creating media file, check storage permissions: ");
				return;
			}
			//cannot get android to resolve the exceptions
			
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch(FileNotFoundExecption e){
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch(IOExecption e){
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
		}
	};
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "P2D2");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
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
