package com.computemachines.android.workplay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by tparker on 4/25/16.
 */
public class RatioChooser extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setStyle(android.R.style.Theme_Holo_Dialog, DialogFragment.STYLE_NORMAL);
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.ratio_chooser, container, false);
//        getDialog().setTitle("Play:Work  Goal Ratio");
//
//        // add dynamically generated child buttons with callbacks (back and hide(0))
//
//        return root;
////        return super.onCreateView(inflater, container, savedInstanceState);
//    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_goal_ratio);
        builder.setItems(new String[]{"1:1", "1:2", "1:3", "1:4", "1:5", "1:6"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                ((ClockingActivity)getActivity()).setGoalRatioDenom(which+1);
                dismiss();
            }
        });
        return builder.create();
    }
}
