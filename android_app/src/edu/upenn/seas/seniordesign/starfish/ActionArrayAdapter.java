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
		
		//Set the text of the list item
		TextView text = (TextView)view.findViewById(R.id.calibration_item_label);
		text.setText(item.getText());

		return view;
	}
}
