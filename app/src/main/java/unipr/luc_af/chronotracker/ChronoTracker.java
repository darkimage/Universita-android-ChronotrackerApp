package unipr.luc_af.chronotracker;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.components.ChronoView;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.PopupItemsModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.services.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChronoTracker extends Fragment {
    private TitleBarModel mTitleBarModel;
    private ChronoView mChronometer;
    private ActivitySessionModel mActivitiSessionModel;

    public ChronoTracker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Utils.getInstance().setToolBarNavigation((AppCompatActivity)getActivity());
        View view = inflater.inflate(R.layout.fragment_chrono_tracker, container, false);
        mChronometer = view.findViewById(R.id.tracker_chronometer);
        mChronometer.Start();

        mTitleBarModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
        mTitleBarModel.setTitle(getActivity().getString(R.string.chrono_tracker_title));
        mActivitiSessionModel = new ViewModelProvider(getActivity()).get(ActivitySessionModel.class);
        return view;
    }


    @Override
    public void onStop() {
        super.onStop();
        mChronometer.Pause();
        mActivitiSessionModel.setSessionStartData(null);
    }


}
