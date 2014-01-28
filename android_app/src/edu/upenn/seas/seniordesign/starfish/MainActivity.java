package edu.upenn.seas.seniordesign.starfish;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/** called when user clicks the Start Test button
	 * 
	 */
	public void cameraTest(View view) {
		Intent intent  = new Intent(this, CameraTestActivity.class);
		startActivity(intent);
	}
	
	/** called when user clicks the Bluetooth startbutton
	 * 
	 */
	public void bluetoothStart(View view) {
		Intent intent  = new Intent(this, BluetoothSetupActivity.class);
		startActivity(intent);
	}

}
