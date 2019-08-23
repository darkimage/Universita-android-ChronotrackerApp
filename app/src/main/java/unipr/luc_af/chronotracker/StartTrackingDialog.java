package unipr.luc_af.chronotracker;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import unipr.luc_af.classes.ActivityGeneral;
import unipr.luc_af.classes.ActivitySport;
import unipr.luc_af.classes.ActivitySportSpecialization;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.classes.StartSessionData;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.services.Database;
import unipr.luc_af.services.Utils;

public class StartTrackingDialog extends DialogFragment {
    private ActivitySessionModel mActivitySessionModel;
    private AthleteModel mAthleteModel;
    private Athlete mCurrentAthlete;
    private TextView mNoActivityTypes;
    private Button mCancelButton;
    private Button mStartButton;
    private Animation mExpandFromTopAnim;
    private ActivitySport mSelectedActivity;
    private ActivitySportSpecialization mSelectedActivityType;
    private boolean mSelectedActivityHasTypes = false;
    private RadioGroup mActivityGroup;
    private RadioGroup mActivityTypeGroup;

    public StartTrackingDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Utils.getInstance().setToolBarNavigation((AppCompatActivity) getActivity());
        View view = inflater.inflate(R.layout.dialog_start_tracking, container, false);

        mActivityGroup = view.findViewById(R.id.dialog_radio_group_activities);
        mActivityGroup.setOnCheckedChangeListener((group, id) ->{
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton currentRadio = group.findViewById(checkedRadioButtonId);
            ActivitySport currentActivity = (ActivitySport)currentRadio.getTag();
            mActivitySessionModel.setDialogSelectedActivity(currentActivity);
        });
        mActivityTypeGroup = view.findViewById(R.id.dialog_radio_group_activities_type);
        mActivityTypeGroup.setOnCheckedChangeListener((group, id) ->{
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton currentRadio = group.findViewById(checkedRadioButtonId);
            ActivitySportSpecialization currentActivityType = (ActivitySportSpecialization)currentRadio.getTag();
            mActivitySessionModel.setDialogSelectedActivityType(currentActivityType);
        });

        mCancelButton = view.findViewById(R.id.cancel_action);
        mCancelButton.setOnClickListener((v) ->{
            userCancel();
            this.dismiss();
        });
        mStartButton = view.findViewById(R.id.start_action);
        mStartButton.setEnabled(false);
        mStartButton.setOnClickListener((v) -> {
            StartSessionData data = new StartSessionData(mCurrentAthlete,mSelectedActivity,mSelectedActivityType);
            mActivitySessionModel.setSessionStartData(data);
            this.dismiss();
        });

        mExpandFromTopAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.expand_from_top);

        DatabaseResult activityArray = (cursor) -> {
            ActivitySport[] activities = getActivities(cursor);
            populateRadioGroup(mActivityGroup,activities);
        };
        Database.getInstance().getActivities(activityArray);

        mNoActivityTypes = view.findViewById(R.id.dialog_activity_no_type_message);

        mActivitySessionModel = new ViewModelProvider(getActivity()).get(ActivitySessionModel.class);
        final Observer<ActivitySport> selectedActivity = (activitySport) -> setActivityTypes(activitySport);
        final Observer<ActivitySportSpecialization> selectedActivityType = (activityType) -> getSelectedActivityType(activityType);
        mActivitySessionModel.getDialogSelectedActivity().observe(getActivity(),selectedActivity);
        mActivitySessionModel.getDialogSelectedActivityType().observe(getActivity(),selectedActivityType);

        mAthleteModel = new ViewModelProvider(getActivity()).get(AthleteModel.class);
        final Observer<Athlete> athleteObserver = (athlete) -> mCurrentAthlete = athlete;
        mAthleteModel.getSelectedAthlete().observe(getActivity(),athleteObserver);
        return view;
    }


    private <E extends ActivityGeneral> void populateRadioGroup(RadioGroup group, E[] data){
        for (int i = 0; i < data.length; i++) {
            RadioButton activityButton = new RadioButton(getActivity());
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 20, 0, 0);
            activityButton.setLayoutParams(params);
//            TypedValue outValue = new TypedValue();
//            getActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
//            activityButton.setBackgroundResource(outValue.resourceId);
            activityButton.setText(data[i].getName());
            activityButton.setId(i);
            activityButton.setTag(data[i]);
            group.addView(activityButton);
        }
    }

    private ActivitySport[] getActivities(Cursor cursor){
        ActivitySport[] activities = new ActivitySport[cursor.getCount()];
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            activities[i] = new ActivitySport(cursor.getLong(0), cursor.getString(1));
            cursor.moveToNext();
        }
        return activities;
    }

    private void getSelectedActivityType(ActivitySportSpecialization activityType){
        mSelectedActivityType = activityType;
        setActionButtonState();
    }

    private void setActivityTypes(ActivitySport selectedActivity){
        mSelectedActivity = selectedActivity;
        mActivitySessionModel.setDialogSelectedActivityType(null);
        mActivityTypeGroup.removeAllViews();
        DatabaseResult activityTypes = (cursor) -> {
            cursor.moveToNext();
            ActivitySportSpecialization[] activitySportTypes = new ActivitySportSpecialization[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                activitySportTypes[i] = new ActivitySportSpecialization(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2));
                cursor.moveToNext();
            }
            if(activitySportTypes.length != 0) {
                mSelectedActivityHasTypes = true;
                mActivityTypeGroup.setVisibility(View.VISIBLE);
                mNoActivityTypes.setVisibility(View.GONE);
                mActivityTypeGroup.startAnimation(mExpandFromTopAnim);
                populateRadioGroup(mActivityTypeGroup,activitySportTypes);

            }else{
                mSelectedActivityHasTypes = false;
                mActivityTypeGroup.setVisibility(View.GONE);
                mNoActivityTypes.setVisibility(View.VISIBLE);
            }
            setActionButtonState();
        };
        Database.getInstance().getActivitiesTypesOfActivity(selectedActivity,activityTypes);
    }

    private void setActionButtonState(){
        if(mSelectedActivity != null && !mSelectedActivityHasTypes){
            mStartButton.setEnabled(true);
        }else if(mSelectedActivity != null && mSelectedActivityType != null){
            mStartButton.setEnabled(true);
        }else {
            mStartButton.setEnabled(false);
        }
    }

    private void userCancel(){
        mActivitySessionModel.setDialogSelectedActivity(null);
        mActivitySessionModel.setDialogSelectedActivityType(null);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        userCancel();
    }

    @Override
    public void onResume() {
        Window window = getDialog().getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        window.setLayout((int) (size.x * 0.90), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        super.onResume();
    }
}
