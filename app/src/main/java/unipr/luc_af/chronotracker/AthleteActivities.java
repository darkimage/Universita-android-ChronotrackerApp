package unipr.luc_af.chronotracker;


import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import unipr.luc_af.adapters.ActivitySessionAdapter;
import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.classes.Lap;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.PopupItemsModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.chronotracker.helpers.Utils;

public class AthleteActivities extends Fragment {
    private TitleBarModel mTitleModel;
    private AthleteModel mAthleteModel;
    private Observer<Athlete> mAthleteObserver;
    private PopupItemsModel mPopupItemsModel;
    private Athlete mSelectedAthlete;
    private RecyclerView mActivitiesList;
    private TextView mNoActivitiesMessage;
    private ActivitySessionAdapter mActivitySessionAdapter;
    private ActivitySessionModel mActivitySessionModel;
    private Animation mFadeIn;
    private Calendar mCurrentSelectedDay;

    public AthleteActivities() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Utils.getInstance().setToolBarNavigation((AppCompatActivity)getActivity());
        View view = inflater.inflate(R.layout.fragment_athlete_activities, container, false);

        CalendarView calendarView = view.findViewById(R.id.calendar_activities);
        mNoActivitiesMessage = view.findViewById(R.id.no_activities_message);
        mFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        mNoActivitiesMessage.setText(getActivity().getString(R.string.no_activities_records));
        calendarView.setOnDateChangeListener((CalendarView var1, int year, int month, int day) ->{
                getActivitiesOfDay(year,month,day);
                mCurrentSelectedDay = Calendar.getInstance();
                mCurrentSelectedDay.set(year,month,day);
        });

        mActivitiesList = view.findViewById(R.id.recycle_list_activities);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mActivitiesList.setLayoutManager(mLayoutManager);
        mActivitySessionAdapter = new ActivitySessionAdapter();
        mActivitiesList.setAdapter(mActivitySessionAdapter);

        return view;
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
//                mAthleteModel.setCurrentDayActivities();
            }
        };
        Database.getInstance().getActivitiesOfDay(date,mSelectedAthlete, activitiesOfDay);
    }

    private ActivitySession[] buildActivitySessions(Cursor cursor){
        ActivitySession[] activitySessions = new ActivitySession[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            activitySessions[i] = new ActivitySession(
                    cursor.getLong(0), cursor.getLong(1),
                    cursor.getLong(2), cursor.getLong(3),
                    cursor.getLong(4), cursor.getLong(5),
                    cursor.getLong(6), cursor.getInt(7),
                    null);
            cursor.moveToNext();
        }
        return activitySessions;
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils utils = Utils.getInstance();
        mPopupItemsModel = new ViewModelProvider(getActivity()).get(PopupItemsModel.class);
        mPopupItemsModel.setActiveItems(new int[] {R.id.menu_export_all, R.id.menu_export_current, R.id.menu_start_tracking});
        mActivitySessionModel = new ViewModelProvider(getActivity()).get(ActivitySessionModel.class);
        mAthleteModel = new ViewModelProvider(getActivity()).get(AthleteModel.class);
        mAthleteObserver = (athlete) ->{
            mTitleModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
            mSelectedAthlete = athlete;
            mTitleModel.setTitle(
                    utils.concatString(" ",
                        athlete.name,
                        athlete.surname,
                        getActivity().getString(R.string.athlete_activities)));
            getActivitiesOfToday();
        };
        mAthleteModel.getSelectedAthlete().observe(getActivity(), mAthleteObserver);

        final Observer<ActivitySession> sessionObserver = (session)->{
            if(mCurrentSelectedDay != null) {
                getActivitiesOfDay(mCurrentSelectedDay.get(Calendar.YEAR),
                        mCurrentSelectedDay.get(Calendar.MONTH),
                        mCurrentSelectedDay.get(Calendar.DATE));
            }
        };
        mActivitySessionModel.getActivitySession().observe(getActivity(),sessionObserver);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAthleteModel.getSelectedAthlete().removeObserver(mAthleteObserver);
        mPopupItemsModel.removeAllActiveItems();
    }

}
