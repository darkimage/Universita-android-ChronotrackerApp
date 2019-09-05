package unipr.luc_af.chronotracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import unipr.luc_af.adapters.LapTimeAdapter;
import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.chronotracker.helpers.Utils;
import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.PopupItemsModel;
import unipr.luc_af.models.TitleBarModel;

public class ActivitySummary extends Fragment {
    //LOGIC
    private static final String SESSION_TAG = "activity_summary_session";
    private ActivitySession mActivitySession;
    private ActivitySessionModel mActivitySessionModel;
    private TitleBarModel mTitleBarModel;
    private PopupItemsModel mPopupItemsModel;
    private LapTimeAdapter mLapTimeAdapter;
    Utils utils = Utils.getInstance();

    //UI
    private TextView mSummaryHeader;
    private TextView mDayText;
    private TextView mAthleteText;
    private TextView mActivityType;
    private TextView mDurationText;
    private TextView mNoLapsText;
    private RecyclerView mLapList;

    public ActivitySummary() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity_summary, container, false);
        utils.setToolBarNavigation((AppCompatActivity) getActivity());
        SetUpModels(view, savedInstanceState);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mActivitySession = savedInstanceState.getParcelable(SESSION_TAG);
        }
    }

    private void SetUpUi(View view) {
        mSummaryHeader = view.findViewById(R.id.activity_summary_header);
        DatabaseResult headerResult = (cursor) -> {
            cursor.moveToNext();
            mSummaryHeader.setText(getActivity().getString(R.string.activity_placeholder, cursor.getString(1)));
        };
        Database.getInstance().getActivityFromId(mActivitySession.activity, headerResult);

        mDayText = view.findViewById(R.id.activity_summary_day);
        mDayText.setText(utils.formatDate(mActivitySession.startTime, "dd/MM/YYYY"));

        mAthleteText = view.findViewById(R.id.activity_summary_athlete_name);
        DatabaseResult athleteResult = (cursor) -> {
            cursor.moveToNext();
            mAthleteText.setText(Utils.getInstance().concatString(" ",
                    cursor.getString(1),
                    cursor.getString(2)));
        };
        Database.getInstance().getAthleteFromId(mActivitySession.athlete, athleteResult);

        mActivityType = view.findViewById(R.id.activity_summary_activity_type);
        DatabaseResult activityTypeResult = (cursor) -> {
            cursor.moveToNext();
            mActivityType.setText(cursor.getString(1));
        };
        if (mActivitySession.activityType != null) {
            Database.getInstance().getActivitiesTypesFromId(mActivitySession.activityType, activityTypeResult);
        } else {
            mActivityType.setText(getActivity().getString(R.string.none));
        }

        mDurationText = view.findViewById(R.id.activity_summary_duration);
        mDurationText.setText(utils.formatTime(mActivitySession.stopTime - mActivitySession.startTime, true));

        mLapList = view.findViewById(R.id.activity_summary_lap_list);
        mLapList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mNoLapsText = view.findViewById(R.id.activity_summary_no_laps);
        if (mActivitySession.laps.length != 0) {
            mLapTimeAdapter = new LapTimeAdapter(mActivitySession.laps, getActivity());
            mLapList.setAdapter(mLapTimeAdapter);
            mNoLapsText.setVisibility(View.GONE);
        } else {
            mLapTimeAdapter = new LapTimeAdapter(mActivitySession.laps, getActivity());
            mLapList.setAdapter(mLapTimeAdapter);
            mNoLapsText.setVisibility(View.VISIBLE);
        }

    }

    private void SetUpModels(View view, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mActivitySession = savedInstanceState.getParcelable(SESSION_TAG);
            SetUpUi(view);
        }
        mTitleBarModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
        mTitleBarModel.setTitle(getActivity().getString(R.string.activity_summary));

        mPopupItemsModel = new ViewModelProvider(getActivity()).get(PopupItemsModel.class);
        mPopupItemsModel.setActiveItems(new int[0]);

        mActivitySessionModel = new ViewModelProvider(getActivity()).get(ActivitySessionModel.class);
        mActivitySessionModel.getSelectedActivitySession().observe(getActivity(), (data) -> {
            if (mActivitySession == null && data != null) {
                mActivitySession = data;
                SetUpUi(view);
            }
            mActivitySessionModel.getSelectedActivitySession().removeObservers(getActivity());
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SESSION_TAG, mActivitySession);
    }
}
