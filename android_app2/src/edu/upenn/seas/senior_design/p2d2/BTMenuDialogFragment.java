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

	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */
	public interface BTDialogListener {
		public void onListItemClick(DialogFragment dialog, int which);
	}

	// Use this instance of the interface to deliver action events
	BTDialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
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
					+ " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mBTArrayAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		if (getAdapter(getActivity())) {
			viewSetup();
			// Use the Builder class for convenient dialog construction
			builder.setTitle(R.string.bluetooth_menu_label);
			final DialogFragment btFragment = this;
			builder.setAdapter(mBTArrayAdapter,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mListener.onListItemClick(btFragment, which);
						}
					});
			return builder.create();
		}
		builder.setMessage(R.string.no_bluetooth);
		return builder.create();
	}

	private void viewSetup() {
		mBTArrayAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item);
		// Register the Bluetooth BroadcastReciever
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		getActivity().registerReceiver(mReceiver, filter);

		// Turn on BT adapter if not already on
		turnOnStartDiscovery();
	}

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

	private boolean getAdapter(Context context) {
		// Create bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Check for bluetooth support
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth, create dialog that explains to
			// user
			// int duration = Toast.LENGTH_SHORT;
			// Toast.makeText(context, R.string.no_bluetooth, duration).show();
			return false;
		}
		return true;
	}

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
