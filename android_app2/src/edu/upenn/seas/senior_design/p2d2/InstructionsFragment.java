package edu.upenn.seas.senior_design.p2d2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

public class InstructionsFragment extends DialogFragment {

	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */
	public interface InstructionDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);

		public void onDialogNegativeClick(DialogFragment dialog);
	}

	// Use this instance of the interface to deliver action events
	InstructionDialogListener mListener;

	public static InstructionsFragment newInstance() {
		InstructionsFragment frag = new InstructionsFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (InstructionDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int inst = -1;
		int defaultValue = -1;
		Bundle bundle = this.getArguments();
		if (bundle != null) {
			inst = bundle.getInt("inst", defaultValue);
		}
		if (inst == 0) {
			return new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.home_instruction_title))
					.setMessage(getString(R.string.home_instruction_text))
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dismiss();
								}
							}).create();
		} else if (inst == 1) {
			return new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.test_instruction_title))
					.setMessage(getString(R.string.test_instruction_text))
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dismiss();
								}
							}).create();
		} else if (inst == 2) {
			AlertDialog dialog = new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.cal_check_title))
					.setMessage(getString(R.string.cal_check_text))
					.setPositiveButton("Looks Good",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Send the positive button event back to
									// the host activity
									mListener
											.onDialogPositiveClick(InstructionsFragment.this);
								}
							})
					.setNegativeButton("Try Again",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									// Send the negative button event back to
									// the host activity
									mListener
											.onDialogNegativeClick(InstructionsFragment.this);
								}
							}).create();
			WindowManager.LayoutParams wmlp = dialog.getWindow()
					.getAttributes();
			wmlp.gravity = Gravity.BOTTOM;
			return dialog;
		} else if (inst == 3) {
			return new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.pos_result_instruction_title))
					.setMessage(getString(R.string.result_instruction_text))
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dismiss();
								}
							}).create();
		} else if (inst == 4) {
			return new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.neg_result_instruction_title))
					.setMessage(getString(R.string.result_instruction_text))
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dismiss();
								}
							}).create();
		} else if (inst == 5) {
			return new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.actuation_instruction_title))
					.setMessage(getString(R.string.actuation_instruction_text))
					.create();
		} else {
			return new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.error_instruction_title))
					.setMessage(getString(R.string.error_instruction_text))
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dismiss();
								}
							}).create();
		}
	}

}
