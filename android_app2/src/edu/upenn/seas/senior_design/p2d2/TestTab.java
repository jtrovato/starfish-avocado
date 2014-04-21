package edu.upenn.seas.senior_design.p2d2;

import org.opencv.android.OpenCVLoader;

import android.app.DialogFragment;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class TestTab extends Fragment implements InstructionsFragment.InstructionDialogListener{
	
	
	private String TAG = "test tab";
	private MainTabActivity a;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		a = (MainTabActivity)getActivity();
		if(a==null)
		{
			Log.e(TAG, "getActivity returned null");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View android = inflater.inflate(R.layout.test_frag, container, false);

		//set up timer
		a.timer_value = (TextView) android.findViewById(R.id.timer_value);
		//start button
		a.startButton = (Button) android.findViewById(R.id.button_start);
		a.startButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				a.startTime = SystemClock.uptimeMillis();
				a.customHandler.postDelayed(a.updateTimerThread, 0);
				a.testInProgress = true;
				//mOpenCvCameraView.lockCamera(); //enable AWB and AE lock

			}
		});
		//stopButton
		a.stopButton = (Button)android.findViewById(R.id.button_stop);
		a.stopButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				a.timeSwapBuff += a.timeInMilliseconds;
				a.customHandler.removeCallbacks(a.updateTimerThread);
				a.testInProgress = false;
				//start the Results Activity using an intent
				Intent resultsIntent = new Intent(a, ResultsActivity.class);
				
				resultsIntent.putExtra("fluo_data", a.fluo_data); //to pass info if needed
				resultsIntent.putExtra("time_data", a.time_data);
				a.startActivity(resultsIntent);
			}
		});
		
		//opencv camera view
		a.mOpenCvCameraView = (CustomView)android.findViewById(R.id.test_activity_java_surface_view);
		//a.mOpenCvCameraView.init(a.mOpenCvCameraView.getWidth(), a.mOpenCvCameraView.getHeight());
		a.mOpenCvCameraView.setVisibility(View.VISIBLE);
		a.mOpenCvCameraView.setCvCameraViewListener(a);
		
		//zoom settings
				//not really necessary because it is all digital zoom, but easier to select channels
				a.zoomBar = (SeekBar) android.findViewById(R.id.seekbar_zoom);
				a.zoomBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					int zoom;
					int maxZoom;
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
					{
						maxZoom = a.mOpenCvCameraView.getMaxZoom();
						zoom = (maxZoom/30)*progress;
						a.mOpenCvCameraView.setZoom(zoom);
						//mOpenCvCameraView.setMacroFocus();
					}
					@Override
					public void onStartTrackingTouch(SeekBar seekBar)
					{
						//TODO
					}
					@Override
					public void onStopTrackingTouch(SeekBar seekBar)
					{
						Toast.makeText(a, "zoom:" + Integer.toString(zoom),
								Toast.LENGTH_SHORT).show();
					}
					
				});
				//exposure settings
				a.isoBar = (SeekBar) android.findViewById(R.id.seekbar_iso);
				a.isoBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					String exposure;
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
					{
						
						a.mOpenCvCameraView.setExposureCompensation(progress); //may need to actually figure out what acceptable value for this are.
						exposure=Integer.toString(progress-12);
					}
					@Override
					public void onStartTrackingTouch(SeekBar seekBar)
					{
						//TODO
					}
					@Override
					public void onStopTrackingTouch(SeekBar seekBar)
					{
						Toast.makeText(a, "exposure:" + exposure,
								Toast.LENGTH_SHORT).show();
					}
				});
				//white balance settings
				a.wbBar = (SeekBar) android.findViewById(R.id.seekbar_wb);
				a.wbBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					String wb;
					
					@Override
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
						a.mOpenCvCameraView.setWhiteBalance(wb);
					}
					@Override
					public void onStartTrackingTouch(SeekBar seekBar)
					{
						//TODO
					}
					@Override
					public void onStopTrackingTouch(SeekBar seekBar)
					{
						Toast.makeText(a, "wb:" + wb,
								Toast.LENGTH_SHORT).show();
					}
				});

		return android; //cannot call getView() until this method returns
	}


    @Override
	public void onPause()
	{
    	Log.d(TAG, "onPause");
		if(a.mOpenCvCameraView != null)
			a.mOpenCvCameraView.disableView();
		super.onPause();

	}
	@Override
	public void onResume()
	{
		Log.d(TAG, "onResume");
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5,  a, a.mLoaderCallback); //this call is called when activity starts and causes the app the crash
		
	}
	
	@Override
	public void onStop()
	{
		Log.d(TAG, "onStop");
		//a.mOpenCvCameraView.disconnectCamera();
		super.onStop();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		//mOpenCvCameraView.releaseCam();
		if(a.mOpenCvCameraView != null)
			a.mOpenCvCameraView.disableView();
		
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		a.cal = true;
		a.boxup = false;
		
	}



	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		a.touch_count = 0;
		a.boxup=false;
		
	}
	
	
	





}