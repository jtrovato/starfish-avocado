package edu.upenn.seas.seniordesign.starfish;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

public class BTConnectionService extends Service {
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket mmSocket;
	private BTConnectingThread mThread;
	private BTConnectedThread mBTConnectedThread;

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
		BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(deviceAddress);
		if (device == null) {
			throw new IllegalArgumentException();
		}
		mThread = new BTConnectingThread(device);
		mThread.start();
	}

	public void closeConnection() {
		if (mmSocket != null) {
			try {
				mmSocket.close();
			} catch (IOException e) {}
		}
	}

	private class ToastRunner implements Runnable {
		private CharSequence message;

		public ToastRunner(CharSequence message) {
			this.message = message;
		}

		@Override
		public void run() {
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			Toast.makeText(context, message, duration).show();
		}

	}

	private class BTConnectingThread extends Thread {
		private final BluetoothDevice mmDevice;

		// SPP UUID suggested by manufacturer
		private final UUID MY_UUID = UUID
				.fromString("00001101-0000-1000-8000-00805F9B34FB");

		public BTConnectingThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			CharSequence connecting = "Attempting connection...";
			CharSequence connectionFailed = "Connection attempt failed";
			CharSequence connectionSuccess = "Connection successful";
			ToastRunner connectingToast = new ToastRunner(connecting);
			ToastRunner connectionFailedToast = new ToastRunner(
					connectionFailed);
			ToastRunner connectionSuccessToast = new ToastRunner(
					connectionSuccess);
			Handler handler = new Handler(Looper.getMainLooper());
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				handler.post(connectingToast);
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					handler.post(connectionFailedToast);
					mmSocket.close();
				} catch (IOException closeException) {}
				return;
			}
			handler.post(connectionSuccessToast);
			// Do work to manage the connection (in a separate thread)
			MainActivity.mmSocket = mmSocket;
			manageConnection();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {}
			// Throw some sort of error?
			// setResult(Activity.RESULT_CANCELED);
		}
	}

	private void manageConnection() {
		mBTConnectedThread = new BTConnectedThread(mmSocket);
		mBTConnectedThread.start();
		String hello = "Hello World!";
		char[] helloArray = hello.toCharArray();
		byte[] helloBytes = new byte[helloArray.length * 2];
		ByteBuffer.wrap(helloBytes).asCharBuffer().put(helloArray);
		mBTConnectedThread.write(helloBytes);
	}

	private class BTConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public BTConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				if (Thread.interrupted()) {
					return;
				}
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					// mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
					// .sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			if (mThread != null) {
				mThread.interrupt();
			}
			if (mBTConnectedThread != null) {
				mBTConnectedThread.interrupt();
			}
			try {
				mmSocket.close();
			} catch (IOException e) {}
		}
	}
}
