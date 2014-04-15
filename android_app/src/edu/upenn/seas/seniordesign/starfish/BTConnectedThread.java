package edu.upenn.seas.seniordesign.starfish;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.bluetooth.BluetoothSocket;

public class BTConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private final BTConnectingThread mThread;

		public BTConnectedThread(BluetoothSocket socket, BTConnectingThread mThread) {
			mmSocket = socket;
			this.mThread = mThread;
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
			try {
				mmSocket.close();
			} catch (IOException e) {}
		}
	}