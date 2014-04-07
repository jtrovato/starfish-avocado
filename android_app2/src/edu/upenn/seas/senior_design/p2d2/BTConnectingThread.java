package edu.upenn.seas.senior_design.p2d2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;


public class BTConnectingThread extends Thread {
	private final BluetoothDevice mmDevice;
	private final BluetoothSocket mmSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private BTConnectedThread mBTConnectedThread;
	private Context context;

	// SPP UUID suggested by manufacturer
	private final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public BTConnectingThread(BluetoothDevice device, BluetoothAdapter mBluetoothAdapter, Context context) {
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		BluetoothSocket tmp = null;
		mmDevice = device;
		this.mBluetoothAdapter = mBluetoothAdapter;
		this.context = context;

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
		// HomeActivity.mmSocket = mmSocket;
		manageConnection();
	}
	
	private void manageConnection() {
		mBTConnectedThread = new BTConnectedThread(mmSocket, this);
		mBTConnectedThread.start();
		HomeActivity.setBTConnectedThread(mBTConnectedThread);
		String hello = "Hello World!";
		char[] helloArray = hello.toCharArray();
		byte[] helloBytes = new byte[helloArray.length * 2];
		ByteBuffer.wrap(helloBytes).asCharBuffer().put(helloArray);
		mBTConnectedThread.write(helloBytes);
	}

	/** Will cancel an in-progress connection, and close the socket */
	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {}
		// Throw some sort of error?
		// setResult(Activity.RESULT_CANCELED);
	}
	
	private class ToastRunner implements Runnable {
		private CharSequence message;

		public ToastRunner(CharSequence message) {
			this.message = message;
		}

		@Override
		public void run() {
			int duration = Toast.LENGTH_SHORT;
			Toast.makeText(context, message, duration).show();
		}

	}
}
