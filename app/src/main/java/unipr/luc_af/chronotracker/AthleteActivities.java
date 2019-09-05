package unipr.luc_af.chronotracker;


import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.Calendar;
import java.util.GregorianCalendar;
import unipr.luc_af.adapters.ActivitySessionAdapter;
import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.PopupItemsModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.chronotracker.helpers.Utils;

import static unipr.luc_af.chronotracker.MainActivity.ATHLETE_ACTIVITY_SUMMARY_TAG;

public class AthleteActivities extends Fragment {
    //LOGIC
    private static final String CURRENT_DAY_TAG = "activities_current_day_tag";
    private static final String CURRENT_ATHLETE_TAG = "current_athlete_tag";
    private TitleBarModel mTitleModel;
    private AthleteModel mAthleteModel;
    private PopupItemsModel mPopupItemsModel;
    private Calendar mCurrentSelectedDay;

    //UI
    private Athlete mSelectedAthlete;
    private RecyclerView mActivitiesList;
    private TextView mNoActivitiesMessage;
    private ActivitySessionAdapter mActivitySessionAdapter;
    private ActivitySessionModel mActivitySessionModel;
    private Animation mFadeIn;
    private CalendarView mCalendarView;


    public AthleteActivities() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Utils.getInstance().setToolBarNavigation((AppCompatActivity)getActivity());
        View view = inflater.inflate(R.layout.fragment_athlete_activities, container, false);
        SetUpModels(savedInstanceState);
        SetUpUi(view,savedInstanceState);
        return view;
    }

    private void SetUpModels(Bundle savedInstanceState){
        Utils utils = Utils.getInstance();
        mPopupItemsModel = new ViewModelProvider(getActivity()).get(PopupItemsModel.class);
        mPopupItemsModel.setActiveItems(new int[] {R.id.menu_export_all, R.id.menu_export_current, R.id.menu_start_tracking});
        mActivitySessionModel = new ViewModelProvider(getActivity()).get(ActivitySessionModel.class);
        mAthleteModel = new ViewModelProvider(getActivity()).get(AthleteModel.class);
        if(savedInstanceState == null) {
            mAthleteModel.getSelectedAthlete().observe(getActivity(), (athlete) -> {
                mTitleModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
                mSelectedAthlete = athlete;
                mTitleModel.setTitle(
                        utils.concatString(" ",
                                athlete.name,
                                athlete.surname,
                                getActivity().getString(R.string.athlete_activities)));
                getActivitiesOfToday();
            });
            mActivitySessionModel.getActivitySession().observe(getActivity(),(session)->{
                if(mCurrentSelectedDay != null) {
                    getActivitiesOfDay(mCurrentSelectedDay.get(Calendar.YEAR),
                            mCurrentSelectedDay.get(Calendar.MONTH),
                            mCurrentSelectedDay.get(Calendar.DATE));
                }
            });
        }
    }

    private void SetUpUi(View view, Bundle savedInstanceState){
        mCalendarView = view.findViewById(R.id.calendar_activities);
        mNoActivitiesMessage = view.findViewById(R.id.no_activities_message);
        mFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        mNoActivitiesMessage.setText(getActivity().getString(R.string.no_activities_records));
        mCurrentSelectedDay = Calendar.getInstance();
        mCalendarView.setOnDateChangeListener((CalendarView var1, int year, int month, int day) ->{
            getActivitiesOfDay(year,month,day);
            mCurrentSelectedDay.set(year,month,day);
        });
        if(savedInstanceState != null){
            mCurrentSelectedDay = (Calendar)savedInstanceState.getSerializable(CURRENT_DAY_TAG);
            mCalendarView.setDate(mCurrentSelectedDay.getTimeInMillis(),true,true);
            mSelectedAthlete = savedInstanceState.getParcelable(CURRENT_ATHLETE_TAG);
            getActivitiesOfDay(mCurrentSelectedDay.get(Calendar.YEAR),mCurrentSelectedDay.get(Calendar.MONTH),mCurrentSelectedDay.get(Calendar.DAY_OF_MONTH));
        }

        mActivitiesList = view.findViewById(R.id.recycle_list_activities);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mActivitiesList.setLayoutManager(mLayoutManager);
        mActivitySessionAdapter = new ActivitySessionAdapter((v,session) -> activitySessionClick(session));
        mActivitiesList.setAdapter(mActivitySessionAdapter);
    }

    private void activitySessionClick(ActivitySession session){
        mActivitySessionModel.setSelectedActivitySession(session);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.horizontal_in_left,
                        R.anim.horizontal_out_left,
                        R.anim.horizontal_in,
                        R.anim.horizontal_out)
                .replace(R.id.root,new ActivitySummary(),ATHLETE_ACTIVITY_SUMMARY_TAG)
                .addToBackStack(ATHLETE_ACTIVITY_SUMMARY_TAG)
                .commit();
    }


    public void getActivitiesOfToday(){
        Calendar today = new GregorianCalendar();
        getActivitiesOfDay(today.get(Calendar.YEAR),today.get(Calendar.MONTH),today.get(Calendar.DAY_OF_MONTH));
    }

    public void getActivitiesOfDay(int year, int month, int day){
        Calendar date = new GregorianCalendar(year,month,day);
        DatabaseResult activitiesOfDay = (cursor)->{
            cursor.moveToNext();
            if(cursor.getCount() == 0){
                mNoActivitiesMessage.setVisibility(View.VISIBLE);
                mNoActivitiesMessage.startAnimation(mFadeIn);
                mActivitiesList.setVisibility(View.GONE);
            }else{
                mNoActivitiesMessage.setVisibility(View.GONE);
                mActivitiesList.setVisibility(View.VISIBLE);
                mActivitiesList.startAnimation(mFadeIn);
                mActivitySessionAdapter.setActivitySessions(buildActivitySessions(cursor));
            }
        };
        Database.getInstance().getActivitiesOfDay(date, mSelectedAthlete, activitiesOfDay);
    }

    private ActivitySession[] buildActivitySessions(Cursor cursor){
        ActivitySession[] activitySessions = new ActivitySession[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            activitySessions[i] = new ActivitySession(
                    cursor.getLong(0), cursor.getLong(1),
                    cursor.getLong(2), (cursor.isNull(3) ? null : cursor.getLong(3)),
                    cursor.getLong(4), cursor.getLong(5),
                    cursor.getLong(6), cursor.getInt(7),
                    null);
            cursor.moveToNext();
        }
        return activitySessions;
    }

    @Override
    public void onStop() {
        super.onStop();
        mAthleteModel.getSelectedAthlete().removeObservers(getActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mCurrentSelectedDay = (Calendar) savedInstanceState.getSerializable(CURRENT_DAY_TAG);
            mSelectedAthlete = savedInstanceState.getParcelable(CURRENT_ATHLETE_TAG);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CURRENT_DAY_TAG,mCurrentSelectedDay);
        outState.putParcelable(CURRENT_ATHLETE_TAG,mSelectedAthlete);
    }
}
