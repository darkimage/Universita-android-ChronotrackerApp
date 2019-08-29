package unipr.luc_af.chronotracker;


import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import java.util.ArrayList;
import unipr.luc_af.adapters.LapTimeAdapter;
import unipr.luc_af.classes.Lap;
import unipr.luc_af.components.ChronoView;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.chronotracker.helpers.Utils;


public class ChronoTracker extends Fragment {
    private ImageButton mLapButton;
    private ImageButton mResetButton;
    private TitleBarModel mTitleBarModel;
    private ImageButton mStartStopButton;
    private ChronoView mChronometer;
    private ActivitySessionModel mActivitiSessionModel;
    private RecyclerView mLapsList;
    private ArrayList<Lap> mLaps = new ArrayList<>();
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
        mChronometer.setOnStateChangeListener((state, chrono) -> setActionStatus(state));
        mChronometer.setOnLapListener((duration, fromStart) -> {
            mLaps.add(0,new Lap(duration,fromStart));
            updateLaps();
        });

        mLapsList = view.findViewById(R.id.tracker_laps);
        mLapsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLapsList.setAdapter(new LapTimeAdapter(new Lap[0], getActivity()));

        mLapButton = view.findViewById(R.id.tracker_lap);
        mLapButton.setEnabled(false);
        mLapButton.setOnClickListener((v) -> mChronometer.Lap());

        mResetButton = view.findViewById(R.id.tracker_reset);
        mResetButton.setOnClickListener((v) -> mChronometer.Reset());
        mResetButton.setEnabled(false);

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
//        mChronometer.Pause();
        mActivitiSessionModel.setSessionStartData(null);
    }

    private void updateLaps(){
        Lap[] laps = new Lap[mLaps.size()];
        mLapsList.setAdapter(new LapTimeAdapter(mLaps.toArray(laps), getActivity()));
        mLapsList.invalidate();
    }

    private void setActionStatus(ChronoView.ChronoData.State state){
        if(state == ChronoView.ChronoData.State.PAUSE){
            mResetButton.setEnabled(true);
            mLapButton.setEnabled(false);
        }
        if(state == ChronoView.ChronoData.State.TRACK){
            mLapButton.setEnabled(true);
            mResetButton.setEnabled(false);
        }
        if(state == ChronoView.ChronoData.State.RESET) {
            mLapButton.setEnabled(false);
            mResetButton.setEnabled(false);
            mLaps.clear();
            updateLaps();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChronometer.Stop();
    }
}
