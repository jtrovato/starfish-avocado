package edu.upenn.seas.senior_design.p2d2;

import java.util.ArrayList;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import edu.upenn.seas.senior_design.p2d2.InstructionsFragment.InstructionDialogListener;

public class ResultsActivity extends Activity implements InstructionDialogListener{
	
	private String TAG = "Results Activity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		Bundle extra = this.getIntent().getExtras();
		ArrayList<int[]> fluo_data = (ArrayList<int[]>)extra.get("fluo_data");
		ArrayList<Long> time_data = (ArrayList<Long>)extra.get("time_data");
		GraphViewData[] data0 = new GraphViewData[time_data.size()];
		GraphViewData[] data1 = new GraphViewData[time_data.size()];
		GraphViewData[] data2 = new GraphViewData[time_data.size()];

		for(int i = 0; i < time_data.size(); i++)
		{
			data0[i] = new GraphViewData(time_data.get(i), fluo_data.get(i)[0]);
			data1[i] = new GraphViewData(time_data.get(i), fluo_data.get(i)[1]);
			data2[i] = new GraphViewData(time_data.get(i), fluo_data.get(i)[2]);

		}
		
		GraphViewSeries fluo_series1 = new GraphViewSeries("Channel 1", new GraphViewSeriesStyle(Color.rgb(200, 50, 00),4), data0);
		GraphViewSeries fluo_series2 = new GraphViewSeries("Channel 2", new GraphViewSeriesStyle(Color.rgb(90, 250, 00),4), data1);
		GraphViewSeries fluo_series3 = new GraphViewSeries("Channel 3", new GraphViewSeriesStyle(Color.rgb(0, 50, 250),4), data2);
		LineGraphView fluo_graph = new LineGraphView(this, "Channel Fluorescence");
		fluo_graph.addSeries(fluo_series1);
		fluo_graph.addSeries(fluo_series2);
		fluo_graph.addSeries(fluo_series3);
		fluo_graph.setShowLegend(true); 
		fluo_graph.setLegendAlign(LegendAlign.BOTTOM);
		fluo_graph.setLegendWidth(200);
		fluo_graph.getGraphViewStyle().setNumHorizontalLabels(5);
		fluo_graph.getGraphViewStyle().setNumVerticalLabels(8);
		
		
		LinearLayout graph_layout = (LinearLayout) this.findViewById(R.id.graph);
        if(fluo_graph == null)
        	Log.e(TAG, "the graphView is null");
        graph_layout.addView(fluo_graph);
        
        resultInstruction();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.results, menu);
		return true;
	}
	
	private void resultInstruction() {
		DialogFragment resultInstAlert = new InstructionsFragment().newInstance();
		Bundle bundle = new Bundle();
		bundle.putInt("inst", 3); //3 corresponds to result instructions
		resultInstAlert.setArguments(bundle);
		resultInstAlert.show(getFragmentManager(), "result_inst");
		}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}

}
