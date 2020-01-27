package unipr.luc_af.chronotracker;


import android.database.SQLException;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;

import unipr.luc_af.adapters.LapTimeAdapter;
import unipr.luc_af.chronotracker.helpers.ChronoService;
import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.chronotracker.helpers.Utils;
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

public class ChronoTracker extends Fragment {
    private static final String PLAY_BUTTON_STATE = "play_btn_state";
    private static final String SESSION_STATE = "session_state";
    private ImageButton mLapButton;
    private ImageButton mResetButton;
    private TitleBarModel mTitleBarModel;
    private ImageButton mStartStopButton;
    private ChronoView mChronometer;
    private ActivitySessionModel mActivitySessionModel;
    private PopupItemsModel mPopupItemsModel;
    private RecyclerView mLapsList;
    private ArrayList<Lap> mLaps;
    private LapTimeAdapter mLapTimeAdapter;
    private AnimatedVectorDrawable mStartToStopAnim;
    private AnimatedVectorDrawable mStopToStartAnim;
    private Boolean startStopButtonState = false;
    private StartSessionData mStartSessionData;
    private ActivitySession mActivitySession;
    private MeasureUnit[] mMeasureUnits;
    private Button mUnitButton;
    private boolean isSessionEnded = false;
    private TextView mUnitText;

    public ChronoTracker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Utils.getInstance().setToolBarNavigation((AppCompatActivity) getActivity());
        View view = inflater.inflate(R.layout.fragment_chrono_tracker, container, false);

        setUpModels(savedInstanceState);
        setUpUi(view, savedInstanceState);
        return view;
    }


    private void setUpModels(Bundle saveInstanceState) {
        mLaps = new ArrayList<>();
        mTitleBarModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
        mTitleBarModel.setTitle(getActivity().getString(R.string.chrono_tracker_title));
        mActivitySessionModel = new ViewModelProvider(getActivity()).get(ActivitySessionModel.class);
        mPopupItemsModel = new ViewModelProvider(getActivity()).get(PopupItemsModel.class);
        mPopupItemsModel.setActiveItems(new int[]{R.id.menu_tracking_done});

        if (saveInstanceState != null) {
            mActivitySession = saveInstanceState.getParcelable(SESSION_STATE);
            mLaps = new ArrayList<>(Arrays.asList(mActivitySession.laps));
        } else {
            mActivitySessionModel.getStartSession().observe(getActivity(), (data) -> {
                if (data != null) {
//                    mStartSessionData = data;
                    mActivitySession = constructSession(data);
                }
            });
        }

        mActivitySessionModel.getEndSession().observe(getActivity(), (data) -> finishSession(data));
    }

    private void setUpUi(View view, Bundle savedInstanceState) {
        mStopToStartAnim = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.pause_to_start_anim);
        mStartToStopAnim = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.start_to_pause_anim);

        mChronometer = view.findViewById(R.id.tracker_chronometer);
        if (savedInstanceState == null) {
            mChronometer.init();
        } else {
            mChronometer.bindToService();
            startStopButtonState = savedInstanceState.getBoolean(PLAY_BUTTON_STATE);
        }
        mChronometer.setOnStateChangeListener((state, chrono) -> setActionStatus(state));
        mChronometer.setOnLapListener((duration, fromStart) -> {
            mLaps.add(0, new Lap(duration, fromStart));
            updateLaps();
        });
        mChronometer.setOnServiceConnectedListener((service) ->
                setOnApplicationClosed(service)
        );

        mLapsList = view.findViewById(R.id.tracker_laps);
        mLapsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLapTimeAdapter = new LapTimeAdapter(new Lap[0], getActivity());
        if (mLaps != null) {
            mLapTimeAdapter.setLaps(getLapsArray());
        }
        mLapsList.setAdapter(mLapTimeAdapter);

        mLapButton = view.findViewById(R.id.toolbar_tracker_lap_btn);
        mLapButton.setEnabled(false);
        mLapButton.setOnClickListener((v) -> mChronometer.Lap());

        mResetButton = view.findViewById(R.id.tracker_reset);
        mResetButton.setOnClickListener((v) -> mChronometer.Reset());
        mResetButton.setEnabled(false);

        mStartStopButton = view.findViewById(R.id.chronoview_start_stop_fab);
        mStartStopButton.setOnClickListener((v) -> {
            if (!startStopButtonState)
                mChronometer.Start();
            else
                mChronometer.Pause();
//            startStopButtonState = !startStopButtonState;
        });

        mActivitySessionModel.getRestoreSession().observe(getActivity(), (data) -> {
            if (data != null) {
                mActivitySession = data;
                if (mActivitySession.laps != null)
                    mLaps = new ArrayList<>(Arrays.asList(mActivitySession.laps));
                setLaps(mActivitySession.laps);
                mActivitySessionModel.setActivitySession(mActivitySession);
                initUnitButton(savedInstanceState, mActivitySession.distance);
            }
            mActivitySessionModel.getRestoreSession().removeObservers(getActivity());
        });

        mUnitButton = view.findViewById(R.id.tracker_unit_button);
        mUnitText = view.findViewById(R.id.tracker_unit_text);
        mUnitButton.setOnClickListener((v) -> toggleUnits());

        initUnitButton(savedInstanceState, null);
    }

    private void initUnitButton(Bundle savedInstanceState, Long unit) {
        DatabaseResult unitsResult = (cursor) -> {
            cursor.moveToNext();
            mMeasureUnits = new MeasureUnit[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                mMeasureUnits[i] = new MeasureUnit(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
                cursor.moveToNext();
            }
            if (savedInstanceState == null && unit == null) {
                setUnitButton(mMeasureUnits[0]);
            } else if (savedInstanceState != null) {
                setUnitButton(mActivitySession.distance);
            } else {
                setUnitButton(unit);
            }
        };
        Database.getInstance().getMeasureUnits(unitsResult);
    }

    private void setUnitButton(Long id) {
        DatabaseResult unitResult = (cursor) -> {
            cursor.moveToNext();
            MeasureUnit measureUnit = new MeasureUnit(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
            setUnitButton(measureUnit);
        };
        Database.getInstance().getMeasureUnitFromId(id, unitResult);
    }

    private void setUnitButton(MeasureUnit unit) {
        mUnitButton.setText(unit.shortName);
        mUnitButton.setTag(unit);
        mUnitText.setText(unit.name);
    }

    private void toggleUnits() {
        MeasureUnit currentUnit = (MeasureUnit) mUnitButton.getTag();
        MeasureUnit nextUnit = mMeasureUnits[(int) (currentUnit.id % (mMeasureUnits.length))];
        setUnitButton(nextUnit);
        updateSession();
    }

    private void setOnApplicationClosed(ChronoService service) {
        service.setOnTaskRemoved(() -> {
            mChronometer.Pause();
            mChronometer.stopChronoService();
            isSessionEnded = true;
            updateSession();
            Database.getInstance().addSession(mActivitySession, (res) -> {
            }, (e) -> {
                System.out.println(e.getLocalizedMessage());
            });
            mActivitySessionModel.setEndSession(null);
        });
    }

    private void setLaps(Lap[] laps) {
        mLapTimeAdapter.setLaps(laps);
        mLapTimeAdapter.notifyDataSetChanged();
        mActivitySession.laps = laps;
    }

    private void updateLaps() {
        mLapTimeAdapter.setLaps(getLapsArray());
//        mActivitySessionModel.setSessionLaps(getLapsArray());
        mLapTimeAdapter.notifyItemInserted(0);
        mLapsList.scrollToPosition(0);
        mActivitySession.laps = getLapsArray();
        mActivitySessionModel.setActivitySession(mActivitySession);
    }

    private void resetLaps() {
//        mActivitySessionModel.setSessionLaps(new Lap[0]);
        mLapTimeAdapter.setLaps(new Lap[0]);
        mLapTimeAdapter.notifyDataSetChanged();
        mActivitySession.laps = new Lap[0];
        mActivitySessionModel.setActivitySession(mActivitySession);
    }

    private Lap[] getLapsArray() {
        Lap[] laps = new Lap[mLaps.size()];
        return mLaps.toArray(laps);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mActivitySessionModel.setStartSession(null);
        outState.putBoolean(PLAY_BUTTON_STATE, startStopButtonState);
        isSessionEnded = false;
        updateSession();
        outState.putParcelable(SESSION_STATE, mActivitySession);
    }

    private void updateSession() {
        if (mActivitySession != null) {
            mActivitySession.laps = getLapsArray();
            mActivitySession.startTime = mChronometer.getData().initialTime;
            mActivitySession.stopTime = mChronometer.getData().lastPausedTime;
            if (mUnitButton.getTag() != null) {
                mActivitySession.distance = ((MeasureUnit) mUnitButton.getTag()).id;
            }
            mActivitySessionModel.setActivitySession(mActivitySession);
        }
    }

    private ActivitySession constructSession(StartSessionData data) {
        ActivitySession session = new ActivitySession();
        session.activity = data.activitySport.id;
        session.athlete = data.athlete.id;
        session.speed = 1;
        session.activityType = (data.activitySportType != null) ? data.activitySportType.id : null;
        mActivitySessionModel.setActivitySession(session);
        return session;
    }

    public interface AddSessionListener {
        void OnSuccess(long id);

        void OnError(SQLException err);
    }

    public static void AddSession(ActivitySession session, AddSessionListener sessionListener) {
        DatabaseInsert addSessionResult = (res) -> {
            sessionListener.OnSuccess(res);
        };
        Database.getInstance().addSession(session, addSessionResult, (e) -> {
            sessionListener.OnError(e);
        });
    }

    private void finishSession(ActivitySession data) {
        if (data != null) {
            isSessionEnded = true;
            mChronometer.Stop();
            updateSession();
            View coordLayout = getActivity().findViewById(R.id.root_coordinator_layout);
            FragmentManager mng = getActivity().getSupportFragmentManager();
            AddSession(mActivitySession, new AddSessionListener() {
                @Override
                public void OnSuccess(long id) {
                    mng.popBackStackImmediate();
                    Snackbar.make(coordLayout, getActivity().getString(R.string.session_added), Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void OnError(SQLException err) {
                    mng.popBackStackImmediate();
                    Snackbar.make(coordLayout, getActivity().getString(R.string.session_error), Snackbar.LENGTH_LONG).show();
                }
            });
            mActivitySessionModel.setEndSession(null);
            mActivitySessionModel.setStartSession(null);
        }
    }

    private void setActionStatus(ChronoService.ChronoData.State state) {
        if (state == ChronoService.ChronoData.State.PAUSE) {
            startStopButtonState = false;
            mStartStopButton.setImageDrawable(mStopToStartAnim);
            mResetButton.setEnabled(true);
            mLapButton.setEnabled(false);
            updateSession();
        }
        if (state == ChronoService.ChronoData.State.TRACK) {
            startStopButtonState = true;
            mStartStopButton.setImageDrawable(mStartToStopAnim);
            mLapButton.setEnabled(true);
            mResetButton.setEnabled(false);
            updateSession();
        }
        if (state == ChronoService.ChronoData.State.RESET) {
            startStopButtonState = false;
            mStartStopButton.setImageDrawable(mStopToStartAnim);
            mLapButton.setEnabled(false);
            mResetButton.setEnabled(false);
            mLaps.clear();
            resetLaps();
        }
        ((AnimatedVectorDrawable) mStartStopButton.getDrawable()).start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!isSessionEnded) {
            mActivitySessionModel.setToolbarSession(mActivitySession);
        }
        mActivitySessionModel.setRestoreSession(null);
        mActivitySessionModel.getRestoreSession().removeObservers(getActivity());
        mActivitySessionModel.setEndSession(null);
        mActivitySessionModel.getEndSession().removeObservers(getActivity());
        mActivitySessionModel.setStartSession(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isSessionEnded) {
            mChronometer.Stop();
        }
        mChronometer.unbindFromService();
    }
}
