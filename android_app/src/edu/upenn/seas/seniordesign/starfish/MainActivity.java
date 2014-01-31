package edu.upenn.seas.seniordesign.starfish;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	private boolean isBTConnected;
	public static final int RESULT_BT_CONNECTED = 0XFF00;
	private static final int BT_SETUP_REQUEST = 0xADB;
	public static BluetoothSocket mmSocket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		isBTConnected = false;
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
	 * called when user clicks the Bluetooth startbutton
	 * 
	 */
	public void bluetoothStart(View view) {
		Intent intent = new Intent(this, BluetoothSetupActivity.class);
		startActivityForResult(intent, BT_SETUP_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == BT_SETUP_REQUEST) {
			if (resultCode == RESULT_BT_CONNECTED) {
				isBTConnected = true;
				manageConnection();
			}
		}
	}

	private void manageConnection() {
		BTConnectedThread mThread = new BTConnectedThread(mmSocket);
		mThread.start();
		String hello = "Hello World!";
		char[] helloArray = hello.toCharArray();
		byte[] helloBytes= new byte[helloArray.length*2];
		ByteBuffer.wrap(helloBytes).asCharBuffer().put(helloArray);
		mThread.write(helloBytes);
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
			try {
				mmSocket.close();
			} catch (IOException e) {}
		}
	}

}
