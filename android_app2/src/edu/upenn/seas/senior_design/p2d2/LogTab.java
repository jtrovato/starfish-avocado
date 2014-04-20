package edu.upenn.seas.senior_design.p2d2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class LogTab extends Fragment {
	
	private String TAG = "log tab";
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View android = inflater.inflate(R.layout.log_frag, container, false);
        return android;
	}
	
	@Override
	public void onStop()
	{
		Log.d(TAG, "onStop");
		super.onStop();
	}
	@Override
	public void onPause()
	{
		Log.d(TAG, "onPause");
		super.onPause();
	}





}
