package edu.upenn.seas.senior_design.p2d2;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

public class CalibrateActivity extends Activity {
	private LocalBroadcastManager manager;

	// adapter is passed to ListView in order to initialize it
	ActionArrayAdapter adapter;

	// listview holds the items that are touched by the user to run calibration
	// tests
	ListView listview;

	// Holds ListItems that contains data for the tests in the ListView items
	ArrayList<ListItem> list;

	public static final String TAG = "edu.upenn.seas.seniordesign.starfish.CalibrateScreenActivity";

	private BTConnectionService mBTService;

	// Defines callbacks for service binding, passed to bindService()
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBTService = ((BTConnectionService.LocalBinder) service)
					.getService();

			if (mBTService == null) {
				btNotConnected();
			} else if (!mBTService.isConnected()) {
				btNotConnected();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// unused
		}

		private void btNotConnected() {
			DialogFragment btAlertFragment = new AlertDialogFragmentBT()
					.newInstance();
			btAlertFragment.show(getFragmentManager(), "no_BT");
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
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibrate);

		// Listview that displays the calibration tests to be done.
		listview = (ListView) findViewById(R.id.calibration_tests);

		// String values for the calibration tests that are done
		// Note: Find a way to move string values to string res
		String[] values = new String[] { "Test LEDs", "Test Pump", "Test Heat" };

		// constructing the ArrayList of ListItems that hold data for the test
		// results. ListItems are processed inside ActionArrayAdapter
		list = new ArrayList<ListItem>();
		for (int i = 0; i < values.length; ++i) {
			list.add(new ListItem(values[i]));
		}

		// add adapter to listview
		adapter = new ActionArrayAdapter(this, list);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int pos, long id) {
				Log.i(TAG, "listview onItemClick()");

				// create a new TestTask, which only runs one test
				new TestTask(pos, false).startTest();
			}
		});

		// button for testing all calibration tests
		Button testAll = (Button) findViewById(R.id.test_all_button);
		testAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Button testAll onClick()");

				new TestTask(0, true).startTest();
			}
		});

		Intent intent = new Intent(this, BTConnectionService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		// Register Broadcast receivers for BT Service
		manager = LocalBroadcastManager.getInstance(getApplicationContext());
		// Receiver for data
		IntentFilter dataFilter = new IntentFilter(
				BTConnectionService.ACTION_BT_RECIEVED);
		manager.registerReceiver(mBTDataReceiver, dataFilter);
		// Receiver for stopping service
		IntentFilter stopFilter = new IntentFilter(
				BTConnectionService.ACTION_BT_STOP);
		manager.registerReceiver(mBTStopReceiver, stopFilter);

	}// end of onCreate()

	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}

	// AsyncTask allows for test to be run off of the main Thread
	// uses an integer as an input parameter (test to be run)
	// returns void to track progress (no progress tracking data)
	// returns boolean as return value (test succeeded or failed)
	private class TestTask extends AsyncTask<Integer, Void, Boolean> {

		// holds the the elements of the View corresponding to the test
		// being run
		View v;
		ImageView icon;
		ProgressBar bar;

		// index of the test, and whether all tests are being run
		int index;
		boolean testAll;

		public TestTask(int i, boolean enableTestAll) {
			Log.i(TAG, "create new TestTask");

			index = i;
			v = listview.getChildAt(index);

			if (v != null) {
				icon = (ImageView) v.findViewById(R.id.passed_test_icon);
				bar = (ProgressBar) v.findViewById(R.id.progress_wheel_icon);
			}

			testAll = enableTestAll;
		}

		public void startTest() {
			Log.i(TAG, "TestTask startTest()");

			this.execute(index);
		}

		// overridden AsyncTask methods

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "AsyncTask onPreExecute()");

			icon.setVisibility(View.GONE);
			bar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			Log.i(TAG, "AsyncTask doInBackground()");

			return selectTest(params[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.i(TAG, "AsyncTask onPostExecute()");

			if (result)
				icon.setImageResource(R.drawable.checkmark);
			else
				icon.setImageResource(R.drawable.crossout);

			icon.setVisibility(View.VISIBLE);
			bar.setVisibility(View.GONE);

			if (listview.getChildAt(index + 1) != null && testAll)
				new TestTask(index + 1, true).startTest();
		}
	}

	/*
	 * Testing methods ledTest() pumpTest() heatTest() valvesTest()
	 */

	// run different tests, select using an int value
	public boolean selectTest(int select) {
		boolean result = false;
		switch (select) {
		case 0:
			result = ledTest();
			break;
		case 1:
			result = pumpTest();
			break;
		case 2:
			result = heatTest();
			break;
		default:
			break;
		}
		return result;
	}

	public boolean ledTest() {
		byte[] turnOn = { (byte) 0xB8, (byte) 0xD3, (byte) 0x01, (byte) 0x3C,
				(byte) 0xFF };
		byte[] check = { (byte) 0xB8, (byte) 0xD3, (byte) 0x01, (byte) 0xFF,
				(byte) 0x3C };
		byte[] turnOff = { (byte) 0xB8, (byte) 0xD3, (byte) 0x01, (byte) 0x3C,
				(byte) 0x00 };

		/*
		 * if (list.get(0).testPassed()) { bytes[4] = (byte) 0x00; }
		 */

		mBTService.writeToBT(turnOn);
		mBTService.writeToBT(check);

		int count = 0;
		while (!list.get(0).testPassed()) {
			if (count > 200) {
				break;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
			count++;
		}

		mBTService.writeToBT(turnOff);
		return list.get(0).testPassed();
	}

	public boolean pumpTest() {
		byte[] check = { (byte) 0xB8, (byte) 0xD3, (byte) 0x01, (byte) 0xFF,
				(byte) 0x8F };

		// if (list.get(1).testPassed()) { bytes[4] = (byte) 0x00; }

		mBTService.writeToBT(check);

		int count = 0;
		while (!list.get(1).testPassed()) {
			if (count > 200) {
				break;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
			count++;
		}
		
		return list.get(1).testPassed();
	}

	public boolean heatTest() {
		byte[] bytes = { (byte) 0xB8, (byte) 0xD3, (byte) 0x01, (byte) 0x57,
				(byte) 0x33 };

		if (list.get(2).testPassed()) {
			bytes[4] = (byte) 0x00;
		}

		mBTService.writeToBT(bytes);

		list.get(2).setTestResult(!list.get(2).testPassed());

		return list.get(2).testPassed();
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
			list.get(0).setTestResult(false);
			Log.i(BLUETOOTH_SERVICE, "LEDS: OFF");
			break;
		case (byte) 0xFF:
			list.get(0).setTestResult(true);
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
			list.get(1).setTestResult(true);
			Log.i(BLUETOOTH_SERVICE, "Fluids: not yet actuated");
			break;
		case (byte) 0x44:
			list.get(1).setTestResult(false);
			Log.i(BLUETOOTH_SERVICE, "Fluids: currently actuating");
			break;
		case (byte) 0xFF:
			list.get(1).setTestResult(false);
			Log.i(BLUETOOTH_SERVICE, "Fluids: already actuated");
			break;
		default:
			list.get(1).setTestResult(false);
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

}
