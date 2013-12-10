package edu.upenn.seas.seniordesign.starfish;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TimerService extends Service {

	private Task retryTask;
	Timer myTimer;

	private boolean timerRunning = false;

	private long RETRY_TIME = 200000;
	private long START_TIME = 5000;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
	    // TODO Auto-generated method stub
	    super.onCreate();
	    myTimer = new Timer();
	    myTimer.scheduleAtFixedRate(new Task(), START_TIME, RETRY_TIME);
	    timerRunning = true;

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    if (!timerRunning) {
	        myTimer = new Timer();
	        myTimer.scheduleAtFixedRate(new Task(), START_TIME, RETRY_TIME);
	        timerRunning = true;
	    }

	    return super.onStartCommand(intent, flags, startId);

	}

	public class Task extends TimerTask {

	    @Override
	    public void run() {

	        // DO WHAT YOU NEED TO DO HERE
	    }


	}

	@Override
	public void onDestroy() {
	    // TODO Auto-generated method stub
	    super.onDestroy();

	    if (myTimer != null) {
	        myTimer.cancel();

	    }

	    timerRunning = false;
	}
}