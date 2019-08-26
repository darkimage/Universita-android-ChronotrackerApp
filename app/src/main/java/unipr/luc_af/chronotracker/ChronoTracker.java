package unipr.luc_af.chronotracker;


import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import unipr.luc_af.components.ChronoView;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.services.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChronoTracker extends Fragment {
    private TitleBarModel mTitleBarModel;
    private ImageButton mStartStopButton;
    private ChronoView mChronometer;
    private ActivitySessionModel mActivitiSessionModel;
    AnimatedVectorDrawable mStartToStopAnim;
    AnimatedVectorDrawable mStopToStartAnim;
    Boolean startStopButtonState = false;

    public ChronoTracker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Utils.getInstance().setToolBarNavigation((AppCompatActivity)getActivity());
        View view = inflater.inflate(R.layout.fragment_chrono_tracker, container, false);

        mStartToStopAnim = (AnimatedVectorDrawable)getActivity().getDrawable(R.drawable.pause_to_start_anim);
        mStopToStartAnim = (AnimatedVectorDrawable)getActivity().getDrawable(R.drawable.start_to_pause_anim);

        mChronometer = view.findViewById(R.id.tracker_chronometer);
//        mChronometer.Start();

        view.findViewById(R.id.test).setOnClickListener((v) ->{
            mChronometer.Lap();
        });

        mStartStopButton = view.findViewById(R.id.chronoview_start_stop_fab);
        mStartStopButton.setOnClickListener((v) ->{
            if(!startStopButtonState){
                mChronometer.Start();
                mStartStopButton.setImageDrawable(mStopToStartAnim);
            }else{
                mChronometer.Pause();
                mStartStopButton.setImageDrawable(mStartToStopAnim);
            }
            ((AnimatedVectorDrawable)mStartStopButton.getDrawable()).start();
            startStopButtonState = !startStopButtonState;
        });

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
