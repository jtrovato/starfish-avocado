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

public class HomeActivity extends Activity implements
		BTMenuDialogFragment.BTDialogListener {
	// Button initializations
	private Button testButton;
	private Button calButton;
	private Button btButton;

	// Bluetooth Initializations
	private BTMenuDialogFragment mPopup;
	private FragmentManager newManager;
	private boolean isBTServiceConnected;
	private boolean mBound;
	private String deviceAddress;
	private BTConnectionService mBTService;
	// btThread must be static: setter method accessed from threads
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
		// test button
		testButton = (Button) findViewById(R.id.button_test);
		testButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// start the Test Activity using an intent
				Intent testIntent = new Intent(HomeActivity.this,
						TestActivity.class);
				// myIntent.putExtra("key", value); //to pass info if needed
				HomeActivity.this.startActivity(testIntent);
			}
		});
		// calibrate button
		calButton = (Button) findViewById(R.id.button_calibrate);
		calButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// start the Calibrate Activity using an intent
				Intent testIntent = new Intent(HomeActivity.this,
						CalibrateActivity.class);
				// myIntent.putExtra("key", value); //to pass info if needed
				HomeActivity.this.startActivity(testIntent);
			}
		});
		// Bluetooth setup button
		btButton = (Button) findViewById(R.id.button_bluetooth);
		btButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showPopup();
			}
		});
		isBTServiceConnected = false;
		mBound = false;
	}

	@Override
	protected void onDestroy() {
		if (btThread != null) {
			btThread.cancel();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	/**
	 * shows dialog which allows user to choose which BT device to connect to
	 */
	private void showPopup() {
		mPopup = new BTMenuDialogFragment();
		newManager = getFragmentManager();
		mPopup.show(newManager, "missiles");
	}

	/**
	 * implements interface BTDialogListener in BTMenuDialogFragment; connects
	 * to the device selected by the user in the bluetooth selection pop-up
	 * dialog
	 * 
	 * @param dialog
	 *            The dialog which was clicked
	 * @param which
	 *            The position of the list item which was clicked
	 */
	@Override
	public void onListItemClick(DialogFragment dialog, int which) {
		BTMenuDialogFragment btDialog = (BTMenuDialogFragment) dialog;
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

	/**
	 * sets the field btThread; must be static because it is accessed by
	 * BTConnectingThread
	 * 
	 * @param thread
	 *            thread to be set as btThread
	 */
	protected static void setBTConnectedThread(BTConnectedThread thread) {
		if (thread == null) {
			throw new IllegalArgumentException();
		}
		btThread = thread;
	}

	protected BTConnectedThread getBTThread() {
		return btThread;
	}
}
