package edu.upenn.seas.seniordesign.starfish;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	protected boolean isBTConnected;
	private boolean isBTServiceConnected;
	private String deviceAddress;
	public static final int RESULT_BT_CONNECTED = 0XFF00;
	private static final int BT_SETUP_REQUEST = 0xADB;
	protected static final String DEVICE_ADDRESS = "Device address";
	public static BluetoothSocket mmSocket;

	// Bluetooth Service declarations
	BTConnectionService mBTService;
	boolean mBound = false;

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
			mBound = false;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		isBTConnected = false;
	}

	@Override
	protected void onDestroy() {
		if (mBTService != null) {
			unbindService(mConnection);
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * called when user clicks the Start Test button
	 * 
	 */
	public void cameraTest(View view) {
		Intent intent = new Intent(this, CameraTestActivity.class);
		startActivity(intent);
	}

	/**
	 * called when user clicks the Bluetooth start button
	 * 
	 */
	public void bluetoothStart(View view) {
		Intent intent = new Intent(this, BluetoothSetupActivity.class);
		startActivityForResult(intent, BT_SETUP_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == BT_SETUP_REQUEST) {
			if (resultCode == RESULT_OK) {
				String deviceAddress = data.getStringExtra(DEVICE_ADDRESS);
				if (deviceAddress == null) {
					throw new IllegalArgumentException();
				}
				this.deviceAddress = deviceAddress;
				if (isBTServiceConnected) {
					mBTService.closeConnection();
					unbindService(mConnection);
					isBTServiceConnected = false;
				}
				Intent intent = new Intent(this, BTConnectionService.class);
				bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			}
		}
	}

}
