package edu.upenn.seas.senior_design.p2d2;

import java.io.FileOutputStream;
import java.util.List;

import org.opencv.android.JavaCameraView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

public class CustomView extends JavaCameraView {

	private Camera.Parameters parameters;
    private static final String TAG = "Sample::Tutorial3View";

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
		List<String> focusModes = parameters.getSupportedFocusModes(); //set focus to MACRO (close-up)
		if(focusModes.contains(Parameters.FOCUS_MODE_MACRO))
		{
			parameters.setFocusMode(Parameters.FOCUS_MODE_MACRO);
		}
    	mCamera.setParameters(parameters);//actually set the parameters
    }
    
    public void setZoom(int zoom){
    	parameters.setZoom(zoom);
    	mCamera.setParameters(parameters);//actually set the parameters
    }
    public int getMaxZoom(){
    	return parameters.getMaxZoom();
    }
    public void setExposureCompensation(int ec){
    	//this method assumes that ec is value 0 to 4 corresponding to ec values of -2 to 2
    	int actual_ec = ec - 2;
    	if(actual_ec < parameters.getMaxExposureCompensation() && actual_ec > parameters.getMinExposureCompensation()){
    		parameters.setExposureCompensation(actual_ec);
    	}else{
    		parameters.setExposureCompensation(0);
    		Log.e("exposure comp", "exposure compensation must be between " + Integer.toString(parameters.getMinExposureCompensation())
    				+ "and" + Integer.toString(parameters.getMaxExposureCompensation()) );
    	}
    	mCamera.setParameters(parameters);//actually set the parameters
    }
    public void setWhiteBalance(String wb){
    	parameters.setWhiteBalance(wb);
    	mCamera.setParameters(parameters);//actually set the parameters
    }
    public void takePicture(final String fileName) {
        Log.i(TAG, "Tacking picture");
        PictureCallback callback = new PictureCallback() {

            private String mPictureFileName = fileName;

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(TAG, "Saving a bitmap to file");
                Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
                try {
                    FileOutputStream out = new FileOutputStream(mPictureFileName);
                    picture.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    picture.recycle();
                    mCamera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mCamera.takePicture(null, null, callback);
    }
}
