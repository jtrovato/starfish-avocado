package edu.upenn.seas.senior_design.p2d2;

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
		mThread = new BTConnectingThread(device, mBluetoothAdapter, getApplicationContext());
		mThread.start();
	}

	public void closeConnection() {
		if (mmSocket != null) {
			try {
				mmSocket.close();
			} catch (IOException e) {}
		}
	}

	

	
}
