package edu.upenn.seas.senior_design.p2d2;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends Activity implements
		BTMenuDialogFragment.BTDialogListener, InstructionsFragment.InstructionDialogListener {
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
	private LocalBroadcastManager manager;

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

	private final BroadcastReceiver mBTDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String stringExtra = intent
					.getStringExtra(getString(R.string.bt_data_type));
			if (stringExtra == null) {
				Log.e(BLUETOOTH_SERVICE,
						"Null data type broadcast from BT service");
				return;
			}
			if (stringExtra.equals(getString(R.string.bt_error))) {
				processError(intent);
			} else if (stringExtra.equals(getString(R.string.bt_fluid_state))) {
				processFluidActuationState(intent);
			} else if (stringExtra.equals(getString(R.string.bt_heating_state))) {
				processHeatingState(intent);
			} else if (stringExtra.equals(getString(R.string.bt_led_state))) {
				processLEDState(intent);
			} else if (stringExtra.equals(getString(R.string.bt_temp_data))) {
				processTempData(intent);
			}
		}
	};

	private final BroadcastReceiver mBTStopReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean shutdown = intent.getBooleanExtra(
					getString(R.string.bt_disconnect_broadcast), false);
			if (shutdown) {
				unbindService(mConnection);
			}
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
						MainTabActivity.class);
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
		/*
		// Bluetooth setup button
		btButton = (Button) findViewById(R.id.button_bluetooth);
		btButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showPopup();
			}
		}); */
		isBTServiceConnected = false;
		mBound = false;
		manager = LocalBroadcastManager.getInstance(getApplicationContext());
		
		homeInstruction();
	}
	//instructions
	private void homeInstruction() {
		DialogFragment homeInstAlert = new InstructionsFragment().newInstance();
		Bundle bundle = new Bundle();
		bundle.putInt("inst", 0); //0 corresponds to home instructions
		homeInstAlert.setArguments(bundle);
		homeInstAlert.show(getFragmentManager(), "home_inst");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
			manager.unregisterReceiver(mBTDataReceiver);
			manager.unregisterReceiver(mBTStopReceiver);
		}
		Intent intent = new Intent(this, BTConnectionService.class);
		mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		// Register Broadcast receivers for BT Service
		// Receiver for data
		IntentFilter dataFilter = new IntentFilter(
				BTConnectionService.ACTION_BT_RECIEVED);
		manager.registerReceiver(mBTDataReceiver, dataFilter);
		// Receiver for stopping service
		IntentFilter stopFilter = new IntentFilter(
				BTConnectionService.ACTION_BT_STOP);
		manager.registerReceiver(mBTStopReceiver, stopFilter);
	}

	/***************************************************************************
	 * Methods for processing BT Data
	 **************************************************************************/
	private void processTempData(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFF);
		if (dataOne == (byte) 0xFF) {
			Log.e(BLUETOOTH_SERVICE, "Bad temp data reached processTempData()");
			return;
		}
		byte dataTwo = intent.getByteExtra(getString(R.string.bt_data_2),
				(byte) 0xFF);
		int tempData = dataOne * 256 + dataTwo;
		String tempDataString = Integer.toString(tempData);
		Log.i(BLUETOOTH_SERVICE, "Temperature = " + tempDataString + " Celcius");
	}

	private void processHeatingState(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFA);
		switch (dataOne) {
		case (byte) 0x00:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Stopped");
			break;
		case (byte) 0x31:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heating to Temp 1");
			break;
		case (byte) 0x33:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heated to Temp 1");
			break;
		case (byte) 0x51:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heating to Temp 2");
			break;
		case (byte) 0x55:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heated to Temp 2");
			break;
		case (byte) 0x62:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heating to Temp 3");
			break;
		case (byte) 0x66:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heated to Temp 3");
			break;
		case (byte) 0xF7:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heating to Temp 4");
			break;
		case (byte) 0xFF:
			Log.i(BLUETOOTH_SERVICE, "Heating state: Heated to Temp 4");
			break;
		default:
			Log.e(BLUETOOTH_SERVICE,
					"Bad fluid actuation state reached processFluidActuationState()");
			break;
		}
	}

	private void processLEDState(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFA);
		switch (dataOne) {
		case (byte) 0x00:
			Log.i(BLUETOOTH_SERVICE, "LEDS: OFF");
			break;
		case (byte) 0xFF:
			Log.i(BLUETOOTH_SERVICE, "LEDS: ON");
			break;
		default:
			Log.e(BLUETOOTH_SERVICE, "Bad LED state reached processLEDState()");
			break;
		}
	}

	private void processFluidActuationState(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFA);
		switch (dataOne) {
		case (byte) 0x00:
			Log.i(BLUETOOTH_SERVICE, "Fluids: not yet actuated");
			break;
		case (byte) 0x44:
			Log.i(BLUETOOTH_SERVICE, "Fluids: currently actuating");
			break;
		case (byte) 0xFF:
			Log.i(BLUETOOTH_SERVICE, "Fluids: already actuated");
			break;
		default:
			Log.e(BLUETOOTH_SERVICE,
					"Bad fluid actuation state reached processFluidActuationState()");
			break;
		}
	}

	private void processError(Intent intent) {
		byte dataOne = intent.getByteExtra(getString(R.string.bt_data_1),
				(byte) 0xFF);
		switch (dataOne) {
		case (byte) 0x11:
			Log.e(BLUETOOTH_SERVICE, "Temperature out of range!");
			break;
		default:
			Log.e(BLUETOOTH_SERVICE, "Bad error intent reached processError()");
			break;
		}
	}
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}
}
