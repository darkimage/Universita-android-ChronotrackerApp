package unipr.luc_af.chronotracker;


import android.content.res.Configuration;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.classes.Lap;
import unipr.luc_af.classes.MeasureUnit;
import unipr.luc_af.classes.StartSessionData;
import unipr.luc_af.components.ChronoView;
import unipr.luc_af.database.interfaces.DatabaseInsert;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.PopupItemsModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.chronotracker.helpers.Utils;

public class ChronoTracker extends Fragment {
    public static final String PLAY_BUTTON_STATE = "play_btn_state";
    private ImageButton mLapButton;
    private ImageButton mResetButton;
    private TitleBarModel mTitleBarModel;
    private ImageButton mStartStopButton;
    private ChronoView mChronometer;
    private ActivitySessionModel mActivitySessionModel;
    private PopupItemsModel mPopupItemsModel;
    private RecyclerView mLapsList;
    private ArrayList<Lap> mLaps = new ArrayList<>();
    private LapTimeAdapter mLapTimeAdapter;
    private AnimatedVectorDrawable mStartToStopAnim;
    private AnimatedVectorDrawable mStopToStartAnim;
    private Boolean startStopButtonState = false;
    private StartSessionData mStartSessionData;
    private ActivitySession mActivitySession;
    private MeasureUnit mMeasureUnit;
    private boolean isSessionEnded = false;

    public ChronoTracker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Utils.getInstance().setToolBarNavigation((AppCompatActivity)getActivity());
        View view = inflater.inflate(R.layout.fragment_chrono_tracker, container, false);
        mStopToStartAnim = (AnimatedVectorDrawable)getActivity().getDrawable(R.drawable.pause_to_start_anim);
        mStartToStopAnim = (AnimatedVectorDrawable)getActivity().getDrawable(R.drawable.start_to_pause_anim);

        mChronometer = view.findViewById(R.id.tracker_chronometer);
        if(savedInstanceState == null){
            mChronometer.init();
        }else {
            mChronometer.bindToService();
            startStopButtonState = savedInstanceState.getBoolean(PLAY_BUTTON_STATE);
        }
        mChronometer.setOnStateChangeListener((state, chrono) -> setActionStatus(state));
        mChronometer.setOnLapListener((duration, fromStart) -> {
            mLaps.add(0,new Lap(duration,fromStart));
            updateLaps();
        });

        mLapsList = view.findViewById(R.id.tracker_laps);
        mLapsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLapTimeAdapter = new LapTimeAdapter(new Lap[0], getActivity());
        mLapsList.setAdapter(mLapTimeAdapter);

        mLapButton = view.findViewById(R.id.tracker_lap);
        mLapButton.setEnabled(false);
        mLapButton.setOnClickListener((v) -> mChronometer.Lap());

        mResetButton = view.findViewById(R.id.tracker_reset);
        mResetButton.setOnClickListener((v) -> mChronometer.Reset());
        mResetButton.setEnabled(false);

        mStartStopButton = view.findViewById(R.id.chronoview_start_stop_fab);
        mStartStopButton.setOnClickListener((v) -> {
            if(!startStopButtonState){
                mChronometer.Start();
//                mStartStopButton.setImageDrawable(mStopToStartAnim);
            }else{
                mChronometer.Pause();
//                mStartStopButton.setImageDrawable(mStartToStopAnim);
            }
//            ((AnimatedVectorDrawable)mStartStopButton.getDrawable()).start();
            startStopButtonState = !startStopButtonState;
        });

        mTitleBarModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
        mTitleBarModel.setTitle(getActivity().getString(R.string.chrono_tracker_title));
        mActivitySessionModel = new ViewModelProvider(getActivity()).get(ActivitySessionModel.class);
        mPopupItemsModel = new ViewModelProvider(getActivity()).get(PopupItemsModel.class);
        mPopupItemsModel.setActiveItems(new int[]{R.id.menu_tracking_done});


        mActivitySessionModel.getSessionStartData().observe(getActivity(), (data) -> {
            if(data != null) {
                mStartSessionData = data;
                mActivitySession = constructSession(data);
            }
        });

        mActivitySessionModel.getEndSession().observe(getActivity(), (data) ->{
            if(data != null) {
                isSessionEnded = true;
                mChronometer.Stop();
                updateSession(mStartSessionData);
                DatabaseInsert addSessionResult = (res) -> {
                    //add snackbar and change to activities list
                    getActivity().getSupportFragmentManager().popBackStack();
                };
                Database.getInstance().addSession(data, addSessionResult, (e) -> {
                    System.out.println(e.getLocalizedMessage());
                });
                mActivitySessionModel.setEndSession(null);
            }
        });

        DatabaseResult unitResult = (cursor) -> {
            cursor.moveToNext();
            mMeasureUnit = new MeasureUnit(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
        };
        Database.getInstance().getMeasureUnitFromId((long)1,unitResult);

        return view;
    }

    private void updateLaps(){
        mLapTimeAdapter.setLaps(getLapsArray());
        mLapTimeAdapter.notifyItemInserted(0);
        mLapsList.scrollToPosition(0);
    }

    private void resetLaps(){
        mLapTimeAdapter.setLaps(new Lap[0]);
        mLapTimeAdapter.notifyDataSetChanged();
    }

    private Lap[] getLapsArray(){
        Lap[] laps = new Lap[mLaps.size()];
        return mLaps.toArray(laps);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mActivitySessionModel.setStartSession(null);
        outState.putBoolean(PLAY_BUTTON_STATE, startStopButtonState);
        isSessionEnded = false;
    }

    private void updateSession(StartSessionData data){
        mActivitySession.athlete = data.athlete.id;
        mActivitySession.activity = data.activitySport.id;
        mActivitySession.activityType = (data.activitySportType!= null) ? data.activitySportType.id : null;
        mActivitySession.laps = getLapsArray();
        mActivitySession.startTime = mChronometer.getData().initialTime;
        mActivitySession.stopTime = mChronometer.getData().lastPausedTime;
        mActivitySession.distance = (long)1;
        mActivitySession.speed = 20;
        mActivitySessionModel.setActivitySession(mActivitySession);
    }

    private ActivitySession constructSession(StartSessionData data){
        return new ActivitySession(
                data.athlete.id,
                data.activitySport.id,
                (data.activitySportType != null) ? data.activitySportType.id : null,
                null);
    }

    private void setActionStatus(ChronoView.ChronoData.State state){
        if(state == ChronoView.ChronoData.State.PAUSE){
            mStartStopButton.setImageDrawable(mStopToStartAnim);
            updateSession(mStartSessionData);
            mResetButton.setEnabled(true);
            mLapButton.setEnabled(false);
        }
        if(state == ChronoView.ChronoData.State.TRACK){
            mStartStopButton.setImageDrawable(mStartToStopAnim);
            updateSession(mStartSessionData);
            mLapButton.setEnabled(true);
            mResetButton.setEnabled(false);
        }
        if(state == ChronoView.ChronoData.State.RESET) {
            mStartStopButton.setImageDrawable(mStopToStartAnim);
            updateSession(mStartSessionData);
            mLapButton.setEnabled(false);
            mResetButton.setEnabled(false);
            mLaps.clear();
            resetLaps();
        }
        ((AnimatedVectorDrawable)mStartStopButton.getDrawable()).start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isSessionEnded) {
            mChronometer.Stop();
        }
        mChronometer.unbindFromService();
    }
}
