package edu.upenn.seas.senior_design.p2d2;



import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends Activity implements BTMenuDialogFragment.BTDialogListener {	
	private Button testButton;
	private Button calButton;
	private Button btButton;
	
	// Bluetooth Initializations
	private String deviceAddress;
	private BTMenuDialogFragment mPopup;
	private FragmentManager newManager;
	private boolean isBTServiceConnected;
	private BTConnectionService mBTService;
	private boolean mBound;
	private static BTConnectedThread btThread;
	
	
	// Defines callbacks for service binding, passed to bindService()
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBTService = ((BTConnectionService.LocalBinder) service)
					.getService();
			isBTServiceConnected = true;

			if (deviceAddress == null) {
				throw new IllegalArgumentException();
			}
			// Initialize and connect
			// Call method to actually connect device
			mBTService.connectToBT(deviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			isBTServiceConnected = false;
			mBound = false;
		}

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		//test button
		testButton = (Button)findViewById(R.id.button_test);
		testButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				//start the Test Activity using an intent
				Intent testIntent = new Intent(HomeActivity.this, TestActivity.class);
				//myIntent.putExtra("key", value); //to pass info if needed
				HomeActivity.this.startActivity(testIntent);
			}
		});
		//calibrate button
		calButton = (Button)findViewById(R.id.button_calibrate);
		calButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				//start the Calibrate Activity using an intent
				Intent testIntent = new Intent(HomeActivity.this, CalibrateActivity.class);
				//myIntent.putExtra("key", value); //to pass info if needed
				HomeActivity.this.startActivity(testIntent);
			}
		});
		//Bluetooth setup button
		btButton = (Button)findViewById(R.id.button_bluetooth);
		btButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				showPopup();
			}
		});
		
		isBTServiceConnected = false;
		mBound = false;
	}
	
	@Override
	protected void onDestroy() {
		if(btThread != null){
			btThread.cancel();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	private void showPopup(){
		mPopup= new BTMenuDialogFragment();
		newManager = getFragmentManager();
		mPopup.show(newManager, "missiles");
		//newManager.executePendingTransactions();
	}

	@Override
	public void onListItemClick(DialogFragment dialog, int which) {
		BTMenuDialogFragment btDialog = (BTMenuDialogFragment)dialog;
		String buttonText = btDialog.mBTArrayAdapter.getItem(which);
		deviceAddress = buttonText.substring(buttonText.length() - 17);
		if (isBTServiceConnected) {
			mBTService.closeConnection();
			unbindService(mConnection);
			isBTServiceConnected = false;
		}
		Intent intent = new Intent(this, BTConnectionService.class);
		mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	protected static void setBTConnectedThread(BTConnectedThread thread){
		if(thread == null){
			throw new IllegalArgumentException();
		}
		btThread = thread;
	}
	
	public static BTConnectedThread getBTThread(){
		return btThread;
	}
	
	
}
