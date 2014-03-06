package edu.upenn.seas.seniordesign.starfish;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ActionArrayAdapter extends ArrayAdapter<ListItem>{
	
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
		protected ImageView icon;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = null;
		
		if(convertView == null){
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.calibration_list_layout, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView)view.findViewById(R.id.calibration_item_label);
			viewHolder.icon = (ImageView)view.findViewById(R.id.passed_test_icon);
			
			view.setTag(viewHolder);
			
			final ListItem item = list.get(position);
			
			view.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					item.setTestResult(!item.testPassed());
					
					if(item.testPassed())
						viewHolder.icon.setImageResource(R.drawable.checkmark);
					else
						viewHolder.icon.setImageResource(R.drawable.crossout);
				}
			});
		}
		else{
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder)view.getTag();
		holder.text.setText(list.get(position).getText());
		
		return view;
	}
}
