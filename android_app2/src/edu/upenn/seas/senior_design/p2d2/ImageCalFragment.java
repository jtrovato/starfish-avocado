package edu.upenn.seas.senior_design.p2d2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ImageCalFragment extends DialogFragment {
	public static ImageCalFragment newInstance() {
		ImageCalFragment frag = new ImageCalFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
			int inst = -1;
			int defaultValue = -1;
			Bundle bundle = this.getArguments();
			if(bundle != null){
			    inst = bundle.getInt("inst", defaultValue);
			}
			if(inst == 0){
				return new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.home_instruction_title))
				.setMessage(getString(R.string.home_instruction_text))
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				})
				.create();
			} else if(inst == 1)
			{
				return new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.test_instruction_title))
				.setMessage(getString(R.string.test_instruction_text))
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				})
				.create();
			}
			else{
				return new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.error_instruction_title))
				.setMessage(getString(R.string.error_instruction_text))
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				})
				.create();
			}
	}


}
