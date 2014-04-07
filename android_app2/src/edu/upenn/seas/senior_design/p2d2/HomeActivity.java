package edu.upenn.seas.senior_design.p2d2;



import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HomeActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 0xFF;
	
	private Button testButton;
	private Button calButton;
	private Button btButton;
	
	//Bluetooth initializations
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mBTArrayAdapter;
	private OnItemClickListener mBTClickListener;
	private ListView mBTListView;
	private OnItemClickListener mBTListener;
	private ListPopupWindow mBTPopup;
	protected static final String DEVICE_ADDRESS = "Device address";
	private String deviceAddress;
	private View popUpView;
	private BTMenuDialogFragment mPopup;
	private FragmentManager newManager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		//test button
		testButton = (Button)findViewById(R.id.button_test);
		testButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				//start the Test Activity using an intent
				Intent testIntent = new Intent(HomeActivity.this, TestActivity.class);
				//myIntent.putExtra("key", value); //to pass info if needed
				HomeActivity.this.startActivity(testIntent);
			}
		});
		//calibrate button
		calButton = (Button)findViewById(R.id.button_calibrate);
		calButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				//start the Calibrate Activity using an intent
				Intent testIntent = new Intent(HomeActivity.this, CalibrateActivity.class);
				//myIntent.putExtra("key", value); //to pass info if needed
				HomeActivity.this.startActivity(testIntent);
			}
		});
		//Bluetooth setup button
		btButton = (Button)findViewById(R.id.button_bluetooth);
		btButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				showPopup();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	private void showPopup(){
		mPopup= new BTMenuDialogFragment();
		newManager = getFragmentManager();
		mPopup.show(newManager, "missiles");
		newManager.executePendingTransactions();
	}
}
