package edu.upenn.seas.senior_design.p2d2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends Activity {

	private Button testButton;
	
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
				//start the Main Activity using an intent
				Intent testIntent = new Intent(HomeActivity.this, MainActivity.class);
				//myIntent.putExtra("key", value); //to pass info if needed
				HomeActivity.this.startActivity(testIntent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

}
