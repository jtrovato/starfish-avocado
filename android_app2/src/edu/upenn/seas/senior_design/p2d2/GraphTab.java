package edu.upenn.seas.senior_design.p2d2;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

public class GraphTab extends Fragment {
	
	private String TAG = "graph tab";
	private MainTabActivity a;
	public int graph_frag_id;
	Handler mHandler = new Handler();
	
	Thread gupdate = new Thread(new Runnable() {
	        
	        public void run() {
	            // TODO Auto-generated method stub
	            while (true) {
	                try {
	                    Thread.sleep(100);
	                    mHandler.post(new Runnable() {

	                        @Override
	                        public void run() {
	                            // TODO Auto-generated method stub
	                        	a.fluo_graph.redrawAll();
	                        }
	                    });
	                } catch (Exception e) {
	                    // TODO: handle exception
	                }
	            }
	        }
        });
	 
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
		a = (MainTabActivity)getActivity();
		//graph stuff
		a.fluo_graph_data = new GraphViewData[]{};
		a.fluo_series1 = new GraphViewSeries("Channel 1", new GraphViewSeriesStyle(Color.rgb(200, 50, 00),4), a.fluo_graph_data);
		a.fluo_series2 = new GraphViewSeries("Channel 2", new GraphViewSeriesStyle(Color.rgb(90, 250, 00),4), a.fluo_graph_data);
		a.fluo_series3 = new GraphViewSeries("Channel 3", new GraphViewSeriesStyle(Color.rgb(0, 50, 250),4), a.fluo_graph_data);
		a.fluo_graph = new LineGraphView(a, "Channel Fluorescence");
		a.fluo_graph.addSeries(a.fluo_series1);
		a.fluo_graph.addSeries(a.fluo_series2);
		a.fluo_graph.addSeries(a.fluo_series3);
		a.fluo_graph.setShowLegend(true); 
		a.fluo_graph.setLegendAlign(LegendAlign.BOTTOM);
		a.fluo_graph.setLegendWidth(200);
		a.fluo_graph.getGraphViewStyle().setNumHorizontalLabels(5);
		a.fluo_graph.getGraphViewStyle().setNumVerticalLabels(8);
		
		
		
        View view = inflater.inflate(R.layout.graph_frag, container, false);
        if(container == null)
        	Log.e(TAG, "container is null");
        if((Object)(container.getId()) == null)
        	Log.e(TAG, "container ID is null");

        a.graph_frag_id = container.getId();
        Log.d(TAG, "graph_frag_id = " + Integer.toString(a.graph_frag_id));
        LinearLayout graph_layout = (LinearLayout) view.findViewById(R.id.graph);
        if(a.fluo_graph == null)
        	Log.e(TAG, "the graphView is null");
        graph_layout.addView(a.fluo_graph);
        //set up graph update handler
        gupdate.start();
        
        
        return view;
	}
	public int getContainerId()
	{
		return ((ViewGroup)getView().getParent()).getId();
	}
	
}
