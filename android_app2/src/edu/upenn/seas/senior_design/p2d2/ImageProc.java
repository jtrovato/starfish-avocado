package edu.upenn.seas.senior_design.p2d2;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;

public class ImageProc {
	
	public static int[] getFluorescence(Mat inputIm,  ArrayList<Mat> channels)
	{

		int[] fluo = new int[channels.size()];
		for(Mat im : channels)
		{
			
		}
		
		return fluo;
	}
	/* this function uses the dimension of the cassette to exactly localize the channels */
	public static ArrayList<Rect> findChannels(Rect ROI)
	{
		ArrayList<Rect> channels = new ArrayList<Rect>();
		Size ROI_size = ROI.size();
		double ROI_width = ROI_size.width;
		double ROI_height = ROI_size.height;
		Rect c1 = new Rect(ROI.tl(), new Point(ROI.tl().x + (0.04/0.3)*ROI_width, ROI.br().y));
		Rect c2 = new Rect(new Point(ROI.tl().x + (0.13/0.3)*ROI_width, ROI.tl().y), new Point(ROI.br().x-(0.13/0.3)*ROI_width, ROI.br().y));
		Rect c3 = new Rect(new Point(ROI.tl().x + (0.26/0.3)*ROI_width, ROI.tl().y), ROI.br());
		channels.add(c1);
		channels.add(c2);
		channels.add(c3);
		
		return channels;
	}
	
	

}
