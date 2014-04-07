package edu.upenn.seas.senior_design.p2d2;

import java.util.Set;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BTMenuDialogFragment extends DialogFragment {
	private BluetoothAdapter mBluetoothAdapter;
	public ArrayAdapter<String> mBTArrayAdapter;
	private static final int REQUEST_ENABLE_BT = 0xFF;

	/**
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks.
	 */
	public interface BTDialogListener {
		public void onListItemClick(DialogFragment dialog, int which);
	}

	// Use this instance of the interface to deliver action events
	BTDialogListener mListener;

	/*
	 * Override the Fragment.onAttach() method to instantiate the
	 * BTDialogListener
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (BTDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement BTDialogListener");
		}
	}

	/**
	 * Initializes the dialog when it is created
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mBTArrayAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get/check for adapter
		if (getAdapter(getActivity())) {
			// Populate the ArrayAdapter with BT devices
			viewSetup();
			// Use the Builder class for convenient dialog construction
			builder.setTitle(R.string.bluetooth_menu_label);
			final DialogFragment btFragment = this;

			// Setup Adapter, click listener to pass data back to HomeActivity
			builder.setAdapter(mBTArrayAdapter,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mListener.onListItemClick(btFragment, which);
						}
					});
			return builder.create();
		}

		// If no bluetooth adapter, tell the user device is incompatible
		builder.setMessage(R.string.no_bluetooth);
		return builder.create();
	}

	/**
	 * sets up ArrayAdapter to be filled with BT devices
	 */
	private void viewSetup() {
		mBTArrayAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item);
		// Register the Bluetooth BroadcastReciever
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		getActivity().registerReceiver(mReceiver, filter);

		// Turn on BT adapter if not already on
		turnOnStartDiscovery();
	}

	/**
	 * populates ArrayAdapter with all paired BT devices
	 */
	private void populatePairedList() {
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		for (BluetoothDevice device : pairedDevices) {
			mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			mBTArrayAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * puts Bluetooth in discovery mode
	 */
	private void bluetoothDiscovery() {
		if (mBluetoothAdapter == null) {
			throw new NullPointerException();
		}
		if (!mBluetoothAdapter.isEnabled()) {
			return;
		}
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		mBluetoothAdapter.startDiscovery();
	}

	/**
	 * Turns on BT; if it is already on, then starts discovery by calling
	 * bluetoothDiscovery()
	 */
	private void turnOnStartDiscovery() {
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			populatePairedList();
			bluetoothDiscovery();
		}
	}

	/**
	 * controls what happens when Bluetooth gets enabled
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth was enabled
				populatePairedList();
				bluetoothDiscovery();
			} else {
				// Tell user there was an error enabling Bluetooth, close
				Toast.makeText(getActivity(), "Error Enabling Bluetooth",
						Toast.LENGTH_SHORT).show();
				this.dismiss();
			}
		}
	}

	/**
	 * Setup of broadcast receiver
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			boolean inArray = false;
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to an array adapter to show in
				// ListView
				String temp = device.getName() + "\n" + device.getAddress();

				// Check of device is already in adapter
				for (int i = 0; i < mBTArrayAdapter.getCount(); i++) {
					if (mBTArrayAdapter.getItem(i).equals(temp)) {
						inArray = true;
						break;
					}
				}
				if (!inArray) {
					mBTArrayAdapter.add(temp);
					// refresh the ListView so the devices are displayed
					mBTArrayAdapter.notifyDataSetChanged();
				}
			}
		}
	};

	/**
	 * sets mBluetoothAdapter to the default adapter
	 * 
	 * @return false if Bluetooth not supported by this device
	 */
	private boolean getAdapter(Context context) {
		// Create bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Check for bluetooth support
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			return false;
		}
		return true;
	}

	/**
	 * what happens when the dialog closes (makes sure discovery is off)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isDiscovering()) {
				mBluetoothAdapter.cancelDiscovery();
			}
		}
		super.onDismiss(dialog);
	}
}
