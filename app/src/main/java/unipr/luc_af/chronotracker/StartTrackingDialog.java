package unipr.luc_af.chronotracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import unipr.luc_af.services.Utils;

public class StartTrackingDialog extends DialogFragment {


    public StartTrackingDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Utils.getInstance().setToolBarNavigation((AppCompatActivity)getActivity());
        return inflater.inflate(R.layout.dialog_start_tracking, container, false);
    }

}
