package edu.upenn.seas.senior_design.p2d2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.opencv.android.JavaCameraView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;

public class CustomView extends JavaCameraView {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private Camera.Parameters parameters;
    private static final String TAG = "Custom Open CV Camera View";

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @SuppressLint("NewApi")
	public void lockCamera(){
    	parameters = mCamera.getParameters();
    	if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    // Running on something older than API level 14, so cannot lock WB or AE
			if(!parameters.isAutoExposureLockSupported())
			{
				Log.w("camera Locks", "Auto Exposure Lock not Supported");
			}
			else
			{
				parameters.setAutoExposureLock(true);
				Log.i("camera Locks", "Auto Exposure Lock enabled");
			}
			if(!parameters.isAutoWhiteBalanceLockSupported())
			{
				Log.w("camera Locks", "Auto White Balance Lock not Supported");
			}
			else
			{
				parameters.setAutoWhiteBalanceLock(true);
				Log.i("camera Locks", "Auto White Balance Lock enabled");
			}
		}
    	mCamera.setParameters(parameters);//actually set the parameters
    }

    public void setMacroFocus(){
    	parameters = mCamera.getParameters();
		List<String> focusModes = parameters.getSupportedFocusModes(); //set focus to MACRO (close-up)
		if(focusModes.contains(Parameters.FOCUS_MODE_MACRO))
		{
			parameters.setFocusMode(Parameters.FOCUS_MODE_MACRO);
			Log.d("focus modes", "focus set to MACRO");
		}
		else
		{
			Log.d("focus modes", "could not set focus mode to MACRO");

		}
    	mCamera.setParameters(parameters);//actually set the parameters
    }
    
    public void setZoom(int zoom){
    	parameters.setZoom(zoom);
    	mCamera.setParameters(parameters);//actually set the parameters
    }
    public int getMaxZoom(){
    	parameters = mCamera.getParameters();
    	return parameters.getMaxZoom();
    }
    public void setExposureCompensation(int ec){
    	//this method assumes that ec is value 0 to 4 corresponding to ec values of -2 to 2
    	parameters = mCamera.getParameters();
    	int max_ec = parameters.getMaxExposureCompensation();
    	int min_ec = parameters.getMinExposureCompensation();
    	Log.d("exposure comp", "exposure compensation must be between " + Integer.toString(parameters.getMinExposureCompensation())
				+ "and" + Integer.toString(parameters.getMaxExposureCompensation()) );
    	//int range = 24;
    	int actual_ec = ec - 12;
    	if(actual_ec < parameters.getMaxExposureCompensation() && actual_ec > parameters.getMinExposureCompensation()){
    		parameters.setExposureCompensation(actual_ec);
    		Log.d("exposure comp", "exposure set to " + Integer.toString(actual_ec));
    	}else{
    		parameters.setExposureCompensation(0);
    		Log.e("exposure comp", "exposure compensation must be between " + Integer.toString(parameters.getMinExposureCompensation())
    				+ "and" + Integer.toString(parameters.getMaxExposureCompensation()) );
    	}
    	mCamera.setParameters(parameters);//actually set the parameters
    }
    public void setWhiteBalance(String wb){
    	parameters = mCamera.getParameters();
    	parameters.setWhiteBalance(wb);
    	mCamera.setParameters(parameters);//actually set the parameters
    }
    public void takePicture() {
        Log.i(TAG, "Taking picture");
        class myCallback implements PictureCallback
    	{
    		@Override
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
    	myCallback mPicture = new myCallback();

        mCamera.takePicture(null, null, mPicture);
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
}
