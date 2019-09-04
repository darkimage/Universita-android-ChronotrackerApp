package unipr.luc_af.chronotracker;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.chronotracker.helpers.Utils;
import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.classes.Lap;
import unipr.luc_af.classes.StartSessionData;
import unipr.luc_af.components.ChronoView;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.ActivitySessionModel;

import static unipr.luc_af.chronotracker.MainActivity.TOOLBAR_TRACKER_TAG;
import static unipr.luc_af.chronotracker.MainActivity.TRACKER_TAG;

public class ToolBarTracker extends Fragment {
    private static final String SESSION_BUNDLE = "session_bundle";
    //LOGIC
    private ChronoView.ChronoService mChronoService;
    private ActivitySessionModel mActivitySessionModel;
    private ActivitySession mActivitySession;
    private Utils utils = Utils.getInstance();
    private ChronoView.ChronoData mChronoData;

    //UI
    private AnimatedVectorDrawable mStartToStopAnim;
    private AnimatedVectorDrawable mStopToStartAnim;
    private Boolean startStopButtonState = false;
    private TextView mDurationText;
    private TextView mAthleteText;
    private TextView mLapText;
    private Button mLapButton;
    private ImageButton mStartStopButton;
    private ConstraintLayout mLayout;

    public ToolBarTracker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_toolbar_tracker, container, false);

        setUpModels(savedInstanceState);
        setUpView(view);
        updateView(0,0);
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindFromChronoService();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindToChronoService();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mChronoService = ((ChronoView.ChronoService.ChronoBinder) iBinder).getService();
            mChronoService.setOnTickListener((duration, lap) -> {
                updateView(duration,lap);
            });
            mChronoData = mChronoService.getData();
            setActionStatus(mChronoData.state);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mChronoService = null;
        }
    };

    private void bindToChronoService(){
        getActivity().bindService(new Intent(getActivity(), ChronoView.ChronoService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFromChronoService(){
        getActivity().unbindService(mConnection);
    }

    private void updateView(long duration, long lap){
        mDurationText.setText(utils.formatTime(duration,true));
        mLapText.setText(utils.formatTime(lap,true));
    }

    private void setUpModels(Bundle savedInstanceState){
        mActivitySessionModel = new ViewModelProvider(getActivity()).get(ActivitySessionModel.class);
        mActivitySessionModel.getToolBarSession().observe(getActivity(), (session) -> {
            if(session != null) {
                mActivitySession = session;
            }
        });

        if(savedInstanceState != null){
            mActivitySession = savedInstanceState.getParcelable(SESSION_BUNDLE);
            mActivitySessionModel.setActivitySession(mActivitySession);
        }

    }

    private void setActionStatus(ChronoView.ChronoData.State state){
        if(state == ChronoView.ChronoData.State.PAUSE){
            startStopButtonState = true;
            mStartStopButton.setImageDrawable(mStopToStartAnim);
            mLapButton.setEnabled(false);
        }
        if(state == ChronoView.ChronoData.State.TRACK){
            startStopButtonState = false;
            mStartStopButton.setImageDrawable(mStartToStopAnim);
            mLapButton.setEnabled(true);
        }
        if(state == ChronoView.ChronoData.State.RESET) {
            startStopButtonState = true;
            mStartStopButton.setImageDrawable(mStopToStartAnim);
            mLapButton.setEnabled(false);
        }
        ((AnimatedVectorDrawable)mStartStopButton.getDrawable()).start();
    }

    private void Lap(){
        mChronoService.LapTask(() ->{
            Lap[] laps = Arrays.copyOf(mActivitySession.laps,mActivitySession.laps.length+1);
            laps[laps.length-1] = new Lap(mChronoData.lastLapDuration, mChronoService.getCurrentTaskElapsed());
            mActivitySession.laps = laps;
            mLapText.setText(utils.formatTime(mChronoData.lastLapDuration,true));
        });
    }

    private void restoreChronometerFragment(){
        mActivitySessionModel.setRestoreSession(mActivitySession);
        ToolBarTracker fragment = (ToolBarTracker)getActivity().getSupportFragmentManager().findFragmentByTag(TOOLBAR_TRACKER_TAG);
        if(fragment != null){
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(
                            R.anim.horizontal_in_left,
                            R.anim.horizontal_out_left,
                            R.anim.horizontal_in,
                            R.anim.horizontal_out)
                    .replace(R.id.root, new ChronoTracker(),TRACKER_TAG)
                    .addToBackStack(TRACKER_TAG)
                    .commit();
            fragmentManager.executePendingTransactions();
        }
    }

    private void setUpView(View view){
        mStopToStartAnim = (AnimatedVectorDrawable)getActivity().getDrawable(R.drawable.pause_to_start_anim);
        mStartToStopAnim = (AnimatedVectorDrawable)getActivity().getDrawable(R.drawable.start_to_pause_anim);

        mLayout = view.findViewById(R.id.toolbar_tracker_layout);
        mLayout.setOnClickListener((v) -> restoreChronometerFragment());

        mDurationText = view.findViewById(R.id.toolbar_tracker_time);
        mLapText = view.findViewById(R.id.toolbar_tracker_lap_text);
        mAthleteText = view.findViewById(R.id.toolbar_tracker_athlete_name);
        DatabaseResult athleteNameResult = (cursor) ->{
            cursor.moveToNext();
            mAthleteText.setText(cursor.getString(1));
        };
        Database.getInstance().getAthleteFromId(mActivitySession.athlete,athleteNameResult);

        mStartStopButton = view.findViewById(R.id.toolbar_tracker_start_pause);

        ImageButton startStopButton = view.findViewById(R.id.toolbar_tracker_start_pause);
        startStopButton.setOnClickListener((v) -> {
            if(startStopButtonState)
                mChronoService.ExecuteTask(() -> setActionStatus(mChronoData.state));
            else
                mChronoService.PauseTask(() -> {
                    setActionStatus(mChronoData.state);
                    setSessionStopTime();
                });
        });

        mLapButton = view.findViewById(R.id.toolbar_tracker_lap_btn);
        mLapButton.setOnClickListener((btn) -> Lap());
    }

    private void setSessionStopTime(){
        mActivitySession.stopTime = mChronoData.lastPausedTime;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SESSION_BUNDLE,mActivitySession);
    }
}
