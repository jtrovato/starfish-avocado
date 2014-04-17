package edu.upenn.seas.senior_design.p2d2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class AlertDialogFragmentBT extends DialogFragment {
	public static AlertDialogFragmentBT newInstance() {
		AlertDialogFragmentBT frag = new AlertDialogFragmentBT();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.bt_unavailable_dialog_title))
				.setMessage(getString(R.string.bt_unavailable_dialog_text))
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				})
				.create();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		getActivity().finish();
		super.onDismiss(dialog);
	}
}
