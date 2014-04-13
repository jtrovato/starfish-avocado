package edu.upenn.seas.senior_design.p2d2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class BTConnectionService extends Service {
	protected static final String ACTION_BT_RECIEVED = "edu.upenn.seas.senior_design.p2d2.action.BT_RECIEVED";
	protected static final String ACTION_BT_STOP = "edu.upenn.seas.senior_design.p2d2.action.BT_STOP";
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket mmSocket;
	private BluetoothDevice device;
	private InputStream mmInStream;
	private OutputStream mmOutStream;
	private int minPacketSize = 3; //minimum number of bytes in a packet

	// SPP UUID suggested by manufacturer
	private final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private boolean isConnected = false; 
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		BTConnectionService getService() {
			return BTConnectionService.this;
		}
	}

	public void connectToBT(String deviceAddress) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
		if (device == null) {
			throw new IllegalArgumentException();
		}
		new BTConnector().execute();
	}

	public boolean writeToBT(byte[] bytes) {
		if (mmSocket == null || mmOutStream == null) {
			return false;
		}
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public void closeConnection() {
		if (mmSocket != null) {
			try {
				mmSocket.close();
			} catch (IOException e) {}
			isConnected = false;
		}
	}

	private void manageConnection() {
		// Start Executor which handles all the bluetooth stuff
		try {
			mmInStream = mmSocket.getInputStream();
			mmOutStream = mmSocket.getOutputStream();
			Executor mExecutor = new BTExecutor();
			mExecutor.execute(new BTReader());
		} catch (IOException e) {}
	}

	private void stop() {
		// Create intent which tells all activities to unbind
		Intent intent = new Intent();
		intent.putExtra(getString(R.string.bt_disconnect_broadcast), true);
		LocalBroadcastManager manager = LocalBroadcastManager
				.getInstance(getApplicationContext());
		manager.sendBroadcast(intent);
	}

	private class BTConnector extends AsyncTask<Void, CharSequence, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {}

			// Define progress updates
			CharSequence connecting = "Attempting connection...";
			CharSequence connectionFailed = "Connection attempt failed";
			CharSequence connectionSuccess = "Connection successful";

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				publishProgress(connecting);
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					publishProgress(connectionFailed);
					isConnected = false;
					mmSocket.close();
				} catch (IOException closeException) {}
				return false;
			}
			publishProgress(connectionSuccess);
			isConnected = true;
			return true;
		}

		protected void onProgressUpdate(CharSequence... progress) {
			CharSequence currentProgress = progress[0];
			Toast.makeText(getApplicationContext(), currentProgress,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				manageConnection();
			} else {
				stop();
			}

		}
	}

	private class BTReader implements Runnable {
		@Override
		public void run() {
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int numBytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				if (Thread.interrupted()) {
					return;
				}
				try {
					// Read from the InputStream
					numBytes = mmInStream.available();
					if (numBytes > minPacketSize) {
						numBytes = mmInStream.read(buffer);
						// Send the obtained bytes to the UI activity
						if (numBytes > 0) {
							byte[] data = Arrays.copyOfRange(buffer, 0,
									numBytes);
							Intent intent = new Intent(ACTION_BT_RECIEVED);
							intent.putExtra(
									getString(R.string.bt_new_bytes_read), data);
							LocalBroadcastManager manager = LocalBroadcastManager
									.getInstance(getApplicationContext());
							manager.sendBroadcast(intent);
						} else if (numBytes < 0) {
							break;
						}
					}
				} catch (IOException e) {
					break;
				}
			}
		}

	}

	private class BTExecutor implements Executor {
		@Override
		public void execute(Runnable command) {
			new Thread(command).start();
		}
	}
	
	public boolean isConnected(){
	    return isConnected;
	}
}
