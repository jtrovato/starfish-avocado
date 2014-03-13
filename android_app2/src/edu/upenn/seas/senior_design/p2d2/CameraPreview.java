package edu.upenn.seas.senior_design.p2d2;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/*
 * a basic camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
	//public static String TAG="Camera Test App"; //not necessary again
	private SurfaceHolder mHolder;
	private Camera mCamera;
	
	@SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera)
	{
		super(context);
		mCamera = camera;
		
		//Install a SurfaceHolder.Callback
		mHolder = this.getHolder();
		mHolder.addCallback(this);
		//deprecated setting but required on < Android 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);			
	}
	
	public void surfaceCreated(SurfaceHolder holder)
	{
		//The surface is created now tell the camera where to draw the preview
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(MainActivity.TAG, "Error setting camera preview: " + e.getMessage());
		}
	}
	
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		//this is taken care of in the activity
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		//If a preview changes or rotates, that is done here. Stop the preview before resizing it.
		if(mHolder.getSurface() == null)
		{
			//preview surface does not exist
			return;
		}
		
		//stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			//ignore: tried to stop non-existant previews
		}
		
		//make changes here
		//start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch(Exception e) {
			Log.d(MainActivity.TAG, "Error start camera preview: " + e.getMessage());
		}
	}
}