package unipr.luc_af.chronotracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
    private String SAVED_ACTIVITY_ID = "activity_id";
    private String SAVED_ACTIVITY_TYPE_ID = "activity_type_id";
    private MutableLiveData<Void> updatePositiveButton = new MutableLiveData<>();
    private ActivitySessionModel mActivitySessionModel;
    private AthleteModel mAthleteModel;
    private Athlete mCurrentAthlete;
    private TextView mNoActivityTypes;
    private Animation mExpandFromTopAnim;
    private Animation mCollapseFromTopAnim;
    private ActivitySport mSelectedActivity;
    private ActivitySportSpecialization mSelectedActivityType;
    private boolean mSelectedActivityHasTypes = false;
    private RadioGroup mActivityGroup;
    private RadioGroup mActivityTypeGroup;
    private Context mContext;
    private boolean previousHasEntries = false;
    private int mActivitySelectedId = -1;
    private int mActivityTypeSelectedId = -1;

    public StartTrackingDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        if(savedInstanceState != null){
            mActivityTypeSelectedId = savedInstanceState.getInt(SAVED_ACTIVITY_TYPE_ID);
            mActivitySelectedId = savedInstanceState.getInt(SAVED_ACTIVITY_TYPE_ID);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mActivitySessionModel = new ViewModelProvider(getActivity()).get(ActivitySessionModel.class);
        final Observer<ActivitySport> selectedActivity = (activitySport) -> setActivityTypes(activitySport);
        final Observer<ActivitySportSpecialization> selectedActivityType = (activityType) -> getSelectedActivityType(activityType);
        mActivitySessionModel.getDialogSelectedActivity().observe(getActivity(),selectedActivity);
        mActivitySessionModel.getDialogSelectedActivityType().observe(getActivity(),selectedActivityType);

        mContext = getContext();
        Utils.getInstance().setToolBarNavigation((AppCompatActivity) getActivity());
        View view = inflater.inflate(R.layout.dialog_start_tracking, null);

        mActivityGroup = view.findViewById(R.id.dialog_radio_group_activities);
        mActivityTypeGroup = view.findViewById(R.id.dialog_radio_group_activities_type);

        mActivityGroup.setOnCheckedChangeListener((group, id) -> {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton currentRadio = group.findViewById(checkedRadioButtonId);
            ActivitySport currentActivity = (ActivitySport) currentRadio.getTag();
            mActivitySessionModel.setDialogSelectedActivity(currentActivity);
        });

        mActivityTypeGroup.setOnCheckedChangeListener((group, id) -> {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton currentRadio = group.findViewById(checkedRadioButtonId);
            ActivitySportSpecialization currentActivityType = (ActivitySportSpecialization) currentRadio.getTag();
            mActivitySessionModel.setDialogSelectedActivityType(currentActivityType);
        });

        mExpandFromTopAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.expand_from_top);
        mCollapseFromTopAnim =  AnimationUtils.loadAnimation(getActivity(), R.anim.collapse_from_top);

        DatabaseResult activityArray = (cursor) -> {
            ActivitySport[] activities = getActivities(cursor);
            populateRadioGroup(mActivityGroup, activities);
            if (savedInstanceState != null) {
                RadioButton activityRadio = mActivityGroup.findViewById(mActivitySelectedId);
                activityRadio.toggle();
            }
        };
        Database.getInstance().getActivities(activityArray);

        mNoActivityTypes = view.findViewById(R.id.dialog_activity_no_type_message);

        mAthleteModel = new ViewModelProvider(getActivity()).get(AthleteModel.class);
        final Observer<Athlete> athleteObserver = (athlete) -> mCurrentAthlete = athlete;
        mAthleteModel.getSelectedAthlete().observe(getActivity(),athleteObserver);

        builder.setView(view)
                .setTitle(R.string.start_tracking_header)
                .setPositiveButton("Start",
                (dialog,whichButton) -> {
                    StartSessionData data = new StartSessionData(mCurrentAthlete,mSelectedActivity,mSelectedActivityType);
                    mActivitySessionModel.setSessionStartData(data);
                    this.dismiss();
                })
                .setNegativeButton("Cancel",
                    (dialog, whichButton) -> {
                        dialog.dismiss();
                    });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener((dialogInterface) ->{
            final Observer<Void> updateButton = (v) ->{
                setActionButtonState(dialogInterface);
            };
            updatePositiveButton.observe(getActivity(),updateButton);
        });
        updatePositiveButton.setValue(null);
        return dialog;
    }

    private <E extends ActivityGeneral> void populateRadioGroup(RadioGroup group, E[] data){
        for (int i = 0; i < data.length; i++) {
            RadioButton activityButton = new RadioButton(mContext);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 20, 0, 0);
            activityButton.setLayoutParams(params);
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
        updatePositiveButton.setValue(null);
    }


    private void setActivityTypes(ActivitySport selectedActivity){
        mSelectedActivity = selectedActivity;
        mSelectedActivityHasTypes = false;
        mActivitySessionModel.setDialogSelectedActivityType(null);
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
                if (!previousHasEntries) {
                    showActivityTypesRadioButtons(activitySportTypes);
                }else{
                    mActivityTypeGroup.postDelayed(() ->{
                        showActivityTypesRadioButtons(activitySportTypes);
                    }, mCollapseFromTopAnim.getDuration());
                }
                previousHasEntries = true;
            }else{
                previousHasEntries = false;
                mSelectedActivityHasTypes = false;
                mActivityTypeSelectedId = -1;
                mActivityTypeGroup.postDelayed(() -> {
                    mActivityTypeGroup.setVisibility(View.GONE);
                    mNoActivityTypes.setVisibility(View.VISIBLE);
                }, mCollapseFromTopAnim.getDuration());
                mActivityTypeGroup.removeAllViews();
            }
            updatePositiveButton.setValue(null);
        };
        Database.getInstance().getActivitiesTypesOfActivity(selectedActivity,activityTypes);
    }

    private void showActivityTypesRadioButtons(ActivitySportSpecialization[] activitySportTypes){
        mActivityTypeGroup.removeAllViews();
        mSelectedActivityHasTypes = true;
        mActivityTypeGroup.setVisibility(View.VISIBLE);
        mNoActivityTypes.setVisibility(View.GONE);
        mActivityTypeGroup.startAnimation(mExpandFromTopAnim);
        populateRadioGroup(mActivityTypeGroup, activitySportTypes);
        RadioButton activityRadio = mActivityTypeGroup.findViewById(mActivityTypeSelectedId);
        if(activityRadio != null) {
            activityRadio.toggle();
        }
    }

    private void setActionButtonState(DialogInterface dialogInterface){
        Button posButton = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
        if(mSelectedActivity != null && !mSelectedActivityHasTypes){
            posButton.setEnabled(true);
        }else if(mSelectedActivity != null && mSelectedActivityType != null){
            posButton.setEnabled(true);
        }else {
            posButton.setEnabled(false);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(SAVED_ACTIVITY_ID,mActivityGroup.getCheckedRadioButtonId());
        if(mSelectedActivityHasTypes) {
            outState.putInt(SAVED_ACTIVITY_TYPE_ID, mActivityTypeGroup.getCheckedRadioButtonId());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivitySessionModel.setDialogSelectedActivityType(null);
        mActivitySessionModel.setDialogSelectedActivity(null);
        updatePositiveButton.setValue(null);
        mActivitySessionModel.getDialogSelectedActivity().removeObservers(getActivity());
        mActivitySessionModel.getDialogSelectedActivity().removeObservers(getActivity());
        updatePositiveButton.removeObservers(getActivity());
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
