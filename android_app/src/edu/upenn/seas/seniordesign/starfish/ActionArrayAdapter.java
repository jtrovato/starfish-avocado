package edu.upenn.seas.seniordesign.starfish;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ActionArrayAdapter extends ArrayAdapter<ListItem>{
	
	//Holds the items that will be in the ListView
	private final List<ListItem> list;
	private final Activity context;
	
	public ActionArrayAdapter(Activity context, List<ListItem> list)
	{
		super(context, R.layout.calibration_list_layout, list);
		this.list = list;
		this.context = context;
	}
	
	static class ViewHolder{
		protected TextView text;
		protected View icon;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View view = null;
		final ListItem item = list.get(position);
		
		//Get the layout of each individual ListItem in the ListView
		LayoutInflater inflater = context.getLayoutInflater();
		view = inflater.inflate(R.layout.calibration_list_layout, null);
		
		//Objects that hold the string to be displayed and the icon (either an image or a
		//loading circle sprite)
		final ImageView icon = (ImageView)view.findViewById(R.id.passed_test_icon);
		final ProgressBar bar = (ProgressBar) view.findViewById(R.id.progress_wheel_icon);
		
		//Set the text of the list item
		TextView text = (TextView)view.findViewById(R.id.calibration_item_label);
		text.setText(item.getText());

		
		//Handler used to pass information between the thread running the test method and the
		//Activity used to display the listview
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
					
				Bundle b = msg.getData();
				String action = b.getString("action");
					
				//If the thread sends a message that it is running, set the icon and
				//loading bar so only the loading bar appears
				if (action.equals("Run")){
					icon.setVisibility(View.GONE);
					bar.setVisibility(View.VISIBLE);
				}
				else{
					
					//Otherwise, the thread is not running. Set the item's icon
					//depending on the result of the test, and show the icon while
					//hiding the loading bar
					item.setTestResult(item.testPassed());
						
					if(item.testPassed())
						icon.setImageResource(R.drawable.checkmark);
					else
						icon.setImageResource(R.drawable.crossout);
						
					icon.setVisibility(View.VISIBLE);
					bar.setVisibility(View.GONE);
				}
			}
		};
			
		//Add a OnClickListener that starts a new thread which runs the ListItem's
		//test method in a separate thread
		view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg) {
				
				Runnable task = new Runnable(){
					@Override
					public void run() {		
						Bundle b = new Bundle();
						b.putString("action", "Run");
						Message msg = new Message();
						msg.setData(b);
						handler.sendMessage(msg);
	
						list.get(position).doWork();
						
						b = new Bundle();
						b.putString("action", "Show");
						msg = new Message();
						msg.setData(b);
						handler.sendMessage(msg);
					}
				};
					
				new Thread(task).start();
			}
		});	
		return view;
	}
}
